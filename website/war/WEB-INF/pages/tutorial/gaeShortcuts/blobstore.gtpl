
<a name="blobstore"></a>
<h2>Enhancements related to the Blobstore and File services</h2>

<p>
<b>Gaelyk</b> provides several enhancements around the usage of the blobstore service.
</p>

<a name="blob-info"></a>
<h3>Getting blob information</h3>

<p>
Given a blob key, you can retrieve various details about the blob when it was uploaded:
</p>

<pre class="brush:groovy">
    BlobKey blob = ...

    // retrieve an instance of BlobInfo
    BlobInfo info = blob.info

    // directly access the BlobInfo details from the key itself
    String filename     = blob.filename
    String contentType  = blob.contentType
    Date creation       = blob.creation
    long size           = blob.size
</pre>

<a name="serving-blob"></a>
<h3>Serving blobs</h3>

<p>
With the blobstore service, you can stream the content of blobs back to the browser, directly on the response object:
</p>

<pre class="brush:groovy">
    BlobKey blob = ...

    // serve the whole blob
    blob.serve response

    // serve a fragment of the blob
    def range = new ByteRange(1000) // starting from 1000
    blob.serve response, range

    // serve a fragment of the blob using an int range
    blob.serve response, 1000..2000
</pre>

<a name="reading-blob"></a>
<h3>Reading the content of a Blob</h3>

<p>
Beyond the ability to serve blobs directly to the response output stream with
<code>blobstoreService.serve(blobKey, response)</code> from your groovlet,
there is the possibility of
<a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/blobstore/BlobstoreInputStream.html">obtaining an <code>InputStream</code></a>
to read the content of the blob.
<b>Gaelyk</b> adds three convenient methods on <code>BlobKey</code>
to easily deal with a raw input stream or with a reader, leveraging Groovy's own input stream and reader methods.
The stream and reader are handled properly with regards to cleanly opening and closing those resources
so that you don't have to take care of that aspect yourself.
</p>

<pre class="brush:groovy">
    BlobKey blobKey = ...

    blobKey.withStream { InputStream stream ->
        // do something with the stream
    }

    // defaults to using UTF-8 as encoding for reading from the underlying stream
    blobKey.withReader { Reader reader ->
        // do something with the reader
    }

    // specifying the encoding of your choice
    blobKey.withReader("UTF-8") { Reader reader ->
        // do something with the reader
    }
</pre>

<p>
You can also fetch byte arrays for a given range:
</p>

<pre class="brush:groovy">
    BlobKey blob = ...
    byte[] bytes

    // using longs
    bytes = blob.fetchData 1000, 2000

    // using a Groovy int range
    bytes = blob.fetchData 1000..2000

    // using a ByteRange
    def range = new ByteRange(1000, 2000) // or 1000..2000 as ByteRange
    bytes = blob.fetchData range
</pre>

<a name="delete-blob"></a>
<h3>Deleting a blob</h3>

<p>Given a blob key, you can easily delete it thanks to the <code>delete()</code> method:</p>

<pre class="brush:groovy">
    BlobKey blob = ...

    blob.delete()
</pre>

<a name="blobstore-each-collect"></a>
<h3>Iterating over and collecting <code>BlobInfo</code>s</h3>

<p>
The blobstore service stores blobs that are identified by <code>BlobKeys</code>,
and whose metadata are represented by <code>BlobInfo</code>.
If you want to iterate over all the blobs from the blobstore,
you can use the <code>BlobInfoFactory</code> and its <code>queryBlobInfos()</code> method,
but <b>Gaelyk</b> simplifies that job with an <code>each{}</code> and a <code>collect{}</code> method
right from the <code>blobstore</code> service:
</p>

<pre class="brush:groovy">
    blobstore.each { BlobInfo info -> out << info.filename }

    def fileNames = blobstore.collect { BlobInfo info -> info.filename }
</pre>

<a name="blobstore-example"></a>
<h3>Example Blobstore service usage</h3>

<p>
In this section, we'll show you a full-blown example.
First of all, let's create a form to submit a file to the blobstore,
in a template named <code>upload.gtpl</code> at the root of your war:
</p>

