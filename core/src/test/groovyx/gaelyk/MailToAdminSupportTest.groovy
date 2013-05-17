package groovyx.gaelyk

import com.google.appengine.api.mail.MailServiceFactory
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import java.util.logging.Filter
import java.util.logging.LogRecord
import java.util.logging.Logger

/**
 * Test the mail service enhancements.
 *
 * @author Guillaume Laforge
 */
class MailToAdminSupportTest extends GroovyTestCase {

    // setup the local environment with a mail service stub
    private LocalMailServiceTestConfig localMSConfig = new LocalMailServiceTestConfig().setLogMailBody(true)//.setLogMailLevel(Level.INFO)
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
        localMSConfig
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

    void testSendToAdmins() {
        def mail = MailServiceFactory.mailService

        mail.sendToAdmins from: "glaforge@gmail.com",
                textBody: "hello admin",
                subject: "new message"
        
        assert localMSConfig.localMailService.sentMessages.size() == 1
        assert localMSConfig.localMailService.sentMessages[0].sender == "glaforge@gmail.com"
        assert localMSConfig.localMailService.sentMessages[0].subject == "new message"
        assert localMSConfig.localMailService.sentMessages[0].textBody == "hello admin"
    }
}
