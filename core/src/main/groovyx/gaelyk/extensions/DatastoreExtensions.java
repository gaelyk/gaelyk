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
package groovyx.gaelyk.extensions;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.GString;
import groovy.lang.Script;
import groovyx.gaelyk.UnindexedEntityWrapper;
import groovyx.gaelyk.datastore.PogoEntityCoercion;
import groovyx.gaelyk.query.QueryBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

/**
 * Extension methods dedicated to the low-level DataStore service
 * 
 * @author Guillaume Laforge
 * @author Scott Murphy
 * @author Benjamin Muschko
 * @author Vladimir Orany
 */
public class DatastoreExtensions {

    /**
     * Convert a <code>Key</code> into its encoded <code>String</code> representation,
     * using the underlying <code>KeyFactory.keyToString()</code> conversion method.
     * 
     * @param self
     *            the key
     * @param clazz
     *            the String class
     * @return the encoded string representation of the key
     */
    public static <T> T asType(Key self, Class<T> clazz) {
        if (clazz == String.class) return clazz.cast(KeyFactory.keyToString(self));
        return clazz.cast(DefaultGroovyMethods.asType(self, clazz));
    }

    /**
     * Gaelyk supports a simplistic object/entity mapping, thanks to type coercion.
     * You can use this type coercion mechanism to coerce POJOs/POGOs and datastore Entities.
     * The <code>Entity</code> kind will be the simple name of the POJO/POGO (same approach as Objectify).
     * So with this mechanism, you can do:
     * 
     * <pre>
     * <code>
     *  class Person { String name, int age }
     * 
     *  def p = new Person(name: "Guillaume", age: 33)
     *  def e = p as Entity
     * 
     *  assert p.name == e.name
     *  assert p.age == e.age
     * </code>
     * </pre>
     * 
     * @return an instance of Entity
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public static <T> T asType(Object self, Class<T> clazz) throws InstantiationException, IllegalAccessException {
        if (clazz == Entity.class) { return clazz.cast(PogoEntityCoercion.convert(self)); }
        if (self.getClass() == Entity.class) { return clazz.cast(asType((Entity) self, clazz)); }
        if (self.getClass() == String.class) { return clazz.cast(asType((String) self, clazz)); }
        return DefaultGroovyMethods.asType(self, clazz);
    }

    /**
     * Gaelyk supports a simplistic object/entity mapping, thanks to type coercion.
     * You can use this type coercion mechanism to coerce POJOs/POGOs and datastore Entities.
     * The <code>Entity</code> kind will be the simple name of the POJO/POGO (same approach as Objectify).
     * So with this mechanism, you can do:
     * 
     * <pre>
     * <code>
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
     * </code>
     * </pre>
     * 
     * @return an instance of a POJO/POGO to coerce into
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public static <T> T asType(Entity self, Class<T> clazz) throws InstantiationException, IllegalAccessException {
        return clazz.cast(PogoEntityCoercion.convert(self, clazz));
    }

    /**
     * Provides a shortcut notation to get a property of an entity.
     * Instead of writing <code>entity.getProperty('propertyName')</code> You can use the shortcut <code>entity['propertyName']</code>
     */
    public static Object getAt(Entity entity, String name) {
        if (!entity.hasProperty(name)) { return null; }
        return transformValueForRetrieval(entity.getProperty(name));
    }

    /**
     * Provides a shortcut notation to get a property of an entity.
     * Instead of writing <code>entity.getProperty('propertyName')</code> You can use the shortcut <code>entity.propertyName</code>
     */
    public static Object get(Entity entity, String name) {
        if (!entity.hasProperty(name)) { return null; }
        return transformValueForRetrieval(entity.getProperty(name));
    }

    public static Object transformValueForRetrieval(Object value) {
        return value instanceof Text ? ((Text) value).getValue() : value;
    }

    /**
     * Provides a shortcut notation to set a property of an entity.
     * Instead of writing <code>entity.setProperty('propertyName', value)</code> You can use the shortcut <code>entity['propertyName'] = value</code>
     */
    public static void setAt(Entity entity, String name, Object value) {
        entity.setProperty(name, transformValueForStorage(value));
    }

