package groovyx.gaelyk

import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.api.mail.MailService
import com.google.appengine.api.mail.MailServiceFactory

/**
 * 
 * @author Guillaume Laforge
 */
class MailSupportTest extends GroovyTestCase {

    // setup the local environement with a mail service stub
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalMailServiceTestConfig().setLogMailBody(true)
    )

    private MailService mail

    protected void setUp() {
        super.setUp()
        // setting up the local environment
        helper.setUp()

        mail = MailServiceFactory.mailService
    }

    protected void tearDown() {
        // uninstalling the local environment
        helper.tearDown()
        super.tearDown()
    }

    void testSend() {
        use (GaelykCategory) {
            mail.send from: "glaforge@gmail.com", to: "someone@gmail.com", textBody: "hello"
        }
    }
}
