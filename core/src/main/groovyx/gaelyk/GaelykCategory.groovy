/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gaelyk

import com.google.appengine.api.mail.MailService.Message
import com.google.appengine.api.mail.MailService
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.Transaction
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.labs.taskqueue.Queue
import com.google.appengine.api.labs.taskqueue.TaskHandle
import com.google.appengine.api.labs.taskqueue.TaskOptions
import com.google.appengine.api.xmpp.XMPPService
import com.google.appengine.api.xmpp.MessageBuilder
import groovy.xml.StreamingMarkupBuilder
import com.google.appengine.api.xmpp.JID
import com.google.appengine.api.xmpp.SendResponse
import com.google.appengine.api.xmpp.MessageType
import com.google.appengine.api.xmpp.Presence
import groovy.util.slurpersupport.GPathResult
import com.google.appengine.api.memcache.MemcacheService
import com.google.appengine.api.mail.MailService.Attachment
import com.google.appengine.api.datastore.Email
import com.google.appengine.api.datastore.Text
import com.google.appengine.api.datastore.Category as DatastoreCategory
import com.google.appengine.api.blobstore.BlobKey
import com.google.appengine.api.datastore.Link
import com.google.appengine.api.datastore.PhoneNumber
import com.google.appengine.api.datastore.PostalAddress
import com.google.appengine.api.datastore.Rating
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import com.google.appengine.api.datastore.ShortBlob
import com.google.appengine.api.datastore.Blob
import com.google.appengine.api.datastore.GeoPt
import com.google.appengine.api.blobstore.BlobstoreInputStream
import com.google.appengine.api.blobstore.ByteRange
import com.google.appengine.api.blobstore.BlobInfo
import com.google.appengine.api.blobstore.BlobInfoFactory
import com.google.appengine.api.blobstore.BlobstoreServiceFactory
import com.google.appengine.api.datastore.Key
import com.google.appengine.api.NamespaceManager
import com.google.appengine.api.images.Transform
import com.google.appengine.api.images.CompositeTransform
import javax.servlet.http.HttpServletResponse
import com.google.appengine.api.images.Image
import com.google.appengine.api.images.ImagesServiceFactory as ISF
import groovyx.gaelyk.cache.CacheHandler
import com.google.appengine.api.memcache.Expiration

import com.google.appengine.api.capabilities.Capability
import com.google.appengine.api.capabilities.CapabilityStatus
import com.google.appengine.api.capabilities.CapabilitiesService

import com.google.appengine.api.urlfetch.HTTPResponse
import com.google.appengine.api.urlfetch.URLFetchService
import com.google.appengine.api.urlfetch.URLFetchServiceFactory
import com.google.appengine.api.urlfetch.HTTPRequest
import com.google.appengine.api.urlfetch.HTTPMethod
import com.google.appengine.api.urlfetch.FetchOptions
import com.google.appengine.api.urlfetch.HTTPHeader

/**
 * Category methods decorating the Google App Engine SDK classes
 * adding new shortcut methods to simplify the usage of the SDK
 * from within Groovy servlets and templates.
 *
 * @author Guillaume Laforge
 */
class GaelykCategory {

    // ----------------------------------------------------------------
    // New methods related to the Servlet API,
    // not covered by the ServletCategory from Groovy
    // ----------------------------------------------------------------

    /**
     * Adds a fake <code>getHeaders()</code> method to <code>HttpServletResponse</code>.
     * It allows the similar subscript notation syntax of request,
     * but for setting or overriding a header on the response
     * (ie. calling <code>response.setHeader()</code>).
     * It also allows the leftShift notation for adding a header to the response
     * (ie. calling <code>response.addHeader()</code>.
     *
     * <pre><code>
     *  // sets or overrides the header 'a'
     *  response.headers['a'] == 'b'
     *
     *  // adds an additional value to an existing header
     *  // or sets a first value for a non-existant header
     *  response.headers['a'] << 'b' 
     * </code></pre>
     *
     * @param response
     * @return a custom map on which you can use the subscript notation to add headers
     */
    static Map getHeaders(HttpServletResponse response) {
        new HashMap() {
            Object put(Object k, Object v) {
                def vString = v.toString()
                response.setHeader(k.toString(), vString)
                return vString
            }

            Object get(Object k) {
                [leftShift: {
                    def vString = it.toString()
                    response.addHeader(k.toString(), vString)
                    return vString }]
            }
        }
    }

    // ----------------------------------------------------------------
    // Category methods dedicated to the Mail service
    // ----------------------------------------------------------------

    /**
     * Create a <code>MailService.Message</code> out of Map parameters.
     * Each map key must correspond to a valid property on the message object.
     */
    private static Message createMessageFromMap(Map m) {
        def msg = new Message()
        m.each { k, v ->
            // to and bcc fields contain collection of addresses
            // so if only one is provided, wrap it in a collection
            if (k in ['to', 'bcc'] && v instanceof String) v = [v]

            // adds a 'from' alias for 'sender'
            if (k == 'from') k = 'sender'

            // single email attachment
            if (k == 'attachment' && v instanceof Map) {
                k = 'attachments'
                v = [new Attachment(v.fileName, v.data)] as Attachment[]
            }

            // collects Attachments and maps representing attachments as a MailMessage.Attachment collection
            if (k == 'attachments') {
                v = v.collect { attachment ->
                    if (attachment instanceof Attachment)
                        attachment
                    else if (attachment instanceof Map)
                        new Attachment(attachment.fileName, attachment.data)
                } as Attachment[]
            }

            // set the property on Message object
            msg."$k" = v
        }
        return msg
    }

