package groovyx.gaelyk.cache

import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig
import com.google.appengine.api.memcache.MemcacheServiceFactory
import groovyx.gaelyk.GaelykCategory

/**
 * Memcache enhancements tests
 *
 * @author Guillaume Laforge
 */
class CacheHandlerTest extends GroovyTestCase {
    // setup the local environement stub services
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalMemcacheServiceTestConfig(),
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

    void testClearCacheForUri() {
        def memcache = MemcacheServiceFactory.memcacheService
        use(GaelykCategory) {
            memcache['content-for-/photos'] = '1234'
            memcache['content-type-for-/photos'] = 'text/html'

            memcache.clearCacheForUri('/photos')

            assert memcache['content-for-/photos'] == null
            assert memcache['content-type-for-/photos'] == null
        }
    }
}
