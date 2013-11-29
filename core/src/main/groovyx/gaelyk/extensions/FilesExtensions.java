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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.runtime.IOGroovyMethods;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.FinalizationException;
import com.google.appengine.api.files.LockException;

/**
 * File service extension methods
 *
 * @author Guillaume Laforge
 */
@SuppressWarnings("deprecation") public class FilesExtensions {
    
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
     * @throws IOException 
     * @throws LockException 
     * @throws FinalizationException 
     * @throws FileNotFoundException 
     */
    public static AppEngineFile withWriter(AppEngineFile file , Closure<?> closure) throws FileNotFoundException, FinalizationException, LockException, IOException {
        return withWriter(file, new HashMap<String, Object>(), closure);
    }
    
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
     * @throws IOException 
     * @throws LockException 
     * @throws FinalizationException 
     * @throws FileNotFoundException 
     */
    public static AppEngineFile withWriter(AppEngineFile file , Map<String, Object> options, Closure<?> closure) throws FileNotFoundException, FinalizationException, LockException, IOException {
        boolean locked = isLocked(options);
        boolean closeFinally = isFinalize(options);
        String encoding = (String) (options.containsKey("encoding") ? options.get("encoding") : "UTF-8");

        FileWriteChannel writeChannel = FileServiceFactory.getFileService().openWriteChannel(file, locked);
        PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, encoding));

        IOGroovyMethods.withWriter(writer, closure);

        if (closeFinally) {
            writeChannel.closeFinally();
        } else {
            writeChannel.close();
        }

        return file;
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
     * </code></pre>
     *
     * @param file the AppEngineFile to write to
     * @param options an optional map containing two possible keys:
     *      locked (a boolean, if you want to acquire a write lock on the file -- false by default),
     *      finalize (a boolean, if you want to close the file definitively -- false by default).
     * @param closure the closure with the output stream as parameter
     * @return the original file, for chaining purpose
     * @throws IOException 
     * @throws LockException 
     * @throws FinalizationException 
     * @throws FileNotFoundException 
     */
    public static AppEngineFile withOutputStream(AppEngineFile file, Closure<?> closure) throws FileNotFoundException, FinalizationException, LockException, IOException {
        return withOutputStream(file, new HashMap<String, Object>(), closure);
    }

    /**
     * Method creating an output stream for the AppEngineFile, writing bynary content to it, and closes it when done.
     *
     * <pre><code>
     *  def file = files.createNewBlobFile("text/plain", "hello.txt")
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
     * @throws IOException 
     * @throws LockException 
     * @throws FinalizationException 
     * @throws FileNotFoundException 
     */
    public static AppEngineFile withOutputStream(AppEngineFile file, Map<String, Object> options, Closure<?> closure) throws FileNotFoundException, FinalizationException, LockException, IOException {
        boolean locked = isLocked(options);
        boolean closeFinally = isFinalize(options);

        FileWriteChannel writeChannel = FileServiceFactory.getFileService().openWriteChannel(file, locked);
        OutputStream stream = Channels.newOutputStream(writeChannel);

        IOGroovyMethods.withStream(stream, closure);

        if (closeFinally) {
            writeChannel.closeFinally();
        } else {
            writeChannel.close();
        }

        return file;
    }

    private static boolean isFinalize(Map<String, Object> options) {
        return (boolean) (options.containsKey("finalize") ? options.get("finalize") : true);
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
     * </code></pre>
     *
     * @param file the AppEngineFile to read from
     * @param options an optional map containing two possible keys:
     *      encoding (a String, the encoding to be used for the reader -- UTF8 by default),
     *      locked (a boolean, if you want to acquire a lock on the file -- false by default),
     * @param closure the closure with the reader as parameter
     * @return the original file, for chaining purpose
     * @throws IOException 
     * @throws LockException 
     * @throws FileNotFoundException 
     */
    public static AppEngineFile withReader(AppEngineFile file, Closure<?> closure) throws FileNotFoundException, LockException, IOException {
        return withReader(file, new HashMap<String, Object>(), closure);
    }

    /**
     * Method creating a reader for the AppEngineFile, read textual content from it, and closes it when done.
     *
     * <pre><code>
     *  def file = files.fromPath(someStringPath)
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
     * @throws IOException 
     * @throws LockException 
     * @throws FileNotFoundException 
     */
    public static AppEngineFile withReader(AppEngineFile file, Map<String, Object> options, Closure<?> closure) throws FileNotFoundException, LockException, IOException {
        String encoding = (String) (options.containsKey("encoding") ? options.get("encoding") : "UTF-8");

        FileReadChannel readChannel = FileServiceFactory.getFileService().openReadChannel(file, isLocked(options));
        BufferedReader reader = new BufferedReader(Channels.newReader(readChannel, encoding));

        IOGroovyMethods.withReader(reader, closure);
        readChannel.close();

        return file;
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
     * </code></pre>
     *
     * @param file the AppEngineFile to read from
     * @param closure the closure with the input stream as parameter
     * @return the original file, for chaining purpose
     * @throws IOException 
     */
    public static AppEngineFile withInputStream(AppEngineFile file, Closure<?> closure) throws IOException {
        return withInputStream(file, new HashMap<String, Object>(), closure);
    }

    /**
     * Method creating a buffered input stream for the AppEngineFile, read binary content from it, and closes it when done.
     *
     * <pre><code>
     *  def file = files.fromPath(someStringPath)
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
     * @throws IOException 
     */
    public static AppEngineFile withInputStream(AppEngineFile file, Map<String, Object> options, Closure<?> closure) throws IOException {
        FileReadChannel readChannel = FileServiceFactory.getFileService().openReadChannel(file, isLocked(options));
        BufferedInputStream stream = new BufferedInputStream(Channels.newInputStream(readChannel));

        IOGroovyMethods.withStream(stream, closure);
        readChannel.close();

        return file;
    }

    private static boolean isLocked(Map<String, Object> options) {
        boolean locked = (boolean) (options.containsKey("locked") ? options.get("locked") : true);
        return locked;
    }

    /**
     * Delete an AppEngineFile file from the blobstore.
     *
     * @param file the file to delete
     */
    public static void delete(AppEngineFile file) {
        BlobstoreExtensions.delete(getBlobKey(file));
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
    public static AppEngineFile fromPath(FileService files, String path) {
        return new AppEngineFile(path);
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
    public static BlobKey getBlobKey(AppEngineFile file) {
        return FileServiceFactory.getFileService().getBlobKey(file);
    }

    /**
     * Retrieves the <code>AppEngineFile</code> associated with this <code>BlobKey</code>.
     *
     * @param key the blob key
     * @return the app engine file
     */
    public static AppEngineFile getFile(BlobKey key) {
        return FileServiceFactory.getFileService().getBlobFile(key);
    }
}
