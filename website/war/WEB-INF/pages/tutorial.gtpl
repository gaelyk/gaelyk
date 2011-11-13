<title>Tutorial</title>

<h1 style="page-break-before: avoid;">Tutorial</h1>

<% if (!request.requestURI.contains('print')) { %>
<div id="sidebox" class="roundPinkBorder">
    <table>
        <tr>
            <td><a href="/tutorial/print"><img src="/images/icon-printer.png" alt="Printer-friendly"></a></td>
            <td><a href="/tutorial/print">Single page documentation</a></td>
        </tr>
        <tr>
            <td><a href="/gaelyk.pdf"><img src="/images/icon-pdf.png" alt="Documentation PDF"></a></td>
            <td><a href="/gaelyk.pdf">PDF documentation</a></td>
        </tr>
    </table>
</div>
<% } %>

<p>
The goal of this tutorial is to quickly get you started with using <b>Gaelyk</b> to  write
and deploy Groovy applications on Google App Engine.
We'll assume you have already downloaded and installed the Google App Engine SDK on your machine.
If you haven't, please do so by reading the 
<a href="http://code.google.com/appengine/docs/java/gettingstarted/installing.html">instructions</a> from Google.
</p>

<p>
The easiest way to get setup quickly is to download the template project from the <a href="/download">download section</a>.
It provides a ready-to-go project with the right configuration files pre-filled and an appropriate directory layout:
</p>

<ul>
    <li><code>web.xml</code> preconfigured with the <b>Gaelyk</b> servlets</li>
    <li><code>appengine-web.xml</code> with the right settings predefined (static file directive)</li>
    <li>a sample Groovlet and template</li>
    <li>the needed JARs (Groovy, Gaelyk and Google App Engine SDK)</li>
</ul>

<p>
You can <a href="/api/index.html">browse the JavaDoc</a> of the classes composing <b>Gaelyk</b>.
</p>

<h2>Table of Content</h2>

