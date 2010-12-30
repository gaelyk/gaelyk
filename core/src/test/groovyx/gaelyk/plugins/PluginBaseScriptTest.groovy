package groovyx.gaelyk.plugins

import org.codehaus.groovy.control.CompilerConfiguration

/**
 * 
 * @author Guillaume Laforge
 */
class PluginBaseScriptTest extends GroovyTestCase {

    void testLoadPluginDescriptor() {
        def config = new CompilerConfiguration()
        config.scriptBaseClass = PluginBaseScript.class.name

        def binding = new Binding()

        PluginBaseScript script = (PluginBaseScript) new GroovyShell(binding, config).parse("""
            binding {
                version = "1.2"
            }

            categories MyCategory, MyOtherCategory

            routes {
                get "/crud", forward: "/crud.groovy"
            }

            before {
                "before"
            }

            after {
                "after"
            }

            class MyCategory {}
            class MyOtherCategory {}

            return "initialized"
        """)

        assert script.run() == "initialized"

        assert script.getBindingVariables()['version'] == "1.2"
        assert script.getCategories()*.name == ['MyCategory', 'MyOtherCategory']
        assert script.getBeforeAction()() == "before"
        assert script.getAfterAction()() == "after"
    }
}
