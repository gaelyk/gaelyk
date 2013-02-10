package groovyx.gaelyk

import com.google.appengine.api.mail.dev.LocalMailService
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.api.mail.MailServiceFactory
import com.google.apphosting.api.ApiProxy

/**
 * Test the mail service enhancements.
 *
 * @author Guillaume Laforge
 */
class MailSupportTest extends GroovyTestCase {

    // setup the local environment with a mail service stub
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalMailServiceTestConfig()
    )

    protected void setUp() {
        super.setUp()
        // setting up the local environment
        helper.setUp()
    }

    protected void tearDown() {
        // uninstalling the local environment
        helper.tearDown()
        super.tearDown()
    }

    void testSend() {
        def mail = MailServiceFactory.mailService

        mail.send from: "glaforge@gmail.com",
                to: "someone@gmail.com",
                textBody: "hello you",
                subject: "new message",
                attachment: [fileName: 'report.csv', data: '1234'.bytes]

        def stubMailService = (LocalMailService) ApiProxy.delegate.getService(LocalMailService.PACKAGE)
        def sentMessage = stubMailService.sentMessages[0]

        assert sentMessage.sender == "glaforge@gmail.com"
        assert sentMessage.getTo(0) == "someone@gmail.com"

        assert sentMessage.textBody == "hello you"
        assert sentMessage.subject == "new message"
    }
}
