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
package groovyx.routes;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.servlet.AbstractHttpServlet;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

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
public class RoutesFilter implements Filter {

    public static final String ORIGINAL_URI = "originalURI";
    public static final String PATH_VARIABLES = "routesPathVariables";
    
    /**
     * Returns original URI from the request.
     * @param request request which should contain original URI attribute
     * @return original URI from the request
     */
    public static String getOriginalURI(ServletRequest request){
        return (String) request.getAttribute(ORIGINAL_URI);
    }
    
    /**
     * Returns matched path variables from the request.
     * @param request request which should contain matched path variables attribute
     * @return matched path variables from the request
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getPathVariables(ServletRequest request){
        return (Map<String, String>) request.getAttribute(PATH_VARIABLES);
    }


    /**
     * Location of the routes file definition
     */
    private String routesFileLocation = "/WEB-INF/routes.groovy";
    private long lastRoutesFileModification = 0;
    private List<Route> routes = new ArrayList<Route>();
    private FilterConfig filterConfig;
    
    protected Logger log;

    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if(filterConfig.getInitParameter("routes.location") != null){
            this.routesFileLocation = filterConfig.getInitParameter("routes.location");
        }
        this.log = Logger.getLogger(getLoggerName());
        if (isHotReloadEnabled()) {
            routes = new CopyOnWriteArrayList<Route>();
        }
        try {
            loadRoutes();
        } catch (CompilationFailedException e) {
            log.severe("Exception loading routes. Message: " + e.getMessage());
        } catch (IOException e) {
            log.severe("Exception loading routes. Message: " + e.getMessage());
        }            
    }

    /**
     * @return name of the logger to be used for this filter
     */
    protected String getLoggerName() {
        return "groovyx.routesfilter";
    }
    /**
     * @return <code>true</code> if the routes file may change after initialization
     */
    protected boolean isHotReloadEnabled() {
        return "true".equals(filterConfig.getInitParameter("hot.reload"));
    }

    /**
     * Load the routes configuration.
     * @throws IOException 
     * @throws CompilationFailedException 
     */
    protected final synchronized void loadRoutes() throws CompilationFailedException, IOException {
        log.config("Loading routes configuration");

        URL routesFileURL = filterConfig.getServletContext().getResource(this.routesFileLocation);
        File routesFile = null;
        try {
            routesFile = new File(routesFileURL.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI syntax expection while converting routes file URL", e);
        }
        if (routesFile != null && routesFile.exists()) {
            log.config("Routes file exist, loading routes.");
            long lastModified = routesFile.lastModified();

            // if the file has changed since the last check, reload the routes
            if (lastModified > lastRoutesFileModification) {
                CompilerConfiguration config = new CompilerConfiguration();
                config.setScriptBaseClass(getRoutesScriptBaseName());

                // define a binding for the routes definition,
                // and inject the Google services
                Binding binding = new Binding();
                enhanceRoutesScriptBinding(binding);

                // evaluate the route definitions
                GroovyShell groovyShell = new GroovyShell(binding, config);
                RoutesBaseScript script = (RoutesBaseScript) groovyShell.parse(routesFile);

                script.run();

                routes.clear();
                routes.addAll(script.getRoutes());

                // update the last modified flag
                lastRoutesFileModification = lastModified;
            }
        } else {
            log.config("Routes file " + this.routesFileLocation + "does not exist!.");
        }
        // add the routes defined by the plugins
        routes.addAll(getAdditionalRoutes());
        log.info("Loaded routes: " + routes.size());
    }
    
    /**
     * Unmodifiable list of routes for testing purposes only.
     * @return unmodifiable list of routes, use {@link RoutesFilter#getAdditionalRoutes()} for adding routes instead.
     */
    protected List<Route> getRoutes() {
        return Collections.unmodifiableList(routes);
    }

    /**
     * @return name of the class to be used as routes script base class, must extend {@link RoutesBaseScript}.
     */
    protected String getRoutesScriptBaseName() {
        return RoutesBaseScript.class.getName();
    }
    
    /**
     * @return additional routes to be added
     */
    protected List<? extends Route> getAdditionalRoutes(){
        return Collections.emptyList();
        
    }
    
    /**
     * Ehnances binding for routes script.
     * @param binding routes script binding
     */
    protected void enhanceRoutesScriptBinding(Binding binding){ }

    /**
     * Forward or redirects requests to another URL if a matching route is defined.
     * Otherwise, the normal filter chain and routing applies.
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws java.io.IOException, javax.servlet.ServletException {
        // reload the routes in local dev mode in case the routes definition has changed since the last request
        if (isHotReloadEnabled()) {
            loadRoutes();
        }

        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

        String uri = getIncludeAwareUri(request);
        if(request.getAttribute(ORIGINAL_URI) == null){
            request.setAttribute(ORIGINAL_URI, uri);
        }

        String method = request.getMethod().toUpperCase();
        
        if(log.isLoggable(Level.FINEST)){
            log.finest("Routing: " + uri);
        }

        boolean foundRoute = false;
        for (Route route : routes) {
            if(log.isLoggable(Level.FINEST)){
                log.finest("Trying route " + route.getRoute());
            }
            // first, check that the HTTP methods are compatible
            if (route.getMethod() == HttpMethod.ALL || route.getMethod().toString().equals(method)) {
                RouteMatch result = route.forUri(uri, request);
                if (result.isMatches()) {
                    if(log.isLoggable(Level.FINEST)){
                        log.finest("MATCH " + route.getRoute());
                    }
                    if (route.isIgnore()) {
                        // skip out completely
                        break;
                    }
                    if(request.getAttribute(PATH_VARIABLES) == null){
                        request.setAttribute(PATH_VARIABLES, result.getVariables());
                    }
                    handleMatchedRoute(result, route, request, response);
                    foundRoute = true;
                    break;
                } else {
                    if(log.isLoggable(Level.FINEST)){
                        log.finest("NO MATCH " + route.getRoute());
                    }
                }
            } else if(log.isLoggable(Level.FINEST)){
                log.finest("Route " + route.getMethod() + " '" + route.getRoute() + "' method don't support method " + method);
            }
        }

        if (!foundRoute) {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
    
    private void handleMatchedRoute(RouteMatch result, Route route, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
        switch (route.getRedirectionType()) {
        case FORWARD:
            handleForward(result, route, request, response);
            break;
        case REDIRECT301:
            handleRedirect301(result, route, request, response);
            break;
        default:
            handleRedirect(result, route, request, response);
            break;
        }
    }

    /**
     * Handle redirect for given route.
     * @param result matched route result
     * @param route given route
     * @param request servlet request
     * @param response servlet response
     * @throws IOException 
     */
    protected void handleRedirect(RouteMatch result, Route route, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(result.getDestination());
    }
    
    /**
     * Handle permanent redirect for given route.
     * @param result matched route result
     * @param route given route
     * @param request servlet request
     * @param response servlet response
     */
    protected void handleRedirect301(RouteMatch result, Route route, HttpServletRequest request,
            HttpServletResponse response) {
        response.setStatus(301);
        response.setHeader("Location", result.getDestination());
        response.setHeader("Connection", "close");
    }
    
    /**
     * Handle forward for given route.
     * @param result matched route result
     * @param route given route
     * @param request servlet request
     * @param response servlet response
     * @throws IOException 
     * @throws ServletException 
     */
    protected void handleForward(RouteMatch result, Route route, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher(result.getDestination()).forward(request, response);
        
    }
    public void destroy() { }

    /**
    * Returns the include-aware uri.
    *
    * @param request the http request to analyze
    * @return the include-aware uri either parsed from request attributes or
    *         hints provided by the servlet container
    */
    public static String getIncludeAwareUri(HttpServletRequest request) {
        String uri = null;
        String info = null;

        uri = (String) request.getAttribute(AbstractHttpServlet.INC_SERVLET_PATH);
        if (uri != null) {
            info = (String) request.getAttribute(AbstractHttpServlet.INC_PATH_INFO);
            if (info != null) {
                uri += info;
            }
            return uri;
        }

        uri = request.getServletPath();
        info = request.getPathInfo();
        if (info != null) {
            uri += info;
        }
        return uri;
    }
}
