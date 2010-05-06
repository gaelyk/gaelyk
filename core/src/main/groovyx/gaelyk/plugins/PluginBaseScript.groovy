/*
 * Copyright 2009-2010 the original author or authors.
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

import groovyx.gaelyk.routes.RoutesBaseScript

/**
 * Base script class used for evaluating the plugin descriptors.
 *
 * @author Guillaume Laforge
 */
abstract class PluginBaseScript extends RoutesBaseScript {

    /** contributed binding variables */
    Map bindingVariables = [:]

    /** contributed categories */
    List<Class> categories = []

    /**
     * Inject new variables in the binding
     *
     * @param c closure containing the new variables to add to the binding
     */
    void binding(Closure c) {
        Closure clonedClosure = c.clone()

        // puts the new binding variables into the map directly through closure delegation
        clonedClosure.delegate = bindingVariables
        clonedClosure.resolveStrategy = Closure.DELEGATE_ONLY
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
     * Install new categories
     *
     * @param cats vararg of categories to install
     */
    void categories(Class... cats) {
        categories = cats as List
    }
}