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
package groovyx.gaelyk.plugins

import groovy.transform.CompileStatic
import groovyx.gaelyk.routes.RoutesBaseScript

/**
 * Base script class used for evaluating the plugin descriptors.
 *
 * @author Guillaume Laforge
 */
@CompileStatic
abstract class PluginBaseScript extends RoutesBaseScript {

    /** contributed binding variables */
    Map bindingVariables = [:]

    /** "before" request hook */
    Closure beforeAction = null

    /** "after" request hook */
    Closure afterAction = null

    /**
     * Inject new variables in the binding
     *
     * @param c closure containing the new variables to add to the binding
     */
    void binding(Closure c) {
        Closure clonedClosure = (Closure)c.clone()

        // puts the new binding variables into the map directly through closure delegation
        clonedClosure.delegate = bindingVariables
        clonedClosure.resolveStrategy = Closure.DELEGATE_FIRST

        clonedClosure()
    }

    /**
     * Define new routes
     *
     * @param c closure containing the new route definitions
     */
    void routes(Closure c) {
        // use the RoutesBaseScript class logic to define the rules
        c()
    }

    /**
     * Add a "before" action before the execution of the request
     *
     * @param c the closure action to execute
     */
    void before(Closure c) {
        beforeAction = c
    }

    /**
     * Add an "after" action after the execution of the request
     *
     * @param c the closure action to execute
     */
    void after(Closure c) {
        afterAction = c
    }

    /**
     * Returns the name of the script.
     * @return the name of the script
     */
    protected getPlugin(){
        getClass().simpleName
    }
}