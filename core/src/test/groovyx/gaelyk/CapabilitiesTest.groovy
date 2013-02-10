package groovyx.gaelyk

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory

import static com.google.appengine.api.capabilities.Capability.*
import static com.google.appengine.api.capabilities.CapabilityStatus.*

/**
 * Capabilities test
 *
 * @author Guillaume Laforge
 */
class CapabilitiesTest extends GroovyTestCase {
    // setup the local environement stub services
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalMemcacheServiceTestConfig(),
            new LocalDatastoreServiceTestConfig()
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

    void testDatastoreAndMemcacheStatus() {
        def capabilities = CapabilitiesServiceFactory.capabilitiesService

        assert capabilities[DATASTORE]          == ENABLED
        assert capabilities[DATASTORE_WRITE]    == ENABLED
        assert capabilities[MEMCACHE]           == ENABLED
    }

    void testCapabilityStatusBooleanCoercion() {
        def capabilities = CapabilitiesServiceFactory.capabilitiesService

        assert capabilities[DATASTORE] && capabilities[DATASTORE_WRITE]

        assert ENABLED
        assert !DISABLED
        assert SCHEDULED_MAINTENANCE
        assert !UNKNOWN
    }
}
