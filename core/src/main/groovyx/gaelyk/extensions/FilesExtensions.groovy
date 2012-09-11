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

import com.google.appengine.api.files.AppEngineFile
import com.google.appengine.api.files.FileServiceFactory
import com.google.appengine.api.files.FileWriteChannel

import java.nio.channels.Channels
import groovy.transform.CompileStatic
import com.google.appengine.api.files.FileService
import com.google.appengine.api.blobstore.BlobKey

/**
 * File service extension methods
 *
 * @author Guillaume Laforge
 */
class FilesExtensions {
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
    // TODO @CompileStatic
    static AppEngineFile withWriter(AppEngineFile file , Map options = [:], Closure closure) {
        boolean locked = options.containsKey("locked") ? options.locked : true
        boolean closeFinally = options.containsKey("finalize") ? options.finalize : true
        String encoding = options.encoding ?: "UTF-8"

        def writeChannel = FileServiceFactory.fileService.openWriteChannel(file, locked)
        def writer = new PrintWriter(Channels.newWriter(writeChannel, encoding))

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
    // TODO @CompileStatic
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
    // TODO @CompileStatic
    static AppEngineFile withReader(AppEngineFile file, Map options = [:], Closure closure) {
        boolean locked = options.containsKey("locked") ? options.locked : true
        String encoding = options.encoding ?: "UTF-8"

        def readChannel = FileServiceFactory.fileService.openReadChannel(file, locked)
        def reader = new BufferedReader(Channels.newReader(readChannel, encoding))

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
    // TODO @CompileStatic
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
    @CompileStatic
    static void delete(AppEngineFile file) {
        BlobstoreExtensions.delete(getBlobKey(file))
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
    @CompileStatic
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
    @CompileStatic
    static BlobKey getBlobKey(AppEngineFile file) {
        FileServiceFactory.fileService.getBlobKey(file)
    }

    /**
     * Retrieves the <code>AppEngineFile</code> associated with this <code>BlobKey</code>
     *
     * @param key the blob key
     * @return the app engine file
     */
    @CompileStatic
    static AppEngineFile getFile(BlobKey key) {
        FileServiceFactory.fileService.getBlobFile(key)
    }
}
