package groovyx.gaelyk.search

import spock.lang.Specification
import spock.util.mop.Use

import java.text.SimpleDateFormat

class SearchQueryStringCategorySpec extends Specification {

    @Use(SearchQueryStringCategory)
    def "Methods for overloaded operators works as expected"(){
        def format = new SimpleDateFormat("yyyy-M-d")
        Date theBeginning = format.parse("1970-1-1")
        String refDate = groovyx.gaelyk.search.SearchQueryStringCategory.DATE_FORMAT.format(theBeginning)


        expect:
        
        'a'.greaterThan('b')                    == 'a > "b"'
        'a'.greaterThan(10)                     == 'a > 10'
        'a'.greaterThan(theBeginning)           == "a > $refDate"
        'a'.greaterThanEqual('b')               == 'a >= "b"'
        'a'.greaterThanEqual(10)                == 'a >= 10'
        'a'.greaterThanEqual(theBeginning)      == "a >= $refDate"
        'a'.lowerThan('b')                      == 'a < "b"'
        'a'.lowerThan(10)                       == 'a < 10'
        'a'.lowerThan(theBeginning)             == "a < $refDate"
        'a'.lowerThanEqual('b')                 == 'a <= "b"'
        'a'.lowerThanEqual(10)                  == 'a <= 10'
        'a'.lowerThanEqual(theBeginning)        == "a <= $refDate"
        'a'.and('b')                            == '(a) AND (b)'
        'a'.or('b')                             == '(a) OR (b)'
        'a'.isEqualTo('b')                      == 'a = "b"'
        'a'.isEqualTo(10)                       == 'a = 10'
        'a'.isEqualTo(theBeginning)             == "a = $refDate"
        'a'.isNotEqualTo('b')                   == 'NOT (a = "b")'
        'a'.isNotEqualTo(10)                    == 'NOT (a = 10)'
        'a'.isNotEqualTo(theBeginning)          == "NOT (a = $refDate)"
        'a'.isSameAs('b')                       == 'a: "b"'
        'a'.isSameAs(10)                        == 'a: 10'
        'a'.isSameAs(theBeginning)              == "a: $refDate"
        'a'.not()                               == 'NOT (a)'
        'a'.plus(10)                            == 'a + 10'
        'a'.plus(theBeginning)                  == "a + $refDate"
        'a'.minus('b')                          == 'a - "b"'
        'a'.minus(10)                           == 'a - 10'
        'a'.minus(theBeginning)                 == "a - $refDate"
        'a'.multiply('b')                       == 'a * "b"'
        'a'.multiply(10)                        == 'a * 10'
        'a'.multiply(theBeginning)              == "a * $refDate"
        'a'.div('b')                            == 'a / "b"'
        'a'.div(10)                             == 'a / 10'
        'a'.div(theBeginning)                   == "a / $refDate"
        'a'.bitwiseNegate()                     == '~"a"'
        '"a"'.bitwiseNegate()                   == '~"a"'

        'a'.plus('b')                           == 'a + "b"'
        
        
    }
    
}
