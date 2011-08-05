package groovyx.gaelyk.querydsl

import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import org.codehaus.groovy.tools.ast.TransformTestHelper
import groovyx.gaelyk.query.QueryDslTransformation
import org.codehaus.groovy.control.CompilePhase
import com.google.appengine.api.datastore.Entity
import groovyx.gaelyk.GaelykCategory
import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.KeyFactory
import groovyx.gaelyk.query.QuerySyntaxException
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Query

/**
 * 
 * @author Guillaume Laforge
 */
class QueryDslTest extends GroovyTestCase {
    // setup the local environement stub services
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
                import groovyx.gaelyk.GaelykCategory

                def datastore = DatastoreServiceFactory.datastoreService

                use(GaelykCategory) {
                    datastore.query {
                        ${queryString}
                    }.toString()
                }
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
    }


    private execute(String queryString) {
        TransformTestHelper th = new TransformTestHelper(new QueryDslTransformation(), CompilePhase.CANONICALIZATION)

        Class<Script> clazz = th.parse """
                import com.google.appengine.api.datastore.*
                import groovyx.gaelyk.GaelykCategory

                def datastore = DatastoreServiceFactory.datastoreService

                use(GaelykCategory) {
                    datastore.execute {
                        ${queryString}
                    }
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
        use (GaelykCategory) {
            [
                [name: 'Guillaume', age: 34, size: 'L'],
                [name: 'Marion',    age: 3,  size: 'XS'],
                [name: 'John',      age: 53, size: 'XL'],
                [name: 'Jack',      age: 21, size: 'L'],
                [name: 'Gus',       age: 13, size: 'M'],
            ].each { props ->
                def e = new Entity('persons')
                props.each { k, v -> e."$k" = v }
                e.save()
            }
        }
    }

    void testWithDataForFetchOptions() {
        createTestData()

        use (GaelykCategory) {
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
        }
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
                import groovyx.gaelyk.GaelykCategory

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

        use(GaelykCategory) {
            def persons = script.run()

            assert persons.size() == 1
            assert persons[0].age == 34
        }
    }

    void testComparisonOnKey() {
        use(GaelykCategory) {
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
                    import groovyx.gaelyk.GaelykCategory

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
    }

    void testDatastoreExecuteInAClassWithGaelykBinding() {
        use (GaelykCategory) {
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
    }

    void testCompilationErrorWhenAsClassIsUsedAndWrongProperty() {
        use (GaelykCategory) {
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
    }
}
