package groovyx.gaelyk.datastore;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class PogoEntityCoercion {

    public static Entity convert(Object dsEntity){
        if (dsEntity instanceof Coercable<?>){
            return ((Coercable<?>) dsEntity).coerce(); 
        }
        if (dsEntity instanceof DatastoreEntity<?>){
            return DatastoreEntityCoercion.convert((DatastoreEntity<?>) dsEntity);
        }
        return ReflectionEntityCoercion.convert(dsEntity);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" }) public static <E> E convert(Entity en, Class<E> cls) throws InstantiationException, IllegalAccessException{
        if (Entity.class.equals(cls)){
            return (E) en;
        }
        if (Key.class.equals(cls)){
            return (E) en.getKey();
        }
        if (Coercable.class.isAssignableFrom(cls)) {
            return ((Coercable<E>)cls.newInstance()).coerce(en);
        }
        if (DatastoreEntity.class.isAssignableFrom(cls)){
            return (E) DatastoreEntityCoercion.convert(en,(Class<? extends DatastoreEntity>) cls);
        }
        return (E) ReflectionEntityCoercion.convert(en, cls);
    }

    
    
}
