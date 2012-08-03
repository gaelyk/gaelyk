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
package groovyx.gaelyk.search

import com.google.appengine.api.search.Document

/**
 * Document definitions
 *
 * @author Guillaume LAforge
 */
class DocumentDefinitions {
    List<Document> docs = []

    void document(Map m, Closure c) {
        def builder = Document.newBuilder()

        if (m.containsKey('id'))        builder.id      = m.id
        if (m.containsKey('locale'))    builder.locale  = m.locale
        if (m.containsKey('rank'))      builder.rank    = m.rank

        def fieldsClosure = (Closure)c.clone()
        fieldsClosure.resolveStrategy = Closure.DELEGATE_FIRST
        fieldsClosure.delegate = new FieldDefinitions(builder)
        fieldsClosure()

        docs << builder.build()
    }
}
