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

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;

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
public class RoutingRule extends groovyx.grout.routes.RoutingRule {
    
    
    RoutingRule(String defaultDestination){
        super(defaultDestination);
    }

    /**
     * A list of alternative destinations depending on the status of a capability
     */
    private List<CapabilityAwareDestination> destinations = new ArrayList<CapabilityAwareDestination>();

    private CapabilitiesService service = CapabilitiesServiceFactory.getCapabilitiesService();

    /**
     * @return the final destination, according to the routing rules and the availability of the various services
     */
    public String getFinalDestination() {
        CapabilityAwareDestination alternate = null;
        
        for(CapabilityAwareDestination dest : destinations){
            if (dest.getComparison() == CapabilityAwareDestination.CapabilityComparisonOperator.IS) {
                if(service.getStatus(dest.getCapability()).getStatus() == dest.getStatus()){
                    alternate = dest;
                    break;
                }
            } else if(dest.getComparison() == CapabilityAwareDestination.CapabilityComparisonOperator.NOT) {
                if(service.getStatus(dest.getCapability()).getStatus() != dest.getStatus()){
                    alternate = dest;
                    break;
                }
            }
        }

        if (alternate != null) {
            return alternate.getDestination();
        } else {
            return getDefaultDestination();
        }
    }
    
    public static RoutingRule buildRoutingRule(Closure<?> destinationDefinition) {
        return RoutingRuleBuilder.buildRoutingRule(destinationDefinition);
    }
}
