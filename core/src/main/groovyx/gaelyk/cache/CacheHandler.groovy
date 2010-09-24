package groovyx.gaelyk.cache

import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import com.google.appengine.api.memcache.MemcacheServiceFactory
import com.google.appengine.api.memcache.Expiration
import java.text.SimpleDateFormat
import groovyx.gaelyk.routes.Route
import groovyx.gaelyk.logging.GroovyLogger

/**
 * Class handling the caching of the pages in Memcache.
 *
 * @since 0.4.4
 * 
 * @author Guillaume Laforge
 */
class CacheHandler {

    // Date formatter for caching headers date creation
    private static final SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
    static {
        httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
    }

    private static final GroovyLogger log = new GroovyLogger("gaelyk.cache")

    static void serve(Route route, HttpServletRequest request, HttpServletResponse response) {
        log.config "Serving for route $route"

        def requestURI = request.requestURI
        def uri = requestURI + (request.queryString ? "?$request.queryString" : "")
        def result = route.forUri(requestURI)

        if (route.cacheExpiration > 0) {
            log.config "Route cacheable"

            def memcache = MemcacheServiceFactory.memcacheService

            def ifModifiedSince = request.getHeader("If-Modified-Since")
            // if an "If-Modified-Since" header is present in the incoming requestion
            if (ifModifiedSince) {
                log.config "If-Modified-Since header present"

                def sinceDate = httpDateFormat.parse(ifModifiedSince)
                def lastModifiedKey = "last-modified-$uri"
                String lastModifiedString = memcache.get(lastModifiedKey)
                if (lastModifiedString && httpDateFormat?.parse(lastModifiedString).before(sinceDate)) {
                    log.config "Sending NOT_MODIFIED"

                    response.sendError HttpServletResponse.SC_NOT_MODIFIED
                    response.setHeader("Last-Modified", ifModifiedSince)
                    return
                }
            }
            serveAndCacheOrServeFromCache(request, response, result.destination, uri, route.cacheExpiration)
        } else {
            log.config "Route not cacheable"

            request.getRequestDispatcher(result.destination).forward request, response
        }
    }

    static private serveAndCacheOrServeFromCache(HttpServletRequest request, HttpServletResponse response, String destination, String uri, int cacheExpiration) {
        log.config "Serve and/or cache for URI $uri"

        def contentKey = "content-for-$uri"
        def typeKey = "content-type-for-$uri"

        def memcache = MemcacheServiceFactory.memcacheService

        def content = memcache.get(contentKey)
        def type = memcache.get(typeKey)

        // the resource is still present in the cache
        if (content && type) {
            log.config "Content present in the cache, outputing content-type and content"

            // if it's in the cache, return the page from the cache
            response.contentType = type
            response.outputStream << content
        } else { // serve and cache
            log.config "Not in the cache"

            def now = new Date()
            def lastModifiedString = httpDateFormat.format(now)

            def lastModifiedKey = "last-modified-$uri"

            // specify caching durations
            response.addHeader "Cache-Control", "max-age=${cacheExpiration}"
            response.addHeader "Last-Modified", lastModifiedString
            response.addHeader "Expires", httpDateFormat.format(new Date(now.time + cacheExpiration * 1000))
            //response.addHeader "ETag", "\"\""
            def duration = Expiration.byDeltaSeconds(cacheExpiration)

            log.config "Wrapping a response for caching and forwarding to resource to be cached"
            def cachedResponse = new CachedResponse(response)
            request.getRequestDispatcher(destination).forward request, cachedResponse
            def byteArray = cachedResponse.output.toByteArray()

            log.config "Byte array of wrapped response will be put in memcache: ${new String(byteArray)}"

            // put the output in memcache
            memcache.put(contentKey, byteArray, duration)
            memcache.put(typeKey, cachedResponse.contentType, duration)
            memcache.put(lastModifiedKey, lastModifiedString, duration)

            log.config "Serving content-type and byte array"

            // output back to the screen
            response.contentType = cachedResponse.contentType
            response.outputStream << byteArray
        }
    }
}
