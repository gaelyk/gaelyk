/*
 * Copyright 2009 the original author or authors.
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
package groovyx.gaelyk.routes

import static com.google.appengine.api.capabilities.Capability.*
import static com.google.appengine.api.capabilities.CapabilityStatus.*
import static groovyx.gaelyk.TestUtil.request as r
import static groovyx.gaelyk.routes.CapabilityAwareDestination.CapabilityComparisonOperator.*

import com.google.appengine.api.capabilities.CapabilitiesService
import com.google.appengine.api.capabilities.Capability
import com.google.appengine.api.capabilities.CapabilityState
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper

/**
 * @author Guillaume Laforge
 */
class CapabilityAwareRoutesTest extends GroovyTestCase {

    // setup the local environement stub services
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig()
    )

    protected void setUp() {
        super.setUp()
        helper.setUp()
    }

    protected void tearDown() {
        helper.tearDown()
        super.tearDown()
    }

    void testClosureDestinationDefinitionShouldThrowExceptionWhenNoTo() {
        shouldFail {
            RoutingRule.buildRoutingRule {}
        }

        assert RoutingRule.buildRoutingRule { to "/default.gtpl" }.defaultDestination == "/default.gtpl"
    }

    void testClosureDestinationDefinition() {
        def rule = RoutingRule.buildRoutingRule {
            to "/default.gtpl"
            to("/maintenance.gtpl").on(DATASTORE).not(ENABLED)
            to("/readonly.gtpl").on(DATASTORE_WRITE).not(ENABLED)
        }

        assert rule.finalDestination == "/default.gtpl"

        assert rule.destinations.size() == 2

        assert rule.destinations[0].destination == "/maintenance.gtpl"
        assert rule.destinations[0].capability == DATASTORE
        assert rule.destinations[0].comparison == NOT
        assert rule.destinations[0].status == ENABLED
        assert rule.destinations[0].alternative

        assert rule.destinations[1].destination == "/readonly.gtpl"
        assert rule.destinations[1].capability == DATASTORE_WRITE
        assert rule.destinations[1].comparison == NOT
        assert rule.destinations[1].status == ENABLED
        assert rule.destinations[1].alternative
    }

    void testWithMockedCapabilityService() {
        def rule = RoutingRule.buildRoutingRule {
            to "/default.gtpl"
            to("/scheduled.gtpl") .on(DATASTORE_WRITE) .is(SCHEDULED_MAINTENANCE)
            to("/disabled.gtpl")  .on(DATASTORE)       .not(ENABLED)
            to("/readonly.gtpl")  .on(DATASTORE_WRITE) .not(ENABLED)
        }

        def states = [
                "/default.gtpl":    [(DATASTORE): ENABLED,   (DATASTORE_WRITE): ENABLED],
                "/disabled.gtpl":   [(DATASTORE): DISABLED,  (DATASTORE_WRITE): DISABLED],
                "/readonly.gtpl":   [(DATASTORE): ENABLED,   (DATASTORE_WRITE): DISABLED],
                "/scheduled.gtpl":  [(DATASTORE): ENABLED,   (DATASTORE_WRITE): SCHEDULED_MAINTENANCE],
        ]

        states.each { expectedDestination, capaStatus ->
            rule.service = [
                    getStatus: { Capability capa -> new CapabilityState(capa, capaStatus[capa], 0) }
            ] as CapabilitiesService

            assert rule.finalDestination == expectedDestination
        }
    }

    void testVariableReplacement() {
        def route = new Route("/foo/@bar", {
            to "/default.gtpl?var=@bar"
            to("/scheduled.gtpl?var=@bar").on(DATASTORE_WRITE).is(ENABLED)
        }, HttpMethod.ALL, RedirectionType.FORWARD, null, null, 0, false, false, false, 0)
        assert route.forUri("/foo/something",r("/foo/something")).destination == "/scheduled.gtpl?var=something"

        route = new Route("/foo/@bar", {
            to "/default.gtpl?var=@bar"
            to("/scheduled.gtpl?var=@bar").on(DATASTORE_WRITE).not(ENABLED)
        }, HttpMethod.ALL, RedirectionType.FORWARD, null, null, 0, false, false, false, 0)
        assert route.forUri("/foo/baz", r("/foo/baz")).destination == "/default.gtpl?var=baz"
    }
}
