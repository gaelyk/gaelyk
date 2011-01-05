<html>
<head>
    <title>Setting up your project</title>
</head>
<body>


<h1>Setting up your project</h1>

<a name="layout"></a>
<h2>Directory layout</h2>

<p>
We'll follow the directory layout proposed by the <b>Gaelyk</b> template project:
</p>

<pre>
/
+-- build.groovy
+-- src
+-- war
    |
    +-- index.gtpl
    +-- css
    +-- images
    +-- js
    +-- WEB-INF
        |
        +-- appengine-web.xml
        +-- web.xml
        +-- plugins.groovy      // if you use plugins
        +-- routes.groovy       // if you use the URL routing system
        +-- classes
        |
        +-- groovy
        |    |
        |    +-- controller.groovy
        |
        +-- includes
        |    |
        |    +-- header.gtpl
        |
        +-- lib
            |
            +-- appengine-api-1.0-sdk-x.y.z.jar
            +-- appengine-api-labs-x.y.z.jar
            +-- gaelyk-x.y.z.jar
            +-- groovy-all-x.y.z.jar

</pre>

<p>
At the root of your project, you'll find:
</p>

<ul>
    <li>
        <code>build.groovy</code>: a small Groovy build file using Groovy's AntBuilder to compiled Groovy and Java sources
        contained in the <code>src</code> directory. It's using Groovy's joint compiler.
        To run this build, if you've got additional sources to compile, simply launch the command <code>groovy build</code>
        &mdash; you'll need <a href="http://groovy.codehaus.org/Installing+Groovy">Groovy installed</a> on your machine.
    </li>
    <li>
        <code>src</code>: If your project needs source files beyond the templates and groovlets,
        you can place both your Java and Groovy sources in that directory.
        Before running the local app engine dev server or before deploying your application to app engine,
        you should run this script to pre-compile your Groovy and Java classes.
    </li>
    <li>
        <code>war</code>: This directory will be what's going to be deployed on app engine.
        It contains your groovlets, templates, images, JavaScript files, stylesheets, and more.
        It also contains the classical <code>WEB-INF</code> directory from typical Java web applications.
    </li>
</ul>

<p>
In the <code>WEB-INF</code> directory, you'll find:
</p>

<ul>
    <li>
        <code>appengine-web.xml</code>: The App Engine specific configuration file we'll detail below.
    </li>
    <li>
        <code>web.xml</code>: The usual Java EE configuration file for web applications.
    </li>
    <li>
        <code>classes</code>: The compiled classes (compiled with <code>build.groovy</code>) will go in that directory.
    </li>
    <li>
        <code>groovy</code>: In that folder, you'll put your controller and service files written in Groovy in the form of Groovlets.
    </li>
    <li>
        <code>includes</code>: We propose to let you put included templates in that directory.
    </li>
    <li>
        <code>lib</code>: All the needed libraries will be put here, the Groovy, <b>Gaelyk</b> and GAE SDK JARs,
        as well as any third-party JARs you may need in your application.
    </li>
</ul>

<blockquote>
<b>Note: </b> You may decide to put the Groovy scripts and includes elsewhere,
but the other files and directories can't be changed,
as they are files App Engine or the servlet container expects to find at that specific location.
</blockquote>

<p>
The template project comes with initial support for Eclipse project files,
allowing you to open the project easily within Eclipse.
</p>

<a name="configuration"></a>
<h2>Configuration files</h2>

<p>
With the directory layout ready, let's have a closer look at the configuration files:
the standard <code>web.xml</code> and App Engine's specific <code>appengine-web.xml</code>:
</p>

<h3>appengine-web.xml</h3>
<pre class="brush:xml">
    &lt;appengine-web-app xmlns="http://appengine.google.com/ns/1.0"&gt;
        &lt;!-- Your application ID --&gt;
        &lt;application&gt;myappid&lt;/application&gt;

        &lt;version&gt;1&lt;/version&gt;

        &lt;!-- If all your templates and groovlets are encoding in UTF-8 --&gt;
        &lt;!-- Please specify the settings below, otherwise weird characters may appear in your templates --&gt;
        &lt;system-properties&gt;
            &lt;property name="file.encoding" value="UTF-8" /&gt;
            &lt;property name="groovy.source.encoding" value="UTF-8" /&gt;

            &lt;!-- Define where the logging configuration file should be found --&gt;
            &lt;property name="java.util.logging.config.file" value="WEB-INF/logging.properties" /&gt;
        &lt;/system-properties&gt;

        &lt;!-- Uncomment this section if you want your application to be able to receive XMPP messages --&gt;
        &lt;!-- Similarily, if you want to receive incoming emails --&gt;
        &lt;!--
        &lt;inbound-services&gt;
            &lt;service&gt;xmpp_message&lt;/service&gt;
            &lt;service&gt;mail&lt;/service&gt;
        &lt;/inbound-services&gt;
        --&gt;

        &lt;static-files&gt;
            &lt;exclude path="/WEB-INF/**.groovy" /&gt;
            &lt;exclude path="**.gtpl" /&gt;
        &lt;/static-files&gt;
    &lt;/appengine-web-app&gt;
