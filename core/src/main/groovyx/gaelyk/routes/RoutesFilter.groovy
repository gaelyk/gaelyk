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
package groovyx.gaelyk.routes

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletResponse
import javax.servlet.ServletRequest
import javax.servlet.FilterConfig
import org.codehaus.groovy.control.CompilerConfiguration
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import groovyx.gaelyk.GaelykBindingEnhancer
import groovyx.gaelyk.plugins.PluginsHandler
import com.google.appengine.api.utils.SystemProperty
import groovyx.gaelyk.ExpirationTimeCategory
import com.google.appengine.api.memcache.MemcacheServiceFactory
import com.google.appengine.api.memcache.Expiration
import java.text.SimpleDateFormat

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

    // Date formatter for caching headers date creation
    private static final SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
    static {
        httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
    }

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

                // define a binding for the routes definition,
                // and inject the Google services
                def binding = new Binding()
                GaelykBindingEnhancer.bind(binding)
                
                // evaluate the route definitions
                RoutesBaseScript script = (RoutesBaseScript) new GroovyShell(binding, config).parse(routesFile)

                use(ExpirationTimeCategory) {
                    script.run()
                }
                this.routes = script.routes

                // First initialization of the plugins if the routes filter is installed
                PluginsHandler.instance.initPlugins()
                // add the routes defined by the plugins
                this.routes.addAll PluginsHandler.instance.routes

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
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
            loadRoutes()
        }

        HttpServletRequest request = (HttpServletRequest)servletRequest
        HttpServletResponse response = (HttpServletResponse)servletResponse

        def requestURI = request.requestURI
        def method = request.method

        def uri = requestURI + (request.queryString ? "?$request.queryString" : "")
        def contentKey = "content-for-$uri"

        boolean foundRoute = false
        for (Route route in routes) {
            // first, check that the HTTP methods are compatible
            if (route.method == HttpMethod.ALL || route.method.toString() == method) {
                def result = route.forUri(requestURI)
                if (result.matches) {
                    if (route.redirectionType == RedirectionType.FORWARD) {
                        if (route.cacheExpiration > 0) {
                            def memcache = MemcacheServiceFactory.memcacheService

                            def ifModifiedSince = request.getHeader("If-Modified-Since")
                            // if an "If-Modified-Since" header is present in the incoming requestion
                            if (ifModifiedSince) {
                                def sinceDate = httpDateFormat.parse(ifModifiedSince)
                                def lastModifiedKey = "last-modified-$uri"
                                String lastModifiedString = memcache.get(lastModifiedKey)
                                if (lastModifiedString && httpDateFormat?.parse(lastModifiedString).before(sinceDate)) {
                                    response.sendError HttpServletResponse.SC_NOT_MODIFIED
                                    response.setHeader("Last-Modified", ifModifiedSince)
                                } else {
                                    // check if the page is already in the cache
                                    if (memcache.contains(contentKey)) {
                                        sendFromCache(request, response, result.destination, uri, route.cacheExpiration)
                                    } else { // the resource was not present in the cache
                                        sendOutputAndCache(request, response, result.destination, uri, route.cacheExpiration)
                                    }
                                }
                            } else {
                                sendFromCache(request, response, result.destination, uri, route.cacheExpiration)
                            }

                        } else {
                            filterConfig.servletContext.getRequestDispatcher(result.destination).forward request, response
                        }
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

    private sendFromCache(HttpServletRequest request, HttpServletResponse response, String destination, String uri, int cacheExpiration) {
        def contentKey = "content-for-$uri"
        def typeKey = "content-type-for-$uri"

        def memcache = MemcacheServiceFactory.memcacheService

        def content = memcache.get(contentKey)
        def type = memcache.get(typeKey)

        // the resource is still present in the cache
        if (content && type) {
            // if it's in the cache, return the page from the cache
            response.contentType = type
            response.outputStream << content
        } else {
            sendOutputAndCache(request, response, destination, uri, cacheExpiration)
        }
    }

    private sendOutputAndCache(HttpServletRequest request, HttpServletResponse response, String destination, String uri, int cacheExpiration) {
        def now = new Date()
        def lastModifiedString = httpDateFormat.format(now)

        def contentKey = "content-for-$uri"
        def typeKey = "content-type-for-$uri"
        def lastModifiedKey = "last-modified-$uri"

        def memcache = MemcacheServiceFactory.memcacheService

        // specify caching durations
        response.addHeader "Cache-Control", "max-age=${cacheExpiration}"
        response.addHeader "Last-Modified", lastModifiedString
        response.addHeader "Expires", httpDateFormat.format(new Date(now.time + cacheExpiration * 1000))
        //response.addHeader "ETag", "\"\""
        def duration = Expiration.byDeltaSeconds(cacheExpiration)

        def cachedResponse = new CachedResponse(response: response)
        filterConfig.servletContext.getRequestDispatcher(destination).forward request, cachedResponse
        def byteArray = cachedResponse.output.toByteArray()

        // put the output in memcache
        memcache.put(contentKey, byteArray, duration)
        memcache.put(typeKey, cachedResponse.contentType, duration)
        memcache.put(lastModifiedKey, lastModifiedString, duration)

        // output back to the screen
        response.contentType = cachedResponse.contentType
        response.outputStream << byteArray
    }

    void destroy() { }
}
