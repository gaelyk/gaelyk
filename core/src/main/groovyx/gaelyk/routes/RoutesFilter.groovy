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
package groovyx.gaelyk.routes

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletResponse
import javax.servlet.ServletRequest
import javax.servlet.FilterConfig
import com.google.apphosting.api.ApiProxy
import org.codehaus.groovy.control.CompilerConfiguration
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * <code>RoutesFilter</code> is a Servlet Filter whose responsability is to define URL mappings for your
 * Gaelyk application. When the servlet filter is configured, a file named <code>routes.groovy</code>
 * will be loaded by the filter, defining the various routes a web request may follow.
 * <p>
 * It is possible to customize the location of the routes definition file by using the
 * <code>routes.location</code> init parameter in the declaration of the filter in <code>web.xml</code>.
 * <p>
 * In development mode, routes will be reloaded automatically on each request, but when the application
 * is deployed on the Google cloud, all the routes will be set in stone.
 *
 * @author Guillaume Laforge
 */
class RoutesFilter implements Filter {

    /**
     * Location of the routes file definition
     */
    private String routesFileLocation
    private long lastRoutesFileModification = 0
    private List<Route> routes = []
    private FilterConfig filterConfig

    void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig
        this.routesFileLocation = filterConfig.getInitParameter("routes.location") ?: "WEB-INF/routes.groovy"
        loadRoutes()
    }

    /**
     * Load the routes configuration
     */
    private loadRoutes() {
        def routesFile = new File(this.routesFileLocation)

        if (routesFile.exists()) {
            def lastModified = routesFile.lastModified()

            // if the file has changed since the last check, reload the routes
            if (lastModified > lastRoutesFileModification) {
                def config = new CompilerConfiguration()
                config.scriptBaseClass = RoutesBaseScript.class.name

                // evaluate the route definitions
                RoutesBaseScript script = (RoutesBaseScript) new GroovyShell(config).parse(routesFile)
                script.run()
                this.routes = script.routes

                // update the last modified flag
                lastRoutesFileModification = lastModified
            }
        }
    }

    /**
     * Forward or redirects requests to another URL if a matching route is defined.
     * Otherwise, the normal filter chain and routing applies.
     */
    void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        // reload the routes in local dev mode in case the routes definition has changed since the last request
        if (ApiProxy.currentEnvironment.class.name.contains("LocalHttpRequestEnvironment")) {
            loadRoutes()
        }

        HttpServletRequest request = (HttpServletRequest)servletRequest
        HttpServletResponse response = (HttpServletResponse)servletResponse

        def requestURI = request.requestURI
        def method = request.method

        boolean foundRoute = false
        for (Route route in routes) {
            // first, check that the HTTP methods are compatible
            if (route.method == HttpMethod.ALL || route.method.toString() == method) {
                def result = route.forUri(requestURI)
                if (result.matches) {
                    if (route.redirectionType == RedirectionType.FORWARD) {
                        filterConfig.servletContext.getRequestDispatcher(result.destination).forward request, response
                    } else {
                        response.sendRedirect result.destination
                    }
                    foundRoute = true
                    break
                }
            }
        }
        
        if (!foundRoute) {
            filterChain.doFilter servletRequest, servletResponse
        }
    }

    void destroy() { }
}
