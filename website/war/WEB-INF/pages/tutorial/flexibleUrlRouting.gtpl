<title>Flexible URL routing</title>

<h1>Flexible URL routing</h1>

<p>
<b>Gaelyk</b> provides a flexible and powerful URL routing system:
you can use a small Groovy Domain-Specific Language for defining routes for nicer and friendlier URLs.
</p>

<a name="configuration"></a>
<h2>Configuring URL routing</h2>

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

<blockquote>
<b>Warning: </b> The filter is stopping the chain filter once a route is found.
So you should ideally put the route filter as the last element of the chain.
</blockquote>

<a name="route-definition"></a>
<h2>Defining URL routes</h2>

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
    <li>specify a handler for incoming email messages</li>
    <li>specify a handler for incoming jabber messages</li>
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

<a name="email-and-jabber"></a>
<h2>Incoming email and jabber messages</h2>

<p>
Two special routing rules exist for defining handlers dedicated to receiving incoming email messages and jabber messages.
</p>

<pre class="brush:groovy">
    email  to: "/receiveEmail.groovy"
    jabber to: "/receiveJabber.groovy"
</pre>

<p>
    jabber chat, to: "/receiveJabber.groovy" // synonym of jabber to: "..."

    // for Jabber subscriptions
    jabber subscription, to: "/subs.groovy"

    // for Jabber user presence notifications
    jabber presence, to: "/presence.groovy"
</p>

<blockquote>
<b>Note: </b> Those two notations are actually equivalent to:
<pre class="brush:groovy">
    post "/_ah/mail/*", forward: "/receiveEmail.groovy"
    post "/_ah/xmpp/message/chat/", forward: "/receiveJabber.groovy"
</pre>
Should upcoming App Engine SDK versions change the URLs, you would still be able to define routes for those handlers,
till a new version of <b>Gaelyk</b> is released with the newer paths.
</blockquote>

<blockquote>
<b>Note: </b> Make sure to read the sections on
<a href="/tutorial/app-engine-shortcuts#incoming-mail">incoming email messages</a> and
<a href="/tutorial/app-engine-shortcuts#jabber-receiving">incoming jabber messages</a>.
</blockquote>

<a name="wildcards"></a>
<h2>Using wildcards</h2>

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

<a name="warmup"></a>
<h2>Warmup requests</h2>

<p>
When an application running on a production instance receives too many incoming requests,
App Engine will spawn a new server instance to serve your users.
However, the new incoming requests were routed directly to the new instance,
even if the application wasn't yet fully initialized for serving requests,
and users would face the infamous "loading request" issue, with long response times,
as the application needed to be fully initialized to be ready to serve those requests.
Thanks to "warmup requests", Google App Engine does a best effort at honoring the time an application needs
to be fully started, before throwing new incoming requests to that new instance.
</p>

<p>
Warmup requests are enabled by default, and new traffic should be directed to new application instances
only when the following artefacts are initialized:
</p>

<ul>
    <li>
        Servlets configured with <code>load-on-startup</code> and their
        <code>void init(ServletConfig)</code> method was called.
    </li>
    <li>
        Servlet filters have had their <code>void init(FilterConfig)</code> method was called.
    </li>
    <li>
        Servlet context listeners have had their <code>void contextInitialized(ServletContextEvent)</code>
        method was called.
    </li>
</ul>

<blockquote>
<b>Note: </b> Please have a look at the documentation regarding
"<a href="http://code.google.com/appengine/docs/java/config/appconfig.html#Warming_Requests">warmup requests</a>".
Please also note that you can also enable billing and activate an option to reserve 3 warm JVMs ready to serve your requests.
</blockquote>

<p>
So to benefit from "warmup requests", the best approach is to follow those standard initialization procedures.
However, you can also define a special Groovlet handler for those warmup requests through the URL routing mechanism.
Your Groovlet will be responsible for the initialization phase your application may be needing.
To define a route for the "warmup requests", you can procede as follows:
</p>

<pre class="brush:groovy">
    all "/_ah/warmup", forward: "/myWarmupRequestHandler.groovy"
</pre>

<a name="path-variables"></a>
<h2>Using path variables</h2>

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

<a name="path-variable-validation"></a>
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

<p>
In addition to the path variables, you also have access to the <code>request</code> from within the validation closure.
For example, if you wanted to check that a particular attribute is present in the request,
like checking a user is registered to access a message board, you could do:
</p>

<pre class="brush:groovy">
    get "/message-board",
        forward: "/msgBoard.groovy",
        validate: { request.registered == true }
</pre>

<a name="capability-routing"></a>
<h2>Capability-aware routing</h2>

<p>
With Google App Engine's capability service, it is possible to programmatically decide what your application
is supposed to be doing when certain services aren't functionning as they should be or are scheduled for maintenance.
For instance, you can react upon the unavailability of the datastore, etc.
With this mechanism available, it is also possible to customize your routes to cope with the various statuses
of the available App Engine services.
</p>

<blockquote>
<b>Note: </b> Please make sure to have a look at the <a href="tutorial.gtpl#capabilities">capabilities support</a> provided by <b>Gaelyk</b>.
</blockquote>

