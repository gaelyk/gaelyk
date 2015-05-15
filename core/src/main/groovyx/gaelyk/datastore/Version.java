package groovyx.gaelyk.datastore;

import java.lang.annotation.*;

/**
 * Annotation for properties of a Groovy class in which the current version of the entity should be injected.
 * <p/>
 * Obtaining the version each time might be time consuming. Use this annotation wisely.
 *
 * @author Guillaume Laforge
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@Inherited
public @interface Version {
}
