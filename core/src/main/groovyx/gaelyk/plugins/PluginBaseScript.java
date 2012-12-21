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
package groovyx.gaelyk.plugins;

import groovyx.grout.routes.RoutesBaseScript;


/**
 * Base script class used for evaluating the plugin descriptors.
 *
 * @author Guillaume Laforge
 */
public abstract class PluginBaseScript extends groovyx.grout.plugins.PluginBaseScript {

    
    @Override protected RoutesBaseScript createRoutesScript() {
        return new groovyx.gaelyk.routes.RoutesBaseScript() {
            @Override public Object run() {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }
    
}