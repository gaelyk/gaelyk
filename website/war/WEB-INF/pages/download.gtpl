<html>
<head>
    <title>Download and release notes</title>
</head>
<body>

<h1>Download</h1>

<div id="sidebox" class="roundPinkBorder">
    <table>
        <tr>
            <td><a href="https://github.com/downloads/glaforge/gaelyk/gaelyk-template-project-1.0.zip"><img src="/images/icon-download.png" alt="Template project"></a></td>
            <td><a href="https://github.com/downloads/glaforge/gaelyk/gaelyk-template-project-1.0.zip">Template project v1.0</a></td>
        </tr>
        <tr>
            <td><a href="https://github.com/downloads/glaforge/gaelyk/gaelyk-1.0.jar"><img src="/images/icon-download.png" alt="Gaelyk JAR"></a></td>
            <td><a href="https://github.com/downloads/glaforge/gaelyk/gaelyk-1.0.jar">Gaelyk JAR v1.0</a></td>
        </tr>
    </table>
</div>

<p>
You can use the <b>Gaelyk</b> JAR in combination with Groovy 1.8 and beyond and with the latest Google App Engine SDK.
</p>

<p>
The latest version was tested with Groovy 1.8.0,
and with the 1.5.1 version of the App Engine SDK.
</p>

<p>
But to get you started quickly, you may use a ready-made template project which bundles and configures everything.
</p>

<h2>Version 1.0</h2>

<h3>Changes</h3>
<ul>
    <li>GAE SDK updated to 1.5.1</li>
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
        <a href="/tutorial/app-engine-shortcuts#gaelykBindings">services and variables are injected in your classes as properties</a>,
        as the ones which are injected in Groovlets and templates
    </li>
    <li>
        The <a href="/tutorial/url-routing#path-variable-validation">validation closures</a> of the routes
        in your URL mapping have access to the request,
        so you can validate a URL depending on what's in your request (attribute, session, etc.)
    </li>
    <li>Added a DSLD file (DSL descriptor) for Eclipse for easing code-completion and navigation</li>
    <li>Problems with the recent XMPP support fixed</li>
    <li>
        Fixed inability to access the various services and variables
        from within binding/before/after blocks in plugin descriptors
    </li>
</ul>

<h3>Artifacts</h3>
<ul>
    <li>Gaelyk JAR: <a href="https://github.com/downloads/glaforge/gaelyk/gaelyk-1.0.jar">gaelyk-1.0.jar</a></li>
    <li>Gaelyk template project: <a href="https://github.com/downloads/glaforge/gaelyk/gaelyk-template-project-1.0.zip">gaelyk-template-project-1.0.zip</a></li>
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
    <li>Gaelyk JAR: <a href="https://github.com/downloads/glaforge/gaelyk/gaelyk-0.7.jar">gaelyk-0.7.jar</a></li>
    <li>Gaelyk template project: <a href="https://github.com/downloads/glaforge/gaelyk/gaelyk-template-project-0.7.zip">gaelyk-template-project-0.7.zip</a></li>
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

<h3>Artifacts</h3>
<ul>
    <li>Gaelyk JAR: <a href="http://cloud.github.com/downloads/glaforge/gaelyk/gaelyk-0.6.1.jar">gaelyk-0.6.1.jar</a></li>
    <li>Gaelyk template project: <a href="http://cloud.github.com/downloads/glaforge/gaelyk/gaelyk-template-project-0.6.1.zip">gaelyk-template-project-0.6.1.zip</a></li>
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