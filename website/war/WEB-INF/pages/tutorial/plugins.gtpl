<html>
<head>
    <title>Simple plugin system</title>
</head>
<body>

<h1>Simple plugin system</h1>

<p>
Since <b>Gaelyk</b> 0.4, a plugin system helps you modularize your applications
and enable you to share commonalities between <b>Gaelyk</b> applications.
</p>

<a name="what"></a>
<h2>What a plugin can do for you</h2>

<p>
A plugin lets you:
</p>

<ul>
    <li>provide additional <b>groovlets</b> and <b>templates</b></li>
    <li>contribute new URL <b>routes</b></li>
    <li>add new <b>categories</b> to enhance existing classes (like third-party libraries)</li>
    <li>define and bind new <b>variables in the binding</b> (the "global" variables available in groovlets and templates)</li>
    <li>provide any kind of <b>static content</b>, such as JavaScript, HTML, images, etc.</li>
    <li>add new <b>libraries</b> (ie. additional JARs)</li>
    <li>and more generally, let you do any <b>initialization</b> at the startup of your application</li>
</ul>

<p>
Possible examples of plugins can:
</p>

<ul>
    <li>provide a "groovy-fied" integration of a third-party library, like nicer JSON support</li>
    <li>create a reusable CRUD administration interface on top of the datastore to easily edit content of all your <b>Gaelyk</b> applications</li>
    <li>install a shopping cart solution or payment system</li>
    <li>setup a lightweight CMS for editing rich content in a rich-media application</li>
    <li>define a bridge with Web 2.0 applications (Facebook Connect, Twitter authentication)</li>
    <li>and more...</li>
</ul>

<a name="anatomy"></a>
<h2>Anatomy of a Gaelyk plugin</h2>

<p>
A plugin is actually just some content you'll drop in your <code>war/</code> folder, at the root of your <b>Gaelyk</b> application!
This is why you can add all kind of static content, as well as groovlets and templates, or additional JARs in <code>WEB-INF/lib</code>.
Furthermore, plugins don't even need to be external plugins that you install in your applications,
but you can just customize your application by using the conventions and capabilities offered by the plugin system.
Then, you really just need to have <code>/WEB-INF/plugins.groovy</code> referencing
<code>/WEB-INF/plugins/myPluginDescriptor.groovy</code>, your plugin descriptor.
</p>

<p>
In addition to that, you'll have to create a plugin descriptor will allow you to define new binding variables,
new routes, new categories, and any initialization code your plugin may need on application startup.
This plugin descriptor should be placed in <code>WEB-INF/plugins</code> and will be a normal groovy script.
From this script, you can even access the Google App Engine services, which are available in the binding of the script --
hence available somehow as pseudo global variables inside your scripts.
</p>

<p>
Also, this plugin descriptor script should be referenced in the <code>plugins.groovy</code> script in <code>WEB-INF/</code>
</p>

<a name="hierarchy"></a>
<h3>Hierarchy</h3>

<p>
As hinted above, the content of a plugin would look something like the following hierarchy:
</p>

<pre>
/
+-- war
    |
    +-- someTemplate.gtpl                       // your templates
    |
    +-- css
    +-- images                                  // your static content
    +-- js
    |
    +-- WEB-INF
        |
        +-- plugins.groovy                      // the list of plugins descriptors to be installed
        |
        +-- plugins
        |   |
        |   +-- myPluginDescriptor.groovy       // your plugin descriptor
        |
        +-- groovy
        |    |
        |    +-- myGroovlet.groovy              // your groovlets
        |
        +-- includes
        |    |
        |    +-- someInclude.gtpl               // your includes
        |
        +-- classes                             // compiled classes like categories
        |
        +-- lib
            |
            +-- my-additional-dependency.jar    // your JARs

</pre>

<p>
We'll look at the plugin descriptor in a moment, but otherwise, all the content you have in your plugin
is actually following the same usual web application conventions in terms of structure,
and the ones usually used by <b>Gaelyk</b> applications (ie. includes, groovlets, etc).
The bare minimum to have a plugin in your application is to have a plugin descriptor,
like <code>/WEB-INF/plugins/myPluginDescriptor.groovy</code> in this example,
that is referenced in <code>/WEB-INF/plugins.groovy</code>.
</p>

<p>
Developing a plugin is just like developing a normal <b>Gaelyk</b> web application.
Follow the usual conventions and describe your plugin in the plugin descriptor.
Then afterwards, package it, share it, and install it in your applications.
</p>

<a name="descriptor"></a>
<h3>The plugin descriptor</h3>

<p>
The plugin descriptor is where you'll be able to tell the <b>Gaelyk</b> runtime to:
</p>

<ul>
    <li>add new variables in the binding of groovlets and templates</li>
    <li>add new routes to the URL routing system</li>
    <li>define new categories to be applied to enrich APIs (GAE, third-party or your own)</li>
    <li>define before / after request actions</li>
    <li>and do any initialization you may need</li>
</ul>

<p>
Here's what a plugin descriptor can look like:
</p>

<pre class="brush:groovy">
// add imports you need in your descriptor
import net.sf.json.*
import net.sf.json.groovy.*

