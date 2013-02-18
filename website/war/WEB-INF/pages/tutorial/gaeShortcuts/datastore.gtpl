<a name="datastore"></a>
<h2>Improvements to the low-level datastore API</h2>

<p>
Although it's possible to use JDO and JPA in Google App Engine,
<b>Gaelyk</b> also lets you use the low-level raw API for accessing the datastore,
and makes the <code>Entity</code> class from that API a bit more Groovy-friendly.
</p>

<a name="entity"></a>
<h3>Using <code>Entity</code>s as maps or POJOs/POGOs</h3>

<blockquote>
    <b>Note: </b> POGO stands for Plain Old Groovy Object.
</blockquote>

<p>
When you use the <code>Entity</code> class from Java, you have to use methods like <code>setProperty()</code> or <code>getProperty()</code>
to access the properties of your <code>Entity</code>, making the code more verbose than it needs to be (at least in Java).
Ultimately, you would like to be able to use this class (and its instances) as if they were just like a simple map, or as a normal Java Bean.
That's what <b>Gaelyk</b> proposes by letting you use the subscript operator just like on maps, or a normal property notation.
The following example shows how you can access <code>Entity</code>s:
</p>

<pre class="brush:groovy">
    import com.google.appengine.api.datastore.Entity

    Entity entity = new Entity("person")

    // subscript notation, like when accessing a map
    entity['name'] = "Guillaume Laforge"
    println entity['name']

    // normal property access notation
    entity.age = 32
    println entity.age
</pre>

<blockquote>
    <b>Note: </b> For string properties, Google App Engine usually distinguishes between strings longer or shorter than 500 characters.
    Short strings are just mere Java strings, while longer strings (&gt;500 chars) should be wrapped in a <code>Text</code> instance.
    <b>Gaelyk</b> shields you from taking care of the difference, and instead, when using the two notations above,
    you just have to deal with mere Java strings, and don't need to use the <code>Text</code> class at all.
</blockquote>

<p>
Some properties of your entities can be unindexed, meaning that they can't be used for your search criteria.
This may be the case for long text properties, for example a bio of a person, etc.
The property notation or subscript notation use normal indexed properties.
If you want to set unindexed properties, you can use the <code>unindexed</code> shorcut:
</p>

<pre class="brush:groovy">
    import com.google.appengine.api.datastore.Entity

    Entity entity = new Entity("person")

    entity.name = "Guillaume Laforge"
    entity.unindexed.bio = "Groovy Project Manager..."
    entity.unindexed['address'] = "Very long address..."
</pre>

<p>
A handy mechanism exists to assign several properties at once, on your entities, using the <code>&lt;&lt;</code> (left shift) operator.
This is particularly useful when you have properties coming from the request, in the <code>params</code> map variable.
You can the do the following to assign all the key/values in the map as properties on your entity:
</p>

<pre class="brush:groovy">
    // the request parameters contain a firstname, lastname and age key/values:
    // params = [firstname: 'Guillaume', lastname: 'Laforge', title: 'Groovy Project Manager']

    Entity entity = new Entity("person")

    entity << params

    assert entity.lastname == 'Laforge'
    assert entity.firstname == 'Guillaume'
    assert entity.title == 'Groovy Project Manager'

    // you can also select only the key/value pairs you'd like to set on the entity
    // thanks to Groovy's subMap() method, which will create a new map with just the keys you want to keep
    entity << params.subMap(['firstname', 'lastname'])
</pre>

<blockquote>
<b>Note: </b> <b>Gaelyk</b> adds a few converter methods to ease the creation of instances
of some GAE SDK types that can be used as properties of entities, using the <code>as</code> operator:
<pre class="brush:groovy">
    "foobar@gmail.com" as Email
    "foobar@gmail.com" as JID

    "http://www.google.com" as Link
    new URL("http://gaelyk.appspot.com") as Link

    "+33612345678" as PhoneNumber
    "50 avenue de la Madeleine, Paris" as PostalAddress

    "groovy" as Category

    32 as Rating
    "32" as Rating

    "long text" as Text

    "some byte".getBytes() as Blob
    "some byte".getBytes() as ShortBlob

    "foobar" as BlobKey

    [45.32, 54.54] as GeoPt
