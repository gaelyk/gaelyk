package groovyx.gaelyk.query;

import groovyx.gaelyk.extensions.DatastoreExtensions;

import java.util.List;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterator;

class CoercedQueryResultIterator<E> implements QueryResultIteratorWithQuery<E> {

    private final QueryResultIterator<Entity> entitiesIterator;
    private final Class<E>                    coercedClass;
    private final Query                       query;

    private CoercedQueryResultIterator(Query query, QueryResultIterator<Entity> entitiesIterator, Class<E> coercedClass) {
        this.query = query;
        this.entitiesIterator = entitiesIterator;
        this.coercedClass = coercedClass;
    }
    
    public static <E> CoercedQueryResultIterator<E> coerce(Query query, QueryResultIterator<Entity> entitiesIterator, Class<E> coercedClass){
        return new CoercedQueryResultIterator<>(query, entitiesIterator, coercedClass);
    }

    public boolean hasNext() {
        return entitiesIterator.hasNext();
    }

    public E next() {
        Entity entity = entitiesIterator.next();
        try {
            return (E) DatastoreExtensions.asType(entity, coercedClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("Forbidden to call remove() on this Query DSL results iterator");
    }

    public Cursor getCursor() {
        return entitiesIterator.getCursor();
    }

    public List<Index> getIndexList() {
        return entitiesIterator.getIndexList();
    }
    
    @Override public Query getQuery() {
        return query;
    }
    
}
