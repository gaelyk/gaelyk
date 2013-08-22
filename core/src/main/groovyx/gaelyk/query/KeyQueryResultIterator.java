package groovyx.gaelyk.query;

import java.util.List;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterator;

class KeyQueryResultIterator implements QueryResultIteratorWithQuery<Key> {

    private KeyQueryResultIterator(Query query, QueryResultIterator<Entity> result) {
        this.query = query;
        this.result = result;
    }

    public static KeyQueryResultIterator from(Query query, QueryResultIterator<Entity> entities){
        return new KeyQueryResultIterator(query, entities);
    }
    
    private final Query                       query;
    private final QueryResultIterator<Entity> result;

    public boolean hasNext() {
        return result.hasNext();
    }

    public Key next() {
        return result.next().getKey();
    }

    public void remove() {
        throw new UnsupportedOperationException("Forbidden to call remove() on this Query DSL results iterator");
    }

    public Cursor getCursor() {
        return result.getCursor();
    }

    public List<Index> getIndexList() {
        return result.getIndexList();
    }

    @Override public Query getQuery() {
        return query;
    }
}
