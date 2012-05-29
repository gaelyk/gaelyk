package groovyx.gaelyk.search

import com.google.appengine.api.search.Document

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
