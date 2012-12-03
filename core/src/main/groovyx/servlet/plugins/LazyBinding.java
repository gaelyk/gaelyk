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
package groovyx.servlet.plugins;

import groovy.lang.Binding;

/**
 * Implementation of a lazy binding which returns a String
 * representing the name of a variable that was not bound
 *
 * @author Guillaume Laforge
 */
class LazyBinding extends Binding {
    
    /**
     * @return the name of the variable if not present in the binding
     */
    public Object getVariable(String name) {
        try {
            return super.getVariable(name);
        } catch (Exception any) {
            return name;
        }
    }
}