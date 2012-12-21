package groovyx.gaelyk.plugins

import groovyx.grout.plugins.LazyBinding;

import org.codehaus.groovy.control.CompilerConfiguration

/**
 * 
 * @author Guillaume Laforge
 */
class PluginsListBaseScriptTest extends GroovyTestCase {
    
    void testBaseScriptWithInstallMethod() {
        def config = new CompilerConfiguration()
        config.scriptBaseClass = PluginsListBaseScript.class.name

        def binding = new LazyBinding()

        def shell = new GroovyShell(binding, config)

        PluginsListBaseScript script = (PluginsListBaseScript) shell.parse("""
            install jsonPlugin
            install restPlugin
            install scaffoldPlugin
        """)
        script.run()

        def plugins = script.getPlugins()

        assert plugins == ['jsonPlugin', 'restPlugin', 'scaffoldPlugin']
    }
}
