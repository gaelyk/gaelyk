/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gaelyk.extensions

import groovy.lang.Closure;
import groovy.transform.CompileStatic
import com.google.appengine.api.datastore.Entity
import groovy.transform.PackageScope
import com.google.appengine.api.datastore.Text
import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.DatastoreServiceFactory
import java.util.concurrent.Future
import com.google.appengine.api.datastore.Transaction
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.AsyncDatastoreService
import com.google.appengine.api.datastore.KeyFactory
import com.google.appengine.api.datastore.Query
import groovyx.gaelyk.query.QueryBuilder
import groovyx.gaelyk.UnindexedEntityWrapper
import com.google.appengine.api.datastore.FetchOptions
import com.google.appengine.api.datastore.PreparedQuery
import org.codehaus.groovy.runtime.DefaultGroovyMethods

import groovyx.gaelyk.datastore.PogoEntityCoercion
import groovyx.gaelyk.datastore.ReflectionEntityCoercion
import com.google.appengine.api.datastore.TransactionOptions.Builder as TOB

/**
 * Extension methods dedicated to the low-level DataStore service
 *
 * @author Guillaume Laforge
 * @author Scott Murphy
 * @author Benjamin Muschko
 */
class DatastoreExtensions {

    /**
     * Convert a <code>Key</code> into its encoded <code>String</code> representation,
     * using the underlying <code>KeyFactory.keyToString()</code> conversion method.
     *
     * @param self the key
     * @param clazz the String class
     * @return the encoded string representation of the key
     */
    @CompileStatic
    static String asType(Key self, Class clazz) {
        if (clazz == String)
            KeyFactory.keyToString(self)
        else DefaultGroovyMethods.asType(self, clazz)
    }

    /**
     * Gaelyk supports a simplistic object/entity mapping, thanks to type coercion.
     * You can use this type coercion mechanism to coerce POJOs/POGOs and datastore Entities.
     * The <code>Entity</code> kind will be the simple name of the POJO/POGO (same approach as Objectify).
     * So with this mechanism, you can do:
     *
     * <pre><code>
     *  class Person { String name, int age }
     *
     *  def p = new Person(name: "Guillaume", age: 33)
     *  def e = p as Entity
     *
     *  assert p.name == e.name
     *  assert p.age == e.age
     * </code></pre>
     *
     * @return an instance of Entity
     */
    @CompileStatic
    static Object asType(Object self, Class clazz) {
        if (clazz == Entity) {
            PogoEntityCoercion.convert(self)
        } else if (self.class == Entity) {
            asType((Entity)self, clazz)
        } else if (self.class == String) {
            asType((String)self, clazz)
        } else DefaultGroovyMethods.asType(self, clazz)
    }

    /**
     * Gaelyk supports a simplistic object/entity mapping, thanks to type coercion.
     * You can use this type coercion mechanism to coerce POJOs/POGOs and datastore Entities.
     * The <code>Entity</code> kind will be the simple name of the POJO/POGO (same approach as Objectify).
     * So with this mechanism, you can do:
     *
     * <pre><code>
     *  class Person { String name, int age }
     *
     *  def e = new Entity("Person")
     *  e.name = "Guillaume"
     *  e.age = 33
     *
     *  def p = e as Person
     *
     *  assert e.name == p.name
     *  assert e.age == p.age
     * </code></pre>
     *
     * @return an instance of a POJO/POGO to coerce into
     */
    @CompileStatic
    static Object asType(Entity self, Class clazz) {
//        return clazz.newInstance(self.properties)
        return PogoEntityCoercion.convert(self, clazz)
    }

    /**
     * Provides a shortcut notation to get a property of an entity.
     * Instead of writing
     * <code>entity.getProperty('propertyName')</code>
     * You can use the shortcut
     * <code>entity['propertyName']</code>
     */
    @CompileStatic
    static Object getAt(Entity entity, String name) {
        if(!entity.hasProperty(name)){
            return null;
        }
        transformValueForRetrieval(entity.getProperty(name))
    }

    /**
     * Provides a shortcut notation to get a property of an entity.
     * Instead of writing
     * <code>entity.getProperty('propertyName')</code>
     * You can use the shortcut
     * <code>entity.propertyName</code>
     */
    @CompileStatic
    static Object get(Entity entity, String name) {
        if(!entity.hasProperty(name)){
            return null;
        }
        transformValueForRetrieval(entity.getProperty(name))
    }

