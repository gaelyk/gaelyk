
<a name="search"></a>
<h2>Search service support</h2>

<p>
    The full-text search functionality can be accessed with the <code>search</code> variable in the bindings
    of Groovlets and templates. You can also specify a special namespace to restrict the searches to that namespace.
</p>

<pre class="brush:groovy">
    // access the search service
    search

    // access the search service for a specific namespace
    search['myNamespace']
</pre>
