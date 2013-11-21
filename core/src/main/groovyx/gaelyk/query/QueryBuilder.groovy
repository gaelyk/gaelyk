/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gaelyk.query

import groovy.transform.CompileStatic;
import groovy.transform.PackageScope
import groovyx.gaelyk.datastore.SelfRestartingQueryResultIterator
import groovyx.gaelyk.extensions.DatastoreExtensions

import java.util.Map.Entry

import com.google.appengine.api.datastore.Cursor
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.FetchOptions
import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.PreparedQuery
import com.google.appengine.api.datastore.PropertyProjection
import com.google.appengine.api.datastore.Query
import com.google.appengine.api.datastore.QueryResultIterator
import com.google.appengine.api.datastore.QueryResultList;

/**
 * The query build is used to create a datastore <code>Query</code>
 * or execute a <code>PreparedQuery</code>,
 * from the <code>datastore.query {}</code> or <code>datastore.execute {}</code> calls.
 *
 * @author Guillaume Laforge
 *
 * @since 1.0
 */
//@CompileStatic
class QueryBuilder {
    private QueryType queryType = QueryType.ALL
    private String fromKind
    private Key ancestor
    @PackageScope Class coercedClass
    @PackageScope List<Clause> clauses = []
    private FetchOptions options = FetchOptions.Builder.withDefaults()
    private Binding binding
    private Map<String, Class<?>> projections = [:]
    private boolean restartAutomatically

    static QueryBuilder builder(){
        new QueryBuilder(null)
    }

    /**
     * Create a query builder object.
     *
     * @param binding the binding of the script where the query is being built, or null otherwise
     */
    QueryBuilder(Binding binding) {
        this.binding = binding
    }

    /**
     * @return a <code>Query</code> object
     */
    Query createQuery() {
        // entity query, or kindless query
        Query query = fromKind ? new Query(fromKind) : new Query()

        // retrieve keys only or full entities
        if (queryType == QueryType.KEYS)
            query.setKeysOnly()

        if (ancestor)
            query.setAncestor(ancestor)

        if(projections){
            for(Entry<String, Class> entry in projections.entrySet()){
                query.addProjection(new PropertyProjection(entry.key, entry.value))
            }
        }

        for (clause in clauses) {
            if (clause instanceof WhereClause) {
                WhereClause whereClause = clause as WhereClause
                query.addFilter(whereClause.column, whereClause.operation, whereClause.comparedValue)
            } else if (clause instanceof SortClause) {
                SortClause sortClause = clause as SortClause
                query.addSort(sortClause.column, sortClause.direction)
            }
        }
        return query
    }

    /**
     * @return the result of the execution of a prepared query
     */
    def execute(boolean iterable = false) {
        if(!iterable && restartAutomatically){
            throw new IllegalArgumentException("Only iterator can be restarted automatically at the moment. Remove 'restart automatically' from the .")
        }
        Query query = createQuery()

        PreparedQuery preparedQuery = DatastoreServiceFactory.datastoreService.prepare(query)

        if (queryType == QueryType.COUNT) {
            return preparedQuery.countEntities(options)
        } else if (queryType == QueryType.SINGLE) {
            if (coercedClass) {
                Entity en = preparedQuery.asSingleEntity()
                if(en == null){
                    return null
                }
                return DatastoreExtensions.asType(en, coercedClass)
            } else {
                return preparedQuery.asSingleEntity()
            }
        }

        if (coercedClass) {
            if (iterable) {
                if(restartAutomatically){
                    return SelfRestartingQueryResultIterator.from(this)
                }
                QueryResultIterator<Entity> entitiesIterator = preparedQuery.asQueryResultIterator(options)

                return CoercedQueryResultIterator.coerce(query, preparedQuery.asQueryResultIterator(options), coercedClass)
            } else {
                return CoercedQueryResultList.coerce(query, preparedQuery.asQueryResultList(options), coercedClass)
            }
        } else {
            if(restartAutomatically && iterable && queryType == QueryType.ALL){
                return SelfRestartingQueryResultIterator.from(this)
            }
            def result = iterable ? preparedQuery.asQueryResultIterator(options) : preparedQuery.asQueryResultList(options)
            if (queryType == QueryType.KEYS) {
                if (iterable) {
                    return restartAutomatically ? SelfRestartingQueryResultIterator.from(this) : CoercedQueryResultIterator.coerce(query, result, Key)
                } else {
                    return CoercedQueryResultList.coerce(query, result, Key)
                }
            } else {
                return result
            }
        }
    }

    /**
     * @return the result of the execution of a prepared query in the form of an iterator
     */
    def iterate() {
        execute(true)
    }

    /**
     * @param name the name of the property to be retrieved from within the closue passed to query/execute()
     * @return the all, keys, single or count query type constants or a string representing the property
     */
    def getProperty(String name) {
        // if the datastore execute or query calls are made in a script
        // check if the parameters or variables in the query are coming from the binding
        // for example: params, header, request, etc.
        if (binding && binding.variables.containsKey(name))
            return binding.variables[name]

        if (name == 'all')    return QueryType.ALL
        if (name == 'keys')   return QueryType.KEYS
        if (name == 'single') return QueryType.SINGLE
        if (name == 'count')  return QueryType.COUNT

        return name
    }

