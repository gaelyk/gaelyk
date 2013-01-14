package groovyx.gaelyk

import com.google.appengine.api.datastore.FetchOptions
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.api.memcache.MemcacheServiceFactory
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.Query

/**
 * Closure memoization test
 *
 * @author Guillaume Laforge
 */
class ClosureMemoizationTest extends GroovyTestCase {
    // setup the local environment stub services
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

    void testSimpleClosureMemoization() {
        def memcache = MemcacheServiceFactory.memcacheService
        int called = 123

        def c = memcache.memoize { called++ }

        assert c() == 123
        assert c() == 123
    }

    void testDatastoreOperationMemoization() {
        def memcache = MemcacheServiceFactory.memcacheService
        def datastore = DatastoreServiceFactory.datastoreService

        def p1 = new Entity('photo')
        p1 << [title: 'first pic']
        p1.save()

        def called = 0
        def countEntities = memcache.memoize { String kind ->
            called++
            datastore.prepare(new Query(kind)).countEntities(FetchOptions.Builder.withDefaults())
        }

        def nbPics = countEntities('photo')

        assert nbPics == 1 && called == 1

        def p2 = new Entity('photo')
        p2 << [title: 'second pic']
        p2.save()

        nbPics = countEntities('photo')

        assert nbPics == 1 && called == 1
    }
}
