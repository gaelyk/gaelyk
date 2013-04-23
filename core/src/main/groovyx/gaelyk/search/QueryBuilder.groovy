package groovyx.gaelyk.search

import com.google.appengine.api.search.Cursor
import com.google.appengine.api.search.FieldExpression
import com.google.appengine.api.search.GeoPoint
import com.google.appengine.api.search.Query
import com.google.appengine.api.search.QueryOptions
import com.google.appengine.api.search.SortExpression
import com.google.appengine.api.search.SortOptions
import com.google.appengine.api.search.SortExpression.SortDirection

class QueryBuilder {
    
    static enum SelectKeyword { 
        
        IDS {
            void handleQueryOptions(QueryOptions.Builder queryOptions){
                queryOptions.idsOnly = true
            }
        },
        ALL {
            void handleQueryOptions(QueryOptions.Builder queryOptions){
                queryOptions.idsOnly = false
            }
        },
        
        void handleQueryOptions(QueryOptions queryOptions){}
    }
    
    static enum FoundKeyword {
        FOUND
    }
    
    static enum SortKeyword {
        SORT
    }
    
    static class NumberAccuracyHandler {
        QueryBuilder self
        
        QueryBuilder accuracy(int accuracy){
            self.queryOptions.numberFoundAccuracy = accuracy
            self
        }
    }
    
    static class SortLimitHandler {
        QueryBuilder self
        
        QueryBuilder to(int limit){
            self.sortOptions.limit = limit
            self
        }
    }
    
    static class SortHandler {
        QueryBuilder self
        SortDirection direction
        
        QueryBuilder by(String expression, Object defaultValue){
            SortExpression.Builder exp = SortExpression.newBuilder()
            exp.expression = expression
            exp.direction = direction
            if(defaultValue instanceof Number){
                exp.defaultValueNumeric = defaultValue.doubleValue()
            } else if(defaultValue instanceof Date){
                exp.defaultValueDate = defaultValue
            } else {
                exp.defaultValue = "${defaultValue}"
            }
            self.sortOptions.addSortExpression(exp)
            self
        }
    }
    
    private Binding binding
    private String indexName
    private StringBuilder queryString = new StringBuilder()
    private QueryOptions.Builder queryOptions = QueryOptions.newBuilder()
    private SortOptions.Builder sortOptions = SortOptions.newBuilder()
    
    QueryBuilder from(String name){
        this.indexName = name
        this
    }
    
    QueryBuilder from(Class cls){
        this.indexName = cls.simpleName
        this
    }
    
    QueryBuilder select(SelectKeyword kwd){
        kwd.handleQueryOptions(queryOptions)
        this
    }
    
    QueryBuilder select(String ... fields){
        queryOptions.setFieldsToReturn(fields)
        this
    }
    
    QueryBuilder select(Map<String, Object> expressions){
        expressions.each { name, exp ->
            assert name
            assert exp
            queryOptions.addExpressionToReturn FieldExpression.newBuilder().setName(name).setExpression(exp).build()
        }
        this
    }

    SelectKeyword getAll(){
        SelectKeyword.ALL
    }
    
    SelectKeyword getIds(){
        SelectKeyword.IDS
    }
    
    FoundKeyword getFound(){
        FoundKeyword.FOUND
    }
    
    SortKeyword getSort(){
        SortKeyword.SORT
    }
    
    SortLimitHandler limit(SortKeyword sort){
        new SortLimitHandler(self:this)
    }
    
    QueryBuilder limit(int limit){
        queryOptions.limit = limit
        this
    }
    
    
    QueryBuilder offset(int offset){
        queryOptions.offset = offset
        this
    }
    
    SortHandler sort(SortDirection dir){
        new SortHandler(self: this, direction: dir)
    }
    
    NumberAccuracyHandler number(FoundKeyword found){
        new NumberAccuracyHandler(self:this)
    }
    
    QueryBuilder cursor(String cursor){
        cursor Cursor.newBuilder().build(cursor)
    }
    
    QueryBuilder cursor(Cursor cursor){
        queryOptions.cursor = cursor
        this
    }
    
    String geopoint(GeoPoint point){
        "geopoint($point.latitude, $point.longitude)"
    }
    
    String geopoint(Number latitude, Number longitude){
        "geopoint($latitude, $longitude)"
    }
    
    String count(String attribute){
        "count($attribute)"
    }
    
    String distance(String from, String to){
        "distance($from, $to)"
    }
    
    String get_rank() { "_rank" }
    
    String get_score() { "_score" }
    
    String max(String... vals){
        "max(${vals.join(', ')})"
    }
    
    String min(String... vals){
        "min(${vals.join(', ')})"
    }
    
    String snippet(String query, String field){
        "snippet(\"${query}\", $field)"
    }
    
    String snippet(String query, String field, int maxCharsPerSnippet, int maxNumSnippets){
        "snippet(\"${query}\", $field, $maxCharsPerSnippet, $maxNumSnippets)"
    }
    
    String snippet(String query, String field, int maxCharsPerSnippet, int maxNumSnippets, String separator){
        "snippet(\"${query}\", $field, $maxCharsPerSnippet, $maxNumSnippets, \"$separator\")"
    }
    
    SortDirection getAsc(){
        SortDirection.ASCENDING
    }
    
    SortDirection getDesc(){
        SortDirection.DESCENDING
    }
    
    def propertyMissing(String name) {
        if (binding && binding.variables.containsKey(name))
            return binding.variables[name]

        return name
    }
    
    String quote(String original){
        "\"$original\""
    }
    
    String group(String content){
        "($group)"
    }
    
    QueryBuilder where(String query){
        _appendQueryString('AND', query)
    }
    
    QueryBuilder _appendQueryString(String keyword, String query){
        if(!queryString){
            queryString << '(' << query << ')'
        } else {
            queryString << ' ' << keyword << ' (' << query << ')'
        }
        this
    }
    QueryBuilder _appendQueryString(String keyword, Map<String, Object> fields){
        if(!fields){
            return this
        }
        _appendQueryString(keyword, field.collect{ field, exp -> "$field: $exp" }.join(' '))
    }
    
    QueryBuilder OR(Map<String, Object> fields){
        _appendQueryString('OR', fields)
    }
    
    QueryBuilder OR(String query){
        _appendQueryString('OR', query)
    }
    
    QueryBuilder NOT(Map<String, Object> fields){
        _appendQueryString('NOT', fields)
    }
    
    QueryBuilder NOT(String query){
        _appendQueryString('NOT', query)
    }
    
    QueryBuilder AND(String query){
        where(query)
    }
    
    QueryBuilder AND(Map<String, Object> fields){
        where(fields)
    }
    
    Query build(){
        Query.Builder query = Query.newBuilder()
        queryOptions.sortOptions = sortOptions
        query.options = queryOptions
        query.build(queryString.toString())
    }
}
