package groovyx.gaelyk.query;

import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;


/**
 * Query result list wrapper which gives access to the original query.
 * 
 * @author Vladimir Orany
 *
 * @param <T> element type
 */
public interface QueryResultListWithQuery<T> extends QueryResultList<T> {

    /**
     * Returns query used to create this result list.
     * @return query used to create this result list
     */
    Query getQuery();
    
}