    // All transformations that need to be done on entity fields
    // before being accessed by the user
    @PackageScope
    static Object transformValueForRetrieval(Object value) {
        value instanceof Text ? value.value : value
    }

    /**
     * Provides a shortcut notation to set a property of an entity.
     * Instead of writing
     * <code>entity.setProperty('propertyName', value)</code>
     * You can use the shortcut
     * <code>entity['propertyName'] = value</code>
     */
    @CompileStatic
    static void setAt(Entity entity, String name, Object value) {
// TODO: decide the correct behaviour
//      if(!value){
//          entity.removeProperty(name)
//          return
//      }
        entity.setProperty(name, transformValueForStorage(value))
    }

    /**
     * Provides a shortcut notation to set a property of an entity.
     * Instead of writing
     * <code>entity.setProperty('propertyName', value)</code>
     * You can use the shortcut
     * <code>entity.propertyName = value</code>
     */
    @CompileStatic
    static void set(Entity entity, String name, Object value) {
// TODO: decide the correct behaviour
//      if(!value){
//          entity.removeProperty(name)
//          return
//      }
        entity.setProperty(name, transformValueForStorage(value))
    }

    // All transformations that need to be done on entity fields
    // prior to their insertion in the datastore
    @CompileStatic
    static Object transformValueForStorage(Object value) {
        // the datastore doesn't allow to store GStringImpl
        // so we need a toString() first
        def newValue = value instanceof GString ? value.toString() : value
        // if we store a string longer than 500 characters
        // it needs to be wrapped in a Text instance
        if (newValue instanceof String && newValue.size() > 500) {
            newValue = new Text(newValue)
        }
        return newValue
    }

    /**
     * Save this entity in the data store.
     * Usage: <code>entity.save()</code>
     */
    @CompileStatic
    static Key save(Entity entity) {
        DatastoreServiceFactory.datastoreService.put(entity)
    }

    /**
     * Save this entity in the data store asynchronously.
     * Usage: <code>entity.asyncSave()</code>
     */
    @CompileStatic
    static Future<Key> asyncSave(Entity entity) {
        DatastoreServiceFactory.asyncDatastoreService.put(entity)
    }

    /**
     * Delete this entity from the data store.
     * Usage: <code>entity.delete()</code>
     */
    @CompileStatic
    static void delete(Entity entity) {
        DatastoreServiceFactory.datastoreService.delete(entity.key)
    }

    /**
     * Delete this entity from the data store.
     * Usage: <code>entity.asyncDelete()</code>
     */
    @CompileStatic
    static Future<Void> asyncDelete(Entity entity) {
        DatastoreServiceFactory.asyncDatastoreService.delete(entity.key)
    }

    /**
     * Delete the entity represented by that key, from the data store.
     * Usage: <code>key.delete()</code>
     */
    @CompileStatic
    static void delete(Key key) {
        DatastoreServiceFactory.datastoreService.delete(key)
    }

    /**
     * Fetch the entity associated with that key from the datastore.
     * Usage: <code>def entity = key.get()</code>
     *
     * @returns an entity
     */
    @CompileStatic
    static Entity get(Key key) {
        DatastoreServiceFactory.datastoreService.get(key)
    }

    /**
     * Fetch the entities associated with the collection of keys from the datastore.
     * Usage: <code>def entities = [key1, key2].get()</code>
     *
     * @returns a map of key and entity
     */
    @CompileStatic
    static Map<Key, Entity> get(Iterable<Key> keys) {
        DatastoreServiceFactory.datastoreService.get(keys)
    }

    /**
     * Fetch the entity associated with that key from the async datastore.
     * Usage: <code>def entityFuture = key.asyncGet()</code>
     *
     * @return an entity future
     */
    @CompileStatic
    static Future<Entity> asyncGet(Key key) {
        DatastoreServiceFactory.asyncDatastoreService.get(key)
    }

    /**
     * Fetch the entities associated with the collection of keys from the async datastore.
     * Usage: <code>def entitiesFuture = [key1, key2].asyncGet()</code>
     *
     * @return a map of key and future entity
     */
    @CompileStatic
    static Future<Map<Key, Entity>> asyncGet(Iterable<Key> keys) {
        DatastoreServiceFactory.asyncDatastoreService.get(keys)
    }

    /**
     * Delete the entity represented by that key, from the data store.
     * Usage: <code>key.delete()</code>
     */
    @CompileStatic
    static Future<Void> asyncDelete(Key key) {
        DatastoreServiceFactory.asyncDatastoreService.delete(key)
    }

