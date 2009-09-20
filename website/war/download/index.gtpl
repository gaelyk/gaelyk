<% include '/WEB-INF/includes/header.gtpl' %>

<h1>Download</h1>

<p>
You can use the <b>Gaelyk</b> JAR in combination with Groovy 1.6 or 1.7 and with the latest Google App Engine SDK.
</p>

<p>
The latest version was tested with Groovy 1.7-beta-2-SNAPSHOT,
and with the 1.2.5 version of the App Engine SDK.
</p>

<p>
But to get you started quickly, you may use a ready-made template project which bundles and configures everything.
</p>

<h2>Version 0.3 (not yet released</h2>

<ul>
    <li>Removing most of the <code>service</code> suffices in the binding variables for brevity and readibility.</li>
    <li>New methods for working with the memcache service (subscript notation, and <code>in</code> keyword support).</li>
</ul>

<h2>Version 0.2</h2>

<ul>
    <li>Gaelyk JAR: <a href="http://cloud.github.com/downloads/glaforge/gaelyk/gaelyk-0.2.jar">gaelyk-0.2.jar</a></li>
    <li>Gaelyk template project: <a href="http://cloud.github.com/downloads/glaforge/gaelyk/gaelyk-template-project-0.2.zip">gaelyk-template-project-0.2.zip</a></li>
</ul>

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

<ul>
    <li>Gaelyk JAR: <a href="http://cloud.github.com/downloads/glaforge/gaelyk/gaelyk-0.1.jar">gaelyk-0.1.jar</a></li>
    <li>Gaelyk template project: <a href="http://cloud.github.com/downloads/glaforge/gaelyk/gaelyk-template-project-0.1.zip">gaelyk-template-project-0.1.zip</a></li>
</ul>

<% include '/WEB-INF/includes/footer.gtpl' %>