</pre>
</blockquote>

<a name="conversion"></a>
<h3>Converting beans to entities and back</h3>

<p>
The mechanism explained above with type conversions (actually called "coercion") is also available
and can be handy for converting between a concrete bean and an entity.
Any POJO or POGO can thus be converted into an <code>Entity</code>,
and you can also convert an <code>Entity</code> to a POJO or POGO.
</p>

<pre class="brush:groovy">
    // given a POJO
    class Person {
        String name
        int age
    }

    def e1 = new Entity("Person")
    e1.name = "Guillaume"
    e1.age = 33

    // coerce an entity into a POJO
    def p1 = e1 as Person

    assert e1.name == p1.name
    assert e1.age == p1.age

    def p2 = new Person(name: "Guillaume", age: 33)
    // coerce a POJO into an entity
    def e2 = p2 as Entity

    assert p2.name == e2.name
    assert p2.age == e2.age
</pre>

<blockquote>
<b>Note: </b> The POJO/POGO class <code>simpleName</code> property is used as the entity kind.
So for example, if the <code>Person</code> class was in a package <code>com.foo</code>,
the entity kind used would be <code>Person</code>, not the fully-qualified name.
This is the same default strategy that <a href="http://code.google.com/p/objectify-appengine/">Objectify</a>
is using.
</blockquote>

<a name="pogo-entity-coercion-annotations"></a>

<p>
Further customization of the coercion can be achieved by using annotations on your classes:
</p>

<ul>
    <li><code>@Entity</code> to add CRUD methods to the POGO class and also to set all fields unindexed by default.</br>
    Following methods are added to the POGO instances:
        <ul>
            <li><code>save()</code> to save the object to the datastore</li>
            <li><code>delete()</code> to remove the object from the datastore</li>
        </ul>
    Following static methods are added to the POGO class:
        <ul>
            <li><code>get(nameOrId)</code> to retrieve the object from the datastore by its name or id or return <code>null</code> if entity not found</li>
            <li><code>delete(nameOrId)</code> to remove the object represented by its name or id from the datastore </li>
            <li><code>count()</code> to count all the object of given POGO class stored in the datastore</li>
            <li><code>count{...query...}</code> to count the objects which satisfies given <a href="query">query</a></li>
            <li><code>find{...query...}</code> to find single object which satisfies given <a href="query">query</a></li>
            <li><code>findAll()</code> to find all the object of given POGO class stored in the datastore</li>
            <li><code>findAll{...query...}</code> to find the objects which satisfies given <a href="query">query</a></li>
            <li><code>iterate()</code> to iterate over all the object of given POGO class stored in the datastore</li>
            <li><code>iterate{...query...}</code> to iterate over the objects which satisfies given <a href="query">query</a></li>            
        </ul>
    If there is no property annotated with <code>@Key</code> annotation it also adds <code>@Key long id</code> property to the POGO class.<br/>
    You can set default behavior from unindexed to indexed setting <code>unidexed</code> property of the annotations to <code>false</code>.
      
    </li>
    <li><code>@Key</code> to specify that a particular property or getter method should be used as the key for the entity (should be a <code>String</code> or a <code>long</code>)</li>
    <li><code>@Version</code> to specify that a particular property or getter method should be used as the unique autoincrement version for the entity (should be type <code>long</code>)</li>
    <li><code>@Indexed</code> for properties or getter methods that should be indexed (ie. can be used in queries)</li>
    <li><code>@Unindexed</code> for properties or getter methods that should be set as unindexed (ie. on which no queries can be done)</li>
    <li><code>@Ignore</code> for properties or getter methods that should be ignored and not persisted</li>
    <li><code>@Parent</code>to specify entity's parent entity</li>
</ul>

<p>
Here's an example of a <code>Person</code> bean using <code>@Entity</code> annotation, 
whose key is a string login, whose biography should be unindexed,
and whose full name can be ignored since it's a computed property:
</p>

