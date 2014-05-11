<html>
<head>
    <title>Download and release notes</title>
</head>
<body>

<h1>Download</h1>

<div id="sidebox" class="roundPinkBorder">
    <table>
        <tr>
            <td><a href="http://dl.bintray.com/gaelyk/gaelyk-templates/gaelyk-template-project-2.1.2.zip"><img src="/images/icon-download.png" alt="Template project"></a></td>
            <td><a href="http://dl.bintray.com/gaelyk/gaelyk-templates/gaelyk-template-project-2.1.2.zip">Template project v2.1.2</a></td>
        </tr>
        <tr>
            <td><a href="http://repo1.maven.org/maven2/org/gaelyk/gaelyk/2.1.2/gaelyk-2.1.2.jar"><img src="/images/icon-download.png" alt="Gaelyk JAR"></a></td>
            <td><a href="http://repo1.maven.org/maven2/org/gaelyk/gaelyk/2.1.2/gaelyk-2.1.2.jar">Gaelyk JAR v2.1.2</a></td>
        </tr>
    </table>
</div>

<p>
You can use the <b>Gaelyk</b> JAR in combination with Groovy 2.3.x and beyond and with the latest Google App Engine SDK.
</p>

<p>
The latest version was tested with Groovy 2.3.0,
and with the 1.9.4 version of the App Engine SDK.
</p>

<p>
But to get you started quickly, you may use a ready-made template project which bundles and configures everything.
</p>

<h2>Version 2.1.2</h2>
<h3>Changes</h3>
<ul>
    <li>Switched to Groovy 2.3</li>
    <li>Fixed null pointer exception for self restarting iterator in conjunction with <code>or</code> in the query</li>
</ul>

<h2>Version 2.1.1</h2>
<h3>Changes</h3>
<ul>
<li>Showing 404 status page properly when groovlet or template not found</li>
</ul>

<h2>Version 2.1</h2>

<h3>Changes</h3>
<ul>
    <li>Gradle plugin switched to official <a href="https://github.com/GoogleCloudPlatform/gradle-appengine-plugin">Gradle AppEngine Plugin</a></li>
    <li>Migrated to GAE SDK 1.9.1</li>
    <li>Static and read-only properties are ignored during datastore coercion</li>
    <li>Enum constants are converted to Strings when coercing to <code>Entity</code></li>
    <li>Added <code>deleteServingUrl</code> to <code>BlobKey</code> to delete previously created serving URL</li>
    <li>Result list now contains original query for debug purposes</li>
    <li>Errors are better reported during coercion</li>
    <li><code>@Entity</code> annotation supports inheritance</li>
    <li>Ability to customize coercion by implementing <code>DatastoreEntity</code> interface</li>
    <li>Added <code>paginate</code> keyword to query DSL for simplified pagination out of box</li>
    <li>Filtering stacktraces for clearer logs</li>

</ul>

<h2>Version 2.0</h2>

