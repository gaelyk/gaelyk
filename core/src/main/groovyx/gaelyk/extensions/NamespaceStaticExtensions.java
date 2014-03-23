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
package groovyx.gaelyk.extensions;

import groovy.lang.Closure;

import com.google.appengine.api.NamespaceManager;

/**
 * Static methods for the NamespaceManager class
 *
 * @author Guillaume Laforge
 */
public class NamespaceStaticExtensions {

    /**
     * Use a namespace in the context of the excution of the closure.
     * This method will save the original namespace and restore it afterwards.
     *
     * <pre><code>
     * namespace.of('test') { ... }
     * </code></pre>
     *
     * @param nm NamespaceManager class
     * @param ns the name of the namespace to use
     * @param c the code to execute under that namespace
     */
    public static void of(NamespaceManager nm, String ns, Closure<?> c) {
        String oldNs = NamespaceManager.get();
        NamespaceManager.set(ns);
        try {
            c.call();
        } finally {
            NamespaceManager.set(oldNs);
        }
    }

}
