package groovyx.gaelyk.query;

import groovyx.gaelyk.extensions.DatastoreExtensions;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;

/**
 * Coerced list.
 * 
 * @author Vladimir Orany
 * 
 * @param <T> coerced class
 */
class CoercedQueryResultList<T> extends AbstractList<T> implements QueryResultListWithQuery<T>, Serializable {

    private static final long               serialVersionUID = 7330407587796412338L;
    private final Query                     query;
    private final QueryResultList<Entity>   originalList;
    private final Class<T>                  coercedClass;

    private CoercedQueryResultList(Query query, QueryResultList<Entity> originalList, Class<T> coercedClass){
        if(originalList == null) {
            throw new IllegalArgumentException("Original list cannot be null!");
        }
        this.query          = query;
        this.originalList   = originalList;
        this.coercedClass   = coercedClass;
    }

    /**
     * Creates new coerced query result list wrapper
     * @param originalList original query result list
     * @param coercedClass class to coerce the entities
     * @return coerced wrapper list
     */
    public static <T> CoercedQueryResultList<T> coerce(Query query, QueryResultList<Entity> originalList, Class<T> coercedClass) {
        return new CoercedQueryResultList<T>(query, originalList, coercedClass);
    }

    @Override public Cursor getCursor() {
        return originalList.getCursor();
    }

    @Override public List<Index> getIndexList() {
        return originalList.getIndexList();
    }
    
    @Override public Query getQuery() {
        return query;
    }

    @Override public T get(int index) {
        return coercedClass.cast(DatastoreExtensions.asType(originalList.get(index), coercedClass));
    }

    @Override public int size() {
        return originalList.size();
    }
    
    @Override public T set(int index, T element) {
        throw new UnsupportedOperationException("You cannot modify this list. " +
        		"Copy the list by calling .collect() method first if you want do so"); 
    }
    
    @Override public void add(int index, T element) {
        throw new UnsupportedOperationException("You cannot modify this list. " +
                "Copy the list by calling .collect() method first if you want do so"); 
    }
    
    @Override public T remove(int index) {
        throw new UnsupportedOperationException("You cannot modify this list. " +
                "Copy the list by calling .collect() method first if you want do so"); 
    }
    
    public List<T> collect() {
        return DefaultGroovyMethods.collect(this);
    }
    
    
    
    

}
