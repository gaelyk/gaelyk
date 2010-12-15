import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalChannelServiceTestConfig
import groovyx.gaelyk.GaelykCategory
import com.google.appengine.api.channel.ChannelServiceFactory

/**
 * Channel service send message test
 *  
 * @author Guillaume Laforge
 */
class ChannelServiceTest extends GroovyTestCase {
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalChannelServiceTestConfig()
    )

    protected void setUp() {
        super.setUp()
        helper.setUp()
    }

    protected void tearDown() {
        helper.tearDown()
        super.tearDown()
    }

    void testSendMessage() {
        def channel = ChannelServiceFactory.channelService
        use (GaelykCategory) {
            def clientId = "1234"
            channel.createChannel(clientId)
            channel.send clientId, "hello"
        }
    }
}
