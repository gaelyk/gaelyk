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
package groovyx.gaelyk

import groovy.servlet.ServletBinding
import groovy.servlet.TemplateServlet
import groovy.text.Template;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import groovyx.gaelyk.plugins.PluginResourceSupport;
import groovyx.gaelyk.plugins.PluginsHandler
import javax.servlet.ServletConfig
import groovyx.gaelyk.logging.GroovyLogger

/**
 * The Gaelyk template servlet extends Groovy's own template servlet 
 * to inject Google App Engine dedicated services in the binding of the Groolets.
 *
 * @author Marcel Overdijk
 * @author Guillaume Laforge
 *
 * @see groovy.servlet.TemplateServlet
 */
class GaelykTemplateServlet extends TemplateServlet {

    @Override
    void init(ServletConfig config) {
        super.init(config)
    }

    /**
     * Injects the default variables and GAE services in the binding of templates
     * as well as the variables contributed by plugins, and a logger.
     *
     * @param binding the binding to enhance
     */
    @Override
    protected void setVariables(ServletBinding binding) {
        GaelykBindingEnhancer.bind(binding)
        PluginsHandler.instance.enrich(binding)
        binding.setVariable("log", GroovyLogger.forTemplateUri(super.getScriptUri(binding.request)))
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
            PluginsHandler.instance.executeBeforeActions(request, response)
            doService(request, response)
            PluginsHandler.instance.executeAfterActions(request, response)
        }
    }
	
	/**
	 * Reworked {@link #service(HttpServletRequest, HttpServletResponse)} method.
	 * The original one relies on the implementation from the superclass
	 * so it cannot be used directly
	 * @param request http request
	 * @param response http response
	 */
	private void doService(HttpServletRequest request, HttpServletResponse response){

		//
		// Get the template source file handle.
		//
		Template template;
		String name;
		String uri = getScriptUri(request);

		File file = super.getScriptUriAsFile(request);
		if (file != null && file.exists()) {
			name = file.getName();
			if (!file.canRead()) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Can not read \"" + name + "\"!");
				return; // throw new IOException(file.getAbsolutePath());
			}
			template = getTemplate(file);
		} else if(PluginResourceSupport.isPluginPath(uri)){
			try {
				template = getTemplate(PluginResourceSupport.getPluginFileURL("templates", uri))
			} catch (Exception e){
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return; // throw new IOException(file.getAbsolutePath());
			}
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return; // throw new IOException(file.getAbsolutePath());
		}

		ServletBinding binding = new ServletBinding(request, response, servletContext);
		setVariables(binding);

		response.setContentType(CONTENT_TYPE_TEXT_HTML + "; charset=" + encoding);
		response.setStatus(HttpServletResponse.SC_OK);


		Writer out = (Writer) binding.getVariable("out");
		if (out == null) {
			out = response.getWriter();
		}

		template.make(binding.getVariables()).writeTo(out);
	}
}