<pre class="brush:xml">
    &lt;html&gt;
    &lt;body&gt;
        &lt;h1&gt;Please upload a text file&lt;/h1&gt;
        &lt;form action="\${blobstore.createUploadUrl('/uploadBlob.groovy')}"
                method="post" enctype="multipart/form-data"&gt;
            &lt;input type="file" name="myTextFile"&gt;
            &lt;input type="submit" value="Submit"&gt;
        &lt;/form&gt;
    &lt;/body&gt;
    &lt;/html&gt;
</pre>

<p>
The form will be posted to a URL created by the blobstore service,
that will then forward back to the URL you've provided when calling
<code>blobstore.createUploadUrl('/uploadBlob.groovy')</code>
</p>

<blockquote>
<b>Warning: </b> The URL to he groovlet to which the blobstore service will forward the uploaded blob details
should be a direct path to the groovlet like <code>/uploadBlob.groovy</code>.
For an unknown reason, you cannot use a URL defined through the URL routing system.
This is not necessarily critical, in the sense that this URL is never deployed in the browser anyway.
</blockquote>

<p>
Now, create a groovlet named <code>uploadBlob.groovy</code> stored in <code>/WEB-INF/groovy</code>
with the following content:
</p>

<pre class="brush:groovy">
    def blobs = blobstore.getUploadedBlobs(request)
    def blob = blobs["myTextFile"]

    response.status = 302

    if (blob) {
        redirect "/success?key=\${blob.keyString}"
    } else {
        redirect "/failure"
    }
</pre>

<p>
In the groovlet, you retrieve all the blobs uploaded in the <code>upload.gtpl</code> page,
and more particularly, the blob coming from the <code>myTextFile</code> input file element.
</p>

<blockquote>
<b>Warning: </b> Google App Engine mandates that you explicitly specify a redirection status code (301, 302 or 303),
and that you <b>do</b> redirect the user somewhere else, otherwise you'll get some runtime errors.
</blockquote>

<p>
We define some friendly URLs in the URL routing definitions for the upload form template, the success and failure pages:
</p>

<pre class="brush:groovy">
    get "/upload",  forward: "/upload.gtpl"
    get "/success", forward: "/success.gtpl"
    get "/failure", forward: "/failure.gtpl"
</pre>

<p>
You then create a <code>failure.gtpl</code> page at the root of your war directory:
</p>

<pre class="brush:xml">
    &lt;html&gt;
        &lt;body&gt;
            &lt;h1&gt;Failure&lt;/h1&gt;
            &lt;h2&gt;Impossible to store or access the uploaded blob&lt;/h2&gt;
        &lt;/body&gt;
    &lt;/html&gt;
</pre>

<p>
And a <code>success.gtpl</code> page at the root of your war directory,
showing the blob details, and outputing the content of the blob (a text file in our case):
</p>

<pre class="brush:xml">
    &lt;% import com.google.appengine.api.blobstore.BlobKey %&gt;
    &lt;html&gt;
        &lt;body&gt;
            &lt;h1&gt;Success&lt;/h1&gt;
            &lt;% def blob = new BlobKey(params.key) %&gt;

            &lt;div>
                File name: \${blob.filename} &lt;br/&gt;
                Content type: \${blob.contentType}&lt;br/&gt;
                Creation date: \${blob.creation}&lt;br/&gt;
                Size: \${blob.size}
            &lt;/div&gt;

            &lt;h2&gt;Content of the blob&lt;/h2&gt;

            &lt;div&gt;
                &lt;% blob.withReader { out << it.text } %&gt;
            &lt;/div&gt;
        &lt;/body&gt;
    &lt;/html&gt;
</pre>

<p>
    Now that you're all set up, you can access <code>http://localhost:8080/upload</code>,
    submit a text file to upload, and click on the button.
    Google App Engine will store the blob and forward the blob information to your <code>uploadBlob.groovy</code> groovlet
    that will then redirect to the success page (or failure page in case something goes wrong).
</p>

<a name="file-service"></a>
<h3>File service</h3>

<p>
The File service API provides a convenient solution for accessing the blobstore,
and particularly for programmatically adding blobs
without having to go through the blobstore form-based upload facilities.
<b>Gaelyk</b> adds a <code>files</code> variable in the binding of Groovlets and templates,
which corresponds to the <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/files/FileService.html">FileService</a> instance.
</p>

<h4>Writing text content</h4>

<p>
Inspired by Groovy's own <code>withWriter{}</code> method, a new method is available on
<a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/files/AppEngineFile.html">AppEngineFile</a>
that can be used as follows, to write text content through a writer:
</p>

