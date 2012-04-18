
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

<p>
You can also run code in separate background thread 
instead of a <code>ThreadManager</code> instance:
</p>

<blockquote>
<b>Warning: </b> If code that's not running in a backend attempts to start a background thread, it raises an exception.
</blockquote>

<pre class="brush:groovy">
    backends.run { /* your background code */ }
</pre>