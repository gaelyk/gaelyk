package groovyx.gaelyk.datastore

import groovy.transform.CompileStatic
import groovyx.gaelyk.query.QueryBuilder
import groovyx.gaelyk.query.QueryResultIteratorWithQuery

import com.google.appengine.api.datastore.Cursor
import com.google.appengine.api.datastore.Index
import com.google.appengine.api.datastore.Query
import com.google.appengine.api.datastore.QueryResultIterator

@CompileStatic
/**
 * Iterator which handles expired queries gracefully.
 * @author Vladimir Orany
 *
 */
class SelfRestartingQueryResultIterator<T> implements QueryResultIteratorWithQuery<T> {

    private final Query query
    private final QueryBuilder queryBuilder
    private QueryResultIteratorWithQuery<T> currentIterator
    private Cursor currentCursor

    private SelfRestartingQueryResultIterator(QueryBuilder builder){
        queryBuilder = builder
        query = builder.createQuery()
        // disable auto restart for the builder otherwise we end up in loop
        builder.restart null
        currentIterator = queryBuilder.iterate() as QueryResultIteratorWithQuery
    }

    /**
     * Creates new restarting iterator from given builder
     * @param builder builder used for iterating
     * @return new instance of restarting iterator
     */
    static SelfRestartingQueryResultIterator from(QueryBuilder builder){
        new SelfRestartingQueryResultIterator(builder)
    }

    T next(){
        try {
            def next = currentIterator.next()
            currentCursor = currentIterator.cursor
            return next
        } catch(e){
            if (!currentCursor) throw new NoSuchElementException()
            if(e.message?.contains('Please restart it with the last cursor')){
                queryBuilder.startAt(currentCursor)
                currentIterator = queryBuilder.iterate() as QueryResultIteratorWithQuery
                return next()
            }
        }
    }

    boolean hasNext(){
        try {
            return currentIterator.hasNext()
        } catch(e){
            if (!currentCursor) return false
            if(e.message?.contains('Please restart it with the last cursor')){
                queryBuilder.startAt(currentCursor)
                currentIterator = queryBuilder.iterate() as QueryResultIteratorWithQuery
                return hasNext()
            }
        }
    }

    @Override public void remove() {
        throw new UnsupportedOperationException()
    }

    @Override public Cursor getCursor() {
        try {
            return currentIterator.getCursor()
        } catch(e){
            if (!currentCursor) return null
            if(e.message?.contains('Please restart it with the last cursor')){
                queryBuilder.startAt(currentCursor)
                currentIterator = queryBuilder.iterate() as QueryResultIteratorWithQuery
                return getCursor()
            }
        }
    }

    @Override public List<Index> getIndexList() {
        try {
            return currentIterator.getIndexList()
        } catch(e){
            if (!currentCursor) return []
            if(e.message?.contains('Please restart it with the last cursor')){
                queryBuilder.startAt(currentCursor)
                currentIterator = queryBuilder.iterate() as QueryResultIteratorWithQuery
                return getIndexList()
            }
        }
    }
    
    @Override public Query getQuery() {
        return query;
    }
}