<pre class="brush:groovy">
    // let's first create a new blob file through the regular FileService method
    def file = files.createNewBlobFile("text/plain", "hello.txt")

    file.withWriter { writer ->
        writer << "some content"
    }
</pre>

<p>
You can also specify three options to the <code>withWriter{}</code> method, in the form of named arguments:
</p>

<ul>
    <li><b>encoding</b>: a string ("UTF-8" by default) defining the text encoding</li>
    <li><b>locked</b>: a boolean (true by default) telling if we want an exclusive access to the file</li>
    <li><b>finalize</b>: a boolean (true by default) to indicate if we want to finalize the file to prevent further appending</li>
</ul>

<pre class="brush:groovy">
    file.withWriter(encoding: "US-ASCII", locked: false, finalize: false) { writer ->
        writer << "some content"
    }
</pre>

<h4>Writing binary content</h4>

<p>In a similar fashion, you can write to an output stream your binary content:</p>

<pre class="brush:groovy">
    // let's first create a new blob file through the regular FileService method
    def file = files.createNewBlobFile("text/plain", "hello.txt")

    file.withOutputStream { stream ->
        stream << "Hello World".bytes
    }
</pre>

<p>
You can also specify two options to the <code>withOutputStream{}</code> method, in the form of named arguments:
</p>

<ul>
    <li><b>locked</b>: a boolean (true by default) telling if we want an exclusive access to the file</li>
    <li><b>finalize</b>: a boolean (true by default) to indicate if we want to finalize the file to prevent further appending</li>
</ul>

<pre class="brush:groovy">
    file.withOutputStream(locked: false, finalize: false) { writer ->
        writer << "Hello World".bytes
    }
</pre>

<blockquote>
<b>Note: </b> To finalize a file in the blobstore, App Engine mandates the file needs to be locked.
That's why by default <code>locked</code> and <code>finalize</code> are set to true by default.
When you want to later be able to append again to the file, make sure to set <code>finalize</code> to false.
And if you want to avoid others from concurrently writing to your file, it's better to set <code>locked</code> to false.
</blockquote>

<h4>Reading binary content</h4>

<p>
<b>Gaelyk</b> already provides reading capabilities from the blobstore support, as we've already seen,
but the File service also supports reading from <code>AppEngineFile</code>s.
To read from an <code>AppEngineFile</code> instance, you can use the <code>withInputStream{}</code> method,
which takes an optional map of options, and a closure whose argument is a <code>BufferedInputStream</code>:
</p>

<pre class="brush:groovy">
    file.withInputStream { BufferedInputStream stream ->
        // read from the stream
    }
</pre>

<p>
You can also specify an option for locking the file (the file is locked by default):
</p>

<pre class="brush:groovy">
    file.withInputStream(locked: false) { BufferedInputStream stream ->
        // read from the stream
    }
</pre>

<h4>Reading text content</h4>

<p>
Similarily to reading from an input stream, you can also read from a <code>BufferedReader</code>,
with the <code>withReader{}</code> method:
</p>

<pre class="brush:groovy">
    file.withReader { BufferedReader reader ->
        log.info reader.text
    }
</pre>

<p>
You can also specify an option for locking the file (the file is locked by default):
</p>

<pre class="brush:groovy">
    file.withReader(locked: false) { BufferedReader reader ->
        log.info reader.text
    }
</pre>

<a name="files-misc"></a>
<h4>Miscelanous improvements</h4>

<p>
If you store a file path in the form of a string (for instance for storing its reference in the datastore),
you need to get back an <code>AppEngineFile</code> from its string representation:
</p>

<pre class="brush:groovy">
    def path = someEntity.filePath
    def file = files.fromPath(path)
</pre>

<p>
If you have a <code>BlobKey</code>, you can retrieve the associated <code>AppEngineFile</code>:
</p>

<pre class="brush:groovy">
    def key = ... // some BlobKey
    def file = key.file
</pre>

<p>
You can retrieve the blob key associated with your file (for example when you want to access an <code>Image</code> instance:
</p>

<pre class="brush:groovy">
    def key = file.blobKey
    def image = key.image
</pre>

<p>
And if you want to delete a file without going through the blobstore service, you can do:
</p>

<pre class="brush:groovy">
    file.delete()
</pre>
