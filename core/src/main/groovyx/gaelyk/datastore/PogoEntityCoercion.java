package groovyx.gaelyk.datastore;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class PogoEntityCoercion {

    public static Entity convert(Object dsEntity){
        if(dsEntity instanceof DatastoreEntity<?>){
            return DatastoreEntityCoercion.convert((DatastoreEntity<?>) dsEntity);
        }
        return ReflectionEntityCoercion.convert(dsEntity);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" }) public static <E> E convert(Entity en, Class<E> cls) throws InstantiationException, IllegalAccessException{
        if(Entity.class.equals(cls)){
            return (E) en;
        }
        if(Key.class.equals(cls)){
            return (E) en.getKey();
        }
        if(DatastoreEntity.class.isAssignableFrom(cls)){
            return (E) DatastoreEntityCoercion.convert(en,(Class<? extends DatastoreEntity>) cls);
        }
        return (E) ReflectionEntityCoercion.convert(en, cls);
    }

    
    
}
