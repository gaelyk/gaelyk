package groovyx.gaelyk.datastore

import groovyx.gaelyk.GaelykCategory
import groovyx.gaelyk.query.QueryBuilder
import groovyx.gaelyk.query.QueryType

import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.EntityNotFoundException
import com.google.appengine.api.datastore.FetchOptions
import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.KeyFactory
import com.google.appengine.api.datastore.Query
import groovyx.gaelyk.extensions.DatastoreExtensions

/**
 * Utility class used for delegating on from classes annotated with {@link groovyx.gaelyk.datastore.Entity}.
 *
 * @author Vladimir Orany
 * @deprecated Do not use this class directly. It's supposed to be used only from POGO methods.
 */
class EntityTransformationHelper {

    static Key save(Object pogo) {
        Key key = DatastoreExtensions.save(DatastoreExtensions.asType(pogo, Entity))
        if (CharSequence.class.isAssignableFrom(pogo.getClass().getMethod('get$key').returnType)) {
            pogo.set$key(key.name)
        } else {
            pogo.set$key(key.id)
        }
        key
    }

    static void delete(Object pogo) {
        DatastoreExtensions.delete(DatastoreExtensions.asType(pogo, Entity))
    }

    static <P> P get(Class<P> pogoClass, long key) {
        try {
           return DatastoreExtensions.asType(DatastoreExtensions.get(KeyFactory.createKey(pogoClass.simpleName, key)), pogoClass)
        } catch (EntityNotFoundException e) {
           return null;
        }
    }

    static <P> P get(Class<P> pogoClass, String key) {
        try {
           return DatastoreExtensions.asType(DatastoreExtensions.get(KeyFactory.createKey(pogoClass.simpleName, key)), pogoClass)
        } catch (EntityNotFoundException e) {
           return null;
        }
    }
    
    static <P> void delete(Class<P> pogoClass, key) {
        DatastoreExtensions.delete(KeyFactory.createKey(pogoClass.simpleName, key))
    }

    static int count(Class<?> pogoClass) {
        DatastoreService ds = DatastoreServiceFactory.datastoreService
        Query q = new Query(pogoClass.simpleName);
        ds.prepare(q).countEntities(FetchOptions.Builder.withDefaults())
    }

    static int count(Class<?> pogoClass, Closure c) {
        QueryBuilder builder = new QueryBuilder(c.thisObject instanceof Script ? c.thisObject.binding : null)
        HelperDatastore datastore = new HelperDatastore(builder: builder)
        datastore.execute(c).select(QueryType.COUNT).from(pogoClass.simpleName, pogoClass).execute()
    }

    static int count(Class<?> pogoClass, QueryBuilder builder) {
        if (builder == null) throw new IllegalArgumentException("Query builder cannot be null!")
        builder.select(QueryType.COUNT).from(pogoClass.simpleName, pogoClass).execute()
    }

    static <P> P find(Class<P> pogoClass, Closure c = {}) {
        QueryBuilder builder = new QueryBuilder(c.thisObject instanceof Script ? c.thisObject.binding : null)
        HelperDatastore datastore = new HelperDatastore(builder: builder)
        datastore.execute(c).select(QueryType.SINGLE).from(pogoClass.simpleName, pogoClass).execute()
    }

    static <P> P find(Class<P> pogoClass, QueryBuilder builder) {
        if (builder == null) throw new IllegalArgumentException("Query builder cannot be null!")
        builder.select(QueryType.SINGLE).from(pogoClass.simpleName, pogoClass).execute()
    }

    static <P> List<P> findAll(Class<P> pogoClass, Closure c = {}) {
        QueryBuilder builder = new QueryBuilder(c.thisObject instanceof Script ? c.thisObject.binding : null)
        HelperDatastore datastore = new HelperDatastore(builder: builder)
        datastore.execute(c).select(QueryType.ALL).from(pogoClass.simpleName, pogoClass).execute()
    }

    static <P> List<P> findAll(Class<P> pogoClass, QueryBuilder builder) {
        if (builder == null) throw new IllegalArgumentException("Query builder cannot be null!")
        builder.select(QueryType.ALL).from(pogoClass.simpleName, pogoClass).execute()
    }

    static <P> Iterator<P> iterate(Class<P> pogoClass, Closure c = {}) {
        QueryBuilder builder = new QueryBuilder(c.thisObject instanceof Script ? c.thisObject.binding : null)
        HelperDatastore datastore = new HelperDatastore(builder: builder)
        datastore.execute(c).select(QueryType.ALL).from(pogoClass.simpleName, pogoClass).iterate()
    }


    static <P> Iterator<P> iterate(Class<P> pogoClass, QueryBuilder builder) {
        if (builder == null) throw new IllegalArgumentException("Query builder cannot be null!")
        builder.select(QueryType.ALL).from(pogoClass.simpleName, pogoClass).iterate()
    }

}

class HelperDatastore {
    QueryBuilder builder

    QueryBuilder query(Closure c) { prepareAndLaunchQuery c }

    QueryBuilder execute(Closure c) { prepareAndLaunchQuery c }

    QueryBuilder iterate(Closure c) { prepareAndLaunchQuery c }

    private QueryBuilder prepareAndLaunchQuery(Closure c) {
        Closure cQuery = c.clone()
        cQuery.resolveStrategy = Closure.DELEGATE_FIRST
        cQuery.delegate = builder
        cQuery()
        return builder
    }
}