    /**
     * Additional <code>send()</code> method taking a map as parameter.
     * The map can contain the normal properties of the
     * <code>MailService.Message</code> class.
     *
     * @throws groovy.lang.MissingPropertyException when the key doesn't correspond
     * to a property of the <code>MailService.Message</code> class.
     */
    static void send(MailService mailService, Map m) {
        Message msg = createMessageFromMap(m)
        mailService.send msg
    }


    /**
     * Additional <code>sendToAdmins()</code> method for sending emails to the application admins. 
     * This method is taking a map as parameter.
     * The map can contain the normal properties of the
     * <code>MailService.Message</code> class.
     *
     * @param
     *
     * @throws groovy.lang.MissingPropertyException when the key doesn't correspond
     * to a property of the <code>MailService.Message</code> class.
     */
    static void sendToAdmins(MailService mailService, Map m) {
        Message msg = createMessageFromMap(m)
        mailService.sendToAdmins msg 
    }

    // ----------------------------------------------------------------
    // Category methods dedicated to the low-level DataStore service
    // ----------------------------------------------------------------

    /**
     * Provides a shortcut notation to get a property of an entity.
     * Instead of writing
     * <code>entity.getProperty('propertyName')</code>
     * You can use the shortcut
     * <code>entity['propertyName']</code>
     */
    static Object getAt(Entity entity, String name) {
        entity.getProperty(name)
    }

    /**
     * Provides a shortcut notation to get a property of an entity.
     * Instead of writing
     * <code>entity.getProperty('propertyName')</code>
     * You can use the shortcut
     * <code>entity.propertyName</code>
     */
    static Object get(Entity entity, String name) {
        entity.getProperty(name)
    }

    /**
     * Provides a shortcut notation to set a property of an entity.
     * Instead of writing
     * <code>entity.setProperty('propertyName', value)</code>
     * You can use the shortcut
     * <code>entity['propertyName'] = value</code>
     */
    static void setAt(Entity entity, String name, Object value) {
        entity.setProperty(name, transformeEntityFieldValue(value))
    }

    /**
     * Provides a shortcut notation to set a property of an entity.
     * Instead of writing
     * <code>entity.setProperty('propertyName', value)</code>
     * You can use the shortcut
     * <code>entity.propertyName = value</code>
     */
    static void set(Entity entity, String name, Object value) {
        entity.setProperty(name, transformeEntityFieldValue(value))
    }

    // All transformations that needs to be done on entity fields
    // prior to their insertion in the datastore
    private static Object transformeEntityFieldValue(Object value) {
        // the datastore doesn't allow to store GStringImpl
        // so we need a toString() first
        value instanceof GString ? value.toString() : value
    }

    /**
     * Save this entity in the data store.
     * Usage: <code>entity.save()</code>
     */
    static Object save(Entity entity) {
        DatastoreServiceFactory.datastoreService.put(entity)
    }

    /**
     * Delete this entity from the data store.
     * Usage: <code>entity.delete()</code>
     */
    static void delete(Entity entity) {
        DatastoreServiceFactory.datastoreService.delete(entity.key)
    }

    /**
     * Delete the entity represented by that key, from the data store.
     * Usage: <code>key.delete()</code> 
     */
    static void delete(Key key) {
        DatastoreServiceFactory.datastoreService.delete(key)
    }

    /**
     * With this method, transaction handling is done transparently.
     * The transaction is committed if the closure executed properly.
     * The transaction is rollbacked if anything went wrong.
     * You can use this method as follows:
     * <code>
     * datastoreService.withTransaction { transaction ->
     *     // do something in that transaction
     * }
     * </code>
     */
    static Transaction withTransaction(DatastoreService service, Closure c) {
        Transaction transaction = service.beginTransaction()
        try {
            // pass the transaction as single parameter of the closure
            c(transaction)
            // commit the transaction if the closure executed without throwing an exception
            transaction.commit()
        } catch (e) {
            // rollback on error
            transaction.rollback()
            // rethrow the exception
            throw e
        }
        return transaction
    }

    /**
     * Set the <code>Entity</code> properties with the key / value pairs of the map,
     * using the leftshift operator as follows:
     * <code>entity &lt;&lt; params</code>
     */
    static Entity leftShift(Entity entity, Map params) {
        params.each { k, v -> entity[k] = v }
        return entity
    }

    // ------------------------------
    // Additional converter methods
    // ------------------------------

    /**
     * Converter method for converting strings into various GAE specific types
     * <pre><code>
     *  "foo@bar.com" as Email
     *  "http://www.google.com" as Link
     *  "+3361234543" as PhoneNumber
     *  "50 avenue de la Madeleine, Paris" as PostalAddress
     *  "groovy" as DatastoreCategory
     *  "32" as Rating
     *  "long text" as Text
     *  "foobar" as BlobKey
     *  "foo@gmail.com" as JID
     * </code></pre>
     */
    static Object asType(String self, Class clazz) {
        if (clazz == Email)
            new Email(self)
        else if (clazz == Text)
            new Text(self)
        else if (clazz == BlobKey)
            new BlobKey(self)
        else if (clazz == Link)
            new Link(self)
        else if (clazz == DatastoreCategory)
            new DatastoreCategory(self)
        else if (clazz == PhoneNumber)
            new PhoneNumber(self)
        else if (clazz == PostalAddress)
            new PostalAddress(self)
        else if (clazz == Rating)
            new Rating(new Integer(self))
        else if (clazz == JID)
            new JID(self)
        else DefaultGroovyMethods.asType(self, clazz)
    }

