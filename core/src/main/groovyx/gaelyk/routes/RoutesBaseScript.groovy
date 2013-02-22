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
package groovyx.gaelyk.routes

import java.util.Map.Entry

/**
 * Base script class used for evaluating the routes.
 * 
 * @author Guillaume Laforge
 */
abstract class RoutesBaseScript extends Script {
    /** The list of routes available */
    List<Route> routes = []
    
    /**
     * The order of the first route in this script, the following routes will have
     * order number {@link #firstRouteOrder} + {@link #routes#size()}
     */
    protected int firstRouteIndex = 0
    
    void setFirstRouteIndex(int index){
        throw new UnsupportedOperationException("Use startRoutingAt(index) instead!")
    }
    
    /**
     * The first route will start with the provided index.
     * 
     * Following routes will have index one higher unless they specify index property itself.
     * 
     * @param index index for the first route in this script
     */
    void startRoutingAt(int index){
        this.@firstRouteIndex = index
    }

    def all    (Map m, String route) { handle m, route, HttpMethod.ALL }
    def get    (Map m, String route) { handle m, route, HttpMethod.GET }
    def post   (Map m, String route) { handle m, route, HttpMethod.POST }
    def put    (Map m, String route) { handle m, route, HttpMethod.PUT }
    def delete (Map m, String route) { handle m, route, HttpMethod.DELETE }

    def email  (Map m) {
        routes << new Route("/_ah/mail/*", m.to,
                HttpMethod.POST, RedirectionType.FORWARD,
                null, null, 0, false, true, false, m.index ? Integer.valueOf(m.index) : (firstRouteIndex + routes.size()))
    }

    def jabber (Map m, String type = "chat") {
        if (type == "subscription") {
            routes << new Route("/_ah/xmpp/subscription/@value/", m.to + "?value=@value",
                    HttpMethod.POST, RedirectionType.FORWARD,
                    null, null, 0, false, false, true, m.containsKey('index') ? Integer.valueOf(m.index) : (firstRouteIndex + routes.size()))
        } else if (type == "presence") {
            routes << new Route("/_ah/xmpp/presence/@value/", m.to + "?value=@value",
                    HttpMethod.POST, RedirectionType.FORWARD,
                    null, null, 0, false, false, true, m.containsKey('index') ? Integer.valueOf(m.index) : (firstRouteIndex + routes.size()))
        } else {
            routes << new Route("/_ah/xmpp/message/chat/", m.to,
                    HttpMethod.POST, RedirectionType.FORWARD,
                    null, null, 0, false, false, true, m.containsKey('index') ? Integer.valueOf(m.index) : (firstRouteIndex + routes.size()))
        }
    }

    /**
     * Handle all routes.
     *
     * @param m a map containing the forward or redirect location,
     * as well as potential validation rules for the variables appearing in the route,
     * a definition of a caching duration, and the ability to ignore certain paths
     * like GAE's /_ah/* special URLs.
     */
    protected handle(Map m, String route, HttpMethod method) {
        RedirectionType redirectionType = m.forward ? RedirectionType.FORWARD : (m.redirect301 ? RedirectionType.REDIRECT301 : RedirectionType.REDIRECT)

        def destination = m.forward ?: (m.redirect301 ?: m.redirect)
        def validator = m.validate ?: null
        def cacheExpiration = m.cache ?: 0
        def ignore = m.ignore ?: false
        def ns = m.namespace ?: null
        
        if(destination instanceof String){
            int counter = 0
            for(Entry<String, String> e in OptionalRoutesHelper.generateRoutes(route, destination)){
                routes << new Route(e.key, e.value, method, redirectionType, validator, ns, cacheExpiration, ignore, false, false, m.containsKey('index') ? (Integer.valueOf(m.index) + counter) : (firstRouteIndex + routes.size()))
                counter++
            }
        } else {
                routes << new Route(route, destination, method, redirectionType, validator, ns, cacheExpiration, ignore, false, false, m.containsKey('index') ? Integer.valueOf(m.index) : (firstRouteIndex + routes.size()))
        }
    }
}