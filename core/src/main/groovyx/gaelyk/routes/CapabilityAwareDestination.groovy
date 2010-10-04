/*
 * Copyright 2009-2010 the original author or authors.
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

import com.google.appengine.api.capabilities.Capability
import com.google.appengine.api.capabilities.CapabilityStatus

/**
 * Route destination which is aware of the GAE capabilities and their status.
 *
 * @author Guillaume Laforge
 */
class CapabilityAwareDestination {
    enum CapabilityComparisonOperator { IS, NOT }

    String destination

    Capability capability
    CapabilityComparisonOperator comparison
    CapabilityStatus status

    /**
     * @return true if this sub-rule is the main route (not dependent on any capability status)
     */
    boolean isMainRoute()   { capability == null && comparison == null && status == null && destination }

    /**
     * @return true if it's not the main route (hence depends on a certain capability status)
     */
    boolean isAlternative() { !isMainRoute() }

    /**
     * @param capability a Capability instance
     * @param status a CapabilityStatus instance
     * @return a CapabilityAwareDestination instance
     */
    CapabilityAwareDestination is(CapabilityStatus status) {
        this.status = status
        this.comparison = CapabilityComparisonOperator.IS
        return this
    }

    /**
     * @param capability a Capability instance
     * @param status a CapabilityStatus instance
     * @return a CapabilityAwareDestination instance
     */
    CapabilityAwareDestination not(CapabilityStatus status) {
        this.status = status
        this.comparison = CapabilityComparisonOperator.NOT
        return this
    }

    String toString() {
        "[destination: $destination, capability: $capability, comparison: $comparison, status: $status, main route? ${isMainRoute()}"
    }
}
