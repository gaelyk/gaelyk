package groovyx.gaelyk.datastore;

import java.lang.annotation.*;

/**
 * Annotation for properties of a Groovy class that should be set as indexed in coercion to and from entities.
 *
 * @author Vladimir Orany
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@Inherited
public @interface Indexed {
}
