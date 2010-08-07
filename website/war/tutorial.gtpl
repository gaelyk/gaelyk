<% include '/WEB-INF/includes/header.gtpl' %>

<h1>Tutorial</h1>

<p>
The goal of this tutorial is to quickly get you started with using <b>Gaelyk</b> to  write
and deploy Groovy applications on Google App Engine.
We'll assume you have already downloaded and installed the Google App Engine SDK of your machine.
If you haven't, please do so by reading the 
<a href="http://code.google.com/appengine/docs/java/gettingstarted/installing.html">instructions</a> from Google.
</p>

<p>
The easiest way to get setup rapidly is to download the template project from the <a href="/download">download section</a>.
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

<h1>Setting up your project</h1>

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
        +-- plugins.groovy // if you use plugins
        +-- routes.groovy  // if you use the URL routing system
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
        &mdash you'll need <a href="http://groovy.codehaus.org/Installing+Groovy">Groovy installed</a> on your machine.
    </li>
    <li>
        <code>src</code>: If your project needs source files beyond the templates and groovlets, 
        you can place both your Java and Groovy sources in that directory.
        Before running the local app engine dev server or before deploying your application to app engine,
        you should run this script to pre-compile your Groovy and Java classes.
    </li>
    <li>
        <code>war</code>: This directory will be what's going to be deployed on app engine.
        It contains your templates, images, JavaScript files, stylesheets, and more.
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
as they are files App Engine or the servlet container expects to find at that specific locaction.
</blockquote>

<h2>Configuration files</h2>

<p>
With the directory layout ready, let's have a closer look at the configuration files:
the standard <code>web.xml</code> and App Engine's specific <code>appengine-web.xml</code>:
</p>

<h3>appengine-web.xml</h3>
<pre class="brush:xml">
    &lt;appengine-web-app xmlns="http://appengine.google.com/ns/1.0"&gt;
        &lt;!-- Your application ID --&gt;
        &lt;application>myappid&lt;/application&gt;
        &lt;!-- The current version of your deployed application --&gt;
        &lt;version&gt;1&lt;/version&gt;

        &lt;!-- Exclude Groovlets and templates from the static files --&gt;
        &lt;!-- to let App Engine know these files are not just mere resources --&gt;
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
</blockquote>

<blockquote>
    <b>Note: </b> In some cases, UTF-8 characters may not always be properly decoded by the template servlet.
    You may solve this problem by using the following snippet in <code>appengine-web.xml</code>:
    <pre class="brush:xml">
        &lt;system-properties&gt;
            &lt;property name="file.encoding" value="UTF-8"/&gt;
            &lt;property name="groovy.source.encoding" value="UTF-8"/&gt;
        &lt;/system-properties&gt;
    </pre>
</blockquote>

<h3>web.xml</h3>
<pre class="brush:xml">
    &lt;web-app xmlns="http://java.sun.com/xml/ns/javaee" version="2.5"&gt;
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

<h1>Views and controllers</h1>

<p>
Now that our project is all setup, it's time to dive into groovlets and templates.    
</p>

<blockquote>
<b>Tip: </b>A good practice is to separate your views from your logic 
(following the usual <a href="http://en.wikipedia.org/wiki/Model–view–controller">MVC pattern</a>).
Since <b>Gaelyk</b> provides both view templates and Groovlet controllers, it's advised to use the former for the view and the later for the logic.
</blockquote>

<p>
<b>Gaelyk</b> builds on Groovy's own <a href="http://groovy.codehaus.org/Groovlets">Groovlets</a> and 
<a href="http://groovy.codehaus.org/Groovy+Templates">template servlet</a> to add a shorcuts to the App Engine SDK APIs.
</p>

<blockquote>
<b>Note: </b> You can learn more about Groovy's Groovlets and templates from this <a href="http://www.ibm.com/developerworks/java/library/j-pg03155/">article on IBM developerWorks</a>. 
<b>Gaelyk</b>'s own Groovlets and templates are just an extension of Groovy's ones,
and simply decorate Groovy's Groovlets and templates by giving access to App Engine services
and add some additional methods to them via <a href="http://groovy.codehaus.org/Groovy+Categories">Groovy categories</a>.
</blockquote>

<p>
A special servlet binding gives you direct access to some implicit variables that you can use in your views and controllers:

<h3>Eager variables</h3>
<ul>
    <li>
        <tt>request</tt> : the 
        <a href="http://java.sun.com/javaee/5/docs/api/javax/servlet/http/HttpServletRequest.html"><code>HttpServletRequest</code></a> 
        object</li>
    <li>
        <tt>response</tt> : the 
        <a href="http://java.sun.com/javaee/5/docs/api/javax/servlet/http/HttpServletResponse.html"><code>HttpServletResponse</code></a> 
        object</li>
    <li>
        <tt>context</tt> : the 
        <a href="http://java.sun.com/javaee/5/docs/api/javax/servlet/ServletContext.html"><code>ServletContext</code></a> 
        object</li>
    <li>
        <tt>application</tt> : same as <code>context</code>
    </li>
    <li>
        <tt>session</tt> : shorthand for <code>request.getSession(<tt>false</tt>)</code> (can be null) which returns an 
        <a href="http://java.sun.com/javaee/5/docs/api/javax/servlet/http/HttpSession.html"><code>HttpSession</code></a>
    </li>
    <li>
        <tt>params</tt> : map of all form parameters (can be empty)
    </li>
    <li>
        <tt>headers</tt> : map of all <tt>request</tt> header fields
    </li>
    <li>
        <tt>log</tt> : a Groovy logger is available for logging messages through <code>java.util.logging</code>
    </li>
    <li>
        <tt>logger</tt> : a logger accessor can be used to get access to any logger
    </li>
</ul>

<h4>Logging messages</h4>

<p>
In your Groovlets and Templates, thanks to the <code>log</code> variable in the binding,
you can log messages through the <code>java.util.logging</code> infrastructure.
The <code>log</code> variable is an instance of <code>groovyx.gaelyk.logging.GroovyLogger</code>
and provides the methods:
<code>severe(String)</code>, <code>warning(String)</code>, <code>info(String)</code>, <code>config(String)</code>,
<code>fine(String)</code>, <code>finer(String)</code>, and <code>finest(String)</code>.
</p>

<p>
The default loggers in your groovlets and templates follow a naming convention.
The groovlet loggers' name starts with the <code>gaelyk.groovlet</code> prefix,
whereas the template loggers' name starts with <code>gaelyk.template</code>.
The name also contains the internal URI of the groovlet and template but transformed:
the slashes are exchanged with dots, and the extension of the file is removed.
</p>

<blockquote>
    <b>Note: </b>
    The extension is dropped, as one may have configured a different extension name for groovlets and templates
    than the usual ones (ie. <code>.groovy</code> and <code>.gtpl</code>).
</blockquote>

<p>
A few examples to illustrate this:
</p>

<table cellspacing="10">
    <tr>
        <th>URI</th>
        <th>Logger name</th>
    </tr>
    <tr>
        <td><code>/myTemplate.gtpl</code></td>
        <td><code>gaelyk.template.myTemplate</code></td>
    </tr>
    <tr>
        <td><code>/crud/scaffolding.gtpl</code></td>
        <td><code>gaelyk.template.crud.scaffolding</code></td>
    </tr>
    <tr>
        <td><code>/WEB-INF/templates/aTemplate.gtpl</code></td>
        <td><code>gaelyk.template.WEB-INF.templates.aTemplate</code></td>
    </tr>
    <tr>
        <td><code>/upload.groovy</code><br/>
            (ie. <code>/WEB-INF/groovy/upload.groovy</code>)</td>
        <td><code>gaelyk.groovlet.upload</code></td>
    </tr>
    <tr>
        <td><code>/account/credit.groovy</code><br/>
            (ie. <code>/WEB-INF/groovy/account/credit.groovy</code>)</td>
        <td><code>gaelyk.groovlet.account.credit</code></td>
    </tr>
</table>

<p>
Additionally, there are two special loggers for the incoming Email servlet, and the Jabber/XPP servlet, with
<code>gaelyk.email</code> and <code>gaelyk.jabber</code>.
</p>

