package groovyx.gaelyk.query

import com.google.appengine.api.datastore.Query
import com.google.appengine.api.datastore.PreparedQuery
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.FetchOptions
import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.Cursor
import com.google.appengine.api.datastore.Entity
import groovyx.gaelyk.GaelykCategory

/**
 * 
 * @author Guillaume Laforge
 */
class QueryBuilder {
    private QueryType queryType = QueryType.ALL
    private String fromKind
    @PackageScope Class coercedClass
    private Key ancestor

    FetchOptions options = FetchOptions.Builder.withDefaults()

    List<Clause> clauses = []

    Query createQuery() {
        // entity query, or kindless query
        Query query = fromKind ? new Query(fromKind) : new Query()

        // retrieve keys only or full entities
        if (queryType == QueryType.KEYS)
            query.setKeysOnly()

        if (ancestor)
            query.setAncestor(ancestor)

        for (clause in clauses) {
            if (clause instanceof WhereClause) {
                WhereClause whereClause = clause
                query.addFilter(whereClause.column, whereClause.operation, whereClause.comparedValue)
            } else if (clause instanceof SortClause) {
                SortClause sortClause = clause
                query.addSort(sortClause.column, sortClause.direction)
            }
        }
        return query
    }

    def execute() {
        Query query = createQuery()

        PreparedQuery preparedQuery = DatastoreServiceFactory.datastoreService.prepare(query)

        if (queryType == QueryType.COUNT) {
            return preparedQuery.countEntities(options)
        } else if (queryType == QueryType.SINGLE) {
            if (coercedClass) {
                return preparedQuery.asSingleEntity()?.asType(coercedClass)
            } else {
                return preparedQuery.asSingleEntity()
            }
        }

        if (coercedClass) {
            def entities = preparedQuery.asQueryResultIterator(options)
            // use "manual" collect{} as in the context of the query{} call
            // the delegation transforms the class into a string expression
            def result = []
            for (entity in entities) result << entity.asType(coercedClass)
            return result
        } else {
            return preparedQuery.asQueryResultIterator(options)
        }
    }


    def getProperty(String name) {
        if (name == 'all')    return QueryType.ALL
        if (name == 'keys')   return QueryType.KEYS
        if (name == 'single') return QueryType.SINGLE
        if (name == 'count')  return QueryType.COUNT
        return name
    }

    QueryBuilder select(QueryType qt) {
        queryType = qt
        return this
    }

    void select(Object qt) {
        throw new QuerySyntaxException("Use 'all', 'keys', 'single' or 'count' for your select clause instead of ${qt}.")
    }

    QueryBuilder from(String entityKind) {
        fromKind = entityKind
        return this
    }

    QueryBuilder from(String entityKind, Class coercedTo) {
        fromKind = entityKind
        coercedClass = coercedTo
        return this
    }

    QueryBuilder ancestor(Key key) {
        ancestor = key
        return this
    }

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

    void sort(Object direction) {
        throw new QuerySyntaxException("Use either 'asc' or 'desc' for sort direction.")
    }

    QueryBuilder where(WhereClause clause) {
        if (coercedClass) {
            if (!coercedClass.metaClass.properties.name.contains(clause.column)) {
                throw new QuerySyntaxException("Your where clause on '${clause.column}' is not possible as ${coercedClass.name} doesn't contain that property")
            }
        }
        this.clauses.add(clause)
        return this
    }

    QueryBuilder and(WhereClause clause) {
        where(clause)
    }

    void where(Object clause) {
        throw new QuerySyntaxException("Use a proper comparison in your where/and clause, instead of ${clause}")
    }

    // ------------------------------------------------
    // fetch options

    QueryBuilder limit(int lim) {
        options.limit(lim)
        return this
    }

    QueryBuilder offset(int ofst) {
        options.offset(ofst)
        return this
    }

    QueryBuilder range(IntRange range) {
        options.offset(range.getFromInt())
        options.limit(range.getToInt() - options.offset + 1)
        return this
    }

    QueryBuilder chunkSize(int size) {
        options.chunkSize(size)
        return this
    }

    QueryBuilder prefetchSize(int size) {
        options.prefetchSize(size)
        return this
    }

    QueryBuilder startAt(Cursor cursor) {
        options.startCursor(cursor)
        return this
    }

    QueryBuilder endAt(Cursor cursor) {
        options.endCursor(cursor)
        return this
    }
}