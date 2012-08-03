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
import com.google.appengine.api.xmpp.JID
import com.google.appengine.api.xmpp.SendResponse
import com.google.appengine.api.xmpp.MessageType
import com.google.appengine.api.xmpp.Presence
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
import com.google.appengine.api.images.Image
import com.google.appengine.api.images.ImagesServiceFactory as ISF
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
import com.google.appengine.api.datastore.AsyncDatastoreService
import com.google.appengine.api.xmpp.Subscription
import com.google.appengine.api.xmpp.PresenceBuilder
import com.google.appengine.api.xmpp.PresenceType
import com.google.appengine.api.xmpp.SubscriptionBuilder
import com.google.appengine.api.xmpp.SubscriptionType
import com.google.appengine.api.xmpp.PresenceShow
import com.google.appengine.api.files.AppEngineFile
import com.google.appengine.api.files.FileService
import com.google.appengine.api.files.FileServiceFactory
import com.google.appengine.api.taskqueue.DeferredTask
import com.google.appengine.api.datastore.Query
import com.google.appengine.api.datastore.PreparedQuery
import com.google.appengine.api.datastore.KeyFactory
import com.google.appengine.api.datastore.FetchOptions
import com.google.appengine.api.LifecycleManager
import com.google.appengine.api.LifecycleManager.ShutdownHook
import com.google.appengine.api.blobstore.BlobstoreService
import com.google.appengine.api.memcache.AsyncMemcacheService
import com.google.appengine.api.memcache.MemcacheServiceFactory
import com.google.appengine.api.memcache.MemcacheServiceException
import com.google.apphosting.api.ApiProxy
import com.google.appengine.api.images.ImagesService
import com.google.appengine.api.images.ImagesServiceFactory
import com.google.appengine.api.images.ImagesServiceFailureException
import com.google.appengine.api.backends.BackendService
import com.google.appengine.api.ThreadManager
import com.google.appengine.api.search.SearchService
import com.google.appengine.api.search.SearchServiceFactory
import com.google.appengine.api.search.Index
import com.google.appengine.api.search.Consistency
import com.google.appengine.api.search.IndexSpec
import com.google.appengine.api.search.AddResponse
import com.google.appengine.api.search.Document
import com.google.appengine.api.search.Field

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.xml.StreamingMarkupBuilder
import groovy.util.slurpersupport.GPathResult

import org.codehaus.groovy.runtime.DefaultGroovyMethods


import groovyx.gaelyk.datastore.PogoEntityCoercion
import groovyx.gaelyk.query.QueryBuilder
import groovyx.gaelyk.search.DocumentDefinitions
import groovyx.gaelyk.cache.CacheHandler

import java.nio.channels.Channels
import java.util.concurrent.Future
import javax.mail.internet.MimeMessage
import javax.servlet.http.HttpServletRequest
import javax.mail.Session
import javax.servlet.http.HttpServletResponse
import groovyx.gaelyk.extensions.DatastoreExtensions
import groovyx.gaelyk.extensions.BlobstoreExtensions

/**
 * Category methods decorating the Google App Engine SDK classes
 * adding new shortcut methods to simplify the usage of the SDK
 * from within Groovy servlets and templates.
 *
 * @author Guillaume Laforge
 * @author Scott Murphy
 */
