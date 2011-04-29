package groovyx.gaelyk

import com.google.appengine.tools.development.testing.LocalXMPPServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.api.xmpp.XMPPServiceFactory
import com.google.appengine.api.xmpp.MessageType
import com.google.appengine.api.xmpp.Message
import com.google.appengine.api.xmpp.JID
import com.google.appengine.api.xmpp.SendResponse
import javax.servlet.http.HttpServletRequest
import com.google.appengine.api.xmpp.PresenceType
import com.google.appengine.api.xmpp.SubscriptionType

/**
 * @author Guillaume Laforge
 */
class XmppTest extends GroovyTestCase {
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalXMPPServiceTestConfig()
    )

    protected void setUp() {
        super.setUp()
        helper.setUp()
    }

    protected void tearDown() {
        helper.tearDown()
        super.tearDown()
    }

    void testXmppMethods() {
        def xmpp = XMPPServiceFactory.XMPPService

        use (GaelykCategory) {
            xmpp.send to: 'foo@bar.com', from: 'me@gmail.com', body: 'hi, how are you?', type: 'CHAT'
            xmpp.send to: ['foo1@bar.com', 'foo2@bar.com'], xml: { tag1 { tag2 'val' } }, type: MessageType.GROUPCHAT

            xmpp.sendInvitation 'foo@bar.com'
            xmpp.sendInvitation 'foo@bar.com', 'me@gmail.com'

            xmpp.getPresence 'foo@bar.com'
            xmpp.getPresence 'foo@bar.com', 'me@gmail.com'

            shouldFail {
                xmpp.send to: 'foo@bar.com', body: 'a body', xml: { p 'text' }
            }
        }
    }

    void testXmlResultFromMessage() {
        def xmpp = XMPPServiceFactory.XMPPService

        use (GaelykCategory) {
            def msg = new Message(MessageType.NORMAL, true, null, "<root><foo><bar id='3'/></foo></root>",
                new JID("me@gmail.com"), new JID("you@gmail.com"))

            assert msg.xml.foo.bar.@id == 3
            assert msg.recipients == ['you@gmail.com']
        }
    }

    void testSendStatus() {
        def xmpp = XMPPServiceFactory.XMPPService

        use (GaelykCategory) {
            def status = new SendResponse()
            status.addStatus new JID('you@gmail.com'), SendResponse.Status.SUCCESS

            assert status.isSuccessful()
        }
    }

    void testPresenceParsing() {
        def xmpp = XMPPServiceFactory.XMPPService

        use (GaelykCategory) {
            def request = [getReader: { ->
                new BufferedReader(new StringReader('''\
                    --ItS1i0T-5328197
                    Content-Disposition: form-data; name="to"

                    you@you.com
                    --ItS1i0T-5328197
                    Content-Disposition: form-data; name="from"

                    me@me.com
                    --ItS1i0T-5328197
                    Content-Disposition: form-data; name="available"

                    true
                    --ItS1i0T-5328197
                    Content-Disposition: form-data; name="stanza"
                    Content-Type: text/xml

                    <presence from="me@me.com" to="you@you.com"><show/><status/></presence>
                    --ItS1i0T-5328197--'''.stripIndent()
                ))},
                getParameter: { 'available' }
            ] as HttpServletRequest

            def presence = xmpp.parsePresence(request)

            assert presence.fromJid.id == 'me@me.com'
            assert presence.toJid.id == 'you@you.com'
            assert presence.presenceType == PresenceType.AVAILABLE
        }
    }

    void testSubscriptionParsing() {
        def xmpp = XMPPServiceFactory.XMPPService

        use (GaelykCategory) {
            def request = [getReader: { ->
                new BufferedReader(new StringReader('''\
                    --000t0ti-9993931
                    Content-Disposition: form-data; name="to"

                    you@you.com
                    --000t0ti-9993931
                    Content-Disposition: form-data; name="from"

                    me@me.com
                    --000t0ti-9993931
                    Content-Disposition: form-data; name="stanza"
                    Content-Type: text/xml

                    <presence from="me@me.com" to="you@you.com" type="subscribed"/>
                    --000t0ti-9993931--'''.stripIndent()
                ))},
                getParameter: { 'subscribed' }
            ] as HttpServletRequest

            def subscription = xmpp.parseSubscription(request)

            assert subscription.fromJid.id == 'me@me.com'
            assert subscription.toJid.id == 'you@you.com'
            assert subscription.subscriptionType == SubscriptionType.SUBSCRIBED
        }

    }
}
