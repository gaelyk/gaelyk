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
    // Miscellaneous methods
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
