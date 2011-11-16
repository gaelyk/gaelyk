<a name="capabilities"></a>
<h2>Capabilities service support</h2>

<p>
Occasionally, Google App Engine will experience some reliability issues with its various services,
or certain services will be down for scheduled maintenance.
The Google App Engine SDK provides a service, the
<code><a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/capabilities/CapabilitiesService.html">CapabilitiesService</a></code>,
to query the current status of the services.
<b>Gaelyk</b> adds support for this service, by injecting it in the binding of your groovlets and templates,
and by adding some syntax sugar to simplify its use.
</p>

<pre class="brush:groovy">
    import static com.google.appengine.api.capabilities.Capability.*
    import static com.google.appengine.api.capabilities.CapabilityStatus.*

    if (capabilities[DATASTORE] == ENABLED && capabilities[DATASTORE_WRITE] == ENABLED) {
        // write something into the datastore
    } else {
        // redirect the user to a page with a nice maintenance message
    }
</pre>

<blockquote>
<b>Note: </b> Make sure to have a look at the <a href="/tutorial/url-routing#capability-routing">capability-aware URL routing</a> configuration.
</blockquote>

<p>
The services that can be queried are defined as static constants on
<code><a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/capabilities/Capability.html">Capability</a></code>
and currently are:
</p>

<ul>
    <li>BLOBSTORE</li>
    <li>DATASTORE</li>
    <li>DATASTORE_WRITE</li>
    <li>IMAGES</li>
    <li>MAIL</li>
    <li>MEMCACHE</li>
    <li>TASKQUEUE</li>
    <li>URL_FETCH</li>
    <li>XMPP</li>
</ul>

<p>
The different possible statuses are defined in the
<code><a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/capabilities/CapabilityStatus.html">CapabilityStatus</a></code>
enum:
</p>

<ul>
    <li>ENABLED</li>
    <li>DISABLED</li>
    <li>SCHEDULED_MAINTENANCE</li>
    <li>UNKNOWN</li>
</ul>

<blockquote>
<b>Tip: </b> Make sure to static import <code>Capability</code> and <code>CapabilityStatus</code>
in order to keep your code as concise and readable as possible, like in the previous example, with:
<pre class="brush:groovy">
    import static com.google.appengine.api.capabilities.Capability.*
    import static com.google.appengine.api.capabilities.CapabilityStatus.*
</pre>
</blockquote>

<p>
Additionally, instead of comparing explicitely against a specific <code>CapabilityStatus</code>,
<b>Gaelyk</b> provides a coercion of the status to a boolean (also called "Groovy Truth").
This allows you to write simpler conditionals:
</p>

<pre class="brush:groovy">
    import static com.google.appengine.api.capabilities.Capability.*
    import static com.google.appengine.api.capabilities.CapabilityStatus.*

    if (capabilities[DATASTORE] && capabilities[DATASTORE_WRITE]) {
        // write something into the datastore
    } else {
        // redirect the user to a page with a nice maintenance message
    }
</pre>

<blockquote>
<b>Note: </b> Only the <code>ENABLED</code> and <code>SCHEDULED_MAINTENACE</code> statuses are considered to be <code>true</code>,
whereas all the other statuses are considered to be <code>false</code>.
</blockquote>
