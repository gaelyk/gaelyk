/*
 * Copyright 2009 the original author or authors.
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
package groovyx.gaelyk

import groovy.servlet.GroovyServlet
import groovy.servlet.ServletBinding

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import javax.servlet.ServletConfig
import groovyx.gaelyk.plugins.PluginsHandler

/**
 * The Gaelyk servlet extends Groovy's own Groovy servlet
 * to inject Google App Engine dedicated services in the binding of the Groolets.
 * 
 * @author Marcel Overdijk
 * @author Guillaume Laforge
 *
 * @see groovy.servlet.GroovyServlet
 */
class GaelykServlet extends GroovyServlet {

    @Override
    def void init(ServletConfig config) {
        super.init(config)
        PluginsHandler.instance.initPlugins()
    }

    /**
     * Injects the default variables and GAE services in the binding of Groovlets
     *  
     * @param binding the binding to enhance
     */
    @Override
    protected void setVariables(ServletBinding binding) {
        GaelykBindingEnhancer.bind(binding)
        PluginsHandler.instance.enrich(binding)
    }

    /**
     * Service incoming requests applying the <code>GaelykCategory</code>
     * and the other categories defined by the installed plugins.
     *
     * @param request the request
     * @param response the response
     * @throws IOException when anything goes wrong
     */
    @Override
    void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        use([GaelykCategory, * PluginsHandler.instance.categories]) {
            super.service(request, response)
        }
    }
}
