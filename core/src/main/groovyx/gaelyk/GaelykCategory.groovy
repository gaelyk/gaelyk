/*
 * Copyright 2009-2011 the original author or authors.
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
import com.google.appengine.api.taskqueue.Queue
import com.google.appengine.api.taskqueue.TaskHandle
import com.google.appengine.api.taskqueue.TaskOptions
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
import com.google.appengine.api.urlfetch.FetchOptions as UrlFetchOptions
import com.google.appengine.api.urlfetch.HTTPHeader
import com.google.appengine.api.memcache.MemcacheService.SetPolicy
import com.google.appengine.api.channel.ChannelService
import com.google.appengine.api.channel.ChannelMessage
import com.google.appengine.api.taskqueue.RetryOptions
import javax.mail.internet.MimeMessage
import javax.servlet.http.HttpServletRequest
import javax.mail.Session
import com.google.appengine.api.datastore.AsyncDatastoreService
import java.util.concurrent.Future
import com.google.appengine.api.xmpp.Subscription
import com.google.appengine.api.xmpp.PresenceBuilder
import com.google.appengine.api.xmpp.PresenceType
import com.google.appengine.api.xmpp.SubscriptionBuilder
import com.google.appengine.api.xmpp.SubscriptionType
import com.google.appengine.api.xmpp.PresenceShow
import com.google.appengine.api.files.AppEngineFile
import com.google.appengine.api.files.FileService
import com.google.appengine.api.files.FileServiceFactory
import java.nio.channels.Channels
import com.google.appengine.api.taskqueue.DeferredTask
import com.google.appengine.api.datastore.Query
import com.google.appengine.api.datastore.PreparedQuery
import com.google.appengine.api.datastore.KeyFactory
import com.google.appengine.api.datastore.FetchOptions
import com.google.appengine.api.LifecycleManager
import com.google.appengine.api.LifecycleManager.ShutdownHook
import groovyx.gaelyk.query.QueryBuilder

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
    // General utility category methods
    // ----------------------------------------------------------------

    /**
     * Transforms a map of key / value pairs into a properly URL encoded query string.
     *
     * <pre><code>
     *  assert "title=
     * </code></pre>
     *
     * @return a query string
     */
    static String toQueryString(Map self) {
        self.collect { k, v -> "${URLEncoder.encode(k.toString())}=${URLEncoder.encode(v.toString())}" }.join('&')
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
     * @throws groovy.lang.MissingPropertyException when the key doesn't correspond
     * to a property of the <code>MailService.Message</code> class.
     */
    static void sendToAdmins(MailService mailService, Map m) {
        Message msg = createMessageFromMap(m)
        mailService.sendToAdmins msg 
    }

    /**
     * Parses an incoming email message coming from the request into a <code>MimeMessage</code>
     *
     * @param request incoming request
     * @return an instance of <code>MimeMessage</code>
     */
    static MimeMessage parseMessage(MailService mailService, HttpServletRequest request) {
        def session = Session.getDefaultInstance(new Properties(), null)
        return new MimeMessage(session, request.inputStream)
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
        transformValueForRetrieval(entity.getProperty(name))
    }

    /**
     * Provides a shortcut notation to get a property of an entity.
     * Instead of writing
     * <code>entity.getProperty('propertyName')</code>
     * You can use the shortcut
     * <code>entity.propertyName</code>
     */
    static Object get(Entity entity, String name) {
        transformValueForRetrieval(entity.getProperty(name))
    }

    // All transformations that need to be done on entity fields
    // before being accessed by the user
    private static Object transformValueForRetrieval(Object value) {
        value instanceof Text ? value.value : value
    }

    /**
     * Provides a shortcut notation to set a property of an entity.
     * Instead of writing
     * <code>entity.setProperty('propertyName', value)</code>
     * You can use the shortcut
     * <code>entity['propertyName'] = value</code>
     */
    static void setAt(Entity entity, String name, Object value) {
        entity.setProperty(name, transformValueForStorage(value))
    }

    /**
     * Provides a shortcut notation to set a property of an entity.
     * Instead of writing
     * <code>entity.setProperty('propertyName', value)</code>
     * You can use the shortcut
     * <code>entity.propertyName = value</code>
     */
    static void set(Entity entity, String name, Object value) {
        entity.setProperty(name, transformValueForStorage(value))
    }

    // All transformations that need to be done on entity fields
    // prior to their insertion in the datastore
    private static Object transformValueForStorage(Object value) {
        // the datastore doesn't allow to store GStringImpl
        // so we need a toString() first
        def newValue = value instanceof GString ? value.toString() : value
        // if we store a string longer than 500 characters
        // it needs to be wrapped in a Text instance
        if (newValue instanceof String && newValue.size() > 500) {
            newValue = new Text(newValue)
        }
        return newValue
    }

    /**
     * Save this entity in the data store.
     * Usage: <code>entity.save()</code>
     */
    static Key save(Entity entity) {
        DatastoreServiceFactory.datastoreService.put(entity)
    }

    /**
     * Save this entity in the data store asynchronously.
     * Usage: <code>entity.asyncSave()</code>
     */
    static Future<Key> asyncSave(Entity entity) {
        DatastoreServiceFactory.asyncDatastoreService.put(entity)
    }

    /**
     * Delete this entity from the data store.
     * Usage: <code>entity.delete()</code>
     */
    static void delete(Entity entity) {
        DatastoreServiceFactory.datastoreService.delete(entity.key)
    }

    /**
     * Delete this entity from the data store.
     * Usage: <code>entity.asyncDelete()</code>
     */
    static Future<Void> asyncDelete(Entity entity) {
        DatastoreServiceFactory.asyncDatastoreService.delete(entity.key)
    }

    /**
     * Delete the entity represented by that key, from the data store.
     * Usage: <code>key.delete()</code> 
     */
    static void delete(Key key) {
        DatastoreServiceFactory.datastoreService.delete(key)
    }

    /**
     * Fetch the entity associated with that key from the datastore.
     * Usage: <code>def entity = key.get()</code>
     *
     * @returns an entity
     */
    static Entity get(Key key) {
        DatastoreServiceFactory.datastoreService.get(key)
    }

    /**
     * Fetch the entities associated with the collection of keys from the datastore.
     * Usage: <code>def entities = [key1, key2].get()</code>
     *
     * @returns a map of key and entity
     */
    static Map<Key, Entity> get(Iterable<Key> keys) {
        DatastoreServiceFactory.datastoreService.get(keys)
    }

    /**
     * Delete the entity represented by that key, from the data store.
     * Usage: <code>key.delete()</code>
     */
    static Future<Void> asyncDelete(Key key) {
        DatastoreServiceFactory.asyncDatastoreService.delete(key)
    }

    /**
     * With this method, transaction handling is done transparently.
     * The transaction is committed if the closure executed properly.
     * The transaction is rollbacked if anything went wrong.
     * You can use this method as follows:
     * <code>
     * datastore.withTransaction { transaction ->
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
            if (transaction.isActive()) {
                transaction.rollback()
            }
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

    /**
     * @return the asynchronous datastore service.
     */
    static AsyncDatastoreService getAsync(DatastoreService service) {
        DatastoreServiceFactory.asyncDatastoreService
    }

    /**
     * Create a query to be later executed on the datastore data.
     *
     * @param c the closure representing the query
     * @return the query
     */
    static Query query(DatastoreService service, Closure c) {
        Closure cQuery = c.clone()
        cQuery.resolveStrategy = Closure.DELEGATE_FIRST
        def builder = new QueryBuilder(c.thisObject instanceof Script ? c.thisObject.binding : null)
        cQuery.delegate = builder
        cQuery()
        return builder.createQuery()
    }

    /**
     * Create and executes a prepared query to retrieve entities from the datastore.
     *
     * @param c the closure representing the query to execute
     * @return the results
     */
    static execute(DatastoreService service, Closure c) {
        Closure cQuery = c.clone()
        cQuery.resolveStrategy = Closure.DELEGATE_FIRST
        def builder = new QueryBuilder(c.thisObject instanceof Script ? c.thisObject.binding : null)
        cQuery.delegate = builder
        cQuery()
        return builder.execute()
    }

    // ------------------------------------
    // Querying datastore metadata
    // (contribution from Benjamin Muschko)
    // ------------------------------------

    static final DEFAULT_FETCH_OPTIONS = FetchOptions.Builder.withDefaults()

    /**
     * Gets datastore namespaces
     *
     * @param service Datastore service
     * @param options Fetch options
     * @return Entities
     */
    static List<Entity> getNamespaces(DatastoreService service, FetchOptions options = DEFAULT_FETCH_OPTIONS) {
        queryMetaData(service, options, Query.NAMESPACE_METADATA_KIND)
    }

    /**
     * Gets datastore namespaces. The closure lets you apply additional filters to your query.
     *
     * @param service Datastore service
     * @param options Fetch options
     * @param closure Closure
     * @return Entities
     */
    static List<Entity> getNamespaces(DatastoreService service, FetchOptions options = DEFAULT_FETCH_OPTIONS, Closure closure) {
        queryMetaData(service, options, Query.NAMESPACE_METADATA_KIND, closure)
    }

    /**
     * Gets datastore kinds.
     *
     * @param service Datastore service
     * @param options Fetch options
     * @return Entities
     */
    static List<Entity> getKinds(DatastoreService service, FetchOptions options = DEFAULT_FETCH_OPTIONS) {
        queryMetaData(service, options, Query.KIND_METADATA_KIND)
    }

    /**
     * Gets datastore kinds. The closure lets you apply additional filters to your query.
     *
     * @param service Datastore service
     * @param options Fetch options
     * @param closure Closure
     * @return Entities
     */
    static List<Entity> getKinds(DatastoreService service, FetchOptions options = DEFAULT_FETCH_OPTIONS, Closure closure) {
        queryMetaData(service, options, Query.KIND_METADATA_KIND, closure)
    }

    /**
     * Gets all datastore kinds and their properties.
     *
     * @param service Datastore service
     * @param options Fetch options
     * @return Entities
     */
    static List<Entity> getProperties(DatastoreService service, FetchOptions options = DEFAULT_FETCH_OPTIONS) {
        queryMetaData(service, options, Query.PROPERTY_METADATA_KIND)
    }

    /**
     * Gets all datastore kinds and their properties. The closure lets you apply additional filters to your query.
     *
     * @param service Datastore service
     * @param kind Kind
     * @param options Fetch options
     * @param closure Closure
     * @return Entities
     */
    static List<Entity> getProperties(DatastoreService service, FetchOptions options = DEFAULT_FETCH_OPTIONS, Closure closure) {
        queryMetaData(service, options, Query.PROPERTY_METADATA_KIND, closure)
    }

    /**
     * Gets datastore kind properties.
     *
     * @param service Datastore service
     * @param kind Kind
     * @param options Fetch options
     * @return Entities
     */
    static List<Entity> getProperties(DatastoreService service, String kind, FetchOptions options = DEFAULT_FETCH_OPTIONS) {
        Query query = new Query(Query.PROPERTY_METADATA_KIND)
        query.setAncestor(createKindKey(kind))
        PreparedQuery preparedQuery = service.prepare(query)
        preparedQuery.asList(options)
    }

    /**
     * Gets datastore kind properties. The closure lets you apply additional filters to your query.
     *
     * @param service Datastore service
     * @param kind Kind
     * @param options Fetch options
     * @param closure Closure
     * @return Entities
     */
    static List<Entity> getProperties(DatastoreService service, String kind, FetchOptions options = DEFAULT_FETCH_OPTIONS, Closure closure) {
        Query query = new Query(Query.PROPERTY_METADATA_KIND)
        query.setAncestor(createKindKey(kind))
        closure(query)
        PreparedQuery preparedQuery = service.prepare(query)
        preparedQuery.asList(options)
    }

    /**
     * Gets datastore kind property.
     *
     * @param service Datastore service
     * @param kind Kind
     * @param property Property
     * @return Entity
     */
    static Entity getProperty(DatastoreService service, String kind, String property) {
        Query query = new Query(Query.PROPERTY_METADATA_KIND)
        query.setAncestor(createPropertyKey(kind, property))
        PreparedQuery preparedQuery = service.prepare(query)
        preparedQuery.asSingleEntity()
    }

    /**
     * Queries for meta data.
     *
     * @param service Datastore service
     * @param options Fetch options
     * @param metaDataQuery Query
     * @return Entities
     */
    private static List<Entity> queryMetaData(DatastoreService service, FetchOptions options, String metaDataQuery) {
        Query query = new Query(metaDataQuery)
        PreparedQuery preparedQuery = service.prepare(query)
        preparedQuery.asList(options)
    }

    /**
     * Queries for meta data.
     *
     * @param service Datastore service
     * @param options Fetch options
     * @param metaDataQuery Query
     * @param closure Closure
     * @return Entities
     */
    private static List<Entity> queryMetaData(DatastoreService service, FetchOptions options, String metaDataQuery, Closure closure) {
        Query query = new Query(metaDataQuery)
        closure(query)
        PreparedQuery preparedQuery = service.prepare(query)
        preparedQuery.asList(options)
    }

    /**
     * Creates kind key
     *
     * @param kind Kind
     * @return Key
     */
    private static Key createKindKey(String kind) {
        KeyFactory.createKey(Query.KIND_METADATA_KIND, kind)
    }

    /**
     * Creates property key.
     *
     * @param kind Kind
     * @param property Property
     * @return Key
     */
    private static Key createPropertyKey(String kind, String property) {
        KeyFactory.createKey(createKindKey(kind), Query.PROPERTY_METADATA_KIND, property)
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
            new Rating(Integer.valueOf(self))
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
        TaskOptions options = TaskOptions.Builder.withDefaults()
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
            } else if (key == 'retryOptions') {
                if (value instanceof Map) {
                    def retryOptions = RetryOptions.Builder.withDefaults()
                    value.each { retryKey, retryValue ->
                        if (retryKey in ['taskRetryLimit', 'taskAgeLimitSeconds',
                                'minBackoffSeconds', 'maxBackoffSeconds', 'maxDoublings']) {
                            retryOptions."${retryKey}"(retryValue)
                        } else {
                            throw new RuntimeException("'$retryKey' is not a valid retry option parameter.")
                        }
                    }
                    options.retryOptions(retryOptions)
                } else if (value instanceof RetryOptions) {
                    options.retryOptions(value)
                } else {
                    throw new RuntimeException("The retry options parameter should either be a map or an instance of RetryOptions.")
                }
            } else if (key == 'method') {
                if (value instanceof TaskOptions.Method) {
                    options = options.method(value)
                } else if(value in ['GET', 'POST', 'PUT', 'DELETE', 'HEAD', 'PULL']) {
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
                } else if (value instanceof Closure) {
                    options = options.payload(value as DeferredTask)
                } else if (value instanceof DeferredTask) {
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

    /**
     * Override the GAE SDK XMPPService#parsePresence as it hard-codes the path for the presence handler,
     * thus preventing from using Gaelyk's routing to point at our own handler.
     *
     * @param xmppService the XMPP service
     * @param request the servlet request
     * @return a Presence
     */
    static Presence parsePresence(XMPPService xmppService, HttpServletRequest request) {
        // value of the presence, added by the routing logic as request parameter
        String value = request.getParameter('value')

        Map formData = parseXmppFormData(request)

        new PresenceBuilder()
            .withFromJid(new JID(formData.from))
            .withToJid(new JID(formData.to))
            .withPresenceType(PresenceType."${value.toUpperCase()}")
            .withPresenceShow(value == 'available' ? PresenceShow.NONE : null)
            .build()
    }

    /**
     * Override the GAE SDK XMPPService#parseSubscription as it hard-codes the path for the subscription handler,
     * thus preventing from using Gaelyk's routing to point at our own handler.
     *
     * @param xmppService the XMPP service
     * @param request the servlet request
     * @return a Subscription
     */
    static Subscription parseSubscription(XMPPService xmppService, HttpServletRequest request) {
        // value of the subscription, added by the routing logic as request parameter
        String value = request.getParameter('value')

        Map formData = parseXmppFormData(request)

        new SubscriptionBuilder()
            .withFromJid(new JID(formData.from))
            .withToJid(new JID(formData.to))
            .withSubscriptionType(SubscriptionType."${value.toUpperCase()}")
            .build()
    }

    /**
     * Parse the form-data from the Jabber requests,
     * as it contains useful information like presence and subscription details, etc.
     *
     * @param text the body of the request
     * @return a map containing form-data key value pairs
     */
    static Map parseXmppFormData(HttpServletRequest request) {
        /*
            App Engine encodes the presence, subscription into the body of the post request, in form-data.
            An example form-data follows:

            --ItS1i0T-5328197
            Content-Disposition: form-data; name="to"

            you@you.com
            --ItS1i0T-5328197
            Content-Disposition: form-data; name="from"

            me@me.com
            --ItS1i0T-5328197
            Content-Disposition: form-data; name="available"

            true
            --ItS1i0T-5328197
            Content-Disposition: form-data; name="stanza"
            Content-Type: text/xml

            <presence from="me@me.com" to="you@you.com"><show/><status/></presence>
            --ItS1i0T-5328197--
         */

        def body = request.reader.text

        // split the request body lines
        def lines = body.readLines()

        // split the form-data lines around the boundaries
        // remove a first surrounding empty lines and closing boundary
        // trim the last \n characters
        def parts = body.split(lines[0].trim())[1..-2]*.trim()

        // reads the part keys and values into a Map
        return parts*.readLines().collectEntries {
            [
                    // extract the name from the form-data part
                    (it[it.findIndexOf{ l -> l.startsWith("Content-Disposition: form-data")}] =~ /.*name="(.*)".*/)[0][1],
                    // the last line contains the data associated with the key
                    it[-1]
            ]
        }
    }

    // ----------------------------------------------------------------
    // Category methods dedicated to the memcache service
    // ----------------------------------------------------------------

    /**
     * Get an object from the cache, with a GString key, coerced to a String.
     *
     * @param key the GString key
     * @return the value stored under that key
     */
    static Object get(MemcacheService memcache, String key) {
        memcache.get((Object)key)
    }

    /**
     * Get an object from the cache, with a GString key, coerced to a String.
     *
     * @param key the GString key
     * @return the value stored under that key
     */
    static Object get(MemcacheService memcache, GString key) {
        memcache.get(key.toString())
    }

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
     * Put an object in the cache under a GString key, coerced to a String.
     *
     * @param key a GString key
     * @param value the value to put in the cache
     */
    static void set(MemcacheService memcache, String key, Object value) {
        memcache.put(key, value)
    }

    /**
     * Put an object in the cache under a GString key, coerced to a String.
     *
     * @param key a GString key
     * @param value the value to put in the cache
     */
    static void put(MemcacheService memcache, GString key, Object value) {
        memcache.put(key.toString(), value)
    }

    /**
     * Put an object in the cache under a GString key, coerced to a String, with an expiration.
     *
     * @param key a GString key
     * @param value the value to put in the cache
     * @param expiration expiration of the key/value
     */
    static void put(MemcacheService memcache, GString key, Object value, Expiration expiration) {
        memcache.put(key.toString(), value, expiration)
    }

    /**
     * Put an object in the cache under a GString key, coerced to a String, with an expiration and a SetPolicy.
     *
     * @param key a GString key
     * @param value the value to put in the cache
     * @param expiration expiration of the key/value
     * @param policy a SetPolicy 
     */
    static void put(MemcacheService memcache, GString key, Object value, Expiration expiration, SetPolicy policy) {
        memcache.put(key.toString(), value, expiration, policy)
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
    // Category methods dedicated to the FileService
    // ----------------------------------------------------------------

    /**
     * Method creating a writer for the AppEngineFile, writing textual content to it, and closes it when done.
     *
     * <pre><code>
     *  def file = files.createNewBlobFile("text/plain", "hello.txt")
     *
     *  // with default options
     *  file.withWriter { writer ->
     *      writer << "some content"
     *  }
     *
     *  // with specific options:
     *  file.withWriter(encoding: "US-ASCII", locked: true, finalize: false) { writer ->
     *      writer << "some content
     *  }
     * </code></pre>
     *
     * @param file the AppEngineFile to write to
     * @param options an optional map containing three possible keys:
     *      encoding (a String, the encoding to be used for the writer -- UTF8 by default),
     *      locked (a boolean, if you want to acquire a write lock on the file -- false by default),
     *      finalize (a boolean, if you want to close the file definitively -- false by default).
     * @param closure the closure with the writer as parameter
     * @return the original file, for chaining purpose
     */
    static AppEngineFile withWriter(AppEngineFile file , Map options = [:], Closure closure) {
        boolean locked = options.containsKey("locked") ? options.locked : true
        boolean closeFinally = options.containsKey("finalize") ? options.finalize : true

        def writeChannel = FileServiceFactory.fileService.openWriteChannel(file, locked)
        def writer = new PrintWriter(Channels.newWriter(writeChannel, options.encoding ?: "UTF-8"))

        writer.withWriter closure

        if (closeFinally) {
            writeChannel.closeFinally()
        } else {
            writeChannel.close()
        }

        return file
    }

    /**
     * Method creating an output stream for the AppEngineFile, writing bynary content to it, and closes it when done.
     *
     * <pre><code>
     *  def file = files.createNewBlobFile("text/plain", "hello.txt")
     *
     *  // with default options
     *  file.withOutputStream { stream ->
     *      stream << "some content".bytes
     *  }
     *
     *  // with specific options:
     *  file.withOutputStream(locked: true, finalize: false) { writer ->
     *      stream << "some content".bytes
     *  }
     * </code></pre>
     *
     * @param file the AppEngineFile to write to
     * @param options an optional map containing two possible keys:
     *      locked (a boolean, if you want to acquire a write lock on the file -- false by default),
     *      finalize (a boolean, if you want to close the file definitively -- false by default).
     * @param closure the closure with the output stream as parameter
     * @return the original file, for chaining purpose
     */
    static AppEngineFile withOutputStream(AppEngineFile file, Map options = [:], Closure closure) {
        boolean locked = options.containsKey("locked") ? options.locked : true
        boolean closeFinally = options.containsKey("finalize") ? options.finalize : true

        def writeChannel = FileServiceFactory.fileService.openWriteChannel(file, locked)
        def stream = Channels.newOutputStream(writeChannel)

        stream.withStream closure

        if (closeFinally) {
            writeChannel.closeFinally()
        } else {
            writeChannel.close()
        }

        return file
    }

    /**
     * Method creating a reader for the AppEngineFile, read textual content from it, and closes it when done.
     *
     * <pre><code>
     *  def file = files.fromPath(someStringPath)
     *
     *  // with default options
     *  file.withReader { reader ->
     *      log.info reader.text
     *  }
     *
     *  // with specific options:
     *  file.withReader(encoding: "US-ASCII", locked: true) { reader ->
     *      log.info reader.text
     *  }
     * </code></pre>
     *
     * @param file the AppEngineFile to read from
     * @param options an optional map containing two possible keys:
     *      encoding (a String, the encoding to be used for the reader -- UTF8 by default),
     *      locked (a boolean, if you want to acquire a lock on the file -- false by default),
     * @param closure the closure with the reader as parameter
     * @return the original file, for chaining purpose
     */
    static AppEngineFile withReader(AppEngineFile file, Map options = [:], Closure closure) {
        boolean locked = options.containsKey("locked") ? options.locked : true

        def readChannel = FileServiceFactory.fileService.openReadChannel(file, locked)
        def reader = new BufferedReader(Channels.newReader(readChannel, options.encoding ?: "UTF-8"))

        reader.withReader closure
        readChannel.close()

        return file
    }

    /**
     * Method creating a buffered input stream for the AppEngineFile, read binary content from it, and closes it when done.
     *
     * <pre><code>
     *  def file = files.fromPath(someStringPath)
     *
     *  // with default options
     *  file.withInputStream { stream ->
     *      // read from the buffered input stream
     *  }
     *
     *  // with specific options:
     *  file.withInputStream(locked: true) { stream ->
     *      // read from the buffered input stream
     *  }
     * </code></pre>
     *
     * @param file the AppEngineFile to read from
     * @param options an optional map containing one possible key:
     *      locked (a boolean, if you want to acquire a lock on the file -- false by default),
     * @param closure the closure with the input stream as parameter
     * @return the original file, for chaining purpose
     */
    static AppEngineFile withInputStream(AppEngineFile file, Map options = [:], Closure closure) {
        boolean locked = options.containsKey("locked") ? options.locked : true

        def readChannel = FileServiceFactory.fileService.openReadChannel(file, locked)
        def stream = new BufferedInputStream(Channels.newInputStream(readChannel))

        stream.withStream closure
        readChannel.close()

        return file
    }

    /**
     * Delete an AppEngineFile file from the blobstore.
     *
     * @param file the file to delete
     */
    static void delete(AppEngineFile file) {
        delete(getBlobKey(file))
    }

    /**
     * Get a reference to an App Engine file from its path.
     * <pre><code>
     *  def path = "...some path..."
     *  def file = files.fromPath(path)
     *  // equivalent of new AppEngineFile(path)
     * </code></pre>
     *
     * @param files the file service
     * @param path the path representing an AppEngineFile
     * @return the AppEngineFile instance
     */
    static AppEngineFile fromPath(FileService files, String path) {
        new AppEngineFile(path)
    }

    /**
     * Retrieves the blob key associated with an App Engine file.
     * <pre><code>
     *  def file = files.createNewBlobFile("text/plain")
     *  def key = file.blobKey
     *  // equivalent of FileServiceFactory.fileService.getBlobKey(file)
     * </code></pre>
     * 
     * @param file the file to get the blob key of
     * @return the blob key associated with the AppEngineFile
     */
    static BlobKey getBlobKey(AppEngineFile file) {
        FileServiceFactory.fileService.getBlobKey(file)
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
            throw new MissingMethodException("of", nm, [ns, c] as Object[])
        
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
        def fetchOptions = UrlFetchOptions.Builder.withDefaults()

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

    // ----------------------------------------------------------------
    // Category methods dedicated to the Channel service
    // ----------------------------------------------------------------

    /**
     * Send a message through the Channel service
     *
     * @param clientId the client ID
     * @param message the message to send
     */
    static void send(ChannelService channel, String clientId, String message) {
        channel.sendMessage(new ChannelMessage(clientId, message))
    }

    // ----------------------------------------------------------------
    // Backend service support
    // ----------------------------------------------------------------

    /**
     * Shortcut to use closures as shutdown hooks.
     * <pre><code>
     *  lifecycle.shutdownHook = { ...shutdown logic... }
     * </code></pre>
     *
     * @param manager the lifecycle manager
     * @param c the closure as shutdown hook
     */
    static void setShutdownHook(LifecycleManager manager, Closure c) {
        manager.setShutdownHook(c as ShutdownHook)
    }
}
