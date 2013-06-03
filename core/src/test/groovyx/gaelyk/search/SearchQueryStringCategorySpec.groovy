package groovyx.gaelyk.search

import spock.lang.Specification
import spock.util.mop.Use

class SearchQueryStringCategorySpec extends Specification {

    @Use(SearchQueryStringCategory)
    def "Methods for overloaded operators works as expected"(){
        Date theBeginning = Date.parse("yyyy-M-d", "1970-1-1")
        
        expect:
        
        'a'.greaterThan('b')                    == 'a > "b"'
        'a'.greaterThan(10)                     == 'a > 10'
        'a'.greaterThan(theBeginning)           == 'a > 1970-1-1'
        'a'.greaterThanEqual('b')               == 'a >= "b"'
        'a'.greaterThanEqual(10)                == 'a >= 10'
        'a'.greaterThanEqual(theBeginning)      == 'a >= 1970-1-1'
        'a'.lowerThan('b')                      == 'a < "b"'
        'a'.lowerThan(10)                       == 'a < 10'
        'a'.lowerThan(theBeginning)             == 'a < 1970-1-1'
        'a'.lowerThanEqual('b')                 == 'a <= "b"'
        'a'.lowerThanEqual(10)                  == 'a <= 10'
        'a'.lowerThanEqual(theBeginning)        == 'a <= 1970-1-1'
        'a'.and('b')                            == '(a) AND (b)'
        'a'.or('b')                             == '(a) OR (b)'
        'a'.isEqualTo('b')                      == 'a = "b"'
        'a'.isEqualTo(10)                       == 'a = 10'
        'a'.isEqualTo(theBeginning)             == 'a = 1970-1-1'
        'a'.isNotEqualTo('b')                   == 'NOT (a = "b")'
        'a'.isNotEqualTo(10)                    == 'NOT (a = 10)'
        'a'.isNotEqualTo(theBeginning)          == 'NOT (a = 1970-1-1)'
        'a'.isSameAs('b')                       == 'a: "b"'
        'a'.isSameAs(10)                        == 'a: 10'
        'a'.isSameAs(theBeginning)              == 'a: 1970-1-1'
        'a'.not()                               == 'NOT (a)'
        'a'.plus('b')                           == 'a + "b"'
        'a'.plus(10)                            == 'a + 10'
        'a'.plus(theBeginning)                  == 'a + 1970-1-1'
        'a'.minus('b')                          == 'a - "b"'
        'a'.minus(10)                           == 'a - 10'
        'a'.minus(theBeginning)                 == 'a - 1970-1-1'
        'a'.multiply('b')                       == 'a * "b"'
        'a'.multiply(10)                        == 'a * 10'
        'a'.multiply(theBeginning)              == 'a * 1970-1-1'
        'a'.div('b')                            == 'a / "b"'
        'a'.div(10)                             == 'a / 10'
        'a'.div(theBeginning)                   == 'a / 1970-1-1'
        'a'.bitwiseNegate()                     == '~"a"'
        '"a"'.bitwiseNegate()                   == '~"a"'
        
        
    }
    
}
