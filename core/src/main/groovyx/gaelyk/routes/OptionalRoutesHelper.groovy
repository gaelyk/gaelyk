package groovyx.gaelyk.routes

import java.util.Map.Entry;

class OptionalRoutesHelper {

    static Map<String, String> generateRoutes(String route, String destination){
        if(!route || !destination){
            return [:]
        }
        def optionalPathParams = route.findAll("[^/]*@\\w*\\?").reverse()

        if(!optionalPathParams){
            return [(route): destination]
        }

        Map<String, String> routes = [:]
        String nextRoute = route

        for(String param in optionalPathParams){
            String currentRoute = nextRoute

            String currentDestination = destination.contains('?') ? (destination + '&') : (destination + '?')
            List<String> queryParts = []
            for(String p in optionalPathParams){
                String bareParam = p.find("@\\w*\\?")
                if(currentRoute.contains(p)){
                     queryParts << "${bareParam[1..-2]}=${bareParam[0..-2]}"
                }
            }
            
            if(!param.startsWith('@')){
                String bareParam = param.find("@\\w*\\?")
                routes.putAll(generateRoutes(currentRoute.replace(param, param[0..-2]), currentDestination + "${bareParam[1..-2]}=${bareParam[0..-2]}"))
            }
            currentDestination += queryParts.join('&')
            
            for(String p in optionalPathParams){
                String replacement = p[0..-2]
                currentRoute = currentRoute.replace p, replacement
            }
            
            routes[currentRoute] = currentDestination
            
            nextRoute = nextRoute.replace(param, '')
            if(route.endsWith('/')){
                nextRoute += '/'
            } else if(nextRoute.endsWith('/') && nextRoute.size() > 1){
                nextRoute = nextRoute[0..-2]
            }
            nextRoute = nextRoute.replaceAll("/+", "/")
        }
        
        routes[nextRoute] = destination
        routes
    }
}
