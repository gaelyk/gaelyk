package groovyx.gaelyk

import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.api.mail.MailServiceFactory

import java.util.logging.Level;
import java.util.logging.Logger
import java.util.logging.Filter
import java.util.logging.LogRecord

import org.junit.Ignore;

/**
 * Test the mail service enhancements.
 *
 * @author Guillaume Laforge
 */
@Ignore
class MailSupportTest extends GroovyTestCase {

    // setup the local environment with a mail service stub
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalMailServiceTestConfig().setLogMailBody(true).setLogMailLevel(Level.ALL)
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

    void testSend() {
        def mail = MailServiceFactory.mailService

        mail.send from: "glaforge@gmail.com",
                to: "someone@gmail.com",
                textBody: "hello you",
                subject: "new message",
                attachment: [fileName: 'report.csv', data: '1234'.bytes]

        println logResult

        assert logResult.contains("glaforge@gmail.com")
        assert logResult.contains("someone@gmail.com")
        assert logResult.contains("new message")
        assert logResult.contains("hello you")
    }
}
