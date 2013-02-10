package groovyx.gaelyk

import com.google.appengine.api.datastore.Entity
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig

/**
 * Test extension methods for POGO to Entity and Entity to POGO coercions.
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
            def e = new Entity("Person")
            e.name = "Guillaume"
            e['age'] = 33

            def p = e as Person

            assert e['name'] == p.name
            assert e.age == p.age
    }

    void testPogoToEntityCoercion() {
            def p = new Person(name: "Guillaume", age: 33)
            def e = p as Entity

            assert p.name == e['name']
            assert p.age == e.age
    }

    void testCoercionWithoutAllPropertiesFilled() {
            def e = new Entity("UserPreferences")
            e['owner'] = "me"
            e.alertViaEmail = true

            assert e as UserPreferences

            def up = new UserPreferences(owner: "me", alertViaEmail: true)

            assert up as Entity
    }
}

class Person {
    String name
    int age
}

class UserPreferences {

    String owner
    String seeds
    String keywords
    boolean alertViaEmail
    boolean alertViaGTalk
    int runInterval

    String toString() {
        "owner: $owner, seeds: $seeds, keywords: $keywords, alertViaEmail: $alertViaEmail, alertViaGTalk: $alertViaGTalk, runInterval: $runInterval"
    }
}