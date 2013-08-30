package groovyx.gaelyk.datastore;

/**
 * Classes implementing this interface signals Gaelyk coercion
 * mechanism that the coercion is customized for this class.
 * 
 * Implementing classes must provide no-args constructor.
 * 
 * @author Vladimir Orany
 *
 * @param <T> implememnting type
 */
public interface Coercable<T> {
    
    /**
     * Coerce self from the given entity.
     * 
     * Usually called on empty new object.
     * 
     * Calling this method shouldn't change its owner anyway.
     * 
     * Never use {@link PogoEntityCoercion} directly in this class but 
     * you can use {@link DatastoreEntityCoercion} for classes implementing
     * {@link DatastoreEntity} or {@link ReflectionEntityCoercion} for the rest.
     * 
     * @param en source entity
     * @return self
     */
    T coerce(com.google.appengine.api.datastore.Entity en);
    
    
    /**
     * Coerce self to the Entity.
     * 
     * Calling this method shouldn't change its owner anyway.
     * 
     * Never use {@link PogoEntityCoercion} directly in this class but 
     * you can use {@link DatastoreEntityCoercion} for classes implementing
     * {@link DatastoreEntity} or {@link ReflectionEntityCoercion} for the rest.
     * 
     * @param t source object
     * 
     * @return entity filled with using this object
     */
    com.google.appengine.api.datastore.Entity coerce(T t);

}
