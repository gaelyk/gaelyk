package groovyx.gaelyk

import javax.servlet.http.HttpServletResponse

/**
 * The new extension methods for the Servlet API
 * which are not covered by Groovy's own servlet category. 
 *
 * @author Guillaume Laforge
 */
class NewServletExtensionMethodsTest extends GroovyTestCase {

    void testResponseHeadersAccessor() {
        def h = [:]
        def response = [
                setHeader: { k, v -> h[k.toString()] = v.toString() },
                addHeader: { k, v ->
                    def currentValueInMap = h[k.toString()]
                    def valueToSetOrAdd = v.toString()
                    if (currentValueInMap) {
                        if (currentValueInMap instanceof List) {
                            h[k.toString()] << valueToSetOrAdd
                        } else {
                            h[k.toString()] = [currentValueInMap, valueToSetOrAdd]
                        }
                    } else {
                        h[k.toString()] = valueToSetOrAdd
                    }
                }
        ] as HttpServletResponse

        response.headers['a'] = "1"
        assert h['a'] == "1"

        response.headers['a'] = "2"
        assert h['a'] == "2"

        response.headers['a'] << "3"
        assert h['a'] == ["2", "3"]
    }
}
