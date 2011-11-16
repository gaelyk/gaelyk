<a name="channel"></a>
<h2>Channel Service improvements</h2>

<p>
For your Comet-style applications, Google App Engine provides its
<a href="http://code.google.com/appengine/docs/java/channel/">Channel service</a>.
The API being very small, beyond adding the <code>channel</code> binding variable,
<b>Gaelyk</b> only adds an additional shortcut method for sending message
with the same <code>send</code> name as Jabber and Email support (for consistency),
but without the need of creating an instance of <code>ChannelMessage</code>:
</p>

<pre class="brush:groovy">
    def clientId = "1234"
    channel.createChannel(clientId)
    channel.send clientId, "hello"
</pre>
