<% include '/WEB-INF/includes/header.gtpl' %>

<h1>Tutorial</h1>

<p>
The goal of this tutorial is to quickly get you started with using <b>Gaelyk</b> to let you write 
and deploy your Groovy applications on Google App Engine.
We'll assume you have already downloaded and installed the Google App Engine SDK of your machine.
If you haven't, please do so by reading the 
<a href="http://code.google.com/intl/fr-FR/appengine/docs/java/gettingstarted/installing.html">instructions</a> from Google.
</p>

<p>
The easiest way to get setup rapidly is to download the template project from the <a href="/download/">download section</a>.
It provides a ready-to-go project with the right configuration files pre-filled and an appropriate directory layout:
<ul>
    <li><code>web.xml</code> precondigured with the <b>Gaelyk</b> servlets</li>
    <li><code>appengine-web.xml</code> with the right settings predefined (static file directive)</li>
    <li>a sample Groovlet and template</li>
    <li>the needed JARs (Groovy, Gaelyk and Google App Engine SDK)</li>
</ul>
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
            +-- gaelyk-x.y.z.jar
            +-- groovy-all-x.y.z.jar
        
</pre>

<p>
At the root of your project, you'll find:
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
</p>

<p>
In the <code>WEB-INF</code> directory, you'll find:
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
        <code>lib</code>: All the needed libraries will be put here, the Groovy, Gaelyk and GAE SDK JARs, 
        as well as any third-party JARs you may need in your application.
    </li>
</ul>
</p>

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
            &lt;servlet-name&gt;GroovyServlet&lt;/servlet-name&gt;
            &lt;servlet-class&gt;groovyx.gaelyk.servlet.GaelykServlet&lt;/servlet-class&gt;
        &lt;/servlet&gt;
        
        &lt;!-- The Gaelyk template servlet --&gt;
        &lt;servlet&gt;
            &lt;servlet-name&gt;TemplateServlet&lt;/servlet-name&gt;
            &lt;servlet-class&gt;groovyx.gaelyk.servlet.GaelykTemplateServlet&lt;/servlet-class&gt;
        &lt;/servlet&gt;

        &lt;!-- Specify a mapping between *.groovy URLs and Groovlets --&gt;
        &lt;servlet-mapping&gt;
            &lt;servlet-name&gt;GroovyServlet&lt;/servlet-name&gt;
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
<ul>
    <li>put blocks of Groovy code inside <code>&lt;% /* some code */ %&gt;</code>,</li>
    <li>call <code>print</code> and <code>println</code> inside those scriptlets for writing to the servlet writer,</li>
    <li>use the <code>&lt;%= variable %&gt;</code> notation to insert a value in the output,</li>
    <li>or also the GString notation <code>\${variable}</code> to insert some text or value.</li>
</ul>
</p>

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
