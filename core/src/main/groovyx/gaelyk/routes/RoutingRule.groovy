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

import com.google.appengine.api.capabilities.CapabilitiesServiceFactory
import com.google.appengine.api.capabilities.Capability
import com.google.appengine.api.capabilities.CapabilitiesService
import groovy.transform.CompileStatic

/**
 * A routing rule represents the forward or redirect part of a rule definition.
 * A default route is present, but alternative routes can be defined
 * depending on the status of a "capability", like DATAST0RE_WRITE, MEMCACHE, etc.,
 * whether that capability is ENABLED, DISABLED, SCHEDULED_MAINTANCE or UNKNOWN.
 * The order of the rules definitions is important,
 * as the first rule validated will be the one which will be followed.
 *
 * @author Guillaume Laforge
 */
class RoutingRule {
    /**
     * The default destination
     */
    String defaultDestination

    /**
     * A list of alternative destinations depending on the status of a capability
     */
    List<CapabilityAwareDestination> destinations

    CapabilitiesService service = CapabilitiesServiceFactory.capabilitiesService

    /**
     * @return the final destination, according to the routing rules and the availability of the various services
     */
    @CompileStatic
    String getFinalDestination() {
        CapabilityAwareDestination alternate = destinations.find { CapabilityAwareDestination dest ->
            if (dest.comparison == CapabilityAwareDestination.CapabilityComparisonOperator.IS) {
                service.getStatus(dest.capability).getStatus() == dest.status
            } else if(dest.comparison == CapabilityAwareDestination.CapabilityComparisonOperator.NOT) {
                service.getStatus(dest.capability).getStatus() != dest.status
            }
        }

        if (alternate) {
            return alternate.destination
        } else {
            return defaultDestination
        }
    }

    static RoutingRule buildRoutingRule(Closure destinationDefinition) {
        def alternativeDestinations = []

        def clone = destinationDefinition.clone()
        clone.delegate = new Expando([
                to: { String dest ->
                    def csd = new CapabilityAwareDestination(destination: dest)
                    alternativeDestinations << csd
                    [on: { Capability c ->
                        csd.capability = c
                        return csd
                    }]
                },
        ])
        clone.resolveStrategy = Closure.DELEGATE_ONLY
        clone()

        def rule = new RoutingRule()

        CapabilityAwareDestination defaultDestination = alternativeDestinations.find { it.isMainRoute() }

        if (defaultDestination) {
            rule.defaultDestination = defaultDestination.destination
        } else {
            throw new RuntimeException(
                    "Route's redirect / forward closures should contain at least a 'to' destination.")
        }

        List<CapabilityAwareDestination> otherDestinations = alternativeDestinations.findAll { it.isAlternative() }
        rule.destinations = otherDestinations

        return rule
    }
}
