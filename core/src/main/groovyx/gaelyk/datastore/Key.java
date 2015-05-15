package groovyx.gaelyk.datastore;

import java.lang.annotation.*;

/**
 * Annotation for properties of a Groovy class that should be the key used in coercion to and from entities.
 *
 * @author Guillaume Laforge
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@Inherited
public @interface Key {
}
