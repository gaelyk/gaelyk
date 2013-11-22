package groovyx.gaelyk.search

import groovyx.gaelyk.RetryingFuture;
import groovyx.gaelyk.datastore.Ignore;

import java.util.concurrent.Future

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

import spock.lang.Specification
import spock.lang.Unroll

import com.google.appengine.api.search.Results
import com.google.appengine.tools.development.testing.LocalSearchServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper

class SearchDslAstTransformationSpec extends Specification {

    @Ignore
    def "Expressions are transformed to method call properly"(){
        TimeZone old = SearchQueryStringCategory.DATE_FORMAT.timeZone
        SearchQueryStringCategory.DATE_FORMAT.timeZone = TimeZone.getTimeZone(TimeZone.GMT_ID)
        
        QueryBuilder builder = evaluate 'prepare', """
        select one: one, two: rating + 10
        from Image
        where rating < 10
        and created >= Date.parse("yyyy-M-d", "1970-1-1")
        and ~"auto"
        and something =~ "a lovely day"
        and other =~ outer
        and distance(geopoint(10,20), location) < 10
        and ((a > 1 || b <= 2) && c == 10)
"""
        
        expect:
        builder.indexName                                       == 'Image'
        builder.queryString.toString()                          == """(rating < 10) AND (created >= 1970-1-1) AND (~"auto") AND (something: "a lovely day") AND (other: "I'm outside the closure") AND (distance(geopoint(10, 20), location) < 10) AND (((a > 1) OR (b <= 2)) AND (c = 10))"""
        builder.queryOptions.expressionsToReturn.size()         == 2
        builder.queryOptions.expressionsToReturn[0].name        == 'one'
        builder.queryOptions.expressionsToReturn[1].expression  == 'rating + 10'
        
        cleanup:
        SearchQueryStringCategory.DATE_FORMAT.timeZone = old
    }
    
    @Unroll
    def "Method #method returns #cls"(){
        def result = evaluate method, """
        select one: one, two: rating + 10
        from Image
        where rating < 10
"""
        expect:
        result in cls
        
        where:
        method          | cls
        'prepare'       | QueryBuilder
        'search'        | Results
        'search(5)'     | Results
        'searchAsync'   | Future
        'searchAsync(5)'| RetryingFuture
    }
    
    private evaluate(String method, String code){
        String init = """
        com.google.appengine.api.search.SearchService search = com.google.appengine.api.search.SearchServiceFactory.searchService  

        String outer = "I'm outside the closure"

        search.$method{ $code }"""
        
        newShell().evaluate(init)
    }
    
    private GroovyShell newShell() {
        CompilerConfiguration cc = new CompilerConfiguration()
        cc.addCompilationCustomizers(new ASTTransformationCustomizer(new SearchDslAstTransformation()))
        new GroovyShell(cc)
    }
    
    LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalSearchServiceTestConfig())
    
    def setup(){ helper.setUp() }
    def cleanup(){ helper.tearDown() }
}
