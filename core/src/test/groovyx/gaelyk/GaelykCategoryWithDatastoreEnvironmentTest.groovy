package groovyx.gaelyk

import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.Query
import com.google.appengine.api.datastore.Transaction

/**
 * @author Guillaume Laforge
 */
class GaelykCategoryWithDatastoreEnvironmentTest extends GroovyTestCase {
    // setup the local environement stub services
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig()
    )

    protected void setUp() {
        super.setUp()
        helper.setUp()
    }

    protected void tearDown() {
        helper.tearDown()
        super.tearDown()
    }

    void testDatastoreOperationMemoization() {
        def datastore = DatastoreServiceFactory.datastoreService

        use (GaelykCategory) {
            def p1 = new Entity('photo')
            p1 << [title: 'first pic']
            p1.save()

            assert datastore.prepare( new Query('photo') ).countEntities() == 1

            def key = p1.key
            key.delete()

            assert datastore.prepare( new Query('photo') ).countEntities() == 0

            p1.save()

            assert datastore.prepare( new Query('photo') ).countEntities() == 1

            p1.delete()

            assert datastore.prepare( new Query('photo') ).countEntities() == 0

            def now = new Date()
            p1 << [date: now, author: "me", focal: 1.6f]

            p1.save()

            p1['focal'] = 5.2f
            p1.save()

            def found = datastore.prepare( new Query('photo') ).asSingleEntity()
            assert found

            assert found.focal == 5.2f
            assert found['author'] == "me"
            assert found.date == now

            assert datastore.prepare( new Query('photo') ).countEntities() == 1

            shouldFail {
                datastore.withTransaction { Transaction transaction ->
                    def p2 = new Entity('photo')
                    p2.title = "Another short"
                    p2.save()
                    throw new Exception("boom")
                }
            }

            assert datastore.prepare( new Query('photo') ).countEntities() == 1

            datastore.withTransaction { Transaction transaction ->
                def p3 = new Entity('photo')
                p3.title = "Last short"
                p3.save()
            }

            assert datastore.prepare( new Query('photo') ).countEntities() == 2
        }
    }
}
