package groovyx.gaelyk.cache

import groovyx.gaelyk.cache.CachedResponse.CustomServletOutputStream
import groovyx.gaelyk.routes.HttpMethod
import groovyx.gaelyk.routes.RedirectionType
import groovyx.gaelyk.routes.Route

import javax.servlet.RequestDispatcher
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.google.appengine.api.memcache.MemcacheServiceFactory
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper

/**
 * Cache handler tests
 *
 * @author Guillaume Laforge
 */
class CacheHandlerTest extends GroovyTestCase {
    // setup the local environement stub services
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalMemcacheServiceTestConfig(),
    )

    private recorder = []

    private String dateBefore = "Sat, 29 Oct 1994 19:43:31 GMT"
    private String dateAfter  = "Fri, 29 Oct 2094 19:43:31 GMT"

    private String uri = "/index"

    private request = [
            getRequestURI: { -> recorder << "req.getRequestURI"; uri },
            getQueryString: { -> recorder << "req.getQueryString"; "" },
            getRequestDispatcher: { String s -> recorder << "req.getRequestDispatcher"; requestDispatcher },
            getHeader: { String h -> recorder << "req.getHeader"; dateAfter },
            toString: { -> "mock request" },
            setAttribute: { String name, val -> },
			getAttribute: { String name -> },
			getServletPath: {-> recorder << "req.getServletPath"; uri },
			getPathInfo: {->}
    ] as HttpServletRequest

    private requestDispatcher = [
            forward: { ServletRequest servletRequest, ServletResponse servletResponse -> recorder << "reqDisp.forward" }
    ] as RequestDispatcher

    private stream = new OutputStream() {
        void write(byte[] bytes) { }
        void write(byte[] bytes, int i, int i1) { }
        void write(int i) { }
    }
    private output = new CustomServletOutputStream(out: stream)

    private response = [
            addHeader: { String h, String v -> recorder << "resp.addHeader" },
            getContentType: { -> recorder << "resp.getContentType"; "text/html" },
            setContentType: { String ct -> recorder << "resp.setContentType" },
            getOutputStream: { -> recorder << "resp.getOutputStream"; output },
            sendError: { int errCode -> recorder << "resp.sendError" },
            setHeader: { k, v -> recorder << "resp.setHeader" }
    ] as HttpServletResponse

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

    void testClearCacheForUri() {
        def memcache = MemcacheServiceFactory.memcacheService

        memcache['content-for-/photos'] = '1234'
        memcache['content-type-for-/photos'] = 'text/html'
        memcache['last-modified-/photos'] = new Date().time.toString()

        memcache.clearCacheForUri('/photos')

        assert memcache['content-for-/photos'] == null
        assert memcache['content-type-for-/photos'] == null
        assert memcache['last-modified-/photos'] == null
    }

    void testCacheServingWithoutCaching() {
        def route = new Route(uri, "/index.groovy", HttpMethod.ALL, RedirectionType.FORWARD, null, null, 0, false, false, false, 0)
        CacheHandler.serve route, request, response
		// TODO: verify
        // assert recorder == ['req.getRequestURI', 'req.getQueryString', 'req.getRequestURI', 'req.getRequestDispatcher', 'reqDisp.forward']
		assert recorder == ['req.getServletPath', 'req.getQueryString', 'req.getRequestDispatcher', 'reqDisp.forward']
    }

    void testCacheServingWithCaching() {
        def route = new Route(uri, "/index.groovy", HttpMethod.ALL, RedirectionType.FORWARD, null, null, 100, false, false, false, 0)
        CacheHandler.serve route, request, response

		// TODO: verify
//        assert recorder == ['req.getRequestURI', 'req.getQueryString', 'req.getRequestURI', 'req.getHeader', 'resp.addHeader',
//                'resp.addHeader', 'resp.addHeader', 'req.getRequestDispatcher', 'reqDisp.forward',
//                'resp.getContentType', 'resp.setContentType', 'resp.getOutputStream', 'resp.getContentType']
		assert recorder ==  ['req.getServletPath', 'req.getQueryString', 'req.getHeader', 'resp.addHeader', 'resp.addHeader', 'resp.addHeader', 'req.getRequestDispatcher', 'reqDisp.forward', 'resp.getContentType', 'resp.setContentType', 'resp.getOutputStream', 'resp.getContentType']

    }

    void testCacheServingWithLastModified() {
        def memcache = MemcacheServiceFactory.memcacheService
        memcache.put("last-modified-$uri".toString(), dateBefore)
        def route = new Route(uri, "/index.groovy", HttpMethod.ALL, RedirectionType.FORWARD, null, null, 100, false, false, false, 0)
        CacheHandler.serve route, request, response

		// TODO: verify
        // assert recorder == ['req.getRequestURI', 'req.getQueryString', 'req.getRequestURI', 'req.getHeader', 'resp.sendError', 'resp.setHeader']
		assert recorder == ['req.getServletPath', 'req.getQueryString', 'req.getHeader', 'resp.sendError', 'resp.setHeader']
    }

    void testCacheServingWithCachingAndInCache() {
        def memcache = MemcacheServiceFactory.memcacheService
        memcache.put("content-for-$uri".toString(), "Hello")
        memcache.put("content-type-for-$uri".toString(), "text/html")
        def route = new Route(uri, "/index.groovy", HttpMethod.ALL, RedirectionType.FORWARD, null, null, 100, false, false, false, 0)
        CacheHandler.serve route, request, response

		// TODO: verify
        // assert recorder == ['req.getRequestURI', 'req.getQueryString', 'req.getRequestURI', 'req.getHeader', 'resp.setContentType', 'resp.getOutputStream']
		assert recorder == ['req.getServletPath', 'req.getQueryString', 'req.getHeader', 'resp.setContentType', 'resp.getOutputStream']
    }
}
