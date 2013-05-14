package groovyx.gaelyk.search

import static java.util.Locale.*

import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.ScoredDocument
import com.google.appengine.api.search.SearchServiceFactory
import com.google.appengine.tools.development.testing.LocalSearchServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper

class SearchShortcutsTest extends GroovyTestCase {
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalSearchServiceTestConfig()
    )

    protected void setUp() {
        super.setUp()
        helper.setUp()
    }

    protected void tearDown() {
        helper.tearDown()
        super.tearDown()
    }

    void testDocumentBuilding() {
        def search = SearchServiceFactory.searchService

        def index = search.index("books")

        def response = index.add {
            document(id: "1234", locale: US, rank: 3) {
                title text: "Big bad wolf", locale: ENGLISH
                published date: new Date()
                numberOfCopies number: 35
                summary html: "<p>super story</p>", locale: ENGLISH
                description text: "a book for children"
                category atom: "children"
                keyword text: "wolf"
                keyword text: "red hook"
                nothing text: null
                emptyList text: []
                locality geoPoint: [10, 50]
                geo geoPoint: new GeoPoint(30, 60)
            }
        }

        assert response.ids.contains("1234")

        def results = index.search("wolf")

        assert results.results.size() == 1

        results.each { ScoredDocument doc ->
            assert doc.id == "1234"
            assert doc.title == "Big bad wolf"
            assert doc.numberOfCopies == 35
            assert doc.summary.contains("story")

            assert doc.keyword.size() == 2
            assert "wolf" in doc.keyword
            assert "red hook" in doc.keyword
            assert !('nothing' in doc.fieldNames)
            assert !('emptyList' in doc.fieldNames)
            assert !doc.noSuchField
        }
    }
    
    void testListFieldsChecks() {
        def search = SearchServiceFactory.searchService

        def index = search.index("books")

        try {
            def response = index.add {
                document(id: "1234", locale: US, rank: 3) {
                    keyword text: ["red hook", "grandma"], locale: [Locale.ENGLISH, Locale.CHINESE]
                }
            }
            fail("Cannot have more than one list parameter in builder!")   
        } catch(IllegalArgumentException e){
            assert e.message == ("Cannot have more than one list parameter in builder!")
        }

    }

}
