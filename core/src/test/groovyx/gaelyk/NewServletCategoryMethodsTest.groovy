package groovyx.gaelyk

import javax.servlet.http.HttpServletResponse

/**
 * The new category methods for the Servlet API
 * which are not covered by Groovy's own servlet category. 
 *
 * @author Guillaume Laforge
 */
class NewServletCategoryMethodsTest extends GroovyTestCase {

    void testResponseHeadersAccessort() {
        use (GaelykCategory) {
            def h = [:]
            def response = [
                    addHeader: { k, v -> h[k.toString()] = v.toString() }
            ] as HttpServletResponse

            response.headers['Content-Type'] = "text/html"

            assert h['Content-Type'] == "text/html"
        }
    }
}
