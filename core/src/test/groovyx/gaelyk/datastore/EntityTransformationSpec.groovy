package groovyx.gaelyk.datastore

import java.lang.reflect.Field;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;

import groovyx.gaelyk.GaelykCategory;
import groovyx.gaelyk.query.QueryDslTransformation;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper

import spock.lang.Specification;
import spock.util.mop.Use;

@Use(GaelykCategory)
class EntityTransformationSpec extends Specification {
	
	LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())
	
	def setup() { helper.setUp() }
	def cleanup() { helper.tearDown() }
	
	def "Save and delete works"(){
		def obj = newShell().evaluate '''
			@groovyx.gaelyk.datastore.Entity
			class MyPogo {}
			
			new MyPogo()
		'''
		expect:
		!obj.id
		obj.hasProperty('id')
		obj.getClass().declaredFields.any { Field f -> f.isAnnotationPresent(groovyx.gaelyk.datastore.Key)}
		
		when:
		Key key = obj.save()
		
		
		then:
		key
		key.kind == 'MyPogo'
		key.get()
		obj.getClass().getMethod('get', Object).invoke(null, key.id)
		obj.getClass().getMethod('count').invoke(null) == 1
		obj.getClass().getMethod('exists', Object).invoke(null, key.id)
		obj.id == key.id
		
		when:
		obj.delete()
		!obj.getClass().getMethod('exists', Object).invoke(null, key.id)
		obj.getClass().getMethod('count').invoke(null) == 0
		key.get()
		
		then:
		thrown(EntityNotFoundException)
		
		where:
		cls << [
		'''	@groovyx.gaelyk.datastore.Entity
			class MyPogo {}
			
			new MyPogo()''',
		'''	@groovyx.gaelyk.datastore.Entity
			class MyPogo { @groovyx.gaelyk.datastore.Key long id }
			
			new MyPogo()''',
		'''	@groovyx.gaelyk.datastore.Entity
		class MyPogo { @groovyx.gaelyk.datastore.Key String id }
		
		new MyPogo(id: 'Test')'''
		]
		
	}
	
	def "Test find all with closure"(){
		def obj = newShell().evaluate '''
			@groovyx.gaelyk.datastore.Entity
			class MyPogo {
				@groovyx.gaelyk.datastore.Indexed String test
			}
			
			new MyPogo(test: "foo").save()
			new MyPogo(test: "foo").save()
			new MyPogo(test: "bar").save()

			MyPogo.findAll''' + argument

		expect:
		obj.size() == result

		where:
		result 	| argument
		3		| '()'
		2		| '{ where test == "foo"}'
		1		| '{ where test == "bar"}'
		
	}
	
	def "Test count "(){
		def obj = newShell().evaluate '''
			@groovyx.gaelyk.datastore.Entity
			class MyPogo {
				@groovyx.gaelyk.datastore.Indexed String test
			}
			
			new MyPogo(test: "foo").save()
			new MyPogo(test: "foo").save()
			new MyPogo(test: "bar").save()

			MyPogo.count''' + argument

		expect:
		obj == result
		
		where:
		result 	| argument
		3		| '()'
		2		| '{ where test == "foo"}'
		1		| '{ where test == "bar"}'
	}
	
	def "Test iterate "(){
		def obj = newShell().evaluate '''
			@groovyx.gaelyk.datastore.Entity
			class MyPogo {
				@groovyx.gaelyk.datastore.Indexed String test
			}
			
			new MyPogo(test: "foo").save()
			new MyPogo(test: "foo").save()
			new MyPogo(test: "bar").save()

			MyPogo.iterate''' + argument

		expect:
		obj.size() == result
		
		where:
		result 	| argument
		3		| '()'
		2		| '{ where test == "foo"}'
		1		| '{ where test == "bar"}'
	}
	
	def "Test find "(){
		def obj = newShell().evaluate '''
			@groovyx.gaelyk.datastore.Entity
			class MyPogo {
				@groovyx.gaelyk.datastore.Indexed String test
			}
			
			new MyPogo(test: "foo").save()
			new MyPogo(test: "foo").save()
			new MyPogo(test: "bar").save()

			MyPogo.find{ where test == "bar" }'''

		expect:
		obj
	}
	
	
	private GroovyShell newShell(){
		CompilerConfiguration cc = new CompilerConfiguration()
		cc.addCompilationCustomizers(new ASTTransformationCustomizer(new QueryDslTransformation()))
		new GroovyShell(cc)
	}

}
