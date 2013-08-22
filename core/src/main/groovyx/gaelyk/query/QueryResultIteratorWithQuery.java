package groovyx.gaelyk.query;

import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterator;

/**
 * Query result iterator wrapper which gives access to the original query.
 * 
 * @author Vladimir Orany
 *
 * @param <T> element type
 */
public interface QueryResultIteratorWithQuery<T> extends QueryResultIterator<T> {

    /**
     * Returns query used to create this result iterator.
     * @return query used to create this result iterator
     */
    Query getQuery();
    
}