<ul>
    <li><a href="/tutorial/setup">Setting up your project</a></li>
    <ul>
        <li><a href="/tutorial/setup#layout">Directory layout</a></li>
        <li><a href="/tutorial/setup#configuration">Configuration files</a></li>
    </ul>

    <li><a href="/tutorial/template-project">The template project</a></li>
    <ul>
        <li><a href="/tutorial/template-project#build">Gradle build file</a></li>
        <li><a href="/tutorial/template-project#spock">Testing with Spock</a></li>
    </ul>

    <li><a href="/tutorial/views-and-controllers">Views and controllers</a></li>
    <ul>
        <li><a href="/tutorial/views-and-controllers#variables">Variables available in the binding</a></li>
        <ul>
            <li><a href="/tutorial/views-and-controllers#eager">Eager variables</a></li>
            <li><a href="/tutorial/views-and-controllers#lazy">Lazy variables</a></li>
            <li><a href="/tutorial/views-and-controllers#gaelykBindings">Injecting services and variables in your classes</a></li>
        </ul>
        <li><a href="/tutorial/views-and-controllers#templates">Templates</a></li>
        <ul>
            <li><a href="/tutorial/views-and-controllers#includes">Includes</a></li>
            <li><a href="/tutorial/views-and-controllers#redirect-forward">Redirect and forward</a></li>
        </ul>
        <li><a href="/tutorial/views-and-controllers#groovlets">Groovlets</a></li>
        <ul>
            <li><a href="/tutorial/views-and-controllers#markup-builder">Using MarkupBuilder to render XML or HTML snippets</a></li>
            <li><a href="/tutorial/views-and-controllers#view-delegation">Delegating to a view template</a></li>
        </ul>
        <li><a href="/tutorial/views-and-controllers#logging">Logging messages</a></li>
    </ul>

    <li><a href="/tutorial/url-routing">Flexible URL routing system</a></li>
    <ul>
        <li><a href="/tutorial/url-routing#configuration">Configuring URL routing</a></li>
        <li><a href="/tutorial/url-routing#route-definition">Defining URL routes</a></li>
        <li><a href="/tutorial/url-routing#wildcards">Using wildcards</a></li>
        <li><a href="/tutorial/url-routing#warmup">Warmup requests</a></li>
        <li><a href="/tutorial/url-routing#email-and-jabber">Incoming email and jabber messages</a></li>
        <li><a href="/tutorial/url-routing#path-variables">Using path variables</a></li>
        <ul>
            <li><a href="/tutorial/url-routing#path-variable-validation">Validating path variables</a></li>
        </ul>
        <li><a href="/tutorial/url-routing#capability-routing">Capability-aware routing</a></li>
        <li><a href="/tutorial/url-routing#ignore">Ignoring certain routes</a></li>
        <li><a href="/tutorial/url-routing#caching">Caching groovlet and template output</a></li>
        <li><a href="/tutorial/url-routing#namespace-scoped">Namespace scoped routes</a></li>
    </ul>

    <li><a href="/tutorial/app-engine-shortcuts">Google App Engine specific shortcuts</a></li>
    <ul>
        <li><a href="/tutorial/app-engine-shortcuts#datastore">Improvements to the low-level datastore API</a></li>
        <ul>
            <li><a href="/tutorial/app-engine-shortcuts#entity">Using <code>Entity</code>s as maps or POJOs/POGOs</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#conversion">Converting beans to entities and back</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#list-to-key-coercion">List to <code>Key</code> conversion</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#save-delete">Added <code>save()</code> and <code>delete()</code> methods on <code>Entity</code></a></li>
            <li><a href="/tutorial/app-engine-shortcuts#delete-get-on-key">Added <code>delete()</code> and <code>get()</code> methods on <code>Key</code></a></li>
            <li><a href="/tutorial/app-engine-shortcuts#key-string-conversion">Converting <code>Key</code> to an encoded <code>String</code> and vice-versa</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#transaction">Added <code>withTransaction()</code> method on the datastore service</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#datastore-get">Added <code>get()</code> methods on the datastore service</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#query">Querying</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#async-datastore">Asynchronous datastore</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#metadata">Datastore metadata querying</a></li>
        </ul>
        <li><a href="/tutorial/app-engine-shortcuts#task-queue">The task queue API shortcuts</a></li>
        <li><a href="/tutorial/app-engine-shortcuts#email">Email support</a></li>
        <ul>
            <li><a href="/tutorial/app-engine-shortcuts#incoming-mail">Incoming email messages</a></li>
        </ul>
        <li><a href="/tutorial/app-engine-shortcuts#jabber">XMPP/Jabber support</a></li>
        <ul>
            <li><a href="/tutorial/app-engine-shortcuts#jabber-sending">Sending messages</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#jabber-receiving">Receiving messages</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#jabber-presence">XMPP presence handling</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#jabber-subscription">XMPP subscription handling</a></li>
        </ul>
        <li><a href="/tutorial/app-engine-shortcuts#memcache">Enhancements to the Memcache service</a></li>
        <ul>
            <li><a href="/tutorial/app-engine-shortcuts#async-memcache">Asynchronous Memcache service</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#memoize">Closure memoization</a></li>
        </ul>
        <li><a href="/tutorial/app-engine-shortcuts#blobstore">Enhancements related to the Blobstore and File services</a></li>
        <ul>
            <li><a href="/tutorial/app-engine-shortcuts#blob-info">Getting blob information</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#serving-blob">Serving blobs</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#reading-blob">Reading the content of a Blob</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#delete-blob">Deleting a blob</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#blobstore-example">Example Blobstore service usage</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#file-service">File service</a></li>
        </ul>
        <li><a href="/tutorial/app-engine-shortcuts#namespace">Namespace support</a></li>
        <li><a href="/tutorial/app-engine-shortcuts#images">Images service enhancements</a></li>
        <ul>
            <li><a href="/tutorial/app-engine-shortcuts#images-wrapper">The images service and service factory wrapper</a></li>
            <li><a href="/tutorial/app-engine-shortcuts#image-dsl">An image manipulation language</a></li>
        </ul>
        <li><a href="/tutorial/app-engine-shortcuts#capabilities">Capabilities service support</a></li>
        <li><a href="/tutorial/app-engine-shortcuts#urlfetch">URLFetch Service improvements</a></li>
        <ul>
            <li><a href="/tutorial/app-engine-shortcuts#urlfetch-options">Allowed options</a></li>
        </ul>
        <li><a href="/tutorial/app-engine-shortcuts#channel">Channel service improvements</a></li>
        <li><a href="/tutorial/app-engine-shortcuts#backend">Backend service support</a></li>
    </ul>

    <li><a href="/tutorial/plugins">Simple plugin system</a></li>
    <ul>
        <li><a href="/tutorial/plugins#what">What a plugin can do for you</a></li>
        <li><a href="/tutorial/plugins#anatomy">Anatomy of a Gaelyk plugin</a></li>
        <ul>
            <li><a href="/tutorial/plugins#hierarchy">Hierarchy</a></li>
            <li><a href="/tutorial/plugins#descriptor">The plugin descriptor</a></li>
        </ul>
        <li><a href="/tutorial/plugins#using">Using a plugin</a></li>
        <li><a href="/tutorial/plugins#distribute">How to distribute and deploy a plugin</a></li>
    </ul>

    <li><a href="/tutorial/run-deploy">Running and deploying Gaelyk applications</a></li>
    <ul>
        <li><a href="/tutorial/run-deploy#run">Running and deploying Gaelyk applications</a></li>
        <li><a href="/tutorial/run-deploy#deploy">Deploying your application in the cloud</a></li>
    </ul>
</ul>
