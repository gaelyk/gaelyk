package groovyx.gaelyk

import com.google.appengine.api.datastore.Entity
import static GaelykCategory.transformValueForStorage
import groovy.transform.CompileStatic

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
        entity.setUnindexedProperty(name, GaelykCategory.transformValueForStorage(value))
    }

    /**
     * Set an unindexed property with the subscript notation: <code>entity.unindexed['prop'] = ...</code>
     *
     * @param name of the unindexed property
     * @param value of the unindexed property
     */
    void setAt(String name, value) {
        entity.setUnindexedProperty(name, GaelykCategory.transformValueForStorage(value))
    }
}
