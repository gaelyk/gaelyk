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

import java.io.IOException

import groovy.lang.Closure
import groovy.servlet.AbstractHttpServlet
import groovy.servlet.GroovyServlet
import groovy.servlet.ServletBinding
import groovy.servlet.ServletCategory
import groovy.util.GroovyScriptEngine
import groovy.util.ResourceException
import groovy.util.ScriptException

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import javax.servlet.ServletConfig
import javax.servlet.ServletRequest

import org.codehaus.groovy.runtime.GroovyCategorySupport

import com.google.appengine.api.utils.SystemProperty

import groovyx.gaelyk.plugins.PluginResourceSupport
import groovyx.gaelyk.plugins.PluginsHandler
import groovyx.gaelyk.logging.GroovyLogger

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
    protected void setVariables(ServletBinding binding) {
        GaelykBindingEnhancer.bind(binding)
        PluginsHandler.instance.enrich(binding)
        binding.setVariable("log", getLog(binding.request))
    }

    private GroovyLogger getLog(ServletRequest request){
        GroovyLogger.forGroovletUri(super.getScriptUri(request))
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
     * Handle web requests to the GroovyServlet
     */
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

        final String precompiledClassName = getPrecompiledClassName(request.servletPath)

        // Run the script
        try {
            if(GaelykBindingEnhancer.localMode){
                try {
                    runGroovlet(scriptUri, binding)
                } catch(ResourceException re){
                    try {
                        runPrecompiled(precompiledClassName, binding)
                    } catch(ClassNotFoundException e){
                        throw re
                    }
                }
            } else {
                try {
                    runPrecompiled(precompiledClassName, binding)
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

            /*
             * Other internal error. Perhaps syntax?!
             */
            servletContext.log("An error occurred processing the request", e)
            pw.print(e.getMessage())
            e.printStackTrace(pw)
            servletContext.log(pw.toString())
            getLog(request).warning(pw.toString())
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString())
        }
    }

    private runGroovlet(String scriptUri, ServletBinding binding) {
        gse.run(scriptUri, binding)
    }

    private runPrecompiled(String precompiledClassName, ServletBinding binding) {
        Class precompiledClass = Class.forName(precompiledClassName)
        Script precompiled = precompiledClass.newInstance([binding]as Object[])
        precompiled.run()
    }

    /**
     * This methods adds plugin awareness to the default {@link AbstractHttpServlet#getResourceConnection(String)} method.
     * @param name resource to be found
     */
    @Override
    URLConnection getResourceConnection(String name) throws ResourceException {
        try {
            return super.getResourceConnection(name)
        } catch (ResourceException re){
            return PluginResourceSupport.getResourceConnection("groovy",name)
        }
    }

    /**
     * @return name of the precompliled script class
     */
    static String getPrecompiledClassName(servletPath){
        def match = servletPath =~ "/(.+)\\.groovy"
        if(!match){
            return null
        }
        match[0][1].replace '/', '.'
    }
}