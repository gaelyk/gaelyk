<a name="email"></a>
<h2>Email support</h2>

<h3>New <code>send()</code> method for the mail service</h3>

<p>
<b>Gaelyk</b> adds a new <code>send()</code> method to the
<a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/mail/MailService.html">mail service</a>,
which takes <i>named arguments</i>. That way, you don't have to manually build a new message yourself.
In your Groovlet, for sending a message, you can do this:
</p>

<pre class="brush:groovy">
    mail.send from: "app-admin-email@gmail.com",
            to: "recipient@somecompany.com",
            subject: "Hello",
            textBody: "Hello, how are you doing? -- MrG",
            attachment: [data: "Chapter 1, Chapter 2".bytes, fileName: "outline.txt"]
</pre>

<p>
Similarily, a <code>sendToAdmins()</code> method was added to, for sending emails to the administrators of the application.
</p>

<blockquote>
    <b>Note: </b> There is a <code>sender</code> alias for the <code>from</code> attribute.
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

<a name="incoming-mail"></a>
<h3>Incoming email messages</h3>

<p>
Your applications can also receive incoming email messages,
in a similar vein as the incoming XMPP messaging support.
To enable incoming email support, you first need to update your <code>appengine-web.xml</code> file as follows:
</p>

<pre class="brush:xml">
    &lt;inbound-services&gt;
        &lt;service&gt;mail&lt;/service&gt;
    &lt;/inbound-services&gt;
</pre>

<p>
In your <code>web.xml</code> file, you can eventually add a security constraint on the web handler
that will take care of treating the incoming emails:
</p>

<pre class="brush:xml">
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
You need to define a Groovlet handler for receiving the incoming emails
with a special <a href="/tutorial/url-routing#email-and-jabber">route definition</a>,
in your <code>/WEB-INF/routes.groovy</code> configuration file:
</p>

<pre class="brush:groovy">
    email to: "/receiveEmail.groovy"
</pre>

<blockquote>
<b>Remark: </b> You are obviously free to change the name and path of the Groovlet.
</blockquote>

<p>
All the incoming emails will be sent as MIME messages through the request of your Groovlet.
To parse the MIME message, you'll be able to use the <code>parseMessage(request)</code> method
on the mail service injected in the binding of your Groovlet, which returns a
<a href="http://java.sun.com/products/javamail/javadocs/javax/mail/internet/MimeMessage.html"><code>javax.mail.MimeMessage</code></a>
instance:
</p>

<pre class="brush:groovy">
    def msg = mail.parseMessage(request)

    log.info "Subject \${msg.subject}, to \${msg.allRecipients.join(', ')}, from \${msg.from[0]}"
</pre>