<p>
This naming convention is particularly interesting as the <code>java.util.logging</code> infrastructure
follows a hierarchy of loggers depending on their names, using dot delimiters, where
<code>gaelyk.template.crud.scaffolding</code> inherits from
<code>gaelyk.template.crud</code> which inherits in turn from
<code>gaelyk.template</code>, then from
<code>gaelyk</code>. You get the idea!
For more information on this hierarchy aspect,
please refer to the <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/util/logging/class-use/LogManager.html">Java documentation</a>.
</p>

<p>
Concretely, it means you'll be able to have a fine grained way of defining your loggers hierarchy
and how they should be configured, as a child inherits from its parent configuration,
and a child is able to override parent's configuration.
So in your <code>logging.properties</code> file, you can have something like:
</p>

<pre>
# Set default log level to INFO
.level = INFO

# Configure Gaelyk's log level to WARNING, including groovlet's and template's
gaelyk.level = WARNING

# Configure groovlet's log level to FINE
gaelyk.groovlet.level = FINE

# Override a specific groovlet familty to FINER
gaelyk.groovlet.crud.level = FINER

# Set a specific groovlet level to FINEST
gaelyk.groovlet.crud.scaffoldingGroovlet.level = FINEST

# Set a specific template level to FINE
gaelyk.template.crud.editView.level = FINE
</pre>

<p class="brush:groovy">
You can also use the <code>GroovyLogger</code> in your Groovy classes:
</p>

<pre class="brush:groovy">
    import groovyx.gaelyk.logging.GroovyLogger
    // ...
    def log = new GroovyLogger("myLogger")
    log.info "This is a logging message with level INFO"
</pre>

<p>
It is possible to access any logger thanks to the logger accessor,
which is available in the binding under the name <code>logger</code>.
From a Groovlet or a Template, you can do:
</p>

<pre class="brush:groovy">
    // access a logger by its name, as a property access
    logger.myNamedLogger.info "logging an info message"

    // when the logger has a complex name (like a package name with dots), prefer the subscript operator:
    logger['com.foo.Bar'].info "logging an info message"
</pre>

<h3>Lazy variables</h3>
<ul>
    <li><tt>out</tt> : shorhand for <code>response.getWriter()</code> which returns a <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/io/PrintWriter.html"><code>PrintWriter</code></a></li>
    <li><tt>sout</tt> : shorhand for <code>response.getOutputStream()</code> which returns a <a href="http://java.sun.com/javaee/5/docs/api/javax/servlet/ServletOutputStream.html"><code>ServletOutputStream</code></a></li>
    <li><tt>html</tt> : shorhand for <code>new MarkupBuilder(response.getWriter())</code> which returns a <a href="http://groovy.codehaus.org/api/groovy/xml/MarkupBuilder.html"><code>MarkupBuilder</code></a></li>
</ul>

<blockquote>
    <b>Note: </b>
    The <i>eager</i> variables are pre-populated in the binding of your Groovlets and templates.
    The <i>lazy</i> variables are instantiated and inserted in the binding only upon the first request.
</blockquote>

<p>
Beyond those standard Servlet variables provided by Groovy's servlet binding, <b>Gaelyk</b> also adds ones of his own
by injecting specific elements of the Google App Engine SDK:
</p>

<ul>
    <li>
        <tt>datastore</tt> : the
        <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/DatastoreService.html">Datastore service</a>
        </li>
    <li>
        <tt>memcache</tt> : the
        <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/memcache/MemcacheService.html">Memcache service</a>
    </li>
    <li>
        <tt>urlFetch</tt> : the
        <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/urlfetch/URLFetchService.html">URL Fetch service</a>
    </li>
    <li>
        <tt>mail</tt> : the
        <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/mail/MailService.html">Mail service</a>
    </li>
    <li>
        <tt>images</tt> : the
        <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/images/ImagesService.html">Images service</a>
    </li>
    <li>
        <tt>users</tt> : the
        <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/users/UserService.html">User service</a>
    </li>
    <li>
        <tt>user</tt> : the currently logged in 
        <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/users/User.html">user</a> 
        (<code>null</code> if no user logged in)
    </li>
    <li>
        <tt>defaultQueue</tt> : the default <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/labs/taskqueue/Queue.html">queue</a>
    </li>
    <li>
        <tt>queues</tt> : a map-like object with which you can access the configured queues
    </li>
    <li>
        <tt>xmpp</tt> : the <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/xmpp/XMPPService.html">Jabber/XMPP service</a>.
    </li>
    <li>
        <tt>blobstore</tt> : the <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/blobstore/BlobstoreService.html">Blobstore service</a>.
    </li>
    <li>
        <tt>oauth</tt> : the <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/oauth/OAuthService.html">OAuth service</a>.
    </li>
    <li>
        <tt>localMode</tt> : a boolean variable which is <code>true</code> when the application is running in local
        development mode, and <code>false</code> when deployed on Google's cloud.
    </li>
    <li>
        <tt>app</tt> : a map variable with the following keys and values:
        <ul>
            <li><tt>id</tt> : the application ID (here: ${app.id})</li>
            <li><tt>version</tt> : the application version (here: ${app.version})</li>
            <li>
                <tt>env</tt> : a map with the following keys and values:
                <ul>
                    <li><tt>name</tt> : the environment name (here: ${app.env.name})</li>
                    <li><tt>version</tt> : the Google App Engine SDK version (here: ${app.env.version})</li>
                </ul>
            </li>
        </ul>
    </li>
</ul>

<blockquote>
    <b>Note: </b>
    Regarding the <code>app</code> variable, this means you can access those values with the following syntax
    in your groovlets and templates:
    <pre class="brush:groovy">
        app.id
        app.version
        app.env.name
        app.env.version
    </pre>
</blockquote>

<blockquote>
    <b>Note: </b>
    You can learn more about the
    <a href="http://code.google.com/appengine/docs/java/runtime.html#The_Environment">environment and system properties</a>
    Google App Engine exposes.
</blockquote>


<p>
Thanks to all these variables and services available, you'll be able to access the Google services and Servlet specific artifacts
with a short and concise syntax to further streamline the code of your application.
In the next section, we'll dive in the <b>Gaelyk</b> templates and groovlets.
</p>

<h2>Templates</h2>

<p>
<b>Gaelyk</b> templates are very similar to JSPs or PHP: they are pages containing scriptlets of code.
You can:
</p>

<ul>
    <li>put blocks of Groovy code inside <code>&lt;% /* some code */ %&gt;</code>,</li>
    <li>call <code>print</code> and <code>println</code> inside those scriptlets for writing to the servlet writer,</li>
    <li>use the <code>&lt;%= variable %&gt;</code> notation to insert a value in the output,</li>
    <li>or also the GString notation <code>\${variable}</code> to insert some text or value.</li>
</ul>

<p>
Let's have a closer look at an example of what a template may look like:
</p>

<pre class="brush:xml">
    &lt;html&gt;
        &lt;body&gt;
            &lt;p&gt;&lt;% 
                def message = "Hello World!"
                print message %&gt;
            &lt;/p&gt;
            &lt;p&gt;&lt;%= message %&gt;&lt;/p&gt;
            &lt;p&gt;\${message}&lt;/p&gt;
            &lt;ul&gt;
            &lt;% 3.times { %&gt;
                &lt;li&gt;\${message}&lt;/li&gt;
            &lt;% } %&gt;
            &lt;/ul&gt;
        &lt;/body&gt;
    &lt;/html&gt;
</pre>

<p>
The resulting HTML produced by the template will look like this:
</p>

<pre class="brush:xml">
    &lt;html&gt;
        &lt;body&gt;
            &lt;p&gt;Hello World!&lt;/p&gt;
            &lt;p&gt;Hello World!&lt;/p&gt;
            &lt;p&gt;Hello World!&lt;/p&gt;
            &lt;ul&gt;
                &lt;li&gt;Hello World!&lt;/li&gt;
                &lt;li&gt;Hello World!&lt;/li&gt;
                &lt;li&gt;Hello World!&lt;/li&gt;
            &lt;/ul&gt;
        &lt;/body&gt;
    &lt;/html&gt;
</pre>

<p>
If you need to import classes, you can also define imports in a scriplet at the top of your template as the following snippet shows:
</p>

<pre class="brush:groovy">
    &lt;% import com.foo.Bar %&gt;
</pre>

<blockquote>
    <b>Note: </b> Of course, you can also use Groovy's type aliasing with <code>import com.foo.ClassWithALongName as CWALN</code>.
    Then, later on, you can instanciate such a class with <code>def cwaln = new CWALN()</code>.
