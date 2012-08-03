/*
 * Copyright 2009-2012 the original author or authors.
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
package groovyx.gaelyk.extensions

import groovy.transform.CompileStatic
import com.google.appengine.api.memcache.MemcacheService
import com.google.appengine.api.memcache.Expiration
import com.google.appengine.api.memcache.MemcacheServiceException
import groovyx.gaelyk.cache.CacheHandler
import com.google.appengine.api.memcache.AsyncMemcacheService
import com.google.appengine.api.memcache.MemcacheServiceFactory
import java.util.concurrent.Future

/**
 * Memcache service extension methods
 *
 * @author Guillaume Laforge
 * @author Scott Murphy
 */
class MemcacheExtensions {

    /**
     * Get an object from the cache, with a object key, ignoring any exceptions.
     *
     * @param key the Object key
     * @return the value stored under that key
     */
    @CompileStatic
    static Object get(MemcacheService memcache, Object key) {
        try {
            return memcache.get((Object)key)
        } catch (MemcacheServiceException mse) {}
        return null
    }

    /**
     * Put an object in the cache under a Object key, coerced to a String, with an expiration and a SetPolicy, ignoring any exceptions.
     *
     * @param key a Object key
     * @param value the value to put in the cache
     * @param expiration expiration of the key/value
     * @param policy a SetPolicy
     */
    @CompileStatic
    static void put(MemcacheService memcache, Object key, Object value, Expiration expiration, MemcacheService.SetPolicy policy) {
        try {
            memcache.put(key, (Object)value, expiration, policy)
        } catch (MemcacheServiceException mse) {}
    }

    /**
     * Get an object from the cache, with a GString key, coerced to a String.
     *
     * @param key the GString key
     * @return the value stored under that key
     */
    @CompileStatic
    static Object get(MemcacheService memcache, String key) {
        get(memcache, (Object)key)
    }

    /**
     * Get an object from the cache, with a GString key, coerced to a String.
     *
     * @param key the GString key
     * @return the value stored under that key
     */
    @CompileStatic
    static Object get(MemcacheService memcache, GString key) {
        get(memcache, (Object)key.toString())
    }

    /**
     * Get an object from the cache, identified by its key, using the subscript notation:
     * <code>def obj = memcache[key]</code>
     *
     * @param key the key identifying the object to get from the cache
     */
    @CompileStatic
    static Object getAt(MemcacheService memcache, Object key) {
        get(memcache, key)
    }

    /**
     * Get an object from the cache, identified by its key, using the subscript notation:
     * <code>def obj = memcache[key]</code>
     *
     * @param key the key identifying the object to get from the cache
     */
    @CompileStatic
    static Object getAt(MemcacheService memcache, String key) {
        //TODO this method should be removed once we only need a getAt() method taking Object key
        // looks like a bug in current Groovy where the two variants are needed
        get(memcache, (Object)key)
    }

    /**
     * Put an object in the cache under a GString key, coerced to a String.
     *
     * @param key a GString key
     * @param value the value to put in the cache
     */
    @CompileStatic
    static void set(MemcacheService memcache, String key, Object value) {
        put(memcache, (Object)key, value, null, MemcacheService.SetPolicy.SET_ALWAYS)
    }

    /**
     * Put an object in the cache under a GString key, coerced to a String.
     *
     * @param key a GString key
     * @param value the value to put in the cache
     */
    @CompileStatic
    static void put(MemcacheService memcache, GString key, Object value) {
        put(memcache, (Object)key.toString(), value, null, MemcacheService.SetPolicy.SET_ALWAYS)
    }

    /**
     * Put an object in the cache under a GString key, coerced to a String, with an expiration.
     *
     * @param key a GString key
     * @param value the value to put in the cache
     * @param expiration expiration of the key/value
     */
    @CompileStatic
    static void put(MemcacheService memcache, GString key, Object value, Expiration expiration) {
        put(memcache, (Object)key.toString(), value, expiration, MemcacheService.SetPolicy.SET_ALWAYS)
    }

    /**
     * Put an object in the cache under a GString key, coerced to a String, with an expiration and a SetPolicy.
     *
     * @param key a GString key
     * @param value the value to put in the cache
     * @param expiration expiration of the key/value
     * @param policy a SetPolicy
     */
    @CompileStatic
    static void put(MemcacheService memcache, GString key, Object value, Expiration expiration, MemcacheService.SetPolicy policy) {
        put(memcache, (Object)key.toString(), value, expiration, policy)
    }

    /**
     * Put an object into the cache, identified by its key, using the subscript notation:
     * <code>memcache[key] = value</code>
     *
     * @param key the key identifying the object to put in the cache
     * @param value the value to put in the cache
     */
    @CompileStatic
    static void putAt(MemcacheService memcache, String key, Object value) {
        //TODO this method should be removed once we only need a putAt() method taking Object key
        // looks like a bug in current Groovy where the two variants are needed
        put(memcache, (Object)key, value, null, MemcacheService.SetPolicy.SET_ALWAYS)
    }

