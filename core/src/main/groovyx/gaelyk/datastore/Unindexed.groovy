package groovyx.gaelyk.datastore

import java.lang.annotation.Retention
import java.lang.annotation.Target
import static java.lang.annotation.ElementType.FIELD
import static java.lang.annotation.ElementType.METHOD
import static java.lang.annotation.RetentionPolicy.RUNTIME

/**
 * 
 * @author Guillaume Laforge
 */

@Retention(RUNTIME)
@Target([METHOD, FIELD])
@interface Unindexed { }