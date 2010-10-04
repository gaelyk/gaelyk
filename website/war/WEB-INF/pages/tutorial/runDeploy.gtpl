<% include '/WEB-INF/includes/header.gtpl' %>

<h1>Running and deploying Gaelyk applications</h1>

<a name="run"></a>
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

<a name="deploy"></a>
<h2>Deploying your application in the cloud</h2>

<p>
Once you're at the root of your application, simply run the usual deployment command:
</p>

<pre>
appcfg.sh update war
</pre>

<% include '/WEB-INF/includes/footer.gtpl' %>