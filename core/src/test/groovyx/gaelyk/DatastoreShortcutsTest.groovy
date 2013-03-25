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
import com.google.appengine.api.datastore.KeyFactory

/**
 * @author Guillaume Laforge
 */
class DatastoreShortcutsTest extends GroovyTestCase {
    // setup the local environment stub services
    // HEAD
    private LocalServiceTestHelper helper = makeHelper() 

    static LocalServiceTestHelper makeHelper() {
        def helperConfig = new LocalDatastoreServiceTestConfig()
        helperConfig.defaultHighRepJobPolicyRandomSeed = 1L
        helperConfig.defaultHighRepJobPolicyUnappliedJobPercentage = 0.0f
        return new LocalServiceTestHelper(helperConfig)
    }
    // groovy2, let's see what's the right one
    // private LocalServiceTestHelper helper = new LocalServiceTestHelper()


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
        def p1 = new Entity('photo')
        p1 << [title: 'parent pic 1']
        p1.save()
        assert datastore.prepare( new Query('photo') ).countEntities() == 1

        def p2 = new Entity('photo')
        p2 << [title: 'parent pic 2']
        p2.save()
        assert datastore.prepare( new Query('photo') ).countEntities() == 2

        def c1 = new Entity('photo', p1.key)
        c1 << [title: 'child pic 1']
        c1.save()
        assert datastore.prepare( new Query('photo') ).countEntities() == 3

        def c2 = new Entity('photo', p2.key)
        c2 << [title: 'child pic 2']
        c2.save()
        assert datastore.prepare( new Query('photo') ).countEntities() == 4
    }

    void testDatastoreOperationMemoization() {
        def datastore = DatastoreServiceFactory.datastoreService

        def p1 = new Entity('photo')
        p1 << [title: 'first pic']
        p1.save()

        assert datastore.prepare(new Query('photo')).countEntities() == 1

        def key = p1.key
        key.delete()

        assert datastore.prepare(new Query('photo')).countEntities() == 0

        p1.save()

        assert datastore.prepare(new Query('photo')).countEntities() == 1

        p1.delete()

        assert datastore.prepare(new Query('photo')).countEntities() == 0

        def now = new Date()
        p1 << [date: now, author: "me", focal: 1.6f]

        p1.save()

        p1['focal'] = 5.2f
        p1.save()

        def found = datastore.prepare(new Query('photo')).asSingleEntity()
        assert found

        assert found.focal == 5.2f
        assert found['author'] == "me"
        assert found.date == now

        assert datastore.prepare(new Query('photo')).countEntities() == 1

        shouldFail {
            datastore.withTransaction { Transaction transaction ->
                def p2 = new Entity('photo')
                p2.title = "Another short"
                p2.save()
                throw new Exception("boom")
            }
        }

        assert datastore.prepare(new Query('photo')).countEntities() == 1

        datastore.withTransaction { Transaction transaction ->
            def p3 = new Entity('photo')
            p3.title = "Last short"
            p3.save()
        }

        assert datastore.prepare(new Query('photo')).countEntities() == 2
    }

    void testKeysGet() {
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

    void testAsynchronous() {
        def datastore = DatastoreServiceFactory.datastoreService

        assert datastore.async instanceof AsyncDatastoreService

        def dog = new Entity('pets')
        dog.name = "Medor"
        Future<Key> fkDog = dog.asyncSave()

        def cat = new Entity('pets')
        cat.name = "Minou"
        Future<Key> fkCat = cat.asyncSave()

        def (Key kDog, Key kCat) = [fkDog, fkCat]*.get()

        assert datastore.prepare(new Query('pets')).countEntities() == 2

        Future<Void> deletedDogFuture = dog.asyncDelete()
        Future<Void> deletedCatFuture = kCat.asyncDelete()

        [deletedDogFuture, deletedCatFuture]*.get()

        assert datastore.prepare(new Query('pets')).countEntities() == 0
    }

    void testTransparentHandlingOfTextTypeOnEntityies() {
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

    void testKeyCoercion() {
        def parentKey = KeyFactory.createKey('person', 1234)

        Key k1 = [parentKey, 'address', 333] as Key
        Key k2 = [parentKey, 'address', 'name'] as Key
        Key k3 = ['address', 444] as Key
        Key k4 = ['address', 'name'] as Key

        assert k1.id == 333
        assert k1.parent == parentKey
        assert k1.kind == 'address'

        assert k2.name == 'name'
        assert k2.parent == parentKey
        assert k2.kind == 'address'

        assert k3.id == 444
        assert k3.kind == 'address'

        assert k4.name == 'name'
        assert k4.kind == 'address'
    }

    void testOverloadedGetMethods() {
        def pk = new Entity('person').with {
            name = 'Guillaume'
            age = 34
            save()
        }

        new Entity('address', 'home', pk).with {
            street = 'main street'
            city = 'Paris'
            save()
        }

        new Entity('address', 1234, pk).with {
            street = 'other street'
            city = 'New York'
            save()
        }

        new Entity('animal', 'Felix').with {
            breed = 'siamese'
            save()
        }

        new Entity('animal', 2345).with {
            breed = 'dog'
            save()
        }

        [DatastoreServiceFactory.datastoreService, DatastoreServiceFactory.datastoreService.async].each { datastore ->
            assert datastore.get(pk, 'address', 'home').city == 'Paris'
            assert datastore.get(pk, 'address', 1234).city == 'New York'
            assert datastore.get('animal', 'Felix').breed == 'siamese'
            assert datastore.get('animal', 2345).breed == 'dog'
        }
    }

    void testKeyStringConversions() {
        def k = ['persons', 'me'] as Key

        assert k as String == KeyFactory.keyToString(k)

        assert ((k as String) as Key) == k
        assert (("agR0ZXN0cg8LEgdwZXJzb25zIgJtZQw" as Key) as String) == "agR0ZXN0cg8LEgdwZXJzb25zIgJtZQw"
    }

    void testUnindexedPropertySetter() {
        def bio = "Groovy Project Manager working on Groovy blabla"
        def address = "Long address here"

        def person = new Entity("Person", "glaforge")
        person.name = "Guillaume Laforge"
        person.unindexed.bio = bio
        person.unindexed['address'] = address
        person.save()

        def e = (["Person", "glaforge"] as Key).get()
        assert e.bio == bio
        assert e.address == address
    }

    void testLongTextConversionWithUnindexedProperty() {
        def person = new Entity("Person", "glaforge")
        person.name = "Guillaume Laforge"
        person.unindexed.bio = "super long text " * 100
        person.save()

        assert person.bio instanceof String
    }
}
