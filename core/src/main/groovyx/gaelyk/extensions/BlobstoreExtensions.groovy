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
import com.google.appengine.api.blobstore.BlobKey
import com.google.appengine.api.blobstore.BlobstoreInputStream
import com.google.appengine.api.blobstore.BlobInfo
import com.google.appengine.api.blobstore.BlobInfoFactory
import com.google.appengine.api.blobstore.BlobstoreServiceFactory
import javax.servlet.http.HttpServletResponse
import com.google.appengine.api.blobstore.ByteRange
import com.google.appengine.api.images.Image
import com.google.appengine.api.images.ImagesServiceFactory
import com.google.appengine.api.images.ImagesService
import com.google.apphosting.api.ApiProxy
import com.google.appengine.api.images.ImagesServiceFailureException
import com.google.appengine.api.blobstore.BlobstoreService

/**
 * Blobstore extension module methods
 *
 * @author Guillaume Laforge
 */
class BlobstoreExtensions {
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
    @CompileStatic
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
    @CompileStatic
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
    @CompileStatic
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
    @CompileStatic
    static BlobInfo getInfo(BlobKey selfKey) {
        new BlobInfoFactory().loadBlobInfo(selfKey)
    }

    /**
     * @return the name of the file stored in the blob
     */
    @CompileStatic
    static String getFilename(BlobKey selfKey) {
        getInfo(selfKey).filename
    }

    /**
     * @return the content-type of the blob
     */
    @CompileStatic
    static String getContentType(BlobKey selfKey) {
        getInfo(selfKey).contentType
    }

    /**
     * @return the creation date of the file stored in the blob
     */
    @CompileStatic
    static Date getCreation(BlobKey selfKey) {
        getInfo(selfKey).creation
    }

    /**
     * @return the size of the blob
     */
    @CompileStatic
    static long getSize(BlobKey selfKey) {
        getInfo(selfKey).size
    }

    /**
     * Delete the blob associated with this blob key.
     *
     * @param selfKey the blob to delete, identified by its key
     */
    @CompileStatic
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
    @CompileStatic
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
    @CompileStatic
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
    @CompileStatic
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
     * <p>
     *     Note that this creates an image object only with the blob key set,
     *     it's not retrieving the image data right away, nor the dimensions of the image.
     * </p>
     *
     * @param selfKey the key
     * @return an Image
     */
    @CompileStatic
    static Image getImage(BlobKey selfKey) {
        ImagesServiceFactory.makeImageFromBlob(selfKey)
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
    static String getServingUrl(BlobKey blobKey, Map options) {
        ImagesService images = ImagesServiceFactory.getImagesService()
        int retries = options.retry?:0
        while (true) {
            Exception ex = null
            try {
                return images.getServingUrl(blobKey)
            } catch (ApiProxy.ApiDeadlineExceededException adee) {
                ex = adee
            } catch (IllegalArgumentException iae) {
                ex = iae
            } catch (ImagesServiceFailureException isfe) {
                ex = isfe
            }
            if (retries-- == 0) {
                if (options.onFail) {
                    return options.onFail(ex)
                }
                throw ex
            } else {
                if (options.onRetry) {
                    if (!options.onRetry(ex, options.retry - (retries + 1)))
                        return options.onFail? options.onFail(ex) : null
                }
            }
        }
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
    @CompileStatic
    static List<BlobInfo> collect(BlobstoreService blobstore, Closure<BlobInfo> c) {
        new BlobInfoFactory().queryBlobInfos().collect c
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
    @CompileStatic
    static Iterator<BlobInfo> each(BlobstoreService blobstore, Closure<BlobInfo> c) {
        new BlobInfoFactory().queryBlobInfos().each c
    }
}
