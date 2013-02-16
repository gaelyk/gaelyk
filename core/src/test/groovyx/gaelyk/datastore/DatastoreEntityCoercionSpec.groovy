package groovyx.gaelyk.datastore

import spock.lang.Specification

import com.google.appengine.api.datastore.Entities
import com.google.appengine.api.datastore.Entity
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper

class DatastoreEntityCoercionSpec extends Specification {
	
    // setup the local environment stub services
    LocalServiceTestHelper helper = makeHelper() 

    static LocalServiceTestHelper makeHelper() {
            def helperConfig = new LocalDatastoreServiceTestConfig()
            helperConfig.defaultHighRepJobPolicyRandomSeed = 1L
            helperConfig.defaultHighRepJobPolicyUnappliedJobPercentage = 100f
            return new LocalServiceTestHelper(helperConfig)
    }

    def setup() {
        helper.setUp()
    }

    def cleanup() {
        helper.tearDown()
    }
    
	def "Test coercion to entity"(){
		ExampleDatastoreEntity ex = new ExampleDatastoreEntity(
            id: 15, 
            version: 1, 
            ignored: 10, 
            indexed1: 'indexed', 
            indexed2: 15,
            unindexed1: 'one',
            unindexed2: 'two',
        )
        
        expect:
        ex.hasDatastoreVersion() == true
        ex.datastoreVersion      == 1
        ex.hasDatastoreKey()     == true
        ex.datastoreKey          == 15
        ex.datastoreUnindexedProperties   == ['unindexed1', 'unindexed2'] as String[]
        ex.datastoreIndexedProperties     == ['indexed1', 'indexed2'] as String[]
        
        when:
        Entity entity = DatastoreEntityCoercion.convert(ex)
        
        then:
        entity.key.id                       == 15
        entity.getProperties().keySet()     == ['unindexed1', 'unindexed2', 'indexed1', 'indexed2'] as Set
        entity.getProperty('unindexed1')    == 'one'
        entity.getProperty('unindexed2')    == 'two'
        entity.getProperty('indexed1')      == 'indexed'
        entity.getProperty('indexed2')      == 15
        entity.isUnindexedProperty('unindexed1')
        entity.isUnindexedProperty('unindexed2')
        !entity.isUnindexedProperty('indexed1')
        !entity.isUnindexedProperty('indexed2')
	}
    
    def "Test coercion to object"(){
        Entity en = new Entity('ExampleDatastoreEntity', 15)
        en.indexed1 = 'indexed'
        en.indexed2 = 15
        en.unindexed.unindexed1 = 'one'
        en.unindexed.unindexed2 = 'two'
        en.save()
        
        when:
        ExampleDatastoreEntity ex = DatastoreEntityCoercion.convert(en, ExampleDatastoreEntity)
        
        then:
        with(ex){
            id == 15
            version != 0
            ignored == 0
            indexed1 == 'indexed'
            indexed2 == 15
            unindexed1 == 'one'
            unindexed2 == 'two'
        }
    }
	
}