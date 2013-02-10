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
package groovyx.gaelyk.query

/**
 * The various options for the select parameter:
 * <ul>
 *     <li>ALL: <code>select all</code> (full entity, default select option when select omitted)</li>
 *     <li>KEYS: <code>select keys</code> (return just the keys)</li>
 *     <li>SINGLE: <code>select single</code> (return just one entity when the search yields just one)</li>
 *     <li>COUNT: <code>select count</code> (return the count of entities for that query)</li>
 * </ul>
 *
 * @author Guillaume Laforge
 *
 * @since 1.0
 */
enum QueryType {
    ALL, KEYS, SINGLE, COUNT
}