    /**
     * With this method, transaction handling is done transparently.
     * The transaction is committed if the closure executed properly.
     * The transaction is rollbacked if anything went wrong.
     * You can use this method as follows:
     * <code>
     * datastore.withTransaction { transaction ->
     *     // do something in that transaction
     * }
     * </code>
     */
    @CompileStatic
    static Transaction withTransaction(DatastoreService service, Closure c) {
        return withTransaction(service, false, c)
    }
    
    /**
     * With this method, transaction handling is done transparently.
     * The transaction is committed if the closure executed properly.
     * The transaction is rollbacked if anything went wrong.
         * If you want to use cross-group transactions, pass {@literal true}
         * as an argument.
         * <p />
     * You can use this method as follows:
     * <code>
     * datastore.withTransaction(true) { transaction ->
     *     // do something in that transaction
     * }
     * </code>
     */
    @CompileStatic
    static Transaction withTransaction(DatastoreService service, boolean crossGroup, Closure c) {
        def opts = crossGroup ? TOB.withXG(true) : TOB.withDefaults()
        Transaction transaction = service.beginTransaction(opts)
        try {
            // pass the transaction as single parameter of the closure
            c(transaction)
            // commit the transaction if the closure executed without throwing an exception
            transaction.commit()
        } catch (e) {
            // rollback on error
            if (transaction.isActive()) {
                transaction.rollback()
            }
            // rethrow the exception
            throw e
        }
        return transaction
    }

    /**
     * With this method, transaction handling is done transparently.
     * The transaction is committed if the closure executed properly.
     * The transaction is rollbacked if anything went wrong.
     * You can use this method as follows:
     * <code>
     * datastore.async.withTransaction { transactionFuture ->
     *     // do something in that transaction
     * }
     * </code>
     */
    @CompileStatic
    static Future<Transaction> withTransaction(AsyncDatastoreService service, Closure c) {
        return withTransaction(service, false, c)
    }

    /**
     * With this method, transaction handling is done transparently.
     * The transaction is committed if the closure executed properly.
     * The transaction is rollbacked if anything went wrong.
         * If you want to use cross-group transactions, pass {@literal true}
         * as an argument.
         * <p />
     * You can use this method as follows:
     * <code>
     * datastore.withTransaction(true) { transactionFuture ->
     *     // do something in that transaction
     * }
     * </code>
     */
    @CompileStatic
    static Future<Transaction> withTransaction(AsyncDatastoreService service, boolean crossGroup, Closure c) {
        def opts = crossGroup ? TOB.withXG(true) : TOB.withDefaults()
        Future<Transaction> transaction = service.beginTransaction(opts)
        try {
            // pass the transaction as single parameter of the closure
            c(transaction)
            // commit the transaction if the closure executed without throwing an exception
            // blocks on the result of all async calls made since the transaction started
            transaction.get().commit()
        } catch (e) {
            // rollback on error
            if (transaction.get().isActive()) {
                try {
                    transaction.get().rollback()
                } catch (IllegalArgumentException iae) { }
            }
            // rethrow the exception
            throw e
        }
        transaction
    }

    /**
     * With this method, transaction handling is done transparently.
     * The transaction is asynchronously committed if the closure executed properly.
     * The transaction is rollbacked if anything went wrong.
     * You can use this method as follows:
     * <code>
     * datastore.async.withTransactionCommitAsync { transactionFuture ->
     *     // do something in that transaction
     * }
     * </code>
     * @return Future<Void> calling .get() blocks until all oustanding async calls have completed
     */
    @CompileStatic
    static Future<Void> withTransactionCommitAsync(AsyncDatastoreService service, Closure c) {
        Future<Transaction> transaction = service.beginTransaction()
        try {
            // pass the transaction as single parameter of the closure
            c(transaction)
            // commit the transaction asynchronously if the closure executed without throwing an exception
            return transaction.get().commitAsync()
        } catch (e) {
            // rollback on error
            if (transaction.get().isActive()) {
                transaction.get().rollback()
            }
            // rethrow the exception
            throw e
        }
    }

    /**
     * Set the <code>Entity</code> properties with the key / value pairs of the map,
     * using the leftshift operator as follows:
     * <code>entity &lt;&lt; params</code>
     */
    static Entity leftShift(Entity entity, Map params) {
        params.each { k, v -> entity[k] = v }
        return entity
    }

