package groovyx.gaelyk.routes

import groovyx.gaelyk.ExpirationTimeCategory

/**
 * Test the ExpirationTimeCategory used in the URL routing DSL to define the caching times.
 *
 * @author Guillaume Laforge
 */
class ExpirationTimeCategoryTest extends GroovyTestCase {

    void testTimeDsl() {
        use(ExpirationTimeCategory) {
            assert  1.second  == 1
            assert 11.seconds == 11
            assert  1.minute  == 60
            assert 22.minutes == 1320
            assert  1.hour    == 3600
            assert 24.hours   == 3600 * 24

        }
    }
}
