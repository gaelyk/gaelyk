<% include '/WEB-INF/includes/header.gtpl' %>

<h1>Tutorial</h1>

<p>
The goal of this tutorial is to quickly get you started with using <b>Gaelyk</b> to let you write 
and deploy your Groovy applications on Google App Engine.
We'll assume you have already downloaded and installed the Google App Engine SDK of your machine.
If you haven't, please do so by reading the 
<a href="http://code.google.com/appengine/docs/java/gettingstarted/installing.html">instructions</a> from Google.
</p>

<p>
The easiest way to get setup rapidly is to download the template project from the <a href="/download/">download section</a>.
It provides a ready-to-go project with the right configuration files pre-filled and an appropriate directory layout:
</p>

<ul>
    <li><code>web.xml</code> precondigured with the <b>Gaelyk</b> servlets</li>
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
        +-- classes
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
</ul>
<h3>Lazy variables</h3>
<ul>
    <li><tt>out</tt> : shorhand for <code>response.getWriter()</code> which returns a <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/io/PrintWriter.html"><code>PrintWriter</code></a></li>
    <li><tt>sout</tt> : shorhand for <code>response.getOutputStream()</code> which returns a <a href="http://java.sun.com/javaee/5/docs/api/javax/servlet/ServletOutputStream.html"><code>ServletOutputStream</code></a></li>
    <li><tt>html</tt> : shorhand for <code>new MarkupBuilder(response.getWriter())</code> which returns a <a href="http://groovy.codehaus.org/api/groovy/xml/MarkupBuilder.html"><code>MarkupBuilder</code></a></li>
</ul>

<blockquote>
    <b>Note: </b>
    The <i>eager</i> variables are pre-populated in the binding of your Groovlets and templates.
    The <i>lazy</i> variables are instanciated and inserted in the binding only upon the first request.
</blockquote>

<p>
Beyond those standard Servlet variables provided by Groovy's servlet binding, <b>Gaelyk</b> also adds ones of his own
by injecting specific elements of the Google App Engine SDK:
</p>

<ul>
    <li>
        <tt>datastoreService</tt> : the 
        <a href="http://code.google.com/intl/fr-FR/appengine/docs/java/javadoc/com/google/appengine/api/datastore/DatastoreService.html">Datastore service</a>
        </li>
    <li>
        <tt>memcacheService</tt> : the 
        <a href="http://code.google.com/intl/fr-FR/appengine/docs/java/javadoc/com/google/appengine/api/memcache/MemcacheService.html">Memcache service</a>
    </li>
    <li>
        <tt>urlFetchService</tt> : the 
        <a href="http://code.google.com/intl/fr-FR/appengine/docs/java/javadoc/com/google/appengine/api/urlfetch/URLFetchService.html">URL Fetch service</a>
    </li>
    <li>
        <tt>mailService</tt> : the 
        <a href="http://code.google.com/intl/fr-FR/appengine/docs/java/javadoc/com/google/appengine/api/mail/MailService.html">Mail service</a>
    </li>
    <li>
        <tt>imagesService</tt> : the 
        <a href="http://code.google.com/intl/fr-FR/appengine/docs/java/javadoc/com/google/appengine/api/images/ImagesService.html">Images service</a>
    </li>
    <li>
        <tt>userService</tt> : the 
        <a href="http://code.google.com/intl/fr-FR/appengine/docs/java/javadoc/com/google/appengine/api/users/UserService.html">User service</a>
    </li>
    <li>
        <tt>user</tt> : the currently logged in 
        <a href="http://code.google.com/intl/fr-FR/appengine/docs/java/javadoc/com/google/appengine/api/users/User.html">user</a> 
        (<code>null</code> if no user logged in)
    </li>
    <li>
        <tt>defaultQueue</tt> : the default <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/labs/taskqueue/Queue.html">queue</a>
    </li>
    <li>
        <tt>queues</tt> : a map-like object with which you can access the configured queues
    </li>
    <li>
        <tt>xmppService</tt> : the <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/xmpp/XMPPService.html">Jabber/XMPP service</a>.
    </li>
