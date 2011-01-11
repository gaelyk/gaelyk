package groovyx.gaelyk

import com.google.appengine.tools.development.testing.LocalXMPPServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.api.xmpp.XMPPServiceFactory
import com.google.appengine.api.xmpp.MessageType
import com.google.appengine.api.xmpp.Message
import com.google.appengine.api.xmpp.JID
import com.google.appengine.api.xmpp.SendResponse

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
}
