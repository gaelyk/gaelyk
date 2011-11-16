<a name="urlfetch"></a>
<h2>URLFetch Service improvements</h2>

<p>
Google App Engine offers the URLFetch Service to interact with remote servers,
to post to or to fetch content out of external websites.
Often, using the URL directly with Groovy's <code>getBytes()</code> or <code>getText()</code> methods is enough,
and transparently uses the URLFetch Service under the hood.
But sometimes, you need a bit more control of the requests you're making to remote servers,
for example for setting specific headers, for posting custom payloads, making asynchronous requests, etc.
<b>Gaelyk</b> 0.5 provides a convenient integration of the service with a groovier flavor.
</p>

<blockquote>
<b>Note: </b> You may also want to have a look at HTTPBuilder's
<a href="http://groovy.codehaus.org/modules/http-builder/doc/httpurlclient.html">HttpURLClient</a>
for a richer HTTP client library that is compatible with Google App Engine.
</blockquote>

<p>
<b>Gaelyk</b> decorates the URL class with 5 new methods, for the 5 HTTP methods GET, POST, PUT, DELETE, HEAD
which can take an optional map for customizing the call:
</p>

<ul>
    <li><code>url.get()</code></li>
    <li><code>url.post()</code></li>
    <li><code>url.put()</code></li>
    <li><code>url.delete()</code></li>
    <li><code>url.head()</code></li>
</ul>

<p>
Those methods return an
<code><a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/urlfetch/HTTPResponse.html">HTTPResponse</a></code>
or a <code><a href="http://download.oracle.com/javase/6/docs/api/java/util/concurrent/Future.html">Future&lt;HTTPResponse&gt;</a></code>
if the <code>async</code> option is set to true.
</p>

<p>
Let's start with a simple example, say, you want to get the <b>Gaelyk</b> home page content:
</p>

<pre class="brush:groovy">
    URL url = new URL('http://gaelyk.appspot.com')

    def response = url.get()

    assert response.responseCode == 200
    assert response.text.contains('Gaelyk')
</pre>

<p>
As you can see above,
<b>Gaelyk</b> adds a <code>getText()</code> and <code>getText(String encoding)</code> method to HTTPResponse,
so that it is easier to get textual content from remote servers &mdash;
<code>HTTPResponse</code> only provided a <code>getContent()</code> method that returns a byte array.
</p>

<p>
If you wanted to make an asynchronous call, you could do:
</p>

<pre class="brush:groovy">
    def future = url.get(async: true)
    def response = future.get()
</pre>

<a name="urlfetch-options"></a>
<h3>Allowed options</h3>

<p>
Several options are allowed as arguments of the 5 methods.
</p>

<ul>
    <li>
        <tt>allowTruncate</tt>:
        a boolean (false by default), to explicit if we want an exception to be thrown if the reponse exceeds the 1MB quota limit
    </li>
    <li>
        <tt>followRedirects</tt>:
        a boolean (true by default), to specify if we want to allow the request to follow redirects
    </li>
    <li>
        <tt>deadline</tt>: a double (default to 10), the number of seconds to wait for a request to succeed
    </li>
    <li>
        <tt>headers</tt>: a map of headers
    </li>
    <li>
        <tt>payload</tt>: a byte array for the binary payload you want to post or put
    </li>
    <li>
        <tt>params</tt>: a map of query parameters
    </li>
    <li>
        <tt>async</tt>: a boolean (false by defauly), to specify you want to do an asynchronous call or not
    </li>
</ul>

<p>
To finish on the URLFetch Service support, we can have a look at another example using some of the options above:
</p>

<pre class="brush:groovy">
    URL googleSearch = "http://www.google.com/search".toURL()
    HTTPResponse response = googleSearch.get(params: [q: 'Gaelyk'],
            headers: ['User-Agent': 'Mozilla/5.0 (Linux; X11)'])

    assert response.statusCode == 200
    assert response.text.contains('http://gaelyk.appspot.com')
    assert response.headersMap.'Content-Type' == 'text/html; charset=utf-8'
</pre>

<blockquote>
<b>Note: </b> <code>response.statusCode</code> is a synonym of <code>response.responseCode</code>.
And notice the convenient <code>response.headersMap</code> shortcut which returns a convenient
<code>Map&lt;String, String&gt;</code> of headers instead of SDK's <code>response.headers</code>'s
<code>List&lt;HTTPHeader&gt;</code>.
</blockquote>