</ul>

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
    &lt;% include 'WEB-INF/includes/header.gtpl' %&gt;

    &lt;div&gt;My main content here.&lt;/div&gt;

    &lt;% include 'WEB-INF/includes/footer.gtpl' %&gt;
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
    request.setAttribute 'list', [1, 2, 3, 4]
    request.setAttribute 'date', new Date()
    
    forward 'display.gtpl'
</pre>

<p>
The Groovlet uses the request attributes as a means to transfer data to the template.
The last line of the Groovlet then forwards the data back to the template view <code>display.gtpl</code>:
</p>

<pre class="brush:xml">
    &lt;html&gt;
        &lt;body&gt;
        &lt;% request.getAttribute('list').each { number -&gt; %&gt; 
            &lt;p&gt;\${number}&lt;/p&gt;
        &lt;% } %&gt;
            &lt;p&gt;\${request.getAttribute('date')}&lt;/p&gt;
        &lt;/body&gt;
    &lt;/html&gt;
</pre>

<h2>URL mapping</h2>

<h3>Different extensions for Groovlets and templates</h3>

<p>
So far, we used the extension <code>.groovy</code> for our Groovlets and <code>.gtpl</code> for our templates.
But these extensions are by no means mandatory. You may, for example, decide to use <code>.action</code> for Groovlets
and <code>.html</code> for the templates, so that nobody guesses the underlying technology being used.
This is as simple as changing the servlet mappings in <code>web.xml</code>.
</p>

<pre class="brush:xml">
    ...
    &lt;servlet-mapping&gt;
        &lt;servlet-name&gt;GroovletServlet&lt;/servlet-name&gt;
        &lt;url-pattern&gt;*.action&lt;/url-pattern&gt;
    &lt;/servlet-mapping&gt;

    &lt;servlet-mapping&gt;
        &lt;servlet-name&gt;TemplateServlet&lt;/servlet-name&gt;
        &lt;url-pattern&gt;*.html&lt;/url-pattern&gt;
    &lt;/servlet-mapping&gt;
    ...
</pre>

<p>
If you want to hide the <code>.groovy</code> extension, or make it optional, you can also leverage a somewhat
lesser-known feature of Groovlets, with its regex resource replacement init parameter.
With the following init parameters defined in <code>web.xml</code>, when you define the <b>Gaelyk</b> servlet,
you can achieve making the <code>.groovy</code> extension optional:
</p>

<pre class="brush:xml">
    ...
    &lt;servlet&gt;
        &lt;servlet-name&gt;GroovletServlet&lt;/servlet-name&gt;
        &lt;servlet-class&gt;groovyx.gaelyk.GaelykServlet&lt;/servlet-class&gt;
        &lt;init-param&gt;
            &lt;!-- This parameter is true by default, you can omit it if you want to replace all occurrences --&gt;
            &lt;param-name&gt;resource.name.replace.all&lt;/param-name&gt;
            &lt;param-value&gt;false&lt;/param-value&gt;
        &lt;/init-param&gt;
        &lt;init-param&gt;
            &lt;param-name&gt;resource.name.regex&lt;/param-name&gt;
            &lt;param-value&gt;(\\w*/)*((\\w*)(\\.groovy)?)(\\?.*)?&lt;/param-value&gt;
        &lt;/init-param&gt;
        &lt;init-param&gt;
            &lt;param-name&gt;resource.name.replacement&lt;/param-name&gt;
            &lt;param-value&gt;\$3.groovy&lt;/param-value&gt;
        &lt;/init-param&gt;
    &lt;/servlet&gt;
    ...
</pre>