<pre class="brush:groovy">
    import groovyx.gaelyk.datastore.Entity
    import groovyx.gaelyk.datastore.Key
    import groovyx.gaelyk.datastore.Unindexed
    import groovyx.gaelyk.datastore.Ignore
    
    @Entity(unindexed=false)
    class Person {
        @Key String login
        String firstName
        String lastName
        @Unindexed String bio
        @Ignore String getFullName() { "\$firstName \$lastName" }
    }
 </pre>
 
<p>
Thanks to <code>@Entity</code> annotation, 
you get basic CRUD operations for free:
</p>
 
 <pre class="brush:groovy">   
    assert Person.count()   == 0
    
    def glaforge = new Person(
        login:      'glaforge', 
        firstName:  'Guillaume', 
        lastName:   'Laforge',
        bio:        'Groovy Project Manager'
    ).save()
    
    assert Person.count()   == 1
    assert glaforge         == Person.get('glaforge')
    assert Person.findAll { where firstName == 'Guillaume' } == 1
    
    glaforge.delete()
    assert Person.count()   == 0
</pre>

<blockquote>
<b>Note: </b> In turn, with this feature, you have a lightweight object/entity mapper.
However, remember it's a simplistic solution for doing object/entity mapping,
and this solution doesn't take into accounts relationships and such.
If you're really interested in a fully featured mapper, you should have a look at
<a href="http://code.google.com/p/objectify-appengine/">Objectify</a>
or <a href="http://code.google.com/p/twig-persist/">Twig</a>.
</blockquote>

<a name="list-to-key-coercion"></a>
<h3>List to <code>Key</code> conversion</h3>

<p>
Another coercion mechanism that you can take advantage of, is to use a list to <code>Key</code> conversion,
instead of using the more verbose <code>KeyFactory.createKey()</code> methods:
</p>

<pre class="brush:groovy">
    [parentKey, 'address', 333] as Key
    [parentKey, 'address', 'name'] as Key
    ['address', 444] as Key
    ['address', 'name'] as Key
</pre>

<a name="save-delete"></a>
<h3>Added <code>save()</code> and <code>delete()</code> methods on <code>Entity</code></h3>

<p>
In the previous sub-section, we've created an <code>Entity</code>, but we need to store it in Google App Engine's datastore.
We may also wish to delete an <code>Entity</code> we would have retrieved from that datastore.
For doing so, in a <i>classical</i> way, you'd need to call the <code>save()</code> and <code>put()</code> methods
from the <code>DataService</code> instance.
However, <b>Gaelyk</b> dynamically adds a <code>save()</code> and <code>delete()</code> method on <code>Entity</code>:
</p>

<pre class="brush:groovy">
    def entity = new Entity("person")
    entity.name = "Guillaume Laforge"
    entity.age = 32

    entity.save()
</pre>

<p>
Afterwards, if you need to delete the <code>Entity</code> you're working on, you can simply call:
</p>

<pre class="brush:groovy">
    entity.delete()
</pre>

<a name="delete-get-on-key"></a>
<h3>Added <code>delete()</code> and <code>get()</code> method on <code>Key</code></h3>

<p>
Sometimes, you are dealing with keys, rather than dealing with entities directly &mdash;
the main reaons being often for performance sake, as you don't have to load the full entity.
If you want to delete an element in the datastore, when you just have the key, you can do so as follows:
</p>

<pre class="brush:groovy">
    someEntityKey.delete()
</pre>

<p>
Given a <code>Key</code>, you can get the associated entity with the <code>get()</code> method:
</p>

<pre class="brush:groovy">
    Entity e = someEntityKey.get()
</pre>

<p>
And if you have a list of entities, you can get them all at once:
</p>

<pre class="brush:groovy">
    def map = [key1, key2].get()

    // and then access the returned entity from the map:
    map[key1]
</pre>

<a name="key-string-conversion"></a>
<h3>Converting <code>Key</code> to an encoded <code>String</code> and vice-versa</h3>

<p>
When you want to store a <code>Key</code> as a string or pass it as a URL parameter,
you can use the <code>KeyFactory</code> methods to encode / decode keys and their string representations.
<b>Gaelyk</b> provides two convenient coercion mechanisms to get the encoded string representation of a key:
</p>