class GaelykCategory {

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
    @CompileStatic
    static String toQueryString(Map self) {
        self.collect { k, v -> "${URLEncoder.encode(k.toString(), 'UTF-8')}=${URLEncoder.encode(v.toString(), 'UTF-8')}" }.join('&')
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
     *  "agR0ZXN0cg8LEgdwZXJzb25zIgJtZQw" as Key
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
        else if (clazz == Key)
            KeyFactory.stringToKey(self)
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
    @CompileStatic
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
    @CompileStatic
    static Object asType(byte[] self, Class blobClass) {
        if (blobClass == ShortBlob)
            new ShortBlob(self)
        else if (blobClass == Blob)
            new Blob(self)
        else DefaultGroovyMethods.asType(self, blobClass)
    }

    /**
     * Converter method for converting a pair of numbers (in a list) into a GeoPt instance:
     * <pre><code>
     *  [45.32, 54.54f] as GeoPt
     * </code></pre>
     * Or to convert a list of elements into a Key, avoiding the usage of KeyFactory.createKey():
     * <pre><code>
     *  [parentKey, 'address', 333] as Key
     *  [parentKey, 'address', 'name'] as Key
     *  ['address', 444] as Key
     *  ['address', 'name'] as Key
     * </code></pre>
     */
    static Object asType(List list, Class clazz) {
        if (clazz == GeoPt && list.size() == 2 && list.every { it instanceof Number }) {
            new GeoPt(*list*.floatValue())
        } else if (clazz == Key && list.size() == 3 && list[0] instanceof Key && list[1] instanceof String &&
                (list[2] instanceof Number || list[2] instanceof String)) {
            // KeyFactory.createKey(Key, String, long)
            // KeyFactory.createKey(Key, String, String)
            KeyFactory.createKey(*list)
        } else if (clazz == Key && list.size() == 2 && list[0] instanceof String &&
            (list[1] instanceof Number || list[1] instanceof String)) {
            // KeyFactory.createKey(String, long)
            // KeyFactory.createKey(String, String)
            KeyFactory.createKey(*list)
        } else DefaultGroovyMethods.asType(list, clazz)
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
    @CompileStatic
    static CapabilityStatus getAt(CapabilitiesService capabilities, Capability capa) {
        return capabilities.getStatus(capa).getStatus()
    }

    /**
     * Coerces a capability status into a boolean.
     * This mechanism is used by the "Groovy Truth".
     *
     * @return true if the capability status is ENABLED, otherwise false.
     */
    @CompileStatic
    static boolean asBoolean(CapabilityStatus capabilityStatus) {
        capabilityStatus == CapabilityStatus.ENABLED || capabilityStatus == CapabilityStatus.SCHEDULED_MAINTENANCE
    }

    // ----------------------------------------------------------------
    // Category methods dedicated to the URL Fetch service
    // ----------------------------------------------------------------

    /**
     * @return the HTTP status code (synonym of <code>getResponseCode()</code>)
     */
    @CompileStatic
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
            def encodedParams = options.params.collect { k, v -> "${URLEncoder.encode(k, 'UTF-8')}=${URLEncoder.encode(v, 'UTF-8')}" }.join('&')
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
    @CompileStatic
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
    @CompileStatic
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
    @CompileStatic
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
    @CompileStatic
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
    @CompileStatic
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
    @CompileStatic
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
    @CompileStatic
    static void setShutdownHook(LifecycleManager manager, Closure c) {
        manager.setShutdownHook(c as ShutdownHook)
    }

    // ----------------------------------------------------------------
    // Search service support
    // ----------------------------------------------------------------

    /**
     * Get a search service instance restricted to a given namespace.
     * <pre><code>
     *     search['namespaceName']
     * </code></pre>
     *
     * @param search the search service
     * @param namespace the namespace name
     * @return a search service over a specific namespace
     */
    @CompileStatic
    static SearchService getAt(SearchService search, String namespace) {
        SearchServiceFactory.getSearchService(namespace)
    }

    /**
     * Shortcut notation to easily get an index from the search service.
     * <pre><code>
     *     def index = search.index("books", Consistency.PER_DOCUMENT)
     * </code></pre>
     *
     * @param search the search service
     * @param indexName the name of the index
     * @param consistency the consistency
     * @return an index
     */
    @CompileStatic
    static Index index(SearchService search, String indexName, Consistency consistency) {
        search.getIndex(IndexSpec.newBuilder().setName(indexName).setConsistency(consistency).build())
    }

    /**
     * Add a new document to the index.
     *
     * <pre><code>
     *     index.add {
     *         document(id: "1234", locale: US, rank: 3) {
     *             title text: "Big bad wolf", locale: ENGLISH
     *             published date: new Date()
     *             numberOfCopies number: 35
     *             summary html: "<p>super story</p>", locale: ENGLISH
     *             description text: "a book for children"
     *             category atom: "children"
     *             keyword text: "wolf"
     *             keyword text: "red hook"
     *         }
     *     }
     * </code></pre>
     *
     * The named arguments are restricted to id, locale and rank.
     * The calls inside the closure correspond to the field name, its type thanks to a named argument
     * of the form <code>type: value</code>, and optionally a locale.
     * You can have several times the same field name, for multi-valued fields.
     *
     * @param index the index to which to add the documents
     * @param closure the closure defining the documents to be added to the index
     * @return an instance of AddResponse
     */
    @CompileStatic
    static AddResponse add(Index index, Closure closure) {
        def docDefClosure = (Closure)closure.clone()
        docDefClosure.resolveStrategy = Closure.DELEGATE_FIRST
        def definitions = new DocumentDefinitions()
        docDefClosure.delegate = definitions
        docDefClosure()

        index.add(definitions.docs)
    }

    /**
     * Get a document field raw value or list of raw values.
     * <pre><code>
     *      assert document.title = "Big bad wolf"
     *      assert document.keyword[0] == "wolf"
     *      assert document.keyword[1] == "red hook"
     * </code></pre>
     *
     * @param document the document
     * @param fieldName the field name
     * @return a raw value or a list of raw values if the field is multivalued
     */
    static Object get(Document document, String fieldName) {
        List<Field> fields = document.getField(fieldName).collect()

        switch (fields.size()) {
            case 0:
                throw new GroovyRuntimeException("No such field '$fieldName' for document '$document'")
            case 1:
                return getFieldRawValue(fields[0])
            default:
                return fields.collect{ Field field -> getFieldRawValue(field) }
        }
    }

    @CompileStatic
    private static getFieldRawValue(Field field) {
        switch(field.getType()) {
            case Field.FieldType.ATOM:   return field.atom
            case Field.FieldType.DATE:   return field.date
            case Field.FieldType.HTML:   return field.HTML
            case Field.FieldType.NUMBER: return field.number
            case Field.FieldType.TEXT:   return field.text
        }
    }

    // ----------------------------------------------------------------
    // Miscelanous methods
    // ----------------------------------------------------------------

    /**
     * Make the get access to Future properties transparent without calling get().
     *
     * @param future the future
     * @param name the property
     * @return the value associated with that property
     */
    static Object get(Future future, String name) {
        DatastoreExtensions.transformValueForRetrieval(future.get().getProperty(name))
    }

    /**
     * Make the set access to Future properties transparent without calling get().
     *
     * @param future the future
     * @param name the property
     * @param value the new value for the property
     */
    static void set(Future future, String name, Object value) {
        future.get().setProperty(name, DatastoreExtensions.transformValueForStorage(value))
    }
    
    /**
    * Runs code in the background thread.
    *
    * @param the code supposed to run in background thread
    */
    @CompileStatic
    static Thread run(BackendService backends, Runnable code){
        Thread thread = ThreadManager.createBackgroundThread(code);
        thread.start()
        thread
    }
}