    /**
     * Gaelyk supports a simplistic object/entity mapping, thanks to type coercion.
     * You can use this type coercion mechanism to coerce POJOs/POGOs and datastore Entities.
     * The <code>Entity</code> kind will be the simple name of the POJO/POGO (same approach as Objectify).
     * So with this mechanism, you can do:
     *
     * <pre><code>
     *  class Person { String name, int age }
     *
     *  def p = new Person(name: "Guillaume", age: 33)
     *  def e = p as Entity
     *
     *  assert p.name == e.name
     *  assert p.age == e.age
     * </code></pre>
     *
     * @return an instance of Entity 
     */
    static Object asType(Object self, Class clazz) {
        if (clazz == Entity) {
            def e = new Entity(self.class.simpleName)
            self.properties.each { k, v ->
                if (!(k in ['class', 'metaClass'])) {
                    e.setProperty(k, v)
                }
            }
            return e
        } else if (self.class == Entity) {
            asType((Entity)self, clazz)
        } else if (self.class == String) {
            asType((String)self, clazz)
        } else DefaultGroovyMethods.asType(self, clazz)
    }

    /**
     * Gaelyk supports a simplistic object/entity mapping, thanks to type coercion.
     * You can use this type coercion mechanism to coerce POJOs/POGOs and datastore Entities.
     * The <code>Entity</code> kind will be the simple name of the POJO/POGO (same approach as Objectify).
     * So with this mechanism, you can do:
     *
     * <pre><code>
     *  class Person { String name, int age }
     *
     *  def e = new Entity("Person")
     *  e.name = "Guillaume"
     *  e.age = 33
     *
     *  def p = e as Person
     *
     *  assert e.name == p.name
     *  assert e.age == p.age
     * </code></pre>
     *
     * @return an instance of a POJO/POGO to coerce into
     */
    
    static Object asType(Entity self, Class clazz) {
        return clazz.newInstance(self.properties)
    }

    /**
     * Converter method for converting a URL into a Link instance
     * <pre><code>
     *  new URL("http://gaelyk.appspot.com") as Link
     * </code></pre>
     */
    static Link asType(URL self, Class linkClass) {
        if (linkClass == Link)
            new Link(self.toString())
        else DefaultGroovyMethods.asType(self, linkClass)
    }

    /**
     * Converter method for converting an integer into a Rating instance
     * <pre><code>
     *  32 as Rating
     * </code></pre>
     */
    static Object asType(Integer self, Class ratingClass) {
        if (ratingClass == Rating)
            new Rating(self)
        else DefaultGroovyMethods.asType(self, ratingClass)
    }

    /**
     * Converter method for converting a byte array into a Blob or ShortBlob instance
     * <pre><code>
     *  "some byte".getBytes() as Blob
     *  "some byte".getBytes() as ShortBlob
     * </code></pre>
     */
    static Object asType(byte[] self, Class blobClass) {
        if (blobClass == ShortBlob)
            new ShortBlob(self)
        else if (blobClass == Blob)
            new Blob(self)
        else DefaultGroovyMethods.asType(self, blobClass)
    }

    /**
     * Converter method for converting a pair of numbers (in a list) into a GeoPt instance
     * <pre><code>
     *  [45.32, 54.54f] as GeoPt
     * </code></pre>
     */
    static Object asType(List floatPair, Class geoptClass) {
        if (geoptClass == GeoPt && floatPair.size() == 2 &&
            floatPair.every { it instanceof Number })
                new GeoPt(*floatPair*.floatValue())
        else DefaultGroovyMethods.asType(floatPair, geoptClass)
    }

    /**
     * Converter method for converting an int range to a blobstore <code>ByteRange</code>:
     * <pre><code>
     *     300..400 as ByteRange
     * </code></pre>
     * Note that Groovy already allowed: <code>[300, 400] as ByteRange</code>.
     *
     * @param range the range to convert
     * @param byteRangeClass the class of the byte range
     * @return a <code>ByteRange</code> instance
     */
    static Object asType(IntRange range, Class byteRangeClass) {
        if (byteRangeClass == ByteRange)
            new ByteRange(range.fromInt, range.toInt)
        else DefaultGroovyMethods.asType(range, byteRangeClass)
    }

    // ----------------------------------------------------------------
    // Category methods dedicated to the task queue system
    // ----------------------------------------------------------------

    /**
     * Shorcut to get the name of the Queue.
     * <p>
     * Instead of having to call <code>queue.getQueueName()</code> or <code>queue.queueName</code>,
     * you can use the syntax <code>queue.name</code> which is more concise.
     *
     * @return the name of the queue
     */
    static String getName(Queue selfQueue) {
        selfQueue.getQueueName()
    }

    /**
     * Add a new task on the queue using a map for holding the task attributes instead of a TaskOptions builder object.
     * <p>
     * Allowed keys are: <ul>
     * <li><code>countdownMillis</code></li>
     * <li><code>etaMillis</code></li>
     * <li><code>headers</code> (a map of key/value pairs)</li>
     * <li><code>method</code> (can be 'GET', 'POST', 'PUT', 'DELETE', 'HEAD' or an enum of TaskOptions.Method)</li>
     * <li><code>params</code> (a map of key/value parameters)</li>
     * <li><code>payload</code></li>
     * <li><code>taskName</code></li>
     * <li><code>url</code></li>
     * </ul>
     *
     * @param params the map of task attributes
     * @return a TaskHandle instance
     */
    static TaskHandle add(Queue selfQueue, Map params) {
        def options = TaskOptions.Builder.withDefaults()
        params.each { key, value ->
            if (key in ['countdownMillis', 'etaMillis', 'taskName', 'url']) {
                options = options."$key"(value)
            } else if (key == 'headers') {
                if (value instanceof Map) {
                    value.each { headerKey, headerValue ->
                        options = options.header(headerKey, headerValue)
                    }
                } else {
                    throw new RuntimeException("The headers key/value pairs should be passed as a map.")
                }
            } else if (key == 'method') {
                if (value instanceof TaskOptions.Method) {
                    options = options.method(value)
                } else if(value in ['GET', 'POST', 'PUT', 'DELETE', 'HEAD']) {
                    options = options.method(TaskOptions.Method.valueOf(value))
                } else {
                    throw new RuntimeException("Not a valid method: $value")
                }
            } else if (key == 'params') {
                if (value instanceof Map) {
                    value.each { paramKey, paramValue ->
                        options = options.param(paramKey, paramValue.toString())
                    }
                } else {
                    throw new RuntimeException("The params key/value pairs should be passed as a map.")
                }
            } else if (key == 'payload') {
                if (value instanceof List) {
                    options = options.payload(*(value.collect { it.toString() }))
                } else if (value instanceof String) {
                    options = options.payload(value)
                } else {
                    options = options.payload(value.toString())
                }
            } else {
                throw new RuntimeException("$key is not a valid task option.\n" +
                    "Allowed: countdownMillis, etaMillis, taskName, url, headers, methods, params and payload")
            }
        }
        return selfQueue.add(options)
    }

