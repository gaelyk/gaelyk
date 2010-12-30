/*
 * Copyright 2009-2010 the original author or authors.
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

    /**
     * Initializes the plugins
     */
    synchronized void initPlugins() {
        if (!initialized) {

            // retrieve the list of plugin names to be loaded
            def pluginsList = loadPluginsList()

            pluginsList.each { String pluginName ->
                def pluginDescriptorFile = new File("WEB-INF/plugins/${pluginName}.groovy")
                if (pluginDescriptorFile.exists()) {
                    def config = new CompilerConfiguration()
                    config.scriptBaseClass = PluginBaseScript.class.name

                    // creates a binding for the plugin descriptor file
                    def binding = new Binding()
                    // and inject the GAE services
                    GaelykBindingEnhancer.bind(binding)

                    // evaluate the list of plugins
                    PluginBaseScript script = (PluginBaseScript) new GroovyShell(binding, config).parse(pluginDescriptorFile)
                    script.run()

                    // use getters directly,
                    // otherwise property access returns variables from the binding of the scripts
                    bindingVariables.putAll script.getBindingVariables()
                    routes.addAll script.getRoutes()
                    categories.addAll script.getCategories()

                    if (script.getBeforeAction()) beforeActions.add script.getBeforeAction()
                    if (script.getAfterAction())  afterActions .add script.getAfterAction()
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
    protected List loadPluginsList() {
        def pluginsListFile = new File("WEB-INF/plugins.groovy")

        if (pluginsListFile.exists()) {
            def config = new CompilerConfiguration()
            config.scriptBaseClass = PluginsListBaseScript.class.name

            // creates a binding for the list of plugins file,
            def binding = new LazyBinding()
            // and inject the GAE services
            GaelykBindingEnhancer.bind(binding)

            // evaluate the list of plugins
            PluginsListBaseScript script = (PluginsListBaseScript) new GroovyShell(binding, config).parse(pluginsListFile)
            script.run()

            return script.getPlugins()
        }

        return []
    }

    /**
     * Add the variables in the binding, as defined by the plugin descriptors.
     *
     * @param binding the binding to add the variables to
     */
    void enrich(Binding binding) {
        bindingVariables.each { String k, Object v -> binding.setVariable(k, v) }
    }
}
