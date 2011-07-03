package groovyx.gaelyk.query

import com.google.appengine.api.datastore.Query

/**
 * A where clause adds a filtering operation for the query.
 * The following operations are allowed: <, <=, ==, >=, >, != and in.
 * On the left-hand-side of the comparison, you must use the property/column name,
 * whereas on the right-hand-side, you should put the value against which the comparison will be done.
 *
 * @author Guillaume Laforge
 *
 * @since 1.0
 */
class WhereClause extends Clause {
    /**
     * The filter operation: <, <=, ==, >=, >, != and in
     */
    Query.FilterOperator operation

    /**
     * The value to which the entity property is compared
     */
    Object comparedValue
}