package groovyx.gaelyk.query;

import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterable;

/**
 * Query result iterable wrapper which gives access to the original query.
 * 
 * @author Vladimir Orany
 *
 * @param <T> element type
 */
public interface QueryResultIterableWithQuery<T> extends QueryResultIterable<T> {

    /**
     * Returns query used to create this result iterable.
     * @return query used to create this result iterable
     */
    Query getQuery();
    
}
