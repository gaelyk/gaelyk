package groovyx.gaelyk.routes

import groovyx.gaelyk.plugins.PluginsHandler

/**
 * This class helps keeping RoutesFilter written in Java.
 * @author Vladimir Orany
 */
class RoutesFilterHelper {

    /**
     * Wraps call to <code>PluginsHanlder.instance</code> singleton which
     * is not available in Java.
     * @return
     */
    static List<groovyx.routes.Route> getPluginRoutes(){
        PluginsHandler.instance.routes
    }
    
}
