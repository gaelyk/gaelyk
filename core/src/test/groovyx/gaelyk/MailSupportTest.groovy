package groovyx.gaelyk

import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.api.mail.MailService
import com.google.appengine.api.mail.MailServiceFactory
import java.util.logging.Logger
import java.util.logging.Filter
import java.util.logging.LogRecord

/**
 * Test the mail service enhancements.
 *
 * @author Guillaume Laforge
 */
class MailSupportTest extends GroovyTestCase {

    // setup the local environement with a mail service stub
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalMailServiceTestConfig().setLogMailBody(true)
    )

    private MailService mail

    private String logResult

    protected void setUp() {
        super.setUp()
        // setting up the local environment
        helper.setUp()

        mail = MailServiceFactory.mailService

        logResult = ""

        Logger log = Logger.getLogger("com.google.appengine.api.mail.dev.LocalMailService")
        log.filter = { LogRecord logRecord ->
            logResult += logRecord.message + '\n'
            return false
        } as Filter
    }

    protected void tearDown() {
        // uninstalling the local environment
        helper.tearDown()
        super.tearDown()
        logResult = ""
    }

    void testSend() {
        use (GaelykCategory) {
            mail.send from: "glaforge@gmail.com",
                    to: "someone@gmail.com",
                    textBody: "hello",
                    subject: "new message"
        }

        println logResult

        assert logResult.contains("glaforge@gmail.com")
        assert logResult.contains("someone@gmail.com")
        assert logResult.contains("new message")
        assert logResult.contains("hello")
    }
}
