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
package groovyx.gaelyk;


import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceException;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.DeadlineExceededException;

/**
 * Base Category methods decorating the Google App Engine SDK classes
 * adding new shortcut methods to simplify the usage of the SDK
 * from within Groovy servlets and templates.
 *
 * @author Guillaume Laforge
 */
public class GaelykCategoryBase {

    /**
     * Get an object from the cache, with a object key, ignoring any exceptions.
     *
     * @param key the Object key
     * @return the value stored under that key
     */
    static Object get(MemcacheService memcache, Object key) {
        try {
            return memcache.get(key);
        } catch (MemcacheServiceException mse) {
        } catch (DeadlineExceededException dee) {
        } catch (ApiProxy.CancelledException ce) { }
        return null;
    }

    /**
      * Put an object in the cache under a Object key, coerced to a String, with an expiration and a SetPolicy, ignoring any exceptions.
      *
      * @param key a Object key
      * @param value the value to put in the cache
      * @param expiration expiration of the key/value
      * @param policy a SetPolicy
      */
     static void put(MemcacheService memcache, Object key, Object value, Expiration expiration, MemcacheService.SetPolicy policy) {
         try {
             memcache.put(key, value, expiration, policy);
         } catch (MemcacheServiceException mse) {
         } catch (DeadlineExceededException dee) {
         } catch (ApiProxy.CancelledException ce) { }
     }

}