<h3>Changes</h3>
<ul>
    <li>Migration to GAE SDK 1.8.0 and Groovy 2.1.3</li>
    <li>
        Several key classes of the Gaelyk code base are now statically compiled thanks to Groovy 2,
        which should bring some performance improvements.
        But a major change is the usage of Groovy 2.0's extension module mechanism
        for all the added nice shortcuts decorating the GAE SDK.
        This also means Groovy categories have been abandoned in favor of that mechanism,
        as they were disabling certain optimizations of Groovy (such as efficient primitive arithmetic).
    </li>
    <li>
    	New <a href="/tutorial/app-engine-shortcuts#advanced_fulltext_search">search DSL</a> (<a href="https://github.com/gaelyk/gaelyk/issues/183">#183</a>)
    </li>
	<li>
		Optional <a href="/tutorial/url-routing#path-variables">route variables</a> (<a href="https://github.com/gaelyk/gaelyk/issues/185">#185</a>)
	</li>
	<li>
		<a href="/tutorial/url-routing#index">Routes indexes</a> (<a href="https://github.com/gaelyk/gaelyk/issues/191">#191</a>)
	</li>
	<li>
		Making <a href="/tutorial/app-engine-shortcuts#restartingAsync">asynchronous search recoverable</a> (<a href="https://github.com/gaelyk/gaelyk/issues/196">#196</a>)
	</li>
	<li>
		Returning <a href="https://developers.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/QueryResultList">QueryResultList</a> and <a href="https://developers.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/QueryResultIterator">QueryResultIterator</a>
		from <code>datastore.execute {}</code> and <code>datastore.iterate {}</code> methods (<a href="/tutorial/app-engine-shortcuts#queryList">see more</a>)
	</li>
	<li>
		Using @DelegatesTo where possible for better IDE support (ie. auto-completion)
	</li>
    <li>
		Removed duplicate logging
	</li>
	<li>
		Ability to <a href="/tutorial/app-engine-shortcuts#executing-datastore-queries"> restart long running queries automatically</a>
	</li>
	<li>
		<code>datastore.build {...}</code> <a href="/tutorial/app-engine-shortcuts#query-dsl-syntax"></a>method</a> to create instance of <code>QueryBuilder</code> for later use
	</li>
	<li>
		Support for <a href="/tutorial/app-engine-shortcuts#search-put-to-index">geo-points in search documents</a>
	</li>
	<li>
		<a href="/tutorial/views-and-controllers#better-params-handling">Better parameters conversion</a>(handling multiple parameters gracefully)
	</li>
	<li>
		<code>@Parent</code> <a href="/tutorial/app-engine-shortcuts#pogo-entity-coercion-annotations">annotation</a> for <code>@Entity</code> classes 
	</li>
	<li>
		Better performance for <code>@Entity</code> classes coercion (<a href="https://github.com/gaelyk/gaelyk/issues/96">#96</a>)
	</li>
	<li>
		<code>DatastoreEntity</code> interface for speeding up the coercion manually (<a href="https://github.com/gaelyk/gaelyk/issues/96">#96</a>)
	</li>
	<li>
		Gracefull unindexed property (<a href="https://github.com/gaelyk/gaelyk/issues/90">#90</a>)
	</li>
	<li>
		Entity to Map coercion
	</li>
	<li>
		Ignoring static properties in coercion (<a href="https://github.com/gaelyk/gaelyk/issues/158">#158</a>)
	</li>
	<li>
		<code>put()</code> and <code>putAsync()</code> <a href="/tutorial/app-engine-shortcuts#search-put-to-index">methods for search index</a>
	</li>
	<li>
		Non-existing property returns <code>null</code> for search documents
	</li>
	<li>
		Route patterns optimizations (<a href="https://github.com/gaelyk/gaelyk/issues/182">#182</a>)
	</li>
	<li>
		Multi-value properties for search documents (<a href="https://github.com/gaelyk/gaelyk/issues/178">#178</a>)
	</li>
	<li>
		GAE services exposed in <a href="/tutorial/url-routing#path-variable-validation">routes validation closure</a>
	</li>
	<li>
		Servlet context is <a href="/tutorial/plugins#context">available for plugin descriptors</a>
	</li>
	<li>
		Groovlet result can be handled by <code>after</code> closure of <a href="/tutorial/plugins#before-and-after">the plugin</a>
	</li>
	<li>
		Parameter to allow cross-group transactions <a href="https://github.com/gaelyk/gaelyk/pull/154">#154</a>
	</li>
</ul>

<h4>Breaking changes</h4>
	
<ul>
    <li>
        The usage and support of Groovy categories for enriching the SDK or any third-party library has been removed.
        In particular, your plugins descriptor using the <code>category</code> section might be affected.
        Instead of using categories which disable some performance optimizations of Groovy,
        you should be using Groovy 2's <a href="http://docs.codehaus.org/display/GROOVY/Creating+an+extension+module">extension modules</a> approach.
    </li>
    <li>
        For the lifecycle manager, the shutdown hook is now a method call with:
        <code>lifecycle.shutdownHook { ... }</code> instead of setting a property:
        <code>lifecycle.shutdownHook = { ... }</code>.
    </li>
    <li>
        On XMPP messages, you must now call the <code>message.xml()</code> method to get the parsed XML payload
        instead of calling the <code>message.xml</code> property.
    </li>
    <li>
    	Adding task to the queue using the left shift operator <code>&lt;&lt;</code> now makes asynchronous call instead of synchronous
    	returning <code>Future&lt;TaskHandle&gt;</code>
    </li>
	<li>
		<code>@Entity</code> classes no longer has version property automatically. You can add it manually using
		<code>@Version</code> annotation on <code>Long</code> property, but don't do it unless you have a real reason
		for it because obtaining the version property tooks very long time.
	</li>
	<li>
		Single star in routes will match only to the closest slash.
	</li>
</ul>

<blockquote>
	<p>As a result of using Groovy 2.0 you will also need to update <code>buildscript{ dependencies {...} }</code> configuration
	closure in your <code>build.gradle</code> 
	file with following	snippet:
	</p>
	<pre class="brush:groovy">
    dependencies {
        classpath 'org.gradle.api.plugins:gradle-gaelyk-plugin:0.4.1'
        classpath 'org.gradle.api.plugins:gradle-gae-plugin:0.8', {
            exclude module: "gradle-fatjar-plugin"
        }
        classpath 'eu.appsatori:gradle-fatjar-plugin:0.2-rc1'
        classpath 'org.gradle.api.plugins:gradle-gae-geb-plugin:0.3'
    }
	</pre>
	<p>
	If you don't do this, the extension modules won't work and you get strange errors in production such as there's no method <code>hours</code>
	for the class <code>Integer</code> or you won't be able to access <code>request</code> attributes using simplified <code>request.attr</code> notation.
	</p>
</blockquote>

<h2>Version 1.2</h2>

<h3>Changes</h3>
<ul>
    <li>Upgraded to Groovy 1.8.6 and App Engine SDK 1.6.6</li>
    <li>
        <b>Gaelyk</b> will be available on Maven Central from now on,
        in the <code>org.gaelyk</code> group with the <code>gaelyk</code> artifact
    </li>
    <li>
        The Gradle Gaelyk plugin used by the template project transparently
        <a href="https://github.com/bmuschko/gradle-gaelyk-plugin#tasks">precompiles your Groovlets and Templates</a>
        for faster startup times
    </li>
    <li>Changed Maven/Gradle group id from <code>groovyx.gaelyk</code> to <code>org.gaelyk</code></li>
    <li>Introduction of <a href="/tutorial/plugins#binaryplugins">binary plugins</a></li>
    <li>You can use <code>redirect301</code> for permanent redirects in routes</li>
    <li>New <code>withTransaction{}</code> and <code>withTransactionAsyncCommit{}</code> methods for the async datastore</li>
    <li>Memcache get()/put() failures are silently ignored to avoid painful exceptions when Memcache service is misbehaving</li>
    <li>Improve POGO coercion's performance</li>
    <li>
        Generated CRUD operations for POGO annotated with
        <a href="/tutorial/app-engine-shortcuts#pogo-entity-coercion-annotations"><code>@Entity</code></a>
    </li>
    <li>Allow using enum values as POGO properties for coercion</li>
    <li>
        Added the <a href="/tutorial/app-engine-shortcuts#search">search service</a> to the binding and bring additional DSLs for index handling and querying
    </li>
    <li>New <a href="/tutorial/views-and-controllers#lazy">geo variables</a> in the binding</li>
    <li>
        The <a href="/tutorial/template-project">template project</a> now uses
        <a href="http://twitter.github.com/bootstrap/">Twitter Bootstrap</a> for its look'n feel
    </li>
</ul>

<h4>Breaking changes</h4>

<ul>
    <li>
        When using <code>select keys</code> with the Query DSL, instances of <code>Key</code> are returned
        instead of <code>Entity</code>s with only a key field.
        Hence, you don't need anymore to call <code>.key</code> on the results of the query.
    </li>
    <li>
        For those using dependency tools like Maven for building their projects,
        please remember to change the groupId for the Gaelyk dependency (group: org.gaelyk / module: gaelyk)
    </li>
</ul>

<h3>Artifacts</h3>
<ul>
    <li>Gaelyk JAR: <a href="https://github.com/downloads/gaelyk/gaelyk/gaelyk-1.2.jar">gaelyk-1.2.jar</a></li>
    <li>Gaelyk template project: <a href="https://github.com/downloads/gaelyk/gaelyk/gaelyk-template-project-1.2.zip">gaelyk-template-project-1.2.zip</a></li>
</ul>

<h2>Version 1.1</h2>

<h3>Changes</h3>
<ul>
    <li>Upgraded to Groovy 1.8.4 and App Engine SDK 1.6.0</li>
    <li>
        The <a href="/tutorial/app-engine-shortcuts#datastore-get">new <code>get()</code> methods on the datastore service</a>
        now also work with the asynchronous datastore.
    </li>
    <li>Added an <code>unindexed</code> property on entities to set unindexed properties: <code>person.unindexed.bio = "..."</code></li>
    <li>
        <a href="/tutorial/app-engine-shortcuts#pogo-entity-coercion-annotations">Three annotations to customize the bean / entity coercion</a>
        (<code>@Key</code>, <code>@Unindexed</code> and <code>@Ignore</code>)
    </li>
    <li>
        Part of the work with the async datastore <code>get()</code>,
        whenever you have a <code>Future</code> to deal with,
        for example when the async datastore returns a <code>Future&lt;Entity&gt;</code>,
        you can call any property on the <code>Future</code> object,
        and it will proxy those property access to the underlying object
        returned by the <code>get()</code> call on the future.
    </li>
    <li>
        In addition to <code>datastore.query{}</code> and <code>datastore.execute{}</code>,
        there is now a <code>datastore.iterate{}</code> method that returns an <code>Iterator</code> instead of a list,
        which is friendlier when your queries return a large number of results.
    </li>
    <li>Added the prospective search service to the binding</li>
    <li>You can access the <a href="/tutorial/app-engine-shortcuts#async-memcache">asynchronous Memcache</a> service with <code>memcache.async</code></li>
    <li>
        Additional <a href="/tutorial/app-engine-shortcuts#files-misc">convenience methods for the file service</a>
    </li>
    <li>
        Added an <a href="/tutorial/app-engine-shortcuts#blobstore-each-collect">each and collect method on blobstore</a>
        to iterate over all the blobs from the blobstore, or to collect some values from all blob infos stored.
    </li>
</ul>

<h3>Artifacts</h3>
<ul>
    <li>Gaelyk JAR: <a href="https://github.com/downloads/gaelyk/gaelyk/gaelyk-1.1.jar">gaelyk-1.1.jar</a></li>
    <li>Gaelyk template project: <a href="https://github.com/downloads/gaelyk/gaelyk/gaelyk-template-project-1.1.zip">gaelyk-template-project-1.1.zip</a></li>
</ul>

<ul>
</ul>


<h2>Version 1.0</h2>

<h3>Changes</h3>
<ul>
    <li>GAE SDK updated to 1.5.2</li>
    <li>
        Introduction of a <a href="/tutorial/app-engine-shortcuts#query">Query DSL
        for creating SQL-like queries against the datastore</a>
    </li>
    <li>
        Updated <a href="/tutorial/template-project">template project</a> with a Gradle build,
        the usage of Gradle GAE / Gaelyk plugins, and the support of Spock for testing Groovlets
    </li>
    <li>Introduction of the <a href="/plugins">plugins page</a> in the Gaelyk website, for referencing known plugins</li>
    <li>
        By annotating classes with <code>GaelykBindings</code>, the same
        <a href="/tutorial/views-and-controllers#gaelykBindings">services and variables are injected in your classes as properties</a>,
        as the ones which are injected in Groovlets and templates
    </li>
    <li>
        The <a href="/tutorial/url-routing#path-variable-validation">validation closures</a> of the routes
        in your URL mapping have access to the request,
        so you can validate a URL depending on what's in your request (attribute, session, etc.)
    </li>
    <li>Added a DSLD file (DSL descriptor) for Eclipse for easing code-completion and navigation</li>
    <li>
        Added a <a href="/tutorial/app-engine-shortcuts#delete-get-on-key"><code>get()</code> method on <code>Key</code></a>,
        as well as on lists of keys
    </li>
    <li>Ability to convert <a href="/tutorial/app-engine-shortcuts#list-to-key-coercion">lists to <code>Key</code>s</a></li>
    <li>Added two encoded <a href="/tutorial/app-engine-shortcuts#key-string-conversion">string and key conversion</a> utilities</li>
    <li><a href="/tutorial/app-engine-shortcuts#datastore-get">Additional <code>datastore.get()</code> methods</a> to retrieve entities by their keys more concisely</li>
    <li>Problems with the recent XMPP support fixed</li>
    <li>
        Fixed inability to access the various services and variables
        from within binding/before/after blocks in plugin descriptors
    </li>
</ul>

<h3>Artifacts</h3>
<ul>
    <li>Gaelyk JAR: <a href="https://github.com/downloads/gaelyk/gaelyk/gaelyk-1.0.jar">gaelyk-1.0.jar</a></li>
    <li>Gaelyk template project: <a href="https://github.com/downloads/gaelyk/gaelyk/gaelyk-template-project-1.0.zip">gaelyk-template-project-1.0.zip</a></li>
</ul>

<h4>Breaking changes</h4>

<ul>
    <li>
        When storing or retrieving a String value into/from an entity attribute,
        Gaelyk now takes care transparently of dealing with <code>String</code> vs <code>Text</code>:
        whether the string you want to access or update is longer than 500 characters or not,
        Gaelyk will treat that as a mere Java string,
        so you won't have to deal with <code>Text</code> anymore.
        More concretely, you don't need to do
        <code>entity.content = new Text("...")</code> for storing a string longer than 500 characters, nor
        <code>entity.content.value</code> to access a string property longer than 500 characters.
        Now, in all situations, for string properties, independently of their size,
        just do <code>entity.content</code> for accessing and setting the value of that property.
    </li>
    <li>
        Inside a plugin's <code>binding {}</code> block, if you need to access variables like the datastore, memcache,
        or any usual such variable that is available in your groovlets and templates, you must prefix those variables with this.
        Example: <code>binding { cachedContent = this.memcache['myKey'] }</code>
    </li>
</ul>


<h2>Version 0.7</h2>

<h3>Changes</h3>
<ul>
    <li>Groovy upgraded to 1.8.0 and GAE SDK to 1.5.0</li>
    <li>
        Added support for XMPP's
        <a href="/tutorial/app-engine-shortcuts#jabber-presence">presence</a> and
        <a href="/tutorial/app-engine-shortcuts#jabber-subscription">subscription</a>
    </li>
    <li>Added variable <code>files</code> for easy access to the File service in your Groovlet and templates</li>
    <li>Added <a href="/tutorial/app-engine-shortcuts#file-service">support for the File service</a></li>
    <li>Thanks to the new file service, the Gaelyk test suites now properly cover the blobstore related enhancements</li>
    <li>Added <a href="/tutorial/app-engine-shortcuts#metadata">datastore metadata querying</a> support</li>
    <li>
        Added minimal <a href="/tutorial/app-engine-shortcuts#backend">backend service support</a>
        and injected the <code>lifecycle</code> manager for the backend instance lifecycle handling
    </li>
    <li>
        Use the concurrent request flag in <code>appengine-web.xml</code> to allow concurrent requests
        to hit your application without needing to have additional instances launched automatically
        by Google App Engine
    </li>
</ul>

<h3>Artifacts</h3>
<ul>
    <li>Gaelyk JAR: <a href="https://github.com/downloads/gaelyk/gaelyk/gaelyk-0.7.jar">gaelyk-0.7.jar</a></li>
    <li>Gaelyk template project: <a href="https://github.com/downloads/gaelyk/gaelyk/gaelyk-template-project-0.7.zip">gaelyk-template-project-0.7.zip</a></li>
</ul>

<h2>Version 0.6.1</h2>

<h3>Changes</h3>
<ul>
    <li>
        Fixed plugins reloading issue in development mode where plugins were reloaded and ran upon each request.
        No reloading happen at all, and plugins are parsed and executed only on startup of the application.
    </li>
    <li>Statement code coverage increased to 91%</li>
</ul>

<h2>Version 0.6</h2>

<h3>Changes</h3>
<ul>
    <li>Updated to GAE SDK 1.4.0 and Groovy 1.7.6</li>
    <li>
        Channel service added in the binding and added a convenient method for
        <a href="/tutorial/app-engine-shortcuts#channel">sending messages</a>
    </li>
    <li>
        Ability to specify the "<a href="/tutorial/url-routing#warmup">warmup request</a>"
        handler through a route definition</li>
    <li>Added <code>app.gaelyk.version</code> in the binding</li>
    <li>Use a servlet context listener for initializing the plugin system</li>
    <li>Initial support for the <a href="/tutorial/app-engine-shortcuts#async-datastore">asynchronous datastore</a></li>
    <li>Updated the task queue enhancements to use the new package (as task queues migrated from labs)</li>
    <li>Introduced a <a href="http://www.gradle.org">Gradle</a> build script for building Gaelyk itself</li>
    <li>Increased the code coverage of the project to over 82%</li>
    <li>
        Added <code>before{}</code> request and <code>after{}</code>
        <a href="/tutorial/plugins#using">request lifecycle hooks to plugins</a>
    </li>
    <li>Added initial Eclipse project files in the template project</li>
    <li>Fixed a bug with <code>ignore</code> URL routes which triggered NPEs after the capabilities routing was added</li>
    <li>Corrected typos in the tutorials</li>
</ul>

<h4>Breaking changes</h4>

<ul>
    <li>
        Compared to the previous version of the toolkit, the handling of incoming emails and incoming jabber messages has changed.
        The <code>GaelykIncomingEmailServlet</code> and <code>GaelykXmppServlet</code> are gone.
        It is no longer required to have dedicated servlets for those two purposes,
        instead you must use the URL routing system to indicate the handlers that will take care of the incoming messages.
        If you were relying on those two servlets, please make sure to upgrade,
        and read the updated tutorial on
        <a href="/tutorial/url-routing#email-and-jabber">URL routing</a> and
        <a href="/tutorial/app-engine-shortcuts#incoming-mail">incoming email</a> and
        <a href="/tutorial/app-engine-shortcuts#jabber-receiving">jabber messages</a>.
    </li>
    <li>
        The initialization of the plugin system is not done anymore by the Groovlet and template servlet,
        but is done by a servlet context listener.
        So you'll have to update your <code>web.xml</code> file to specify that listener.
        Please have a look at the template project or the documentation on how to
        <a href="/tutorial/setup#web-xml">setup the new context listener</a>.
    </li>
</ul>

<h2>Version 0.5.6</h2>

<h3>Changes</h3>
<ul>
    <li>Upgraded to GAE SDK 1.3.8</li>
    <li>Fixed a bug when using <code>memcache.clearCacheForUri()</code> which didn't clear all the cache entries</li>
    <li>Added a method <code>Map#toQueryString()</code> to simplify the creation of a query string when you have a map on hands</li>
    <li>
        Additonal checks when using Memcache's <code>get()</code> and <code>put()</code> methods when using GString keys
        (now automatically coerced to normal Strings)
    </li>
    <li>Fixed a small bug with the type coercion of Strings into built-in Datastore types</li>
</ul>

<h2>Version 0.5.5</h2>

<h3>Changes</h3>
<ul>
    <li>Added a <a href="/tutorial/url-routing#capability-routing">capability-aware URL routing</a> mechanism</li>
    <li>Added a <a href="/tutorial/url-routing#namespace-scoped">namespace-aware URL routing</a> mechanism</li>
    <li>Completely reorganized the documentation, particularly splitting the content over several pages and providing a table of content.</li>
</ul>

<h2>Version 0.5</h2>

<h3>Changes</h3>
<ul>
    <li>Fixed the problem of badly placed includes when cache was enabled</li>
    <li>Added an <code>ignore: true</code> parameter in route definitions if you want to quickly skip certain patterns</li>
    <li>The GDSL file for IntelliJ IDEA has been updated so that you have code-completion in your groovlets and templates</li>
    <li>Many <a href="/tutorial/app-engine-shortcuts#images">enhancements around the images service</a>
        <ul>
            <li>Provide a convenient wrapper class for the <code>ImagesService</code> and <code>ImagesServiceFactory</code></li>
            <li>New methods dedicated to the handling of images</li>
            <li>A DSL for manipulating and transforming images</li>
        </ul>
    </li>
    <li>Possibility to <a href="/tutorial/url-routing#cacheclear">clear the cache for a given URI</a></li>
    <li>Closure <a href="/tutorial/app-engine-shortcuts#memoize">memoization</a> through memcache</li>
    <li><a href="/tutorial/app-engine-shortcuts#capabilities">Capabilities service additions</a> to query the status of the App Engine services</li>
    <li><a href="/tutorial/app-engine-shortcuts#urlfetch">URLFetch service enhancements</a></li>
    <li>Upgrade of the website and template project to Groovy 1.7.5</li>
</ul>

<h2>Version 0.4.4</h2>

<h3>Changes</h3>
<ul>
    <li>Updated to GAE SDK 1.3.7</li>
    <li>
        Jabber and incoming email groovlets now have their implicit logger
        (<code>gaelyk.email</code> and <code>gaelyk.jabber</code>)
    </li>
    <li>Plugins are now impacting Jabber and incoming email groovlets as well</li>
    <li>Fixed a bug the conversion of String to Datastore's Category type</li>
    <li>Internal refactorings of the caching logic</li>
    <li>
        Added <a href="/tutorial/app-engine-shortcuts#namespace">namespace support</a>:
        <ul>
            <li><code>namespace</code> added in the binding, pointing at <code>NamespaceManager</code></li>
            <li>new method namespace.of("customerA") { ... } to execute a closure in the context of a specific namespace</li>
        </ul>
    </li>
</ul>

<h2>Version 0.4.3</h2>

<h3>Changes</h3>
<ul>
    <li>
        Improvements in the logging infrastructure for Groovlets and Templates so they follow a standard hierarchy approach
        (make sure to read the updated tutorial section on this topic)
    </li>
    <li>A new <code>delete()</code> method was added on Datastore's <code>Key</code></li>
    <li>Simple Entity and POJO/POGO mapping through type coercion</li>
    <li>Added the OAuth service in the binding</li>
    <li>Updated Groovy to the latest 1.7.4 version</li>
</ul>

<h2>Version 0.4.2</h2>

<h3>Changes</h3>
<ul>
    <li>Bug fixes regarding the encoding issue with the caching system</li>
    <li>New blobstore service related improvements</li>
</ul>

<h2>Version 0.4.1</h2>

<h3>Changes</h3>
<ul>
    <li>Updated Gaelyk and the template project to using GAE SDK 1.3.5 and Groovy 1.7.3</li>
    <li>
        Give access to new variables in the binding, such as
        <code>app.id</code>, <code>app.version</code>, <code>app.env.name</code>, and <code>app.env.version</code>
    </li>
    <li>
        Added a <code>log</code> variable in the binding, so that you can easily log from groovlets and templates
    </li>
    <li>Changed the <code>localMode</code> binding variable implementation to use the new underlying environment information</li>
    <li>New methods on BlobKey to allow easy reading of the resource with an input stream or a reader</li>
    <li>Memcache caching support for groovlet and template output, when specifying a cache duration in the URL routes</li>
    <li>A few minor bug fixes</li>
</ul>

<h2>Version 0.4</h2>

<h3>Changes</h3>
<ul>
    <li>Created a <a href="/tutorial/plugins">simple plugin system</a> to futher modularize your applications and share commonalities between projects</li>
</ul>

<h2>Version 0.3.3</h2>

<h3>Changes</h3>
<ul>
    <li>Upgraded to the latest versions of Groovy 1.7.2 and the GAE SDK 1.3.3.1</li>
    <li>Fixed a long standing bug where we could not use <code>from</code> as named argument for <code>mail.send()</code></li>
    <li>Small internal refactoring on how the binding variables are injected</li>
    <li>Started adding a few more tests for improving the coverage of the project</li>
    <li>Enhance the binding of the routes script, so you can have programmatic routes depending on GAE services (ie. one could store routes in the datastore)</li>
</ul>

<h2>Version 0.3.2</h2>

<h3>Changes</h3>
<ul>
    <li>Memcache <code>getAt</code> syntax was not working for String keys.</li>
    <li>Remove the "generated by" messages in the templates and groovlets.</li>
    <li>
        Added a <code>localMode</code> binding variable indicating whether the application is running locally
        or deployed on Google App Engine's cloud.
    </li>
    <li>Added a servlet filter URL routing system for friendlier and RESTful URLs</li>
    <li>Upgraded the template project to the newly released Groovy 1.7-RC-1</li>
</ul>

<h2>Version 0.3.1</h2>

<h3>Changes</h3>
<ul>
    <li>Fixed issue with using the same xmpp script name as the service variable in the binding</li>
</ul>

<h2>Version 0.3</h2>

<h3>Changes</h3>
<ul>
    <li>Removing most of the <code>service</code> suffices in the binding variables for brevity and readibility.</li>
    <li>New methods for working with the memcache service (subscript notation, and <code>in</code> keyword support).</li>
    <li>Support for incoming email support</li>
    <li>Fixed issue with sending of emails</li>
</ul>

<h2>Version 0.2</h2>

<h3>Changes</h3>
<ul>
    <li>
        The <b>Gaelyk</b> classes have been moved to the <code>groovyx.gaelyk</code> package,
        instead of <code>groovyx.gaelyk.servlet</code>.
    </li>
    <li>
        Additional <em>groovyfication</em> and support for task queues and Jabber/XMPP.
    </li>
    <li>
        Upgrade to the latest 1.2.5 version of the Google App Engine SDK,
        as well as using the the <em>labs</em> JAR with the task queue support.
    </li>
    <li>
        JavaDoc for the <b>Gaelyk</b> sources added and linked from the tutorial documentation.
    </li>
</ul>

<h2>Version 0.1</h2>

<h3>Initial version</h3>

</body>
</html>
