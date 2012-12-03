package groovyx.gaelyk.routes

import groovyx.gaelyk.plugins.PluginsHandler
import groovyx.routes.RoutesProvider;

/**
 * Provides Gaelyk plugin routes.
 * @author Vladimir Orany
 */
class GaelykPluginsRoutesProvider implements RoutesProvider{

    List<groovyx.routes.Route> getRoutes(){
        PluginsHandler.instance.routes
    }
    
}
