package groovyx.gaelyk.datastore;

import groovyx.gaelyk.extensions.DatastoreExtensions;

import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;

public class DatastoreEntityCoercion {

    public static Entity convert(DatastoreEntity<?> dsEntity){
        if(dsEntity == null) return null;
        
        String kind = dsEntity.getClass().getSimpleName();
        Entity entity = null;
        
        if(dsEntity.hasDatastoreKey()){
            Object key = dsEntity.getDatastoreKey();
            if(key instanceof CharSequence && key != null){
                entity = new Entity(kind, key.toString());
            } else if(key instanceof Number && key != null){
                entity = new Entity(kind, ((Number)key).longValue());
            }
        }
        
        if(entity == null){
            entity = new Entity(kind);
        }
        
        for(String propertyName : dsEntity.getIndexedProperties()){
            entity.setProperty(propertyName, dsEntity.getProperty(propertyName));
        }
        for(String propertyName : dsEntity.getUnindexedProperties()){
            entity.setUnindexedProperty(propertyName, dsEntity.getProperty(propertyName));
        }
        
        return entity;
    }
    
    @SuppressWarnings("unchecked") public static <E extends DatastoreEntity<?>> E convert(Entity en, Class<E> dsEntityClass) throws InstantiationException, IllegalAccessException{
        E dsEntity = dsEntityClass.newInstance();
        if(dsEntity.hasDatastoreKey()){
            if(dsEntity.hasDatastoreNumericKey()){
                ((DatastoreEntity<Long>)dsEntity).setDatastoreKey(en.getKey().getId());
            } else {
                ((DatastoreEntity<String>)dsEntity).setDatastoreKey(en.getKey().getName());
            }
        }
        if (dsEntity.hasDatastoreVersion()) {
            try {
                dsEntity.setDatastoreVersion(Entities.getVersionProperty(DatastoreExtensions.get(Entities.createEntityGroupKey(en.getKey()))));                
            } catch(NullPointerException npe){
                dsEntity.setDatastoreVersion(0); 
            }
        }
        for(String propertyName : dsEntity.getIndexedProperties()){
            dsEntity.setProperty(propertyName, en.getProperty(propertyName));
        }
        for(String propertyName : dsEntity.getUnindexedProperties()){
            dsEntity.setProperty(propertyName, en.getProperty(propertyName));
        }
        return dsEntity;
    }
    
}
