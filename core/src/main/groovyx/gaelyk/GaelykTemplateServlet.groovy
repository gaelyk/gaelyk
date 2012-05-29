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
import groovy.servlet.ServletCategory
import groovy.servlet.TemplateServlet
import groovy.text.Template

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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

    private static final String PRECOMPILED_TEMPLATE_PREFIX = '$gtpl$'

    private Closure serviceClosure = {}

    @Override
    void init(ServletConfig config) {
        if(config.getInitParameter('preferPrecompiled') == 'true' || !GaelykBindingEnhancer.localMode){
            serviceClosure = { HttpServletRequest request, HttpServletResponse response, ServletBinding binding ->
                try {
                    try {
                        runPrecompiled(getPrecompiledClassName(request), binding, response)
                    } catch(ClassNotFoundException e){
                        runTemplate(request, response, binding)
                    }
                } catch(FileNotFoundException te){
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND)
                } catch(IllegalAccessException te){
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN)
                }
            }
        } else {
            serviceClosure = { HttpServletRequest request, HttpServletResponse response, ServletBinding binding ->
                try {
                    try {
                        runTemplate(request, response, binding)
                    } catch(FileNotFoundException e){
                        runPrecompiled(getPrecompiledClassName(request), binding, response)
                    } catch(IllegalAccessException e){
                        runPrecompiled(getPrecompiledClassName(request), binding, response)
                    }
                } catch(ClassNotFoundException te){
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND)
                }
            }
        }
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
        use([
            ServletCategory,
            GaelykCategory,
            * PluginsHandler.instance.categories
        ]) {
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
        response.setContentType(CONTENT_TYPE_TEXT_HTML + "; charset=" + encoding)
        ServletBinding binding = new ServletBinding(request, response, servletContext)
        setVariables(binding)
        serviceClosure(request, response, binding)
    }

    private runTemplate(HttpServletRequest request, HttpServletResponse response, ServletBinding binding) {
        Template template = tryFindTemplate(request)
        response.setStatus(HttpServletResponse.SC_OK)
        Writer out = (Writer) binding.getVariable("out")
        if (out == null) {
            out = response.getWriter()
        }
        template.make(binding.getVariables()).writeTo(out)
    }

    private Template tryFindTemplate(HttpServletRequest request) {
        String uri = getScriptUri(request)
        File file = super.getScriptUriAsFile(request)
        if (file != null && file.exists()) {
            String name = file.getName()
            if (!file.canRead()) {
                throw new IllegalAccessException("Can not read \"" + name + "\"!")
            }
            return getTemplate(file)
        }
        throw new FileNotFoundException()
    }

    /**
     * @return name of the precompiled script class
     */
    static String getPrecompiledClassName(HttpServletRequest request){
        String incServletPath = (String) request.getAttribute(INC_SERVLET_PATH)
        String servletPath = incServletPath ?: request.servletPath
        
        def match = servletPath =~ "/((.+?/)*)(.+)\\.gtpl"
        if(!match){
            throw new ClassNotFoundException('No class found for servlet path ' + servletPath)
        }
        String ret = ''
        if(match[0][1]){
            ret += packageToDir(match[0][1])
        }
        ret += PRECOMPILED_TEMPLATE_PREFIX
        ret += match[0][3]
        ret
    }
    
    static String packageToDir(String pkg){
        return pkg.replaceAll(/[^a-zA-Z0-9\/]/, '_').replace('/', '.').toLowerCase()
    }

    private runPrecompiled(String precompiledClassName, ServletBinding binding, HttpServletResponse response) {
        try {
            Class precompiledClass = Class.forName(precompiledClassName)
            Script precompiled = precompiledClass.newInstance([binding]as Object[])
            precompiled.run()
            response.setStatus(HttpServletResponse.SC_OK)
        } catch(e){
            if(e instanceof ClassNotFoundException){
                throw e
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
            e.printStackTrace(binding.out)
        }
    }
}