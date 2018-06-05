package groovyx.gaelyk.datastore;

import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;

import groovy.lang.GString;
import groovyx.gaelyk.extensions.DatastoreExtensions;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DatastoreEntityCoercion {

    public static enum Proxy {
        INSTANCE;

        Entity convert(DatastoreEntity dsEntity){
            return DatastoreEntityCoercion.convert(dsEntity);
        }

        public static Object convert(Entity en, Class dsEntityClass, DatastoreEntity dsEntity) throws InstantiationException, IllegalAccessException{
            return DatastoreEntityCoercion.convert(en, dsEntityClass, dsEntity);
        }

        public static Object convert(Entity en, Class dsEntityClass) throws InstantiationException, IllegalAccessException{
            return DatastoreEntityCoercion.convert(en, dsEntityClass);
        }

    }

    public static Entity convert(DatastoreEntity<?> dsEntity){
        if(dsEntity == null) return null;
        
        String kind = dsEntity.getClass().getSimpleName();
        Entity entity = null;
        
        if(dsEntity.hasDatastoreKey()){
            Object key = dsEntity.getDatastoreKey();
            if(key instanceof CharSequence && key != null){
                if (dsEntity.hasDatastoreParent()) {
                    entity = new Entity(kind, key.toString(), dsEntity.getDatastoreParent());
                } else {
                    entity = new Entity(kind, key.toString());
                }
            } else if (key instanceof Number && key != null && ((Number) key).longValue() != 0) {
                if (dsEntity.hasDatastoreParent()) {
                    entity = new Entity(kind, ((Number) key).longValue(), dsEntity.getDatastoreParent());
                } else {
                    entity = new Entity(kind, ((Number) key).longValue());
                }
            } else if(dsEntity.hasDatastoreParent()){
                entity = new Entity(kind, dsEntity.getDatastoreParent());
            }
        }
        
        if(entity == null){
            entity = new Entity(kind);
        }
        
        for (String propertyName : dsEntity.getDatastoreIndexedProperties()) {
            Object value = dsEntity.getProperty(propertyName);
            try {
                entity.setProperty(propertyName, transformValueForStorage(value));
            } catch (Exception e) {
                throw new IllegalArgumentException("Problem setting value '" + value + "' to indexed property '" + propertyName + "' of entity "
                        + dsEntity.getClass().getSimpleName(), e);
            }
        }
        for (String propertyName : dsEntity.getDatastoreUnindexedProperties()) {
            Object value = dsEntity.getProperty(propertyName);
            try {
                entity.setUnindexedProperty(propertyName, transformValueForStorage(value));
            } catch (Exception e) {
                throw new IllegalArgumentException("Problem setting value '" + value + "' to unindexed property '" + propertyName + "' of entity "
                        + dsEntity.getClass().getSimpleName(), e);
            }
        }
        
        return entity;
    }
    
    private static Object transformValueForStorage(Object value) {
        Object newValue = (value instanceof GString || value instanceof Enum<?>) ? value.toString() : value;
        // if we store a string longer than 1500 bytes
        // it needs to be wrapped in a Text instance
        // See https://github.com/gaelyk/gaelyk/issues/222
        if (newValue instanceof String && ((String)newValue).getBytes(UTF_8).length > 1500) {
            newValue = new Text((String) newValue);
        }
        return newValue;
    }
    
    @SuppressWarnings("unchecked") public static <E extends DatastoreEntity<?>> E convert(Entity en, Class<E> dsEntityClass) throws InstantiationException, IllegalAccessException{
        return convert(en, dsEntityClass, dsEntityClass.newInstance());
    }

    @SuppressWarnings("unchecked") public static <E extends DatastoreEntity<?>> E convert(Entity en, Class<E> dsEntityClass, E dsEntity) throws InstantiationException, IllegalAccessException{
        
        if(dsEntity.hasDatastoreKey()){
            if(dsEntity.hasDatastoreNumericKey()){
                ((DatastoreEntity<Long>)dsEntity).setDatastoreKey(en.getKey().getId());
            } else {
                ((DatastoreEntity<String>)dsEntity).setDatastoreKey(en.getKey().getName());
            }
        }
        if (dsEntity.hasDatastoreParent()) {
            dsEntity.setDatastoreParent(en.getKey().getParent());
        }
        if (dsEntity.hasDatastoreVersion()) {
            try {
                dsEntity.setDatastoreVersion(Entities.getVersionProperty(DatastoreExtensions.get(Entities.createEntityGroupKey(en.getKey()))));                
            } catch (Exception e) {
                dsEntity.setDatastoreVersion(0); 
            }
        }
        for(String propertyName : dsEntity.getDatastoreIndexedProperties()){
            setEntityProperty(en, dsEntity, propertyName);
        }
        for(String propertyName : dsEntity.getDatastoreUnindexedProperties()){
            setEntityProperty(en, dsEntity, propertyName);
        }
        return dsEntity;
    }

    private static <E extends DatastoreEntity<?>> void setEntityProperty(Entity en, E dsEntity, String propertyName) {
        if (!en.hasProperty(propertyName)) {
            // the property doesn't have the property set so let it blank
            // this is important for keeping default values
            return;
        }
        Object value = en.getProperty(propertyName);
        if (value instanceof Text) {
            value = ((Text) value).getValue();
        } 
        try {
            dsEntity.setProperty(propertyName, value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Problem setting value '" + value + "' to property '" + propertyName + "' of entity " + dsEntity.getClass().getSimpleName(), e);
        }

    }
    
}
