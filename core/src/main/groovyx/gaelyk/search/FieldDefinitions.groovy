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

import com.google.appengine.api.search.Field
import groovy.transform.TupleConstructor
import com.google.appengine.api.search.Document

/**
 * Field definitions
 *
 * @author Guillaume Laforge
 */
@TupleConstructor
class FieldDefinitions {
    Document.Builder documentBuilder

    def invokeMethod(String name, Object args) {
        assert args[0] instanceof Map, "The document field definition should be described with a Map"

        Map fieldDefMap = args[0]

        Field.Builder fieldBuilder = Field.newBuilder()
        fieldBuilder.name = name

        // special case for HTML, since the setHTML setter is uppercase,
        // but the DSL allows lowercase
        if (fieldDefMap.containsKey('html')) {
            fieldDefMap.HTML = fieldDefMap.html
            fieldDefMap.remove('html')
        }

        // special case for dates, as the time must be erased
        if (fieldDefMap.containsKey('date')) {
            fieldDefMap.date = Field.date(fieldDefMap.date)
        }

        fieldDefMap.each { String key, value ->
            fieldBuilder."$key" = value
        }

        documentBuilder.addField(fieldBuilder.build())
    }
}
