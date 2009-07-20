<% include '/WEB-INF/includes/header.gtpl' %>

<h1>Community</h1>

<h2>Discussions</h2>

<p>
If you want to discuss about <b>Gaelyk</b>, ask questions, suggest new features, and more,
you can join the <a href="http://groups.google.com/group/gaelyk">Gaelyk Google Group</a>
</p>

<h3>Latest messages in our discussion group</h3>

<div id="latestmessages">Loading <b>latest messages</b> through AJAX...</div>

<h2>Contribute</h2>

<p>
If you wish to contribute to the development of <b>Gaelyk</b>:
<ul>
    <li>
        you can do so by forking our <a href="http://github.com/glaforge/gaelyk/tree/master">repository on Github</a>
        and by providing patches,
    </li>
    <li>
        you can submit new issues or features in our <a href="http://github.com/glaforge/gaelyk/issues">issue tracker</a>.
    </li>
</ul>
</p>

<h3>Latest activity on our repository</h3>

<div id="latestcommits">Loading <b>latest commits</b> through AJAX...</div>

<h3>Open issues in our bug tracker</h3>

<div id="latestissues">Loading <b>latest issues</b> through AJAX...</div>

<% include '/WEB-INF/includes/footer.gtpl' %>

<script type="text/javascript">
    \$(document).ready(function() {
        \$("#latestmessages").load("/latestmessages.groovy");
        \$("#latestcommits").load("/latestcommits.groovy");
        \$("#latestissues").load("/latestissues.groovy");
    });
</script>