<pre class="brush:groovy">
    def key = ['addresses', 1234] as Key
    def encodedKey = key as String
</pre>

<p>And to retrieve the key from its encoded string representation:</p>

<pre class="brush:groovy">
    def encodedKey = params.personKey  // the encoded string representation of the key
    def key = encodedKey as Key
</pre>

<a name="transaction"></a>
<h3>Added <code>withTransaction()</code> method on the datastore service</h3>

<p>
Last but not least, if you want to work with transactions, instead of using the <code>beginTransaction()</code>
method of <code>DataService</code>, then the <code>commit()</code> and <code>rollback()</code> methods on that <code>Transaction</code>,
and doing the proper transaction handling yourself, you can use the <code>withTransaction()</code> method
that <b>Gaelyk</b> adds on <code>DataService</code> and which takes care of that boring task for you:
</p>

<pre class="brush:groovy">
    datastore.withTransaction {
        // do stuff with your entities within the transaction
    }
    // enable cross group transactions
    datastore.withTransaction(true) {
        // do stuff with more than one entity group
        new Entity('foo').save()
        new Entity('bar').save()
    }
</pre>

<p>
The <code>withTransaction()</code> method takes a closure as the two parameters.
First one is optional boolean value. If set to <code>true</code> 
<a href="https://developers.google.com/appengine/docs/java/datastore/overview#Cross_Group_Transactions"> 
cross group transactions</a> are enabled. The second parameter is closure 
and within that closure, upon its execution by <b>Gaelyk</b>, 
your code will be in the context of a transaction.
</p>

<a name="datastore-get"></a>
<h3>Added <code>get()</code> methods on the datastore service</h3>

<p>
To retrieve entities from the datastore, you can use the <code>datastore.get(someKey)</code> method,
and pass it a <code>Key</code> you'd have created with <code>KeyFactory.createKey(...)</code>:
this is a bit verbose, and <b>Gaelyk</b> proposes additional <code>get()</code> methods on the datastore service,
which do the key creation for you:
</p>

<pre class="brush:groovy">
    Key pk = ... // some parent key
    datastore.get(pk, 'address', 'home') // by parent key, kind and name
    datastore.get(pk, 'address', 1234)   // by parent key, kind and id

    datastore.get('animal', 'Felix')     // by kind and name
    datastore.get('animal', 2345)        // by kind and id
</pre>

<p>
This mechanism also works with the asynchronous datastore, as <b>Gaelyk</b> wraps the <code>Future&lt;Entity&gt;</code>
transparently, so you don't have to call <code>get()</code> on the future:
</p>

<pre class="brush:groovy">
    Key pk = ... // some parent key
    datastore.async.get(pk, 'address', 'home') // by parent key, kind and name
    datastore.async.get(pk, 'address', 1234)   // by parent key, kind and id

    datastore.async.get('animal', 'Felix')     // by kind and name
    datastore.async.get('animal', 2345)        // by kind and id
</pre>

<blockquote>
<b>Note: </b> When you have a <code>Future&lt;Entity&gt; f</code>,
when you call <code>f.someProperty</code>, <b>Gaelyk</b> will actually lazily call
<code>f.get().someProperty</code>, making the usage of the future transparent.
However, note it only works for properties, it doesn't work for method call on futures,
where you will have to call <code>get()</code> first.
This transparent handling of future properties is working for all <code>Future</code>s,
not just <code>Future&lt;Entity&gt;</code>.
</blockquote>

<a name="query"></a>
<h3>Querying</h3>

<p>
With the datastore API, to query the datastore, the usual approach is to create a <code>Query</code>,
prepare a <code>PreparedQuery</code>, and retrieve the results as a list or iterator.
Below you will see an example of queries used in the <a href="http://groovyconsole.appspot.com">Groovy Web Console</a>
to retrieve scripts written by a given author, sorted by descending date of creation:
</p>

