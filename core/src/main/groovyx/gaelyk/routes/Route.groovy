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

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Representation of a route URL mapping.
 *
 * @author Guillaume Laforge
 */
class Route {
    /** The route pattern */
    String route

    /** The destination pattern when the route is matched */
    String destination

    /** The HTTP method used to reach that route */
    HttpMethod method

    /** Whether we're doing a redirect or a forward to the new location */
    RedirectionType redirectionType

    /* The list of variables in the route */
    private List variables

    /* The real regex pattern used for matching URIs */
    private Pattern regex

    /* Closure validating the variables match the required regex patterns */
    private Closure validator

    /**
     * Constructor taking a route, a destination, an HTTP method (optional), a redirection type (optional),
     * and a closure for validating the variables against regular expression patterns.
     */
    Route(String route, String destination, HttpMethod method = HttpMethod.ALL, RedirectionType redirectionType = RedirectionType.FORWARD, Closure validator = null) {
        this.route = route
        this.destination = destination
        this.variables = extractParameters(route)
        this.regex = Pattern.compile(transformRouteIntoRegex(route))
        this.method = method
        this.redirectionType = redirectionType

        this.validator = validator
    }

    String toString() {
        "[Route: $route, method: $method, redirection: $redirectionType, to: $destination]"
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
        route.replaceAll('\\.', '\\\\.')
                .replaceAll('\\*\\*', '(?:.+\\/?){0,}')
                .replaceAll('\\*', '.+')
                .replaceAll('@\\w+', '(.+)')
    }

    /**
     * Checks whether a URI matches a route.
     *
     * @return a map with a 'matches' boolean key telling whether the route is matched
     * and a variables key containing a map of the variable key and matched value.
     */
    def forUri(String uri) {
        Matcher matcher = regex.matcher(uri)

        if (matcher.matches()) {
            def variableMap = variables ?
                // a map like ['@year': '2009', '@month': '11']
                variables.inject([:]) { map, variable -> [*:map, (variable): matcher[0][map.size()+1]] } :
                [:] // an empty variables map if no variables were present

            // if a closure validator was defined, check all the variables match the regex pattern
            if (validator) {
                // create a map so the properties
                def delegateVariables = variableMap.inject([:]) { Map m, entry ->
                    [*:m, (entry.key.substring(1)): entry.value]
                }

                def clonedValidator = this.validator.clone()
                clonedValidator.resolveStrategy = Closure.DELEGATE_ONLY
                clonedValidator.delegate = delegateVariables

                boolean validated = clonedValidator()
                if (!validated) {
                    return [matches: false]
                }
            }

            // replace all the variables
            def effectiveDestination = variableMap.inject (destination) { String dest, var ->
                dest.replaceAll(var.key, var.value)
            }

            [matches: true, variables: variableMap, destination: effectiveDestination]
        } else {
            [matches: false]
        }
    }
}