// add new variables in the binding
binding {
    // a simple string variable
    jsonLibVersion = "2.3"
    // an instance of a class of a third-party JAR
	json = new JsonGroovyBuilder()
}

// add new routes with the usual routing system format
routes {
    get "/json", forward: "/json.groovy"
}

before {
    log.info "Visiting \${request.requestURI}"
    binding.uri = request.requestURI
    request.message = "Hello"
}

after {
    log.info "Exiting \${request.requestURI}"
}

// install a category you've developped
categories jsonlib.JsonlibCategory

// any other initialization code you'd need
// ...
</pre>

Inside the <code>binding</code> closure block, you just assign a value to a variable.
And this variable will actually be available within your groovlets and templates as implicit variables.
So you can reference them with <code>\${myVar}}</code> in a template,
or use <code>myVar</code> directly inside a groovlet, without having to declare or retrieve it in any way.

<blockquote>
<b>Note:</b> a plugin may overwrite the default <b>Gaelyk</b> variable binding,
or variable bindings defined by the previous plugin in the initialization chain.
In the plugin usage section, you'll learn how to influence the order of loading of plugins.
</blockquote>

Inside the <code>routes</code> closure block, you'll put the URL routes following the same syntax
as the one we explained in the <a href="tutorial.gtpl#url-routing">URL routing</a> section.

<blockquote>
<b>Note:</b> Contrary to binding variables or categories, the first route that matches is the one which is chosen.
This means a plugin cannot overwrite the existing application routes, or routes defined by previous plugins in the chain.
</blockquote>

<blockquote>
<b>Important:</b> If your plugins contribute routes, make sure your application has also configured the routes filter,
as well as defined a <code>WEB-INF/routes.groovy</code> script, otherwise no plugin routes will be present.
</blockquote>

<p>
In the <code>before</code> and <code>after</code> blocks,
you can access the <code>request</code>, <code>response</code>, <code>log</code>, and <code>binding</code> variables.
The logger name is of the form <code>gaelyk.plugins.myPluginName</code>.
The <code>binding</code> variables allows you to update the variables
that are put in the binding of Groovlets and templates.
</p>

<p>
The <code>categories</code> method call takes a list of classes
which are <a href="http://groovy.codehaus.org/Groovy+Categories">Groovy categories</a>.
It's actually just a <em>varargs</em> method taking as many classes as you want.
</p>

<p>
Wherever in your plugin descriptor, you can put any initialization code you may need in your plugin.
</p>

<blockquote>
<b>Important:</b> The plugins are loaded once, as soon as the first request is served.
So your initialization code, adding binding variables, categories and routes, will only be done once per application load.
Knowing that Google App Engine can load and unload apps depending on traffic, this is important to keep in mind as well.
</blockquote>

<blockquote>
<b>Remark:</b> In development mode, plugin descriptors are reloaded upon each request,
so it may slow down the requests to your groovlets and templates significantly.
</blockquote>

<a name="using"></a>
<h2>Using a plugin</h2>

<p>
If you recall, we mentioned the <code>plugins.groovy</code> script.
This is a new script since <b>Gaelyk</b> 0.4, that lives alongside the <code>routes.groovy</code> script
(if you have one) in <code>/WEB-INF</code>.
If you don't have a <code>plugins.groovy</code> script, obviously, no plugin will be installed &mdash;
or at least none of the initialization and configuration done in the various plugin descriptors will ever get run.
</p>

<p>
This <code>plugins.groovy</code> configuration file just lists the plugins you have installed and want to use.
An example will illustrate how you reference a plugin:
</p>

<pre class="brush:groovy">
install jsonPlugin
</pre>

<blockquote>
<b>Note:</b> For each plugin, you'll have an <code>install</code> method call, taking as parameter the name of the plugin.
This name is actually just the plugin descriptor script name.
In this example, this means <b>Gaelyk</b> will load <code>WEB-INF/plugins/jsonPlugin.groovy</code>.
</blockquote>

<p>
As mentioned previously while talking about the precedence rules, the order with which the plugins are loaded
may have an impact on your application or other plugins previously installed and initialized.
But hopefully, such conflicts shouldn't happen too often, and this should be resolved easily,
as you have full control over the code you're installing through these plugins to make the necessary amendments
should there be any.
</p>

<p>
When you are using two plugins with before / after request actions,
the order of execution of these actions also depends on the order in which you installed your plugins.
For example, if you have installed <code>pluginOne</code> first and <code>pluginTwo</code> second,
here's the order of execution of the actions and of the Groovlet or template:
</p>

<ul>
    <li>pluginOne's before action</li>
    <ul>
        <li>pluginTwo's before action</li>
        <ul>
            <li>execution of the request</li>
        </ul>
        <li>pluginTwo's after action</li>
    </ul>
    <li>pluginOne's after action</li>
</ul>

<a name="distribute"></a>
<h2>How to distribute and deploy a plugin</h2>

<p>
If you want to share a plugin you've worked on, you just need to zip everything that constitutes the plugin.
Then you can share this zip, and someone who wishes to install it on his application will just need to unzip it
and pickup the various files of that archive and stick them up in the appropriate directories
in his/her <b>Gaelyk</b> <code>war/</code> folder, and reference that plugin, as explained in the previous section.
</p>

</body>
</html>