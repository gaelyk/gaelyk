
<a name="namespace"></a>
<h2>Namespace support</h2>

<p>
Google App Engine SDK allows you to create
"<a href="http://code.google.com/appengine/docs/java/multitenancy/multitenancy.html">multitenant</a>"-aware
applications, through the concept of namespace, that you can handle through the
<a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/NamespaceManager.html">NamespaceManager</a> class.
</p>

<p>
<b>Gaelyk</b> adds the variable <code>namespace</code> into the binding of your groovlets and templates.
This <code>namespace</code> variable is simply the <code>NamespaceManager</code> class.
<b>Gaelyk</b> adds a handy method for automating the pattern of setting a temporary namespace and restoring it to its previous value,
thanks to the added <code>of()</code> method, taking a namespace name in the form of a string,
and a closure to be executed when that namespace is active.
This method can be used as follows:
</p>

<pre class="brush:groovy">
    // temporarily set a new namespace
    namespace.of("customerA") {
        // use whatever service leveraging the namespace support
        // like the datastore or memcache
    }
    // once the closure is executed, the old namespace is restored
</pre>
