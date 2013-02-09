package groovyx.gaelyk.datastore

import groovyx.gaelyk.query.QueryDslTransformation

import java.lang.reflect.Field

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

import spock.lang.Specification

import com.google.appengine.api.datastore.EntityNotFoundException
import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.KeyFactory
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper

class EntityTransformationSpec extends Specification {

    LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())

    def setup() { helper.setUp() }
    def cleanup() { helper.tearDown() }

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
            }

            new MyPogo3(test: "foo").save()
            new MyPogo3(test: "foo").save()
            new MyPogo3(test: "bar").save()

            MyPogo3.findAll''' + argument

        expect:
        obj.size() == result

        where:
        result  | argument
        3       | '()'
        2       | '{ where test == "foo"}'
        1       | '{ where test == "bar"}'

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
	
	
	@spock.lang.Ignore
	def "Test parent"(){
		def obj = newShell().evaluate '''
            @groovy.transform.CompileStatic
            @groovyx.gaelyk.datastore.Entity
            class MyPogo8 {
				@groovyx.gaelyk.datastore.Parent com.google.appengine.api.datastore.Key parent
                @groovyx.gaelyk.datastore.Key String name
                @groovyx.gaelyk.datastore.Indexed String test
            }

			com.google.appengine.api.datastore.Entity myparent = ['MyParent', 'parent']
			myparent.save()

            def mypogo = new MyPogo8(name: "name", test: "foo", parent: myparent.key)
			mypogo.save()
			mypogo
'''

		expect:
		obj.parent == KeyFactory.createKey('MyParent', 'parent')
	}


    private GroovyShell newShell() {
        CompilerConfiguration cc = new CompilerConfiguration()
        cc.addCompilationCustomizers(new ASTTransformationCustomizer(new QueryDslTransformation()))
        new GroovyShell(cc)
    }
}
