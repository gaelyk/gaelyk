package groovyx.gaelyk.datastore

import groovyx.gaelyk.query.QueryDslTransformation

import java.lang.reflect.Field

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

import spock.lang.IgnoreRest;
import spock.lang.Specification

import com.google.appengine.api.datastore.EntityNotFoundException
import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.KeyFactory
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper

class EntityTransformationSpec extends Specification {

    LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())

    def setup() {
        helper.setUp()
    }
    def cleanup() {
        helper.tearDown()
    }

    def "Save and delete works"(){
        def obj = newShell().evaluate '''
            @groovyx.gaelyk.datastore.Entity
            @groovy.transform.CompileStatic
            class MyPogo1 {}

            new MyPogo1()
        '''

        expect:
        !obj.id
        obj.hasProperty('id')
        obj.getClass().declaredFields.any { Field f -> f.isAnnotationPresent(groovyx.gaelyk.datastore.Key)}

        when:
        Key key = obj.save()


        then:
        key
        key.kind == 'MyPogo1'
        key.get()
        obj.getClass().getMethod('get', long).invoke(null, key.id)
        obj.getClass().getMethod('count').invoke(null) == 1
        obj.id == key.id

        when:
        obj.delete()
        obj.getClass().getMethod('count').invoke(null) == 0
        key.get()

        then:
        thrown(EntityNotFoundException)
    }

    def "Delete by key works"(){
        def obj = newShell().evaluate '''
                    @groovy.transform.CompileStatic
                    @groovyx.gaelyk.datastore.Entity
                    class MyPogo2 {}

                    new MyPogo2()
            '''
        when:
        Key key = obj.save()


        then:
        obj.count() == 1

        when:
        obj.getClass().getMethod('delete', long).invoke(null, key.id)

        then:
        obj.count() == 0
    }

    def "Test find all with closure"(){
        def obj = newShell().evaluate '''
            @groovy.transform.CompileStatic
            @groovyx.gaelyk.datastore.Entity
            class MyPogo3 {
                @groovyx.gaelyk.datastore.Indexed String test
                
                // this method must be compiled dynamic, before DelegatesTo method is fixed
                @groovy.transform.CompileStatic(groovy.transform.TypeCheckingMode.SKIP)
                static findAllByTest(String t){
                    MyPogo3.findAll{ 
                        where 'test' == t
                    }
                }
            }

             new MyPogo3(test: "foo").save()
             new MyPogo3(test: "foo").save()
             new MyPogo3(test: "bar").save()

            MyPogo3.findAllByTest''' + argument

        expect:
        obj.size() == result

        where:
        result  | argument
        2       | '("foo")'
        1       | '("bar")'
    }

    def "Test find all with closure in script"(){
        def obj = newShell().evaluate '''
            @groovy.transform.CompileStatic
            @groovyx.gaelyk.datastore.Entity
            class MyPogo3 {
                @groovyx.gaelyk.datastore.Indexed String test                
            }

             new MyPogo3(test: "foo").save()
             new MyPogo3(test: "foo").save()
             new MyPogo3(test: "bar").save()

            MyPogo3.findAll''' + argument

        expect:
        obj.size() == result

        where:
        result  | argument
        2       | '{ where test == "foo" }'
        1       | '{ where test == "bar" }'
    }


    def "Test count "(){
        def obj = newShell().evaluate '''
            @groovy.transform.CompileStatic
            @groovyx.gaelyk.datastore.Entity
            class MyPogo4 {
                @groovyx.gaelyk.datastore.Indexed String test
            }

            new MyPogo4(test: "foo").save()
            new MyPogo4(test: "foo").save()
            new MyPogo4(test: "bar").save()

            MyPogo4.count''' + argument

        expect:
        obj == result

        where:
        result  | argument
        3       | '()'
        2       | '{ where test == "foo"}'
        1       | '{ where test == "bar"}'
    }

    def "Test iterate "(){
        def obj = newShell().evaluate '''
            @groovy.transform.CompileStatic
            @groovyx.gaelyk.datastore.Entity
            class MyPogo5 {
                @groovyx.gaelyk.datastore.Indexed String test
            }

            new MyPogo5(test: "foo").save()
            new MyPogo5(test: "foo").save()
            new MyPogo5(test: "bar").save()

            MyPogo5.iterate''' + argument

        expect:
        obj.size() == result

        where:
        result  | argument
        3       | '()'
        2       | '{ where test == "foo"}'
        1       | '{ where test == "bar"}'
    }

    def "Test find "(){
        def obj = newShell().evaluate '''
            @groovy.transform.CompileStatic
            @groovyx.gaelyk.datastore.Entity
            class MyPogo6 {
                @groovyx.gaelyk.datastore.Indexed String test
            }

            new MyPogo6(test: "foo").save()
            new MyPogo6(test: "foo").save()
            new MyPogo6(test: "bar").save()

            MyPogo6.find{ where test == "bar" }'''

        expect:
        obj
    }

    def "Test find with generic"(){
        def obj = newShell().evaluate '''
            @groovy.transform.CompileStatic
            @groovyx.gaelyk.datastore.Entity
            class MyPogo6 {
                @groovyx.gaelyk.datastore.Indexed String test
                List<String> something = new ArrayList<String>()
            }

            new MyPogo6(test: "foo").save()
            new MyPogo6(test: "foo").save()
            new MyPogo6(test: "bar").save()

            MyPogo6.find{ where test == "bar" }'''

        expect:
        obj
    }

    def "Test get by name"() {
        def obj = newShell().evaluate '''
            @groovyx.gaelyk.datastore.Entity class MyPogoWithName {

                @groovyx.gaelyk.datastore.Key String email

            }

            new MyPogoWithName(email: 'test@example.com').save()

            MyPogoWithName.get('test@example.com')
        '''

        expect:
        obj
        obj.email == 'test@example.com'
    }

    def "Test key"(){
        def obj = newShell().evaluate '''
            @groovy.transform.CompileStatic
            @groovyx.gaelyk.datastore.Entity
            class MyPogo7 {
                @groovyx.gaelyk.datastore.Key String name
                @groovyx.gaelyk.datastore.Indexed String test
            }

            new MyPogo7(name: "name", test: "foo")'''

        expect:
        !obj.hasProperty('id')
    }

    def "Test parent"(){
        def obj = newShell().evaluate '''
            @groovy.transform.CompileStatic
            @groovyx.gaelyk.datastore.Entity
            @groovy.transform.Canonical
            class MyPogo8 {
                @groovyx.gaelyk.datastore.Parent com.google.appengine.api.datastore.Key parent
                @groovyx.gaelyk.datastore.Key String name
                @groovyx.gaelyk.datastore.Indexed String test
            }

            com.google.appengine.api.datastore.Entity myparent = ['MyParent', 'parent']
            myparent.save()

            def mypogo = new MyPogo8(name: "name", test: "foo", parent: myparent.key)
            mypogo.save()

            assert MyPogo8.get(myparent.key, 'name') == mypogo
            
            try {
                MyPogo8.get('name')
                
                throw new RuntimeException('Method get(name) should not be present!')
            } catch (e) {
                // ok
            }
            mypogo
        '''

        expect:
        obj.parent == KeyFactory.createKey('MyParent', 'parent')
    }

    def "Test DatastoreEntity implementation"(){
        def obj = newShell().evaluate '''
                @groovy.transform.CompileStatic
                @groovyx.gaelyk.datastore.Entity
                class MyPogoSuper9 {
                    String superProp
                }


                @groovy.transform.CompileStatic
                @groovyx.gaelyk.datastore.Entity
                class MyPogo9 extends MyPogoSuper9 {
                @groovyx.gaelyk.datastore.Key Long name
                @groovyx.gaelyk.datastore.Indexed String test1
                @groovyx.gaelyk.datastore.Unindexed String test2
                String test3
                }
                
                new MyPogo9(name: 10, test1: "one", test2: "two", test3: "three")'''
        expect:
        obj.hasDatastoreKey() == true
        obj.hasDatastoreNumericKey() == true
        obj.getDatastoreKey() == 10
        obj.hasDatastoreVersion() == false
        obj.getDatastoreIndexedProperties() == ['test1']
        obj.getDatastoreUnindexedProperties() == [
            'test2',
            'test3',
            'superProp'
        ]
        obj.hasDatastoreParent() == false
        obj.getDatastoreParent() == null
    }
    
    def "Test inheritance"(){
        def obj = newShell().evaluate '''
                @groovy.transform.CompileStatic
                abstract class MyPogoSuper10 {
                    String superProp
                    String ignoredInChild

                    abstract boolean isLive()
                }


                @groovy.transform.CompileStatic
                @groovyx.gaelyk.datastore.Entity
                class MyPogo10 extends MyPogoSuper10 {
                    @groovyx.gaelyk.datastore.Key long id
                    @groovyx.gaelyk.datastore.Indexed String test1
                    @groovyx.gaelyk.datastore.Unindexed String test2
                    String test3

                    boolean live
                    @groovyx.gaelyk.datastore.Ignore String ignoredInChild

                    String getThisMustBeIgnored(){
                        "IGNORED"
                    }

                    String getVirtualProperty() {}
                    void   setVirtualProperty(String s) {}

                }
                
                new MyPogo10(id: 10, test1: "one", test2: "two", test3: "three")'''
        expect:
        obj.hasDatastoreKey() == true
        obj.hasDatastoreNumericKey() == true
        obj.getDatastoreKey() == 10
        obj.hasDatastoreVersion() == false
        obj.getDatastoreIndexedProperties() == ['test1']
        obj.getDatastoreUnindexedProperties() == [
            'test2',
            'test3',
            'live',
            'superProp'
        ]
        obj.hasDatastoreParent() == false
        obj.getDatastoreParent() == null
    }

    def "Test DatastoreEntity implementation 2"(){
        def obj = newShell().evaluate '''
            import groovyx.gaelyk.datastore.Key
            import groovyx.gaelyk.datastore.Entity as GE
            import groovy.transform.Canonical
            
            @Canonical @GE
            class Person {
              @Key long id
              String name
            }

            new Person(id: 15, name: 'test')'''
        expect:
        obj
    }

    def "Count entities"(){
        def obj = newShell().evaluate '''
            import groovyx.gaelyk.datastore.Key
            import groovyx.gaelyk.datastore.Entity as GE
            import groovy.transform.Canonical
            import groovyx.gaelyk.datastore.Indexed
            
            @Canonical @GE
            class Person {
              @Key long id
              @Indexed String name
            }

            new Person(id: 15, name: 'test').save()
            new Person(id: 16, name: 'tset').save()

            Person.count { where name == 'test' }
        '''
        expect:
        obj == 1
    }

    def "Id is set"(){
        def obj = newShell().evaluate '''
            import groovyx.gaelyk.datastore.Key
            import groovyx.gaelyk.datastore.Entity as GE
            import groovyx.gaelyk.datastore.Indexed
            import com.google.appengine.api.datastore.*
            
            @GE @groovy.transform.CompileStatic
            class Person {
                @Key long id
                @Indexed String name
            }

            def key = new Person(name: 'test').save()

            Entity entity = DatastoreServiceFactory.datastoreService.get('Person', key.id)
            Person pogo = entity as Person
            [key: key, entity: entity, pogo: pogo]
        '''
        expect:
        obj.pogo.id == obj.key.id
    }

    /*@spock.lang.Ignore*/
    def "Id is set 2"(){
        DatastoreEntity obj = new Order()

        expect:
        obj.hasDatastoreKey()
        obj.hasDatastoreNumericKey()

        when:
        obj.setDatastoreKey(15)

        then:
        obj.getDatastoreKey() == 15
        obj.id == 15

        when:
        obj.setDatastoreVersion(20)

        then:
        obj.getDatastoreVersion() == 20
    }



    private GroovyShell newShell() {
        CompilerConfiguration cc = new CompilerConfiguration()
        cc.addCompilationCustomizers(new ASTTransformationCustomizer(new QueryDslTransformation()))
        new GroovyShell(cc)
    }
}
