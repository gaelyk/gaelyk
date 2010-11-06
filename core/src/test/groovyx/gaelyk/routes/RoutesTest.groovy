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

/**
 * Tests for the routing support.
 *
 * @author Guillaume Laforge
 */
class RoutesTest extends GroovyTestCase {

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
            "/*":                               /\/.+/,
            "/*.*":                             /\/.+\..+/,
            "/company/about":                   /\/company\/about/,
            "/*/@from/*/@to":                   /\/.+\/(.+)\/.+\/(.+)/,
            "/say/@from/to/@to":                /\/say\/(.+)\/to\/(.+)/,
            "/script/@id":                      /\/script\/(.+)/,
            "/blog/@year/@month/@day/@title":   /\/blog\/(.+)\/(.+)\/(.+)\/(.+)/,
            "/author/@author":                  /\/author\/(.+)/,
            "/tag/@tag":                        /\/tag\/(.+)/,
            "/@file.@extension":                /\/(.+)\.(.+)/,
            "/**":                              /\/(?:.+\/?){0,}/,
            "/**/@file.@extension":             /\/(?:.+\/?){0,}\/(.+)\.(.+)/,
            "/**/@filename.*":                  /\/(?:.+\/?){0,}\/(.+)\..+/,
            "/**/*.*":                          /\/(?:.+\/?){0,}\/.+\..+/,
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
            "/company/about": ["/company/about": [:]],
            "/*/@from/*/@to": ["/groovy/glaforge/gaelyk/me": ['from': 'glaforge', 'to': 'me']],
        ]

        routeAndMatchingPaths.each { String route, Map urisVariables ->
            urisVariables.each { String uri, Map variables ->
                def r = new Route(route, "/destination")
                def result = r.forUri(uri)
                assert result.matches
                assert result.variables == variables
            }
        }
    }

    void testValidatorClosure() {
        def d = "/destination"
        def m = HttpMethod.GET
        def r = RedirectionType.FORWARD

        assert new Route("/blog/@year", d, m, r, { year.isNumber() }).forUri("/blog/2004").matches
        assert !new Route("/blog/@year", d, m, r, { year.isNumber() }).forUri("/blog/2004xxx").matches

        assert new Route("/isbn/@isbn/toc", d, m, r, { isbn ==~ /\d{9}(\d|X)/ }).forUri("/isbn/012345678X/toc").matches
        assert !new Route("/isbn/@isbn/toc", d, m, r, { isbn =~ /\d{9}(\d|X)/ }).forUri("/isbn/XYZ/toc").matches
    }

    void testIgnoreRoute() {
        new Route("/ignore", null, HttpMethod.ALL, RedirectionType.FORWARD, null, null, 0, true).forUri("/ignore").matches
    }
}