    /**
     * Select all entity properties, keys only, a single entity, or the count of entities matching that query.
     * By default, if the select clause is not used, the query builder assumes a select all.
     * Possible syntax:
     * <pre><code>
     *  select all
     *  select keys
     *  select single
     *  select count
     * </code></pre>
     *
     * @param qt the type of query
     * @return the query builder for chaining calls
     */
    QueryBuilder select(QueryType qt) {
        queryType = qt
        return this
    }


    /**
     * Select particular entity properties of entities matching that query.
     * Possible syntax:
     * <pre><code>
     *  select name: String, age: Integer
     * </code></pre>
     *
     * @param projs projections used in this query builder
     * @return the query builder for chaining calls
     */
    QueryBuilder select(Map<String, Object> projs) {
        projections.putAll(projs)
        return this
    }

    /**
    * Select particular entity properties of entities matching that query.
    * Possible syntax:
    * <pre><code>
    *  select name: String, age: Integer
    * </code></pre>
    *
    * @param projs projections used in this query builder
    * @return the query builder for chaining calls
    */
   QueryBuilder select(String... projs) {
       for(String proj in projs){
           projections.put(proj, null)
       }
       return this
   }


    /**
     * @throws QuerySyntaxException if a wrong parameter is passed to the select clause.
     */
    void select(Object qt) {
        throw new QuerySyntaxException("Use 'all', 'keys', 'single', 'count' or 'prop1: Type1, prop2: Type2, ...' for your select clause instead of ${qt}.")
    }

    /**
     * Defines the entity kind we want to retrieve.
     *
     * Possible syntax:
     * <pre><code>
     *  from persons
     *  from 'persons'
     *  from someStringVariable
     * </code></pre>
     *
     * @param entityKind the kind of the entity we want to search for
     * @return the query builder for chaining calls
     */
    QueryBuilder from(String entityKind) {
        fromKind = entityKind
        return this
    }

    /**
     * Defines the entity kind we want to retrieve,
     * and specifies that we want to coerce the <code>Entity</code> into a specific class.
     *
     * Possible syntax:
     * <pre><code>
     *  from persons as Person
     *  from 'persons' as Person
     *  from someStringVariable as SomeClass
     * </code></pre>
     *
     * @param entityKind the entity kind
     * @param coercedTo the class into which we want to coerce the entities
     * @return the query builder for chaining calls
     */
    QueryBuilder from(String entityKind, Class coercedTo) {
        fromKind = entityKind
        coercedClass = coercedTo
        return this
    }

    /**
     * Specify finding entities descending a certain ancestor
     *
     * Possible syntax:
     * <pre><code>
     *  ancestor someKeyVariable
     * </code></pre>
     *
     * @param key the key of the ancestor
     * @return the query builder for chaining calls
     */
    QueryBuilder ancestor(Key key) {
        ancestor = key
        return this
    }

    /**
     * Specify sorting on a property and its direction.
     *
     * Possible syntax:
     * <pre><code>
     *  sort asc by age
     *  sort desc by dateCreated
     * </code></pre>
     *
     * @param direction asc or desc for ascending and descending sorting respectively
     * @return a sort clause on which the <code>by()</code> method can be called
     */
    SortClause sort(String direction) {
        Query.SortDirection dir
        if (direction == 'asc') {
            dir =  Query.SortDirection.ASCENDING
        } else if (direction == 'desc') {
            dir =  Query.SortDirection.DESCENDING
        } else {
            throw new QuerySyntaxException("Use either 'asc' or 'desc' for sort direction.")
        }

        return new SortClause(
            builder: this,
            direction: dir
        )
    }

    /**
     * @throws QuerySyntaxException when a wrong direction is being used
     */
    void sort(Object direction) {
        throw new QuerySyntaxException("Use either 'asc' or 'desc' for sort direction.")
    }

    /**
     * Defines a where clause for comparing an entity property to another value.
     * The left-hand-side of the comparison should be the column,
     * and the right-hand-side the value to be compared against.
     * The following operators are supported: <, <=, ==, >=, >, != and in
     *
     * Possible syntax:
     * <pre><code>
     *  where dateCreated > new Date() - 1
     *  where name == 'Guillaume'
     *  where numChildren in [0, 1]
     *  where color != 'red'
     * </code></pre>
     *
     * @param clause the where clause
     * @return the query builder for chaining calls
     */
    QueryBuilder where(WhereClause clause) {
        if (coercedClass) {
            if (!coercedClass.metaClass.properties['name'].contains(clause.column)) {
                throw new QuerySyntaxException("Your where clause on '${clause.column}' is not possible as ${coercedClass.name} doesn't contain that property")
            }
        }
        this.clauses.add(clause)
        return this
    }

    /**
     * A synonym of where.
     *
     * @param clause the where clause
     * @return the query builder for chaining calls
     */
    QueryBuilder and(WhereClause clause) {
        where(clause)
    }

    /**
     * @throws QuerySyntaxException when something different than a where clause is given
     */
    void where(Object clause) {
        throw new QuerySyntaxException("Use a proper comparison in your where/and clause, instead of ${clause}")
    }

