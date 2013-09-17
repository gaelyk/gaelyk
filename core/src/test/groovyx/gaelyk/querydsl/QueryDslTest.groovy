package groovyx.gaelyk.querydsl

import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import org.codehaus.groovy.tools.ast.TransformTestHelper

import groovyx.gaelyk.query.QueryBuilder;
import groovyx.gaelyk.query.QueryDslTransformation
import org.codehaus.groovy.control.CompilePhase

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.KeyFactory
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.RawValue

import groovyx.gaelyk.query.QuerySyntaxException
import com.google.appengine.api.datastore.DatastoreServiceFactory

/**
 *
 * @author Guillaume Laforge
 */
class QueryDslTest extends GroovyTestCase {
    // setup the local environment stub services
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig().setNoStorage(true)
    )

    protected void setUp() {
        super.setUp()
        helper.setUp()
    }

    protected void tearDown() {
        helper.tearDown()
        super.tearDown()
    }

    private String queryFor(String queryString) {
        TransformTestHelper th = new TransformTestHelper(new QueryDslTransformation(), CompilePhase.CANONICALIZATION)

        Class<Script> clazz = th.parse """
                import com.google.appengine.api.datastore.*

                def datastore = DatastoreServiceFactory.datastoreService

                datastore.query {
                    ${queryString}
                }.toString()
        """

        clazz.newInstance().run()
    }

    void testQueries() {
        // simple example
        assert queryFor('''
                            select all from persons
                            where name == 'Guillaume'
                            sort desc by age
                        ''') ==
                'SELECT * FROM persons WHERE name = Guillaume ORDER BY age DESC'

        // example using variables
        assert queryFor('''
                            def params = [name: 'Guillaume']
                            def col = 'age'

                            select all from persons
                            where name == params.name
                            sort desc by col
                        ''') ==
                'SELECT * FROM persons WHERE name = Guillaume ORDER BY age DESC'

        // example on the same line with command chain expressions
        assert queryFor('''select all from persons where name == 'Guillaume' sort desc by age''') ==
                'SELECT * FROM persons WHERE name = Guillaume ORDER BY age DESC'

        // with quotes for entity kind and columns, getting keys only, and additional comparisons
        assert queryFor('''
                            select keys from 'persons'
                            where 'age' <= 99 and 'age' > 15
                        ''') ==
                'SELECT __key__ FROM persons WHERE age <= 99 AND age > 15'

        // inequality
        assert queryFor('''
                            select all from persons where age != 18
                        ''') ==
                'SELECT * FROM persons WHERE age != 18'

        // 'in'
        assert queryFor('''
                            select all from persons where age in [0, 1, 2, 3]
                        ''') ==
                'SELECT * FROM persons WHERE age IN [0, 1, 2, 3]'

        // ancestor
        Key key = KeyFactory.createKey('persons', 1234)
        assert queryFor('''
                            select all ancestor KeyFactory.createKey('persons', 1234)
                        ''') ==
                "SELECT * WHERE __ancestor__ is ${key}"

        // check for where !alive
        assert queryFor('''
                            select all from persons where !alive
                        ''') ==
                'SELECT * FROM persons WHERE alive = false'

        // check for where alive
        assert queryFor('''
                            select all from persons where alive
                        ''') ==
                'SELECT * FROM persons WHERE alive = true'
        


    }

    void testSyntaxErrors() {
        shouldFail(QuerySyntaxException) { queryFor 'select foo from persons' }
        shouldFail(QuerySyntaxException) { queryFor 'from persons sort 123 by name' }
        shouldFail(QuerySyntaxException) { queryFor 'from persons sort abc by name' }
        shouldFail(QuerySyntaxException) { queryFor 'from persons where null' }
        shouldFail(QuerySyntaxException) { queryFor 'from persons where 123' }

        shouldFail { queryFor 'from persons where name = "Guillaume"' }
        shouldFail { queryFor 'from persons \n name == "Guillaume"' }
    }


    private execute(String queryString) {
        TransformTestHelper th = new TransformTestHelper(new QueryDslTransformation(), CompilePhase.CANONICALIZATION)

        Class<Script> clazz = th.parse """
                import com.google.appengine.api.datastore.*

                def datastore = DatastoreServiceFactory.datastoreService

                datastore.execute {
                    ${queryString}
                }

                class Person {
                    String name
                    int age
                    String size
                }
        """

        clazz.newInstance().run()
    }

    void createTestData() {
        [
                [name: 'Guillaume', age: 34, size: 'L'],
                [name: 'Marion', age: 3, size: 'XS'],
                [name: 'John', age: 53, size: 'XL'],
                [name: 'Jack', age: 21, size: 'L'],
                [name: 'Gus', age: 13, size: 'M'],
        ].each { props ->
            def e = new Entity('persons')
            props.each { k, v -> e."$k" = v }
            e.save()
        }
    }

    void testWithDataForFetchOptions() {
        createTestData()

        assert execute('select all from persons where age == 34')[0].name == 'Guillaume'
        assert execute('select count from persons where age > 20') == 3
        assert execute('select single from persons where age == 13').name == 'Gus'
        assert execute('from persons where age == 3')[0].name == 'Marion'
        assert execute('from persons range 2..4').size() == 3
        assert execute('from persons range 2..<4').size() == 2
        assert execute('from persons offset 1 limit 2').size() == 2
        assert execute('select count from persons chunkSize 1 prefetchSize 1') == 5

        // need to figure out a way to test cursors

        def aPerson = execute('select single from persons as Person where age == 53')
        assert aPerson.class.name == 'Person'
        assert aPerson.name == 'John'
		

        def twoPersons = execute('''
            from persons as Person
            where age < 18
            sort desc by age
        ''')
        assert twoPersons.size() == 2
        assert twoPersons[0].class.name == 'Person'
        assert twoPersons[0].name == 'Gus'
        assert twoPersons[1].class.name == 'Person'
        assert twoPersons[1].name == 'Marion'

        shouldFail(QuerySyntaxException) { execute 'from persons as Person where foo != 3' }
        shouldFail(QuerySyntaxException) { execute 'from persons as Person sort desc by bingo' }
		
		assert execute('from persons offset 1 limit 2') instanceof QueryResultList
		
    }

    void testIncorrectArguments() {
        shouldFail(IllegalArgumentException) { execute 'from persons offset -10' }
        shouldFail(IllegalArgumentException) { execute 'from persons limit -10' }
        shouldFail(IllegalArgumentException) { execute 'from persons prefetchSize -10' }
        shouldFail(IllegalArgumentException) { execute 'from persons chunkSize -10' }
    }

    void testDynamicVariablesUsage() {
        TransformTestHelper th = new TransformTestHelper(new QueryDslTransformation(), CompilePhase.CANONICALIZATION)

        Class<Script> clazz = th.parse """
                import com.google.appengine.api.datastore.*

                new Entity('person').with {
                    name = 'Guillaume'
                    age = 34
                    save()
                }
                new Entity('person').with {
                    name = 'Marion'
                    age = 3
                    save()
                }

                datastore.execute {
                    select all from person
                    where name == params.name
                }
        """

        Script script = clazz.newInstance()
        script.binding = new Binding([params: [name: 'Guillaume'], datastore: DatastoreServiceFactory.datastoreService])

        def persons = script.run()

        assert persons.size() == 1
        assert persons[0].age == 34
    }

    void testComparisonOnKey() {
        def keys = ['a', 'b', 'c', 'd'].collect { n ->
            new Entity('player').with {
                name = n
                save()
            }
        }

        new Entity('team').with {
            name = 'one'
            players = keys[0, 1]
            save()
        }

        new Entity('team').with {
            name = 'two'
            players = keys[2, 3]
            save()
        }

/*
        // using the standard querying mechanism, this would work like this

        def datastore = DatastoreServiceFactory.datastoreService

        def query = new Query("team")
        query.addFilter("players", Query.FilterOperator.EQUAL, keys[2])

        def preparedQuery = datastore.prepare(query)
        def teamTwo = preparedQuery.asSingleEntity()

        assert teamTwo.name == 'two'
*/

        TransformTestHelper th = new TransformTestHelper(new QueryDslTransformation(), CompilePhase.CANONICALIZATION)

        Class<Script> clazz = th.parse """
                import com.google.appengine.api.datastore.*

                def datastore = DatastoreServiceFactory.datastoreService

                def playerB = datastore.execute {
                    select single from player
                    where name == 'b'
                }

                datastore.execute {
                    select single from team
                    where players == playerB.key
                }
        """

        Script script = clazz.newInstance()
        def teamOne = script.run()

        assert teamOne.name == 'one'
    }

    void testDatastoreExecuteInAClassWithGaelykBinding() {
        new Entity('books').with {
            title = 'Harry Potter'
            isbn = '1234567890'
            save()
        }

        TransformTestHelper th = new TransformTestHelper(new QueryDslTransformation(), CompilePhase.CANONICALIZATION)

        Class<Script> clazz = th.parse '''
            @groovyx.gaelyk.GaelykBindings
            class ExecuteWithinClassWithBindings {
                def exec() {
                    datastore.execute {
                        select single from books
                        where title == 'Harry Potter'
                    }
                }
            }
            new ExecuteWithinClassWithBindings().exec()
        '''

        def resultBook = clazz.newInstance().run()

        assert resultBook.isbn == '1234567890'
    }

    void testCompilationErrorWhenAsClassIsUsedAndWrongProperty() {
        new Entity('addresses').with {
            street = 'main street'
            zip = 12345
            city = 'New York'
            save()
        }

        TransformTestHelper th = new TransformTestHelper(new QueryDslTransformation(), CompilePhase.CANONICALIZATION)

        Class<Script> clazz = th.parse '''
                datastore.execute {
                    select single from addresses
                    where zip == 12345
                }
            '''

        def binding = new Binding([datastore: DatastoreServiceFactory.datastoreService])

        def addr = clazz.newInstance(binding).run()

        assert addr.city == 'New York'

        clazz = th.parse '''
                class Address {
                    String street
                    int zip
                    String city
                }

                datastore.execute {
                    select single
                    from addresses as Address
                    where nonexistantprop == 'abc'
                }
            '''

        shouldFail(QuerySyntaxException) {
            clazz.newInstance(binding).run()
        }
    }

    void testQueryByKeyIssue() {
        def e1 = new Entity("city", "San Jose")
        e1.save()

        def k1 = ["city", "San Jose"] as Key
        def p1 = new Entity("person", "Michael")
        p1.city = k1
        p1.save()

        TransformTestHelper th = new TransformTestHelper(new QueryDslTransformation(), CompilePhase.CANONICALIZATION)

        Class<Script> clazz = th.parse '''
                import com.google.appengine.api.datastore.Key

                def k1 = ["city", "San Jose"] as Key

                datastore.execute {
                    from person
                    where city ==  k1
                }
            '''

        def binding = new Binding([datastore: DatastoreServiceFactory.datastoreService])

        def peopleInSanJose = clazz.newInstance(binding).run()

        assert peopleInSanJose.size() == 1
    }

    void testProjectionQueryWithMap() {
        def e1 = new Entity("city", "San Jose")
        e1.prop1 = 'Name'
        e1.prop2 = 35
        e1.prop3 = true
        e1.save()

        TransformTestHelper th = new TransformTestHelper(new QueryDslTransformation(), CompilePhase.CANONICALIZATION)

        Class<Script> clazz = th.parse '''
                import com.google.appengine.api.datastore.Key

                datastore.execute {
                    select prop1: String, prop2: Long from city
                }
            '''

        def binding = new Binding([datastore: DatastoreServiceFactory.datastoreService])

        def ret = clazz.newInstance(binding).run()

        assert ret.size() == 1

        Entity re = ret[0]

        assert re.hasProperty('prop1')
        assert re.hasProperty('prop2')
        assert !re.hasProperty('prop3')

        assert re.getProperty('prop1') instanceof String
        assert re.getProperty('prop2') instanceof Long
        assert re.getProperty('prop3') == null
    }

    void testProjectionQueryWithList() {
        def e1 = new Entity("city", "San Jose")
        e1.prop1 = 'Name'
        e1.prop2 = 35
        e1.prop3 = true
        e1.save()

        TransformTestHelper th = new TransformTestHelper(new QueryDslTransformation(), CompilePhase.CANONICALIZATION)

        Class<Script> clazz = th.parse '''
                import com.google.appengine.api.datastore.Key

                datastore.execute {
                    select prop1, prop2 from city
                }
            '''

        def binding = new Binding([datastore: DatastoreServiceFactory.datastoreService])

        def ret = clazz.newInstance(binding).run()

        assert ret.size() == 1

        Entity re = ret[0]

        assert re.hasProperty('prop1')
        assert re.hasProperty('prop2')
        assert !re.hasProperty('prop3')

        assert re.getProperty('prop1') instanceof RawValue
        assert re.getProperty('prop2') instanceof RawValue
        assert re.getProperty('prop3') == null
    }

    void testIteratorResults() {
        TransformTestHelper th = new TransformTestHelper(new QueryDslTransformation(), CompilePhase.CANONICALIZATION)

        Class<Script> clazz = th.parse '''
                import com.google.appengine.api.datastore.Entity

                10.times { counter ->
                    new Entity('mugs').with {
                        name = "Mug #$counter"
                        save()
                    }
                }

                def r1 = datastore.iterate {
                    select all from mugs
                }

                def r2 = datastore.iterate {
                    select all from mugs as Mug
                }

                def r3 = datastore.iterate {
                    select keys from mugs
                }

                def r4 = datastore.execute {
                    select keys from mugs
                }

                return [r1, r2, r3, r4]

                class Mug { String name }
            '''

        def binding = new Binding([datastore: DatastoreServiceFactory.datastoreService])

        def (r1, r2, r3, r4) = clazz.newInstance(binding).run()

        assert r1 instanceof Iterator
        assert r1.size() == 10

        assert r2 instanceof Iterator
        r2 = r2.toList()
        def counter = 0
        assert r2.every { counter++; it.class.simpleName == 'Mug' }
        assert counter == 10

        assert r3 instanceof Iterator
        assert r3.next() instanceof Key

        assert r4 instanceof List
        assert r4[0] instanceof Key
    }
    
    void testBuild() {
        TransformTestHelper th = new TransformTestHelper(new QueryDslTransformation(), CompilePhase.CANONICALIZATION)

        Class<QueryBuilder> clazz = th.parse """
                import com.google.appengine.api.datastore.*

                def datastore = DatastoreServiceFactory.datastoreService

                datastore.build {
                    from Kind
                }
        """

        QueryBuilder builder = clazz.newInstance().run()
        assert builder.createQuery().kind == 'Kind'
    }
    
    void testPaginate() {
        QueryBuilder query = new QueryBuilder()
        
        assert !query.@options.limit
        assert !query.@options.offset
        assert !query.@options.chunkSize
        assert !query.@options.startCursor
        
        query = new QueryBuilder().paginate([:])
        
        assert  query.@options.limit     == 100
        assert !query.@options.offset
        assert !query.@options.chunkSize
        assert !query.@options.startCursor
        
        query = new QueryBuilder().paginate(limit: "10")
        
        assert  query.@options.limit     == 10
        assert !query.@options.offset
        assert  query.@options.chunkSize == 10
        assert !query.@options.startCursor
        
        query = new QueryBuilder().paginate(limit: "10", offset: "20")
        
        assert  query.@options.limit     == 10
        assert  query.@options.offset    == 20
        assert  query.@options.chunkSize == 10
        assert !query.@options.startCursor
        
        query = new QueryBuilder().paginate(limit: "10", cursor: "ExQ")
        
        assert  query.@options.limit     == 10
        assert !query.@options.offset
        assert  query.@options.chunkSize == 10
        assert  query.@options.startCursor.toWebSafeString() == "ExQ"
        
        query = new QueryBuilder().paginate(limit: "10", cursor: "ExQ", offset: 20)
        
        assert  query.@options.limit         == 10
        assert !query.@options.offset
        assert  query.@options.chunkSize     == 10
        assert  query.@options.startCursor.toWebSafeString() == "ExQ"
        
    }
}
