package groovyx.gaelyk.datastore;

import java.util.List;

/**
 * Helper interface to speed up Object to Entity coercion skipping unnecessary reflection which
 * is extremely expensive on Google App Engine.
 * <p>
 * If class implements this interface manually and one of following annotations are present, the annotations has no effect.
 * </p>
 * <ul>
 * <li>{@link Key}</li>
 * <li>{@link Version}</li>
 * <li>{@link Indexed}</li>
 * <li>{@link Unindexed}</li>
 * </ul>
 * 
 * @param I
 *            class of the identifier - either {@link String} or {@link Long}
 * @author Vladimir Orany
 */
public interface DatastoreEntity<K> {
    /**
     * Returns <code>true</code> if the entity has key property.
     * 
     * @return <code>true</code> if the entity has key property
     */
    boolean hasDatastoreKey();

    /**
     * Returns key of the entity if present and <code>null</code> otherwise.
     * 
     * @return key of the entity if present and <code>null</code> otherwise
     */
    K getDatastoreKey();

    /**
     * Sets the key of the entity, do nothing if the entity does not have key property.
     * 
     * @param key
     *            the key of the entity to be set
     */
    void setDatastoreKey(K key);
    
    /**
     * Returns <code>true</code> if data store key is numeric.
     * @return <code>true</code> if data store key is numeric
     */
    boolean hasDatastoreNumericKey();

    /**
     * Returns <code>true</code> if the entity has version property.
     * 
     * @return <code>true</code> if the entity has version property
     */
    boolean hasDatastoreVersion();

    /**
     * Returns version of the entity if present and <code>null</code> otherwise.
     * 
     * @return version of the entity if present and <code>null</code> otherwise
     */
    long getDatastoreVersion();

    /**
     * Sets the version of the entity, do nothing if the entity does not have version property.
     * 
     * @param version
     *            the id of the entity to be set
     */
    void setDatastoreVersion(long version);

    /**
     * Returns list of the names of properties which should be saved in the data store with the index.
     * This method should return same values for each instance. It cannot be static because of Java interface restrictions.
     * 
     * @return list of the names of properties which should be saved in the data store with the index
     */
    List<String> getDatastoreIndexedProperties();

    /**
     * Returns list of the names of properties which should be saved in the data store unindexed.
     * This method should return same values for each instance. It cannot be static because of Java interface restrictions.
     * 
     * @return list of the names of properties which should be saved in the data store unindexed
     */
    List<String> getDatastoreUnindexedProperties();
    
    // taken from GroovyObject, for java compatibility. Groovy classes has these methods
    // automagically
    
    /**
     * Retrieves a property value.
     *
     * @param propertyName the name of the property of interest
     * @return the given property
     */
    Object getProperty(String propertyName);

    /**
     * Sets the given property to the new value.
     *
     * @param propertyName the name of the property of interest
     * @param newValue     the new value for the property
     */
    void setProperty(String propertyName, Object newValue);

}
