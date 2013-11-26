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
package groovyx.gaelyk.extensions;

import groovy.lang.Closure;
import groovy.lang.IntRange;
import groovyx.gaelyk.RetryingFuture;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.ByteRange;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.xmpp.JID;

/**
 * Miscellaneous extension methods and utilities
 *
 * @author Guillaume Laforge
 * @author Vladimir Orany
 */
public class MiscExtensions {
    // ----------------------------------------------------------------
    // General utility category methods
    // ----------------------------------------------------------------

    /**
     * Transforms a map of key / value pairs into a properly URL encoded query string.
     *
     * <pre><code>
     *  assert "title=Gaelyk" == [title: "Gaelyk"].toQueryString()
     * </code></pre>
     *
     * @return a query string represenging the input map
     */
    public static String toQueryString(Map<?,?> self) {
        if (self == null || self.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try {
            for (Entry<?, ?> entry : self.entrySet()) {
                if (entry.getKey() != null) {
                    sb.append(URLEncoder.encode(entry.getKey().toString(), "UTF-8"));
                }
                sb.append("=");
                if (entry.getValue() != null) {
                    sb.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));                    
                }
                sb.append("&");
            }
        } catch (UnsupportedEncodingException e) {
            // should never happen
        }
        String result = sb.toString();
        return result.substring(0, result.length() - 1);
    }

    // ------------------------------
    // Additional converter methods
    // ------------------------------

    /**
     * Converter method for converting strings into various GAE specific types.
     * 
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
     * 
     * Also converts single String to String array with one element.
     * @return String converted to given object if possible
     */
    public static <T> T asType(String self, Class<T> clazz) {
        if (clazz == Email.class)
            return clazz.cast(new Email(self));
        if (clazz == Text.class)
            return clazz.cast(new Text(self));
        if (clazz == BlobKey.class)
            return clazz.cast(new BlobKey(self));
        if (clazz == Link.class)
            return clazz.cast(new Link(self));
        if (clazz == Category.class)
            return clazz.cast(new Category(self));
        if (clazz == PhoneNumber.class)
            return clazz.cast(new PhoneNumber(self));
        if (clazz == PostalAddress.class)
            return clazz.cast(new PostalAddress(self));
        if (clazz == Rating.class)
            return clazz.cast(new Rating(Integer.valueOf(self)));
        if (clazz == JID.class)
            return clazz.cast(new JID(self));
        if (clazz == Key.class)
            return clazz.cast(KeyFactory.stringToKey(self));
        if (clazz == String[].class)
            return clazz.cast(new String[] { self });
        return StringGroovyMethods.asType(self, clazz);
    }

    /**
     * Converter method for converting a URL into a Link instance
     * <pre><code>
     *  new URL("http://gaelyk.appspot.com") as Link
     * </code></pre>
     * return {@link Link} representation of given {@link URL}
     */
    public static <T> T asType(URL self, Class<T> linkClass) {
        if (linkClass == Link.class)
            return linkClass.cast(new Link(self.toString()));
        return DefaultGroovyMethods.asType(self, linkClass);
    }

    /**
     * Converter method for converting an integer into a Rating instance
     * <pre><code>
     *  32 as Rating
     * </code></pre>
     */
    public static <T> T asType(Integer self, Class <T> ratingClass) {
        if (ratingClass == Rating.class)
            return ratingClass.cast(new Rating(self));
        return DefaultGroovyMethods.asType(self, ratingClass);
    }

    /**
     * Converter method for converting a byte array into a Blob or ShortBlob instance
     * <pre><code>
     *  "some byte".getBytes() as Blob
     *  "some byte".getBytes() as ShortBlob
     * </code></pre>
     */
    public static <T> T asType(byte[] self, Class<T> blobClass) {
        if (blobClass == ShortBlob.class)
            return blobClass.cast(new ShortBlob(self));
        if (blobClass == Blob.class)
            return blobClass.cast(new Blob(self));
        return DefaultGroovyMethods.asType(self, blobClass);
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
    public static <T> T asType(List<?> list, Class<T> clazz) {
        if (list == null || clazz == null) return null;
        if (clazz == GeoPt.class && list.size() == 2 && list.get(0) instanceof Number && list.get(1) instanceof Number) {
            return clazz.cast(new GeoPt(((Number) list.get(0)).floatValue(), ((Number) list.get(1)).floatValue()));
        }
        
        if (clazz == Key.class) {
            if (list.size() == 2 && list.get(0) instanceof String) {
                if (list.get(1) instanceof Number) {
                    return clazz.cast(KeyFactory.createKey((String) list.get(0), ((Number) list.get(1)).longValue()));
                }
                if (list.get(1) instanceof String) {
                    return clazz.cast(KeyFactory.createKey((String) list.get(0), ((String) list.get(1))));
                }
            }
            if (list.size() == 3 && list.get(0) instanceof Key && list.get(1) instanceof String) {
                if (list.get(2) instanceof Number) {
                    return clazz.cast(KeyFactory.createKey((Key) list.get(0), (String) list.get(1), ((Number) list.get(2)).longValue()));
                }
                if (list.get(2) instanceof String) {
                    return clazz.cast(KeyFactory.createKey((Key) list.get(0), (String) list.get(1), ((String) list.get(2))));
                }
            }
        }
        
        return clazz.cast(DefaultGroovyMethods.asType(list, clazz));
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
    public static <T> T asType(IntRange range, Class<T> byteRangeClass) {
        if (byteRangeClass == ByteRange.class)
            return byteRangeClass.cast(new ByteRange(range.getFromInt(), range.getToInt()));
        return DefaultGroovyMethods.asType(range, byteRangeClass);
    }
	
	/**
	 * Perform cast on last item of String array if expected
	 * class is not array or collection.
	 *
	 * This method should handle situation when multiple params are submitted but
	 * only one value is expected. The first value is used for this purpose.
	 *
	 * @param multipleParams	usually multiple values in params map
	 * @param cls				desired class e.g. int
	 * @return first value cast to desired type if the desired type is not array or collection
	 */
	@SuppressWarnings("deprecation") public static <T> T asType(String[] multipleParams, Class<T> cls){
		if(!cls.isArray() && !Collection.class.isAssignableFrom(cls) && multipleParams.length > 0 ){
			return DefaultGroovyMethods.asType(multipleParams[0], cls);
		}
		return DefaultGroovyMethods.asType(multipleParams, cls);
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
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
	public static Object get(Future<Entity> future, String name) throws InterruptedException, ExecutionException {
        return DatastoreExtensions.transformValueForRetrieval(future.get().getProperty(name));
    }

    /**
     * Make the set access to Future properties transparent without calling get().
     *
     * @param future the future
     * @param name the property
     * @param value the new value for the property
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public static void set(Future<Entity> future, String name, Object value) throws InterruptedException, ExecutionException {
        future.get().setProperty(name, DatastoreExtensions.transformValueForStorage(value));
    }
    
    /**
     * Creates {@link RetryingFuture} from the closure.
     * 
     * <p>Allows following notation to create retrying closure:</p>
     * 
     * <code>
     *  Future result = 3 * { index.searchAsync('a=b') }
     * </code>
     * 
     * Closure must return {@link Future}.
     * 
     * @param factory closure to construct new {@link Future}
     * @param retries number of retries before failing
     * @return future which will first retries for particular times before throwing the exception
     */
    public static <R> Future<R> multiply(Number retries, Closure<Future<R>> factory){
        return RetryingFuture.retry(retries.intValue(), factory);
    }
    
    
}
