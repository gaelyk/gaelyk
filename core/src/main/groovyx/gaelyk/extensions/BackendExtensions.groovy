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
package groovyx.gaelyk.extensions

import groovy.transform.CompileStatic
import com.google.appengine.api.LifecycleManager
import com.google.appengine.api.backends.BackendService
import com.google.appengine.api.ThreadManager

/**
 * Backend service extension methods
 *
 * @author Guillaume Laforge
 */
class BackendExtensions {

    /**
     * Shortcut to use closures as shutdown hooks.
     * <pre><code>
     *  lifecycle.shutdownHook = { ...shutdown logic... }
     * </code></pre>
     *
     * @param manager the lifecycle manager
     * @param c the closure as shutdown hook
     */
    static void shutdownHook(LifecycleManager manager, @DelegatesTo(LifecycleManager.ShutdownHook) Closure c) {
        manager.setShutdownHook(c as LifecycleManager.ShutdownHook)
    }

    /**
     * Runs code in the background thread.
     *
     * @param the code supposed to run in background thread
     */
    @CompileStatic
    static Thread run(BackendService backends, Runnable code){
        Thread thread = ThreadManager.createBackgroundThread(code);
        thread.start()
        thread
    }
}