    /**
     * @return the asynchronous datastore service.
     */
    @CompileStatic
    static AsyncDatastoreService getAsync(DatastoreService service) {
        DatastoreServiceFactory.asyncDatastoreService
    }

    /**
     * Get an entity from the datastore.
     *
     * @param parentKey the parent key
     * @param kind the kind
     * @param id the id
     * @return the entity identified by its parent key, its kind, and id, retrieved from the datastore
     */
    @CompileStatic
    static Entity get(DatastoreService service, Key parentKey, String kind, long id) {
        service.get(KeyFactory.createKey(parentKey, kind, id))
    }

    /**
     * Get an entity from the datastore.
     *
     * @param parentKey the parent key
     * @param kind the kind
     * @param name the name
     * @return the entity identified by its parent key, its kind, and name, retrieved from the datastore
     */
    @CompileStatic
    static Entity get(DatastoreService service, Key parentKey, String kind, String name) {
        service.get(KeyFactory.createKey(parentKey, kind, name))
    }

    /**
     * Get an entity from the datastore.
     *
     * @param kind the kind
     * @param id the id
     * @return the entity identified by its kind, and id, retrieved from the datastore
     */
    @CompileStatic
    static Entity get(DatastoreService service, String kind, long id) {
        service.get(KeyFactory.createKey(kind, id))
    }

    /**
     * Get an entity from the datastore.
     *
     * @param kind the kind
     * @param name the name
     * @return the entity identified by its kind, and name, retrieved from the datastore
     */
    @CompileStatic
    static Entity get(DatastoreService service, String kind, String name) {
        service.get(KeyFactory.createKey(kind, name))
    }

    /**
     * Get an entity from the async datastore.
     *
     * @param parentKey the parent key
     * @param kind the kind
     * @param id the id
     * @return the entity future identified by its parent key, its kind, and id, retrieved from the datastore
     */
    @CompileStatic
    static Future<Entity> get(AsyncDatastoreService service, Key parentKey, String kind, long id) {
        service.get(KeyFactory.createKey(parentKey, kind, id))
    }

    /**
     * Get an entity from the async datastore.
     *
     * @param parentKey the parent key
     * @param kind the kind
     * @param name the name
     * @return the entity future identified by its parent key, its kind, and name, retrieved from the datastore
     */
    @CompileStatic
    static Future<Entity> get(AsyncDatastoreService service, Key parentKey, String kind, String name) {
        service.get(KeyFactory.createKey(parentKey, kind, name))
    }

    /**
     * Get an entity from the async datastore.
     *
     * @param kind the kind
     * @param id the id
     * @return the entity future identified by its kind, and id, retrieved from the datastore
     */
    @CompileStatic
    static Future<Entity> get(AsyncDatastoreService service, String kind, long id) {
        service.get(KeyFactory.createKey(kind, id))
    }

    /**
     * Get an entity from the async datastore.
     *
     * @param kind the kind
     * @param name the name
     * @return the entity future identified by its kind, and name, retrieved from the datastore
     */
    @CompileStatic
    static Future<Entity> get(AsyncDatastoreService service, String kind, String name) {
        service.get(KeyFactory.createKey(kind, name))
    }

    /**
     * Create a query to be later executed on the datastore data.
     *
     * @param c the closure representing the query
     * @return the query
     */
    static Query query(DatastoreService service, @DelegatesTo(value=QueryBuilder, strategy=Closure.DELEGATE_FIRST) Closure c) {
        Closure cQuery = c.clone()
        cQuery.resolveStrategy = Closure.DELEGATE_FIRST
        def builder = new QueryBuilder(c.thisObject instanceof Script ? c.thisObject.binding : null)
        cQuery.delegate = builder
        cQuery()
        return builder.createQuery()
    }

    /**
     * Create and executes a prepared query to retrieve entities from the datastore.
     *
     * @param c the closure representing the query to execute
     * @return the results
     */
    @CompileStatic
    static execute(DatastoreService service, @DelegatesTo(value=QueryBuilder, strategy=Closure.DELEGATE_FIRST) Closure c) {
        QueryBuilder builder = prepareAndLaunchQuery(c)
        return builder.execute()
    }

    /**
     * Create and executes a prepared query to retrieve entities from the datastore in the form of an iterator.
     *
     * @param c the closure representing the query to execute
     * @return the iterator over the results
     */
    @CompileStatic
    static iterate(DatastoreService service, @DelegatesTo(value=QueryBuilder, strategy=Closure.DELEGATE_FIRST) Closure c) {
        QueryBuilder builder = prepareAndLaunchQuery(c)
        return builder.iterate()
    }