</blockquote>
<blockquote>
    <b>Note: </b> Also please note that import directives don't look like JSP directives (as of this writing).
</blockquote>

<p>
As we detailed in the previous section, you can also access the Servlet objects (request, response, session, context),
as well as Google App Engine's own services.
For instance, the following template will display a different message depending on whether a user is currently logged in or not:
</p>

<pre class="brush:xml">
    &lt;html&gt;
        &lt;body&gt;
            &lt;% if (user) { %&gt;
                &lt;p&gt;You are currently logged in.&lt;/p&gt;
            &lt;% } else { %&gt;
                &lt;p&gt;You're not logged in.&lt;/p&gt;
            &lt;% } %&gt;
        &lt;/body&gt;
    &lt;/html&gt;
</pre>

<h3>Includes</h3>

<p>
Often, you'll need to reuse certain graphical elements across different pages.
For instance, you always have a header, a footer, a navigation menu, etc.
In that case, the include mechanism comes in handy.
As adivsed, you may store templates in <code>WEB-INF/includes</code>.
In your main page, you may include a template as follows:
</p>

<pre class="brush:xml">
    &lt;% include '/WEB-INF/includes/header.gtpl' %&gt;

    &lt;div&gt;My main content here.&lt;/div&gt;

    &lt;% include '/WEB-INF/includes/footer.gtpl' %&gt;
</pre>

<h3>Redirect and forward</h3>

<p>
When you want to chain templates or Groovlets, you can use the Servlet redirect and forward capabilities.
To do a forward, simply do:
</p>

<pre class="brush:groovy">
    &lt;% forward 'index.gtpl' %&gt;
</pre>

<p>
For a redirect, you can do:
</p>

<pre class="brush:groovy">
    &lt;% redirect 'index.gtpl' %&gt;
</pre>

<h2>Groovlets</h2>

<p>
In contrast to view templates, Groovlets are actually mere Groovy scripts.
But they can access the output stream or the writer of the servlet, to write directly into the output.
Or they can also use the markup builder to output HTML or XML content to the view.
</p>

<p>
Let's have a look at an example Groovlet:
</p>

<pre class="brush:groovy">
    println """
        &lt;html&gt;
            &lt;body&gt;
    """
    
    [1, 2, 3, 4].each { number -> println "<p>\${number}</p>" }
    
    def now = new Date()
    
    println """
                &lt;p&gt;
                    \${now}
                &lt;/p&gt;
            &lt;/body&gt;
        &lt;/html&gt;
    """
</pre>

<p>
You can use <code>print</code> and <code>println</code> to output some HTML or other plain-text content to the view.
Instead of writing to <code>System.out</code>, <code>print</code> and <code>println</code> write to the output of the servlet.
For outputing HTML or XML, for instance, it's better to use a template, 
or to send fragments written with a Markup builder as we shall see in the next sessions.
Inside those Groovy scripts, you can use all the features and syntax constructs of Groovy 
(lists, maps, control structures, loops, create methods, utility classes, etc.)
</p>

<h3>Using <code>MarkupBuilder</code> to render XML or HTML snippets</h3>

<p>
Groovy's <code>MarkupBuilder</code> is a utility class that lets you create markup content (HTML / XML) with a Groovy notation,
instead of having to use ugly <code>println</code>s.
Our previous Groovlet can be written more cleanly as follows:
</p>

<pre class="brush:groovy">
html.html {
    body {
        [1, 2, 3, 4].each { number -> p number }

        def now = new Date()
        
        p now
    }
}
</pre>

<blockquote>
<b>Note: </b> You may want to learn more about <code>MarkupBuilder</code> in the 
<a href="http://groovy.codehaus.org/Creating+XML+using+Groovy%27s+MarkupBuilder">Groovy wiki documentation</a> or on this 
<a href="http://www.ibm.com/developerworks/java/library/j-pg05199/index.html?S_TACT=105AGX02&S_CMP=EDU">article from IBM developerWorks</a>.
</blockquote>

<p>
    
</p>

<h3>Delegating to a view template</h3>

<p>
As we explained in the section about redirects and forwards, at the end of your Groovlet, 
you may simply redirect or forward to a template.
This is particularly interesting if we want to properly decouple the logic from the view.
To continue improving our previous Groovlets, we may, for instance, have a Groovlet compute the data needed by a template to render.
We'll need a Groovlet and a template. The Groovlet <code>WEB-INF/groovy/controller.groovy</code> would be as follows:
</p>

<pre class="brush:groovy">
    request['list'] = [1, 2, 3, 4]
    request['date'] = new Date()
    
    forward 'display.gtpl'
</pre>

<blockquote>
<b>Note: </b> For accessing the request attributes, the following syntaxes are actually equivalent:
<pre class="brush:groovy">
    request.setAttribute('list', [1, 2, 3, 4])
    request.setAttribute 'list', [1, 2, 3, 4]
    request['list'] = [1, 2, 3, 4]
    request.list = [1, 2, 3, 4]
</pre>
</blockquote>


<p>
The Groovlet uses the request attributes as a means to transfer data to the template.
The last line of the Groovlet then forwards the data back to the template view <code>display.gtpl</code>:
</p>

<pre class="brush:xml">
    &lt;html&gt;
        &lt;body&gt;
        &lt;% request.list.each { number -&gt; %&gt;
            &lt;p&gt;\${number}&lt;/p&gt;
        &lt;% } %&gt;
            &lt;p&gt;\${request.date}&lt;/p&gt;
        &lt;/body&gt;
    &lt;/html&gt;
</pre>

<a name="url-routing"></a>
<h2>Flexible URL routing</h2>

<p>
Since <b>Gaelyk</b> 0.3.2, a flexible and powerful URL routing system was introduced:
you can use a small Groovy Domain-Specific Language for defining routes for nicer and friendlier URLs.
</p>

<h3>Configuring URL routing</h3>

<p>
To enable the URL routing system, you should configure the <code>RoutesFilter</code> servlet filter in <code>web.xml</code>:
</p>

<pre class="brush:xml">
    ...
    &lt;filter&gt;
        &lt;filter-name&gt;RoutesFilter&lt;/filter-name&gt;
        &lt;filter-class&gt;groovyx.gaelyk.routes.RoutesFilter&lt;/filter-class&gt;
    &lt;/filter&gt;
    ...
    &lt;filter-mapping&gt;
        &lt;filter-name&gt;RoutesFilter&lt;/filter-name&gt;
        &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
    &lt;/filter-mapping&gt;
    ...
</pre>

<blockquote>
<b>Note: </b> We advise to setup only one route filter, but it is certainly possible to define several ones
for different areas of your site.
By default, the filter is looking for the file <code>WEB-INF/routes.groovy</code> for the routes definitions,
but it is possible to override this setting by specifying a different route DSL file with a servlet filter configuration parameter:
<pre class="brush:xml">
    &lt;filter&gt;
        &lt;filter-name&gt;RoutesFilter&lt;/filter-name&gt;
        &lt;filter-class&gt;groovyx.gaelyk.routes.RoutesFilter&lt;/filter-class&gt;
        &lt;init-param&gt;
            &lt;param-name&gt;routes.location&lt;/param-name&gt;
            &lt;param-value&gt;WEB-INF/blogRoutes.groovy&lt;/param-value&gt;
        &lt;/init-param&gt;
    &lt;/filter&gt;
</pre>
</blockquote>

<h3>Defining URL routes</h3>

<p>
By default, once the filter is configured, URL routes are defined in <code>WEB-INF/routes.groovy</code>,
in the form of a simple Groovy scripts, defining routes in the form of a lightweight DSL.
The capabilities of the routing system are as follow, you can:
</p>

<ul>
    <li>match requests made with a certain method (GET, POST, PUT, DELETE), or all</li>
    <li>define the final destination of the request</li>
    <li>chose whether you want to forward or redirect to the destination URL (i.e. URL rewriting through forward vs. redirection)</li>
    <li>express variables in the route definition and reuse them as variables in the final destination of the request</li>
    <li>validate the variables according to some boolean expression, or regular expression matching</li>
    <li>use the available GAE services in the script (for instance, creating routes from records from the datastore)</li>
    <li>cache the output of groovlets and templates pointed by that route for a specified period of time</li>
</ul>

