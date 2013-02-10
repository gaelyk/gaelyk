package groovyx.gaelyk

import javax.servlet.ServletContextEvent
import javax.servlet.ServletContext
import groovyx.gaelyk.plugins.PluginsHandler
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig
import com.google.appengine.tools.development.testing.LocalImagesServiceTestConfig
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig
import com.google.appengine.tools.development.testing.LocalXMPPServiceTestConfig
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig
import com.google.appengine.api.utils.SystemProperty

/**
 * @author Guillaume Laforge
 */
class GaelykServletContextListenerTest extends GroovyTestCase {

    // setup the local environement stub services
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig(),
            new LocalMemcacheServiceTestConfig(),
            new LocalURLFetchServiceTestConfig(),
            new LocalMailServiceTestConfig(),
            new LocalImagesServiceTestConfig(),
            new LocalUserServiceTestConfig(),
            new LocalTaskQueueTestConfig(),
            new LocalXMPPServiceTestConfig(),
            new LocalBlobstoreServiceTestConfig()
    )

    protected void setUp() {
        super.setUp()

        // setting up the local environment
        helper.setUp()

        // sets the environment to "Development"
        SystemProperty.environment.set("Development")
    }

    protected void tearDown() {
        // uninstalling the local environment
        helper.tearDown()

        super.tearDown()
    }

    void testContextListener() {
        boolean called = false
        PluginsHandler.instance.scriptContent = { String path ->
            called = true
            if (path == 'WEB-INF/plugins.groovy') {
                'install myPlugin'
            } else if (path == 'WEB-INF/plugins/myPlugin.groovy') {
                '''import javax.servlet.ServletContext
                assert servletContext in ServletContext'''
            }
        }

        def context = [:] as ServletContext
        def event = new ServletContextEvent(context)

        def listener = new GaelykServletContextListener()

        listener.contextInitialized event

        assert called
    }
    
    void testVerifyVersion(){
        assert !GaelykServletContextListener.verifyGroovyVersionInternal('3.0.0')
        assert GaelykServletContextListener.verifyGroovyVersionInternal('2.1')
        assert GaelykServletContextListener.verifyGroovyVersionInternal('2.1.0')
        assert GaelykServletContextListener.verifyGroovyVersionInternal('2.0.0')
        assert GaelykServletContextListener.verifyGroovyVersionInternal('2.0.5')
        assert !GaelykServletContextListener.verifyGroovyVersionInternal('1.8.7')
        assert !GaelykServletContextListener.verifyGroovyVersionInternal('1.7')
        
        // verify that we are building using good version
        GaelykServletContextListener.verifyGroovyVersion()
    }
}
