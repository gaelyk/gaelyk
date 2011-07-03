package groovyx.gaelyk.query

import groovy.transform.InheritConstructors

/**
 * Exception thrown when there is a syntax problem in your datastore queries using the datastore query DSL.
 *
 * @author Guillaume Laforge
 *
 * @since 1.0
 */
@InheritConstructors
class QuerySyntaxException extends RuntimeException { }