<p>
Let's see those various capabilities in action.
Imagine we want to define friendly URLs for our blog application.
Let's configure a first route in <code>WEB-INF/routes.groovy</code>.
Say you want to provide a shorthand URL <code>/about</code> that would redirect to your first blog post.
You could configure the <code>/about</code> route for all GET requests calling the <code>get</code> method.
You would then redirect those requests to the final destination with the <code>redirect</code> named argument:
</p>

<pre class="brush:groovy">
    get "/about", redirect: "/blog/2008/10/20/welcome-to-my-blog"
</pre>

<p>If you prefer to do a forward, so as to do URL rewriting to keep the nice short URL,
you would just replace <code>redirect</code> with <code>forward</code> as follows:</p>

<pre class="brush:groovy">
    get "/about", forward: "/blog/2008/10/20/welcome-to-my-blog"
</pre>

<p>
If you have different routes for different HTTP methods, you can use the <code>get</code>, <code>post</code>,
<code>put</code> and <code>delete</code> methods.
If you want to catch all the requests independently of the HTTP method used, you can use the <code>all</code> function.
Another example, if you want to post only to a URL to create a new blog article,
and want to delegate the work to a <code>post.groovy</code> Groovlet, you would create a route like this one:
</p>

<pre class="brush:groovy">
    post "/new-article", forward: "/post.groovy"     // shortcut for "/WEB-INF/groovy/post.groovy"
</pre>

<blockquote>
<b>Note: </b> When running your applications in development mode, <b>Gaelyk</b> is configured to take into accounts
any changes made to the <code>routes.groovy</code> definition file.
Each time a request is made, which goes through the route servlet filter, <b>Gaelyk</b> checks whether a more
recent route definition file exists.
However, once deployed on the Google App Engine cloud, the routes are set in stone and are not reloaded.
The sole cost of the routing system is the regular expression mapping to match request URIs against route patterns.
</blockquote>

<h3>Using wildcards</h3>

<p>
You can use a single and a double star as wildcards in your routes, similarly to the Ant globing patterns.
A single star matches a word (<code>/\\w+/</code>), where as a double start matches an arbitrary path.
For instance, if you want to show information about the blog authors,
you may forward all URLs starting with <code>/author</code> to the same Groovlet:
</p>

<pre class="brush:groovy">
    get "/author/*", forward: "/authorsInformation.groovy"
</pre>

<p>
This route would match requests made to <code>/author/johnny</code> as well as to <code>/author/begood</code>.
</p>

<p>
In the same vein, using the double star to forward all requests starting with <code>/author</code> to the same Groovlet:
</p>

<pre class="brush:groovy">
    get "/author/**", forward: "/authorsInformation.groovy"
</pre>

<p>
This route would match requests made to <code>/author/johnny</code>, as well as <code>/author/johnny/begood</code>,
or even <code>/author/johnny/begood/and/anyone/else</code>.
</p>

<blockquote>
<b>Warning: </b> Beware of the abuse of too many wildcards in your routes,
as they may be time consuming to compute when matching a request URI to a route pattern.
Better prefer several explicit routes than a too complicated single route.
</blockquote>

<h3>Using path variables</h3>

<p>
<b>Gaelyk</b> provides a more convenient way to retrieve the various parts of a request URI, thanks to path variables.
</p>

<p>
In a blog application, you want your article to have friendly URLs.
For example, a blog post announcing the release of Groovy 1.7-RC-1 could be located at:
<code>/article/2009/11/27/groovy-17-RC-1-released</code>.
And you want to be able to reuse the various elements of that URL to pass them in the query string of the Groovlet
which is responsible for displaying the article.
You can then define a route with path variables as shown in the example below:
</p>

<pre class="brush:groovy">
    get "/article/@year/@month/@day/@title", forward: "/article.groovy?year=@year&month=@month&day=@day&title=@title"
</pre>

<p>
The path variables are of the form <code>@something</code>, where something is a word (in terms of regular expressions).
Here, with our original request URI, the variables will contains the string <code>'2009'</code> for the
<code>year</code> variable, <code>'11'</code> for <code>month</code>, <code>'27'</code> for <code>day</code>,
and <code>'groovy-17-RC-1-released</code> for the <code>title</code> variable.
And the final Groovlet URI which will get the request will be
<code>/WEB-INF/groovy/article.groovy?year=2009&month=11&day=27&title=groovy-17-RC-1-released</code>,
once the path variable matching is done.
</p>

<blockquote>
<b>Note: </b> If you want to have optional path variables, you should define as many routes as options.
So you would define the following routes to display all the articles published on some year, month, or day:
<pre class="brush:groovy">
    get "/article/@year/@month/@day/@title", forward: "/article.groovy?year=@year&month=@month&day=@day&title=@title"
    get "/article/@year/@month/@day",        forward: "/article.groovy?year=@year&month=@month&day=@day"
    get "/article/@year/@month",             forward: "/article.groovy?year=@year&month=@month"
    get "/article/@year",                    forward: "/article.groovy?year=@year"
    get "/article",                          forward: "/article.groovy"
</pre>
Also, note that routes are matched in order of appearance.
So if you have several routes which map an incoming request URI, the first one encountered in the route definition file will win.
</blockquote>

<h3>Validating path variables</h3>

<p>
The routing system also allows you to validate path variables thanks to the usage of a closure.
So if you use path variable validation, a request URI will match a route if the route path matches,
but also if the closure returns a boolean, or a value which is coercible to a boolean
through to the usual <i>Groovy Truth</i> rules.
Still using our article route, we would like the year to be 4 digits, the month and day 2 digits,
and impose no particular constraints on the title path variable,
we could define our route as follows:
</p>

<pre class="brush:groovy">
    get "/article/@year/@month/@day/@title",
        forward: "/article.groovy?year=@year&month=@month&day=@day&title=@title",
        validate: { year ==~ /\\d{4}/ && month ==~ /\\d{2}/ && day ==~ /\\d{2}/ }
</pre>

<blockquote>
<b>Note: </b> Just as the path variables found in the request URI are replaced in the rewritten URL,
the path variables are also available inside the body of the closure,
so you can apply your validation logic.
Here in our closure, we used Groovy's regular expression matching support,
but you can use boolean logic that you want, like <code>year.isNumber()</code>, etc.
</blockquote>

<h3>Caching groovlet and template output</h3>

<p>
Since <b>Gaelyk</b> 0.4.1, support for caching groovlet and template output has been added,
and can be defined through the URL routing system.
This caching capability obviously leverages the Memcache service of Google App Engine.
In the definition of your routes, you simply have to add a new named parameter: <code>cache</code>,
indicating the number of seconds, minutes or hours you want the page to be cached.
Here are a few examples:
</p>

<pre class="brush:groovy">
    get "/news",     forward: "/new.groovy",     cache: 10.minutes
    get "/tickers",  forward: "/tickers.groovy", cache: 1.second
    get "/download", forward: "/download.gtpl",  cache: 2.hours
</pre>

<p>
The duration can be any number (an int) of second(s), minute(s) or hour(s):
both plural and singular forms are supported.
</p>

<blockquote>
<b>Note: </b> byte arrays (the content to be cached) and strings (the URI, the content-type and last modified information)
are stored in Memcache, and as they are simple types, they should even survive Google App Engine loading requests.
</blockquote>

<blockquote>
<b>Warning: </b> There is a known issue, in some cases, when you include other templates in your template you want to cache,
thanks to the <code>include</code> directive, the output of the various templates is mixed up.
Please report back to us if you encounter such issues, and help us fix it!
</blockquote>

<h1>Google App Engine specific shortcuts</h1>

<p>
In addition to providing direct access to the App Engine services, <b>Gaelyk</b> also adds some syntax sugar on top of these APIs.
Let's review some of these improvements.
</p>

<blockquote>
<b>Note: </b> These additions are not numerous, but other ones may be added in the future as the need arises.
</blockquote>

<h2>Email support</h2>

<h3>New <code>send()</code> method for the mail service</h3>

<p>
<b>Gaelyk</b> adds a new <code>send()</code> method to the 
<a href="http://code.google.com/intl/fr-FR/appengine/docs/java/javadoc/com/google/appengine/api/mail/MailService.html">mail service</a>,
which takes <i>named arguments</i>. That way, you don't have to manually build a new message yourself.
In your Groovlet, for sending a message, you can do this:
</p>

<pre class="brush:groovy">
    mail.send sender: "app-admin-email@gmail.com",
            to: "recipient@somecompany.com",
            subject: "Hello",
            textBody: "Hello, how are you doing? -- MrG",
            attachment: [data: "Chapter 1, Chapter 2".bytes, fileName: "outline.txt"]
