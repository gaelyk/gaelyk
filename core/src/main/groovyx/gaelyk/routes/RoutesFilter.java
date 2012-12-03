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

import groovyx.gaelyk.cache.CacheHandler;
import groovyx.routes.RouteMatch;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.utils.SystemProperty;

/**
 * <code>RoutesFilter</code> is a Servlet Filter whose responsability is to define URL mappings for your
 * Gaelyk application. When the servlet filter is configured, a file named <code>routes.groovy</code>
 * will be loaded by the filter, defining the various routes a web request may follow.
 * <p>
 * It is possible to customize the location of the routes definition file by using the
 * <code>routes.location</code> init parameter in the declaration of the filter in <code>web.xml</code>.
 * <p>
 * In development mode, routes will be reloaded automatically on each request, but when the application
 * is deployed on the Google cloud, all the routes will be set in stone.
 *
 * @author Guillaume Laforge
 */
public class RoutesFilter extends groovyx.routes.RoutesFilter {

    // this class is nearly compilable as java file, only PluginsHandler
    // instance is not recognized for some reason

    @Override
    protected String getDefaultLoggerName() {
        return "gaelyk.routesfilter";
    }
    
    @Override
    protected boolean isHotReloadEnabled() {
        return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
    }
    
    @Override
    protected String getDefaultRoutesScriptBaseName() {
        return RoutesBaseScript.class.getName();
    }
    
    @Override
    protected void handleForward(final RouteMatch result, final groovyx.routes.Route route, 
            final HttpServletRequest request, final HttpServletResponse response) 
                    throws ServletException, IOException {
        if(!(route instanceof Route)){
            super.handleForward(result, route, request, response);
            return;
        }
        if (!(result instanceof RouteMatchWithNamespace)) {
            CacheHandler.serve((Route)route, request, response);
            return;
        }
        RouteMatchWithNamespace nsr = (RouteMatchWithNamespace) result;

        if (nsr.getNamespace() == null) {
            CacheHandler.serve((Route)route, request, response);
            return;
        }
        String oldNs = NamespaceManager.get();
        NamespaceManager.set(nsr.getNamespace());
        try {
            CacheHandler.serve((Route)route, request, response);
        } finally {
            NamespaceManager.set(oldNs);
        }
    }
}
