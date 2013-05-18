<a name="memcache"></a>
<h2>Enhancements to the Memcache service</h2>

<p>
<b>Gaelyk</b> provides a few additional methods to the 
<a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/memcache/MemcacheService.html">Memcache service</a>, 
to get and put values in the cache using Groovy's natural subscript notation, 
as well as for using the <code>in</code> keyword to check when a key is present in the cache or not.
</p>

<pre class="brush:groovy">
    // under src/main/groovy
    class Country implements Serializable {
        static final long serialVersionUID = 123456L;
        String name 
    }

	// in groovlet
    def countryFr = new Country(name: 'France')

    // use the subscript notation to put a country object in the cache, identified by a string
    // (you can also use non-string keys)
    memcache['FR'] = countryFr

    // check that a key is present in the cache
    if ('FR' in memcache) {
        // use the subscript notation to get an entry from the cache using a key
        def countryFromCache = memcache['FR']
    }
</pre>

<blockquote>
<b>Note: </b> Make sure the objects you put in the cache are serializable.
Also, be careful with the last example above as the <code>'FR'</code> entry in the cache
may have disappeared between the time you do the <code>if (... in ...)</code> check
and the time you actually retrieve the value associated with the key from memcache.
</blockquote>

<a name="async-memcache"></a>
<h3>Asynchronous Memcache service</h3>

<p>
The Memcache service is synchronous, but App Engine also proposes an asynchronous Memcache service
that you can access by calling the <code>async</code> property on the Memcache service instance:
</p>

<pre class="brush:groovy">
    memcache.async.put(key, value)
</pre>

<blockquote>
<b>Note: </b> Additionally, the usual property notation and subscript access notation are also available.
</blockquote>

<a name="memoize"></a>
<h3>Closure memoization</h3>

<p>
As Wikipedia puts it, <a href="http://en.wikipedia.org/wiki/Memoization">memoization</a> is an <i>optimization technique
used primarily to speed up computer programs by having function calls avoid repeating the calculation
of results for previously-processed inputs</i>.
<b>Gaelyk</b> provides such a mechanism for closures, storing invocation information
(a closure call with its arguments values) in memcache.
</p>

<p>
An example, if you want to avoid computing expansive operations (like repeatedly fetching results from the datastore)
in a complex algorithm:
</p>

<pre class="brush:groovy">
    Closure countEntities = memcache.memoize { String kind ->
        datastore.prepare( new Query(kind) ).countEntities()
    }

    // the first time, the expensive datastore operation will be performed and cached
    def totalPics = countEntities('photo')

    /* add new pictures to the datastore */

    // the second invocation, the result of the call will be the same as before, coming from the cache
    def totalPics2 = countEntities('photo')
</pre>

<blockquote>
<b>Note: </b> Invocations are stored in memcache only for up to the 60 seconds request time limit of App Engine.
</blockquote>