    /**
     * Add a new task on the queue using a map for holding the task attributes instead of a TaskOptions builder object.
     * This method adds a <code>&lt;&lt;</code> operator on the <code>Queue</code> for adding new tasks to it. 
     * <p>
     * Allowed keys are: <ul>
     * <li><code>countdownMillis</code></li>
     * <li><code>etaMillis</code></li>
     * <li><code>headers</code> (a map of key/value pairs)</li>
     * <li><code>method</code> (can be 'GET', 'POST', 'PUT', 'DELETE', 'HEAD' or an enum of TaskOptions.Method)</li>
     * <li><code>params</code> (a map of key/value parameters)</li>
     * <li><code>payload</code></li>
     * <li><code>taskName</code></li>
     * <li><code>url</code></li>
     * </ul>
     *
     * @param params the map of task attributes
     * @return a TaskHandle instance
     */
    static TaskHandle leftShift(Queue selfQueue, Map params) {
        GaelykCategory.add(selfQueue, params)
    }

    // ----------------------------------------------------------------
    // Category methods dedicated to the Jabber/XMPP support
    // ----------------------------------------------------------------

    /**
     * Send an XMPP/Jabber message with the XMPP service using a map of attributes to build the message.
     * <p>
     * Possible attributes are:
     * <ul>
     * <li>from: the sender Jabber ID represented as a String</li>
     * <li>to: a String or a list of String representing recepients' Jabber IDs</li>
     * <li>type: an instance of the MessageType enum, or a String representation
     * ('CHAT', 'ERROR', 'GROUPCHAT', 'HEADLINE', 'NORMAL')</li>
     * <li>body: a String representing the raw text to send</li>
     * <li>xml: a closure representing the XML you want to send (serialized using StreamingMarkupBuilder)</li>
     * </ul>
     *
     * @param msgAttr a map of attributes as described
     * @return an intance of SendResponse
     */
    static SendResponse send(XMPPService xmppService, Map msgAttr) {
        MessageBuilder msgBuilder = new MessageBuilder()

        if (msgAttr.xml && msgAttr.body) {
            throw new RuntimeException("You have to choose between XML and text bodies, you can't have both!")
        }

        // sets the body of the message
        if (msgAttr.xml) {
            msgBuilder.asXml(true)
            def xml = new StreamingMarkupBuilder().bind(msgAttr.xml)
            msgBuilder.withBody(xml.toString())
        } else if (msgAttr.body) {
            msgBuilder.withBody(msgAttr.body)
        }

        // sets the recepients of the message
        if (msgAttr.to) {
            if (msgAttr.to instanceof String) {
                msgBuilder.withRecipientJids(new JID(msgAttr.to))
            } else if (msgAttr.to instanceof List) {
                msgBuilder.withRecipientJids(msgAttr.to.collect{ new JID(it) } as JID[])
            }
        }

        // sets the sender of the message
        if (msgAttr.from) {
            msgBuilder.withFromJid(new JID(msgAttr.from))
        }

        // sets the type of the message
        if (msgAttr.type) {
            if (msgAttr.type instanceof MessageType) {
                msgBuilder.withMessageType(msgAttr.type)
            } else if (msgAttr.type instanceof String) {
                msgBuilder.withMessageType(MessageType.valueOf(msgAttr.type))
            }
        }

        xmppService.sendMessage(msgBuilder.build())
    }

    /**
     * Send a chat invitation to a Jabber ID.
     *
     * @param the Jabber ID to invite
     */
    static void sendInvitation(XMPPService xmppService, String jabberId) {
        xmppService.sendInvitation(new JID(jabberId))
    }

    /**
     * Send a chat invitation to a Jabber ID from another Jabber ID.
     *
     * @param jabberIdTo the Jabber ID to invite
     * @param jabberIdFrom the Jabber ID to use to send the invitation request
     */
    static void sendInvitation(XMPPService xmppService, String jabberIdTo, String jabberIdFrom) {
        xmppService.sendInvitation(new JID(jabberIdTo), new JID(jabberIdFrom))
    }

    /**
     * Get the presence of a Jabber ID.
     *
     * @param the Jabber ID
     * @return the presence information
     */
    static Presence getPresence(XMPPService xmppService, String jabberId) {
        xmppService.getPresence(new JID(jabberId))
    }

    /**
     * Get the presence of a Jabber ID.
     *
     * @param jabberIdTo the Jabber ID to get the presence from
     * @param jabberIdFrom the Jabber ID to use to send the presence request
     * @return the presence information
     */
    static Presence getPresence(XMPPService xmppService, String jabberIdTo, String jabberIdFrom) {
        xmppService.getPresence(new JID(jabberIdTo), new JID(jabberIdFrom))
    }

    /**
     * Get the sender Jabber ID of the message in the form of a String.
     *
     * @return the Jabber ID of the sender
     */
    static String getFrom(com.google.appengine.api.xmpp.Message message) {
        message.getFromJid().getId()
    }

