package groovyx.gaelyk.routes;

import groovyx.grout.routes.RouteMatch;

/**
 * Route match with name space capabilities.
 * @author Vladimir Orany
 *
 */
public class RouteMatchWithNamespace extends RouteMatch {

    private final String namespace;
    
    /**
     * Creates new name space capable routes match.
     * @param original original routes match
     * @param namespace name space to be matched
     * @return new name space capable routes match
     */
    public static RouteMatch withNamespace(RouteMatch original, String namespace){
        return new RouteMatchWithNamespace(original, namespace);
    }
    
    /**
     * Creates new name space capable routes match.
     * @param match original routes match
     * @param namespace name space to be matched
     */
    protected RouteMatchWithNamespace(RouteMatch match, String namespace) {
        super(match);
        this.namespace = namespace;
    } 
    
    /**
     * @return namespace for this routes match
     */
    public String getNamespace() {
        return namespace;
    }

}