</pre>

<p>
The sole thing which is peculiar here is the fact we're excluding the files with a <code>.groovy</code> and <code>.gtpl</code>
extensions, as these files are non-static and correspond respectively to the <b>Gaelyk</b> Groovlets and templates.
We instruct App Engine to not serve these files as mere resource files, like images or stylesheets.
</p>

<blockquote>
    <b>Note: </b> You may decide to use different extensions than <code>.groovy</code> and <code>.gtpl</code>,
    if you prefer to have URLs with extensions which don't <i>leak</i> the underlying technologies being used.
    Or make sure to use the <a href="/tutorial/url-routing">flexible URL routing system</a>.
</blockquote>

<h3>web.xml</h3>
<pre class="brush:xml">
    &lt;web-app xmlns="http://java.sun.com/xml/ns/javaee" version="2.5"&gt;
        &lt;!-- A servlet context listener to initialize the plugin system --&gt;
        &lt;listener&gt;
            &lt;listener-class&gt;groovyx.gaelyk.GaelykServletContextListener&lt;/listener-class&gt;
        &lt;/listener&gt;

        &lt;!-- The Gaelyk Groovlet servlet --&gt;
        &lt;servlet&gt;
            &lt;servlet-name&gt;GroovletServlet&lt;/servlet-name&gt;
            &lt;servlet-class&gt;groovyx.gaelyk.GaelykServlet&lt;/servlet-class&gt;
        &lt;/servlet&gt;

        &lt;!-- The Gaelyk template servlet --&gt;
        &lt;servlet&gt;
            &lt;servlet-name&gt;TemplateServlet&lt;/servlet-name&gt;
            &lt;servlet-class&gt;groovyx.gaelyk.GaelykTemplateServlet&lt;/servlet-class&gt;
        &lt;/servlet&gt;

        &lt;!-- The URL routing filter --&gt;
        &lt;filter&gt;
            &lt;filter-name&gt;RoutesFilter&lt;/filter-name&gt;
            &lt;filter-class&gt;groovyx.gaelyk.routes.RoutesFilter&lt;/filter-class&gt;
        &lt;/filter&gt;

        &lt;!-- Specify a mapping between *.groovy URLs and Groovlets --&gt;
        &lt;servlet-mapping&gt;
            &lt;servlet-name&gt;GroovletServlet&lt;/servlet-name&gt;
            &lt;url-pattern&gt;*.groovy&lt;/url-pattern&gt;
        &lt;/servlet-mapping&gt;

        &lt;!-- Specify a mapping between *.gtpl URLs and templates --&gt;
        &lt;servlet-mapping&gt;
            &lt;servlet-name&gt;TemplateServlet&lt;/servlet-name&gt;
            &lt;url-pattern&gt;*.gtpl&lt;/url-pattern&gt;
        &lt;/servlet-mapping&gt;

        &lt;filter-mapping&gt;
            &lt;filter-name&gt;RoutesFilter&lt;/filter-name&gt;
            &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
        &lt;/filter-mapping&gt;

        &lt;!-- Define index.gtpl as a welcome file --&gt;
        &lt;welcome-file-list&gt;
            &lt;welcome-file&gt;index.gtpl&lt;/welcome-file&gt;
        &lt;/welcome-file-list&gt;
    &lt;/web-app&gt;
</pre>

<p>
In <code>web.xml</code>, we define the two Gaelyk servlets for Groovlets and templates,
as well as their respective mappings to URLs ending with <code>.groovy</code> and <code>.gtpl</code>.
We then define a welcome file for <code>index.gtpl</code>, so that URLs looking like a directory search for and template with that default name.
</p>

</body>
</html>