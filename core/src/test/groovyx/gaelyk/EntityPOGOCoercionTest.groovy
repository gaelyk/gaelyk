package groovyx.gaelyk

import com.google.appengine.api.datastore.Entity
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.api.datastore.dev.LocalDatastoreService
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig

/**
 * Test GaelykCategory's methods for POGO to Entity and Entity to POGO coercions.
 * 
 * @author Guillaume Laforge
 */
class EntityPOGOCoercionTest extends GroovyTestCase {

    // setup the local environement with a datastore service stub
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig()
    )

    protected void setUp() {
        super.setUp()
        helper.setUp()
    }

    protected void tearDown() {
        super.tearDown()
        helper.tearDown()
    }

    void testEntityToPogoCoercion() {
        use(GaelykCategory) {
            def e = new Entity("Person")
            e.name = "Guillaume"
            e.age = 33

            def p = e as Person

            assert e.name == p.name
            assert e.age == p.age
        }
    }

    void testPogoToEntityCoercion() {
        use(GaelykCategory) {
            def p = new Person(name: "Guillaume", age: 33)
            def e = p as Entity

            assert p.name == e.name
            assert p.age == e.age
        }
    }
}

class Person {
    String name
    int age
}
