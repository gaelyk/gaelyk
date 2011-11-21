package groovyx.gaelyk

import com.google.appengine.api.LifecycleManager
import com.google.appengine.tools.development.testing.LocalServiceTestHelper

/**
 * @author Guillaume Laforge
 */
class BackendServiceTest extends GroovyTestCase {

     // setup the local environment stub services
    private LocalServiceTestHelper helper = new LocalServiceTestHelper()

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

    void testShutdownHookClosure() {
        def lifecycle = LifecycleManager.instance

        use (GaelykCategory) {
            def shutdown = false

            lifecycle.shutdownHook = { shutdown = true }

            lifecycle.beginShutdown(0)

            assert shutdown
        }
    }
}
