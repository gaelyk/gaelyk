package groovyx.routes;

import groovy.lang.Binding;

/**
 * Interface for class enhancing routes script.
 * 
 * If your application or library wants to add additional binding to the routes
 * script, implement this interface and list it in
 * <code>META-INF/services/groovyx.routes.RoutesScriptBindingEnhancer</code>.
 * 
 * @author Vladimir Orany
 *
 */
public interface RoutesScriptBindingEnhancer {
    
    /**
     * Enhance routes script binding with aditional variables.
     * @param binding empty routes script binding
     */
    void enhance(Binding binding);

}