    private static QueryBuilder prepareAndLaunchQuery(@DelegatesTo(value=QueryBuilder, strategy=Closure.DELEGATE_FIRST) Closure c) {
        Closure cQuery = c.clone()
        cQuery.resolveStrategy = Closure.DELEGATE_FIRST
        def builder = new QueryBuilder(c.thisObject instanceof Script ? c.thisObject.binding : null)
        cQuery.delegate = builder
        cQuery()
        return builder
    }

    /**
     * Adds an <code>unindexed</code> property to entities to wrap entities,
     * so as to set unindexed properties on the entity.
     *
     * @return a wrapper for an entity
     */
    @CompileStatic
    static UnindexedEntityWrapper getUnindexed(Entity entity) {
        new UnindexedEntityWrapper(entity)
    }

    /**
     * Gaelyk supports a simplistic object/entity mapping, thanks to type coercion.
     * You can use this type coercion mechanism to coerce POJOs/POGOs and datastore Entities.
     * The <code>Future<Entity></code> kind will be the simple name of the POJO/POGO (same approach as Objectify).
     * So with this mechanism, you can do:
     *
     * <pre><code>
     *  class Person { String name, int age }
     *
     *  def e = key.asyncGet()
     *
     *  def p = e as Person
     *
     *  assert e.name == p.name
     *  assert e.age == p.age
     * </code></pre>
     *
     * @return an instance of a POJO/POGO to coerce into
     */
    @CompileStatic
    static Object asType(Future<Entity> self, Class clazz) {
        asType(self.get(), clazz)
    }

    /**
     * Set the <code>Future<Entity></code> properties with the key / value pairs of the map,
     * using the leftshift operator as follows:
     * <code>futureEntity &lt;&lt; params</code>
     */
    @CompileStatic
    static Future<Entity> leftShift(Future<Entity> future, Map params) {
        leftShift(future.get(), params)
        return future
    }

    /**
     * Save this entity future in the data store.
     * Usage: <code>futureEntity.save()</code>
     */
    @CompileStatic
    static Key save(Future<Entity> future) {
        save(future.get())
    }

    /**
     * Save this entity future in the data store asynchronously.
     * Usage: <code>futureEntity.asyncSave()</code>
     */
    @CompileStatic
    static Future<Key> asyncSave(Future<Entity> future) {
        asyncSave(future.get())
    }

    /**
     * Delete this entity from the data store.
     * Usage: <code>entity.delete()</code>
     */
    @CompileStatic
    static void delete(Future<Entity> future) {
        delete(future.get())
    }

    /**
     * Delete this entity from the data store.
     * Usage: <code>entity.asyncDelete()</code>
     */
    @CompileStatic
    static Future<Void> asyncDelete(Future<Entity> future) {
        asyncDelete(future.get())
    }

    /**
     * Convenience method to retrieve the key from a Future Entity
     * Usage: <code>future.key</code>
     */
    @CompileStatic
    static Key getKey(Future<Entity> future) {
        future.get().key
    }

    // ------------------------------------
    // Querying datastore metadata
    // (contribution from Benjamin Muschko)
    // ------------------------------------

    static final DEFAULT_FETCH_OPTIONS = FetchOptions.Builder.withDefaults()

    /**
     * Gets datastore namespaces
     *
     * @param service Datastore service
     * @param options Fetch options
     * @return Entities
     */
    @CompileStatic
    static List<Entity> getNamespaces(DatastoreService service, FetchOptions options = DEFAULT_FETCH_OPTIONS) {
        queryMetaData(service, options, Query.NAMESPACE_METADATA_KIND)
    }

    /**
     * Gets datastore namespaces. The closure lets you apply additional filters to your query.
     *
     * @param service Datastore service
     * @param options Fetch options
     * @param closure Closure
     * @return Entities
     */
    @CompileStatic
    static List<Entity> getNamespaces(DatastoreService service, FetchOptions options = DEFAULT_FETCH_OPTIONS, Closure closure) {
        queryMetaData(service, options, Query.NAMESPACE_METADATA_KIND, closure)
    }

    /**
     * Gets datastore kinds.
     *
     * @param service Datastore service
     * @param options Fetch options
     * @return Entities
     */
    @CompileStatic
    static List<Entity> getKinds(DatastoreService service, FetchOptions options = DEFAULT_FETCH_OPTIONS) {
        queryMetaData(service, options, Query.KIND_METADATA_KIND)
    }

