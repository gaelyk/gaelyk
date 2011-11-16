<a name="jabber"></a>
<h2>XMPP/Jabber support</h2>

<p>
Your application can send and receive instant messaging through XMPP/Jabber.
</p>

<blockquote>
<b>Note: </b> You can learn more about
<a href="http://code.google.com/appengine/docs/java/xmpp/overview.html">XMPP support</a> on the online documentation.
</blockquote>

<a name="jabber-sending"></a>
<h3>Sending messages</h3>

<p>
<b>Gaelyk</b> provides a few additional methods to take care of sending instant messages, get the presence of users,
or to send invitations to other users.
Applications usually have a corresponding Jabber ID named after your application ID, such as <code>yourappid@appspot.com</code>.
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

<a name="jabber-receiving"></a>
<h3>Receiving messages</h3>

<p>
It is also possible to receive messages from users.
For that purpose, <b>Gaelyk</b> lets you define a Groovlet handler that will be receiving the incoming messages.
To enable the reception of messages, you'll have to do two things:
</p>

<ul>
    <li>add a new configuration fragment in <code>/WEB-INF/appengine-web.xml</code></li>
    <li>add a route for the Groovlet handler in <code>/WEB-INF/routes.groovy</code></li>
</ul>

<p>As a first step, let's configure <code>appengine-web.xml</code> by adding this new element:</p>

<pre class="brush:xml">
    &lt;inbound-services&gt;
        &lt;service&gt;xmpp_message&lt;/service&gt;
    &lt;/inbound-services&gt;
</pre>

<p>
Similarily to the incoming email support, you can define security constraints:
</p>

<pre class="brush:xml">
    ...
    &lt;!-- Only allow the SDK and administrators to have access to the incoming jabber endpoint --&gt;
    &lt;security-constraint&gt;
        &lt;web-resource-collection&gt;
            &lt;url-pattern&gt;/_ah/xmpp/message/chat/&lt;/url-pattern&gt;
        &lt;/web-resource-collection&gt;
        &lt;auth-constraint&gt;
            &lt;role-name&gt;admin&lt;/role-name&gt;
        &lt;/auth-constraint&gt;
    &lt;/security-constraint&gt;
    ...
</pre>

<p>Then let's add the route definition in <code>routes.groovy</code>:</p>

<pre class="brush:groovy">
    jabber to: "/receiveJabber.groovy"
</pre>

<p>
Alternatively, you can use the longer version:
</p>

<pre class="brush:groovy">
    jabber chat, to: "/receiveJabber.groovy"
</pre>

<blockquote>
<b>Remark: </b> You are obviously free to change the name and path of the Groovlet.
</blockquote>

<p>
All the incoming Jabber/XMPP messages will be sent through the request of your Groovlet.
Thanks to the <code>parseMessage(request)</code> method on the <code>xmpp</code> service
injected in the binding of your Groovlet, you'll be able to access the details of a
<a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/xmpp/Message.html"><code>Message</code></a>
instance, as shown below:
</p>

<pre class="brush:groovy">
    def message = xmpp.parseMessage(request)

    log.info "Received from \${message.from} with body \${message.body}"

    // if the message is an XML document instead of a raw string message
    if (message.isXml()) {
        // get the raw XML string
        message.stanza

        // or get a document parsed with XmlSlurper
        message.xml
    }
</pre>

<a name="jabber-presence"></a>
<h3>XMPP presence handling</h3>

<p>
To be notified of users' presence, you should first configure <code>appengine-web.xml</code>
to specify you want to activate the incoming presence service:
</p>

<pre class="brush:xml">
    &lt;inbound-services&gt;
        &lt;service&gt;xmpp_presence&lt;/service&gt;
    &lt;/inbound-services&gt;
</pre>

<p>
Then, add a special route definition in <code>routes.groovy</code>:
</p>

<pre class="brush:groovy">
    jabber presence, to: "/presence.groovy"
</pre>

<blockquote>
<b>Remark: </b> You are obviously free to change the name and path of the Groovlet handling the presence requests.
</blockquote>

<p>
Now, in your <code>presence.groovy</code> Groovlet, you can call the overriden <code>XMPPService#parsePresence</code> method:
</p>

<pre class="brush:groovy">
    // parse the incoming presence from the request
    def presence = xmpp.parsePresence(request)

    log.info "\${presence.fromJid.id} is \${presence.available ? '' : 'not'} available"
</pre>

<a name="jabber-subscription"></a>
<h3>XMPP subscription handling</h3>

<p>
To be notified of subscriptions, you should first configure <code>appengine-web.xml</code>
to specify you want to activate the incoming subscription service:
</p>

<pre class="brush:xml">
    &lt;inbound-services&gt;
        &lt;service&gt;xmpp_subscribe&lt;/service&gt;
    &lt;/inbound-services&gt;
</pre>

<p>
Then, add a special route definition in <code>routes.groovy</code>:
</p>

<pre class="brush:groovy">
    jabber subscription, to: "/subscription.groovy"
</pre>

<blockquote>
<b>Remark: </b> You are obviously free to change the name and path of the Groovlet handling the subscription requests.
</blockquote>

<p>
Now, in your <code>subscription.groovy</code> Groovlet, you can call the overriden <code>XMPPService#parseSubscription</code> method:
</p>

<pre class="brush:groovy">
    // parse the incoming subscription from the request
    def subscription = xmpp.parseSubscription(request)

    log.info "Subscription from \${subscription.fromJid.id}: \${subscription.subscriptionType}}"
</pre>