<p>
This mechanism takes the request URI and transform that path into a different path using regular expression replacement,
to point to a difference resource, ie. a Groovlet.
So for example, if your path is <code>/knowledgeBase/question/show?id=33</code>,
it would be transformed into <code>show.groovy</code> for the Groovlet resource to be found by the Groovlet servlet.
Here, we're just keeping the last <i>word</i> matched to point to the Groovlet that's going to be executed:
the third group denoted by <code>\$3</code> corresponds to the second occurrence of <code>\\w*</code> in the regex.
</p>

<p>
You could also decide to keep the path, by tweaking the regular expression.
You would use <code>((\\w*/)*)((\\w*)(\\.groovy)?)(\\?.*)?</code> as regex
and <code>\$1\$3.groovy</code> as replacement string.
Contrary to our previous example, you'd get <code>/knowledgeBase/question/show.groovy</code> as a result.
</p>

<blockquote>
<b>Note: </b> To help you with finding the right regular expression and replacement string,
you may validate your matching with the following Groovy snippet, that you can run in the Groovy console or shell:
<pre class="brush:groovy">
    String myPath = '/knowledgeBase/question/show?id=33'
    String regex = '(\\\\w*/)*((\\\\w*)(\\\\.groovy)?)(\\\\?.*)?'
    String replacement = '\$3.groovy'

    // use replaceFirst() when the init param "resource.name.replace.all"
    // is set to false, replaceAll() otherwise
    assert myPath.replaceFirst(regex, replacement) == 'show.groovy'
</pre>
</blockquote>

<blockquote>
<b>Warning: </b> the <code>(\\?.*)?</code> part in the regular expression corresponds to the request parameters
passed in the URL. Make sure to not keep that part in the resulting string, because there would be no file with a
name like <code>show.groovy?id=32</code> on the file system.
</blockquote>

<h3>REST-ful URLs</h3>

<p>
Nowadays, the REST architectural style has become a best practice for our web applications, so you may also wish
to provide REST-ful URLs to the users and consumers of your <b>Gaelyk</b> pages and services.
In this section, we'll see how we can get REST-ful URLs using that string replacement technique.
You could use a solution which keeps the path and omits the <code>.groovy</code> extension,
similar to the one suggested above.
Or you could use a simpler mapping, if you want to redirect all URLs to a central REST endpoint,
that would then serve as a kind of front controller.
You could do as follows:
</p>

<pre class="brush:xml">
    ...
    &lt;servlet&gt;
        &lt;servlet-name&gt;GroovletServlet&lt;/servlet-name&gt;
        &lt;servlet-class&gt;groovyx.gaelyk.GaelykServlet&lt;/servlet-class&gt;
        &lt;init-param&gt;
            &lt;param-name&gt;resource.name.replace.all&lt;/param-name&gt;
            &lt;param-value&gt;false&lt;/param-value&gt;
        &lt;/init-param&gt;
        &lt;init-param&gt;
            &lt;param-name&gt;resource.name.regex&lt;/param-name&gt;
            &lt;!-- Match all request URIs (regular expression) --&gt;
            &lt;param-value&gt;.*&lt;/param-value&gt;
        &lt;/init-param&gt;
        &lt;init-param&gt;
            &lt;param-name&gt;resource.name.replacement&lt;/param-name&gt;
            &lt;!-- Always redirect to the rest.groovy Groovlet --&gt;
            &lt;param-value&gt;rest.groovy&lt;/param-value&gt;
        &lt;/init-param&gt;
    &lt;/servlet&gt;
    ...
    &lt;servlet-mapping&gt;
        &lt;servlet-name>GroovletServlet&lt;/servlet-name&gt;
        &lt;!-- Match all request URIs (servlet url pattern matching) --&gt;
        &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
    &lt;/servlet-mapping&gt;
    ...
</pre>

<blockquote>
<b>Warning: </b> Remember that the servlet mapping URL pattern is not a usual Java regular expression.
Whereas the <code>resource.name.regex</code> init parameter corresponds to a real Java regular expression.
</blockquote>

<p>
Inside your <code>rest.groovy</code> Groovlet, you are then able to know which HTTP method was used, by using:
</p>