    /**
     * Get the XML content of this message (if it's an XML message) in the form of a DOM parsed with XmlSlurper.
     *
     * @return the slurped XML document 
     */
    static GPathResult getXml(com.google.appengine.api.xmpp.Message message) {
        if (message.isXml()) {
            def slurper = new XmlSlurper()
            return slurper.parseText(message.getStanza())
        } else {
            throw new RuntimeException("You can't get the XML of this message as this is not an XML message.")
        }
    }

    /**
     * Gets the list of recipients of this message in the form of a list of Jabber ID strings.
     *
     * @return a list of Jabber ID strings
     */
    static List getRecipients(com.google.appengine.api.xmpp.Message message) {
        message.getRecipientJids().collect { it.getId() }
    }

    /**
     * Checks the status of the sending of the message was successful for all its recipients
     */
    static boolean isSuccessful(SendResponse status) {
        status.statusMap.every { it.value == SendResponse.Status.SUCCESS }
    }

    // ----------------------------------------------------------------
    // Category methods dedicated to the memcache service
    // ----------------------------------------------------------------

    /**
     * Get an object from the cache, identified by its key, using the subscript notation:
     * <code>def obj = memcache[key]</code>
     *
     * @param key the key identifying the object to get from the cache
     */
    static Object getAt(MemcacheService memcache, Object key) {
        memcache.get(key)
    }

    /**
     * Get an object from the cache, identified by its key, using the subscript notation:
     * <code>def obj = memcache[key]</code>
     *
     * @param key the key identifying the object to get from the cache
     */
    static Object getAt(MemcacheService memcache, String key) {
        //TODO this method should be removed once we only need a getAt() method taking Object key
        // looks like a bug in current Groovy where the two variants are needed
        memcache.get(key)
    }

    /**
     * Put an object into the cache, identified by its key, using the subscript notation:
     * <code>memcache[key] = value</code>
     *
     * @param key the key identifying the object to put in the cache
     * @param value the value to put in the cache
     */
    static void putAt(MemcacheService memcache, String key, Object value) {
        //TODO this method should be removed once we only need a putAt() method taking Object key
        // looks like a bug in current Groovy where the two variants are needed
        memcache.put(key, value)
    }

    /**
     * Put an object into the cache, identified by its key, using the subscript notation:
     * <code>memcache[key] = value</code>
     *
     * @param key the key identifying the object to put in the cache
     * @param value the value to put in the cache
     */
    static void putAt(MemcacheService memcache, Object key, Object value) {
        memcache.put(key, value)
    }

    /**
     * Shortcut to check whether a key is contained in the cache using the <code>in</code> operator:
     * <code>key in memcache</code>
     */
    static boolean isCase(MemcacheService memcache, Object key) {
        memcache.contains(key)
    }

    /**
     * Clear the cached content for a given URI.
     * 
     * @param uri the URI for which to clear the cache
     * @return the set of keys that have been cleared (should be two in this case)
     */
    static Set clearCacheForUri(MemcacheService memcache, String uri) {
        CacheHandler.clearCacheForUri(uri)
    }

    /**
     * Memoize a closure invocation in memcache.
     * Closure call result are stored in memcache, retaining the closure hashCode and the argument values as key.
     * The results are kept in memcache only up to the 30 seconds request time limit of Google App Engine.
     *
     * <pre><code>
     * def countEntities = memcache.memoize { String kind -> datastore.prepare( new Query(kind) ).countEntities() }
     * def totalPhotos = countEntities('photos')
     * </pre></code>
     *
     * @param closure the closure to memoize
     * @return a memoized closure
     */
    static Closure memoize(MemcacheService memcache, Closure closure) {
        return new Closure(closure.owner) {
            Object call(Object[] args) {
                // a closure call is identified by its hashcode and its call argument values
                def key = [
                        closure: closure.hashCode(),
                        arguments: args.toList()
                ]
                // search for a result for such a call in memcache
                def result = memcache.get(key)
                if (result != null) {
                    // a previous invocation exists
                    return result
                } else {
                    // no previous invocation, so calling the closure and caching the result 
                    result = closure(* args)
                    memcache.put(key, result, Expiration.byDeltaSeconds(30))
                    return result
                }
            }
        }
    }

    // ----------------------------------------------------------------
    // Category methods dedicated to the blobstore service
    // ----------------------------------------------------------------

    /**
     * Creates an <code>InputStream</code> over the blob.
     * The stream is passed as parameter of the closure.
     * This methods takes care of properly opening and closing the stream.
     * You can use this method as follows:
     * <pre><code>
     * blobKey.withStream { inputstream -> ... }
     * </code></pre>
     *
     * @param selfKey a BlobKey
     * @param c the closure to execute, passing in the stream as parameter of the closure
     * @return the return value of the closure execution
     */
    static Object withStream(BlobKey selfKey, Closure c) {
        def stream = new BlobstoreInputStream(selfKey)
        stream.withStream(c)
    }

    /**
     * Creates a (buffered) <code>Reader</code> over the blob with a specified encoding.
     * The reader is passed as parameter of the closure.
     * This methods takes care of properly opening and closing the reader and underlying stream.
     * You can use this method as follows:
     * <pre><code>
     * blobKey.withReader("UTF-8") { reader -> ... }
     * </code></pre>
     *
     * @param selfKey a BlobKey
     * @param encoding the encoding used to read from the stream (UTF-8, etc.)
     * @param c the closure to execute, passing in the stream as parameter of the closure
     * @return the return value of the closure execution
     */
    static Object withReader(BlobKey selfKey, String encoding, Closure c) {
        def stream = new BlobstoreInputStream(selfKey)
        stream.withReader(encoding, c)
    }

