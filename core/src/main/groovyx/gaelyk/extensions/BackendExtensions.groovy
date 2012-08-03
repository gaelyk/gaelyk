package groovyx.gaelyk.extensions

import groovy.transform.CompileStatic
import com.google.appengine.api.LifecycleManager
import com.google.appengine.api.backends.BackendService
import com.google.appengine.api.ThreadManager

/**
 * Backend service extension methods
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
    static void shutdownHook(LifecycleManager manager, Closure c) {
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
