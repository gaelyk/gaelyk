package groovyx.gaelyk.routes;

import groovy.lang.Binding;
import groovyx.gaelyk.GaelykBindingEnhancer;
import groovyx.routes.RoutesScriptBindingEnhancer;

/**
 * Enhances routes script with Gaelyk specific binding.
 * @author Vladimir
 *
 */
public class GaelykRoutesEnhancer implements RoutesScriptBindingEnhancer {

    public void enhance(Binding binding){
        GaelykBindingEnhancer.bind(binding);

        // adds three nouns for the XMPP support
        binding.setVariable("chat",         "chat");
        binding.setVariable("presence",     "presence");
        binding.setVariable("subscription", "subscription");
    }
    
}
