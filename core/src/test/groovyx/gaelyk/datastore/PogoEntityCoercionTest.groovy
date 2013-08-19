package groovyx.gaelyk.datastore

import groovy.transform.Canonical

import com.google.appengine.api.datastore.Entity
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper

/**
 * @author Guillaume Laforge
 */
class PogoEntityCoercionTest extends GroovyTestCase {
    // setup the local environment stub services
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
    new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(100)
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
        def props = ReflectionEntityCoercion.props(p1)

        assert props.s1 == PropertyDescriptor.UNINDEXED
        assert props.s1.unindexed()
        assert !props.s1.ignore()
        assert !props.s1.key()
        assert !props.s1.version()
        assert !props.s1.parent()

        assert props.s2 == PropertyDescriptor.IGNORED
        assert !props.s2.unindexed()
        assert props.s2.ignore()
        assert !props.s2.key()
        assert !props.s2.version()
        assert !props.s2.parent()

        assert props.s3 == PropertyDescriptor.KEY
        assert !props.s3.unindexed()
        assert !props.s3.ignore()
        assert props.s3.key()
        assert !props.s3.version()
        assert !props.s3.parent()

        assert props.s4 == PropertyDescriptor.VERSION
        assert !props.s4.unindexed()
        assert !props.s4.ignore()
        assert !props.s4.key()
        assert props.s4.version()
        assert !props.s4.parent()

        assert props.s5 == PropertyDescriptor.IGNORED
        assert !props.s5.unindexed()
        assert props.s5.ignore()
        assert !props.s5.key()
        assert !props.s5.version()
        assert !props.s5.parent()

        assert props.s6 == PropertyDescriptor.PARENT
        assert !props.s6.unindexed()
        assert !props.s6.ignore()
        assert !props.s6.key()
        assert !props.s6.version()
        assert props.s6.parent()

        assert ReflectionEntityCoercion.findKey(props) == 's3'
    }
    
    void testReflectionOnPogoInheritance() {
        def p2 = new P2()
        def props = ReflectionEntityCoercion.props(p2)

        assert props.s1 == PropertyDescriptor.UNINDEXED
        assert props.s1.unindexed()
        assert !props.s1.ignore()
        assert !props.s1.key()
        assert !props.s1.version()
        assert !props.s1.parent()

        assert props.s2 == PropertyDescriptor.IGNORED
        assert !props.s2.unindexed()
        assert props.s2.ignore()
        assert !props.s2.key()
        assert !props.s2.version()
        assert !props.s2.parent()

        assert props.s3 == PropertyDescriptor.KEY
        assert !props.s3.unindexed()
        assert !props.s3.ignore()
        assert props.s3.key()
        assert !props.s3.version()
        assert !props.s3.parent()

        assert props.s4 == PropertyDescriptor.VERSION
        assert !props.s4.unindexed()
        assert !props.s4.ignore()
        assert !props.s4.key()
        assert props.s4.version()
        assert !props.s4.parent()

        assert props.s5 == PropertyDescriptor.IGNORED
        assert !props.s5.unindexed()
        assert props.s5.ignore()
        assert !props.s5.key()
        assert !props.s5.version()
        assert !props.s5.parent()

        assert props.s6 == PropertyDescriptor.PARENT
        assert !props.s6.unindexed()
        assert !props.s6.ignore()
        assert !props.s6.key()
        assert !props.s6.version()
        assert props.s6.parent()

        assert ReflectionEntityCoercion.findKey(props) == 's3'
    }

    void testObjectToEntityConversion() {
        def p1 = new Person('glaforge', 'Guillaume', 'Laforge', 'Groovy Project Manager')

        def e = p1 as Entity

        assert e.getKind() == 'Person'
        assert e.getKey().getName() == 'glaforge'
        assert !e.hasProperty('fullName')
        assert e.isUnindexedProperty('bio')

        def p2 = e as Person

        p1.version = p2.version

        assert p1 == p2

        def nullKeyConversionTest = new Person(firstName: p1.firstName) as Entity
        assert p1.firstName == nullKeyConversionTest.firstName
    }

    void testEntityToObjectConversion() {
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
        assert p.version

        def e2 = p as Entity

        assert e1 == e2
        assert e1.key.name == e2.key.name
    }

    void testLongKey() {
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

    void testEnumProperties() {
        Entity e = new Entity('Match')
        e.outcome = 'WIN'
        Match m = e as Match

        assert m.outcome == MatchOutcome.WIN

        Entity e2 = m as Entity

        assert e2.outcome == 'WIN'
    }

    void testEnumNullProperties() {
        Entity e = new Entity('Match')
        e.outcome = null
        Match m = e as Match

        assert m.outcome == null

        Entity e2 = m as Entity

        assert e2.outcome == null
    }

    void testProblemWithInterface(){
        Entity e = new Entity('User', 10)
        e.firstName = 'Vladimir'
        e.lastName = 'Orany'

        def user = e as User
        assert user.id == 10
    }

    void testFindVersion(){
        assert ReflectionEntityCoercion.findVersion([:]) == null
        assert ReflectionEntityCoercion.findVersion([prop: PropertyDescriptor.IGNORED]) == null
        assert ReflectionEntityCoercion.findVersion(prop: PropertyDescriptor.VERSION) == 'prop'
    }
    
    void testEntityIsCoercedToEntity(){
       Entity en = new Entity('Anything')
       assert PogoEntityCoercion.convert(en, Entity) == en
    }
    
    void testEntityIsCoercedToKey(){
        Entity en = new Entity('Anything', 100)
        assert PogoEntityCoercion.convert(en, com.google.appengine.api.datastore.Key) == en.key
     }
}

class P1 {
    @Unindexed String s1
    @Ignore String s2
    @Key String s3
    @Version long s4
    static String s5
    @Parent com.google.appengine.api.datastore.Key s6
}

class P2 extends P1{
    String s10
    @Unindexed s11
}

@Canonical
class Person {
    @Key String login
    String firstName
    String lastName
    @Unindexed String bio

    String getFullName() { "$firstName $lastName" }

    @Version long version
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

class User implements UserDetailsSocial {
    @Key Long id
    @Unindexed String firstName
    @Unindexed String lastName
}

