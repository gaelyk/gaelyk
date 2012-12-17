package groovyx.gaelyk.routes

import spock.lang.Specification
import spock.lang.Unroll

class OptionalRoutesHelperSpec extends Specification {
    
    @Unroll
    def "For #route and #destination routes #routes should be generated"(){
        def sorted = routes.entrySet().asList()
        when:
        def generated = OptionalRoutesHelper.generateRoutes(route, destination).entrySet().asList()
        then:
        generated == sorted
        
        where:
        route                               | destination               | routes
        "/"                                 | "/index.groovy"           | [
            "/":                        "/index.groovy" 
        ]
        "/@anything?"                       | "/index.groovy"           | [
            "/@anything":               "/index.groovy?anything=@anything", 
            "/":                        "/index.groovy"
        ]
        "/@anything?"                       | "/index.groovy?foo=bar"   | [
            "/@anything":               "/index.groovy?foo=bar&anything=@anything", 
            "/":                        "/index.groovy?foo=bar"
        ]
        "/@anything?/"                      | "/index.groovy?foo=bar"   | [
            "/@anything/":              "/index.groovy?foo=bar&anything=@anything", 
            "/":                        "/index.groovy?foo=bar"
        ]
        "/@anything?/@something?"           | "/index.groovy"           | [
            "/@anything/@something":    "/index.groovy?something=@something&anything=@anything", 
            "/@anything":               "/index.groovy?anything=@anything", 
            "/":                        "/index.groovy"
        ]
        "/@anything?/@foo"                  | "/index.groovy?foo=@foo"  | [
            "/@anything/@foo": "/index.groovy?foo=@foo&anything=@anything", 
            "/@foo": "/index.groovy?foo=@foo"
        ]
        "/@anything?/bar-@foo?"             | "/index.groovy"           | [
            "/@anything/bar-@foo":      "/index.groovy?foo=@foo&anything=@anything", 
            "/bar-@foo":                "/index.groovy?foo=@foo", 
            "/@anything":               "/index.groovy?anything=@anything", 
            "/":                        "/index.groovy"
        ]
        "/@anything?/bar-@foo?/foo-@bar?"   | "/index.groovy"           | [
            "/@anything/bar-@foo/foo-@bar": "/index.groovy?bar=@bar&foo=@foo&anything=@anything",
            "/@anything/bar-@foo/foo-@bar": "/index.groovy?bar=@bar&foo=@foo&anything=@anything",
            "/bar-@foo/foo-@bar":           "/index.groovy?bar=@bar&foo=@foo",
            "/@anything/foo-@bar":          "/index.groovy?bar=@bar&anything=@anything",
            "/foo-@bar":                    "/index.groovy?bar=@bar",
            "/@anything/bar-@foo":          "/index.groovy?foo=@foo&anything=@anything",
            "/bar-@foo":                    "/index.groovy?foo=@foo", 
            "/@anything":                   "/index.groovy?anything=@anything", 
            "/":                            "/index.groovy"
        ]
        "/@a?/@b?/@c?/foo-@bar?"            | "/index.groovy"           | [
            "/@a/@b/@c/foo-@bar":       "/index.groovy?bar=@bar&c=@c&b=@b&a=@a",
            "/@a/@b/foo-@bar":          "/index.groovy?bar=@bar&b=@b&a=@a",
            "/@a/foo-@bar":             "/index.groovy?bar=@bar&a=@a",
            "/foo-@bar":                "/index.groovy?bar=@bar",
            "/@a/@b/@c":                "/index.groovy?c=@c&b=@b&a=@a",
            "/@a/@b":                   "/index.groovy?b=@b&a=@a",
            "/@a":                      "/index.groovy?a=@a",
            "/":                        "/index.groovy",
        ]
    }

}
