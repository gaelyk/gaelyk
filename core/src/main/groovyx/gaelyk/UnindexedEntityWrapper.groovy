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
package groovyx.gaelyk

import groovy.transform.CompileStatic
import groovyx.gaelyk.extensions.DatastoreExtensions

import com.google.appengine.api.datastore.Entity

/**
 * Wrapper around an entity, so as to be able to set unindexed properties, with:
 * <pre><code>
 *     def person = new Entity("Person")
 *     person.unindexed.bio = "..." // or unindexed['bio']
 * </code></pre>
 *
 * @author Guillaume Laforge
 */
@CompileStatic
class UnindexedEntityWrapper {
    Entity entity

    /**
     * Wrap an entity
     * @param entity to be wrapped
     */
    UnindexedEntityWrapper(Entity entity) {
        this.entity = entity
    }

    /**
     * Set an unindexed property with the property notation: <code>entity.unindexed.prop = ...</code>
     *
     * @param name of the unindexed property
     * @param value of the unindexed property
     */
    void setProperty(String name, value) {
        entity.setUnindexedProperty(name, DatastoreExtensions.transformValueForStorage(value))
    }

    /**
     * Set an unindexed property with the subscript notation: <code>entity.unindexed['prop'] = ...</code>
     *
     * @param name of the unindexed property
     * @param value of the unindexed property
     */
    void setAt(String name, value) {
        entity.setUnindexedProperty(name, DatastoreExtensions.transformValueForStorage(value))
    }

    /**
     * Get any property with the property notation: <code>def val = entity.unindexed.prop</code>
     *
     * @param name of the property
     * @param value of the property
     */
    Object getProperty(String name) {
        DatastoreExtensions.get(this.entity, name)
    }

    /**
     * Get any property with the property notation: <code>def val = entity.unindexed['prop']</code>
     *
     * @param name of the property
     * @param value of the property
     */
    void getAt(String name) {
        DatastoreExtensions.get(this.entity, name)
    }
}
