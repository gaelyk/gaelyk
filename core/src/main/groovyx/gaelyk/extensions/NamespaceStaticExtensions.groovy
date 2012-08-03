package groovyx.gaelyk.extensions

import groovy.transform.CompileStatic
import com.google.appengine.api.NamespaceManager

/**
 * Static methods for the NamespaceManager class
 */
@CompileStatic
class NamespaceStaticExtensions {

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
    static void of(NamespaceManager nm, String ns, Closure c) {
        def oldNs = NamespaceManager.get()
        NamespaceManager.set(ns)
        try {
            c()
        } finally {
            NamespaceManager.set(oldNs)
        }
    }

}
