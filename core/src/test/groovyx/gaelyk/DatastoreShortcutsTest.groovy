package groovyx.gaelyk

import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.Query
import com.google.appengine.api.datastore.Transaction
import com.google.appengine.api.datastore.AsyncDatastoreService
import com.google.appengine.api.datastore.Key
import java.util.concurrent.Future

/**
 * @author Guillaume Laforge
 */
class DatastoreShortcutsTest extends GroovyTestCase {
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

    void testKeysGet() {
        use (GaelykCategory) {
            Key k1 = new Entity('articles').with {
                title = "article one"
                save()
            }

            Key k2 = new Entity('articles').with {
                title = "article two"
                save()
            }

            def e = k1.get()
            assert e.title == "article one"

            def map = [k1, k2].get()
            assert map[k1].title == "article one"
            assert map[k2].title == "article two"
        }
    }

    void testAsynchronous() {
        def datastore = DatastoreServiceFactory.datastoreService

        use (GaelykCategory) {
            assert datastore.async instanceof AsyncDatastoreService

            def dog = new Entity('animal')
            dog.name = "Medor"
            Future<Key> fkDog = dog.asyncSave()

            def cat = new Entity('animal')
            cat.name = "Minou"
            Future<Key> fkCat = cat.asyncSave()

            def (Key kDog, Key kCat) = [fkDog, fkCat]*.get()

            assert datastore.prepare( new Query('animal') ).countEntities() == 2

            Future<Void> deletedDogFuture = dog.asyncDelete()
            Future<Void> deletedCatFuture = kCat.asyncDelete()

            [deletedDogFuture, deletedCatFuture]*.get()

            assert datastore.prepare( new Query('animal') ).countEntities() == 0
        }
    }

    void testTransparentHandlingOfTextTypeOnEntityies() {
        use (GaelykCategory) {
            def key = new Entity('articles').with {
                shortText = "this is a short text"
                longText = "Z" * 1000
                save()
            }

            def entity = key.get()

            assert entity.shortText instanceof String
            assert entity.shortText.size() == 20

            assert entity.longText instanceof String
            assert entity.longText.size() == 1000
        }
    }
}
