package groovyx.gaelyk.datastore

import java.util.List;

import groovy.transform.CompileStatic;
import groovyx.gaelyk.query.QueryBuilder

import com.google.appengine.api.datastore.Cursor
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.QueryResultIterator

@CompileStatic
/**
 * Iterator which handles expired queries gracefully.
 * @author Vladimir Orany
 *
 */
class SelfRestartingQueryResultIterator implements QueryResultIterator {

    final QueryBuilder query
    private QueryResultIterator currentIterator
    private Cursor currentCursor

    private SelfRestartingQueryResultIterator(QueryBuilder builder){
        query = builder
        // disable auto restart for the builder otherwise we end up in loop
        builder.restart null
        currentIterator = query.iterate() as QueryResultIterator
    }

    /**
     * Creates new restarting iterator from given builder
     * @param builder builder used for iterating
     * @return new instance of restarting iterator
     */
    static SelfRestartingQueryResultIterator from(QueryBuilder builder){
        new SelfRestartingQueryResultIterator(builder)
    }

    Object next(){
        try {
            def next = currentIterator.next()
            currentCursor = currentIterator.cursor
            return next
        } catch(e){
            if(e.message?.contains('Please restart it with the last cursor')){
                query.startAt(currentCursor)
                currentIterator = query.iterate() as QueryResultIterator
                return next()
            }
        }
    }

    boolean hasNext(){
        try {
            return currentIterator.hasNext()
        } catch(e){
            if(e.message?.contains('Please restart it with the last cursor')){
                query.startAt(currentCursor)
                currentIterator = query.iterate() as QueryResultIterator
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
            if(e.message?.contains('Please restart it with the last cursor')){
                query.startAt(currentCursor)
                currentIterator = query.iterate() as QueryResultIterator
                return getCursor()
            }
        }
    }

    @Override public List getIndexList() {
        try {
            return currentIterator.getIndexList()
        } catch(e){
            if(e.message?.contains('Please restart it with the last cursor')){
                query.startAt(currentCursor)
                currentIterator = query.iterate() as QueryResultIterator
                return getIndexList()
            }
        }
    }
}
