/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gaelyk

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import org.codehaus.groovy.transform.GroovyASTTransformationClass

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
 * @author Vladimir Orany
 * @author Guillaume Laforge
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass(classes = [GaelykBindingsTransformation])
@interface GaelykBindings { }