<pre class="brush:groovy">
    import com.google.appengine.api.datastore.*
    import static com.google.appengine.api.datastore.FetchOptions.Builder.*

    // query the scripts stored in the datastore
    // "savedscript" corresponds to the entity table containing the scripts' text
    def query = new Query("savedscript")

    // sort results by descending order of the creation date
    query.addSort("dateCreated", Query.SortDirection.DESCENDING)

    // filters the entities so as to return only scripts by a certain author
    query.addFilter("author", Query.FilterOperator.EQUAL, params.author)

    PreparedQuery preparedQuery = datastore.prepare(query)

    // return only the first 10 results
    def entities = preparedQuery.asList( withLimit(10) )
</pre>

<p>
Fortunately, <b>Gaelyk</b> provides a query DSL for simplifying the way you can query the datastore.
Here's what it looks like with the query DSL:
</p>

<pre class="brush:groovy">
    def entities = datastore.execute {
        select all from savedscript
        sort desc by dateCreated
        where author == params.author
        limit 10
    }
</pre>

<p>
Let's have a closer look at the syntax supported by the DSL.
There are two methods added dynamically to the datastore: <code>query{}</code> and <code>execute{}</code>.
The former allow you to create a <code>Query</code> that you can use then to prepare a <code>PreparedQuery</code>.
The latter is going further as it executes the query to return a single entity, a list, a count, etc.
</p>

<h4>Creating queries</h4>

<p>
You can create a <code>Query</code> with the <code>datastore.query{}</code> method.
The closure argument passed to this method supports the verbs <code>select</code>, <code>from</code>, <code>where/and</code> and <code>sort</code>.
Here are the various options of those verbs:
</p>

<pre class="brush:groovy">
    // select the full entity with all its properties
    select all
    // return just the keys of the entities matched by the query
    select keys
    // return just a few properties (must be indexed)
    select name: String, age: Integer
    // return just a few properties (must be indexed) as RawValue
    select name, age

    // specify the entity kind to search into
    from entityKind

    // specify that entities searched should be child of another entity
    // represented by its key
    ancestor entityKey

    // add a filter operation
    // operators allowed are: &lt;, &lt;=, ==, !=, &gt;, &gt;=, in
    where propertyName &lt;  value
    where propertyName &lt;= value
    where propertyName == value
    where propertyName != value
    where propertyName &gt;= value
    where propertyName &gt;  value
    where propertyName in listOfValues

    // you can use "and" instead of "where" to add more where clauses

    // ascending sorting
    sort asc  by propertyName
    // descending sorting
    sort desc by propertyName
</pre>

<blockquote>
<b>Notes: </b>
<ul>
    <li>
        The entity kind of the <code>from</code> verb and the property name of the <code>where</code> verb
        and <code>sort/by</code> verbrs are actually mere strings, but you don't need to quote them.
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

<h4>Executing queries</h4>

<p>
You can use the <code>datastore.execute{}</code> call to execute the queries,
or the <code>datastore.iterate{}</code> call if you want to get the results in the form of an iterator.
The <code>select</code> verb also provides additional values.
The <code>from</code> verb allows to specify a class to coerce the results to a POGO.
In addition, you can specify the <code>FetchOptions</code> with additional verbs like:
<code>limit</code>, <code>offset</code>, <code>range</code>, <code>chunkSize</code>, <code>fetchSize</code>
<code>startAt</code>, <code>endAt</code>
</p>

<pre class="brush:groovy">
    // select the full entity with all its properties
    select all
    // return just the keys of the entities matched by the query
    select keys
    // return one single entity if the query really returns one single result
    select single
    // return the count of entities matched by the query
    select count
    // return just a few properties (must be indexed)
    select name: String, age: Integer
    // return just a few properties (must be indexed) as RawValue
    select name, age

    // from an entity kind
    from entityKind
    // specify the entity kind as well as a type to coerce the results to
    from entityKind as SomeClass

    // specify that entities searched should be child of another entity
    // represented by its key
    ancestor entityKey

    where propertyName &lt;  value
    where propertyName &lt;= value
    where propertyName == value
    where propertyName != value
    where propertyName &gt;= value
    where propertyName &gt;  value
    where propertyName in listOfValues

    // you can use "and" instead of "where" to add more where clauses

    // ascending sorting
    sort asc  by propertyName
    // descending sorting
    sort desc by propertyName

    // limit to only 10 results
    limit 10
    // return the results starting from a certain offset
    offset 100
    // range combines offset and limit together
    range 100..109

    // fetch and chunk sizes
    fetchSize 100
    chunkSize 100

    // cursor handling
    startAt cursorVariable
    startAt cursorWebSafeStringRepresentation
    endAt cursorVariable
    endAt cursorWebSafeStringRepresentation
