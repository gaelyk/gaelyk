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
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

/**
 * Google Cloud Storage extension methods
 *
 * @author Guillaume Laforge
 * @author Scott Murphy
 */
public class FilesExtensions {
    
    /**
     * Method creating a writer for the GcsFilename, writing textual content to it, and closes it when done.
     *
     * <pre><code>
     *  def file = new GcsFilename("mybucket", "hello.txt") 
     *
     *  // with default options
     *  file.withWriter { writer ->
     *      writer << "some content"
     *  }
     *
     * </code></pre>
     *
     * @param file the GcsFilename to write to
     * @param closure the closure with the writer as parameter
     * @return the original file, for chaining purpose
     * @throws IOException 
     */
    public static GcsFilename withWriter(GcsFilename file, Closure<?> closure) throws IOException {
        return withWriter(file, new HashMap<String, Object>(), closure);
    }
    
    /**
     * Method creating a writer for the GcsFilename, writing textual content to it, and closes it when done.
     *
     * <pre><code>
     *  def file = new GcsFilename("mybucket", "hello.txt") 
     *
     *  // with default options
     *  file.withWriter { writer ->
     *      writer << "some content"
     *  }
     *
     *  // with specific options:
     *  file.withWriter(new GcsFileOptions.Builder().mimeType("text/plain")) { writer ->
     *      writer << "some content
     *  }
     * </code></pre>
     *
     * @param file the GcsFilename to write to
     * @param options an optional map containing three possible keys:
     *      encoding (a String, the encoding to be used for the writer -- UTF8 by default),
     *      options GcsFileOptions,       
     * @param closure the closure with the writer as parameter
     * @return the original file, for chaining purpose
     * @throws IOException 
     */
    public static GcsFilename withWriter(GcsFilename file, Map<String, Object> options, Closure<?> closure) throws IOException {

        String encoding = (String) (options.containsKey("encoding") ? options.get("encoding") : "UTF8");

        GcsOutputChannel writeChannel = GcsServiceFactory.createGcsService().createOrReplace(file, options.containsKey("options") ? (GcsFileOptions) options.get("options") : new GcsFileOptions.Builder().build());
        PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, encoding));

        IOGroovyMethods.withWriter(writer, closure);
        
        writeChannel.waitForOutstandingWrites();
        writeChannel.close();

        return file;
    }
    
    /**
     * Method creating an output stream for the GcsFilename, writing bynary content to it, and closes it when done.
     *
     * <pre><code>
     *  def file = new GcsFilename("mybucket", "hello.txt") 
     *
     *  // with default options
     *  file.withOutputStream { stream ->
     *      stream << "some content".bytes
     *  }
     *  
     * </code></pre>
     *
     * @param file the GcsFilename to write to
     * @param closure the closure with the output stream as parameter
     * @return the original file, for chaining purpose
     * @throws IOException 
     */
    public static GcsFilename withOutputStream(GcsFilename file, Closure<?> closure) throws IOException {
        return withOutputStream(file, new HashMap<String, Object>(), closure);
    }

    /**
     * Method creating an output stream for the GcsFilename, writing bynary content to it, and closes it when done.
     *
     * <pre><code>
     *  def file = new GcsFilename("mybucket", "hello.txt") 
     *
     *  // with specific options:
     *  file.withOutputStream(locked: true, finalize: false) { writer ->
     *      stream << "some content".bytes
     *  }
     * </code></pre>
     *
     * @param file the GcsFilename to write to
     * @param options an optional map containing three possible keys:
     *      options GcsFileOptions,  
     * @param closure the closure with the output stream as parameter
     * @return the original file, for chaining purpose
     * @throws IOException 
     */
    public static GcsFilename withOutputStream(GcsFilename file, Map<String, Object> options, Closure<?> closure) throws IOException {
        GcsOutputChannel writeChannel = GcsServiceFactory.createGcsService().createOrReplace(file, options.containsKey("options") ? (GcsFileOptions) options.get("options") : new GcsFileOptions.Builder().build());
        OutputStream stream = Channels.newOutputStream(writeChannel);

        IOGroovyMethods.withStream(stream, closure);

        writeChannel.waitForOutstandingWrites();
        writeChannel.close();

        return file;
    }

    /**
     * Method creating a reader for the GcsFilename, read textual content from it, and closes it when done.
     *
     * <pre><code>
     *  def file = new GcsFilename("mybucket", someStringPath)
     *
     *  // with default options
     *  file.withReader { reader ->
     *      log.info reader.text
     *  }
     *
     * </code></pre>
     *
     * @param file the GcsFilename to read from
     * @param closure the closure with the reader as parameter
     * @return the original file, for chaining purpose
     * @throws IOException 
     */
    public static GcsFilename withReader(GcsFilename file, Closure<?> closure) throws IOException {
        return withReader(file, new HashMap<String, Object>(), closure);
    }

    /**
     * Method creating a reader for the GcsFilename, read textual content from it, and closes it when done.
     *
     * <pre><code>
     *  def file = new GcsFilename("mybucket", someStringPath)
     *
     *  // with specific options:
     *  file.withReader(encoding: "US-ASCII") { reader ->
     *      log.info reader.text
     *  }
     * </code></pre>
     *
     * @param file the GcsFilename to read from
     * @param options an optional map containing two possible keys:
     *      encoding (a String, the encoding to be used for the reader -- UTF8 by default),
     * @param closure the closure with the reader as parameter
     * @return the original file, for chaining purpose
     * @throws IOException 
     */
    public static GcsFilename withReader(GcsFilename file, Map<String, Object> options, Closure<?> closure) throws IOException {
        String encoding = (String) (options.containsKey("encoding") ? options.get("encoding") : "UTF8");

        GcsInputChannel readChannel = GcsServiceFactory.createGcsService().openReadChannel(file, 0);
        BufferedReader reader = new BufferedReader(Channels.newReader(readChannel, encoding));

        IOGroovyMethods.withReader(reader, closure);
        readChannel.close();

        return file;
    }
    
    /**
     * Method creating a buffered input stream for the GcsFilename, read binary content from it, and closes it when done.
     *
     * <pre><code>
     *  def file = new GcsFilename("mybucket", someStringPath)
     *
     *  // with default options
     *  file.withInputStream { stream ->
     *      // read from the buffered input stream
     *  }
     *
     * </code></pre>
     *
     * @param file the GcsFilename to read from
     * @param closure the closure with the input stream as parameter
     * @return the original file, for chaining purpose
     * @throws IOException 
     */
    public static GcsFilename withInputStream(GcsFilename file, Closure<?> closure) throws IOException {
        GcsInputChannel readChannel = GcsServiceFactory.createGcsService().openReadChannel(file, 0);
        BufferedInputStream stream = new BufferedInputStream(Channels.newInputStream(readChannel));

        IOGroovyMethods.withStream(stream, closure);
        readChannel.close();

        return file;
    }

    /**
     * Delete an GcsFilename file from the blobstore.
     *
     * @param file the file to delete
     * @throws IOException         
     */
    public static void delete(GcsFilename file) throws IOException {
        GcsServiceFactory.createGcsService().delete(file);
    }

    /**
     * Retrieves the blob key associated with an App Engine file.
     * <pre><code>
     *  def file = new GcsFilename("mybucket", someStringPath)
     *  def key = file.blobKey
     * </code></pre>
     *
     * @param file the file to get the blob key of
     * @return the blob key associated with the GcsFilename
     */
    public static BlobKey getBlobKey(GcsFilename file) {
        return BlobstoreServiceFactory.getBlobstoreService().createGsBlobKey("/gs/"+file.getBucketName()+"/"+file.getObjectName());
    }

}
