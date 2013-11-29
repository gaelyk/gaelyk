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
package groovyx.gaelyk.extensions;

import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.Capability;

/**
 * Capabilities method extensions
 *
 * @author Guillaume Laforge
 */
public class CapabilitiesExtensions {

    /**
     * Query the status of the various App Engine services.
     *
     * <pre><code>
     * import static com.google.appengine.api.capabilities.Capability.*
     * import static com.google.appengine.api.capabilities.CapabilityStatus.*
     *
     * capabilities[DATASTORE] == ENABLED
     * </code></pre>
     *
     * @param capa the capability to know the status of
     * @return a status
     */
    public static CapabilityStatus getAt(CapabilitiesService capabilities, Capability capa) {
        return capabilities.getStatus(capa).getStatus();
    }

    /**
     * Coerces a capability status into a boolean.
     * This mechanism is used by the "Groovy Truth".
     *
     * @return true if the capability status is ENABLED, otherwise false.
     */
    public static boolean asBoolean(CapabilityStatus capabilityStatus) {
        return capabilityStatus == CapabilityStatus.ENABLED || capabilityStatus == CapabilityStatus.SCHEDULED_MAINTENANCE;
    }
}
