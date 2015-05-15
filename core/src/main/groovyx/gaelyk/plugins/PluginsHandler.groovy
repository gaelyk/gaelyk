/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gaelyk.plugins

import groovy.transform.CompileStatic
import groovyx.gaelyk.GaelykBindingEnhancer
import groovyx.gaelyk.logging.GroovyLogger
import groovyx.gaelyk.routes.Route

import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Configure the installed plugins.
 *
 * @author Guillaume Laforge
 * @author Vladimir Orany
 * @author Marcin Erdmann
 */
@Singleton(lazy = true)
class PluginsHandler {

    // indicates whether the plugins have already been loaded or not
    // as both the template and groovlet servlets can call the handler
    // we can't know which one will initialize the plugins first
    private boolean initialized = false

    Map<String, Object> bindingVariables = [:]
    List<Route> routes = []
    List<Closure> beforeActions = []
    List<Closure> afterActions = []
    List<String> installed = []

    final Closure defaultScriptContentLoader = { String path ->
        def file = new File(path)
        if(file.exists()){
            return file.text
        }
        InputStream is = Thread.currentThread().contextClassLoader.getResourceAsStream(path)
        if(!is) return ""
        is.text
    }

    Closure scriptContent = defaultScriptContentLoader

    GroovyLogger log = new GroovyLogger('gaelyk.pluginshandler')

    @CompileStatic
    void reinit() {
        initialized = false
        bindingVariables = [:]
        routes = []
        beforeActions = []
        afterActions = []
        installed = []
        scriptContent = defaultScriptContentLoader
    }

    /**
     * Initializes the plugins
     */
    synchronized void initPlugins(ServletContext servletContext, ignoreBinary = false) {
        if (!initialized) {
            log.config "Loading plugin descriptors"

            Iterable<PluginBaseScript> loader = ignoreBinary ? []: ServiceLoader.load(PluginBaseScript)

            Iterable<PluginBaseScript> loadedPlugins = loadPluginsList().collect { String pluginName ->
                def pluginPath = "WEB-INF/plugins/${pluginName}.groovy"
                String content = scriptContent(pluginPath)
                if (content) {
                    log.config "Loading plugin $pluginName"

                    def config = new CompilerConfiguration()
                    config.scriptBaseClass = PluginBaseScript.class.name

                    // evaluate the plugin descriptor
                    def shell = new GroovyShell(config)
                    return (PluginBaseScript) shell.parse(content, "${pluginName}.groovy")
                } else {
                    log.config "Plugin $pluginName doesn't exist"
                    return null
                }
            }.grep()

            int counter = 0
            int routesStep = 330
            Closure init = { PluginBaseScript pluginScript ->
                
                String pluginName = pluginScript.getClass().getSimpleName()
                log.config "Initializing plugin $pluginName"
                // creates a binding for the plugin descriptor file
                def binding = new Binding()
                // and inject the GAE services
                GaelykBindingEnhancer.bind(binding)
                // and plugin logger
                binding.setVariable("log", new GroovyLogger("gaelyk.plugins.${pluginName}", true))
                binding.setVariable("servletContext", servletContext)

                pluginScript.binding = binding
                
                // resolve the routes index conflict
                if(pluginScript.firstRouteIndex == 0){
                    pluginScript.firstRouteIndex = (++counter) * routesStep
                }
                
                pluginScript.run()

                // use getters directly,
                // otherwise property access returns variables from the binding of the scripts
                bindingVariables.putAll pluginScript.getBindingVariables()
                routes.addAll pluginScript.getRoutes()

                if (pluginScript.getBeforeAction()) beforeActions.add pluginScript.getBeforeAction()
                if (pluginScript.getAfterAction())  afterActions .add pluginScript.getAfterAction()

                log.config "Plugin $pluginName initialized!"
                installed << pluginName
            }
            try {
                for(PluginBaseScript plugin in loader){
                    init plugin
                }
            } catch(ServiceConfigurationError e){
                log.config e.message
            }

            for(PluginBaseScript plugin in loadedPlugins){
                init plugin
            }

            log.config "Found ${installed.size()} plugin(s)"


            // reverse the order of the "after" actions so they are executed in reverse order
            if (afterActions) afterActions = afterActions.reverse()

            initialized = true
        }
    }

    /**
     * @return the list of plugins
     */
    synchronized List loadPluginsList() {
        String pluginsListFileContent = scriptContent("WEB-INF/plugins.groovy")

        if (pluginsListFileContent) {
            def config = new CompilerConfiguration()
            config.scriptBaseClass = PluginsListBaseScript.class.name

            // creates a binding for the list of plugins file,
            def binding = new LazyBinding()
            // and inject the GAE services
            GaelykBindingEnhancer.bind(binding)

            // evaluate the list of plugins
            def shell = new GroovyShell(binding, config)
            PluginsListBaseScript script = (PluginsListBaseScript) shell.parse(pluginsListFileContent, "plugins.groovy")
            script.run()

            return script.getPlugins()
        }
        return []
    }

    @CompileStatic
    boolean isInstalled(String pluginName){
        pluginName in installed
    }

    /**
     * Add the variables in the binding, as defined by the plugin descriptors.
     *
     * @param binding the binding to add the variables to
     */
    void enrich(Binding binding) {
        bindingVariables.each { String k, Object v -> binding.setVariable(k, v) }
    }

    /**
     * Execute all the "before" actions
     *
     * @param request
     * @param response
     */
    void executeBeforeActions(HttpServletRequest request, HttpServletResponse response) {
        beforeActions.each { Closure action ->
            cloneDelegateAndExecute action, request, response
        }
    }

    /**
     * Execute all the "after" actions
     *
     * @param request
     * @param response
     */
    void executeAfterActions(HttpServletRequest request, HttpServletResponse response, Object executionResult = null) {
        afterActions.each { Closure action ->
            cloneDelegateAndExecute action, request, response, executionResult
        }
    }

    private void cloneDelegateAndExecute(Closure action, HttpServletRequest request, HttpServletResponse response, result = null) {
        Binding binding = new Binding()
        GaelykBindingEnhancer.bind(binding)

        def delegate = [
            *:bindingVariables,
            request: request,
            response: response,
            log: action.owner.log,
            binding: bindingVariables
        ]

        if(result){
            delegate.result = result
        }

        Closure cloned = action.clone()
        cloned.resolveStrategy = Closure.DELEGATE_FIRST
        cloned.delegate = delegate

        cloned()
    }
}
