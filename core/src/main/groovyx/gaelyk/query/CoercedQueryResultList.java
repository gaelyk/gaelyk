package groovyx.gaelyk.query;

import groovyx.gaelyk.extensions.DatastoreExtensions;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.QueryResultList;

/**
 * Coerced list.
 * 
 * @author Vladimir Orany
 * 
 * @param <T> coerced class
 */
class CoercedQueryResultList<T> extends ArrayList<T> implements QueryResultList<T> {

    private static final long serialVersionUID = 7330407587796412338L;
    private final Cursor      cursor;
    private final List<Index> indexList;

    private CoercedQueryResultList(QueryResultList<Entity> originalList, Class<T> coercedClass){
        super(originalList.size());
        for (Entity entity : originalList){
             add(coercedClass.cast(DatastoreExtensions.asType(entity, coercedClass)));
        }
        this.cursor = originalList.getCursor();
        this.indexList = originalList.getIndexList();
    }

    /**
     * Creates new coerced query result list wrapper
     * @param originalList original query result list
     * @param coercedClass class to coerce the entities
     * @return coerced wrapper list
     */
    public static <T> CoercedQueryResultList<T> coerce(QueryResultList<Entity> originalList, Class<T> coercedClass) {
        return new CoercedQueryResultList<T>(originalList, coercedClass);
    }

    @Override public Cursor getCursor() {
        return cursor;
    }

    @Override public List<Index> getIndexList() {
        return indexList;
    }

}
