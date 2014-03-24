package groovyx.gaelyk.datastore

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovyx.gaelyk.extensions.DatastoreExtensions
import groovyx.gaelyk.query.QueryBuilder
import groovyx.gaelyk.query.QueryType

import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Entities
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.EntityNotFoundException
import com.google.appengine.api.datastore.FetchOptions
import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.KeyFactory
import com.google.appengine.api.datastore.Query

/**
 * Utility class used for delegating on from classes annotated with {@link groovyx.gaelyk.datastore.Entity}.
 *
 * @author Vladimir Orany
 * @deprecated Do not use this class directly. It's supposed to be used only from POGO methods.
 */
@CompileStatic
class EntityTransformationHelper {

    static Key save(DatastoreEntity<?> pogo) {
        Key key = DatastoreExtensions.save((Entity)DatastoreExtensions.asType(pogo, Entity))
        if (pogo.hasDatastoreNumericKey()) {
            pogo.setDatastoreKey(key.id)
        } else {
            pogo.setDatastoreKey(key.name)
        }
        if(pogo.hasDatastoreParent()){
            pogo.setDatastoreParent(key.parent)
        }
        if(pogo.hasDatastoreVersion()){
            try {
                pogo.setDatastoreVersion(Entities.getVersionProperty(DatastoreExtensions.get(Entities.createEntityGroupKey(key))))
            } catch (Exception e) {
                pogo.setDatastoreVersion(0)
            }
        }
        key
    }

    static void delete(Object pogo) {
        DatastoreExtensions.delete((Entity)DatastoreExtensions.asType(pogo, Entity))
    }

    static <P extends DatastoreEntity<?>> P get(Class<P> pogoClass, long key) {
        try {
            Entity entity = DatastoreExtensions.get(KeyFactory.createKey(pogoClass.simpleName, key))
            return (P) DatastoreEntityCoercion.Proxy.INSTANCE.convert(entity, pogoClass)
        } catch (EntityNotFoundException e) {
            return null
        }
    }

    static <P extends DatastoreEntity<?>> P get(Class<P> pogoClass, String key) {
        try {
            Entity entity = DatastoreExtensions.get(KeyFactory.createKey(pogoClass.simpleName, key))
            return (P) DatastoreEntityCoercion.Proxy.INSTANCE.convert(entity, pogoClass)
        } catch (EntityNotFoundException e) {
            return null
        }
    }

    static <P extends DatastoreEntity<?>> P get(Class<P> pogoClass, Key parentKey, long key) {
        try {
            Entity entity = DatastoreExtensions.get(KeyFactory.createKey(parentKey, pogoClass.simpleName, key))
            return (P) DatastoreEntityCoercion.Proxy.INSTANCE.convert(entity, pogoClass)
        } catch (EntityNotFoundException e) {
            return null
        }
    }

    static <P extends DatastoreEntity<?>> P get(Class<P> pogoClass, Key parentKey, String key) {
        try {
            Entity entity = DatastoreExtensions.get(KeyFactory.createKey(parentKey, pogoClass.simpleName, key))
            return (P) DatastoreEntityCoercion.Proxy.INSTANCE.convert(entity, pogoClass)
        } catch (EntityNotFoundException e) {
            return null
        }
    }

    static <P> void delete(Class<P> pogoClass, long key) {
        DatastoreExtensions.delete(KeyFactory.createKey(pogoClass.simpleName, key))
    }
    
    static <P> void delete(Class<P> pogoClass, String key) {
        DatastoreExtensions.delete(KeyFactory.createKey(pogoClass.simpleName, key))
    }

    static <P> void delete(Class<P> pogoClass, Key parentKey,  long key) {
        DatastoreExtensions.delete(KeyFactory.createKey(parentKey, pogoClass.simpleName, key))
    }
    
    static <P> void delete(Class<P> pogoClass, Key parentKey, String key) {
        DatastoreExtensions.delete(KeyFactory.createKey(parentKey, pogoClass.simpleName, key))
    }

    static int count(Class<?> pogoClass) {
        DatastoreService ds = DatastoreServiceFactory.datastoreService
        Query q = new Query(pogoClass.simpleName)
        ds.prepare(q).countEntities(FetchOptions.Builder.withDefaults())
    }

