package groovyx.gaelyk.plugins

import groovyx.grout.plugins.LazyBinding;

/**
 * Test for the lazy binding used by the plugin mechanism
 * 
 * @author Guillaume Laforge
 */
class LazyBindingTest extends GroovyTestCase {

    void testBinding() {
        def binding = new LazyBinding()

        binding.name = "Guillaume"
        binding.age = 33

        assert binding.name == "Guillaume"
        assert binding.age == 33

        assert binding.unknown == "unknown"
        assert binding.nonexistant == "nonexistant"
    }
}
