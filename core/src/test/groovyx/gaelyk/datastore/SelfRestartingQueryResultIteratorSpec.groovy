package groovyx.gaelyk.datastore

import groovyx.gaelyk.query.QueryBuilder
import spock.lang.Specification

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper

class SelfRestartingQueryResultIteratorSpec extends Specification {

    private static final COUNT = 1000
    
    def "Test direct usage"(){
        when:
        // the name datastore is required!
        DatastoreService datastore = DatastoreServiceFactory.datastoreService
        QueryBuilder builder = datastore.build {
            from Test
        }
        
        then:
        builder instanceof QueryBuilder
        

        when:
        int count = 0
        SelfRestartingQueryResultIterator iter = SelfRestartingQueryResultIterator.from(builder)
        for(en in iter){
            count++
        }
        
        then:
        count == COUNT
    }
    
    def "Test usage with datastore and entity"(){
        when:
        // the name datastore is required!
        int count = 0
        DatastoreService datastore = DatastoreServiceFactory.datastoreService
        def tests = datastore.iterate {
            from Test
            restart automatically
        }
        for(en in tests){
            count++
        }
        
        then:
        count == COUNT
    }
    
    def "Test usage with datastore and pogo"(){
        when:
        int count = 0
        // the name datastore is required!
        DatastoreService datastore = DatastoreServiceFactory.datastoreService
        def tests = datastore.iterate {
            from Test as TestPogo
            restart automatically
        }
        for(en in tests){
            count++
        }
        
        then:
        count == COUNT
    }
    
    def "Test usage with datastore and keys"(){
        when:
        int count = 0
        // the name datastore is required!
        DatastoreService datastore = DatastoreServiceFactory.datastoreService
        def keys = datastore.iterate {
            select keys
            from Test
            restart automatically
        }
        for(key in keys){
            count++
        }
        
        then:
        count == COUNT
    }
    
    
    
    LocalServiceTestHelper services = new LocalServiceTestHelper(
        new LocalDatastoreServiceTestConfig().setMaxQueryLifetimeMs(1)
    )
    
    def setup(){ 
        services.setUp() 
        COUNT.times {
            new Entity('Test').save()
        }
    }
    def cleanup() { services.tearDown() }
    
}

class TestPogo {}
