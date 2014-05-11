package groovyx.gaelyk.plugins

import groovy.mock.interceptor.MockFor

import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.memcache.MemcacheService
import com.google.appengine.api.utils.SystemProperty
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalImagesServiceTestConfig
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig
import com.google.appengine.tools.development.testing.LocalXMPPServiceTestConfig

/**
 * 
 * @author Guillaume Laforge
 */
class PluginsHandlerTest extends GroovyTestCase {

    // setup the local environment stub services
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

    private Binding binding

    protected void setUp() {
        super.setUp()

        // setting up the local environment
        helper.setUp()

        // sets the environment to "Development"
        SystemProperty.environment.set("Development")

        PluginsHandler.instance.reinit()
    }

    protected void tearDown() {
        // uninstalling the local environment
        helper.tearDown()

        super.tearDown()
    }

    void testStandardPluginScriptReadingRoutine() {

        def content = PluginsHandler.instance.scriptContent("pluginScript.sample")
        assert content == """\
            binding {
                version = '1.2.3'
            }""".stripIndent()
    }

    void testEnrichExistingBindingWithPluginBindingDefinition() {
        PluginsHandler.instance.with {
            scriptContent = { String path ->
                if (path == "WEB-INF/plugins.groovy") {
                    "install myPlugin"
                } else if (path == "WEB-INF/plugins/myPlugin.groovy") {
                    """
                    binding {
                        version = '1.2.3'
                    }
                    """
                } else ""
            }

            initPlugins(null, true)

            def binding = new Binding(version: '0.5.6')
            enrich binding

            assert binding.getVariable('version') == '1.2.3'
        }
    }

    void testNoPlugins() {
        PluginsHandler.instance.with {
            scriptContent = { String path -> "" }
            initPlugins(null, true)

            assert !bindingVariables
            assert !routes
            assert !beforeActions
            assert !afterActions
        }
    }

    void testOnePlugin() {
        def servletContextControl = new MockFor(ServletContext)
        servletContextControl.demand.setAttribute { String key, Object value -> }
        def servletContextMock = servletContextControl.proxyInstance()
        PluginsHandler.instance.with {
            scriptContent = { String path ->
                if (path == "WEB-INF/plugins.groovy") {
                    "install myPlugin"
                } else if (path == "WEB-INF/plugins/myPlugin.groovy") {
                    """
                    servletContext.setAttribute('foo', 'bar')
                    binding {
                        version = "1.2.3"
                    }

                    routes {
                        startRoutingAt 10

                        get "/index", forward: "/index.groovy"
                        post "/upload", forward: "/upload.groovy"
                        all "/something", forward: "/something.groovy", index: 15
                    }

                    before { 'before' }

                    after  { 'after' }

                    class MyCat {}
                    """
                } else ""
            }

            initPlugins(servletContextMock, true)
            servletContextControl.verify(servletContextMock)
            assert bindingVariables.version == "1.2.3"
            assert routes.size() == 3
            assert routes[2].index == 15
            assert routes[0].index == 10
            assert routes[1].index == 11
            assert beforeActions.size() == 1
            assert beforeActions[0]() == 'before'
            assert afterActions.size() == 1
            assert afterActions[0]() == 'after'
        }
    }

    void testTwoPluginsToCheckOrderOfActions() {
        def output = new StringBuilder()

        def request  = [
            getAttribute: { String key -> output },
            setAttribute: { String attrName, String attrValue ->
                output << attrValue }
        ] as HttpServletRequest

        def response = [:] as HttpServletResponse

        PluginsHandler.instance.with {
            scriptContent = { String path ->
                if (path == "WEB-INF/plugins.groovy") {
                    """
                    install pluginOne
                    install pluginTwo
                    install pluginThree
                    """
                } else if (path == "WEB-INF/plugins/pluginOne.groovy") {
                    """
                    before { request.setAttribute('sample', '1') }
                    after  { request.setAttribute('sample', '2') }
                    """
                } else if (path == "WEB-INF/plugins/pluginTwo.groovy") {
                    """
                    before { request.setAttribute('sample', '3') }
                    after  { request.setAttribute('sample', '4') }
                    """
                } else if (path == "WEB-INF/plugins/pluginThree.groovy") {
                    """
                    before { request.setAttribute('sample', '5') }
                    after  { request.setAttribute('sample', '6') }
                    """
                }
            }

            initPlugins(null, true)

            assert beforeActions.size() == 3
            assert afterActions.size()  == 3

            executeBeforeActions request, response
            executeAfterActions  request, response, 'THE RESULT'

            assert output.toString() == '135642'
        }
    }

    void testAccessOriginalBindingVars() {
        PluginsHandler.instance.with {
            scriptContent = { String path ->
                if (path == "WEB-INF/plugins.groovy") {
                    "install myPlugin"
                } else if (path == "WEB-INF/plugins/myPlugin.groovy") {
                    """
                    binding {
                        book = "Harry Potter"
                        memcacheCopy = this.memcache
                    }

                    before {
                        request.setAttribute('fromBindingBlock',binding)
                        request.setAttribute('fromBeforeBlock', this.datastore)
                    }
                    """
                } else ""
            }

            initPlugins(null, true)

            def values = [:]
            def request = [setAttribute: { String name, obj -> values[name] = obj }, set:  { String name, obj -> values[name] = obj }] as HttpServletRequest
            executeBeforeActions(request, [:] as HttpServletResponse)

            assert values.fromBindingBlock.book == "Harry Potter"
            assert values.fromBindingBlock.memcacheCopy instanceof MemcacheService

            assert values.fromBeforeBlock instanceof DatastoreService
        }

    }

    void testServiceLoader(){
        ServiceLoader<PluginBaseScript> loader = ServiceLoader.load(PluginBaseScript)
        assert loader.inject(0) { acc, val -> ++acc } == 1

    }
}


