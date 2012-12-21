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
package groovyx.grout.plugins;

import groovy.lang.Closure;
import groovy.lang.Script;
import groovyx.grout.routes.Route;
import groovyx.grout.routes.RoutesBaseScript;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base script class used for evaluating the plugin descriptors.
 *
 * @author Guillaume Laforge
 */
public abstract class PluginBaseScript extends Script {

    /** contributed binding variables */
    private Map<String, Object> bindingVariables = new LinkedHashMap<String, Object>();

    /** "before" request hook */
    private Closure<?> beforeAction = null;

    /** "after" request hook */
    private Closure<?> afterAction = null;
    
    private RoutesBaseScript routesScript = createRoutesScript();

    /**
     * Inject new variables in the binding
     *
     * @param c closure containing the new variables to add to the binding
     */
    public void binding(Closure<?> c) {
        Closure<?> clonedClosure = (Closure<?>)c.clone();

        // puts the new binding variables into the map directly through closure delegation
        clonedClosure.setDelegate(bindingVariables);
        clonedClosure.setResolveStrategy(Closure.DELEGATE_FIRST);

        clonedClosure.call();
    }

    /**
     * Creates new routes script used as delgate in routes configuration method.
     * @return new routes script used as delgate in routes configuration method.
     */
    protected RoutesBaseScript createRoutesScript() {
        return new RoutesBaseScript() {
            @Override public Object run() { return null; }
        };
    }

    /**
     * Define new routes
     *
     * @param c closure containing the new route definitions
     */
    public void routes(Closure<?> c) {
        // use the RoutesBaseScript class logic to define the rules
        Closure<?> clonedClosure = (Closure<?>)c.clone();

        // puts the new binding variables into the map directly through closure delegation
        clonedClosure.setDelegate(routesScript);
        clonedClosure.setResolveStrategy(Closure.DELEGATE_FIRST);

        clonedClosure.call();
    }

    /**
     * Add a "before" action before the execution of the request
     *
     * @param c the closure action to execute
     */
    public void before(Closure<?> c) {
        beforeAction = c;
    }

    /**
     * Add an "after" action after the execution of the request
     *
     * @param c the closure action to execute
     */
    public void after(Closure<?> c) {
        afterAction = c;
    }

    /**
     * Returns the name of the script.
     * @return the name of the script
     */
    protected String getPlugin(){
        return getClass().getSimpleName();
    }
    
    /** contributed binding variables */
    public Map<String, Object> getBindingVariables() {
        return bindingVariables;
    }
    
    /** "before" request hook */
    public Closure<?> getAfterAction() {
        return afterAction;
    }
    
    /** @return "after" request hook */
    public Closure<?> getBeforeAction() {
        return beforeAction;
    }
    
    /**
     * List of routes created in {@link #routes(Closure)} configuration method.
     * @return list of routes created in {@link #routes(Closure)} configuration method
     */
    public List<Route> getRoutes(){
        return routesScript.getRoutes();
    }
}