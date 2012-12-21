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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;

/**
 * Representation of a route URL mapping.
 *
 * @author Guillaume Laforge
 * @author Vladimir Orany
 */
public class Route {
    
    private static final Logger log = Logger.getLogger(Route.class.getName());
    
    /** The route pattern */
    private final String route;

    /** The destination pattern when the route is matched, can be a String or a RoutingRule */
    private final RoutingRule destination;

    /** The HTTP method used to reach that route */
    private final HttpMethod method;

    /** Whether we're doing a redirect or a forward to the new location */
    private final RedirectionType redirectionType;

    /** The list of variables in the route */
    private final List<String> variables;

    /** The real regex pattern used for matching URIs */
    private final Pattern regex;

    /** Closure validating the variables match the required regex patterns */
    private final Closure<?> validator;

    /** Should a uri matching this route just be ignored? */
    private final boolean ignore;

    /**
     * Constructor taking a route, a destination, an HTTP method (optional), a redirection type (optional),
     * and a closure for validating the variables against regular expression patterns.
     */
    public Route(String route, RoutingRule destination, HttpMethod method,
          RedirectionType redirectionType, Closure<?> validator, boolean ignore) {
        this.route = route;
        this.method = method;
        this.redirectionType = redirectionType;
        this.validator = validator;
        this.ignore = ignore;
        this.destination = destination;

        // extract the path variables from the route
        this.variables = extractParameters(route);

        // create a regular expression out of the route string
        this.regex = Pattern.compile(transformRouteIntoRegex(route));

    }
    
    /**
     * Clone constructor.
     * @param original route to be cloned
     * @see #Route(String, RoutingRule, HttpMethod, RedirectionType, Closure, boolean)
     */
    public Route(Route original){
        this(original.route, original.destination, original.method, original.redirectionType, original.validator, original.ignore);
    }

    /**
     * Creates simple forwarding route for all HTTP methods.
     */
    public Route(String route, String destination) {
        this(route, new RoutingRule(destination), HttpMethod.ALL, RedirectionType.FORWARD, null, false);
    }
    
    public Route(String route, RoutingRule destination) {
        this(route, destination, HttpMethod.ALL, RedirectionType.FORWARD, null, false);
    }

    /**
     * Checks whether a URI matches a route.
     * @param uri2
     *
     * @return a map with a 'matches' boolean key telling whether the route is matched
     * and a variables key containing a map of the variable key and matched value.
     */
    public RouteMatch forUri(String includeAwareURI, HttpServletRequest request) {
        String uri = cutOffDisregardingParts(includeAwareURI);
        
        if(log.isLoggable(Level.FINEST)){
            log.finest(route + ": matching '" + uri + "'");
        }
        
        Matcher matcher = regex.matcher(uri);

        String finalDestination = destination == null || ignore == true ? "" : destination.getFinalDestination();
        
        if (matcher.matches()) {
            if(log.isLoggable(Level.FINEST)){
                log.finest(route + ": regex '" + regex.pattern() + "' matched");
            }
            Map<String,String> variableMap = new LinkedHashMap<String, String>();
            if(variables != null && !variables.isEmpty()){
                for (int i = 0; i < variables.size(); i++) {
                    variableMap.put(variables.get(i).substring(1), matcher.group(i+1));
                }
            }

            // if a closure validator was defined, check all the variables match the regex pattern
            if (validator != null) {
                if (!isValid(request, variableMap)) {
                    if(log.isLoggable(Level.FINEST)){
                        log.finest(route + ": validator refused the route");
                    }
                    return RouteMatch.noMatch();
                } else {
                    if(log.isLoggable(Level.FINEST)){
                        log.finest(route + ": passed validator validation");
                    }
                }
            }
            return RouteMatch.to(getEffectiveDestination(finalDestination, variableMap), variableMap);
        } else {
            if(log.isLoggable(Level.FINEST)){
                log.finest(route + ": regex " + regex.pattern() + " did not match");
            }
            return RouteMatch.noMatch();
        }
    }

    /**
     * @return The route pattern
     */
    public final String getRoute() {
        return route;
    }

    /**
     * @return The destination pattern when the route is matched
     */
    public final RoutingRule getDestination() {
        return destination;
    }

    /**
     * @return The HTTP method used to reach that route
     */
    public final HttpMethod getMethod() {
        return method;
    }

    /**
     * Whether we're doing a redirect or a forward to the new location
     */
    public final RedirectionType getRedirectionType() {
        return redirectionType;
    }

    /**
     * @return Should a uri matching this route just be ignored?
     */
    public final boolean isIgnore() {
        return ignore;
    }
    
    /**
     * Supplies additional variables available in validation closure.
     * @return additional variables available in validation closure
     */
    protected Map<String, Object> getAdditionalValidationVaribles() {
        return Collections.emptyMap();
    }

    /**
     * Extract a list of parameters in the route URI.
     */
    private static List<String> extractParameters(String route) {
        Pattern pattern = Pattern.compile("@\\w*");
        return Collections.unmodifiableList(StringGroovyMethods.findAll(route, pattern));
    }

    /**
     * Transform a route pattern into a proper regex pattern.
     */
    private static String transformRouteIntoRegex(String route) {
        if(route.matches("\\/\\*\\*\\/\\*\\.(\\w+)$")){
            return route.replace("**/*.", ".*\\.") + "$";
        }
        if(route.matches("\\/\\*\\*(\\/\\*\\.\\*)?$")){
            return ".*";
        }
        return route.replaceAll("\\.", "\\\\.")
                    .replaceAll("\\*\\*", "(?:.+\\/?){0,}")
                    .replaceAll("\\*", "[^\\/]+")
                    .replaceAll("@\\w+", "(.+)");
    }
    
    private String getEffectiveDestination(String finalDestination, Map<String, String> variableMap) {
        String effectiveDestination = finalDestination;
        for(Entry<String, String> entry : variableMap.entrySet()){
            effectiveDestination = effectiveDestination.replaceAll("@" + entry.getKey(), entry.getValue());
        }
        return effectiveDestination;
    }

    private String cutOffDisregardingParts(String uri) {
        // disregarding URL parts with appended ;jsessionid=xxx
        int cutoff = uri.indexOf(';');
        if (cutoff > -1) {
            uri = uri.substring(0, cutoff);
        } else {
            // disregarding URL parts with parameters ?x=y
            cutoff = uri.indexOf('?');
            if (cutoff > -1) {
                uri = uri.substring(0, cutoff);
            }
        }
        return uri;
    }
    
    private boolean isValid(ServletRequest request, Map<String, String> variableMap){
        Closure<?> clonedValidator = (Closure<?>) validator.clone();

        // adds the request to the variables available for validation
        Map<String, Object> validatorDelegate = new LinkedHashMap<String, Object>();
                
        validatorDelegate.putAll(getAdditionalValidationVaribles());
        validatorDelegate.putAll(new LinkedHashMap<String, String>(variableMap));
        validatorDelegate.put("request", request);

        clonedValidator.setDelegate(validatorDelegate);
        clonedValidator.setResolveStrategy(Closure.DELEGATE_ONLY);
        
        // TODO: is this the best way how to do it?
        Object validationResult = clonedValidator.call();
        return Boolean.TRUE.equals(DefaultGroovyMethods.asType(validationResult, Boolean.class));
    }
}
