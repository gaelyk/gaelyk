package groovyx.gaelyk.datastore;

import java.lang.annotation.*;

/**
 * Annotation for properties of a Groovy class that should be the parent used in coercion to and from entities.
 * <p/>
 * The property must be of type {@link com.google.appengine.api.datastore.Key}
 *
 * @author Guillaume Laforge
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@Inherited
public @interface Parent {
}
