package groovyx.gaelyk

import javax.servlet.http.HttpServletRequest

/**
 * @author Guillaume Laforge
 */
class TestUtil {
    static HttpServletRequest request(String uri) {
        [
                getRequestURI: {-> uri },
                toString: {-> "mock request" },
                getAttribute: { String name -> name.toUpperCase() },
                setAttribute: { String name, val -> }
        ] as HttpServletRequest
    }
}