    // ------------------------------------------------
    // fetch options

    /**
     * Defines a limit fetch option for the <code>PreparedQuery</code>
     *
     * Possible syntax:
     * <pre><code>
     *  limit 10
     *  offset 50 limit 10
     * </code></pre>
     *
     * @param lim the limit
     * @return the query builder for chaining calls
     */
    QueryBuilder limit(int lim) {
        options.limit(lim)
        return this
    }

    /**
     * Defines an offset fetch option for the <code>PreparedQuery</code>
     *
     * Possible syntax:
     * <pre><code>
     *  offset 10
     *  offset 50 limit 10
     * </code></pre>
     *
     * @param ofst the offset
     * @return the query builder for chaining calls
     */
    QueryBuilder offset(int ofst) {
        options.offset(ofst)
        return this
    }

    /**
     * Defines a range, ie. an offset and limit fetch options for the <code>PreparedQuery</code>
     *
     * Possible syntax:
     * <pre><code>
     *  range 10..20
     * </code></pre>
     *
     * @param range an int range
     * @return the query builder for chaining calls
     */
    QueryBuilder range(IntRange range) {
        options.offset(range.getFromInt())
        options.limit(range.getToInt() - options.offset + 1)
        return this
    }

    /**
     * Defines a chunk size fetch option for the <code>PreparedQuery</code>
     *
     * @param size size of the chunks
     * @return the query builder for chaining calls
     */
    QueryBuilder chunkSize(int size) {
        options.chunkSize(size)
        return this
    }

    /**
     * Defines a prefetch size fetch option for the <code>PreparedQuery</code>
     *
     * Possible syntax:
     * <pre><code>
     *  offset 10
     *  offset 50 limit 10
     * </code></pre>
     *
     * @param size the prefetch size
     * @return the query builder for chaining calls
     */
    QueryBuilder prefetchSize(int size) {
        options.prefetchSize(size)
        return this
    }

    /**
     * Defines a start cursor fetch option for the <code>PreparedQuery</code>
     *
     * Possible syntax:
     * <pre><code>
     *  startAt someCursorVariable
     * </code></pre>
     *
     * @param cursor the start cursor
     * @return the query builder for chaining calls
     */
    QueryBuilder startAt(Cursor cursor) {
        options.startCursor(cursor)
        return this
    }

    /**
     * Defines a start cursor fetch option for the <code>PreparedQuery</code>
     * using a string representation of the cursor
     *
     * Possible syntax:
     * <pre><code>
     *  startAt cursorString
     * </code></pre>
     *
     * @param cursor the start cursor in its string representation
     * @return the query builder for chaining calls
     */
    QueryBuilder startAt(String cursorString) {
        return startAt(Cursor.fromWebSafeString(cursorString))
    }

    /**
     * Defines an end cursor fetch option for the <code>PreparedQuery</code>
     *
     * Possible syntax:
     * <pre><code>
     *  endAt someCursorVariable
     * </code></pre>
     *
     * @param cursor the end cursor
     * @return the query builder for chaining calls
     */
    QueryBuilder endAt(Cursor cursor) {
        options.endCursor(cursor)
        return this
    }

    /**
     * Defines an end cursor fetch option for the <code>PreparedQuery</code>
     * using a string representation of the cursor
     *
     * Possible syntax:
     * <pre><code>
     *  endAt cursorString
     * </code></pre>
     *
     * @param cursor the end cursor
     * @return the query builder for chaining calls
     */
    QueryBuilder endAt(String cursorString) {
        return endAt(Cursor.fromWebSafeString(cursorString))
    }
    
    /**
     * Defines an end cursor fetch option for the <code>PreparedQuery</code>
     * using a string representation of the cursor
     *
     * Possible syntax:
     * <pre><code>
     *  endAt cursorString
     * </code></pre>
     *
     * @param cursor the end cursor
     * @return the query builder for chaining calls
     */
    QueryBuilder restart(String auto) {
        restartAutomatically = 'automatically' == auto
        return this
    }
    
    /**
     * Uses usually user supplied parameter map.
     * 
     * <pre><code>
     *  if (params.limit) {
     *          limit (params.limit as int)
     *          chunkSize (params.limit as int)
     *      }
     *      
     *      if (params.cursor) {
     *          startAt (params.cursor as String)
     *      } else if (params.offset) {
     *          offset (params.offset as int)
     *      }
     * </code></pre>
     * It also sets the max page to 100 to prevent DDoS
     * or any other user supplied value.
     * 
     * @param params map containing one of more of following keys: limit, offset, cursor
     * @param maxPage maximum (or default) limit 
     */
    QueryBuilder paginate(Map params, int maxPage = 100) {
        if (params.limit && (params.limit as int) > 0) {
            int theLimit = Math.min(params.limit as int, maxPage)
            limit theLimit
            chunkSize theLimit
        } else {
            limit maxPage
        }
        
        if (params.cursor) {
            startAt (params.cursor as String)
        } else if (params.offset) {
            offset (params.offset as int)
        }
        this
    }
    
}
