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

import static java.lang.annotation.ElementType.*
import static java.lang.annotation.RetentionPolicy.*

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention
import java.lang.annotation.Target

/**
 * Annotation for properties of a Groovy class in which the current version of the entity should be injected.
 *
 * Obtaining the version each time might be time consuming. Use this annotation wisely.
 *
 * @author Guillaume Laforge
 */

@Retention(RUNTIME)
@Target([METHOD, FIELD])
@Inherited
@interface Version {
}