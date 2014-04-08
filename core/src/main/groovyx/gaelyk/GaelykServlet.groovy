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

import groovy.servlet.GroovyServlet
import groovy.servlet.ServletBinding
import groovy.transform.CompileStatic
import groovyx.gaelyk.logging.GroovyLogger
import groovyx.gaelyk.plugins.PluginsHandler
import groovyx.gaelyk.routes.RoutesFilter;

import javax.servlet.ServletConfig
import javax.servlet.ServletRequest
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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

    /**
     * The script engine executing the Groovy scripts for this servlet
     */
    private GroovyScriptEngine gse
    private boolean preferPrecompiled
    private boolean logErrors

    @Override
    @CompileStatic
    void init(ServletConfig config) {
        preferPrecompiled = !GaelykBindingEnhancer.localMode || config.getInitParameter('preferPrecompiled') != 'false' && (config.getInitParameter('preferPrecompiled') == 'true')
        logErrors = config.getInitParameter('logErrors') != 'false' && (config.getInitParameter('logErrors') == 'true')
        super.init(config)
        // Set up the scripting engine
        gse = createGroovyScriptEngine()
    }

    /**
     * Injects the default variables and GAE services in the binding of Groovlets
     * as well as the variables contributed by plugins, and a logger.
     *  
     * @param binding the binding to enhance
     */
    @Override
    @CompileStatic
    protected void setVariables(ServletBinding binding) {
        GaelykBindingEnhancer.bind(binding)
        PluginsHandler.instance.enrich(binding)
        binding.setVariable("log", getLog((ServletRequest)binding.getVariable('request')))
    }

    private GroovyLogger getLog(ServletRequest request){
        GroovyLogger.forGroovletUri(super.getScriptUri(request))
    }

    /**
     * Service incoming requests and executing before/after actions defined by plugins.
     *
     * @param request the request
     * @param response the response
     * @throws IOException when anything goes wrong
     */
    @Override
    @CompileStatic
    void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Set it to HTML by default
        response.contentType = "text/html; charset="+encoding
        // but plugin handler can change that easily!
        PluginsHandler.instance.executeBeforeActions(request, response)
        def result = doService(request, response)
        PluginsHandler.instance.executeAfterActions(request, response, result)
    }

    /**
     * Handle web requests to the GroovyServlet
     */
    @CompileStatic
    private doService(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Set up the script context
        final ServletBinding binding = [
            request,
            response,
            servletContext
        ]
        setVariables(binding)

        // Get the script path from the request - include aware (GROOVY-815)
        String scriptUri = getScriptUri(request)
        def result = null
        // Run the script
        try {
            if(preferPrecompiled){
                try {
                    result = runPrecompiled(getPrecompiledClassName(request.servletPath), binding)
                } catch(ClassNotFoundException e){
                    result = runGroovlet(scriptUri, binding)
                }
            } else {
                try {
                    result = runGroovlet(scriptUri, binding)
                } catch(ResourceException re){
                    try {
                        result = runPrecompiled(getPrecompiledClassName(request.servletPath), binding)
                    } catch(ClassNotFoundException e){
                        throw re
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
                pw.print("GaelykServlet Error: ")
                pw.print(" script: '")
                pw.print(scriptUri)
                getLog(request).warning(sw.toString())
            }
            throw e // Let propogate out the filter chain and container handle the exception.
        }
        result
    }

    @CompileStatic
    private runGroovlet(String scriptUri, ServletBinding binding) {
        gse.run(scriptUri, binding)
    }

    // @CompileStatic
    private runPrecompiled(String precompiledClassName, ServletBinding binding) {
        Class<Script> precompiledClass = (Class<Script>) Class.forName(precompiledClassName)
        Script precompiled = precompiledClass.newInstance([binding]as Object[])
        precompiled.run()
    }

    /**
     * @return name of the precompliled script class
     */
    static String getPrecompiledClassName(servletPath){
        def match = servletPath =~ "/(.+)\\.groovy"
        if(!match){
            throw new ClassNotFoundException('No class found for servlet path ' + servletPath)
        }
        match[0][1].replace '/', '.'
    }
}