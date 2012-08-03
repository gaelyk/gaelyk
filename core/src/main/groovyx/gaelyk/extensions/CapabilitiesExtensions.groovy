package groovyx.gaelyk.extensions

import groovy.transform.CompileStatic
import com.google.appengine.api.capabilities.CapabilityStatus
import com.google.appengine.api.capabilities.CapabilitiesService
import com.google.appengine.api.capabilities.Capability

/**
 * Capabilities method extensions
 */
@CompileStatic
class CapabilitiesExtensions {

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
    static CapabilityStatus getAt(CapabilitiesService capabilities, Capability capa) {
        return capabilities.getStatus(capa).getStatus()
    }

    /**
     * Coerces a capability status into a boolean.
     * This mechanism is used by the "Groovy Truth".
     *
     * @return true if the capability status is ENABLED, otherwise false.
     */
    static boolean asBoolean(CapabilityStatus capabilityStatus) {
        capabilityStatus == CapabilityStatus.ENABLED || capabilityStatus == CapabilityStatus.SCHEDULED_MAINTENANCE
    }
}
