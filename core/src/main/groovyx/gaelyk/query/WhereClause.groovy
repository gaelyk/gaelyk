package groovyx.gaelyk.query

import com.google.appengine.api.datastore.Query
import groovy.transform.ToString

/**
 * 
 * @author Guillaume Laforge
 */
@ToString(includeNames = true)
class WhereClause extends Clause {
    Query.FilterOperator operation
    Object comparedValue
}