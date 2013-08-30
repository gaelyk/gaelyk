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

import groovy.transform.CompileStatic
import groovyx.gaelyk.extensions.DatastoreExtensions

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

import com.google.appengine.api.datastore.Entities
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.EntityNotFoundException

/**
 * Utility class handling the POGO to Entity coercion, and Entity to POGO coercion as well.
 *
 * @author Guillaume Laforge
 */
@CompileStatic
class ReflectionEntityCoercion {

    /**
     * Cached information about annotations present on POGO classes
     */
    private static Map<Class, Map<String, PropertyDescriptor>> cachedProps = [:]

    /**
     * Goes through all the properties and finds how they are annotated.
     * @param p the object to introspect
     * @return a map whose keys consist of property names
     * and whose values are maps of ignore/unindexed/key/value keys and
     * values of closures returning booleans
     */
    // XXX: if the static compilation is skipped, props[property] = value works good
    // @CompileStatic(TypeCheckingMode.SKIP)
    static Map<String, PropertyDescriptor> props(Object p) {
        def clazz = p.class
        boolean defaultIndexed = true
        if (clazz.isAnnotationPresent(groovyx.gaelyk.datastore.Entity.class)) {
            defaultIndexed = !clazz.getAnnotation(groovyx.gaelyk.datastore.Entity).unindexed()
        }
        if (!cachedProps.containsKey(clazz)) {
            Map<String, PropertyDescriptor> props = [:]
            for (String property in p.properties.keySet()) {
                if (!(property in ['class', 'metaClass']) && !(property.startsWith('$') || property.startsWith('_'))) {
                    def descriptor = getPropertyDescriptorFor(clazz, property, defaultIndexed)
                    // XXX: This does not work. If it is Groovy bug, it's really nasty!
                    // props[property] = descriptor
                    props.put property, descriptor
                } else {
                    // XXX: This does not work. If it is Groovy bug, it's really nasty!
                    // props[property] = PropertyDescriptor.IGNORED
                    props.put property, PropertyDescriptor.IGNORED
                }
            }
            cachedProps[clazz] = props
        }

        return cachedProps[clazz]
    }

    static PropertyDescriptor getPropertyDescriptorFor(Class clazz, String property, boolean defaultIndexed) {
        Field f = null
        Method m = null
        try {
            f = clazz.getDeclaredField(property)
        } catch (e) {
            try {
                if(!clazz.getDeclaredMethods().any { Method it -> it.name == "set${property.capitalize()}" && it.parameterTypes.length == 1}){
                    throw new NoSuchMethodException("set${property.capitalize()}")
                }
                m = clazz.getDeclaredMethod("get${property.capitalize()}")
            } catch (NoSuchMethodException nsme) {
                if(clazz.superclass && clazz.superclass != Object) {
                    return getPropertyDescriptorFor(clazz.superclass, property, defaultIndexed)
                }
                return PropertyDescriptor.IGNORED
            }
        }
        boolean isIgnoredByDefault = f ? (Modifier.isStatic(f.getModifiers()) || Modifier.isFinal(f.getModifiers())) : Modifier.isStatic(m.getModifiers())
        def annos = f ? f.annotations : m.annotations
        if (isIgnoredByDefault || annos.any { it instanceof Ignore }) return PropertyDescriptor.IGNORED
        if (annos.any { it instanceof Key }) return PropertyDescriptor.KEY
        if (annos.any { it instanceof Version }) return PropertyDescriptor.VERSION
        if (annos.any { it instanceof Parent }) return PropertyDescriptor.PARENT
        if (defaultIndexed ? annos.any { it instanceof Unindexed } : !annos.any { it instanceof Indexed }) return PropertyDescriptor.UNINDEXED
        PropertyDescriptor.INDEXED
    }

