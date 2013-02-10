package groovyx.gaelyk

import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalSearchServiceTestConfig
import com.google.appengine.api.search.SearchServiceFactory
import com.google.appengine.api.search.SearchService

class SearchServiceTest extends GroovyTestCase {

    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalSearchServiceTestConfig()
    )

    protected void setUp() {
        super.setUp()
        helper.setUp()
    }

    protected void tearDown() {
        super.tearDown()
        helper.tearDown()
    }

    void testGetAtNamespace() {
        def search = SearchServiceFactory.searchService

        assert search instanceof SearchService

        def namespacedSearch = search['aNamespace']

        assert namespacedSearch instanceof SearchService
        assert namespacedSearch.namespace == 'aNamespace'
    }
}
