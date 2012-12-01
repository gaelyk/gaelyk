package groovyx.gaelyk.routes

import com.google.appengine.api.capabilities.Capability

/**
 * Refactored code from {@link RoutingRule} which needs to be kept written
 * in Groovy.
 * @author Vladimir Orany
 *
 */
class RoutingRuleBuilder {
    
    /**
     * Build capability aware routing rule.
     * @param destinationDefinition definition of destination
     * @return capability aware routing rule
     */
    public static RoutingRule buildRoutingRule(Closure<?> destinationDefinition) {
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


        CapabilityAwareDestination defaultDestination = alternativeDestinations.find { it.isMainRoute() }

        if (!defaultDestination) {
            throw new RuntimeException(
                "Route's redirect / forward closures should contain at least a 'to' destination.")
        }
        
        def rule = new RoutingRule(defaultDestination.destination)
        rule.destinations = alternativeDestinations.findAll { it.isAlternative() }

        return rule
    }
}