</pre>

<p>
Similarily, a <code>sendToAdmins()</code> method was added to, for sending emails to the administrators of the application.
</p>

<blockquote>
    <b>Note: </b> There is a <code>from</code> alias for the <code>sender</code> attribute.
    And instead of a <code>textBody</code> attribute, you can send HTML content with the <code>htmlBody</code> attribute.
</blockquote>

<blockquote>
    <b>Note: </b> There are two attachment attributes: <code>attachment</code> and <code>attachments</code>.
    <ul>
        <li>
            <code>attachment</code> is used for when you want to send just one attachment.
            You can pass a map with a <code>data</code> and a <code>fileName</code> keys.
            Or you can use an instance of <code>MailMessage.Attachment</code>.
        </li>
        <li>
            <code>attachments</code> lets you define a list of attachments.
            Again, either the elements of that list are maps of <code>data</code> / <code>fileName</code> pairs,
            or instances of <code>MailMessage.Attachment</code>.
        </li>
    </ul>
</blockquote>

<h3>Incoming email messages</h3>

<p>
Since Google App Engine SDK version 1.2.6 (and <b>Gaelyk</b> 0.3), support for incoming email messages has been added,
in a similar vein as the incoming XMPP messaging support.
To enable incoming email support, you first need to update your <code>appengine-web.xml</code> file as follows:
</p>

<pre class="brush:xml">
    &lt;inbound-services&gt;
        &lt;service&gt;mail&lt;/service&gt;
    &lt;/inbound-services&gt;
</pre>

<p>
In your <code>web.xml</code> file, you must add a new servlet and a new servlet mapping:
</p>

