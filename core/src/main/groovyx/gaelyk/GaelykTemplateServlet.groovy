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
package groovyx.gaelyk

import groovy.servlet.ServletBinding
import groovy.servlet.TemplateServlet
import groovy.text.Template
import groovy.transform.CompileStatic
import groovyx.gaelyk.logging.GroovyLogger
import groovyx.gaelyk.plugins.PluginsHandler
import groovyx.gaelyk.routes.RoutesFilter;

import javax.servlet.ServletConfig
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.codehaus.groovy.runtime.InvokerInvocationException

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

    private static final String PRECOMPILED_TEMPLATE_PREFIX = '_gtpl_'
    
    private boolean preferPrecompiled = false
    private boolean logErrors

    @Override
    @CompileStatic
    void init(ServletConfig config) {
        preferPrecompiled = !GaelykBindingEnhancer.localMode || config.getInitParameter('preferPrecompiled') != 'false' && (config.getInitParameter('preferPrecompiled') == 'true')
        logErrors = config.getInitParameter('logErrors') != 'false' && (config.getInitParameter('logErrors') == 'true')
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
     * Service incoming requests and executing before/after actions defined by plugins
     *
     * @param request the request
     * @param response the response
     * @throws IOException when anything goes wrong
     */
    @Override
    @CompileStatic
    void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PluginsHandler.instance.executeBeforeActions(request, response)
        doService(request, response)
        // there is no result for temlates
        PluginsHandler.instance.executeAfterActions(request, response)
    }

    /**
     * Reworked {@link #service(HttpServletRequest, HttpServletResponse)} method.
     * The original one relies on the implementation from the superclass
     * so it cannot be used directly
     * @param request http request
     * @param response http response
     */
    @CompileStatic
    private void doService(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(CONTENT_TYPE_TEXT_HTML + "; charset=" + encoding)
        ServletBinding binding = new ServletBinding(request, response, servletContext)
        setVariables(binding)
        try {
            if(preferPrecompiled) {
                try {
                    runPrecompiled(getPrecompiledClassName(request), binding, response)
                } catch(ClassNotFoundException e) {
                    runTemplate(request, response, binding)
                }
            } else {
                try {
                    runTemplate(request, response, binding)
                } catch(ResourceException | FileNotFoundException e) {
                    try {
                        runPrecompiled(getPrecompiledClassName(request), binding, response)
                    } catch(ClassNotFoundException cnfe){
                        throw e
                    }
                }
            }
        } catch (Throwable e) {
            e = RoutesFilter.filterStackTrace(request, e)
            StringWriter sw = []
            PrintWriter pw  = [sw]

            /*
             * Resource not found.
             */
            if (e instanceof ResourceException || e instanceof ClassNotFoundException || e instanceof FileNotFoundException) {
                if (logErrors) {
                    pw.println("': ")
                    e.printStackTrace(pw)                
                    servletContext.log(sw.toString())
                }
                response.sendError(HttpServletResponse.SC_NOT_FOUND)
                return
            } else if (logErrors) {
                pw.print("GaelykTemplateServlet Error: ")
                pw.print(" template: '")
                pw.print(getScriptUri(request))
                getLog(request).warning(sw.toString())

            }
            throw e // Let propagate out the filter chain and container handle the exception.
        }

    }

    @CompileStatic
    private runTemplate(HttpServletRequest request, HttpServletResponse response, ServletBinding binding) {
        Template template = tryFindTemplate(request)
        Writer out = (Writer) binding.getVariable("out")
        if (out == null) {
            out = response.getWriter()
        }
        template.make(binding.getVariables()).writeTo(out)
    }

    @CompileStatic
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
        String message = file ? "Cannot find template: $file.absolutePath" : "Cannot find template for URI $uri"
        throw new FileNotFoundException(message)
    }

    /**
     * @return name of the precompiled script class
     */
    static String getPrecompiledClassName(HttpServletRequest request) {
        String incServletPath = (String) request.getAttribute(INC_SERVLET_PATH)
        String servletPath = incServletPath ?: request.servletPath

        def match = servletPath =~ "/((.+?/)*)(.+)\\.gtpl"
        if (!match) {
            throw new ClassNotFoundException('No class found for servlet path ' + servletPath)
        }
        String ret = ''
        if (match[0][1]) {
            ret += packageToDir(match[0][1])
        }
        ret += PRECOMPILED_TEMPLATE_PREFIX
        ret += match[0][3].replaceAll(/[^a-zA-Z0-9\$]/, '_')
        ret
    }

    @CompileStatic
    static String packageToDir(String pkg) {
        return pkg.replaceAll(/[^a-zA-Z0-9\/]/, '_').replace('/', '.').toLowerCase()
    }

    // @CompileStatic
    private runPrecompiled(String precompiledClassName, ServletBinding binding, HttpServletResponse response) {
        Class<Script> precompiledClass = (Class<Script>) Class.forName(precompiledClassName)
        Script precompiled = precompiledClass.newInstance([binding]as Object[])
        precompiled.run()
    }
    
    private GroovyLogger getLog(ServletRequest request){
        GroovyLogger.forTemplateUri(super.getScriptUri(request))
    }
}