<pre class="brush:groovy">
    request.requestURI.tokenize('/')
</pre>

<p>
For a path like <code>/customer/32</code>, you would get a Groovy list like <code>['customer', '32']</code>.
</p>

<p>
You can retrieve the HTTP method being used with:
</p>

<pre class="brush:groovy">
    request.method
</pre>

And remember you can also get the request parameters thanks to the <code>params</code> variable.

<h1>Google App Engine specific shortcuts</h1>

<p>
In addition to providing direct access to the App Engine services, <b>Gaelyk</b> also adds some syntax sugar on top of these APIs.
Let's review some of these improvements.
</p>

<blockquote>
<b>Note: </b> These additions are not numerous, but other ones may be added in the future as the need arises.
</blockquote>

<h2>New <code>send()</code> method for the mail service</h2>

<p>
<b>Gaelyk</b> adds a new <code>send()</code> method to the 
<a href="http://code.google.com/intl/fr-FR/appengine/docs/java/javadoc/com/google/appengine/api/mail/MailService.html">mail service</a>,
which takes <i>named arguments</i>. That way, you don't have to manually build a new message yourself.
In your Groovlet, for sending a message, you can do this:
</p>

<pre class="brush:groovy">
    mailService.send to: 'foobar@gmail.com',
            subject: 'Hello World',
            htmlBody: '<bold>Hello</bold>'
</pre>

<p>
Similarily, a <code>sendToAdmins()</code> method was added to, for sending emails to the administrators of the application.
</p>

<h2>Improvements to the low-level datastore API</h2>

<p>
Although it's possible to use JDO and JPA in Google App Engine, as explained in the <i>Views and Controllers</i> section,
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

<h3>Added <code>withTransaction()</code> method on the datastore service</h3>

<p>
Last but not least, if you want to work with transactions, instead of using the <code>beginTransaction()</code> 
method of <code>DataService</code>, then the <code>commit()</code> and <code>rollback()</code> methods on that <code>Transaction</code>,
and doing the proper transaction handling yourself, you can use the <code>withTransaction()</code> method
that <b>Gaelyk</b> adds on <code>DataService</code> and which takes care of that boring task for you:
</p>

<pre class="brush:groovy">
    datastoreService.withTransaction {
        // do stuff with your entities within the transaction
    }
</pre>

<p>
The <code>withTransaction()</code> method takes a closuer as sole parameter,
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
    def query = new Query("savedscript")

    // sort results by descending order of the creation date
    query.addSort("dateCreated", Query.SortDirection.DESCENDING)

    // filters the entities so as to return only scripts by a certain author
    query.addFilter("author", Query.FilterOperator.EQUAL, params.author)

    PreparedQuery preparedQuery = datastoreService.prepare(query)

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
To be able to send messages to other users, your application will have to invite other users, and be invited to chat.
So make sure you do so for being able to send messages.
</p>

<p>
Let's see what it would look like in a Groovlet for sending messages to a user:
</p>

<pre class="brush:groovy">
    String recipient = "someone@gmail.com"

    // check if the user is online
    if (xmppService.getPresence(recipient).isAvailable()) {
        // send the message
        def status = xmppService.send to: recipient, body: "Hello, how are you?"

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
    if (xmppService.getPresence(recipient).isAvailable()) {
        // send the message
        def status = xmppService.send to: recipient, xml: {
            customers {
                customer(id: 1) {
                    name 'Google'
                }
            }
        }

        // checks the message was successfully delivered to the service
        assert status.isSuccessful()
    }
</pre>

<blockquote>
<b>Implementation detail: </b> the closure associated with the <code>xml</code> attribute is actually passed to
an instance of <a href="http://groovy.codehaus.org/Reading+XML+using+Groovy's+XmlSlurper"><code>XmlSlurper</code></a>
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
The XMPP servlet will look for a Groovy script named <code>xmpp.groovy</code> in <code>/WEB-INF/groovy</code>
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
