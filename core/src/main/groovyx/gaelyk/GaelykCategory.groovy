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

/**
 * Category methods decorating the Google App Engine SDK classes
 * adding new shortcut methods to simplify the usage of the SDK
 * from within Groovy servlets and templates.
 *
 * @author Guillaume Laforge
 */
class GaelykCategory {

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
     * <code>entity.propertyName = value</code>
     * Or
     * <code>entity['propertyName'] = value</code>
     */
    static Object setAt(Entity entity, String name, Object value) {
        entity.setProperty(name, value)
    }

    /**
     * Provides a shortcut notation to set a property of an entity.
     * Instead of writing
     * <code>entity.setProperty('propertyName', value)</code>
     * You can use the shortcut
     * <code>entity.propertyName = value</code>
     * Or
     * <code>entity['propertyName'] = value</code>
     */
    static Object set(Entity entity, String name, Object value) {
        entity.setProperty(name, value)
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
    static Object delete(Entity entity) {
        DatastoreServiceFactory.datastoreService.delete(entity.key)
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

    // Additional converter methods for types that are storable as Entity properties

    /**
     * Converter method for converting strings into various GAE specific types
     * <pre><code>
     *  "foo@bar.com" as Email
     *  "http://www.google.com" as Link
     *  "+3361234543" as PhoneNumber
     *  "50 avenue de la Madeleine, Paris" as PostalAddress
     *  "groovy" as Category
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
        else if (clazz == Category)
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

    // ----------------------------------------------------------------
    // Category methods dedicated to the URL fetcher service
    // ----------------------------------------------------------------
    
    
}
