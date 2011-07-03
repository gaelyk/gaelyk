package groovyx.gaelyk.query

/**
 * The various options for the select parameter:
 * <ul>
 *     <li>ALL: <code>select all</code> (full entity, default select option when select omitted)</li>
 *     <li>KEYS: <code>select keys</code> (return just the keys)</li>
 *     <li>SINGLE: <code>select single</code> (return just one entity when the search yields just one)</li>
 *     <li>COUNT: <code>select count</code> (return the count of entities for that query)</li>
 * </ul>
 *
 * @author Guillaume Laforge
 *
 * @since 1.0
 */
enum QueryType {
    ALL, KEYS, SINGLE, COUNT
}