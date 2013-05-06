
<a name="search"></a>
<h2>Search service support</h2>

<p>
    The full-text search functionality can be accessed with the <code>search</code> variable in the binding
    of Groovlets and templates. You can also specify a special namespace to restrict the searches to that namespace.
</p>

<pre class="brush:groovy">
    // access the search service
    search

    // access the search service for a specific namespace
    search['myNamespace']
</pre>

<p>
    You access a particular search index with the <code>index()</code> method,
    where you specify the index name and consistency mode by passing their values as parameters:
</p>

<pre class="brush:groovy">
    def index = search.index("books")
</pre>

<p>
    To add documents to an index, you call the <code>add()</code> method on the index,
    which takes a closure that accepts <code>document(map) {}</code> method calls.
    You can specify several documents in a single <code>add()</code> call,
    by simply making several <code>document()</code> calls inside the closure passed to <code>add()</code>.
</p>

<pre class="brush:groovy">
    def index = search.index("books")

    def response = index.add {
        document(id: "1234", locale: US, rank: 3) {
            title text: "Big bad wolf", locale: ENGLISH
            published date: new Date()
            numberOfCopies number: 35
            summary html: "<p>super story</p>", locale: ENGLISH
            description text: "a book for children"
            category atom: "children"
            category atom: "book"
            keyword text: ["wolf", "red hook"]
        }
        // other documents with other document(...) {} calls
    }
</pre>

<p>
    The named parameters passed to the <code>document()</code> methods can be <code>id</code>, <code>locale</code>
    and <code>rank</code>.
    Inside the closure, you can have as many field definitions of the form:
    <code>fieldName type: value</code> or with an optional locale:
    <code>fieldName type: value, locale: someLocale</code>.
    Fields can be repeated in order to have multi-valued document fields or you
    can specify map values as list: <code>fieldName type: [one, two]</code>.
    Empty lists and <code>null</code> values are ignored completely. Such fields
    are not added to the document.
</p>

<p>
    Once you have added documents to an index, you can search for them,
    and iterate over all the results:
</p>

<pre class="brush:groovy">
    // search the index
    def results = index.search("wolf")

    // iterate over all the resuts
    results.each { ScoredDocument doc ->
        assert doc.id == "1234"

        assert doc.title == "Big bad wolf"
        assert doc.numberOfCopies == 35
        assert doc.summary.contains("story")

        assert doc.keyword.size() == 2
        assert "wolf" in doc.keyword
        assert "red hook" in doc.keyword
    }
</pre>

<p>
    As you can see, you can access a document field with the Groovy property notation.
    When a field is multivalued, the <code>doc.field</code> property access actually returns a list of values.
</p>


<p>
    Because search API is sometimes too much faulty, you can specify number of retries used by <code>index.searchAsync</code>.
    Following code will attempt to search books related to wolfs three times before failing
</p>

<pre class="brush:groovy">
    def results = index.searchAsync("wolf", 3).get()
</pre>

<blockquote>
	<b>Note: </b> You can make any closure returning <a href="http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Future.html">Future</a>
	retrying by using <code>numberOfRetries * { future }</code> notation. For example <code>3 * { index.searchAsync("wolf") }</code>
	will behave the same way as described above. Keep the code inside the closure reasonable small because the closure
	is called at the beginning of each attempt.
</blockquote>

<a name="advanced_fulltext_search"></a>
<h3>Advanced Full Text Search</h3>

<p>
App Engine Search API is very unfriendly. It overuses builders in any possible way. 
Fortunately, <b>Gaelyk</b> provides a search DSL for simplifying the way you running full text queries.
The DSL is very close to the datastore query DSL:
</p>


<pre class="brush:groovy">
    def documents = search.search {
        select all from books
        sort desc by published, SearchApiLimits.MINIMUM_DATE_VALUE
        where title =~ params.title
        and keyword = params.keyword
        limit 10
    }
</pre>

<p>
The query DSL could be used with two methods on search service object: <code>search</code> and <code>searchAsync</code>.
Let's have a closer look at the syntax supported by the DSL:
</p>

<pre class="brush:groovy">
    // select the full document with all its fields
    select all
    // return just the ids of the documents matched by the query
    select ids
    // return just a few document's fields
    select name, age
    // return just a few expresions, see https://developers.google.com/appengine/docs/java/search/overview#Expressions
    // methods like distance or geopoint are also supported
    select numberOfCopies: numberOfCopies - 10, body: snippet(params.body, body), rating: max(rating, 10)


    // specify the index to search into
    from books

    // add a filter operation
    // operators allowed are: &lt;, &lt;=, ==, !=, &gt;, &gt;=, =~, ~
    // date values are properly handled for you
    where propertyName &lt;  expression
    where propertyName &lt;= expression
    where propertyName == expression
    where propertyName != expression
    where propertyName &gt;= expression
    where propertyName &gt;  expression
    
    // instead of query operator ':' use Groovy matches operator "=~""
    where propertyName =~ value
    
    // to search for singular and plural form of the word you can use "~" operator 
    where propertyName == ~value

    // you can use "and" instead of "where" to add more where clauses
    // to use logical disjunction ("or"), you can use "||" logical operator
    // you can use "&amp;&amp;" as well for "and"
    where propertyName == value || propertyName == other
    
    // you can also use built-in methods such as distance, geopoint, max, min, count
    where distance(geopoint(10, 50), locality) < 10

    // ascending sorting, the default value is mandatory
    sort asc  by propertyName, defaultValue
    // descending sorting, the default value is mandatory
    sort desc by propertyName, defaultValue
     
    // limit to only 10 results
    limit 10
    // return the results starting from a certain offset
    offset 100

    // cursor handling
    startAt cursorVariable
    startAt cursorWebSafeStringRepresentation
    
    // limits sorting
    limit sort to 1000
    
    // sets number found accuracy to 150
    number found accuracy 150
</pre>

<blockquote>
<b>Notes: </b>
<ul>
    <li>
        The expressions are actually mere strings, but you don't need to quote them.
    </li>
    <li>
        Also, for the <code>where</code> clause, be sure to put the property name on the left-hand-side of the comparison,
        and the compared value on the right-hand-side of the operator.
    </li>
    <li>
        When you need more than one <code>where</code> clause, you can use <code>and</code>
        which is a synonym of <code>where</code>.
    </li>
    <li>
        You can omit the <code>select</code> part of the query if you wish:
        by default, it will be equivalent to <code>select all</code>.
    </li>
    <li>
        It is possible to put all the verbs of the DSL on a single line (thanks to Groovy 1.8 command chains notation),
        or split across several lines as you see fit for readability or compactness.
    </li>
</ul>
</blockquote>

