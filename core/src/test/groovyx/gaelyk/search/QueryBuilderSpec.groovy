package groovyx.gaelyk.search

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll;

import com.google.appengine.api.search.GeoPoint
import com.google.appengine.api.search.SortExpression;

class QueryBuilderSpec extends Specification {

    def "Sets the index name"(){
        QueryBuilder builder = new QueryBuilder()
        
        when:
        builder.from 'IndexName'
        
        then:
        builder.indexName == 'IndexName'
        
        when:
        builder.from Image
        
        then:
        builder.indexName == 'Image'
    }
    
    def "Select ids only"(){
        QueryBuilder builder = new QueryBuilder()
        
        expect:
        !builder.queryOptions.idsOnly
        
        when:
        builder.with {
            select ids
        }
        
        then:
        builder.queryOptions.idsOnly
        
        when:
        builder.with {
            select all
        }
        
        then:
        !builder.queryOptions.idsOnly
    }
    
    def "Select fields"(){
        QueryBuilder builder = new QueryBuilder()
        
        when:
        builder.select 'a', 'b', 'c'
        
        then:
        builder.queryOptions.fieldsToReturn == ['a', 'b', 'c']
    }
    
    
    def "Number count accuracy"(){
        QueryBuilder builder = new QueryBuilder()
        
        when:
        builder.with {
            number found accuracy 150
        }
        
        then:
        builder.queryOptions.numberFoundAccuracy == 150
    }
    
    def "Select expressions"(){
        QueryBuilder builder = new QueryBuilder()
        
        when:
        builder.select one: 'a', two: 'snippet("query", b)'
        
        then:
        builder.queryOptions.expressionsToReturn.size() == 2
        builder.queryOptions.expressionsToReturn[0].name == 'one'
        builder.queryOptions.expressionsToReturn[0].expression == 'a'
        builder.queryOptions.expressionsToReturn[1].name == 'two'
        builder.queryOptions.expressionsToReturn[1].expression == 'snippet("query", b)'
    }
    
    def "Basic pagination options"(){
        QueryBuilder builder = new QueryBuilder()
        
        when:
        builder.limit 10
        
        then:
        builder.queryOptions.limit == 10
        
        when:
        builder.offset 15
        
        then:
        builder.queryOptions.offset == 15
    }
    
    @Ignore
    def "Cursor string handling"(){
        QueryBuilder builder = new QueryBuilder()
        com.google.appengine.api.search.Cursor cursor = com.google.appengine.api.search.Cursor.newBuilder().build()
        // this returns null, if you use other string it throws illegal argument ex
        String webSafe = cursor.toWebSafeString()
        
        when:
        builder.cursor webSafe
        
        then:
        builder.queryOptions.cursor.webSafeString == webSafe
    }
    
    def "Cursor direct handling"(){
        QueryBuilder builder = new QueryBuilder()
        com.google.appengine.api.search.Cursor cursor = com.google.appengine.api.search.Cursor.newBuilder().build()
        String webSafe = cursor.toWebSafeString()

        when:
        builder.cursor cursor
        
        then:
        builder.queryOptions.cursor.webSafeString == webSafe
    }
    
    def "Search query specific methods"(){
        QueryBuilder builder = new QueryBuilder()
        
        expect:
        builder.geopoint(new GeoPoint(15, 20))              == 'geopoint(15.0, 20.0)'
        builder.geopoint(1.5, 2.0)                          == 'geopoint(1.5, 2.0)'
        builder.distance('geopoint(15, 20)', 'location')    == 'distance(geopoint(15, 20), location)'
        builder._rank                                       == '_rank'
        builder._score                                      == '_score'
        builder.count('size')                               == 'count(size)'
        builder.max('15', '20')                             == 'max(15, 20)'
        builder.min('15', '20')                             == 'min(15, 20)'
        builder.snippet("query", 'body', 100, 10, ';')      == 'snippet("query", body, 100, 10, ";")'
        builder.snippet("query", 'body', 100, 10)           == 'snippet("query", body, 100, 10)'
        builder.snippet("query", 'body')                    == 'snippet("query", body)'
        
        builder.with {
            distance(geopoint(15, 20), location)
        } == 'distance(geopoint(15, 20), location)'
        
    }
    
    def "Limit sorting"(){
        QueryBuilder builder = new QueryBuilder()
        
        when:
        builder.with {
            limit sort to 1000
        }
        
        then:
        builder.sortOptions.limit == 1000
    }
    
    def "Add sort expressions"(){
        QueryBuilder builder = new QueryBuilder()
        
        when:
        builder.with {
            sort asc  by date, new Date(10)
            sort desc by rate, 1000
            sort desc by test, 'xyz'
        }
        
        then:
        builder.sortOptions.sortExpressions.size() == 3
        builder.sortOptions.sortExpressions[0].direction == SortExpression.SortDirection.ASCENDING
        builder.sortOptions.sortExpressions[0].expression == 'date'
        builder.sortOptions.sortExpressions[0].defaultValueDate == new Date(10)
        builder.sortOptions.sortExpressions[1].direction == SortExpression.SortDirection.DESCENDING
        builder.sortOptions.sortExpressions[1].expression == 'rate'
        builder.sortOptions.sortExpressions[1].defaultValueNumeric == 1000
        builder.sortOptions.sortExpressions[2].direction == SortExpression.SortDirection.DESCENDING
        builder.sortOptions.sortExpressions[2].expression == 'test'
        builder.sortOptions.sortExpressions[2].defaultValue == 'xyz'
    }
    
    @Unroll
    def "Where clause with #query"(){
        QueryBuilder builder = new QueryBuilder()
        
        when:
        builder.with closure
        
        then:
        builder.queryString.toString() == query
        
        where:
        query | closure
        '(test)' |
            { where test }
        '(test) AND (tset)' |
            { where test and tset }
    }
}

class Image {}
