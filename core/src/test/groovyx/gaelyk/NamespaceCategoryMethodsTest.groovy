package groovyx.gaelyk

import com.google.appengine.api.NamespaceManager
import com.google.appengine.tools.development.testing.LocalServiceTestHelper

/**
 * Test the category methods associated with the NamespaceManager class introduced in SDK 1.3.7.
 *
 * @author Guillaume Laforge
 */
class NamespaceCategoryMethodsTest extends GroovyTestCase {

    def namespace = NamespaceManager

    // setup the local environement so the NamespaceManager is initialized
    private LocalServiceTestHelper helper = new LocalServiceTestHelper()

    protected void setUp() {
        super.setUp()
        helper.setUp()
    }

    protected void tearDown() {
        super.tearDown()
        helper.tearDown()
    }

    void testNamespaceOf() {
        def oldNs = namespace.get()
        boolean executed = false

        namespace.of("customerA") {
            executed = true
            assert namespace.get() == "customerA"
        }
        assert executed

        def currentNs = namespace.get()
        // check that the original namespace is restored
        assert oldNs == currentNs
    }

    void testAnExceptionIsThrownWhenUsingCategoryMethodWithWrongClass() {
        shouldFail(MissingMethodException) {
            Integer.of("foo") {}
        }
    }

}