    /**
     * Provides a shortcut notation to set a property of an entity.
     * Instead of writing <code>entity.setProperty('propertyName', value)</code> You can use the shortcut <code>entity.propertyName = value</code>
     */
    public static void set(Entity entity, String name, Object value) {
        entity.setProperty(name, transformValueForStorage(value));
    }

    // All transformations that need to be done on entity fields
    // prior to their insertion in the datastore
    public static Object transformValueForStorage(Object value) {
        // the datastore doesn't allow to store GStringImpl
        // so we need a toString() first
        Object newValue = value instanceof GString ? value.toString() : value;
        // if we store a string longer than 500 characters
        // it needs to be wrapped in a Text instance
        if (newValue instanceof String && ((String) newValue).length() > 500) {
            newValue = new Text((String) newValue);
        }
        return newValue;
    }

    /**
     * Save this entity in the data store.
     * Usage: <code>entity.save()</code>
     */
    public static Key save(Entity entity) {
        return DatastoreServiceFactory.getDatastoreService().put(entity);
    }

    /**
     * Save this entity in the data store asynchronously.
     * Usage: <code>entity.asyncSave()</code>
     */
    public static Future<Key> asyncSave(Entity entity) {
        return DatastoreServiceFactory.getAsyncDatastoreService().put(entity);
    }

    /**
     * Delete this entity from the data store.
     * Usage: <code>entity.delete()</code>
     */
    public static void delete(Entity entity) {
        DatastoreServiceFactory.getDatastoreService().delete(entity.getKey());
    }

    /**
     * Delete this entity from the data store.
     * Usage: <code>entity.asyncDelete()</code>
     */
    public static Future<Void> asyncDelete(Entity entity) {
        return DatastoreServiceFactory.getAsyncDatastoreService().delete(entity.getKey());
    }

    /**
     * Delete the entity represented by that key, from the data store.
     * Usage: <code>key.delete()</code>
     */
    public static void delete(Key key) {
        DatastoreServiceFactory.getDatastoreService().delete(key);
    }

    /**
     * Fetch the entity associated with that key from the datastore.
     * Usage: <code>def entity = key.get()</code>
     * @throws EntityNotFoundException 
     * 
     * @returns an entity
     */
    public static Entity get(Key key) throws EntityNotFoundException {
        return DatastoreServiceFactory.getDatastoreService().get(key);
    }

    /**
     * Fetch the entities associated with the collection of keys from the datastore.
     * Usage: <code>def entities = [key1, key2].get()</code>
     * 
     * @returns a map of key and entity
     */
    public static Map<Key, Entity> get(Iterable<Key> keys) {
        return DatastoreServiceFactory.getDatastoreService().get(keys);
    }

    /**
     * Fetch the entity associated with that key from the async datastore.
     * Usage: <code>def entityFuture = key.asyncGet()</code>
     * 
     * @return an entity future
     */
    public static Future<Entity> asyncGet(Key key) {
        return DatastoreServiceFactory.getAsyncDatastoreService().get(key);
    }

    /**
     * Fetch the entities associated with the collection of keys from the async datastore.
     * Usage: <code>def entitiesFuture = [key1, key2].asyncGet()</code>
     * 
     * @return a map of key and future entity
     */
    public static Future<Map<Key, Entity>> asyncGet(Iterable<Key> keys) {
        return DatastoreServiceFactory.getAsyncDatastoreService().get(keys);
    }

    /**
     * Delete the entity represented by that key, from the data store.
     * Usage: <code>key.delete()</code>
     */
    public static Future<Void> asyncDelete(Key key) {
        return DatastoreServiceFactory.getAsyncDatastoreService().delete(key);
    }

    /**
     * With this method, transaction handling is done transparently.
     * The transaction is committed if the Closure<?> executed properly.
     * The transaction is rollbacked if anything went wrong.
     * You can use this method as follows: <code>
     * datastore.withTransaction { transaction ->
     *     // do something in that transaction
     * }
     * </code>
     */
    public static Transaction withTransaction(DatastoreService service, Closure<?> c) {
        return withTransaction(service, false, c);
    }

