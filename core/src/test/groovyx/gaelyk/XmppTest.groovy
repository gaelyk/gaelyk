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

    void testXmlResultFromMessage() {
        def xmpp = XMPPServiceFactory.XMPPService

        def msg = new Message(MessageType.NORMAL, true, null, "<root><foo><bar id='3'/></foo></root>",
                new JID("me@gmail.com"), new JID("you@gmail.com"))

        assert msg.xml().foo.bar.@id == 3
        assert msg.recipients == ['you@gmail.com']
    }

    void testSendStatus() {
        def xmpp = XMPPServiceFactory.XMPPService

        def status = new SendResponse()
        status.addStatus new JID('you@gmail.com'), SendResponse.Status.SUCCESS

        assert status.isSuccessful()
    }

    void testPresenceParsing() {
        def xmpp = XMPPServiceFactory.XMPPService

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
        assert presence.available == true
    }

    void testPresenceParsingInProduction() {
        def xmpp = XMPPServiceFactory.XMPPService

        def request = [getReader: { ->
            new BufferedReader(new StringReader('''\
                    ------=_Part_85738709_708711.1306093405673
                    Content-Type: text/plain; charset="UTF-8"
                    Content-Disposition: form-data; name="from"

                    me@me.com
                    ------=_Part_85738709_708711.1306093405673
                    Content-Type: text/plain; charset="UTF-8"
                    Content-Disposition: form-data; name="to"

                    you@you.com
                    ------=_Part_85738709_708711.1306093405673
                    Content-Type: text/plain; charset="UTF-8"
                    Content-Disposition: form-data; name="status"

                    status message
                    ------=_Part_85738709_708711.1306093405673
                    Content-Type: text/xml; charset="UTF-8"
                    Content-Disposition: form-data; name="stanza"

                    <cli:presence from="me@me.com" to="you@you.com" xmlns:cli="jabber:client">
                    <cli:priority>24</cli:priority>
                    <caps:c node="http://mail.google.com/xmpp/client/caps" ver="1.1" ext="pmuc-v1 sms-v1 camera-v1 video-v1 voice-v1" xmlns:caps="http://jabber.org/protocol/caps"/>
                    <cli:status>status message</cli:status>
                    <x xmlns="vcard-temp:x:update">
                    <photo>e6d327cf4e5b2asdf5aebfc9ade38bd2ff6sdf</photo>
                    </x>
                    </cli:presence>
                    ------=_Part_85738709_708711.1306093405673--'''.stripIndent()
            ))},
                getParameter: { 'available' }
        ] as HttpServletRequest

        def presence = xmpp.parsePresence(request)

        assert presence.fromJid.id == 'me@me.com'
        assert presence.toJid.id == 'you@you.com'
        assert presence.presenceType == PresenceType.AVAILABLE
        assert presence.available == true
    }

    void testSubscriptionParsing() {
        def xmpp = XMPPServiceFactory.XMPPService

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

    void testSubscriptionParsingInProduction() {
        def xmpp = XMPPServiceFactory.XMPPService

        def request = [getReader: { ->
            new BufferedReader(new StringReader('''\
                    ------=_Part_85738709_708711.1306093405673
                    Content-Type: text/plain; charset="UTF-8"
                    Content-Disposition: form-data; name="from"

                    me@me.com
                    ------=_Part_85738709_708711.1306093405673
                    Content-Type: text/plain; charset="UTF-8"
                    Content-Disposition: form-data; name="to"

                    you@you.com
                    ------=_Part_85738709_708711.1306093405673
                    Content-Type: text/xml; charset="UTF-8"
                    Content-Disposition: form-data; name="stanza"

                    <presence from="me@me.com" to="you@you.com" type="subscribed"/>
                    ------=_Part_85738709_708711.1306093405673--'''.stripIndent()
            ))},
                getParameter: { 'subscribed' }
        ] as HttpServletRequest

        def subscription = xmpp.parseSubscription(request)

        assert subscription.fromJid.id == 'me@me.com'
        assert subscription.toJid.id == 'you@you.com'
        assert subscription.subscriptionType == SubscriptionType.SUBSCRIBED
    }
}