    /**
     * Gets datastore kinds. The closure lets you apply additional filters to your query.
     *
     * @param service Datastore service
     * @param options Fetch options
     * @param closure Closure
     * @return Entities
     */
    @CompileStatic
    static List<Entity> getKinds(DatastoreService service, FetchOptions options = DEFAULT_FETCH_OPTIONS, Closure closure) {
        queryMetaData(service, options, Query.KIND_METADATA_KIND, closure)
    }

    /**
     * Gets all datastore kinds and their properties.
     *
     * @param service Datastore service
     * @param options Fetch options
     * @return Entities
     */
    @CompileStatic
    static List<Entity> getProperties(DatastoreService service, FetchOptions options = DEFAULT_FETCH_OPTIONS) {
        queryMetaData(service, options, Query.PROPERTY_METADATA_KIND)
    }

    /**
     * Gets all datastore kinds and their properties. The closure lets you apply additional filters to your query.
     *
     * @param service Datastore service
     * @param kind Kind
     * @param options Fetch options
     * @param closure Closure
     * @return Entities
     */
    @CompileStatic
    static List<Entity> getProperties(DatastoreService service, FetchOptions options = DEFAULT_FETCH_OPTIONS, Closure closure) {
        queryMetaData(service, options, Query.PROPERTY_METADATA_KIND, closure)
    }

    /**
     * Gets datastore kind properties.
     *
     * @param service Datastore service
     * @param kind Kind
     * @param options Fetch options
     * @return Entities
     */
    @CompileStatic
    static List<Entity> getProperties(DatastoreService service, String kind, FetchOptions options = DEFAULT_FETCH_OPTIONS) {
        Query query = new Query(Query.PROPERTY_METADATA_KIND)
        query.setAncestor(createKindKey(kind))
        PreparedQuery preparedQuery = service.prepare(query)
        preparedQuery.asList(options)
    }

    /**
     * Gets datastore kind properties. The closure lets you apply additional filters to your query.
     *
     * @param service Datastore service
     * @param kind Kind
     * @param options Fetch options
     * @param closure Closure
     * @return Entities
     */
    @CompileStatic
    static List<Entity> getProperties(DatastoreService service, String kind, FetchOptions options = DEFAULT_FETCH_OPTIONS, Closure closure) {
        Query query = new Query(Query.PROPERTY_METADATA_KIND)
        query.setAncestor(createKindKey(kind))
        closure(query)
        PreparedQuery preparedQuery = service.prepare(query)
        preparedQuery.asList(options)
    }

    /**
     * Gets datastore kind property.
     *
     * @param service Datastore service
     * @param kind Kind
     * @param property Property
     * @return Entity
     */
    @CompileStatic
    static Entity getProperty(DatastoreService service, String kind, String property) {
        Query query = new Query(Query.PROPERTY_METADATA_KIND)
        query.setAncestor(createPropertyKey(kind, property))
        PreparedQuery preparedQuery = service.prepare(query)
        preparedQuery.asSingleEntity()
    }

    /**
     * Queries for meta data.
     *
     * @param service Datastore service
     * @param options Fetch options
     * @param metaDataQuery Query
     * @return Entities
     */
    @CompileStatic
    private static List<Entity> queryMetaData(DatastoreService service, FetchOptions options, String metaDataQuery) {
        Query query = new Query(metaDataQuery)
        PreparedQuery preparedQuery = service.prepare(query)
        preparedQuery.asList(options)
    }

    /**
     * Queries for meta data.
     *
     * @param service Datastore service
     * @param options Fetch options
     * @param metaDataQuery Query
     * @param closure Closure
     * @return Entities
     */
    private static List<Entity> queryMetaData(DatastoreService service, FetchOptions options, String metaDataQuery, Closure closure) {
        Query query = new Query(metaDataQuery)
        closure(query)
        PreparedQuery preparedQuery = service.prepare(query)
        preparedQuery.asList(options)
    }

    /**
     * Creates kind key
     *
     * @param kind Kind
     * @return Key
     */
    @CompileStatic
    private static Key createKindKey(String kind) {
        KeyFactory.createKey(Query.KIND_METADATA_KIND, kind)
    }

    /**
     * Creates property key.
     *
     * @param kind Kind
     * @param property Property
     * @return Key
     */
    @CompileStatic
    private static Key createPropertyKey(String kind, String property) {
        KeyFactory.createKey(createKindKey(kind), Query.PROPERTY_METADATA_KIND, property)
    }
}
