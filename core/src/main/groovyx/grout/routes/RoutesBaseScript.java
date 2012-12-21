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
package groovyx.grout.routes;

import groovy.lang.Closure;
import groovy.lang.Script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base script class used for evaluating the routes.
 * 
 * @author Guillaume Laforge
 * @author Vladimir Orany
 */
public abstract class RoutesBaseScript extends Script {
    
    /** The list of routes available */
    private final List<Route> routes = new ArrayList<Route>();

    public void all    (Map<String, Object> m, String route) { handle(m, route, HttpMethod.ALL); }
    public void get    (Map<String, Object> m, String route) { handle(m, route, HttpMethod.GET); }
    public void post   (Map<String, Object> m, String route) { handle(m, route, HttpMethod.POST); }
    public void put    (Map<String, Object> m, String route) { handle(m, route, HttpMethod.PUT); }
    public void delete (Map<String, Object> m, String route) { handle(m, route, HttpMethod.DELETE); }

    /**
     * Handle all routes.
     *
     * @param m a map containing the forward or redirect location,
     * as well as potential validation rules for the variables appearing in the route,
     * a definition of a caching duration, and the ability to ignore certain paths
     * like /_ah/* special URLs.
     */
    protected final void handle(Map<String, Object> m, String route, HttpMethod method) {
        Object dest = extractDestination(m);
        if(dest instanceof String){
            for(Entry<String, String> e : OptionalRoutesHelper.generateRoutes(route, (String) dest).entrySet()){
                routes.add(createRoute(m, e.getKey(), e.getValue(), method));            
            }
        } else {
            routes.add(createRoute(m, route, dest, method)); 
        }

    }
    
    /**
     * Adds new route to the routes.
     * @param route new route
     */
    protected final void addRoute(Route route){
        routes.add(route);
    }
    
    /**
     * Creates new route.
     *
     * @param m a map containing the forward or redirect location,
     * as well as potential validation rules for the variables appearing in the route,
     * a definition of a caching duration, and the ability to ignore certain paths
     * like /_ah/* special URLs.
     */
    protected Route createRoute(Map<String, Object> m, String route, Object destination, HttpMethod method) {
        Closure<?> validator = (Closure<?>) m.get("validate");
        boolean ignore = m.get("ignore") == Boolean.TRUE;

        return new Route(route, createRoutingRule(destination), method, extractRedirectionType(m), validator, ignore);
    }
    /**
     * Extract redirection type from the route parameters map 
     * @param m routes parameter map
     * @return redirection type from routes parameters map
     */
    protected RedirectionType extractRedirectionType(Map<String, Object> m) {
        RedirectionType redirectionType =  RedirectionType.REDIRECT;
        if(m.containsKey("forward")){
            redirectionType = RedirectionType.FORWARD;
        } else if(m.containsKey("redirect301")){
            redirectionType = RedirectionType.REDIRECT301;
        }
        return redirectionType;
    }
    
    /**
     * Extract destination from the route parameters map
     * @param m routes parameter map
     * @return destination from routes parameters map
     */
    protected Object extractDestination(Map<String, Object> m) {
        Object destination = m.get("redirect");
        if(m.containsKey("forward")){
            //redirectionType = RedirectionType.FORWARD;
            destination = m.get("forward");
        } else if(m.containsKey("redirect301")){
            //redirectionType = RedirectionType.REDIRECT301;
            destination = m.get("redirect301");
        }
        return destination;
    }
    
    /**
     * Creates routing rule for destination.
     * @param destination destination of routing rule
     * @return routing rule for destination
     */
    protected RoutingRule createRoutingRule(Object destination) {
        if(destination == null){
            return null;
        }
        return new RoutingRule(destination.toString());
    }
    
    public List<Route> getRoutes() {
        return Collections.unmodifiableList(routes);
    }
}