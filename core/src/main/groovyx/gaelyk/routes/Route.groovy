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

import groovy.transform.Canonical
import groovyx.gaelyk.GaelykBindingEnhancer

import java.util.regex.Matcher
import java.util.regex.Pattern

import javax.servlet.http.HttpServletRequest

/**
 * Representation of a route URL mapping.
 *
 * @author Guillaume Laforge
 */
@Canonical
class Route implements Comparable<Route>{
    /** The route pattern */
    String route

    /** The destination pattern when the route is matched, can be a String or a RoutingRule */
    def destination

    /** The HTTP method used to reach that route */
    HttpMethod method

    /** Whether we're doing a redirect or a forward to the new location */
    RedirectionType redirectionType

    /** The time in seconds the resource to stay in memcache */
    int cacheExpiration

    /** Closure defining a namespace for the scope of the request */
    Closure namespace

    /* The list of variables in the route */
    private List variables

    /* The real regex pattern used for matching URIs */
    private Pattern regex

    /* Closure validating the variables match the required regex patterns */
    private Closure validator

    /** Should a uri matching this route just be ignored? */
    boolean ignore

    /** If the route is for incoming email */
    boolean email

    /** If the route is for incoming jabber messages */
    boolean jabber
    
    /** Can change the order of the routes in routes filter. Routes with lesser number will be evaluated first. Could be negative. */
    int index

    /**
     * Constructor taking a route, a destination, an HTTP method (optional), a redirection type (optional),
     * and a closure for validating the variables against regular expression patterns.
     */
    Route(String route, /* String or Closure */ destination, HttpMethod method,
          RedirectionType redirectionType, Closure validator,
          Closure namespace, int cacheExpiration, boolean ignore,
          boolean email, boolean jabber, int index) {
        this.route = route
        this.method = method
        this.redirectionType = redirectionType
        this.namespace = namespace
        this.cacheExpiration = cacheExpiration
        this.validator = validator
        this.ignore = ignore
        this.email = email
        this.jabber = jabber
        this.index = index

        // extract the path variables from the route
        this.variables = extractParameters(route)

        // create a regular expression out of the route string
        this.regex = Pattern.compile(transformRouteIntoRegex(route))

        // either a normal String route definition,
        // or a closure route with capability aware routing rules
        this.destination = destination instanceof String || ignore == true ?
            destination :
            RoutingRule.buildRoutingRule((Closure) destination)
    }

    /**
     * Extract a list of parameters in the route URI.
     */
    static List<String> extractParameters(String route) {
        route.findAll(/@\w*/)
    }

    /**
     * Transform a route pattern into a proper regex pattern.
     */
    static String transformRouteIntoRegex(String route) {
        if(route.matches(/\/\*\*\/\*\.(\w+)$/)){
            return route.replace('**/*.', '.*\\.') + '$'
        }
        if(route.matches(/\/\*\*(\/\*\.\*)?$/)){
            return '.*'
        }
        route.replaceAll('\\.', '\\\\.')
                .replaceAll('\\*\\*', '(?:.+\\/?){0,}')
                .replaceAll('\\*', '[^\\/]+')
                .replaceAll('@\\w+', '(.+)')
    }

    /**
     * Checks whether a URI matches a route.
     *
     * @return a map with a 'matches' boolean key telling whether the route is matched
     * and a variables key containing a map of the variable key and matched value.
     */
    def forUri(String uri, HttpServletRequest request) {
        // disregarding URL parts with appended ;jsessionid=xxx
        int cutoff = uri.indexOf(';')
        if (cutoff > -1) {
            uri = uri.substring(0, cutoff)
        } else {
            // disregarding URL parts with parameters ?x=y
            cutoff = uri.indexOf('?')
            if (cutoff > -1) {
                uri = uri.substring(0, cutoff)
            }
        }

        Matcher matcher = regex.matcher(uri)

        String finalDestination = destination instanceof String || ignore == true ? 
            destination : destination.finalDestination

        if (matcher.matches()) {
            def variableMap = variables ?
                // a map like ['year': '2009', 'month': '11']
                variables.inject([:]) { map, variable ->
                    map[variable.substring(1)] = matcher[0][map.size()+1]
                    return map
                } : [:] // an empty variables map if no variables were present

            // if a closure validator was defined, check all the variables match the regex pattern
            if (validator) {
                def clonedValidator = this.validator.clone()

                // adds the request to the variables available for validation
                def binding = new Binding()
                GaelykBindingEnhancer.bind(binding)
                def validatorDelegate = binding.variables
                validatorDelegate += variableMap.clone()
                validatorDelegate.request = request
                clonedValidator.delegate = validatorDelegate
                clonedValidator.resolveStrategy = Closure.DELEGATE_ONLY

                boolean validated = false
                    validated = clonedValidator()
                if (!validated) {
                    return [matches: false]
                }
            }

            // replace all the variables
            def effectiveDestination = variableMap.inject (finalDestination) { String dest, var ->
                dest.replaceAll('@' + var.key, var.value)
            }

            def result = [matches: true, variables: variableMap, destination: effectiveDestination]

            // if a closure namespace was defined, clone it, and inject the variables if any
            if (namespace) {
                def ns = namespace.clone()
                ns.resolveStrategy = Closure.DELEGATE_ONLY
                ns.delegate = variableMap

                // add the namespace to the found matching route
                result.namespace = ns()
            }

            request.setAttribute("originalURI", uri)

            return result
        } else {
            [matches: false]
        }
    }

    @Override public int compareTo(Route o) {
        if(o == null) {
            return 0
        }
        this.index <=> o.index
    }
}