    static int count(Class<?> pogoClass, @DelegatesTo(value=QueryBuilder, strategy=Closure.DELEGATE_FIRST) Closure c) {
        QueryBuilder builder = new QueryBuilder(c.thisObject instanceof Script ? ((Script)c.thisObject).binding : null)
        HelperDatastore datastore = new HelperDatastore(builder: builder)
        (int)datastore.execute(c).select(QueryType.COUNT).from(pogoClass.simpleName, pogoClass).execute()
    }

    static int count(Class<?> pogoClass, QueryBuilder builder) {
        if (builder == null) throw new IllegalArgumentException("Query builder cannot be null!")
        (int)builder.select(QueryType.COUNT).from(pogoClass.simpleName, pogoClass).execute()
    }

    static <P> P find(Class<P> pogoClass, @DelegatesTo(value=QueryBuilder, strategy=Closure.DELEGATE_FIRST) Closure c = {}) {
        QueryBuilder builder = new QueryBuilder(c.thisObject instanceof Script ? ((Script)c.thisObject).binding : null)
        HelperDatastore datastore = new HelperDatastore(builder: builder)
        datastore.execute(c).select(QueryType.SINGLE).from(pogoClass.simpleName, pogoClass).execute()
    }

    static <P> P find(Class<P> pogoClass, QueryBuilder builder) {
        if (builder == null) throw new IllegalArgumentException("Query builder cannot be null!")
        builder.select(QueryType.SINGLE).from(pogoClass.simpleName, pogoClass).execute()
    }

    static <P> List<P> findAll(Class<P> pogoClass, @DelegatesTo(value=QueryBuilder, strategy=Closure.DELEGATE_FIRST) Closure c = {}) {
        QueryBuilder builder = new QueryBuilder(c.thisObject instanceof Script ? ((Script)c.thisObject).binding : null)
        HelperDatastore datastore = new HelperDatastore(builder: builder)
        (List<P>) datastore.execute(c).select(QueryType.ALL).from(pogoClass.simpleName, pogoClass).execute()
    }

    static <P> List<P> findAll(Class<P> pogoClass, QueryBuilder builder) {
        if (builder == null) throw new IllegalArgumentException("Query builder cannot be null!")
        (List<P>) builder.select(QueryType.ALL).from(pogoClass.simpleName, pogoClass).execute()
    }

    static <P> Iterator<P> iterate(Class<P> pogoClass, @DelegatesTo(value=QueryBuilder, strategy=Closure.DELEGATE_FIRST) Closure c = {}) {
        QueryBuilder builder = new QueryBuilder(c.thisObject instanceof Script ? ((Script)c.thisObject).binding : null)
        HelperDatastore datastore = new HelperDatastore(builder: builder)
        (Iterator<P>) datastore.execute(c).select(QueryType.ALL).from(pogoClass.simpleName, pogoClass).iterate()
    }


    static <P> Iterator<P> iterate(Class<P> pogoClass, QueryBuilder builder) {
        if (builder == null) throw new IllegalArgumentException("Query builder cannot be null!")
        (Iterator<P>) builder.select(QueryType.ALL).from(pogoClass.simpleName, pogoClass).iterate()
    }
}

@CompileStatic
class HelperDatastore {
    QueryBuilder builder

    QueryBuilder query(@DelegatesTo(value=QueryBuilder, strategy=Closure.DELEGATE_FIRST) Closure c) {
        prepareAndLaunchQuery c
    }

    QueryBuilder execute(@DelegatesTo(value=QueryBuilder, strategy=Closure.DELEGATE_FIRST) Closure c) {
        prepareAndLaunchQuery c
    }

    QueryBuilder iterate(@DelegatesTo(value=QueryBuilder, strategy=Closure.DELEGATE_FIRST) Closure c) {
        prepareAndLaunchQuery c
    }

    private QueryBuilder prepareAndLaunchQuery(@DelegatesTo(value=QueryBuilder, strategy=Closure.DELEGATE_FIRST) Closure c) {
        Closure cQuery = (Closure) c.clone()
        cQuery.resolveStrategy = Closure.DELEGATE_FIRST
        cQuery.setDelegate(builder)
        cQuery()
        return builder
    }
}
