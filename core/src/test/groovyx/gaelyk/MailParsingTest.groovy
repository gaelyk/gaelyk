package groovyx.gaelyk

import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import javax.servlet.http.HttpServletRequest
import com.google.appengine.api.mail.MailServiceFactory
import javax.servlet.ServletInputStream

/**
 * @author Guillaume Laforge
 */
class MailParsingTest extends GroovyTestCase {

    // setup the local environement with a mail service stub
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalMailServiceTestConfig().setLogMailBody(true)
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

    void testParseIncomingEmailFromReques() {
        def request = [
                getInputStream: {->
                    new ServletInputStream() {
                        @Delegate ByteArrayInputStream internal = new ByteArrayInputStream("""\
                        To: <to@appspot.com>
                        From: from@example.com
                        Subject: Hello World
                        Date: Tue, 04 Jan 2011 11:53:24 +0100
                        Mime-Version: 1.0
                        Content-type: text/plain; charset="UTF-8"

                        Thanks a lot for the invitation""".stripIndent().getBytes())
                    }
                }
        ] as HttpServletRequest

        def mail = MailServiceFactory.mailService

        def message = mail.parseMessage(request)

        assert message.allRecipients[0].toString() == 'to@appspot.com'
        assert message.from[0].toString() == 'from@example.com'
        assert message.subject == 'Hello World'
        assert message.content.toString() == 'Thanks a lot for the invitation'
    }
}
