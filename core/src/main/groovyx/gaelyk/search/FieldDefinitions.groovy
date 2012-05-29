package groovyx.gaelyk.search

import com.google.appengine.api.search.Field
import groovy.transform.TupleConstructor
import com.google.appengine.api.search.Document

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
