package groovyx.gaelyk.plugins

import org.codehaus.groovy.control.CompilerConfiguration
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig
import com.google.appengine.tools.development.testing.LocalImagesServiceTestConfig
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig
import com.google.appengine.tools.development.testing.LocalXMPPServiceTestConfig
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.api.utils.SystemProperty
import groovyx.gaelyk.GaelykBindingEnhancer

/**
 * 
 * @author Guillaume Laforge
 */
class PluginBaseScriptTest extends GroovyTestCase {

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

        binding = new Binding()
        GaelykBindingEnhancer.bind(binding)

        PluginsHandler.instance.reinit()
    }

    protected void tearDown() {
        // uninstalling the local environment
        helper.tearDown()

        super.tearDown()
    }


    void testLoadPluginDescriptor() {
        def config = new CompilerConfiguration()
        config.scriptBaseClass = PluginBaseScript.class.name

        def binding = new Binding()

        PluginBaseScript script = (PluginBaseScript) new GroovyShell(binding, config).parse("""
            binding {
                version = "1.2"
            }

            routes {
                get "/crud", forward: "/crud.groovy"
            }

            before {
                "before"
            }

            after {
                "after"
            }

            return "initialized"
        """)

        assert script.run() == "initialized"

        assert script.getBindingVariables()['version'] == "1.2"
        assert script.getBeforeAction()() == "before"
        assert script.getAfterAction()() == "after"
    }
}