<p>
To leverage this mechanism, instead of using a simple string representing the redirect or forward destination of a route,
you can also use a closure with sub-rules defining the routing, depending on the status of the services:
</p>

<pre class="brush:groovy">
    import static com.google.appengine.api.capabilities.Capability.*
    import static com.google.appengine.api.capabilities.CapabilityStatus.*

    get "/update", forward: {
        to "/update.groovy"
        to("/maintenance.gtpl").on(DATASTORE)      .not(ENABLED)
        to("/readonly.gtpl")   .on(DATASTORE_WRITE).not(ENABLED)
    }
</pre>

<p>
In the example above, we're passing a closure to the forward parameter.
There is a mandatory default destination defined: <code>/update.groovy</code>,
that is chosen if no capability-aware sub-rule matches.
</p>

<blockquote>
<b>Important: </b> The sub-rules are checked in the order they are defined: so the first one matching will be applied.
If none matches, the default destination will be used.
</blockquote>

<p>
The sub-rules are represented in the form of chained method calls:
</p>

<ol>
    <li>A destination is defined with the <code>to("/maintenance.gtpl")</code> method.</li>
    <li>Then, an <code>on(DATASTORE)</code> method tells which capability should the rule be checked against.</li>
    <li>
        Eventually, the <code>not(ENABLED)</code> method is used
        to check if the <code>DATASTORE</code> is <code>not</code> in the status <code>ENABLED</code>.
    </li>
</ol>

<blockquote>
<b>Tip: </b> If you're using Groovy 1.8-beta-2 and beyond, you'll be able to use an even nicer syntax,
with fewer punctuation marks:
<pre class="brush:groovy">
    import static com.google.appengine.api.capabilities.Capability.*
    import static com.google.appengine.api.capabilities.CapabilityStatus.*

    get "/update", forward: {
        to "/update.groovy"
        to "/maintenance.gtpl" on DATASTORE       not ENABLED
        to "/readonly.gtpl"    on DATASTORE_WRITE not ENABLED
    }
</pre>
</blockquote>

<p>
The last method of the chain can be either <code>not()</code>, as in our previous examples, or <code>is()</code>.
For example, you can define a sub-rule for the case where a scheduled maintenance window is planned:
</p>

<pre class="brush:groovy">
    // using Groovy-1.8-beta-2+ syntax:
    to "/urlFetchMaintenance.gtpl" on URL_FETCH is SCHEDULED_MAINTENANCE
</pre>

<p>
The following capabilities are available, as defined as constants in the
<a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/capabilities/Capability.html">Capability</a> class:
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
The available status capabilities, as defined on the
<a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/capabilities/CapabilityStatus.html">CapabilityStatus</a> enum,
are as follows:
</p>

<ul>
    <li>ENABLED</li>
    <li>DISABLED</li>
    <li>SCHEDULED_MAINTENANCE</li>
    <li>UNKNOWN</li>
</ul>

<a name="ignore"></a>
<h2>Ignoring certain routes</h2>

<p>
As a fast path to bypass certain URL patterns, you can use the <code>ignore: true</code> parameter in your route definition:
</p>

<pre class="brush:groovy">
    all "/_ah/**", ignore: true
</pre>

<a name="caching"></a>
<h2>Caching groovlet and template output</h2>

<p>
<b>Gaelyk</b> provides support for caching groovlet and template output,
and this be defined through the URL routing system.
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

<a name="cacheclear"></a>
<p>
It is possible to clear the cache for a given URI if you want to provide a fresher page to your users:
</p>

<pre class="brush:groovy">
    memcache.clearCacheForUri('/breaking-news')
</pre>

<blockquote>
<b>Note: </b> There are as many cache entries as URIs with query strings.
So if you have <code>/breaking-news</code> and <code>/breaking-news?category=politics</code>,
you will have to clear the cache for both, as <b>Gaelyk</b> doesn't track all the query parameters.
</blockquote>

<a name="namespace-scoped"></a>
<h3>Namespace scoped routes</h3>

<p>
Another feature of the URL routing system, with the combination of Google App Engine's namespace handling support,
is the ability to define a namespace, for a given route.
This mechanism is particularly useful when you want to segregate data for a user, a customer, a company, etc.,
i.e. as soon as you're looking for making your application multitenant.
Let's see this in action with an example:
</p>

<pre class="brush:groovy">
    post "/customer/@cust/update", forward: "/customerUpdate.groovy?cust=@cust", namespace: { "namespace-\$cust" }
</pre>

<p>
For the route above, we want to use a namespace per customer.
The <code>namespace</code> closure will be called for each request to that route,
returning the name of the namespace to use, in the scope of that request.
If the incoming URI is <code>/customer/acme/update</code>, the resulting namespace used for that request
will be <code>namespace-acme</code>.
</p>

<blockquote>
<b>Note: </b> Make sure to have a look at the
<a href="/tutorial/app-engine-shortcuts#namespace">namespace support</a> also built-in <b>Gaelyk</b>.
</blockquote>
