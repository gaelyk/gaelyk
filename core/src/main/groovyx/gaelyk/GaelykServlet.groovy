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

import groovy.servlet.GroovyServlet
import groovy.servlet.ServletBinding

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import javax.servlet.ServletConfig
import javax.servlet.ServletRequest

import groovyx.gaelyk.plugins.PluginsHandler
import groovyx.gaelyk.logging.GroovyLogger

import groovy.transform.CompileStatic

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


    @Override
    @CompileStatic
    void init(ServletConfig config) {
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
        PluginsHandler.instance.executeBeforeActions(request, response)
        doService(request, response)
        PluginsHandler.instance.executeAfterActions(request, response)
    }

    /**
     * Handle web requests to the GroovyServlet
     */
    @CompileStatic
    private void doService(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Set it to HTML by default
        response.contentType = "text/html; charset="+encoding

        // Set up the script context
        final ServletBinding binding = [
            request,
            response,
            servletContext
        ]
        setVariables(binding)

        // Get the script path from the request - include aware (GROOVY-815)
        String scriptUri = getScriptUri(request)

        // Run the script
        try {
            if(GaelykBindingEnhancer.localMode){
                try {
                    runGroovlet(scriptUri, binding)
                } catch(ResourceException re){
                    try {
                        runPrecompiled(getPrecompiledClassName(request.servletPath), binding)
                    } catch(ClassNotFoundException e){
                        throw re
                    }
                }
            } else {
                try {
                    runPrecompiled(getPrecompiledClassName(request.servletPath), binding)
                } catch(ClassNotFoundException e){
                    runGroovlet(scriptUri, binding)
                }
            }
        } catch (e) {
            StringWriter sw = []
            PrintWriter pw  = [sw]

            pw.print("GroovyServlet Error: ")
            pw.print(" script: '")
            pw.print(scriptUri)
            pw.print("': ")

            /*
             * Resource not found.
             */
            if (e instanceof ResourceException) {
                pw.print(" Script not found, sending 404.")
                servletContext.log(sw.toString())
                getLog(request).warning(sw.toString())
                response.sendError(HttpServletResponse.SC_NOT_FOUND)
                return
            }
            throw e // Let propogate out the filter chain and container handle the exception. 
        }
    }

    @CompileStatic
    private runGroovlet(String scriptUri, ServletBinding binding) {
        gse.run(scriptUri, binding)
    }

    @CompileStatic
    private runPrecompiled(String precompiledClassName, ServletBinding binding) {
        Class<Script> precompiledClass = Class.forName(precompiledClassName)
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