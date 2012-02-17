package groovyx.gaelyk.plugins

import com.google.appengine.api.utils.SystemProperty
import groovyx.gaelyk.GaelykBindingEnhancer
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
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import groovy.servlet.ServletCategory
import com.google.appengine.api.memcache.MemcacheService
import com.google.appengine.api.datastore.DatastoreService

/**
 * 
 * @author Guillaume Laforge
 */
class PluginsHandlerTest extends GroovyTestCase {

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

    private Binding binding

    protected void setUp() {
        super.setUp()

        // setting up the local environment
        helper.setUp()

        // sets the environment to "Development"
        SystemProperty.environment.set("Development")

        binding = new Binding()
        GaelykBindingEnhancer.bind(binding)

        PluginsHandler.instance.reinit()
    }

    protected void tearDown() {
        // uninstalling the local environment
        helper.tearDown()

        super.tearDown()
    }

    void testStandardPluginScriptReadingRoutine() {
        def content = PluginsHandler.instance.scriptContent("src/test/groovyx/gaelyk/plugins/pluginScript.sample")
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
			
			fileExists = { it ===  "WEB-INF/plugins.groovy" || it  == "WEB-INF/plugins/myPlugin.groovy"}

            initPlugins()

            def binding = new Binding(version: '0.5.6')
            enrich binding

            assert binding.getVariable('version') == '1.2.3'
        }
    }

    void testNoPlugins() {
        PluginsHandler.instance.with {
            scriptContent = { String path -> "" }
            initPlugins()

            assert !bindingVariables
            assert !routes
            assert !categories
            assert !beforeActions
            assert !afterActions
        }
    }

    void testOnePlugin() {
        PluginsHandler.instance.with {
            scriptContent = { String path ->
                if (path == "WEB-INF/plugins.groovy") {
                    "install myPlugin"
                } else if (path == "WEB-INF/plugins/myPlugin.groovy") {
                    """
                    binding {
                        version = "1.2.3"
                    }

                    routes {
                        get "/index", forward: "/index.groovy"
                        post "/upload", forward: "/upload.groovy"
                    }

                    categories MyCat

                    before { 'before' }

                    after  { 'after' }

                    class MyCat {}
                    """
                } else ""
            }

			fileExists = { it ===  "WEB-INF/plugins.groovy" || it  == "WEB-INF/plugins/myPlugin.groovy"}
			
            initPlugins()

            assert bindingVariables.version == "1.2.3"
            assert routes.size() == 2
            assert categories*.name == ['MyCat']
            assert beforeActions.size() == 1
            assert beforeActions[0]() == 'before'
            assert afterActions.size() == 1
            assert afterActions[0]() == 'after'
        }
    }

    void testTwoPluginsToCheckOrderOfActions() {
        def output = new StringBuilder()

        def request  = [
                getAttribute: { String key -> output }
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
                    before { request.sample << '1' }
                    after  { request.sample << '2' }
                    """
                } else if (path == "WEB-INF/plugins/pluginTwo.groovy") {
                    """
                    before { request.sample << '3' }
                    after  { request.sample << '4' }
                    """
                } else if (path == "META-INF/gaelyk-plugins/pluginThree.groovy") {
                    """
                    before { request.sample << '5' }
                    after  { request.sample << '6' }
                    """
                }
            }
			
			fileExists = { 
				it ==  "WEB-INF/plugins.groovy" 			|| 
				it == "WEB-INF/plugins/pluginOne.groovy"	||
				it == "WEB-INF/plugins/pluginTwo.groovy"	||
				it == "META-INF/gaelyk-plugins/pluginThree.groovy"
            }

            initPlugins()

            assert beforeActions.size() == 3
            assert afterActions.size()  == 3

            executeBeforeActions request, response
            executeAfterActions  request, response

            use(ServletCategory) {
                assert request.sample.toString() == '135642'
            }
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
                        request.fromBindingBlock = binding
                        request.fromBeforeBlock = this.datastore
                    }
                    """
                } else ""
            }
			
			fileExists = {
				it ==  "WEB-INF/plugins.groovy" 			||
				it == "WEB-INF/plugins/myPlugin.groovy"
			}

            initPlugins()

            def values = [:]
            def request = [setAttribute: { String name, obj -> values[name] = obj }] as HttpServletRequest
            executeBeforeActions(request, [:] as HttpServletResponse)

            assert values.fromBindingBlock.book == "Harry Potter"
            assert values.fromBindingBlock.memcacheCopy instanceof MemcacheService

            assert values.fromBeforeBlock instanceof DatastoreService
        }

    }
}


