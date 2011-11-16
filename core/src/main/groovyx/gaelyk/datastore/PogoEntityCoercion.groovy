package groovyx.gaelyk.datastore

import com.google.appengine.api.datastore.Entity

/**
 * Utility class handling the POGO to Entity coercion, and Entity to POGO coercion as well.
 *
 * @author Guillaume Laforge
 */
class PogoEntityCoercion {
    /**
     * Goes through all the properties and finds how they are annotated.
     * @param p the object to introspect
     * @return a map whose keys consist of property names
     * and whose values are maps of ignore/unindexed/key/value keys and
     * values of closures returning booleans
     */
    static Map props(Object p) {
        p.properties.findAll { String k, v -> !(k in ['class', 'metaClass']) }
                    .collectEntries { String k, v ->
            def annos
            try {
                annos = p.class.getDeclaredField(k).annotations
            } catch (e) {
                annos = p.class.getDeclaredMethod("get${k.capitalize()}").annotations
            }
            [(k), [
                    ignore:     { annos.any { it instanceof Ignore } },
                    unindexed:  { annos.any { it instanceof Unindexed } },
                    key:        { annos.any { it instanceof Key } },
                    value:      { v }
            ]]
        }
    }

    /**
     * Find the key in the properties
     *
     * @param props the properties
     * @return the name of the key or null if none is found
     */
    static String findKey(Map props) {
        props.findResult { String prop, Map m ->
            if (m.key()) return prop
        }
    }

    /**
     * Convert an object into an entity
     *
     * @param p the object
     * @return the entity
     */
    static Entity convert(Object p) {
        Entity entity
        
        Map props = props(p)
        String key = findKey(props)
        if (key) {
            entity = new Entity(p.class.simpleName, p.getProperty(key))
        } else {
            entity = new Entity(p.class.simpleName)
        }
        
        props.each { String propName, Map m ->
            if (propName != key) {
                if (!props[propName].ignore()) {
                    if (props[propName].unindexed()) {
                        entity.setUnindexedProperty(propName, props[propName].value())
                    } else {
                        entity.setProperty(propName, props[propName].value())
                    }
                }
            }
        }
        
        return entity
    }

    /**
     * Convert an entity into an object
     *
     * @param e the entity
     * @param clazz the class of the object to return
     * @return an instance of the class parameter
     */
    static Object convert(Entity e, Class clazz) {
        def entityProps = e.getProperties()

        def o = clazz.newInstance()
        entityProps.each { k, v ->
            if (o.metaClass.hasProperty(o, k)) {  
                o[k] = v
            }
        }
        
        def classProps = props(o)

        String key = findKey(classProps)

        if (key) {
            o."$key" = e.key.name ?: e.key.id
        }
        
        return o
    }
}
