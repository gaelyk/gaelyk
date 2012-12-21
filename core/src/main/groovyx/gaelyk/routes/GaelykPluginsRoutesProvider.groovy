package groovyx.gaelyk.routes

import groovyx.gaelyk.plugins.PluginsHandler
import groovyx.grout.routes.RoutesProvider;

/**
 * Provides Gaelyk plugin routes.
 * @author Vladimir Orany
 */
class GaelykPluginsRoutesProvider implements RoutesProvider{

    List<groovyx.grout.routes.Route> getRoutes(){
        PluginsHandler.instance.routes
    }
    
}
