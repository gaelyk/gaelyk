package groovyx.gaelyk.datastore

import groovyx.gaelyk.GaelykCategory
import com.google.appengine.api.datastore.Entity
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import groovy.transform.Canonical

/**
 * @author Guillaume Laforge
 */
class PogoEntityCoercionTest extends GroovyTestCase {
    // setup the local environment stub services
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

    void testReflectionOnPogo() {
        def p1 = new P1()
        def props = PogoEntityCoercion.props(p1)
        
        assert props.s1.unindexed()
        assert !props.s1.ignore()
        assert !props.s1.key()

        assert !props.s2.unindexed()
        assert props.s2.ignore()
        assert !props.s2.key()

        assert !props.s3.unindexed()
        assert !props.s3.ignore()
        assert props.s3.key()

        assert PogoEntityCoercion.findKey(props) == 's3'
		
		def p2 = new P2()
		def props2 = PogoEntityCoercion.props(p2)
        assert props.s1.unindexed()
        assert !props.s2.unindexed()
    }

    void testObjectToEntityConversion() {
        def p1 = new Person('glaforge', 'Guillaume', 'Laforge', 'Groovy Project Manager')

        use(GaelykCategory) {
            def e = p1 as Entity
            
            assert e.getKind() == 'Person'
            assert e.getKey().getName() == 'glaforge'
            assert !e.hasProperty('fullName')
            assert e.isUnindexedProperty('bio')

            def p2 = e as Person

            assert p1 == p2

            def nullKeyConversionTest = new Person(firstName: p1.firstName) as Entity
            assert p1.firstName == nullKeyConversionTest.firstName
        }
    }

    void testEntityToObjectConversion() {
        use(GaelykCategory) {
            def e1 = new Entity('Person', 'glaforge')
            e1.with {
                firstName = 'Guillaume'
                lastName = 'Laforge'
                bio = 'Groovy Project Manager'
                save()
            }
            
            def p = e1 as Person
            
            assert p.firstName == 'Guillaume'
            assert p.lastName == 'Laforge'
            assert p.fullName == 'Guillaume Laforge'
            assert p.login == 'glaforge'
            
            def e2 = p as Entity
            
            assert e1 == e2
            assert e1.key.name == e2.key.name
        }
    }
    
    void testLongKey() {
        use (GaelykCategory) {
            def ba = new BankAccount(1234, 100.00, "main account")
            
            def e = ba as Entity
            
            assert e.key.id == 1234
            assert e.balance == 100.00
            assert e.name == "main account"
            
            def ba2 = e as BankAccount
            
            assert ba2.id == 1234
            assert ba2.balance == 100.00
            assert ba2.name == "main account"
        }
    }

    void testEnumProperties() {
        use (GaelykCategory) {
            Entity e = new Entity('Match')
            e.outcome = 'WIN'
            Match m = e as Match

            assert m.outcome == MatchOutcome.WIN

            Entity e2 = m as Entity

            assert e2.outcome == 'WIN'
        }
    }
}

class P1 {
    @Unindexed String s1
    @Ignore String s2
    @Key String s3
}

@groovyx.gaelyk.datastore.Entity(unindexed=false)
class P2 {
	String s1
	@Indexed s2
}

@Canonical
class Person {
    @Key String login
    String firstName
    String lastName
    @Unindexed String bio
    @Ignore String getFullName() { "$firstName $lastName" } 
}

@Canonical
class BankAccount {
    @Key long id
    double balance
    String name
} 

class TeddyBear {
    String name
}

@Canonical
class Match {
    MatchOutcome outcome
}

enum MatchOutcome {
    WIN, DRAW, LOSE
}