    /**
     * Find the key in the properties
     *
     * @param props the properties
     * @return the name of the key or null if none is found
     */
    static String findKey(Map<String, PropertyDescriptor> props) {
        props.findResult { String prop, PropertyDescriptor m ->
            if (m.key()) return prop
        }
    }

    /**
     * Find the parent in the properties
     *
     * @param props the properties
     * @return the name of the key or null if none is found
     */
    static String findParent(Map<String, PropertyDescriptor> props) {
        props.findResult { String prop, PropertyDescriptor m ->
            if (m.parent()) return prop
        }
    }

    /**
     * Find the version in the properties
     *
     * @param props the properties
     * @return the name of the key or null if none is found
     */
    static String findVersion(Map<String, PropertyDescriptor> props) {
        props.findResult { String prop, PropertyDescriptor m ->
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

        Map<String, PropertyDescriptor> props = props(p)
        String key = findKey(props)
        String parent = findParent(props)
        def value = key ? p.metaClass.getProperty(p, key) : null
        com.google.appengine.api.datastore.Key parentKey = parent ? p.metaClass.getProperty(p, parent) : null
        if (key && value) {
            if (value instanceof CharSequence && value) {
                if(parentKey){
                    entity = new Entity(p.class.simpleName, value?.toString(), parentKey)
                } else {
                    entity = new Entity(p.class.simpleName, value?.toString())
                }
            } else if(value instanceof Number && value) {
                if(parentKey){
                    entity = new Entity(p.class.simpleName, ((Number) value).longValue(), parentKey)
                } else {
                    entity = new Entity(p.class.simpleName, ((Number) value).longValue())
                }
            } else if(parentKey) {
                entity = new Entity(p.class.simpleName, parentKey)
            }
        } else {
            entity = new Entity(p.class.simpleName)
        }

        props.each { String propName, PropertyDescriptor m ->
            if (propName != key && propName != parent) {
                if (!props[propName].ignore() && !props[propName].version() && !props[propName].parent()) {
                    def val = p.metaClass.getProperty(p, propName)
                    if (props[propName].unindexed()) {
                        entity.setUnindexedProperty(propName, val)
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
    static convert(Entity e, Class clazz, o = clazz.newInstance()) {
        def entityProps = e.getProperties()
        
        if (o instanceof Map) {
            entityProps.each { k, v ->
                (o as Map)[k] = v
            }
            o['id'] = e.key.name ?: e.key.id
            if(e.key.parent){
                o['parent'] = e.key.parent
            }
        } else {
            entityProps.each { String k, v ->
                if (o.metaClass.hasProperty(o, k)) {
                    try {
                        o[k] = v                        
                    } catch(ReadOnlyPropertyException rope){
                        // cannot set read only property!
                    }
                }
            }

            Map<String, PropertyDescriptor> classProps = props(o)

            String key = findKey(classProps)

            if (key) {
                o.metaClass.setProperty(o, key, e.key.name ?: e.key.id)
            }

            String version = findVersion(classProps)

            if (version) {
                try {
                    if (e.key) {
                        o.metaClass.setProperty(o, version, Entities.getVersionProperty(DatastoreExtensions.get(Entities.createEntityGroupKey(e.key))))
                    }
                } catch (EntityNotFoundException ex) {
                    o.metaClass.setProperty(o, version, 0)
                }
            }
            String parent = findParent(classProps)
            if(parent){
                o.metaClass.setProperty(o, parent, e.key.parent)
            }
        }
        return o
    }
}

@CompileStatic
enum PropertyDescriptor {
    //           ignore,unindex,key,    version
    IGNORED {
        boolean ignore() { true }
    },
    KEY {
        boolean key() { true }
    },
    VERSION {
        boolean version() { true }
    },
    INDEXED,
    UNINDEXED {
        boolean unindexed() { true }
    },
    PARENT {
        boolean parent() { true }
    }

    boolean ignore() { false }

    boolean unindexed() { false }

    boolean key() { false }

    boolean version() { false }

    boolean parent() { false }
}
