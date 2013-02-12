package groovyx.gaelyk.routes

import javax.servlet.FilterConfig
import javax.servlet.ServletContext
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig
import com.google.appengine.tools.development.testing.LocalImagesServiceTestConfig
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig
import com.google.appengine.tools.development.testing.LocalXMPPServiceTestConfig
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig
import com.google.appengine.api.utils.SystemProperty
import groovyx.gaelyk.GaelykBindingEnhancer
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.FilterChain
import javax.servlet.RequestDispatcher
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

/**
 *
 * @author Guillaume Laforge
 */
class RoutesFilterTest extends GroovyTestCase {

    // setup the local environement stub services
    LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig(),
            new LocalMemcacheServiceTestConfig(),
            new LocalURLFetchServiceTestConfig(),
            new LocalMailServiceTestConfig(),
            new LocalImagesServiceTestConfig(),
            new LocalUserServiceTestConfig(),
            new LocalTaskQueueTestConfig(),
            new LocalXMPPServiceTestConfig(),
            new LocalBlobstoreServiceTestConfig()
    )

    Binding binding

    def filter = new RoutesFilter()

    protected void setUp() {
        super.setUp()

        // setting up the local environment
        helper.setUp()

        // sets the environment to "Development"
        SystemProperty.environment.set("Development")

        binding = new Binding()
        GaelykBindingEnhancer.bind(binding)


        filter.init(new FilterConfig() {
            String getFilterName() { "RoutesFilter" }

            String getInitParameter(String s) {
                if (s == 'routes.location') {
                    'src/test/groovyx/gaelyk/routes/routes.sample'
                }
            }

            Enumeration getInitParameterNames() {
                ['routes.location'] as Enumeration
            }

            ServletContext getServletContext() {
                [:] as ServletContext
            }

        })

        filter.loadRoutes()

        assert filter.routes
    }

    protected void tearDown() {
        // uninstalling the local environment
        helper.tearDown()

        super.tearDown()
    }

    void testNoRouteFound() {
        def request = [
                getRequestURI: { -> "/nowhere" },
                getMethod: { -> "GET" },
                setAttribute: { String name, val -> },
                getAttribute: { String name -> },
                getServletPath: { -> '' },
                getPathInfo: { -> '/nowhere' },
        ] as HttpServletRequest

        def response = [:] as HttpServletResponse

        def chained = false
        def chain = [
                doFilter: { HttpServletRequest req, HttpServletResponse resp -> chained = true }
        ] as FilterChain

        filter.doFilter(request, response, chain)

        assert chained
    }

    void testIgnoredRoute() {
        def request = [
                getRequestURI: { -> "/ignore" },
                getMethod: { -> "GET" },
                setAttribute: { String name, val -> },
                getAttribute: { String name -> },
                getServletPath: { -> '' },
                getPathInfo: { -> '/ignore' }
        ] as HttpServletRequest

        def response = [:] as HttpServletResponse

        def chained = false
        def chain = [
                doFilter: { HttpServletRequest req, HttpServletResponse resp -> chained = true }
        ] as FilterChain

        filter.doFilter(request, response, chain)

        assert chained
    }

    void testRedirectRoute() {
        def request = [
                getRequestURI: { -> "/redirect" },
                getMethod: { -> "GET" },
                setAttribute: { String name, val -> },
                getAttribute: { String name -> },
                getServletPath: { -> '' },
                getPathInfo: { -> '/redirect' }
        ] as HttpServletRequest

        def redirected = ""

        def response = [
                sendRedirect: { String where -> redirected = where }
        ] as HttpServletResponse

        def chain = [:] as FilterChain

        filter.doFilter(request, response, chain)

        assert redirected == "/elsewhere.gtpl"
    }

    void testRouteWithANamespace() {
        def forwarded = false
        def dispatched = ""

        def dispatcher = [
                forward: { ServletRequest req, ServletResponse resp -> forwarded = true }
        ] as RequestDispatcher

        def request = [
                getRequestURI: { -> "/acme/home" },
                getQueryString: { -> "" },
                getMethod: { -> "GET" },
                getRequestDispatcher: { String s -> dispatched = s; return dispatcher },
                setAttribute: { String name, val -> },
                getAttribute: { String name -> },
                getServletPath: { -> '' },
                getPathInfo: { ->  "/acme/home"  }
        ] as HttpServletRequest

        def response = [:] as HttpServletResponse

        def chain = [:] as FilterChain

        filter.doFilter(request, response, chain)

        assert forwarded
        assert dispatched == "/customer.groovy?cust=acme"
    }

    void testNormalRoute() {
        def forwarded = false
        def dispatched = ""

        def dispatcher = [
                forward: { ServletRequest req, ServletResponse resp -> forwarded = true }
        ] as RequestDispatcher

        def attributes = [:]

        def request = [
                getRequestURI: { -> "/somewhere" },
                getQueryString: { -> "" },
                getMethod: { -> "GET" },
                getRequestDispatcher: { String s -> dispatched = s; return dispatcher },
                setAttribute: { String name, val -> attributes[name] = val },
                getAttribute: { String name -> attributes[name] },
                getServletPath: { -> '' },
                getPathInfo: { -> '/somewhere' }
        ] as HttpServletRequest

        def response = [:] as HttpServletResponse

        def chain = [:] as FilterChain

        filter.doFilter(request, response, chain)

        assert forwarded
        assert dispatched == "/somewhere.groovy"
        assert attributes[RoutesFilter.ORIGINAL_URI] == "/somewhere"
    }

    void testValidatedRoute() {
        def forwarded = false
        def dispatched = ""

        def dispatcher = [
                forward: { ServletRequest req, ServletResponse resp -> forwarded = true }
        ] as RequestDispatcher

        def attributes = [:]

        def request = [
                getRequestURI: {-> "/validate" },
                getQueryString: {-> "" },
                getMethod: {-> "GET" },
                getRequestDispatcher: { String s -> dispatched = s; return dispatcher },
                setAttribute: { String name, val -> attributes[name] = val },
                getAttribute: { String name -> attributes[name] },
                getServletPath: {-> '' },
                getPathInfo: {-> '/validate' }
        ] as HttpServletRequest

        def response = [:] as HttpServletResponse

        def chain = [:] as FilterChain

        filter.doFilter(request, response, chain)

        assert forwarded
        assert dispatched == "/validate.gtpl"
        assert attributes[RoutesFilter.ORIGINAL_URI] == "/validate"
    }
}
