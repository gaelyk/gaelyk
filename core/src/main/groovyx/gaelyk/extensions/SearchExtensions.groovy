package groovyx.gaelyk.extensions

import groovy.transform.CompileStatic
import com.google.appengine.api.search.SearchService
import com.google.appengine.api.search.SearchServiceFactory
import com.google.appengine.api.search.Index
import com.google.appengine.api.search.Consistency
import com.google.appengine.api.search.IndexSpec
import com.google.appengine.api.search.AddResponse
import groovyx.gaelyk.search.DocumentDefinitions
import com.google.appengine.api.search.Document
import com.google.appengine.api.search.Field

/**
 * Search service method extensions
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
     *     def index = search.index("books", Consistency.PER_DOCUMENT)
     * </code></pre>
     *
     * @param search the search service
     * @param indexName the name of the index
     * @param consistency the consistency
     * @return an index
     */
    @CompileStatic
    static Index index(SearchService search, String indexName, Consistency consistency) {
        search.getIndex(IndexSpec.newBuilder().setName(indexName).setConsistency(consistency).build())
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
    @CompileStatic
    static AddResponse add(Index index, Closure closure) {
        def docDefClosure = (Closure)closure.clone()
        docDefClosure.resolveStrategy = Closure.DELEGATE_FIRST
        def definitions = new DocumentDefinitions()
        docDefClosure.delegate = definitions
        docDefClosure()

        index.add(definitions.docs)
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
        List<Field> fields = document.getField(fieldName).collect()

        switch (fields.size()) {
            case 0:
                throw new GroovyRuntimeException("No such field '$fieldName' for document '$document'")
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