</pre>

<blockquote>
<b>Notes: </b> If you use the <code>from addresses as Address</code> clause, specifying a class to coerce the results into,
if your <code>where</code> and <code>and</code> clauses use properties that are not present in the target class,
a <code>QuerySyntaxException</code> will be thrown.
</blockquote>

<p>
	For <code>select all</code> queries using <code>iterate</code> method nad for the <code>select all</code> queries using <code>execute</code> which are not coerced to POGO
	the methods return instance
	of <a href="https://developers.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/QueryResultIterator">QueryResultIterator</a> 
	or <a href="https://developers.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/QueryResultList">QueryResultList</a> from which <code>cursor</code>
	and <code>indexList</code> properites could be read.
</p>

<a name="async-datastore"></a>
<h3>Asynchronous datastore</h3>

<p>
In addition to the "synchronous" datastore service, the App Engine SDK also provides an
<a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/AsyncDatastoreService.html">AsynchrnousDatastoreService</a>.
You can retrieve the asynchronous service with the <code>datastore.async</code> shortcut.
</p>

<p>
<b>Gaelyk</b> adds a few methods on entities and keys that leverage the asynchronous service:
</p>

<ul>
    <li><code>entity.asyncSave()</code> returns a <code>Future&lt;Key&gt;</code></li>
    <li><code>entity.asyncDelete()</code> returns a <code>Future&lt;Void&gt;</code></li>
    <li><code>key.asyncDelete()</code> returns a <code>Future&lt;Void&gt;</code></li>
</ul>

<a name="metadata"></a>
<h3>Datastore metadata querying</h3>

<p>
The datastore contains some special entities representing useful
<a href="http://code.google.com/appengine/docs/java/datastore/metadataqueries.html">metadata</a>,
like the available kinds, namespaces and properties.
<b>Gaelyk</b> provides shortcuts to interrogate the datastore for such entity metadata.
</p>

<h4>Namespace querying</h4>

<pre class="brush:groovy">
    // retrieve the list of namespaces (as a List&lt;Entity&gt;)
    def namespaces = datastore.namespaces

    // access the string names of the namespaces
    def namespaceNames = namespaces.key.name

    // if you want only the first two
    datastore.getNamespaces(FetchOptions.Builder.withLimit(2))

    // if you want to apply further filtering on the underlying datastore query
    datastore.getNamespaces(FetchOptions.Builder.withLimit(2)) { Query query ->
        // apply further filtering on the query parameter
    }
</pre>

<h4>Kind querying</h4>

<pre class="brush:groovy">
    // retrieve the list of entity kinds (as a List&lt;Entity&gt;)
    def kinds = datastore.kinds

    // get only the string names
    def kindNames = kinds.key.name

    // get the first kind
    datastore.getKinds(FetchOptions.Builder.withLimit(10))

    // futher query filtering:
    datastore.getKinds(FetchOptions.Builder.withLimit(10)) { Query query ->
        // apply further filtering on the query parameter
    }
</pre>

<h4>Properties querying</h4>

<pre class="brush:groovy">
    // retrieve the list of entity properties (as a List&lt;Entity&gt;)
    def props = datastore.properties

    // as for namespaces and kinds, you can add further filtering
    datastore.getProperties(FetchOptions.Builder.withLimit(10)) { Query query ->
        // apply further filtering on the query parameter
    }

    // if you want to retrive the list of properties for a given entity kind,
    // for an entity Person, with two properties name and age:
    def entityKindProps = datastore.getProperties('Person')
    // lists of entity names
    assert entityKindProps.key.parent.name == ['Person', 'Person']
    // list of entity properties
    assert entityKindProps.key.name == ['name', 'age']
</pre>
