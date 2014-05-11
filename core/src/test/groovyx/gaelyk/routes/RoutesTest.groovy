/*
 * Copyright 2009-2011 the original author or authors.
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

import static groovyx.gaelyk.TestUtil.request as r

import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig

/**
 * Tests for the routing support.
 *
 * @author Guillaume Laforge
 */
class RoutesTest extends GroovyTestCase {

    LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig())

    protected void setUp() {
        super.setUp()
        helper.setUp()
    }

    protected void tearDown() {
        super.tearDown()
        helper.tearDown()
    }

/** Tests the variable extraction logic */
    void testRoutesParameterExtraction() {
        def inputOutputExpected = [
            "/":                                [],
            "/*":                               [],
            "/**/*.*":                          [],
            "/*.*":                             [],
            "/**/@filename.*":                  ["@filename"],
            "/company/about":                   [],
            "/*/@from/*/@to":                   ["@from", "@to"],
            "/say/@from/to/@to":                ["@from", "@to"],
            "/script/@id":                      ["@id"],
            "/blog/@year/@month/@day/@title":   ["@year", "@month", "@day", "@title"],
            "/author/@author":                  ["@author"],
            "/tag/@tag":                        ["@tag"],
            "/@file.@extension":                ["@file", "@extension"],
            "/**/@file.@extension":             ["@file", "@extension"]
        ]

        inputOutputExpected.each { route, params ->
            assert Route.extractParameters(route) == params
        }
    }

    void testRegexRouteEquivalence() {
        def inputOutputExpected = [
            "/":                                /\//,
            "/*":                               /\/[^\/]+/,
            "/*.*":                             /\/[^\/]+\.[^\/]+/,
            "/company/about":                   /\/company\/about/,
            "/*/@from/*/@to":                   /\/[^\/]+\/(.+)\/[^\/]+\/(.+)/,
            "/say/@from/to/@to":                /\/say\/(.+)\/to\/(.+)/,
            "/script/@id":                      /\/script\/(.+)/,
            "/blog/@year/@month/@day/@title":   /\/blog\/(.+)\/(.+)\/(.+)\/(.+)/,
            "/author/@author":                  /\/author\/(.+)/,
            "/tag/@tag":                        /\/tag\/(.+)/,
            "/@file.@extension":                /\/(.+)\.(.+)/,
            "/**":                              /.*/,
            "/**/@file.@extension":             /\/(?:.+\/?){0,}\/(.+)\.(.+)/,
            "/**/@filename.*":                  /\/(?:.+\/?){0,}\/(.+)\.[^\/]+/,
            "/**/*.*":                          /.*/,
            "/**/*.groovy":                     /\/.*\.groovy$/,
        ]

        inputOutputExpected.each { route, regex ->
            assert Route.transformRouteIntoRegex(route) == regex
        }
    }

    void testRoutesAndVariableMatches() {
        def routeAndMatchingPaths = [
            "/blog/@year/@month/@day/@title":    [
                    "/blog/2009/11/27/Thanksgiving": ['year': '2009', 'month': '11', 'day': '27', 'title': 'Thanksgiving'],
                    "/blog/2008/03/04/birth": ['year': '2008', 'month': '03', 'day': '04', 'title': 'birth']
            ],
            "/**/@author/file/@file.@extension": [
                    "/foo/bar/glaforge/file/cv.doc": ['author': 'glaforge', 'file': 'cv', 'extension': 'doc']
            ],
            "/*.*": ["/cv.doc": [:]],
            "/*.*": ["/123-cv.doc": [:]],
            "/*/wildcard": ["/some/wildcard": [:]],
            "/company/about": ["/company/about": [:]],
            "/*/@from/*/@to": ["/groovy/glaforge/gaelyk/me": ['from': 'glaforge', 'to': 'me']],
        ]

        routeAndMatchingPaths.each { String route, Map urisVariables ->
            urisVariables.each { String uri, Map variables ->
                def rt = new Route(route, "/destination", HttpMethod.ALL, RedirectionType.FORWARD, null, null, 0, false, false, false, 0)
                def result = rt.forUri(uri, r(uri))
                assert result.matches
                assert result.variables == variables
            }
        }
    }

    void testNonMatchingRoutes() {
        def routeAndNonMatchingPaths = [
            "/somewhere": "/elswhere",
            "/*": "/something/not/matching"
        ]

        routeAndNonMatchingPaths.each { String route, String uri ->
            def rt = new Route(route, "/somewhere.groovy", HttpMethod.ALL, RedirectionType.FORWARD, null, null, 0, false, false, false, 0)
            assert !rt.forUri(uri, r(uri)).matches
        }
    }

    void testValidatorClosure() {
        def d = "/destination"
        def m = HttpMethod.GET
        def rt = RedirectionType.FORWARD

        assert new Route("/blog/@year", d, m, rt, { year.isNumber() }, null, 0, false, false, false, 0).forUri("/blog/2004",r("/blog/2004")).matches
        assert !new Route("/blog/@year", d, m, rt, { year.isNumber() }, null, 0, false, false, false, 0).forUri("/blog/2004xxx",r("/blog/2004xxx")).matches

        assert new Route("/isbn/@isbn/toc", d, m, rt, { isbn ==~ /\d{9}(\d|X)/ }, null, 0, false, false, false, 0).forUri("/isbn/012345678X/toc",r("/isbn/012345678X/toc")).matches
        assert !new Route("/isbn/@isbn/toc", d, m, rt, { isbn =~ /\d{9}(\d|X)/ }, null, 0, false, false, false, 0).forUri("/isbn/XYZ/toc",r("/isbn/XYZ/toc")).matches

        assert new Route("/admin", d, m, rt, { request.getAttribute('user') == 'USER' }, null, 0, false, false, false, 0).forUri("/admin",r("/admin")).matches
        assert !new Route("/admin", d, m, rt, { request.getAttribute('user') == 'dummy' }, null, 0, false, false, false, 0).forUri("/admin",r("/admin")).matches
    }

    void testIgnoreRoute() {
        assert new Route("/ignore", null, HttpMethod.ALL, RedirectionType.FORWARD, null, null, 0, true, false, false, 0).forUri("/ignore",r("/ignore")).matches
    }

    void testNamespacedRoute() {
        def route = new Route("/@cust/show", "/showCust.groovy?ns=@cust", HttpMethod.ALL, RedirectionType.FORWARD, null, { cust }, 0, false, false, false, 0).forUri("/acme/show",r("/acme/show"))

        assert route.matches
        assert route.namespace == "acme"
    }

    void testRoutesWithParametersAndJSessionID() {
        def rt = new Route("/signup-user", "/signupUser.groovy", HttpMethod.ALL, RedirectionType.FORWARD, null, null, 0, false, false, false, 0)
        
        assert rt.forUri("/signup-user", r("/signup-user")).matches
        assert rt.forUri("/signup-user?login=failed", r("/signup-user?login=failed")).matches
        assert rt.forUri("/signup-user;jsessionid=17o5jy7lz9t4t", r("/signup-user;jsessionid=17o5jy7lz9t4t")).matches
        assert rt.forUri("/signup-user;jsessionid=17o5jy7lz9t4t?login=failed", r("/signup-user;jsessionid=17o5jy7lz9t4t?login=failed")).matches
    }
}
