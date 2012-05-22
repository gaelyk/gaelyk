package groovyx.gaelyk.datastore

import java.lang.annotation.Retention
import java.lang.annotation.Target
import static java.lang.annotation.RetentionPolicy.*
import static java.lang.annotation.ElementType.*

/**
 * Annotation for properties of a Groovy class in which the current version of the entity should be injected.
 *
 * @author Guillaume Laforge
 */

@Retention(RUNTIME)
@Target([METHOD, FIELD])
@interface Version { }