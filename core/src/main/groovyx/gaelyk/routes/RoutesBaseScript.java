/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gaelyk.routes;

import groovy.lang.Closure;
import groovyx.routes.HttpMethod;
import groovyx.routes.RedirectionType;

import java.util.Map;

/**
 * Base script class used for evaluating the routes.
 * 
 * @author Guillaume Laforge
 * @author Vladimir Orany
 */
public abstract class RoutesBaseScript extends groovyx.routes.RoutesBaseScript {

    public void email  (Map<String, Object> m) {
        addRoute(new Route(new groovyx.routes.Route("/_ah/mail/*", createRoutingRule(m.get("to")), HttpMethod.POST,  RedirectionType.FORWARD, null, false), null, 0, true, false));
    }
    
    public void jabber(Map<String, Object> m){
        jabber(m, "chat");
    }

    public void jabber(Map<String, Object> m, String type) {
        if (type == "subscription") {
            addRoute(new Route(new groovyx.routes.Route("/_ah/xmpp/subscription/@value/", createRoutingRule(m.get("to") + "?value=@value"), HttpMethod.POST,  RedirectionType.FORWARD, null, false), null, 0, false, true));
        } else if (type == "presence") {
            addRoute(new Route(new groovyx.routes.Route("/_ah/xmpp/presence/@value/", createRoutingRule(m.get("to") + "?value=@value"), HttpMethod.POST,  RedirectionType.FORWARD, null, false), null, 0, false, true));
        } else {
            addRoute(new Route(new groovyx.routes.Route("/_ah/xmpp/message/chat/", createRoutingRule(m.get("to")), HttpMethod.POST,  RedirectionType.FORWARD, null, false), null, 0, false, true));
        }
    }
    
    @Override
    protected groovyx.routes.Route createRoute(Map<String, Object> m, String route, HttpMethod method) {
        return new Route(super.createRoute(m, route, method), (Closure<?>) m.get("namespace") , getCacheExpiration(m.get("cache")), false, false);
    }

    private int getCacheExpiration(Object object) {
        if(object == null || !(object instanceof Number)){
            return 0;
        }
        return ((Number)object).intValue();
    }
    
    @Override
    protected groovyx.routes.RoutingRule createRoutingRule(Object destination) {
        if(destination == null || destination instanceof String){
            return super.createRoutingRule(destination);
        }            
        return RoutingRuleBuilder.buildRoutingRule((Closure<?>) destination);
    }
}