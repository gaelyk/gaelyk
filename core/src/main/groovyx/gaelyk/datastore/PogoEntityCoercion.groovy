package groovyx.gaelyk.datastore

import com.google.appengine.api.datastore.Entity

/**
 *
 * @author Guillaume Laforge
 */
class PogoEntityCoercion {
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
    
    static String findKey(Map props) {
        props.findResult { String prop, Map m ->
            if (m.key()) return prop
        }
    }

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

    static Object convert(Entity e, Class clazz) {
        def entityProps = e.getProperties()

        def o = clazz.newInstance(entityProps)
        
        def classProps = props(o)

        String key = findKey(classProps)

        if (key) {
            o."$key" = e.key.name ?: e.key.id
        }
        
        return o
    }
}
