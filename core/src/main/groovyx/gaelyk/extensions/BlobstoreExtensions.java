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

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.ByteRange;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ImagesServiceFailureException;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.apphosting.api.ApiProxy;

/**
 * Blobstore extension module methods
 *
 * @author Guillaume Laforge
 */
public class BlobstoreExtensions {
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
     * @throws IOException 
     */
    public static <T> T withStream(BlobKey selfKey, Closure<T> c) throws IOException {
        BlobstoreInputStream stream = new BlobstoreInputStream(selfKey);
        return IOGroovyMethods.withStream(stream, c);
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
     * @throws S 
     */
    public static <T> T withReader(BlobKey selfKey, String encoding, Closure<T> c) throws IOException {
        BlobstoreInputStream stream = new BlobstoreInputStream(selfKey);
        return IOGroovyMethods.withReader(stream, encoding, c);
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
     * @throws IOException 
     */
    public static <T> T withReader(BlobKey selfKey, Closure<T> c) throws IOException {
        return withReader(selfKey, "UTF-8", c);
    }

    /**
     * Get the <code>BlobInfo</code> associated with a blob key with:
     * <pre><code>
     *  blobKey.info
     * </code></pre>
     * @param selfKey the blob key to get information from
     * @return an instance of <code>BlobInfo</code>
     */
    public static BlobInfo getInfo(BlobKey selfKey) {
        return new BlobInfoFactory().loadBlobInfo(selfKey);
    }

    /**
     * @return the name of the file stored in the blob
     */
    public static String getFilename(BlobKey selfKey) {
        return getInfo(selfKey).getFilename();
    }

    /**
     * @return the content-type of the blob
     */
    public static String getContentType(BlobKey selfKey) {
        return getInfo(selfKey).getContentType();
    }

    /**
     * @return the creation date of the file stored in the blob
     */
    public static Date getCreation(BlobKey selfKey) {
        return getInfo(selfKey).getCreation();
    }

    /**
     * @return the size of the blob
     */
    public static long getSize(BlobKey selfKey) {
        return getInfo(selfKey).getSize();
    }

    /**
     * Delete the blob associated with this blob key.
     *
     * @param selfKey the blob to delete, identified by its key
     */
    public static void delete(BlobKey selfKey) {
        BlobstoreServiceFactory.getBlobstoreService().delete(selfKey);
    }

    /**
     * Serve a range of the blob to the response.
     *
     * @param selfKey the blob to serve
     * @param the response on which to serve the blob
     * @throws IOException 
     */
    public static void serve(BlobKey selfKey, HttpServletResponse response) throws IOException {
        BlobstoreServiceFactory.getBlobstoreService().serve(selfKey, response);
    }
    
    /**
     * Serve a range of the blob to the response.
     *
     * @param selfKey the blob to serve
     * @param the response on which to serve the blob
     * @param range the range of the blob (parameter can be ommitted)
     * @throws IOException 
     */
    public static void serve(BlobKey selfKey, HttpServletResponse response, ByteRange range) throws IOException {
            BlobstoreServiceFactory.getBlobstoreService().serve(selfKey, range, response);
    }

    /**
     * Serve a range of the blob to the response.
     * @param selfKey
     * @param response
     * @param range
     * @throws IOException 
     */
    public static void serve(BlobKey selfKey, HttpServletResponse response, IntRange range) throws IOException {
        BlobstoreServiceFactory.getBlobstoreService().serve(selfKey, new ByteRange(range.getFromInt(), range.getToInt()), response);
    }

    /**
     * Fetch a segment of a blob.
     *
     * @param selfKey the blob key identifying the blob
     * @param start the beginning of the segment
     * @param end the end of the segment
     * @return an array of bytes
     */
    public static byte[] fetchData(BlobKey selfKey, long start, long end) {
        return BlobstoreServiceFactory.getBlobstoreService().fetchData(selfKey, start, end);
    }

    /**
     * Fetch a segment of a blob.
     * <pre><code>
     * blobKey.fetchData 1000..2000
     * </code></pre>
     *
     * @param selfKey the blob key identifying the blob
     * @param a Groovy int range
     * @return an array of bytes
     */
    public static byte[] fetchData(BlobKey selfKey, IntRange intRange) {
        return fetchData(selfKey, intRange.getFromInt(), intRange.getToInt());
    }

    /**
     * Fetch a segment of a blob.
     *
     * @param selfKey the blob key identifying the blob
     * @param byteRange a <code>ByteRange</code> representing the segment
     * @return an array of bytes
     */
    public static byte[] fetchData(BlobKey selfKey, ByteRange byteRange) {
        return fetchData(selfKey, byteRange.getStart(), byteRange.getEnd());
    }

    /**
     * Fetch an image stored in the blobstore.
     * <pre><code>
     * def image = blobKey.image
     * // equivalent of ImagesServiceFactory.makeImageFromBlob(selfKey)
     * </code></pre>
     *
     * <p>
     *     Note that this creates an image object only with the blob key set,
     *     it's not retrieving the image data right away, nor the dimensions of the image.
     * </p>
     *
     * @param selfKey the key
     * @return an Image
     */
    public static Image getImage(BlobKey selfKey) {
        return ImagesServiceFactory.makeImageFromBlob(selfKey);
    }

    /**
     * Obtains a URL that can serve the image stored as a blob dynamically.
     *
     * Note: getServingUrl can be time consuming so this should only be
     * done once per blobkey and the result should be stored for future use.
     *
     * <pre><code>
     * image.url = blobKey.getServingUrl(retry: 2, onRetry: { ex, i ->
     *    // do something... log exception? Thread.sleep(1000*i) ?
     *    true // must return true in order to continue next retry
     * }, onFail: { ex -> // do something
     * })
     * </code></pre>
     *
     * @param selfKey the key
     * @param a Map of options
     *          retries - the number of times to retry upon failure.
     *          onRetry - a closure that is called upon each retry attempt.
     *              Takes 2 parameters: 1. causing exception 2. # retries
     *              Closure must return true in order to continue otherwise
     *              no more retries will be attempted and onFail will be
     *              returned.  If no onFail is specified, null will be
     *              returned as the URL.
     *          onFail - a closure that is called if serving url could not
     *              be retrieved successfully.
     *              Takes 1 parameter: causing exception
     *              Note: if you don't pass an onFail closure, the
     *              underlying exception will propagate out otherwise
     *              the result of onFail will be returned as the URL.
     * @return a URL that can serve the image dynamically.
     */
    public static String getServingUrl(BlobKey blobKey, Map<String, Object> options) {
        ImagesService images = ImagesServiceFactory.getImagesService();
        int retries = options.containsKey("retry") ?  ((Number)options.get("retry")).intValue() : 0;
        int origRetries = retries;
        Closure<?> onFail = options.containsKey("onFail") ? (Closure<?>) options.get("onFail") : null;
        Closure<?> onRetry = options.containsKey("onRetry") ? (Closure<?>) options.get("onRetry") : null;
        while (true) {
            Exception ex = null;
            try {
                return images.getServingUrl(ServingUrlOptions.Builder.withBlobKey(blobKey));
            } catch (ApiProxy.ApiDeadlineExceededException adee) {
                ex = adee;
            } catch (IllegalArgumentException iae) {
                ex = iae;
            } catch (ImagesServiceFailureException isfe) {
                ex = isfe;
            }
            if (retries-- == 0) {
                if (onFail != null) {
                    return (String) onFail.call(ex);
                }
                throw ex instanceof RuntimeException ? (RuntimeException) ex : new InvokerInvocationException(ex);
            } else {
                if (onRetry != null) {
                    if (onRetry.call(ex, origRetries - (retries + 1)) != null)
                        return onFail != null ? (String) onFail.call(ex) : null;
                }
            }
        }
    }

    /**
     * Deletes a URL that was previously generated by the blobKey
     */
    public static void deleteServingUrl(BlobKey blobKey) {
        ImagesService images = ImagesServiceFactory.getImagesService();
        images.deleteServingUrl(blobKey);
    }

    /**
     * Collect all the BlobInfos of the blobs stored in the blobstore.
     * <pre><code>
     *     blobstore.each { BlobInfo info -> ... }
     * </code></pre>
     *
     * @param blobstore the blobstore service
     * @param c the closure passed to the collect method
     * @return a List of BlobInfos
     */
    public static List<BlobInfo> collect(BlobstoreService blobstore, Closure<BlobInfo> c) {
        return DefaultGroovyMethods.collect(new BlobInfoFactory().queryBlobInfos(), c);
    }

    /**
     * Iterates over all the BlobInfos of the blobs stored in the blobstore.
     * <pre><code>
     *      def filenames = blobstore.collect { BlobInfo info -> info.filename }
     * </code></pre>
     *
     * @param blobstore the blobstore service
     * @param c the closure passed to the each method
     * @return an iterator over BlobInfos
     */
    public static Iterator<BlobInfo> each(BlobstoreService blobstore, Closure<BlobInfo> c) {
        return DefaultGroovyMethods.each(new BlobInfoFactory().queryBlobInfos(), c);
    }
}
