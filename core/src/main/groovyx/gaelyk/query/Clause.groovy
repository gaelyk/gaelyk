package groovyx.gaelyk.query

/**
 * Base class for sort and filter clauses.
 *
 * @author Guillaume Laforge
 *
 * @since 1.0
 */
abstract class Clause {
    /**
     * The current query builder used for creating the query
     */
    QueryBuilder builder

    /**
     * The column on which the clause is being applied
     */
    String column
}