    /**
     * Creates a (buffered) <code>Reader</code> over the blob using UTF-8 as default encoding.
     * The reader is passed as parameter of the closure.
     * This methods takes care of properly opening and closing the reader and underlying stream.
     * You can use this method as follows:
     * <pre><code>
     *  blobKey.withReader { reader -> ... }
     * </code></pre>
     *
     * @param selfKey a BlobKey
     * @param encoding the encoding used to read from the stream (UTF-8, etc.)
     * @param c the closure to execute, passing in the stream as parameter of the closure
     * @return the return value of the closure execution
     */
    static Object withReader(BlobKey selfKey, Closure c) {
        withReader(selfKey, "UTF-8", c)
    }

    /**
     * Get the <code>BlobInfo</code> associated with a blob key with:
     * <pre><code>
     *  blobKey.info
     * </code></pre>
     * @param selfKey the blob key to get information from
     * @return an instance of <code>BlobInfo</code>
     */
    static BlobInfo getInfo(BlobKey selfKey) {
        new BlobInfoFactory().loadBlobInfo(selfKey)
    }

    /**
     * @return the name of the file stored in the blob
     */
    static String getFilename(BlobKey selfKey) {
        getInfo(selfKey).filename
    }

    /**
     * @return the content-type of the blob
     */
    static String getContentType(BlobKey selfKey) {
        getInfo(selfKey).contentType
    }

    /**
     * @return the creation date of the file stored in the blob
     */
    static Date getCreation(BlobKey selfKey) {
        getInfo(selfKey).creation
    }

    /**
     * @return the size of the blob
     */
    static long getSize(BlobKey selfKey) {
        getInfo(selfKey).size
    }

    /**
     * Delete the blob associated with this blob key.
     *
     * @param selfKey the blob to delete, identified by its key
     */
    static void delete(BlobKey selfKey) {
        BlobstoreServiceFactory.blobstoreService.delete selfKey
    }

    /**
     * Serve a range of the blob to the response
     *
     * @param selfKey the blob to serve
     * @param the response on which to serve the blob
     * @param range the range of the blob (parameter can be ommitted)
     */
    static void serve(BlobKey selfKey, HttpServletResponse response, ByteRange range = null) {
        if (range)
            BlobstoreServiceFactory.blobstoreService.serve selfKey, range, response
        else
            BlobstoreServiceFactory.blobstoreService.serve selfKey, response
    }

    /**
     *
     * @param selfKey
     * @param response
     * @param range
     */
    static void serve(BlobKey selfKey, HttpServletResponse response, IntRange range) {
        BlobstoreServiceFactory.blobstoreService.serve selfKey, new ByteRange(range.fromInt, range.toInt), response
    }

    /**
     * Fetch a segment of a blob
     *
     * @param selfKey the blob key identifying the blob
     * @param start the beginning of the segment
     * @param end the end of the segment
     * @return an array of bytes
     */
    static byte[] fetchData(BlobKey selfKey, long start, long end) {
        BlobstoreServiceFactory.blobstoreService.fetchData selfKey, start, end
    }

    /**
     * Fetch a segment of a blob
     * <pre><code>
     * blobKey.fetchData 1000..2000
     * </code></pre>
     *
     * @param selfKey the blob key identifying the blob
     * @param a Groovy int range
     * @return an array of bytes
     */
    static byte[] fetchData(BlobKey selfKey, IntRange intRange) {
        fetchData(selfKey, intRange.fromInt, intRange.toInt)
    }

    /**
     * Fetch a segment of a blob
     *
     * @param selfKey the blob key identifying the blob
     * @param byteRange a <code>ByteRange</code> representing the segment
     * @return an array of bytes
     */
    static byte[] fetchData(BlobKey selfKey, ByteRange byteRange) {
        fetchData(selfKey, byteRange.start, byteRange.end)
    }

    /**
     * Fetch an image stored in the blobstore.
     * <pre><code>
     * def image = blobKey.image
     * // equivalent of ImagesServiceFactory.makeImageFromBlob(selfKey)
     * </code></pre>
     *
     * @param selfKey the key
     * @return an Image
     */
    static Image getImage(BlobKey selfKey) {
        ISF.makeImageFromBlob(selfKey)
    }

    // ----------------------------------------------------------------
    // Category methods dedicated to the NamespaceManager
    // ----------------------------------------------------------------

    /**
     * Use a namespace in the context of the excution of the closure.
     * This method will save the original namespace and restore it afterwards.
     * 
     * <pre><code>
     * namespace.of('test') { ... }
     * </code></pre>
     *
     * @param nm NamespaceManager class
     * @param ns the name of the namespace to use
     * @param c the code to execute under that namespace
     */
    static void of(Class nm, String ns, Closure c) {
        if (nm != NamespaceManager)
            throw new MissingMethodException("with", nm, [ns, c] as Object[])
        
        def oldNs = NamespaceManager.get()
        NamespaceManager.set(ns)
        try {
            c()
        } finally {
            NamespaceManager.set(oldNs)
        }
    }

    // ----------------------------------------------------------------
    // Category methods dedicated to the ImageService
    // ----------------------------------------------------------------

    /**
     * Use the leftShift operator, <<, to concatenate a transform to the composite transform.
     * <pre><code>
     * def cropTransform = ...
     * def rotateTransform = ...
     *
     * croptTransform << rotateTransform
     * </code></pre>
     * @param leftTransform a transform
     * @param rightTransform another transform
     * @return a composite transform
     */
    static CompositeTransform leftShift(CompositeTransform leftTransform, Transform rightTransform) {
        leftTransform.concatenate(rightTransform)
    }

