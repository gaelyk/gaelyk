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

import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityStatus;

/**
 * Route destination which is aware of the GAE capabilities and their status.
 *
 * @author Guillaume Laforge
 * @author Vladimir Orany
 */
public class CapabilityAwareDestination {
    public static enum CapabilityComparisonOperator { IS, NOT }

    private String destination;

    private Capability capability;
    private CapabilityComparisonOperator comparison;
    private CapabilityStatus status;

    /**
     * @return true if this sub-rule is the main route (not dependent on any capability status)
     */
    public boolean isMainRoute()   { return capability == null && comparison == null && status == null && destination != null;}

    /**
     * @return true if it's not the main route (hence depends on a certain capability status)
     */
    public boolean isAlternative() { return !isMainRoute(); }

    /**
     * @param capability a Capability instance
     * @param status a CapabilityStatus instance
     * @return a CapabilityAwareDestination instance
     */
    public CapabilityAwareDestination is(CapabilityStatus status) {
        this.status = status;
        this.comparison = CapabilityComparisonOperator.IS;
        return this;
    }

    /**
     * @param capability a Capability instance
     * @param status a CapabilityStatus instance
     * @return a CapabilityAwareDestination instance
     */
    public CapabilityAwareDestination not(CapabilityStatus status) {
        this.status = status;
        this.comparison = CapabilityComparisonOperator.NOT;
        return this;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Capability getCapability() {
        return capability;
    }

    public void setCapability(Capability capability) {
        this.capability = capability;
    }

    public CapabilityComparisonOperator getComparison() {
        return comparison;
    }

    public void setComparison(CapabilityComparisonOperator comparison) {
        this.comparison = comparison;
    }

    public CapabilityStatus getStatus() {
        return status;
    }

    public void setStatus(CapabilityStatus status) {
        this.status = status;
    }
    
    
}
