package groovyx.gaelyk

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig
import com.google.appengine.tools.development.testing.LocalImagesServiceTestConfig
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig
import com.google.appengine.tools.development.testing.LocalXMPPServiceTestConfig
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig
import com.google.appengine.api.utils.SystemProperty
import javax.servlet.ServletConfig
import javax.servlet.ServletContext
import javax.servlet.RequestDispatcher
import javax.servlet.ServletResponse
import javax.servlet.ServletRequest

/**
 * @author Guillaume Laforge
 */
class GaelykServletTest extends GroovyTestCase {

    // setup the local environement stub services
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
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

    protected void setUp() {
        super.setUp()

        // setting up the local environment
        helper.setUp()

        // sets the environment to "Development"
        SystemProperty.environment.set("Development")
    }

    protected void tearDown() {
        // uninstalling the local environment
        helper.tearDown()

        super.tearDown()
    }

    void testGet() {
        def tempFile = File.createTempFile("groovlet", ".groovy")
        tempFile.createNewFile()
        tempFile << """
            log.info 'start'
            out << 'hello'
            include 'bye.gtpl'
        """

        def writer = new StringWriter()
        def printWriter = new PrintWriter(writer)

        def ctxt = [
                log: { String msg, Exception e = null -> 
                    println "log $msg"
                    if(e){
                        e.printStackTrace()
                    }
                 },
                getRealPath: { String p ->
                    println "getRealPath($p)"
                    if (p.contains('index'))
                        tempFile.absolutePath
                    else
                        p
                },
                getResource: { String p ->
                    println "getResource($p)"
                    if (p.contains('BeanInfo') || p.contains('$'))
                        return null
                    else
                        tempFile.toURI().toURL()
                }
        ] as ServletContext

        def config = [
                getServletContext: {-> ctxt },
                getInitParameter: { String p -> println "getInitParameter($p)" }
        ] as ServletConfig

        def session = [:] as HttpSession

        def request = [
                toString: {-> "mock request" },
                getProtocol: {-> println "getProtocol()"; "HTTP/1.1" },
                getAttribute: { String attr -> println "getAttribute($attr)" },
                getServletPath: {-> println "getServletPath()"; "/index.groovy" },
                getPathInfo: {-> println "getPathInfo()"; null },
                getSession: { boolean b -> println "getSession($b)"; session },
                getParameter: { String name -> },
                getParameterNames: {->
                    println "getParameterNames()"; new Enumeration() {
                        boolean hasMoreElements() { return false }
                        Object nextElement() { return null }
                    }
                },
                getHeaderNames: {->
                    println "getHeaderNames()"; new Enumeration() {
                        boolean hasMoreElements() { return false }
                        Object nextElement() { return null }
                    }
                },
                getRequestDispatcher: { String p -> [
                        include: { ServletRequest request, ServletResponse response ->
                            println "include $p"
                            response.writer << "bye"
                        },
                        forward: { ServletRequest request, ServletResponse response ->
                            println "forward $p"
                        }
                ] as RequestDispatcher },
                getHeader: {String name->}
        ] as HttpServletRequest

        def response = [
                toString: {-> "mock response" },
                setContentType: { String ct -> println "setContentType($ct)" },
                sendError: { int err, String msg = null -> println "sendError($err, $msg)" },
                getWriter: { -> println "getWriter()"; printWriter }
        ] as HttpServletResponse

        try {
            def servlet = new GaelykServlet()
            servlet.init(config)
            servlet.service(request, response)

            assert writer.toString() == 'hellobye'
        } catch (e) {
            e.printStackTrace(System.out)
            fail("Exception during servlet get action")
        } finally {
            tempFile.delete()
        }
    }
    
    
    void testGetPrecompiledClassName(){
        assert GaelykServlet.getPrecompiledClassName('/index.groovy') == 'index'
        assert GaelykServlet.getPrecompiledClassName('/api/index.groovy') == 'api.index'
        assert GaelykServlet.getPrecompiledClassName('/api/test/index.groovy') == 'api.test.index'
    }
}