    /**
     * With this method, transaction handling is done transparently.
     * The transaction is committed if the Closure<?> executed properly.
     * The transaction is rollbacked if anything went wrong.
     * If you want to use cross-group transactions, pass {@literal true} as an argument.
     * <p />
     * You can use this method as follows: <code>
     * datastore.withTransaction(true) { transaction ->
     *     // do something in that transaction
     * }
     * </code>
     */
    public static Transaction withTransaction(DatastoreService service, boolean crossGroup, Closure<?> c) {
        TransactionOptions opts = crossGroup ? TransactionOptions.Builder.withXG(true) : TransactionOptions.Builder.withDefaults();
        Transaction transaction = service.beginTransaction(opts);
        try {
            // pass the transaction as single parameter of the closure
            c.call(transaction);
            // commit the transaction if the Closure<?> executed without throwing an exception
            transaction.commit();
        } catch (Exception e) {
            // rollback on error
            if (transaction.isActive()) {
                transaction.rollback();
            }
            // rethrow the exception
            throw e;
        }
        return transaction;
    }

    /**
     * With this method, transaction handling is done transparently.
     * The transaction is committed if the Closure<?> executed properly.
     * The transaction is rollbacked if anything went wrong.
     * You can use this method as follows: <code>
     * datastore.async.withTransaction { transactionFuture ->
     *     // do something in that transaction
     * }
     * </code>
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public static Future<Transaction> withTransaction(AsyncDatastoreService service, Closure<?> c) throws InterruptedException, ExecutionException {
        return withTransaction(service, false, c);
    }

    /**
     * With this method, transaction handling is done transparently.
     * The transaction is committed if the Closure<?> executed properly.
     * The transaction is rollbacked if anything went wrong.
     * If you want to use cross-group transactions, pass {@literal true} as an argument.
     * <p />
     * You can use this method as follows: <code>
     * datastore.withTransaction(true) { transactionFuture ->
     *     // do something in that transaction
     * }
     * </code>
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public static Future<Transaction> withTransaction(AsyncDatastoreService service, boolean crossGroup, Closure<?> c) throws InterruptedException, ExecutionException {
        TransactionOptions opts = crossGroup ? TransactionOptions.Builder.withXG(true) : TransactionOptions.Builder.withDefaults();
        Future<Transaction> transaction = service.beginTransaction(opts);
        try {
            // pass the transaction as single parameter of the closure
            c.call(transaction);
            // commit the transaction if the Closure<?> executed without throwing an exception
            // blocks on the result of all async calls made since the transaction started
            transaction.get().commit();
        } catch (Exception e) {
            // rollback on error
            if (transaction.get().isActive()) {
                try {
                    transaction.get().rollback();
                } catch (IllegalArgumentException iae) {}
            }
            // rethrow the exception
            throw e;
        }
        return transaction;
    }

    /**
     * With this method, transaction handling is done transparently.
     * The transaction is asynchronously committed if the Closure<?> executed properly.
     * The transaction is rollbacked if anything went wrong.
     * You can use this method as follows: <code>
     * datastore.async.withTransactionCommitAsync { transactionFuture ->
     *     // do something in that transaction
     * }
     * </code>
     * 
     * @return Future<Void> calling .get() blocks until all oustanding async calls have completed
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public static Future<Void> withTransactionCommitAsync(AsyncDatastoreService service, Closure<?> c) throws InterruptedException, ExecutionException {
        Future<Transaction> transaction = service.beginTransaction();
        try {
            // pass the transaction as single parameter of the closure
            c.call(transaction);
            // commit the transaction asynchronously if the Closure<?> executed without throwing an exception
            return transaction.get().commitAsync();
        } catch (Exception e) {
            // rollback on error
            if (transaction.get().isActive()) {
                transaction.get().rollback();
            }
            // rethrow the exception
            throw e;
        }
    }

    /**
     * Set the <code>Entity</code> properties with the key / value pairs of the map,
     * using the leftshift operator as follows: <code>entity &lt;&lt; params</code>
     */
    public static Entity leftShift(Entity entity, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            entity.setProperty(entry.getKey(), entry.getValue());
        }
        return entity;
    }

    /**
     * @return the asynchronous datastore service.
     */
    public static AsyncDatastoreService getAsync(DatastoreService service) {
        return DatastoreServiceFactory.getAsyncDatastoreService();
    }

    /**
     * Get an entity from the datastore.
     * 
     * @param parentKey
     *            the parent key
     * @param kind
     *            the kind
     * @param id
     *            the id
     * @return the entity identified by its parent key, its kind, and id, retrieved from the datastore
     * @throws EntityNotFoundException 
     */
    public static Entity get(DatastoreService service, Key parentKey, String kind, long id) throws EntityNotFoundException {
        return service.get(KeyFactory.createKey(parentKey, kind, id));
    }

    /**
     * Get an entity from the datastore.
     * 
     * @param parentKey
     *            the parent key
     * @param kind
     *            the kind
     * @param name
     *            the name
     * @return the entity identified by its parent key, its kind, and name, retrieved from the datastore
     * @throws EntityNotFoundException 
     */
    public static Entity get(DatastoreService service, Key parentKey, String kind, String name) throws EntityNotFoundException {
        return service.get(KeyFactory.createKey(parentKey, kind, name));
    }

    /**
     * Get an entity from the datastore.
     * 
     * @param kind
     *            the kind
     * @param id
     *            the id
     * @return the entity identified by its kind, and id, retrieved from the datastore
     * @throws EntityNotFoundException 
     */
    public static Entity get(DatastoreService service, String kind, long id) throws EntityNotFoundException {
        return service.get(KeyFactory.createKey(kind, id));
    }

    /**
     * Get an entity from the datastore.
     * 
     * @param kind
     *            the kind
     * @param name
     *            the name
     * @return the entity identified by its kind, and name, retrieved from the datastore
     * @throws EntityNotFoundException 
     */
    public static Entity get(DatastoreService service, String kind, String name) throws EntityNotFoundException {
        return service.get(KeyFactory.createKey(kind, name));
    }

    /**
     * Get an entity from the async datastore.
     * 
     * @param parentKey
     *            the parent key
     * @param kind
     *            the kind
     * @param id
     *            the id
     * @return the entity future identified by its parent key, its kind, and id, retrieved from the datastore
     */
    public static Future<Entity> get(AsyncDatastoreService service, Key parentKey, String kind, long id) {
        return service.get(KeyFactory.createKey(parentKey, kind, id));
    }

    /**
     * Get an entity from the async datastore.
     * 
     * @param parentKey
     *            the parent key
     * @param kind
     *            the kind
     * @param name
     *            the name
     * @return the entity future identified by its parent key, its kind, and name, retrieved from the datastore
     */
    public static Future<Entity> get(AsyncDatastoreService service, Key parentKey, String kind, String name) {
        return service.get(KeyFactory.createKey(parentKey, kind, name));
    }

    /**
     * Get an entity from the async datastore.
     * 
     * @param kind
     *            the kind
     * @param id
     *            the id
     * @return the entity future identified by its kind, and id, retrieved from the datastore
     */
    public static Future<Entity> get(AsyncDatastoreService service, String kind, long id) {
        return service.get(KeyFactory.createKey(kind, id));
    }

    /**
     * Get an entity from the async datastore.
     * 
     * @param kind
     *            the kind
     * @param name
     *            the name
     * @return the entity future identified by its kind, and name, retrieved from the datastore
     */
    public static Future<Entity> get(AsyncDatastoreService service, String kind, String name) {
        return service.get(KeyFactory.createKey(kind, name));
    }

    /**
     * Create a query to be later executed on the datastore data.
     * 
     * @param c
     *            the Closure<?> representing the query
     * @return the query
     */
    public static Query query(DatastoreService service, @DelegatesTo(value = QueryBuilder.class, strategy = Closure.DELEGATE_FIRST) Closure<?> c) {
        return build(service, c).createQuery();
    }

    /**
     * Prepares {@link QueryBuilder} to be executed later.
     * 
     * @param c
     *            the Closure<?> representing the query
     * @return the query builder
     */
    public static QueryBuilder build(DatastoreService service, @DelegatesTo(value = QueryBuilder.class, strategy = Closure.DELEGATE_FIRST) Closure<?> c) {
        Closure<?> cQuery = (Closure<?>) c.clone();
        cQuery.setResolveStrategy(Closure.DELEGATE_FIRST);
        QueryBuilder builder = new QueryBuilder(c.getThisObject() instanceof Script ? ((Script) c.getThisObject()).getBinding() : null);
        cQuery.setDelegate(builder);
        cQuery.call();
        return builder;
    }

    /**
     * Create and executes a prepared query to retrieve entities from the datastore.
     * 
     * @param c
     *            the Closure<?> representing the query to execute
     * @return the results
     */
    public static Object execute(DatastoreService service, @DelegatesTo(value = QueryBuilder.class, strategy = Closure.DELEGATE_FIRST) Closure<?> c) {
        QueryBuilder builder = build(service, c);
        return builder.execute();
    }

    /**
     * Create and executes a prepared query to retrieve entities from the datastore in the form of an iterator.
     * 
     * @param c
     *            the Closure<?> representing the query to execute
     * @return the iterator over the results
     */
    public static Object iterate(DatastoreService service, @DelegatesTo(value = QueryBuilder.class, strategy = Closure.DELEGATE_FIRST) Closure<?> c) {
        QueryBuilder builder = build(service, c);
        return builder.iterate();
    }

    /**
     * Adds an <code>unindexed</code> property to entities to wrap entities,
     * so as to set unindexed properties on the entity.
     * 
     * @return a wrapper for an entity
     */
    public static UnindexedEntityWrapper getUnindexed(Entity entity) {
        return new UnindexedEntityWrapper(entity);
    }

    /**
     * Gaelyk supports a simplistic object/entity mapping, thanks to type coercion.
     * You can use this type coercion mechanism to coerce POJOs/POGOs and datastore Entities.
     * The <code>Future<Entity></code> kind will be the simple name of the POJO/POGO (same approach as Objectify).
     * So with this mechanism, you can do:
     * 
     * <pre>
     * <code>
     *  class Person { String name, int age }
     * 
     *  def e = key.asyncGet()
     * 
     *  def p = e as Person
     * 
     *  assert e.name == p.name
     *  assert e.age == p.age
     * </code>
     * </pre>
     * 
     * @return an instance of a POJO/POGO to coerce into
     * @throws ExecutionException 
     * @throws InterruptedException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public static <T> T asType(Future<Entity> self, Class<T> clazz) throws InstantiationException, IllegalAccessException, InterruptedException, ExecutionException {
        return clazz.cast(asType(self.get(), clazz));
    }

    /**
     * Set the <code>Future<Entity></code> properties with the key / value pairs of the map,
     * using the leftshift operator as follows: <code>futureEntity &lt;&lt; params</code>
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public static Future<Entity> leftShift(Future<Entity> future, Map<String, Object> params) throws InterruptedException, ExecutionException {
        leftShift(future.get(), params);
        return future;
    }

    /**
     * Save this entity future in the data store.
     * Usage: <code>futureEntity.save()</code>
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public static Key save(Future<Entity> future) throws InterruptedException, ExecutionException {
        return save(future.get());
    }

    /**
     * Save this entity future in the data store asynchronously.
     * Usage: <code>futureEntity.asyncSave()</code>
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public static Future<Key> asyncSave(Future<Entity> future) throws InterruptedException, ExecutionException {
        return asyncSave(future.get());
    }

    /**
     * Delete this entity from the data store.
     * Usage: <code>entity.delete()</code>
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public static void delete(Future<Entity> future) throws InterruptedException, ExecutionException {
        delete(future.get());
    }

    /**
     * Delete this entity from the data store.
     * Usage: <code>entity.asyncDelete()</code>
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public static Future<Void> asyncDelete(Future<Entity> future) throws InterruptedException, ExecutionException {
        return asyncDelete(future.get());
    }

    /**
     * Convenience method to retrieve the key from a Future Entity
     * Usage: <code>future.key</code>
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public static Key getKey(Future<Entity> future) throws InterruptedException, ExecutionException {
        return future.get().getKey();
    }

    // ------------------------------------
    // Querying datastore metadata
    // (contribution from Benjamin Muschko)
    // ------------------------------------

    private static final FetchOptions DEFAULT_FETCH_OPTIONS = FetchOptions.Builder.withDefaults();

    /**
     * Gets datastore namespaces
     * 
     * @param service
     *            Datastore service
     * @param options
     *            Fetch options
     * @return Entities
     */
    public static List<Entity> getNamespaces(DatastoreService service, FetchOptions options) {
        return queryMetaData(service, options, Query.NAMESPACE_METADATA_KIND);
    }

    /**
     * Gets datastore namespaces
     * 
     * @param service
     *            Datastore service
     * @return Entities
     */
    public static List<Entity> getNamespaces(DatastoreService service) {
        return queryMetaData(service, DEFAULT_FETCH_OPTIONS, Query.NAMESPACE_METADATA_KIND);
    }

    /**
     * Gets datastore namespaces. The Closure<?> lets you apply additional filters to your query.
     * 
     * @param service
     *            Datastore service
     * @param options
     *            Fetch options
     * @param Closure
     *            <?> Closure
     * @return Entities
     */
    public static List<Entity> getNamespaces(DatastoreService service, FetchOptions options, Closure<?> closure) {
        return queryMetaData(service, options, Query.NAMESPACE_METADATA_KIND, closure);
    }

    /**
     * Gets datastore namespaces. The Closure<?> lets you apply additional filters to your query.
     * 
     * @param service
     *            Datastore service
     * @param Closure
     *            <?> Closure
     * @return Entities
     */
    public static List<Entity> getNamespaces(DatastoreService service, Closure<?> closure) {
        return queryMetaData(service, DEFAULT_FETCH_OPTIONS, Query.NAMESPACE_METADATA_KIND, closure);
    }

    /**
     * Gets datastore kinds.
     * 
     * @param service
     *            Datastore service
     * @param options
     *            Fetch options
     * @return Entities
     */
    public static List<Entity> getKinds(DatastoreService service, FetchOptions options) {
        return queryMetaData(service, options, Query.KIND_METADATA_KIND);
    }

    /**
     * Gets datastore kinds.
     * 
     * @param service
     *            Datastore service
     * @return Entities
     */
    public static List<Entity> getKinds(DatastoreService service) {
        return queryMetaData(service, DEFAULT_FETCH_OPTIONS, Query.KIND_METADATA_KIND);
    }

    /**
     * Gets datastore kinds. The Closure<?> lets you apply additional filters to your query.
     * 
     * @param service
     *            Datastore service
     * @param options
     *            Fetch options
     * @param Closure
     *            <?> Closure
     * @return Entities
     */
    public static List<Entity> getKinds(DatastoreService service, FetchOptions options, Closure<?> closure) {
        return queryMetaData(service, options, Query.KIND_METADATA_KIND, closure);
    }

    /**
     * Gets datastore kinds. The Closure<?> lets you apply additional filters to your query.
     * 
     * @param service
     *            Datastore service
     * @param Closure
     *            <?> Closure
     * @return Entities
     */
    public static List<Entity> getKinds(DatastoreService service, Closure<?> closure) {
        return queryMetaData(service, DEFAULT_FETCH_OPTIONS, Query.KIND_METADATA_KIND, closure);
    }

    /**
     * Gets all datastore kinds and their properties.
     * 
     * @param service
     *            Datastore service
     * @param options
     *            Fetch options
     * @return Entities
     */
    public static List<Entity> getProperties(DatastoreService service, FetchOptions options) {
        return queryMetaData(service, options, Query.PROPERTY_METADATA_KIND);
    }

    /**
     * Gets all datastore kinds and their properties.
     * 
     * @param service
     *            Datastore service
     * @return Entities
     */
    public static List<Entity> getProperties(DatastoreService service) {
        return queryMetaData(service, DEFAULT_FETCH_OPTIONS, Query.PROPERTY_METADATA_KIND);
    }

    /**
     * Gets all datastore kinds and their properties. The Closure<?> lets you apply additional filters to your query.
     * 
     * @param service
     *            Datastore service
     * @param kind
     *            Kind
     * @param options
     *            Fetch options
     * @param Closure
     *            <?> Closure
     * @return Entities
     */
    public static List<Entity> getProperties(DatastoreService service, FetchOptions options, Closure<?> closure) {
        return queryMetaData(service, options, Query.PROPERTY_METADATA_KIND, closure);
    }

    /**
     * Gets all datastore kinds and their properties. The Closure<?> lets you apply additional filters to your query.
     * 
     * @param service
     *            Datastore service
     * @param kind
     *            Kind
     * @param Closure
     *            <?> Closure
     * @return Entities
     */
    public static List<Entity> getProperties(DatastoreService service, Closure<?> closure) {
        return queryMetaData(service, DEFAULT_FETCH_OPTIONS, Query.PROPERTY_METADATA_KIND, closure);
    }

    /**
     * Gets datastore kind properties.
     * 
     * @param service
     *            Datastore service
     * @param kind
     *            Kind
     * @param options
     *            Fetch options
     * @return Entities
     */
    public static List<Entity> getProperties(DatastoreService service, String kind, FetchOptions options) {
        Query query = new Query(Query.PROPERTY_METADATA_KIND);
        query.setAncestor(createKindKey(kind));
        PreparedQuery preparedQuery = service.prepare(query);
        return preparedQuery.asList(options);
    }

    /**
     * Gets datastore kind properties.
     * 
     * @param service
     *            Datastore service
     * @param kind
     *            Kind
     * @return Entities
     */
    public static List<Entity> getProperties(DatastoreService service, String kind) {
        return getProperties(service, kind, DEFAULT_FETCH_OPTIONS);
    }

    /**
     * Gets datastore kind properties. The Closure<?> lets you apply additional filters to your query.
     * 
     * @param service
     *            Datastore service
     * @param kind
     *            Kind
     * @param options
     *            Fetch options
     * @param Closure
     *            <?> Closure
     * @return Entities
     */
    public static List<Entity> getProperties(DatastoreService service, String kind, FetchOptions options, Closure<?> closure) {
        Query query = new Query(Query.PROPERTY_METADATA_KIND);
        query.setAncestor(createKindKey(kind));
        closure.call(query);
        PreparedQuery preparedQuery = service.prepare(query);
        return preparedQuery.asList(options);
    }

    /**
     * Gets datastore kind properties. The Closure<?> lets you apply additional filters to your query.
     * 
     * @param service
     *            Datastore service
     * @param kind
     *            Kind
     * @param Closure
     *            <?> Closure
     * @return Entities
     */
    public static List<Entity> getProperties(DatastoreService service, String kind, Closure<?> closure) {
        return getProperties(service, kind, DEFAULT_FETCH_OPTIONS, closure);
    }

    /**
     * Gets datastore kind property.
     * 
     * @param service
     *            Datastore service
     * @param kind
     *            Kind
     * @param property
     *            Property
     * @return Entity
     */
    public static Entity getProperty(DatastoreService service, String kind, String property) {
        Query query = new Query(Query.PROPERTY_METADATA_KIND);
        query.setAncestor(createPropertyKey(kind, property));
        PreparedQuery preparedQuery = service.prepare(query);
        return preparedQuery.asSingleEntity();
    }

    /**
     * Queries for meta data.
     * 
     * @param service
     *            Datastore service
     * @param options
     *            Fetch options
     * @param metaDataQuery
     *            Query
     * @return Entities
     */
    private static List<Entity> queryMetaData(DatastoreService service, FetchOptions options, String metaDataQuery) {
        Query query = new Query(metaDataQuery);
        PreparedQuery preparedQuery = service.prepare(query);
        return preparedQuery.asList(options);
    }

    /**
     * Queries for meta data.
     * 
     * @param service
     *            Datastore service
     * @param options
     *            Fetch options
     * @param metaDataQuery
     *            Query
     * @param Closure
     *            <?> Closure
     * @return Entities
     */
    private static List<Entity> queryMetaData(DatastoreService service, FetchOptions options, String metaDataQuery, Closure<?> closure) {
        Query query = new Query(metaDataQuery);
        closure.call(query);
        PreparedQuery preparedQuery = service.prepare(query);
        return preparedQuery.asList(options);
    }

    /**
     * Creates kind key
     * 
     * @param kind
     *            Kind
     * @return Key
     */
    private static Key createKindKey(String kind) {
        return KeyFactory.createKey(Query.KIND_METADATA_KIND, kind);
    }

    /**
     * Creates property key.
     * 
     * @param kind
     *            Kind
     * @param property
     *            Property
     * @return Key
     */
    private static Key createPropertyKey(String kind, String property) {
        return KeyFactory.createKey(createKindKey(kind), Query.PROPERTY_METADATA_KIND, property);
    }
}
