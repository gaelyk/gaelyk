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
package groovyx.gaelyk.extensions

import groovy.transform.CompileStatic
import com.google.appengine.api.search.SearchService
import com.google.appengine.api.search.SearchServiceFactory
import com.google.appengine.api.search.Index
import com.google.appengine.api.search.IndexSpec
import com.google.appengine.api.search.AddResponse
import com.google.appengine.api.search.PutResponse
import groovyx.gaelyk.search.DocumentDefinitions
import com.google.appengine.api.search.Document
import com.google.appengine.api.search.Field
import java.util.concurrent.Future

/**
 * Search service method extensions
 *
 * @author Guillaume Laforge
 */
class SearchExtensions {

    /**
     * Get a search service instance restricted to a given namespace.
     * <pre><code>
     *     search['namespaceName']
     * </code></pre>
     *
     * @param search the search service
     * @param namespace the namespace name
     * @return a search service over a specific namespace
     */
    @CompileStatic
    static SearchService getAt(SearchService search, String namespace) {
        SearchServiceFactory.getSearchService(namespace)
    }

    /**
     * Shortcut notation to easily get an index from the search service.
     * <pre><code>
     *     def index = search.index("books")
     * </code></pre>
     *
     * @param search the search service
     * @param indexName the name of the index
     * @return an index
     */
    @CompileStatic
    static Index index(SearchService search, String indexName) {
        search.getIndex(IndexSpec.newBuilder().setName(indexName).build())
    }

    /**
     * Add a new document to the index.
     *
     * <pre><code>
     *     index.add {
     *         document(id: "1234", locale: US, rank: 3) {
     *             title text: "Big bad wolf", locale: ENGLISH
     *             published date: new Date()
     *             numberOfCopies number: 35
     *             summary html: "<p>super story</p>", locale: ENGLISH
     *             description text: "a book for children"
     *             category atom: "children"
     *             keyword text: "wolf"
     *             keyword text: "red hook"
     *         }
     *     }
     * </code></pre>
     *
     * The named arguments are restricted to id, locale and rank.
     * The calls inside the closure correspond to the field name, its type thanks to a named argument
     * of the form <code>type: value</code>, and optionally a locale.
     * You can have several times the same field name, for multi-valued fields.
     *
     * @param index the index to which to add the documents
     * @param closure the closure defining the documents to be added to the index
     * @return an instance of AddResponse
     */
    @Deprecated
    @CompileStatic
    static AddResponse add(Index index, @DelegatesTo(value=DocumentDefinitions, strategy=Closure.DELEGATE_FIRST) Closure closure) {
        def docDefClosure = (Closure)closure.clone()
        docDefClosure.resolveStrategy = Closure.DELEGATE_FIRST
        def definitions = new DocumentDefinitions()
        docDefClosure.delegate = definitions
        docDefClosure()

        index.add(definitions.docs)
    }    
    
    /**
     * Put a new document to the index.
     *
     * <pre><code>
     *     index.put {
     *         document(id: "1234", locale: US, rank: 3) {
     *             title text: "Big bad wolf", locale: ENGLISH
     *             published date: new Date()
     *             numberOfCopies number: 35
     *             summary html: "<p>super story</p>", locale: ENGLISH
     *             description text: "a book for children"
     *             category atom: "children"
     *             keyword text: "wolf"
     *             keyword text: "red hook"
     *         }
     *     }
     * </code></pre>
     *
     * The named arguments are restricted to id, locale and rank.
     * The calls inside the closure correspond to the field name, its type thanks to a named argument
     * of the form <code>type: value</code>, and optionally a locale.
     * You can have several times the same field name, for multi-valued fields.
     *
     * @param index the index to which to put the documents
     * @param closure the closure defining the documents to be added to the index
     * @return an instance of PutResponse
     */
    @CompileStatic
    static PutResponse put(Index index, @DelegatesTo(value=DocumentDefinitions, strategy=Closure.DELEGATE_FIRST) Closure closure) {
        def docDefClosure = (Closure)closure.clone()
        docDefClosure.resolveStrategy = Closure.DELEGATE_FIRST
        def definitions = new DocumentDefinitions()
        docDefClosure.delegate = definitions
        docDefClosure()

        index.put(definitions.docs)
    }    
    
    /**
     * Put a new document to the index.
     *
     * <pre><code>
     *     index.putAsync {
     *         document(id: "1234", locale: US, rank: 3) {
     *             title text: "Big bad wolf", locale: ENGLISH
     *             published date: new Date()
     *             numberOfCopies number: 35
     *             summary html: "<p>super story</p>", locale: ENGLISH
     *             description text: "a book for children"
     *             category atom: "children"
     *             keyword text: "wolf"
     *             keyword text: "red hook"
     *         }
     *     }
     * </code></pre>
     *
     * The named arguments are restricted to id, locale and rank.
     * The calls inside the closure correspond to the field name, its type thanks to a named argument
     * of the form <code>type: value</code>, and optionally a locale.
     * You can have several times the same field name, for multi-valued fields.
     *
     * @param index the index to which to put the documents
     * @param closure the closure defining the documents to be added to the index
     * @return an instance of PutResponse
     */
    @CompileStatic
    static Future<PutResponse> putAsync(Index index, @DelegatesTo(value=DocumentDefinitions, strategy=Closure.DELEGATE_FIRST) Closure closure) {
        def docDefClosure = (Closure)closure.clone()
        docDefClosure.resolveStrategy = Closure.DELEGATE_FIRST
        def definitions = new DocumentDefinitions()
        docDefClosure.delegate = definitions
        docDefClosure()

        index.putAsync(definitions.docs)
    }    

    /**
     * Get a document field raw value or list of raw values.
     * <pre><code>
     *      assert document.title = "Big bad wolf"
     *      assert document.keyword[0] == "wolf"
     *      assert document.keyword[1] == "red hook"
     * </code></pre>
     *
     * @param document the document
     * @param fieldName the field name
     * @return a raw value or a list of raw values if the field is multivalued
     */
    static Object get(Document document, String fieldName) {
        List<Field> fields = document.getFields(fieldName).collect()

        switch (fields.size()) {
            case 0:
                return null
            case 1:
                return getFieldRawValue(fields[0])
            default:
                return fields.collect{ Field field -> getFieldRawValue(field) }
        }
    }

    @CompileStatic
    private static getFieldRawValue(Field field) {
        switch(field.getType()) {
            case Field.FieldType.ATOM:   return field.atom
            case Field.FieldType.DATE:   return field.date
            case Field.FieldType.HTML:   return field.HTML
            case Field.FieldType.NUMBER: return field.number
            case Field.FieldType.TEXT:   return field.text
        }
    }
}
