/*
 * Copyright 2009-2011 the original author or authors.
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

import org.codehaus.groovy.control.CompilerConfiguration
import groovyx.gaelyk.GaelykBindingEnhancer
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import groovy.servlet.ServletCategory
import groovyx.gaelyk.logging.GroovyLogger

/**
 * Configure the installed plugins.
 * 
 * @author Guillaume Laforge
 */
@Singleton(lazy = true)
class PluginsHandler {

    // indicates whether the plugins have already been loaded or not
    // as both the template and groovlet servlets can call the handler
    // we can't know which one will initialize the plugins first
    private boolean initialized = false

    Map bindingVariables = [:]
    List routes = []
    List categories = []
    List beforeActions = []
    List afterActions = []
	List installed = []

    final defaultScriptContentLoader = { String path ->
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

    void reinit() {
        initialized = false
        bindingVariables = [:]
        routes = []
        categories = []
        beforeActions = []
        afterActions = []
		installed = []
        scriptContent = defaultScriptContentLoader
    }

    /**
     * Initializes the plugins
     */
    synchronized void initPlugins() {
        if (!initialized) {
            log.config "Loading plugin descriptors"

            // retrieve the list of plugin names to be loaded
            def pluginsList = loadPluginsList()
            log.config "Found ${pluginsList.size()} plugin(s)"

            pluginsList.each { String pluginName ->
				def pluginPath = "WEB-INF/plugins/${pluginName}.groovy"
				String content = scriptContent(pluginPath)
				if(!content){
					pluginPath = "META-INF/gaelyk-plugins/${pluginName}.groovy"
					content = scriptContent(pluginPath)
				}
                if (content) {
                    log.config "Loading plugin $pluginName"

                    def config = new CompilerConfiguration()
                    config.scriptBaseClass = PluginBaseScript.class.name

                    // creates a binding for the plugin descriptor file
                    def binding = new Binding()
                    // and inject the GAE services
                    GaelykBindingEnhancer.bind(binding)
                    // and plugin logger
                    binding.setVariable("log", new GroovyLogger("gaelyk.plugins.${pluginName}", true))

                    // evaluate the plugin descriptor
                    def shell = new GroovyShell(binding, config)
                    PluginBaseScript script = (PluginBaseScript) shell.parse(content, "${pluginName}.groovy")
                    script.run()

                    // use getters directly,
                    // otherwise property access returns variables from the binding of the scripts
                    bindingVariables.putAll script.getBindingVariables()
                    routes.addAll script.getRoutes()
                    categories.addAll script.getCategories()

                    if (script.getBeforeAction()) beforeActions.add script.getBeforeAction()
                    if (script.getAfterAction())  afterActions .add script.getAfterAction()
					installed << pluginName
                } else {
					log.config "Plugin $pluginName doesn't exist"
				}
            }

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
	
	public boolean isInstalled(String pluginName){
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
    void executeAfterActions(HttpServletRequest request, HttpServletResponse response) {
        afterActions.each { Closure action ->
            cloneDelegateAndExecute action, request, response
        }
    }

    private void cloneDelegateAndExecute(Closure action, HttpServletRequest request, HttpServletResponse response) {
        Binding binding = new Binding()
        GaelykBindingEnhancer.bind(binding)

        Closure cloned = action.clone()
        cloned.resolveStrategy = Closure.DELEGATE_FIRST
        cloned.delegate = [
                *:bindingVariables,
                request: request,
                response: response,
                log: action.owner.log,
                binding: bindingVariables
        ]
        
        use (ServletCategory) {
            cloned()
        }
    }
}