    /**
     * Use the rightShift operator, >>, to "pre-concatenate" a transform to the composite transform.
     * <pre><code>
     * def cropTransform = ...
     * def rotateTransform = ...
     *
     * croptTransform >> rotateTransform
     * </code></pre>
     * @param leftTransform a transform
     * @param rightTransform another transform
     * @return a composite transform
     */

    static CompositeTransform rightShift(CompositeTransform leftTransform, Transform rightTransform) {
        leftTransform.preConcatenate(rightTransform)
    }

    /**
     * Transform a byte array into an Image.
     *
     * <pre><code>
     * def byteArray = ...
     * def image = byteArray.image
     * </code></pre>
     *
     * @param byteArray a byte array
     * @return an Image
     */
    static Image getImage(byte[] byteArray) {
        ISF.makeImage(byteArray)
    }


     /**
      * Image transform DSL.
      * <pre><code>
      *  bytes.image.transform {
      *      resize 100, 100
      *      crop 0.1, 0.1, 0.9, 0.9
      *      flip horizontal
      *      flip vertical
      *      rotate 90
      *      feeling lucky
      *  }
      * </code></pre>
      *
      * @param selfImage the image to transform
      * @param c the closure containg the various transform steps
      * @return a transformed image
      */
    static Image transform(Image selfImage, Closure c) {
        Closure clone = c.clone()
        clone.resolveStrategy = Closure.DELEGATE_ONLY

        // create an empty composite transform
        CompositeTransform compTransf = ISF.makeCompositeTransform()

        clone.delegate = new Expando([
                // methods
                resize:     { width, height ->                compTransf << ISF.makeResize(width, height) },
                crop:       { leftX, topY, rightX, bottomY -> compTransf << ISF.makeCrop(leftX, topY, rightX, bottomY) },
                horizontal: { flip ->                         compTransf << ISF.makeHorizontalFlip() },
                vertical:   { flip ->                         compTransf << ISF.makeVerticalFlip() },
                rotate:     { degrees ->                      compTransf << ISF.makeRotate(degrees) },
                feeling:    { luck ->                         compTransf << ISF.makeImFeelingLucky() },

                // variables
                lucky:          true,
                flip:           true
        ])

        // calculate a combined transform
        clone()

        // apply the composite transform and generate the resulting image
        return ISF.imagesService.applyTransform(compTransf, selfImage)
    }

    /**
     * Create a new resized image.
     *
     * <pre><code>
     *  def thumbnail = image.resize(100, 100)
     * </code></pre>
     *
     * @param selfImage image to resize
     * @param width new width
     * @param height new height
     * @return a resized image
     */
    static Image resize(Image selfImage, int width, int height) {
        ISF.imagesService.applyTransform(ISF.makeResize(width, height), selfImage)
    }

    /**
     * Create a new cropped image.
     *
     * <pre><code>
     *  def cropped = image.crop(0.1, 0.1, 0.9, 0.9)
     * </code></pre>
     *
     * @param selfImage image to crop
     * @param leftX
     * @param topY
     * @param rightX
     * @param bottomY
     * @return a cropped image
     */
    static Image crop(Image selfImage, double leftX, double topY, double rightX, double bottomY) {
        ISF.imagesService.applyTransform(ISF.makeCrop(leftX, topY, rightX, bottomY), selfImage)
    }

    /**
     * Create a new image flipped horizontally.
     *
     * <pre><code>
     *  def himage = image.horizontalFlip()
     * </code></pre>
     *
     * @param selfImage image to flip horizontally
     * @return a flipped image
     */
    static Image horizontalFlip(Image selfImage) {
        ISF.imagesService.applyTransform(ISF.makeHorizontalFlip(), selfImage)
    }

    /**
     * Create a new image flipped vertically.
     *
     * <pre><code>
     *  def vimage = image.verticalFlip()
     * </code></pre>
     *
     * @param selfImage image to flip vertically
     * @return a flipped image
     */
    static Image verticalFlip(Image selfImage) {
        ISF.imagesService.applyTransform(ISF.makeVerticalFlip(), selfImage)
    }

    /**
     * Create a new rotated image.
     *
     * <pre><code>
     *  def rotated = image.rotate(90)
     * </code></pre>
     *
     * @param selfImage image to rotate
     * @param degrees number of degrees to rotate (must be a multiple of 90)
     * @return a rotated image
     */
    static Image rotate(Image selfImage, int degrees) {
        ISF.imagesService.applyTransform(ISF.makeRotate(degrees), selfImage)
    }

    /**
     * Create a new image applying the "I'm feeling lucky" transformation.
     *
     * <pre><code>
     *  def adjusted = image.iAmFeelingLucky()
     * </code></pre>
     *
     * @param selfImage image to adjust
     * @return an adjusted image
     */
    static Image imFeelingLucky(Image selfImage) {
        ISF.imagesService.applyTransform(ISF.makeImFeelingLucky(), selfImage)
    }

    /**
     * Create an image from a file.
     *
     * @param f PNG or JPEG file
     * @return an instance of <code>Image</code>
     */
    static Image getImage(File f) {
        ISF.makeImage(f.bytes)
    }

    // ----------------------------------------------------------------
    // Category methods dedicated to the Capabilities service
    // ----------------------------------------------------------------

    /**
     * Query the status of the various App Engine services.
     *
     * <pre><code>
     * import static com.google.appengine.api.capabilities.Capability.*
     * import static com.google.appengine.api.capabilities.CapabilityStatus.*
     *
     * capabilities[DATASTORE] == ENABLED
     * </code></pre>
     *
     * @param capa the capability to know the status of
     * @return a status
     */
    static CapabilityStatus getAt(CapabilitiesService capabilities, Capability capa) {
        return capabilities.getStatus(capa).getStatus()
    }

