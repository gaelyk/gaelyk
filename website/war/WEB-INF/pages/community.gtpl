<html>
<head>
    <title>Community</title>
</head>
<body>

<h1>Community</h1>

<p>
In this community section, you'll find links to:
</p>
<ul>
    <li>Participate in the <a href="#discuss">discussions</a> of our Google Group</li>
    <li>Contribute code through GitHub and follow the latest activity in our code <a href="#repository">repository</a></li>
    <li>File feature requests and bugs in our <a href="#bugtracker">bug tracker</a></li>
</ul>

<a name="discuss"></a>
<h2>Discussions</h2>

<p>
If you want to discuss about <b>Gaelyk</b>, ask questions, suggest new features, and more,
you can join the <a href="http://groups.google.com/group/gaelyk">Gaelyk Google Group</a>
</p>

<table id="google-group-subscribe" class="roundPinkBorder" cellspacing="0">
    <tr>
        <td>
            <img src="http://groups.google.com/intl/en/images/logos/groups_logo_sm.gif"
                height=30 width=151 alt="Google Groupes">
        </td>
    </tr>
    <tr>
        <td style="padding-left: 5px"> <b>Subscribe to the Gaelyk Google Group</b> </td>
    </tr>
    <form action="http://groups.google.com/group/gaelyk/boxsubscribe">
    <tr>
        <td style="padding-left: 5px; border-bottom: 0px;"> Email : <input type=text name=email>
            <input type=submit name="sub" value="Abonner">
        </td>
    </tr>
    </form>
</table>

<h3>Latest messages in our <a href="http://groups.google.com/group/gaelyk">discussion group</a></h3>

<% include "/latestmessages.groovy" %>

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

<a name="repository"></a>
<h3>Latest activity on our <a href="https://github.com/glaforge/gaelyk/commits/master">repository</a></h3>

<% // include "/latestcommits.groovy" %>

<a name="bugtracker"></a>
<h3>Open issues in our <a href="https://github.com/glaforge/gaelyk/issues">bug tracker</a></h3>

<% // include "/latestissues.groovy" %>

</body>
</html>