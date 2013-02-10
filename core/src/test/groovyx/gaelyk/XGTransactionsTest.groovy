package groovyx.gaelyk

import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query
import com.google.appengine.api.datastore.Transaction
import com.google.appengine.api.datastore.AsyncDatastoreService
import com.google.appengine.api.datastore.Key
import java.util.concurrent.Future
import com.google.appengine.api.datastore.KeyFactory

/**
 * @author Guillaume Laforge
 */
class XGTransactionsTest extends GroovyTestCase {
    // setup the local environment stub services
    private LocalServiceTestHelper helper = makeHelper() 

    static LocalServiceTestHelper makeHelper() {
			def helperConfig = new LocalDatastoreServiceTestConfig()
            helperConfig.defaultHighRepJobPolicyRandomSeed = 1L
			helperConfig.defaultHighRepJobPolicyUnappliedJobPercentage = 100f
			return new LocalServiceTestHelper(helperConfig)
    }

    protected void setUp() {
        super.setUp()
        helper.setUp()
    }

    protected void tearDown() {
        helper.tearDown()
        super.tearDown()
    }

	void testDatastoreTransactionWithBuilderOptions() {
		def datastore = DatastoreServiceFactory.datastoreService
		datastore.withTransaction(true) {
            new Entity('foo').save()
            new Entity('bar').save()
		}
		shouldFail {
            datastore.withTransaction(false){
                new Entity('foo').save()
                new Entity('bar').save()
            }
		}
        assert !DatastoreServiceFactory.datastoreService.activeTransactions
	}
}