    /**
     * Coerces a capability status into a boolean.
     * This mechanism is used by the "Groovy Truth".
     *
     * @return true if the capability status is ENABLED, otherwise false.
     */
    static boolean asBoolean(CapabilityStatus capabilityStatus) {
        capabilityStatus == CapabilityStatus.ENABLED || capabilityStatus == CapabilityStatus.SCHEDULED_MAINTENANCE
    }

    // ----------------------------------------------------------------
    // Category methods dedicated to the URL Fetch service
    // ----------------------------------------------------------------

    /**
     * @return the HTTP status code (synonym of <code>getResponseCode()</code>)
     */
    static int getStatusCode(HTTPResponse response) {
        response.responseCode
    }

    /**
     * @return a convenient Map<String, String> of HTTP Headers from the response
     */
    static Map<String, String> getHeadersMap(HTTPResponse response) {
        response.headers.inject([:]) { Map m, HTTPHeader h -> m[h.name] = h.value; m }
    }
    
    /**
     * Gets the text of the response.
     *
     * @param response the response
     * @param encoding encoding used (default: 'UTF-8')
     * @return the string representing the response content
     */
    static String getText(HTTPResponse response, String encoding = 'UTF-8') {
        new String(response.content, encoding)
    }

    private static fetch(URL url, HTTPMethod method, Map<String, String> options) {
        URLFetchService urlFetch = URLFetchServiceFactory.URLFetchService
        def fetchOptions = FetchOptions.Builder.withDefaults()

        // specify the fetch options
        options.each { String key, value ->
            switch(key) {
                case 'allowTruncate':
                    if (value)
                        fetchOptions.allowTruncate()
                    else
                        fetchOptions.disallowTruncate()
                    break
                case 'followRedirects':
                    if (value)
                        fetchOptions.followRedirects()
                    else
                        fetchOptions.doNotFollowRedirects()
                    break
                case 'deadline':
                    fetchOptions.deadline = value
                    break
                // bypass the headers, payload, params and async options
                case 'headers':
                case 'payload':
                case 'params':
                case 'async':
                    break
                default:
                    throw new RuntimeException("Unknown fetch option: $key")
            }
        }

        // add params
        if (options.params) {
            def encodedParams = options.params.collect { k, v -> "${URLEncoder.encode(k)}=${URLEncoder.encode(v)}" }.join('&')
            // if it's a POST method, encode the params as an URL encoded payload
            if (method == HTTPMethod.POST) {
                if (!options.headers) { options.headers = [:] }
                options.headers << ['Content-Type': 'application/x-www-form-urlencoded']
                options.payload = encodedParams
            } else {
                url = new URL("${url.toString()}?${encodedParams}")
            }
        }

        def request = new HTTPRequest(url, method, fetchOptions)

        // add the headers to the request
        if (options.headers) {
            Map headers = options.headers
            headers.each { String key, String value ->
                request.addHeader(new HTTPHeader(key, value))
            }
        }

        // add the payload
        if (options.payload) {
            request.payload = options.payload
        }

        // do an async call, if the async: true option is present
        if (options.async)
            urlFetch.fetchAsync(request)
        else
            urlFetch.fetch(request)
    }

    /**
     * Use the URLFetch Service to do a GET on the URL.
     *
     * @param url URL to GET
     * @param options a map that can contain parameters such as:
     *  allowTruncate (boolean), followRedirects (boolean), deadline (double), headers (map of String key/value pairs),
     *  payload (byte[]), params (map of String key/value pairs), async (boolean)
     * @return an HTTPResponse or a Future<HTTPResponse> if async options is set to true
     */
    static get(URL url, Map<String, String> options = [:]) {
        fetch(url, HTTPMethod.GET, options)
    }

    /**
     * Use the URLFetch Service to do a POST on the URL.
     *
     * @param url URL to POST to
     * @param options a map that can contain parameters such as:
     *  allowTruncate (boolean), followRedirects (boolean), deadline (double), headers (map of String key/value pairs),
     *  payload (byte[]), params (map of String key/value pairs), async (boolean)
     * @return an HTTPResponse or a Future<HTTPResponse> if async options is set to true
     */
    static post(URL url, Map<String, String> options = [:]) {
        fetch(url, HTTPMethod.POST, options)
    }

    /**
     * Use the URLFetch Service to do a PUT on the URL.
     *
     * @param url URL to PUT to
     * @param options a map that can contain parameters such as:
     *  allowTruncate (boolean), followRedirects (boolean), deadline (double), headers (map of String key/value pairs),
     *  payload (byte[]), params (map of String key/value pairs), async (boolean)
     * @return an HTTPResponse or a Future<HTTPResponse> if async options is set to true
     */
    static put(URL url, Map<String, String> options = [:]) {
        fetch(url, HTTPMethod.PUT, options)
    }

    /**
     * Use the URLFetch Service to do a DELETE on the URL.
     *
     * @param url URL to DELETE
     * @param options a map that can contain parameters such as:
     *  allowTruncate (boolean), followRedirects (boolean), deadline (double), headers (map of String key/value pairs),
     *  payload (byte[]), params (map of String key/value pairs), async (boolean)
     * @return an HTTPResponse or a Future<HTTPResponse> if async options is set to true
     */
    static delete(URL url, Map<String, String> options = [:]) {
        fetch(url, HTTPMethod.DELETE, options)
    }

    /**
     * Use the URLFetch Service to do a HEAD on the URL.
     *
     * @param url URL to HEAD
     * @param options a map that can contain parameters such as:
     *  allowTruncate (boolean), followRedirects (boolean), deadline (double), headers (map of String key/value pairs),
     *  payload (byte[]), params (map of String key/value pairs), async (boolean)
     * @return an HTTPResponse or a Future<HTTPResponse> if async options is set to true
     */
    static head(URL url, Map<String, String> options = [:]) {
        fetch(url, HTTPMethod.HEAD, options)
    }
}
