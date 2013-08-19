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
package groovyx.gaelyk.datastore

import java.lang.annotation.ElementType
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import org.codehaus.groovy.transform.GroovyASTTransformationClass

/**
 * Annotation for better object coercion of POGOs.
 *
 * This annotation perform transformation which adds <code>exists</code>,<code>save</code>, <code>delete</code> methods to the POGOs.
 * It also adds <code>get</code>,<code>count</code>,<code>find</code><code>findAll</code> and <code>iterate</code> static methods to
 * the annotated classes.
 * <br/>
 *
 * Property <code>id</code> of the type <code>long</code> is added to the class if annotation <code>Key</code>
 * is not present on any field. 
 * Property <code>version</code> of the type <code>long</code> is added to the class if annotation <code>Version</code>
 * is not present on any field. 
 * 
 * Properties of the class annotated with this annotation are unindexed by default.
 * Set {link #unidexed()} to <code>false</code> to change the default behavior. Use <code>Indexed</code>
 * annotation on property you want index.
 * 
 * All classes annotated by this annotation will implement {@link DatastoreEntity} interface.
 *
 * @author Vladimir Orany
 */


@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass(classes = [EntityTransformation])
@Inherited
@interface Entity {
    boolean unindexed() default true
}
