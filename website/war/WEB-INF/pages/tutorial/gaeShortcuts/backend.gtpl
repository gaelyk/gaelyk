
<a name="backend"></a>
<h2>Backend service support</h2>

<p>
The backend service support is quite minimal, from a <b>Gaelyk</b> perspective,
as only a <code>backends</code> (corresponding to a <code>BackendService</code> instance)
and <code>lifecylce</code> (the <code>LifecycleManager</code>) variables
have been added to the binding of Groovlets and templates.
</p>

<p>
In addition, a method for shutdown hooks was added that allows you to use a closure
instead of a <code>ShutdownHook</code> instance:
</p>

<pre class="brush:groovy">
    lifecycle.shutdownHook = { /* shutting down logic */ }
</pre>