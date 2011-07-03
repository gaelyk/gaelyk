package groovyx.gaelyk.query

import com.google.appengine.api.datastore.Query
import groovy.transform.ToString

/**
 * 
 * @author Guillaume Laforge
 */
@ToString(includeNames = true)
class SortClause extends Clause {
    Query.SortDirection direction

    QueryBuilder by(col) {
        if (builder.@coercedClass) {
            if (!builder.@coercedClass.metaClass.properties.name.contains(col)) {
                throw new QuerySyntaxException("Your sort clause on '${col}' is not possible as ${builder.@coercedClass.name} doesn't contain that property")
            }
        }

        column = col.toString()
        builder.getClauses().add(this)
        return builder
    }
}