<pre class="brush:xml">
    ...
    &lt;servlet>
        &lt;servlet-name&gt;EmailServlet&lt;/servlet-name&gt;
        &lt;servlet-class&gt;groovyx.gaelyk.GaelykIncomingEmailServlet&lt;/servlet-class&gt;
    &lt;/servlet>
    ...
    &lt;servlet-mapping>
        &lt;servlet-name&gt;EmailServlet&lt;/servlet-name&gt;
        &lt;url-pattern&gt;/_ah/mail/*&lt;/url-pattern&gt;
    &lt;/servlet-mapping&gt;
    ...
    &lt;!-- Only allow the SDK and administrators to have access to the incoming email endpoint --&gt;
    &lt;security-constraint&gt;
        &lt;web-resource-collection&gt;
            &lt;url-pattern&gt;/_ah/mail/*&lt;/url-pattern&gt;
        &lt;/web-resource-collection&gt;
        &lt;auth-constraint&gt;
            &lt;role-name&gt;admin&lt;/role-name&gt;
        &lt;/auth-constraint&gt;
    &lt;/security-constraint&gt;
    ...
</pre>

<p>
The <code>GaelykIncomingEmailServlet</code> will delegate the work of processing the incoming message to a script
situated by defauly in the <code>/WEB-INF/groovy/email.groovy</code> groovlet.
This groovlet has access to the usual services which are bound into the script's binding.
But there's an additional object in the binding, a <code>message</code> instance of
<a href="http://java.sun.com/products/javamail/javadocs/javax/mail/internet/MimeMessage.html"><code>javax.mail.MimeMessage</code></a>.
Then, from the <code>email.groovy</code> groovlet, you can access the properties of this object:
</p>

<pre class="brush:groovy">
    // access the sender of the email
    message.from

    // get the subject of the message
    message.subject
</pre>

<blockquote>
    <b>Note: </b> The <code>/_ah/mail/*</code> is hard-wired in the Google App Engine SDK.
    The star pattern corresponds to the recepient of the email.
    For instance, you may receive an email to <code>recipient@yourappid.appspot.com</code>,
    and the star will be corresponding to <code>recipient</code>.
</blockquote>

<h2>Improvements to the low-level datastore API</h2>

<p>
Although it's possible to use JDO and JPA in Google App Engine,
<b>Gaelyk</b> also lets you use the low-level raw API for accessing the datastore,
and makes the <code>Entity</code> class from that API a bit more Groovy-friendly.
</p>

<h3>Using <code>Entity</code>s as maps or POJOs/POGOs</h3>

<blockquote>
    <b>Note: </b> POGO stands for Plain Old Groovy Object.
</blockquote>

<p>
When you use the <code>Entity</code> class from Java, you have to use methods like <code>setProperty()</code> or <code>getProperty()</code>
to access the properties of your <code>Entity</code>, making the code more verbose than it needs to be (at least in Java).
Ultimately, you would like to be able to use this class (and its instances) as if they were just like a simple map, or as a normal Java Bean.
That's what <b>Gaelyk</b> proposes by letting you use the subscript operator just like on maps, or a normal property notation.
The following example shows how you can access <code>Entity</code>s:
</p>

<pre class="brush:groovy">
    import com.google.appengine.api.datastore.Entity
    
    Entity entity = new Entity("person")

    // subscript notation, like when accessing a map
    entity['name'] = "Guillaume Laforge"
    println entity['name']

    // normal property access notation
    entity.age = 32
    println entity.age
</pre>

<p>
A handy mechanism exists to assign several properties at once, on your entities, using the <code>&lt;&lt;</code> (leftshit) operator.
This is particularly useful when you have properties coming from the request, in the <code>params</code> map variable.
You can the do the following to assign all the key/values in the map as properties on your entity:
</p>

<pre class="brush:groovy">
    // the request parameters contain a firstname, lastname and age key/values:
    // params = [firstname: 'Guillaume', lastname: 'Laforge', title: 'Groovy Project Manager']

    Entity entity = new Entity("person")

    entity << params

    assert entity.lastname == 'Laforge'
    assert entity.firstname == 'Guillaume'
    assert entity.title == 'Groovy Project Manager'

    // you can also select only the key/value pairs you'd like to set on the entity
    // thanks to Groovy's subMap() method, which will create a new map with just the keys you want to keep
    entity << params.subMap([firstname, lastname])
</pre>

<blockquote>
<b>Note: </b> <b>Gaelyk</b> adds a few converter methods to ease the creation of instances
of some GAE SDK types that can be used as properties of entities, using the <code>as</code> operator:
<pre class="brush:groovy">
    "foobar@gmail.com" as Email
    "foobar@gmail.com" as JID

    "http://www.google.com" as Link
    new URL("http://gaelyk.appspot.com") as Link

    "+33612345678" as PhoneNumber
    "50 avenue de la Madeleine, Paris" as PostalAddress

    "groovy" as Category
    
    32 as Rating
    "32" as Rating

    "long text" as Text

    "some byte".getBytes() as Blob
    "some byte".getBytes() as ShortBlob

    "foobar" as BlobKey

    [45.32, 54.54] as GeoPt
</pre>
</blockquote>

<h3>Converting beans to entities and back</h3>

<p>
The mechanism explained above with type conversions (actually called "coercion") is also available
and can be handy for converting between a concrete bean and an entity.
Any POJO or POGO can thus be converted into an <code>Entity</code>,
and you can also convert an <code>Entity</code> to a POJO or POGO.
</p>

<pre class="brush:groovy">
    // given a POJO
    class Person {
        String name
        int age
    }

    def e1 = new Entity("Person")
    e1.name = "Guillaume"
    e1.age = 33

    // coerce an entity into a POJO
    def p1 = e1 as Person

    assert e1.name == p1.name
    assert e1.age == p1.age

    def p2 = new Person(name: "Guillaume", age: 33)
    // coerce a POJO into an entity
    def e2 = p2 as Entity

    assert p2.name == e2.name
    assert p2.age == e2.age
</pre>

<blockquote>
<b>Note: </b> The POJO/POGO class simpleName property is used as the entity kind.
So for example, if the <code>Person</code> class was in a package <code>com.foo</code>,
the entity kind used would be <code>Person</code>, not the fully-qualified name.
This is the same default strategy that <a href="http://code.google.com/p/objectify-appengine/">Objectify</a>
is using.
</blockquote>


<blockquote>
<b>Note: </b> In turn, with this feature, you have a lightweight object/entity mapper.
However, remember it's a simplistic solution for doing object/entity mapping,
and this solution doesn't take into accounts relationships and such.
If you're really interested in a fully featured mapper, you should have a look at
<a href="http://code.google.com/p/objectify-appengine/">Objectify</a>
or <a href="http://code.google.com/p/twig-persist/">Twig</a>.
</blockquote>


<h3>Added <code>save()</code> and <code>delete()</code> methods on <code>Entity</code></h3>

<p>
In the previous sub-section, we've created an <code>Entity</code>, but we need to store it in Google App Engine's datastore.
We may also wish to delete an <code>Entity</code> we would have retrieved from that datastore.
For doing so, in a <i>classical</i> way, you'd need to call the <code>save()</code> and <code>put()</code> methods
from the <code>DataService</code> instance.
However, <b>Gaelyk</b> dynamically adds a <code>save()</code> and <code>delete()</code> method on <code>Entity</code>:
</p>

<pre class="brush:groovy">
    def entity = new Entity("person")
    entity.name = "Guillaume Laforge"
    entity.age = 32
    
    entity.save()
</pre>

<p>
Afterwards, if you need to delete the <code>Entity</code> you're working on, you can simply call:
</p>

<pre class="brush:groovy">
    entity.delete()
</pre>

<h3>Added <code>delete()</code> method on <code>Key</code></h3>

<p>
Sometimes, you are dealing with keys, rather than dealing with entities directly &mdash;
the main reaons being often for performance sake, as you don't have to load the full entity.
If you want to delete an element in the datastore, when you just have the key, you can do so as follows:
</p>

<pre class="brush:groovy">
    someEntityKey.delete()
</pre>

<h3>Added <code>withTransaction()</code> method on the datastore service</h3>

<p>
Last but not least, if you want to work with transactions, instead of using the <code>beginTransaction()</code> 
method of <code>DataService</code>, then the <code>commit()</code> and <code>rollback()</code> methods on that <code>Transaction</code>,
and doing the proper transaction handling yourself, you can use the <code>withTransaction()</code> method
that <b>Gaelyk</b> adds on <code>DataService</code> and which takes care of that boring task for you:
</p>

<pre class="brush:groovy">
    datastore.withTransaction {
        // do stuff with your entities within the transaction
    }
</pre>

<p>
The <code>withTransaction()</code> method takes a closure as sole parameter,
and within that closure, upon its execution by <b>Gaelyk</b>, your code will be in the context of a transaction.
</p>

<h3>Querying</h3>

<p>
<b>Gaelyk</b> currently doesn't provide additional capabilities for querying the datastore
beyond what is provided by the Google App Engine SDK &mdash; however, the situation may change in future releases.
Below you will see an example of queries used in the <a href="http://groovyconsole.appspot.com">Groovy Web Console</a>
to retrieve scripts written by a given author, sorted by descending date of creation:
</p>

<pre class="brush:groovy">
    import com.google.appengine.api.datastore.*
    import static com.google.appengine.api.datastore.FetchOptions.Builder.*

    // query the scripts stored in the datastore
    // "savedscript" corresponds to the entity table containing the scripts' text 
    def query = new Query("savedscript")

    // sort results by descending order of the creation date
    query.addSort("dateCreated", Query.SortDirection.DESCENDING)

    // filters the entities so as to return only scripts by a certain author
    query.addFilter("author", Query.FilterOperator.EQUAL, params.author)

    PreparedQuery preparedQuery = datastore.prepare(query)

    // return only the first 10 results
    def entities = preparedQuery.asList( withLimit(10) )
</pre>

<h2>The task queue API shortcuts</h2>

<p>
In version 1.2.5 of the Google App Engine SDK, Google added an experimental support for task queues.
An application has a default queue, but other queues can be added through the configuration of a
<code>queue.xml</code> file in <code>/WEB-INF</code>.
</p>

<blockquote>
<b>Note: </b> You can learn more about <a href="http://code.google.com/appengine/docs/java/config/queue.html">queues</a>
and <a href="http://code.google.com/appengine/docs/java/taskqueue/overview.html">task queues</a>, and how to
configure them on the online documentation.
</blockquote>

<p>
In your Groovlets and templates, you can access the default queue directly, as it is passed into the binding:
</p>

<pre class="brush:groovy">
    // access the default queue
    defaultQueue
</pre>

<p>
You can access the queues either using a subscript notation or the property access notation:
</p>

<pre class="brush:groovy">
    // access a configured queue named "dailyEmailQueue" using the subscript notation
    queues['dailyEmailQueue']

    // or using the property access notation
    queues.dailyEmailQueue

    // you can also access the default queue with:
    queues.default
</pre>    

<p>
To get the name of a queue, you can call the provided <code>getQueueName()</code> method,
but <b>Gaelyk</b> provides also a <code>getName()</code> method on
<a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/labs/taskqueue/Queue.html">Queue</a>
so that you can write <code>queue.name</code>, instead of the more verbose <code>queue.getQueueName()</code> or
<code>queue.queueName</code>, thus avoid repetition of queue.
</p>

<p>
For creating tasks and submitting them on a queue, with the SDK you have to use the
<code><a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/labs/taskqueue/TaskOptions.Builder.html">TaskOptions.Builder</a></code>.
In addition to this builder approach, <b>Gaelyk</b> provides a shortcut notation for adding tasks to the queue using named arguments:
</p>

<pre class="brush:groovy">
    // add a task to the queue
    queue.add countdownMillis: 1000, url: "/task/dailyEmail",
        taskName: "Send daily email newsletter",
        method: 'PUT', params: [date: '20090914'],
        payload: content
</pre>

<p>
There is also a variant with an overloaded <code>&lt;&lt;</code> operator:
</p>

<pre class="brush:groovy">
    // add a task to the queue
    queue << [
        countdownMillis: 1000, url: "/task/dailyEmail",
        taskName: "Send daily email newsletter",
        method: 'PUT', params: [date: '20090914'],
        payload: content
   ]
</pre>

<h2>XMPP/Jabber support</h2>

<p>
Since version 1.2.5 of the Google App Engine SDK, support for instant messaging through XMPP/Jabber support has been added.
This also means your <b>Gaelyk</b> applications can now send and receive instant messages.
</p>

<blockquote>
<b>Note: </b> You can learn more about
<a href="http://code.google.com/appengine/docs/java/xmpp/overview.html">XMPP support</a> on the online documentation.
</blockquote>

<h3>Sending messages</h3>

<p>
<b>Gaelyk</b> provides a few additional methods to take care of sending instant messages, get the presence of users,
or to send invitations to other users.
Applications usually have a corresponding Jabber ID named after your application ID, such as <code>gaelyk@appspot.com</code>.
To be able to send messages to other users, your application will have to invite other users, or be invited to chat.
So make sure you do so for being able to send messages.
</p>

<p>
Let's see what it would look like in a Groovlet for sending messages to a user:
</p>

<pre class="brush:groovy">
    String recipient = "someone@gmail.com"

    // check if the user is online
    if (xmpp.getPresence(recipient).isAvailable()) {
        // send the message
        def status = xmpp.send(to: recipient, body: "Hello, how are you?")

        // checks the message was successfully delivered to all the recipients
        assert status.isSuccessful()
    }
</pre>

<p>
<b>Gaelyk</b> once again decorates the various XMPP-related classes in the App Engine SDK with new methods:
</p>

<ul>
    <li>on <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/xmpp/XMPPService.html"><code>XMPPService</code></a>'s instance
        <ul>
            <li><code>SendResponse send(Map msgAttr)</code> : more details on this method below</li>
            <li><code>void sendInvitation(String jabberId)</code> : send an invitation to a user</li>
            <li><code>sendInvitation(String jabberIdTo, String jabberIdFrom)</code> : send an invitation to a user from a different Jabber ID</li>
            <li><code>Presence getPresence(String jabberId)</code> : get the presence of this particular user</li>
            <li><code>Presence getPresence(String jabberIdTo, String jabberIdFrom)</code> : same as above but using a different Jabber ID for the request</li>
        </ul>
    </li>
    <li>on <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/xmpp/Message.html"><code>Message</code></a> instances
        <ul>
            <li><code>String getFrom()</code> : get the Jabber ID of the sender of this message</li>
            <li><code>GPathResult getXml()</code> : get the XmlSlurper parsed document of the XML payload</li>
            <li><code>List&lt;String&gt; getRecipients()</code> : get a list of Strings representing the Jabber IDs of the recipients</li>
        </ul>
    </li>
    <li>on <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/xmpp/SendResponse.html"><code>SendResponse</code></a> instances
        <ul>
            <li><code>boolean isSuccessful()</code> : checks that all recipients received the message</li>
        </ul>
    </li>
</ul>

<p>
To give you a little more details on the various attributes you can use to create messages to be sent,
you can pass the following attributes to the <code>send()</code> method of <code>XMPPService</code>:
</p>

<ul>
    <li><tt>body</tt> : the raw text content of your message</li>
    <li><tt>xml</tt> : a closure representing the XML payload you want to send</li>
    <li><tt>to</tt> : contains the recipients of the message (either a String or a List of String)</li>
    <li><tt>from</tt> : a String representing the Jabber ID of the sender</li>
    <li><tt>type</tt> : either an instance of the
        <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/xmpp/MessageType.html"><code>MessageType</code></a>
        enum or a String (<code>'CHAT'</code>, <code>'ERROR'</code>, <code>'GROUPCHAT'</code>, <code>'HEADLINE'</code>,
        <code>'NORMAL'</code>)</li>
</ul>

<blockquote>
<b>Note: </b> <code>body</code> and <code>xml</code> are exclusive, you can't specify both at the same time.
</blockquote>

<p>
We mentioned the ability to send XML payloads, instead of normal chat messages:
this functionality is particularly interesting if you want to use XMPP/Jabber as a communication transport
between services, computers, etc. (ie. not just real human beings in front of their computer).
We've shown an example of sending raw text messages, here's how you could use closures in the <code>xml</code>
to send XML fragments to a remote service:
</p>

<pre class="brush:groovy">
    String recipient = "service@gmail.com"

    // check if the service is online
    if (xmpp.getPresence(recipient).isAvailable()) {
        // send the message
        def status = xmpp.send(to: recipient, xml: {
            customers {
                customer(id: 1) {
                    name 'Google'
                }
            }
        })

        // checks the message was successfully delivered to the service
        assert status.isSuccessful()
    }
</pre>

<blockquote>
<b>Implementation detail: </b> the closure associated with the <code>xml</code> attribute is actually passed to
an instance of <a href="http://groovy.codehaus.org/Creating+XML+using+Groovy%27s+StreamingMarkupBuilder"><code>StreamingMarkupBuilder</code></a>
which creates an XML stanza.
</blockquote>

<h3>Receiving messages</h3>

<p>
It is also possible to receive messages from users.
For that purpose, <b>Gaelyk</b> introduces a new servlet that will take care of being the receiver of those messages,
which will arrive to your application, as a web request through this new Servlet (<code>GaelykXmppServlet</code>).
To enable the reception of messages, you'll have to do two things:
</p>

<ul>
    <li>add a new configuration fragment in <code>appengine-web.xml</code></li>
    <li>defing the servlet in the <code>web.xml</code></li>
</ul>

<p>As a first step, let's configure <code>appengine-web.xml</code> by adding this new element:</p>

<pre class="brush:xml">
    &lt;inbound-services&gt;
        &lt;service&gt;xmpp_message&lt;/service&gt;
    &lt;/inbound-services&gt;
</pre>

<p>Then configure the new servlet as follows:</p>

<pre class="brush:xml">
    ...
    &lt;servlet&gt;
        &lt;servlet-name&gt;XmppServlet&lt;/servlet-name&gt;
        &lt;servlet-class&gt;groovyx.gaelyk.GaelykXmppServlet&lt;/servlet-class&gt;
    &lt;/servlet&gt;
    ...
    &lt;servlet-mapping&gt;
        &lt;servlet-name&gt;XmppServlet&lt;/servlet-name&gt;
        &lt;url-pattern&gt;/_ah/xmpp/message/chat/&lt;/url-pattern&gt;
    &lt;/servlet-mapping&gt;
    ...
</pre>

<blockquote>
<b>Note: </b> The URL pattern of the servlet mapping cannot be changed, as this is a hard-wired URL that the
Google App Engine will look for in order to send messages to your application.
</blockquote>

<p>
The XMPP servlet will look for a Groovy script named <code>jabber.groovy</code> in <code>/WEB-INF/groovy</code>
of your application &mdash; this is also hard-wired, but this time, mandated by <b>Gaelyk</b>.
This script, similarily to how mere Groovlets work, will handle the incoming messages, through a <code>POST</code>
to the <code>/_ah/xmpp/message/chat</code> URL.
As usual, all the common variables are available in your script through the binding.
But there's also a new variable that you can use: <code>message</code>, an instance of
<a href="http://code.google.com/intl/fr-FR/appengine/docs/java/javadoc/com/google/appengine/api/xmpp/Message.html"><code>Message</code></a>
that you can use as shown below:
</p>

<pre class="brush:groovy">
    // get the body of the message
    message.body

    // get the sender Jabber ID
    message.from

    // get the list of recipients Jabber IDs
    message.recipients

    // if the message is an XML document instead of a raw string message
    if (message.isXml()) {
        // get the raw XML
        message.stanza

        // get a document parsed with XmlSlurper
        message.xml
    }
</pre>

<h2>Enhancements to the Memcache service</h2>

<p>
<b>Gaelyk</b> provides a few additional methods to the Memcache service, to get and put values in the cache
using Groovy's natural subscript notation, as well as for using the <code>in</code> keyword to check when a key
is present in the cache or not.
</p>

<pre class="brush:groovy">
    class Country implements Serializable { String name }

    def countryFr = new Country(name: 'France')

    // use the subscript notation to put a country object in the cache, identified by a string
    // (you can also use non-string keys)
    memcache['FR'] = countryFr

    // check that a key is present in the cache
    if ('FR' in memcache) {
        // use the subscript notation to get an entry from the cache using a key
        def countryFromCache = memcache['FR']
    }
</pre>

<blockquote>
<b>Note: </b> Make sure the objects you put in the cache are serializable.
Also, be careful with the last example above as the <code>'FR'</code> entry in the cache
may have disappeared between the time you do the <code>if (... in ...)</code> check
and the time you actually retrieve the value associated with the key from memcache.
</blockquote>

<a name="blobstore"></a>
<h2>Enhancements related to the BlobStore</h2>

<p>
<b>Gaelyk</b> provides several enhancements around the usage of the blobstore service.
</p>

<h3>Getting blob information</h3>

<p>
Given a blob key, you can retrieve various details about the blob when it was uploaded:
</p>

<pre class="brush:groovy">
    BlobKey blob = ...

    // retrieve an instance of BlobInfo
    BlobInfo info = blob.info

    // directly access the BlobInfo details from the key itself
    String filename     = blob.filename
    String contentType  = blob.contentType
    Date creation       = blob.creation
    long size           = blob.size
</pre>

<h3>Deleting a blob</h3>

<p>Given a blob key, you can easily delete it thanks to the <code>delete()</code> method:</p>

<pre class="brush:groovy">
    BlobKey blob = ...

    blob.delete()
</pre>

<h3>Serving blobs</h3>

<p>
With the blobstore service, you can stream the content of blobs back to the browser, directly on the respone object:
</p>

<pre class="brush:groovy">
    BlobKey blob = ...

    // serve the whole blob
    blob.serve response

    // serve a fragment of the blob
    def range = new ByteRange(1000) // starting from 1000
    blob.serve response, range

    // serve a fragment of the blob using an int range
    blob.serve response, 1000..2000
</pre>

<h3>Reading the content of a Blob</h3>

<p>
Beyond the ability to serve blobs directly to the response output stream with
<code>blobstoreService.serve(blobKey, response)</code> from your groovlet,
since version 1.3.5 of Google App Engine's SDK, there is the possibility of
<a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/blobstore/BlobstoreInputStream.html">obtaining an <code>InputStream</code></a>
to read the content of the blob.
<b>Gaelyk</b> 0.4.1 adds three convenient methods on <code>BlobKey</code>
to easily deal with a raw input stream or with a reader, leveraging Groovy's own input stream and reader methods.
The stream and reader are handled properly with regards to cleanly openning and close those resources
so that you don't have to take care of that aspect yourself.
</p>

<pre class="brush:groovy">
    BlobKey blobKey = ...

    blobKey.withStream { InputStream stream ->
        // do something with the stream
    }

    // defaults to using UTF-8 as encodin for reading from the underlying stream
    blobKey.withReader { Reader reader ->
        // do something with the reader
    }

    // specifying the encoding of your choice
    blobKey.withReader("UTF-8") { Reader reader ->
        // do something with the reader
    }
</pre>

<p>
You can also fetch byte arrays for a given range:
</p>

<pre class="brush:groovy">
    BlobKey blob = ...
    byte[] bytes

    // using longs
    bytes = blob.fetchData 1000, 2000

    // using a Groovy int range
    bytes = blob.fetchData 1000..2000

    // using a ByteRange
    def range = new ByteRange(1000, 2000) // or 1000..2000 as ByteRange
    bytes = blob.fetchData range
</pre>

<h2>Example Blobstore service usage</h2>

<p>
In this section, we'll show you a full-blown example.
First of all, let's create a form to submit a file to the blobstore,
in a template named <code>upload.gtpl</code> at the root of your war:
</p>

<pre class="brush:xml">
    &lt;html&gt;
    &lt;body&gt;
        &lt;h1&gt;Please upload a text file&lt;/h1&gt;
        &lt;form action="\${blobstore.createUploadUrl('/uploadBlob.groovy')}"
                method="post" enctype="multipart/form-data"&gt;
            &lt;input type="file" name="myTextFile"&gt;
            &lt;input type="submit" value="Submit"&gt;
        &lt;/form&gt;
    &lt;/body&gt;
    &lt;/html&gt;
</pre>

<p>
The form will be posted to a URL created by the blobstore service,
that will then forward back to the URL you've provided when calling
<code>blobstore.createUploadUrl('/uploadBlob.groovy')</code>
</p>

<blockquote>
<b>Warning: </b> The URL to he groovlet to which the blobstore service will forward the uploaded blob details
should be a direct path to the groovlet like <code>/WEB-INF/groovy/uploadBlob.groovy</code> or the shortcut
<code>/uploadBlob.groovy</code>.
For an unknown reason, you cannot use a URL defined through the URL routing system.
This is not necessarily critical, in the sense that this URL is never deplayed in the browser anyway.
</blockquote>

<p>
Now, create a groovlet named <code>uploadBlob.groovy</code> stored in <code>/WEB-INF/groovy</code>
with the following content:
</p>

<pre class="brush:groovy">
    def blobs = blobstore.getUploadedBlobs(request)
    def blob = blobs["myTextFile"]

    response.status = 302

    if (blob) {
        redirect "/success?key=\${blob.keyString}"
    } else {
        redirect "/failure"
    }
</pre>

<p>
In the groovlet, you retrieve all the blobs uploaded in the <code>upload.gtpl</code> page,
and more particularly, the blob coming from the <code>myTextFile</code> input file element.
</p>

<blockquote>
<b>Warning: </b> Google App Engine mandates that you specify explicitely a redirection status code (301, 302 or 303),
and that you <b>do</b> redirect the user somewhere else, otherwise you'll get some runtime errors.
</blockquote>

<p>
We define some friendly URLs in the URL routing definitions for the upload form template, the success and failure pages:
</p>

<pre class="brush:groovy">
    get "/upload",  forward: "/upload.gtpl"
    get "/success", forward: "/success.gtpl"
    get "/failure", forward: "/failure.gtpl"
</pre>

<p>
You then create a <code>failure.gtpl</code> page at the root of your war directory:
</p>

<pre class="brush:xml">
    &lt;html&gt;
        &lt;body&gt;
            &lt;h1&gt;Failure&lt;/h1&gt;
            &lt;h2&gt;Impossible to store or access the uploaded blob&lt;/h2&gt;
        &lt;/body&gt;
    &lt;/html&gt;
</pre>

<p>
And a <code>success.gtpl</code> page at the root of your war directory,
showing the blob details, and outputing the content of the blob (a text file in our case):
</p>

<pre class="brush:xml">
    &lt;% import com.google.appengine.api.blobstore.BlobKey %&gt;
    &lt;html&gt;
        &lt;body&gt;
            &lt;h1&gt;Success&lt;/h1&gt;
            &lt;% def blob = new BlobKey(params.key) %&gt;

            &lt;div>
                File name: \${blob.filename} &lt;br/&gt;
                Content type: \${blob.contentType}&lt;br/&gt;
                Creation date: \${blob.creation}&lt;br/&gt;
                Size: \${blob.size}
            &lt;/div&gt;

            &lt;h2&gt;Content of the blob&lt;/h2&gt;

            &lt;div&gt;
                &lt;% blob.withReader { out << it.text } %&gt;
            &lt;/div&gt;
        &lt;/body&gt;
    &lt;/html&gt;
</pre>

<p>
    Now that you're all set up, you can access <code>http://localhost:8080/upload</code>,
    submit a text file to upload, and click on the button.
    Google App Engine will store the blob and forward the blob information to your <code>uploadBlob.groovy</code> groovlet
    that will then redirect to the success page (or failure page in case something goes wrong).
</p>

<a name="plugin"></a>
<h1>Simple plugin system</h1>

<p>
Since <b>Gaelyk</b> 0.4, a plugin system helps you modularize your applications
and enable you to share commonalities between <b>Gaelyk</b> applications.
</p>

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

<h3>The plugin descriptor</h3>

<p>
The plugin descriptor is where you'll be able to tell the <b>Gaelyk</b> runtime to:
</p>

<ul>
    <li>add new variables in the binding of groovlets and templates</li>
    <li>add new routes to the URL routing system</li>
    <li>define new categories to be applied to enrich APIs (GAE, third-party or your own)</li>
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
as the one we explained in the <a href="#url-routing">URL routing</a> section.

<blockquote>
<b>Note:</b> Contrary to binding variables or categories, the first route that matches is the one which is chosen.
This means a plugin cannot overwrite the existing application routes, or routes defined by previous plugins in the chain.
</blockquote>

<blockquote>
<b>Important:</b> If your plugins contribute routes, make sure your application has also configured the routes filter,
as well as defined a <code>WEB-INF/routes.groovy</code> script, otherwise no plugin routes will be present. 
</blockquote>

<p>
The <code>categories</code> method call takes a list of classes
which are <a href="http://groovy.codehaus.org/Groovy+Categories">Groovy categories</a>.
It's actually just a <em>varags</em> method taking as many classes as you want.
</p>

<p>
Wherever in your plugin descriptor, you can put any initialization code you may need in your plugin.
</p>

<blockquote>
<b>Important:</b> The plugins are loaded once, as soon as the first request is served.
So your initialization code, adding binding variables, categories and routes, will only be done once per application load.
Knowing that Google App Engine can load and unload apps depending on trafic, this is important to keep in mind as well.
</blockquote>

<blockquote>
<b>Remark:</b> Another thing to remember is that as plugins are loaded only once when the first request happen,
there is no reloading capabilities for the plugin descriptor. Of course, groovlets or templates are reloadable,
when you're doing changes live while your application is working in development mode.
But changes to the plugin descriptor won't reload the whole application, so if you're changing the descriptor,
you will have to restart the container.
</blockquote>

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
As mentioned previously while talking about the precendence rules, the order with which the plugins are loaded
may have an impact on your application or other plugins previously installed and initialized.
But hopefully, such conflicts shouldn't happen too often, and this should be resolved easily,
as you have full control over the code you're installing through these plugins to make the necessary amendments
should there be any.
</p>

<h2>How to distribute and deploy a plugin</h2>

<p>
If you want to share a plugin you've worked on, you just need to zip everything that constitutes the plugin.
Then you can share this zip, and someone who wishes to install it on his application will just need to unzip it
and pickup the various files of that archive and stick them up in the appropriate directories
in his/her <b>Gaelyk</b> <code>war/</code> folder, and reference that plugin, as explained in the previous section.
</p>

<h1>Running and deploying Gaelyk applications</h1>

<h2>Running your application locally</h2>

<p>
Google App Engine provides a local servlet container, powered by Jetty, which lets you run your applications locally.
If you're using the <b>Gaelyk</b> template, when you're at the root of your project 
&mdash; and we assume you have installed the App Engine SDK on your machine &mdash;
you can run your application with the following command-line:
</p>

<pre>
dev_appserver.sh war
</pre>

<blockquote>
<b>Note: </b> Notice that there are some subtle differences between running locally and in the cloud.
You'd better always check how your application works once deployed, as there may be some differences in behaviour between the two.
</blockquote>

<h2>Deploying your application in the cloud</h2>

<p>
Once you're at the root of your application, simply run the usual deployment command:
</p>

<pre>
appcfg.sh update war
</pre>

<% include '/WEB-INF/includes/footer.gtpl' %>
