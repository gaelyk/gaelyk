package groovyx.gaelyk.datastore

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

/**
 * Annotation for better object coercion of POGOs.
 *
 * This annotation perform transformation which adds <code>exists</code>,<code>save</code>, <code>delete</code> methods to the POGOs.
 * It also adds <code>get</code>,<code>count</code>,<code>find</code><code>findAll</code> and <code>iterate</code> static methods to
 * the annotated classes.
 * <br/>
 *
 * Property <code>id</code> of the type <code>long</code> is added to the class if annotation <code>Key</code>
 * is not present on any field. Properties of the class annotated with this annotation are unindexed by default.
 * Set {link #unidexed()} to <code>false</code> to change the default behavior. Use <code>Indexed</code>
 * annotation on property you want index.
 *
 * @author Vladimir Orany
 */


@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass(classes = [EntityTransformation])
@interface Entity {
    boolean unindexed() default true;
}
