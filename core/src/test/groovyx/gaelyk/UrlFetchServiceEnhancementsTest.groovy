package groovyx.gaelyk

import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig
import com.google.appengine.api.urlfetch.URLFetchService
import com.google.appengine.api.urlfetch.URLFetchServiceFactory
import com.google.appengine.api.urlfetch.HTTPResponse
import java.util.concurrent.Future

/**
 * Tests around the support of the URL Fetch Service
 *
 * @author Guillaume Laforge
 */
class UrlFetchServiceEnhancementsTest extends GroovyTestCase {
    // setup the local environement stub services
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalURLFetchServiceTestConfig(),
    )

    URLFetchService urlFetch = URLFetchServiceFactory.URLFetchService
    URL gaelyk = "http://gaelyk.appspot.com".toURL()
    URL googleHome = "http://www.google.com/search".toURL()
    URL googleSearch = "http://www.google.com/search".toURL()
    URL googleSearchWithGaelykQ = "http://www.google.com/search?q=Gaelyk".toURL()
    URL formPost = "http://hroch486.icpf.cas.cz/cgi-bin/echo.pl".toURL()

    protected void setUp() {
        super.setUp()
        // setting up the local environment
        helper.setUp()
    }

    protected void tearDown() {
        // uninstalling the local environment
        helper.tearDown()
        super.tearDown()
    }

    void testForbiddenParameter() {
        shouldFail {
            gaelyk.get(fancyParam: true)
        }
    }

    void testGetGaelykHomePageWithOptions() {
        HTTPResponse response = gaelyk.get(
                followRedirects: true, allowTruncate: false, deadline: 30,
                headers: ['User-Agent': 'Mozilla/5.0 (Linux; X11)'])

        assert response.responseCode == 200
        assert response.text.contains('Gaelyk')
        assert response.headers
    }

    void testGetGaelykHomePageAndCheckResponseHeaders() {
        HTTPResponse response = gaelyk.get(deadline: 30, allowTruncate: true)

        assert response.responseCode == 200

        println response.headersMap.'Content-Type' == 'text/html; charset=utf-8'
    }

    void testPostForbiddenToGoogle() {
        HTTPResponse response = googleSearch.delete(followRedirects: false)

        assert response.statusCode == 405
        assert response.text.contains('The request method <code>DELETE</code> is inappropriate for the URL')
    }

    void testGoogleWithFuture() {
        Future<HTTPResponse> future = googleSearchWithGaelykQ.get(async: true)
        HTTPResponse response = future.get()

        assert response.responseCode == 200
        assert response.text.contains('http://gaelyk.appspot.com')
    }

    void testGoogleSearch() {
        HTTPResponse response = googleSearch.get(params: [q: 'Gaelyk'])

        println response.text

        assert response.responseCode == 200
        assert response.text.contains('http://gaelyk.appspot.com')
    }

    void _testPostToFormWithPayload() {
        HTTPResponse response = formPost.post(payload: 'your_name=Gaelyk&fruit=Apricot', deadline: 10)

        assert response.responseCode == 200
        assert response.text.contains('Gaelyk')
        assert response.text.contains('Apricot')
    }

    void _testPostToFormWithParameters() {
        HTTPResponse response = formPost.post(params: [your_name: 'Gaelyk', fruit: 'Apricot'], deadline: 10)

        assert response.responseCode == 200
        assert response.text.contains('Gaelyk')
        assert response.text.contains('Apricot')
    }
}
