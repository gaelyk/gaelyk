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
package groovyx.gaelyk.routes;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovyx.gaelyk.GaelykBindingEnhancer;
import groovyx.routes.HttpMethod;
import groovyx.routes.RedirectionType;
import groovyx.routes.RouteMatch;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Representation of a route URL mapping.
 *
 * @author Guillaume Laforge
 * @author Vladimir Orany
 */
public class Route extends groovyx.routes.Route {

    /** Closure defining a namespace for the scope of the request */
    private final Closure<?> namespace;

    private final int cacheExpiration;
    private final boolean email;
    private final boolean jabber;
    

    /**
     * Constructor taking a route, a destination, an HTTP method (optional), a redirection type (optional),
     * and a closure for validating the variables against regular expression patterns.
     */
    public Route(groovyx.routes.Route original, Closure<?> namespace, int cacheExpiration, boolean email, boolean jabber) {
        super(original);
        this.namespace = namespace;
        this.cacheExpiration = cacheExpiration;
        this.email = email;
        this.jabber = jabber;
    }
    
    /**
     * Simple forwarding rule for all methods.
     */
    public Route(String route, String destination){
        this(new groovyx.routes.Route(route, destination));
    }
    
    /**
     * Simple forwarding rule for all methods.
     */
    public Route(String route, Closure<?> destination){
        this(new groovyx.routes.Route(route, RoutingRuleBuilder.buildRoutingRule(destination)));
    }
    
    /**
     * Backward compatibility constructor.
     */
    public Route(String route, String destination, HttpMethod method,
            RedirectionType redirectionType, Closure<?> validator,
            Closure<?> namespace, int cacheExpiration, boolean ignore,
            boolean email, boolean jabber){
        this(new groovyx.routes.Route(route, new RoutingRule(destination), method, redirectionType, validator, ignore), namespace, cacheExpiration, email, jabber);
    }
    
    /**
     * Backward compatibility constructor.
     */
    public Route(String route, Closure<?> destination, HttpMethod method,
            RedirectionType redirectionType, Closure<?> validator,
            Closure<?> namespace, int cacheExpiration, boolean ignore,
            boolean email, boolean jabber){
        this(new groovyx.routes.Route(route, RoutingRuleBuilder.buildRoutingRule(destination), method, redirectionType, validator, ignore), namespace, cacheExpiration, email, jabber);
    }
    
    /**
     * Backward compatibility constructor.
     */
    public Route(String route, String destination, HttpMethod method,
            RedirectionType redirectionType, Closure<?> validator,
            Closure<?> namespace, int cacheExpiration){
        this(new groovyx.routes.Route(route, new RoutingRule(destination), method, redirectionType, validator, false), namespace, cacheExpiration, false, false);
    }
    
    /**
     * Backward compatibility constructor.
     */
    public Route(String route, Closure<?> destination, HttpMethod method,
            RedirectionType redirectionType, Closure<?> validator,
            Closure<?> namespace, int cacheExpiration){
        this(new groovyx.routes.Route(route, RoutingRuleBuilder.buildRoutingRule(destination), method, redirectionType, validator, false), namespace, cacheExpiration, false, false);
    }
    
    /**
     * Creates route wrapping original route.
     * @param original route wrapping original route
     */
    public Route(groovyx.routes.Route original){
        this(original, null, 0, false, false);
    }

    @Override
    public RouteMatch forUri(HttpServletRequest request) {
        RouteMatch match = super.forUri(request);
        if (namespace == null) {
            return match;
        }
        Closure<?> ns = (Closure<?>) namespace.clone();
        ns.setResolveStrategy(Closure.DELEGATE_ONLY);
        ns.setDelegate(new LinkedHashMap<String, String>(match.getVariables()));

        Object result = ns.call();
        
        if(result == null){
            return match;
        }
        // add the namespace to the found matching route
        return RouteMatchWithNamespace.withNamespace(match, result.toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Map<String, Object> getAdditionalValidationVaribles() {
        Binding binding = new Binding();
        GaelykBindingEnhancer.bind(binding);
        return binding.getVariables();
    }
    
    /** 
     * @return The time in seconds the resource to stay in memcache 
     */
    public int getCacheExpiration() {
        return cacheExpiration;
    }
    
    /**
     *  @return If the route is for incoming email 
     */
    public boolean isEmail() {
        return email;
    }

    
    /**
     * @return If the route is for incoming jabber messages 
     */
    public boolean isJabber() {
        return jabber;
    }
}
