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
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalMailServiceTestConfig().setLogMailBody(true)//.setLogMailLevel(Level.INFO)
    )

    private String logResult = ""

    protected void setUp() {
        super.setUp()
        // setting up the local environment
        helper.setUp()

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
    }

    void testSendToAdmins() {
        def mail = MailServiceFactory.mailService

        mail.sendToAdmins from: "glaforge@gmail.com",
                textBody: "hello admin",
                subject: "new message"

        println logResult

        assert logResult.contains("glaforge@gmail.com")
        assert logResult.contains("new message")
        assert logResult.contains("hello admin")
    }
}
