package groovyx.gaelyk

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

/**
 * If you annotate your classes with the <code>@GaelykBindings</code> annotation,
 * the compiler will automatically inject the variables and services
 * that are usually injected in your Groovlets and templates.
 * <p>
 * Example:
 * <pre><code>
 *  import groovyx.gaelyk.GaelykBindings
 *
 *  // annotate your class with the transformation
 *  @GaelykBindings
 *  class WeblogService {
 *      def numberOfComments(post) {
 *          // the datastore service is available
 *          datastore.execute {
 *              select count from comments where postId == post.id
 *          }
 *      }
 *  }
 * </code></pre>
 *
 *
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass(classes = [GaelykBindingsTransformation])
@interface GaelykBindings { }