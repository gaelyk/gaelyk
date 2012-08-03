package groovyx.gaelyk.datastore

import com.google.appengine.api.datastore.Entities
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.EntityNotFoundException
import groovyx.gaelyk.extensions.DatastoreExtensions

/**
 * Utility class handling the POGO to Entity coercion, and Entity to POGO coercion as well.
 *
 * @author Guillaume Laforge
 */
class PogoEntityCoercion {

    /**
     * Cached information about annotations present on POGO classes
     */
    private static Map<Class, Map> cachedProps = [:]

    /**
     * Goes through all the properties and finds how they are annotated.
     * @param p the object to introspect
     * @return a map whose keys consist of property names
     * and whose values are maps of ignore/unindexed/key/value keys and
     * values of closures returning booleans
     */
    static Map props(Object p) {
        def clazz = p.class
        boolean defaultIndexed = true;
        if(clazz.isAnnotationPresent(groovyx.gaelyk.datastore.Entity.class)){
            defaultIndexed = ! clazz.getAnnotation(groovyx.gaelyk.datastore.Entity).unindexed()
        }
        if (!cachedProps.containsKey(clazz)) {
            cachedProps[clazz] = p.properties.findAll { String k, v -> !(k in ['class', 'metaClass']) && !(k.startsWith('$') || k.startsWith('_')) }
                    .collectEntries { String k, v ->
                def annos
                try {
                    annos = p.class.getDeclaredField(k).annotations
                } catch (e) {
                    try {
                        annos = p.class.getDeclaredMethod("get${k.capitalize()}").annotations
                    } catch (NoSuchMethodException nsme){
                        return [(k), [ignore: {true}, unindexed: {false}, key: {false}]]
                    }
                }
                [(k), [
                        ignore:    { annos.any { it instanceof Ignore } },
                        unindexed: { defaultIndexed ? annos.any { it instanceof Unindexed } : !annos.any { it instanceof Indexed } },
                        key:       { annos.any { it instanceof Key } },
                        version:   { annos.any { it instanceof Version } }
                ]]
            }
        }

        return cachedProps[clazz]
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
    * Find the key in the properties
    *
    * @param props the properties
    * @return the name of the key or null if none is found
    */
    static String findVersion(Map props) {
        props.findResult { String prop, Map m ->
            if (m.version()) return prop
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
        def value = key ? p."$key" : null
        if (key && value) {
            entity = new Entity(p.class.simpleName, value)
        } else {
            entity = new Entity(p.class.simpleName)
        }

        props.each { String propName, Map m ->
            if (propName != key) {
                if (!props[propName].ignore() && !props[propName].version()) {
                    def val = p."$propName"
                    if (props[propName].unindexed()) {
                        // TODO: decide the correct behaviour
//                      if(!val){
//                          entity.removeProperty(propName)
//                      } else {
                        entity.setUnindexedProperty(propName, p."$propName")
//                  }
                    } else {

                        if (val instanceof Enum) val = val as String
                        entity.setProperty(propName, val)
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

        String version = findVersion(classProps)

        if (version) {
            try {
                if(e.key)  {
                   o."$version" = Entities.getVersionProperty(DatastoreExtensions.get(Entities.createEntityGroupKey(e.key)))
                }
            } catch (EntityNotFoundException ex){
                o."$version" = 0
            }
        }

        return o
    }
}
