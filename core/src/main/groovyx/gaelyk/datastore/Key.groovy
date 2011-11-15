package groovyx.gaelyk.datastore

import java.lang.annotation.Retention
import java.lang.annotation.Target
import static java.lang.annotation.RetentionPolicy.*
import static java.lang.annotation.ElementType.*

/**
 * Annotation for properties of a Groovy class that should be the key used in coercion to and from entities.
 *
 * @author Guillaume Laforge
 */

@Retention(RUNTIME)
@Target([METHOD, FIELD])
@interface Key { }