    /**
     * Put an object into the cache, identified by its key, using the subscript notation:
     * <code>memcache[key] = value</code>
     *
     * @param key the key identifying the object to put in the cache
     * @param value the value to put in the cache
     */
    @CompileStatic
    static void putAt(MemcacheService memcache, Object key, Object value) {
        put(memcache, key, value, null, MemcacheService.SetPolicy.SET_ALWAYS)
    }

    /**
     * Shortcut to check whether a key is contained in the cache using the <code>in</code> operator:
     * <code>key in memcache</code>
     */
    @CompileStatic
    static boolean isCase(MemcacheService memcache, Object key) {
        try {
            return memcache.contains(key)
        } catch (MemcacheServiceException mse) { }
        false
    }

    /**
     * Clear the cached content for a given URI.
     *
     * @param uri the URI for which to clear the cache
     * @return the set of keys that have been cleared (should be two in this case)
     */
    @CompileStatic
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
                    put(memcache, key, result, Expiration.byDeltaSeconds(60), MemcacheService.SetPolicy.SET_ALWAYS)
                    return result
                }
            }
        }
    }

    // Asynchronous memcache service

    /**
     * From the <code>memcache</code> binding variable, you can access the asynchronous Memcache service:
     * <pre><code>
     *      memcache.async
     * </code></pre>
     * @return the asynchronous Memcache service
     */
    @CompileStatic
    static AsyncMemcacheService getAsync(MemcacheService memcache) {
        MemcacheServiceFactory.asyncMemcacheService
    }

    /**
     * Get an object from the async cache, with a GString key, coerced to a String.
     *
     * @param key the GString key
     * @return the value stored under that key
     */
    @CompileStatic
    static Future<? extends Object> get(AsyncMemcacheService memcache, String key) {
        memcache.get((Object)key)
    }

    /**
     * Get an object from the async cache, with a GString key, coerced to a String.
     *
     * @param key the GString key
     * @return the value stored under that key
     */
    @CompileStatic
    static Future<? extends Object> get(AsyncMemcacheService memcache, GString key) {
        memcache.get(key.toString())
    }

    /**
     * Get an object from the async cache, identified by its key, using the subscript notation:
     * <code>def obj = memcache[key]</code>
     *
     * @param key the key identifying the object to get from the cache
     */
    @CompileStatic
    static Future<? extends Object> getAt(AsyncMemcacheService memcache, Object key) {
        memcache.get(key)
    }

    /**
     * Get an object from the async cache, identified by its key, using the subscript notation:
     * <code>def obj = memcache[key]</code>
     *
     * @param key the key identifying the object to get from the cache
     */
    @CompileStatic
    static Future<? extends Object> getAt(AsyncMemcacheService memcache, String key) {
        //TODO this method should be removed once we only need a getAt() method taking Object key
        // looks like a bug in current Groovy where the two variants are needed
        memcache.get(key)
    }

    /**
     * Put an object in the async cache under a GString key, coerced to a String.
     *
     * @param key a GString key
     * @param value the value to put in the cache
     */
    @CompileStatic
    static Future<Void> set(AsyncMemcacheService memcache, String key, Object value) {
        memcache.put(key, value)
    }

    /**
     * Put an object in the async cache under a GString key, coerced to a String.
     *
     * @param key a GString key
     * @param value the value to put in the cache
     */
    @CompileStatic
    static Future<Void> put(AsyncMemcacheService memcache, GString key, Object value) {
        memcache.put(key.toString(), value)
    }

    /**
     * Put an object in the async cache under a GString key, coerced to a String, with an expiration.
     *
     * @param key a GString key
     * @param value the value to put in the cache
     * @param expiration expiration of the key/value
     */
    @CompileStatic
    static Future<Void> put(AsyncMemcacheService memcache, GString key, Object value, Expiration expiration) {
        memcache.put(key.toString(), value, expiration)
    }

    /**
     * Put an object in the async cache under a GString key, coerced to a String, with an expiration and a SetPolicy.
     *
     * @param key a GString key
     * @param value the value to put in the cache
     * @param expiration expiration of the key/value
     * @param policy a SetPolicy
     */
    @CompileStatic
    static Future<Boolean> put(AsyncMemcacheService memcache, GString key, Object value, Expiration expiration, MemcacheService.SetPolicy policy) {
        memcache.put(key.toString(), value, expiration, policy)
    }

    /**
     * Put an object into the cache, identified by its key, using the subscript notation:
     * <code>memcache[key] = value</code>
     *
     * @param key the key identifying the object to put in the cache
     * @param value the value to put in the cache
     */
    @CompileStatic
    static Future<Void> putAt(AsyncMemcacheService memcache, String key, Object value) {
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
    @CompileStatic
    static Future<Void> putAt(AsyncMemcacheService memcache, Object key, Object value) {
        memcache.put(key, value)
    }
}
