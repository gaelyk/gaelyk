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
package groovyx.gaelyk.datastore

import java.lang.reflect.Modifier;

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
				def isStatic = false
                try {
                    def field = p.class.getDeclaredField(k)
					isStatic = Modifier.isStatic(field.modifiers)
                    annos = field.annotations
                } catch (e) {
                    try {
                        def method = p.class.getDeclaredMethod("get${k.capitalize()}")
                        annos = method.annotations
						isStatic = Modifier.isStatic(method.modifiers)
                    } catch (NoSuchMethodException nsme){
                        return [(k), [ignore: {true}, unindexed: {false}, key: {false}, version: { false }]]
                    }
                }
                [(k), [
                        ignore:    { isStatic || annos.any { it instanceof Ignore } },
                        unindexed: { isStatic || defaultIndexed ? annos.any { it instanceof Unindexed } : !annos.any { it instanceof Indexed } },
                        key:       { !isStatic && annos.any { it instanceof Key } },
                        version:   { !isStatic && annos.any { it instanceof Version } }
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
            if (m.containsKey('key') && m.key()) return prop
        }
    }

    /**
    * Find the version in the properties
    *
    * @param props the properties
    * @return the name of the key or null if none is found
    */
    static String findVersion(Map props) {
        props.findResult { String prop, Map m ->
            if (m.containsKey('version') && m.version()) return prop
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
