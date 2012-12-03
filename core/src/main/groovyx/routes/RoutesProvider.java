package groovyx.routes;

import java.util.List;

/**
 * Interface for class providing additional routes.
 * 
 * If your application or library wants to provide additional routes
 * for {@link RoutesFilter}, implement this interface and list it in
 * <code>META-INF/services/groovyx.routes.RoutesProvider</code>.
 * 
 * @author Vladimir Orany
 *
 */
public interface RoutesProvider {
    
    /**
     * @return list of routes which should be added to one loaded from
     * routes file
     */
    List<Route> getRoutes();

}
