package groovyx.routes;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Result of matching route against URI.
 * 
 * @author Vladimir Orany
 * @see Route#forUri(javax.servlet.http.HttpServletRequest)
 */
public class RouteMatch {
    
    /**
     * Result of non-matching route.
     */
    public static final RouteMatch NO_MATCH = new RouteMatch(false, null , new LinkedHashMap<String, String>());
    
    private final boolean matches;
    private final Map<String, String> variables;
    private final String destination;
    
    /**
     * @return non-matching result.
     */
    public static RouteMatch noMatch(){
        return NO_MATCH;
    }
    
    /**
     * @param destination final destination of given route
     * @return route match routing to given destination
     */
    public static RouteMatch to(String destination){
        return new RouteMatch(true, destination , new LinkedHashMap<String, String>());
    }
    
    
    /**
     * 
     * @param destination final destination of given route
     * @param variables values for path variables
     * @return route match routing to given destination and variables
     */
    public static RouteMatch to(String destination, Map<String, String> variables){
        return new RouteMatch(true, destination , variables);
    }
    
    /**
     * New routes match for given parameters.
     * @param matches <code>true</code> if the route matchers the pattern
     * @param destination final destination of given route
     * @param variables values for path variables
     */
    protected RouteMatch(boolean matches, String destination, Map<String, String> variables) {
        this.matches = matches;
        this.variables = Collections.unmodifiableMap(variables);
        this.destination = destination;
    }
    
    /**
     * Clone constructor.
     * @param match original match
     */
    protected RouteMatch(RouteMatch match){
       this(match.matches, match.destination, match.variables); 
    }

    
    /**
     * @return <code>true</code> if the route matches.
     */
    public boolean isMatches() {
        return matches;
    }
    
    /**
     * @return values for path variables
     */
    public Map<String, String> getVariables() {
        return variables;
    }
    
    /**
     * @return final destination of this route if matcher, <code>null</code> otherwise.
     */
    public String getDestination() {
        return destination;
    }
    
    /**
     * @return true if the route matched given URI
     */
    public boolean asBoolean(){
        return matches;
    }

}
