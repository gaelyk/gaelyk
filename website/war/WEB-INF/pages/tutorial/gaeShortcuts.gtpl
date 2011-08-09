<title>Google App Engine specific shortcuts</title>

<h1>Google App Engine specific shortcuts</h1>

<p>
In addition to providing direct access to the App Engine services, <b>Gaelyk</b> also adds some syntax sugar on top of these APIs.
Let's review some of these improvements.
</p>

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
<b>Note: </b> The POJO/POGO class simpleName property is used as the entity kind.
So for example, if the <code>Person</code> class was in a package <code>com.foo</code>,
the entity kind used would be <code>Person</code>, not the fully-qualified name.
This is the same default strategy that <a href="http://code.google.com/p/objectify-appengine/">Objectify</a>
is using.
</blockquote>

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
</pre>

<p>
The <code>withTransaction()</code> method takes a closure as the sole parameter,
and within that closure, upon its execution by <b>Gaelyk</b>, your code will be in the context of a transaction.
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
However, note it longy works for properties, it doesn't work for method call on futures,
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
You can use the <code>datastore.execute{}</code> call to execute the queries.
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

<a name="task-queue"></a>
<h2>The task queue API shortcuts</h2>

<p>
Google App Engine SDK provides support for "task queues".
An application has a default queue, but other queues can be added through the configuration of a
<code>queue.xml</code> file in <code>/WEB-INF</code>.
</p>

<blockquote>
<b>Note: </b> You can learn more about <a href="http://code.google.com/appengine/docs/java/config/queue.html">queues</a>
and <a href="http://code.google.com/appengine/docs/java/taskqueue/overview.html">task queues</a>, and how to
configure them on the online documentation.
</blockquote>

<p>
In your Groovlets and templates, you can access the default queue directly, as it is passed into the binding:
</p>

<pre class="brush:groovy">
    // access the default queue
    defaultQueue
</pre>

<p>
You can access the queues either using a subscript notation or the property access notation:
</p>

<pre class="brush:groovy">
    // access a configured queue named "dailyEmailQueue" using the subscript notation
    queues['dailyEmailQueue']

    // or using the property access notation
    queues.dailyEmailQueue

    // you can also access the default queue with:
    queues.default
</pre>

<p>
To get the name of a queue, you can call the provided <code>getQueueName()</code> method,
but <b>Gaelyk</b> provides also a <code>getName()</code> method on
<a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/labs/taskqueue/Queue.html">Queue</a>
so that you can write <code>queue.name</code>, instead of the more verbose <code>queue.getQueueName()</code> or
<code>queue.queueName</code>, thus avoid repetition of queue.
</p>

<p>
For creating tasks and submitting them on a queue, with the SDK you have to use the
<code><a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/labs/taskqueue/TaskOptions.Builder.html">TaskOptions.Builder</a></code>.
In addition to this builder approach, <b>Gaelyk</b> provides a shortcut notation for adding tasks to the queue using named arguments:
</p>

<pre class="brush:groovy">
    // add a task to the queue
    queue.add countdownMillis: 1000, url: "/task/dailyEmail",
        taskName: "dailyNewsletter",
        method: 'PUT', params: [date: '20101214'],
        payload: content, retryOptions: RetryOptions.Builder.withDefaults()
</pre>

<p>
There is also a variant with an overloaded <code>&lt;&lt;</code> operator:
</p>

<pre class="brush:groovy">
    // add a task to the queue
    queue << [
        countdownMillis: 1000, url: "/task/dailyEmail",
        taskName: "dailyNewsletter",
        method: 'PUT', params: [date: '20101214'],
        payload: content,
        retryOptions: [
            taskRetryLimit: 10,
            taskAgeLimitSeconds: 100,
            minBackoffSeconds: 40,
            maxBackoffSeconds: 50,
            maxDoublings: 15
        ]
    ]
</pre>

<a name="email"></a>
<h2>Email support</h2>

<h3>New <code>send()</code> method for the mail service</h3>

<p>
<b>Gaelyk</b> adds a new <code>send()</code> method to the
<a href="http://code.google.com/intl/fr-FR/appengine/docs/java/javadoc/com/google/appengine/api/mail/MailService.html">mail service</a>,
which takes <i>named arguments</i>. That way, you don't have to manually build a new message yourself.
In your Groovlet, for sending a message, you can do this:
</p>

<pre class="brush:groovy">
    mail.send from: "app-admin-email@gmail.com",
            to: "recipient@somecompany.com",
            subject: "Hello",
            textBody: "Hello, how are you doing? -- MrG",
            attachment: [data: "Chapter 1, Chapter 2".bytes, fileName: "outline.txt"]
</pre>

<p>
Similarily, a <code>sendToAdmins()</code> method was added to, for sending emails to the administrators of the application.
</p>

<blockquote>
    <b>Note: </b> There is a <code>sender</code> alias for the <code>from</code> attribute.
    And instead of a <code>textBody</code> attribute, you can send HTML content with the <code>htmlBody</code> attribute.
</blockquote>

<blockquote>
    <b>Note: </b> There are two attachment attributes: <code>attachment</code> and <code>attachments</code>.
    <ul>
        <li>
            <code>attachment</code> is used for when you want to send just one attachment.
            You can pass a map with a <code>data</code> and a <code>fileName</code> keys.
            Or you can use an instance of <code>MailMessage.Attachment</code>.
        </li>
        <li>
            <code>attachments</code> lets you define a list of attachments.
            Again, either the elements of that list are maps of <code>data</code> / <code>fileName</code> pairs,
            or instances of <code>MailMessage.Attachment</code>.
        </li>
    </ul>
</blockquote>

<a name="incoming-mail"></a>
<h3>Incoming email messages</h3>

<p>
Your applications can also receive incoming email messages,
in a similar vein as the incoming XMPP messaging support.
To enable incoming email support, you first need to update your <code>appengine-web.xml</code> file as follows:
</p>

<pre class="brush:xml">
    &lt;inbound-services&gt;
        &lt;service&gt;mail&lt;/service&gt;
    &lt;/inbound-services&gt;
</pre>

<p>
In your <code>web.xml</code> file, you can eventually add a security constraint on the web handler
that will take care of treating the incoming emails:
</p>

<pre class="brush:xml">
    ...
    &lt;!-- Only allow the SDK and administrators to have access to the incoming email endpoint --&gt;
    &lt;security-constraint&gt;
        &lt;web-resource-collection&gt;
            &lt;url-pattern&gt;/_ah/mail/*&lt;/url-pattern&gt;
        &lt;/web-resource-collection&gt;
        &lt;auth-constraint&gt;
            &lt;role-name&gt;admin&lt;/role-name&gt;
        &lt;/auth-constraint&gt;
    &lt;/security-constraint&gt;
    ...
</pre>

<p>
You need to define a Groovlet handler for receiving the incoming emails
with a special <a href="/tutorial/url-routing#email-and-jabber">route definition</a>,
in your <code>/WEB-INF/routes.groovy</code> configuration file:
</p>

<pre class="brush:groovy">
    email to: "/receiveEmail.groovy"
</pre>

<blockquote>
<b>Remark: </b> You are obviously free to change the name and path of the Groovlet.
</blockquote>

<p>
All the incoming emails will be sent as MIME messages through the request of your Groovlet.
To parse the MIME message, you'll be able to use the <code>parseMessage(request)</code> method
on the mail service injected in the binding of your Groovlet, which returns a
<a href="http://java.sun.com/products/javamail/javadocs/javax/mail/internet/MimeMessage.html"><code>javax.mail.MimeMessage</code></a>
instance:
</p>

<pre class="brush:groovy">
    def msg = mail.parseMessage(request)

    log.info "Subject \${msg.subject}, to \${msg.allRecipients.join(', ')}, from \${msg.from[0]}"
</pre>

<a name="jabber"></a>
<h2>XMPP/Jabber support</h2>

<p>
Your application can send and receive instant messaging through XMPP/Jabber.
</p>

<blockquote>
<b>Note: </b> You can learn more about
<a href="http://code.google.com/appengine/docs/java/xmpp/overview.html">XMPP support</a> on the online documentation.
</blockquote>

<a name="jabber-sending"></a>
<h3>Sending messages</h3>

<p>
<b>Gaelyk</b> provides a few additional methods to take care of sending instant messages, get the presence of users,
or to send invitations to other users.
Applications usually have a corresponding Jabber ID named after your application ID, such as <code>yourappid@appspot.com</code>.
To be able to send messages to other users, your application will have to invite other users, or be invited to chat.
So make sure you do so for being able to send messages.
</p>

<p>
Let's see what it would look like in a Groovlet for sending messages to a user:
</p>

<pre class="brush:groovy">
    String recipient = "someone@gmail.com"

    // check if the user is online
    if (xmpp.getPresence(recipient).isAvailable()) {
        // send the message
        def status = xmpp.send(to: recipient, body: "Hello, how are you?")

        // checks the message was successfully delivered to all the recipients
        assert status.isSuccessful()
    }
</pre>

<p>
<b>Gaelyk</b> once again decorates the various XMPP-related classes in the App Engine SDK with new methods:
</p>

<ul>
    <li>on <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/xmpp/XMPPService.html"><code>XMPPService</code></a>'s instance
        <ul>
            <li><code>SendResponse send(Map msgAttr)</code> : more details on this method below</li>
            <li><code>void sendInvitation(String jabberId)</code> : send an invitation to a user</li>
            <li><code>sendInvitation(String jabberIdTo, String jabberIdFrom)</code> : send an invitation to a user from a different Jabber ID</li>
            <li><code>Presence getPresence(String jabberId)</code> : get the presence of this particular user</li>
            <li><code>Presence getPresence(String jabberIdTo, String jabberIdFrom)</code> : same as above but using a different Jabber ID for the request</li>
        </ul>
    </li>
    <li>on <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/xmpp/Message.html"><code>Message</code></a> instances
        <ul>
            <li><code>String getFrom()</code> : get the Jabber ID of the sender of this message</li>
            <li><code>GPathResult getXml()</code> : get the XmlSlurper parsed document of the XML payload</li>
            <li><code>List&lt;String&gt; getRecipients()</code> : get a list of Strings representing the Jabber IDs of the recipients</li>
        </ul>
    </li>
    <li>on <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/xmpp/SendResponse.html"><code>SendResponse</code></a> instances
        <ul>
            <li><code>boolean isSuccessful()</code> : checks that all recipients received the message</li>
        </ul>
    </li>
</ul>

<p>
To give you a little more details on the various attributes you can use to create messages to be sent,
you can pass the following attributes to the <code>send()</code> method of <code>XMPPService</code>:
</p>

<ul>
    <li><tt>body</tt> : the raw text content of your message</li>
    <li><tt>xml</tt> : a closure representing the XML payload you want to send</li>
    <li><tt>to</tt> : contains the recipients of the message (either a String or a List of String)</li>
    <li><tt>from</tt> : a String representing the Jabber ID of the sender</li>
    <li><tt>type</tt> : either an instance of the
        <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/xmpp/MessageType.html"><code>MessageType</code></a>
        enum or a String (<code>'CHAT'</code>, <code>'ERROR'</code>, <code>'GROUPCHAT'</code>, <code>'HEADLINE'</code>,
        <code>'NORMAL'</code>)</li>
</ul>

<blockquote>
<b>Note: </b> <code>body</code> and <code>xml</code> are exclusive, you can't specify both at the same time.
</blockquote>

<p>
We mentioned the ability to send XML payloads, instead of normal chat messages:
this functionality is particularly interesting if you want to use XMPP/Jabber as a communication transport
between services, computers, etc. (ie. not just real human beings in front of their computer).
We've shown an example of sending raw text messages, here's how you could use closures in the <code>xml</code>
to send XML fragments to a remote service:
</p>

<pre class="brush:groovy">
    String recipient = "service@gmail.com"

    // check if the service is online
    if (xmpp.getPresence(recipient).isAvailable()) {
        // send the message
        def status = xmpp.send(to: recipient, xml: {
            customers {
                customer(id: 1) {
                    name 'Google'
                }
            }
        })

        // checks the message was successfully delivered to the service
        assert status.isSuccessful()
    }
</pre>

<blockquote>
<b>Implementation detail: </b> the closure associated with the <code>xml</code> attribute is actually passed to
an instance of <a href="http://groovy.codehaus.org/Creating+XML+using+Groovy%27s+StreamingMarkupBuilder"><code>StreamingMarkupBuilder</code></a>
which creates an XML stanza.
</blockquote>

<a name="jabber-receiving"></a>
<h3>Receiving messages</h3>

<p>
It is also possible to receive messages from users.
For that purpose, <b>Gaelyk</b> lets you define a Groovlet handler that will be receiving the incoming messages.
To enable the reception of messages, you'll have to do two things:
</p>

<ul>
    <li>add a new configuration fragment in <code>/WEB-INF/appengine-web.xml</code></li>
    <li>add a route for the Groovlet handler in <code>/WEB-INF/routes.groovy</code></li>
</ul>

<p>As a first step, let's configure <code>appengine-web.xml</code> by adding this new element:</p>

<pre class="brush:xml">
    &lt;inbound-services&gt;
        &lt;service&gt;xmpp_message&lt;/service&gt;
    &lt;/inbound-services&gt;
</pre>

<p>
Similarily to the incoming email support, you can define security constraints:
</p>

<pre class="brush:xml">
    ...
    &lt;!-- Only allow the SDK and administrators to have access to the incoming jabber endpoint --&gt;
    &lt;security-constraint&gt;
        &lt;web-resource-collection&gt;
            &lt;url-pattern&gt;/_ah/xmpp/message/chat/&lt;/url-pattern&gt;
        &lt;/web-resource-collection&gt;
        &lt;auth-constraint&gt;
            &lt;role-name&gt;admin&lt;/role-name&gt;
        &lt;/auth-constraint&gt;
    &lt;/security-constraint&gt;
    ...
</pre>

<p>Then let's add the route definition in <code>routes.groovy</code>:</p>

<pre class="brush:groovy">
    jabber to: "/receiveJabber.groovy"
</pre>

<p>
Alternatively, you can use the longer version:
</p>

<pre class="brush:groovy">
    jabber chat, to: "/receiveJabber.groovy"
</pre>

<blockquote>
<b>Remark: </b> You are obviously free to change the name and path of the Groovlet.
</blockquote>

<p>
All the incoming Jabber/XMPP messages will be sent through the request of your Groovlet.
Thanks to the <code>parseMessage(request)</code> method on the <code>xmpp</code> service
injected in the binding of your Groovlet, you'll be able to access the details of a
<a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/xmpp/Message.html"><code>Message</code></a>
instance, as shown below:
</p>

<pre class="brush:groovy">
    def message = xmpp.parseMessage(request)

    log.info "Received from \${message.from} with body \${message.body}"

    // if the message is an XML document instead of a raw string message
    if (message.isXml()) {
        // get the raw XML string
        message.stanza

        // or get a document parsed with XmlSlurper
        message.xml
    }
</pre>

<a name="jabber-presence"></a>
<h3>XMPP presence handling</h3>

<p>
To be notified of users' presence, you should first configure <code>appengine-web.xml</code>
to specify you want to activate the incoming presence service:
</p>

<pre class="brush:xml">
    &lt;inbound-services&gt;
        &lt;service&gt;xmpp_presence&lt;/service&gt;
    &lt;/inbound-services&gt;
</pre>

<p>
Then, add a special route definition in <code>routes.groovy</code>:
</p>

<pre class="brush:groovy">
    jabber presence, to: "/presence.groovy"
</pre>

<blockquote>
<b>Remark: </b> You are obviously free to change the name and path of the Groovlet handling the presence requests.
</blockquote>

<p>
Now, in your <code>presence.groovy</code> Groovlet, you can call the overriden <code>XMPPService#parsePresence</code> method:
</p>

<pre class="brush:groovy">
    // parse the incoming presence from the request
    def presence = xmpp.parsePresence(request)

    log.info "\${presence.fromJid.id} is \${presence.available ? '' : 'not'} available"
</pre>

<a name="jabber-subscription"></a>
<h3>XMPP subscription handling</h3>

<p>
To be notified of subscriptions, you should first configure <code>appengine-web.xml</code>
to specify you want to activate the incoming subscription service:
</p>

<pre class="brush:xml">
    &lt;inbound-services&gt;
        &lt;service&gt;xmpp_subscribe&lt;/service&gt;
    &lt;/inbound-services&gt;
</pre>

<p>
Then, add a special route definition in <code>routes.groovy</code>:
</p>

<pre class="brush:groovy">
    jabber subscription, to: "/subscription.groovy"
</pre>

<blockquote>
<b>Remark: </b> You are obviously free to change the name and path of the Groovlet handling the subscription requests.
</blockquote>

<p>
Now, in your <code>subscription.groovy</code> Groovlet, you can call the overriden <code>XMPPService#parseSubscription</code> method:
</p>

<pre class="brush:groovy">
    // parse the incoming subscription from the request
    def subscription = xmpp.parseSubscription(request)

    log.info "Subscription from \${subscription.fromJid.id}: \${subscription.subscriptionType}}"
</pre>


<a name="memcache"></a>
<h2>Enhancements to the Memcache service</h2>

<p>
<b>Gaelyk</b> provides a few additional methods to the Memcache service, to get and put values in the cache
using Groovy's natural subscript notation, as well as for using the <code>in</code> keyword to check when a key
is present in the cache or not.
</p>

<pre class="brush:groovy">
    class Country implements Serializable { String name }

    def countryFr = new Country(name: 'France')

    // use the subscript notation to put a country object in the cache, identified by a string
    // (you can also use non-string keys)
    memcache['FR'] = countryFr

    // check that a key is present in the cache
    if ('FR' in memcache) {
        // use the subscript notation to get an entry from the cache using a key
        def countryFromCache = memcache['FR']
    }
</pre>

<blockquote>
<b>Note: </b> Make sure the objects you put in the cache are serializable.
Also, be careful with the last example above as the <code>'FR'</code> entry in the cache
may have disappeared between the time you do the <code>if (... in ...)</code> check
and the time you actually retrieve the value associated with the key from memcache.
</blockquote>

<a name="memoize"></a>
<h3>Closure memoization</h3>

<p>
As Wikipedia puts it, <a href="http://en.wikipedia.org/wiki/Memoization">memoization</a> is an <i>optimization technique
used primarily to speed up computer programs by having function calls avoid repeating the calculation
of results for previously-processed inputs</i>.
<b>Gaelyk</b> provides such a mechanism for closures, storing invocation information
(a closure call with its arguments values) in memcache.
</p>

<p>
An example, if you want to avoid computing expansive operations (like repeatedly fetching results from the datastore)
in a complex algorithm:
</p>

<pre class="brush:groovy">
    Closure countEntities = memcache.memoize { String kind ->
        datastore.prepare( new Query(kind) ).countEntities()
    }

    // the first time, the expensive datastore operation will be performed and cached
    def totalPics = countEntities('photo')

    /* add new pictures to the datastore */

    // the second invocation, the result of the call will be the same as before, coming from the cache
    def totalPics2 = countEntities('photo')
</pre>

<blockquote>
<b>Note: </b> Invocations are stored in memcache only for up to the 30 seconds request time limit of App Engine.
</blockquote>

<a name="blobstore"></a>
<h2>Enhancements related to the Blobstore and File services</h2>

<p>
<b>Gaelyk</b> provides several enhancements around the usage of the blobstore service.
</p>

<a name="blob-info"></a>
<h3>Getting blob information</h3>

<p>
Given a blob key, you can retrieve various details about the blob when it was uploaded:
</p>

<pre class="brush:groovy">
    BlobKey blob = ...

    // retrieve an instance of BlobInfo
    BlobInfo info = blob.info

    // directly access the BlobInfo details from the key itself
    String filename     = blob.filename
    String contentType  = blob.contentType
    Date creation       = blob.creation
    long size           = blob.size
</pre>

<a name="serving-blob"></a>
<h3>Serving blobs</h3>

<p>
With the blobstore service, you can stream the content of blobs back to the browser, directly on the response object:
</p>

<pre class="brush:groovy">
    BlobKey blob = ...

    // serve the whole blob
    blob.serve response

    // serve a fragment of the blob
    def range = new ByteRange(1000) // starting from 1000
    blob.serve response, range

    // serve a fragment of the blob using an int range
    blob.serve response, 1000..2000
</pre>

<a name="reading-blob"></a>
<h3>Reading the content of a Blob</h3>

<p>
Beyond the ability to serve blobs directly to the response output stream with
<code>blobstoreService.serve(blobKey, response)</code> from your groovlet,
there is the possibility of
<a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/blobstore/BlobstoreInputStream.html">obtaining an <code>InputStream</code></a>
to read the content of the blob.
<b>Gaelyk</b> adds three convenient methods on <code>BlobKey</code>
to easily deal with a raw input stream or with a reader, leveraging Groovy's own input stream and reader methods.
The stream and reader are handled properly with regards to cleanly opening and closing those resources
so that you don't have to take care of that aspect yourself.
</p>

<pre class="brush:groovy">
    BlobKey blobKey = ...

    blobKey.withStream { InputStream stream ->
        // do something with the stream
    }

    // defaults to using UTF-8 as encoding for reading from the underlying stream
    blobKey.withReader { Reader reader ->
        // do something with the reader
    }

    // specifying the encoding of your choice
    blobKey.withReader("UTF-8") { Reader reader ->
        // do something with the reader
    }
</pre>

<p>
You can also fetch byte arrays for a given range:
</p>

<pre class="brush:groovy">
    BlobKey blob = ...
    byte[] bytes

    // using longs
    bytes = blob.fetchData 1000, 2000

    // using a Groovy int range
    bytes = blob.fetchData 1000..2000

    // using a ByteRange
    def range = new ByteRange(1000, 2000) // or 1000..2000 as ByteRange
    bytes = blob.fetchData range
</pre>

<a name="delete-blob"></a>
<h3>Deleting a blob</h3>

<p>Given a blob key, you can easily delete it thanks to the <code>delete()</code> method:</p>

<pre class="brush:groovy">
    BlobKey blob = ...

    blob.delete()
</pre>

<a name="blobstore-each-collect"></a>
<h3>Iterating over and collecting <code>BlobInfo</code>s</h3>

<p>
The blobstore service stores blobs that are identified by <code>BlobKeys</code>,
and whose metadata are represented by <code>BlobInfo</code>.
If you want to iterate over all the blobs from the blobstore,
you can use the <code>BlobInfoFactory</code> and its <code>queryBlobInfos()</code> method,
but <b>Gaelyk</b> simplifies that job with an <code>each{}</code> and a <code>collect{}</code> method
right from the <code>blobstore</code> service:
</p>

<pre class="brush:groovy">
    blobstore.each { BlobInfo info -> out << info.filename }

    def fileNames = blobstore.collect { BlobInfo info -> info.filename }
</pre>

<a name="blobstore-example"></a>
<h3>Example Blobstore service usage</h3>

<p>
In this section, we'll show you a full-blown example.
First of all, let's create a form to submit a file to the blobstore,
in a template named <code>upload.gtpl</code> at the root of your war:
</p>

<pre class="brush:xml">
    &lt;html&gt;
    &lt;body&gt;
        &lt;h1&gt;Please upload a text file&lt;/h1&gt;
        &lt;form action="\${blobstore.createUploadUrl('/uploadBlob.groovy')}"
                method="post" enctype="multipart/form-data"&gt;
            &lt;input type="file" name="myTextFile"&gt;
            &lt;input type="submit" value="Submit"&gt;
        &lt;/form&gt;
    &lt;/body&gt;
    &lt;/html&gt;
</pre>

<p>
The form will be posted to a URL created by the blobstore service,
that will then forward back to the URL you've provided when calling
<code>blobstore.createUploadUrl('/uploadBlob.groovy')</code>
</p>

<blockquote>
<b>Warning: </b> The URL to he groovlet to which the blobstore service will forward the uploaded blob details
should be a direct path to the groovlet like <code>/uploadBlob.groovy</code>.
For an unknown reason, you cannot use a URL defined through the URL routing system.
This is not necessarily critical, in the sense that this URL is never deployed in the browser anyway.
</blockquote>

<p>
Now, create a groovlet named <code>uploadBlob.groovy</code> stored in <code>/WEB-INF/groovy</code>
with the following content:
</p>

<pre class="brush:groovy">
    def blobs = blobstore.getUploadedBlobs(request)
    def blob = blobs["myTextFile"]

    response.status = 302

    if (blob) {
        redirect "/success?key=\${blob.keyString}"
    } else {
        redirect "/failure"
    }
</pre>

<p>
In the groovlet, you retrieve all the blobs uploaded in the <code>upload.gtpl</code> page,
and more particularly, the blob coming from the <code>myTextFile</code> input file element.
</p>

<blockquote>
<b>Warning: </b> Google App Engine mandates that you explicitly specify a redirection status code (301, 302 or 303),
and that you <b>do</b> redirect the user somewhere else, otherwise you'll get some runtime errors.
</blockquote>

<p>
We define some friendly URLs in the URL routing definitions for the upload form template, the success and failure pages:
</p>

<pre class="brush:groovy">
    get "/upload",  forward: "/upload.gtpl"
    get "/success", forward: "/success.gtpl"
    get "/failure", forward: "/failure.gtpl"
</pre>

<p>
You then create a <code>failure.gtpl</code> page at the root of your war directory:
</p>

<pre class="brush:xml">
    &lt;html&gt;
        &lt;body&gt;
            &lt;h1&gt;Failure&lt;/h1&gt;
            &lt;h2&gt;Impossible to store or access the uploaded blob&lt;/h2&gt;
        &lt;/body&gt;
    &lt;/html&gt;
</pre>

<p>
And a <code>success.gtpl</code> page at the root of your war directory,
showing the blob details, and outputing the content of the blob (a text file in our case):
</p>

<pre class="brush:xml">
    &lt;% import com.google.appengine.api.blobstore.BlobKey %&gt;
    &lt;html&gt;
        &lt;body&gt;
            &lt;h1&gt;Success&lt;/h1&gt;
            &lt;% def blob = new BlobKey(params.key) %&gt;

            &lt;div>
                File name: \${blob.filename} &lt;br/&gt;
                Content type: \${blob.contentType}&lt;br/&gt;
                Creation date: \${blob.creation}&lt;br/&gt;
                Size: \${blob.size}
            &lt;/div&gt;

            &lt;h2&gt;Content of the blob&lt;/h2&gt;

            &lt;div&gt;
                &lt;% blob.withReader { out << it.text } %&gt;
            &lt;/div&gt;
        &lt;/body&gt;
    &lt;/html&gt;
</pre>

<p>
    Now that you're all set up, you can access <code>http://localhost:8080/upload</code>,
    submit a text file to upload, and click on the button.
    Google App Engine will store the blob and forward the blob information to your <code>uploadBlob.groovy</code> groovlet
    that will then redirect to the success page (or failure page in case something goes wrong).
</p>

<a name="file-service"></a>
<h3>File service</h3>

<p>
The File service API provides a convenient solution for accessing the blobstore,
and particularly for programmatically adding blobs
without having to go through the blobstore form-based upload facilities.
<b>Gaelyk</b> adds a <code>files</code> variable in the binding of Groovlets and templates,
which corresponds to the <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/files/FileService.html">FileService</a> instance.
</p>

<h4>Writing text content</h4>

<p>
Inspired by Groovy's own <code>withWriter{}</code> method, a new method is available on
<a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/files/AppEngineFile.html">AppEngineFile</a>
that can be used as follows, to write text content through a writer:
</p>

<pre class="brush:groovy">
    // let's first create a new blob file through the regular FileService method
    def file = files.createNewBlobFile("text/plain", "hello.txt")

    file.withWriter { writer ->
        writer << "some content"
    }
</pre>

<p>
You can also specify three options to the <code>withWriter{}</code> method, in the form of named arguments:
</p>

<ul>
    <li><b>encoding</b>: a string ("UTF-8" by default) defining the text encoding</li>
    <li><b>locked</b>: a boolean (true by default) telling if we want an exclusive access to the file</li>
    <li><b>finalize</b>: a boolean (true by default) to indicate if we want to finalize the file to prevent further appending</li>
</ul>

<pre class="brush:groovy">
    file.withWriter(encoding: "US-ASCII", locked: false, finalize: false) { writer ->
        writer << "some content"
    }
</pre>

<h4>Writing binary content</h4>

<p>In a similar fashion, you can write to an output stream your binary content:</p>

<pre class="brush:groovy">
    // let's first create a new blob file through the regular FileService method
    def file = files.createNewBlobFile("text/plain", "hello.txt")

    file.withOutputStream { stream ->
        stream << "Hello World".bytes
    }
</pre>

<p>
You can also specify two options to the <code>withOutputStream{}</code> method, in the form of named arguments:
</p>

<ul>
    <li><b>locked</b>: a boolean (true by default) telling if we want an exclusive access to the file</li>
    <li><b>finalize</b>: a boolean (true by default) to indicate if we want to finalize the file to prevent further appending</li>
</ul>

<pre class="brush:groovy">
    file.withOutputStream(locked: false, finalize: false) { writer ->
        writer << "Hello World".bytes
    }
</pre>

<blockquote>
<b>Note: </b> To finalize a file in the blobstore, App Engine mandates the file needs to be locked.
That's why by default <code>locked</code> and <code>finalize</code> are set to true by default.
When you want to later be able to append again to the file, make sure to set <code>finalize</code> to false.
And if you want to avoid others from concurrently writing to your file, it's better to set <code>locked</code> to false.
</blockquote>

<h4>Reading binary content</h4>

<p>
<b>Gaelyk</b> already provides reading capabilities from the blobstore support, as we've already seen,
but the File service also supports reading from <code>AppEngineFile</code>s.
To read from an <code>AppEngineFile</code> instance, you can use the <code>withInputStream{}</code> method,
which takes an optional map of options, and a closure whose argument is a <code>BufferedInputStream</code>:
</p>

<pre class="brush:groovy">
    file.withInputStream { BufferedInputStream stream ->
        // read from the stream
    }
</pre>

<p>
You can also specify an option for locking the file (the file is locked by default):
</p>

<pre class="brush:groovy">
    file.withInputStream(locked: false) { BufferedInputStream stream ->
        // read from the stream
    }
</pre>

<h4>Reading text content</h4>

<p>
Similarily to reading from an input stream, you can also read from a <code>BufferedReader</code>,
with the <code>withReader{}</code> method:
</p>

<pre class="brush:groovy">
    file.withReader { BufferedReader reader ->
        log.info reader.text
    }
</pre>

<p>
You can also specify an option for locking the file (the file is locked by default):
</p>

<pre class="brush:groovy">
    file.withReader(locked: false) { BufferedReader reader ->
        log.info reader.text
    }
</pre>

<a name="files-misc"></a>
<h4>Miscelanous improvements</h4>

<p>
If you store a file path in the form of a string (for instance for storing its reference in the datastore),
you need to get back an <code>AppEngineFile</code> from its string representation:
</p>

<pre class="brush:groovy">
    def path = someEntity.filePath
    def file = files.fromPath(path)
</pre>

<p>
If you have a <code>BlobKey</code>, you can retrieve the associated <code>AppEngineFile</code>:
</p>

<pre class="brush:groovy">
    def key = ... // some BlobKey
    def file = key.file
</pre>

<p>
You can retrieve the blob key associated with your file (for example when you want to access an <code>Image</code> instance:
</p>

<pre class="brush:groovy">
    def key = file.blobKey
    def image = key.image
</pre>

<p>
And if you want to delete a file without going through the blobstore service, you can do:
</p>

<pre class="brush:groovy">
    file.delete()
</pre>

<a name="namespace"></a>
<h2>Namespace support</h2>

<p>
Google App Engine SDK allows you to create
"<a href="http://code.google.com/appengine/docs/java/multitenancy/multitenancy.html">multitenant</a>"-aware
applications, through the concept of namespace, that you can handle through the
<a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/NamespaceManager.html">NamespaceManager</a> class.
</p>

<p>
<b>Gaelyk</b> adds the variable <code>namespace</code> into the binding of your groovlets and templates.
This <code>namespace</code> variable is simply the <code>NamespaceManager</code> class.
<b>Gaelyk</b> adds a handy method for automating the pattern of setting a temporary namespace and restoring it to its previous value,
thanks to the added <code>of()</code> method, taking a namespace name in the form of a string,
and a closure to be executed when that namespace is active.
This method can be used as follows:
</p>

<pre class="brush:groovy">
    // temporarily set a new namespace
    namespace.of("customerA") {
        // use whatever service leveraging the namespace support
        // like the datastore or memcache
    }
    // once the closure is executed, the old namespace is restored
</pre>

<a name="images"></a>
<h2>Images service enhancements</h2>

<a name="images-wrapper"></a>
<h3>The images service and service factory wrapper</h3>

<p>
The Google App Engine SDK is providing two classes for handling images:
</p>

<ul>
    <li>
        <code><a href="http://code.google.com/intl/fr-FR/appengine/docs/java/javadoc/com/google/appengine/api/images/ImagesServiceFactory.html">ImageServiceFactory</a></code>
        is used to retrieve the Images service, to create images (from blobs, byte arrays), and to make transformation operations.
    </li>
    <li>
        <code><a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/images/ImagesService.html">ImageService</a></code>
        is used for applying transforms to images, create composite images, serve images, etc.
    </li>
</ul>

<p>
Very quickly, as you use the images handling capabilities of the API,
you quickly end up jumping between the factory and the service class all the time.
But thanks to <b>Gaelyk</b>, both <code>ImagesServiceFactory</code> and <code>ImagesService</code> are combined into one.
So you can call any method on either of them on the same <code>images</code> instance available in your groovlets and templates.
</p>

<pre class="brush:groovy">
    // retrieve an image stored in the blobstore
    def image = images.makeImageFromBlob(blob)

    // apply a resize transform on the image to create a thumbnail
    def thumbnail = images.applyTransform(images.makeResize(260, 260), image)

    // serve the binary data of the image to the servlet output stream
    sout << thumbnail.imageData
</pre>

<p>
On the first line above, we created the image out of the blobstore using the images service,
but there is also a more rapid shortcut for retrieving an image when given a blob key:
</p>

<pre class="brush:groovy">
    def blobKey = ...
    def image = blobKey.image
</pre>

<p>
In case you have a file or a byte array representing your image, you can also easily instanciate an <code>Image</code> with:
</p>

<pre class="brush:groovy">
    // from a byte array
    byte[] byteArray = ...
    def image = byteArray.image

    // from a file directly
    image = new File('/images/myimg.png').image
</pre>

<a name="image-dsl"></a>
<h3>An image manipulation language</h3>

<p>
The images service permits the manipulation of images by applying various transforms,
like resize, crop, flip (vertically or horizontally), rotate, and even an "I'm feeling lucky" transform!
The <b>Gaelyk</b> image manipulation DSL allows to simplify the combination of such operations=
</p>

<pre class="brush:groovy">
    blobKey.image.transform {
        resize 100, 100
        crop 0.1, 0.1, 0.9, 0.9
        horizontal flip
        vertical flip
        rotate 90
        feeling lucky
    }
</pre>

<p>
The benefit of this approach is that transforms are combined within a single composite transform,
which will be applied in one row to the original image, thus saving on CPU computation.
But if you just need to make one transform, you can also call new methods on <code>Image</code> as follows:
</p>

<pre class="brush:groovy">
    def image = ...

    def thumbnail   = image.resize(100, 100)
    def cropped     = image.crop(0.1, 0.1, 0.9, 0.9)
    def hmirror     = image.horizontalFlip()
    def vmirror     = image.verticalFlip()
    def rotated     = image.rotate(90)
    def lucky       = image.imFeelingLucky()
</pre>

<a name="capabilities"></a>
<h2>Capabilities service support</h2>

<p>
Occasionally, Google App Engine will experience some reliability issues with its various services,
or certain services will be down for scheduled maintenance.
The Google App Engine SDK provides a service, the
<code><a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/capabilities/CapabilitiesService.html">CapabilitiesService</a></code>,
to query the current status of the services.
<b>Gaelyk</b> adds support for this service, by injecting it in the binding of your groovlets and templates,
and by adding some syntax sugar to simplify its use.
</p>

<pre class="brush:groovy">
    import static com.google.appengine.api.capabilities.Capability.*
    import static com.google.appengine.api.capabilities.CapabilityStatus.*

    if (capabilities[DATASTORE] == ENABLED && capabilities[DATASTORE_WRITE] == ENABLED) {
        // write something into the datastore
    } else {
        // redirect the user to a page with a nice maintenance message
    }
</pre>

<blockquote>
<b>Note: </b> Make sure to have a look at the <a href="/tutorial/url-routing#capability-routing">capability-aware URL routing</a> configuration.
</blockquote>

<p>
The services that can be queried are defined as static constants on
<code><a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/capabilities/Capability.html">Capability</a></code>
and currently are:
</p>

<ul>
    <li>BLOBSTORE</li>
    <li>DATASTORE</li>
    <li>DATASTORE_WRITE</li>
    <li>IMAGES</li>
    <li>MAIL</li>
    <li>MEMCACHE</li>
    <li>TASKQUEUE</li>
    <li>URL_FETCH</li>
    <li>XMPP</li>
</ul>

<p>
The different possible statuses are defined in the
<code><a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/capabilities/CapabilityStatus.html">CapabilityStatus</a></code>
enum:
</p>

<ul>
    <li>ENABLED</li>
    <li>DISABLED</li>
    <li>SCHEDULED_MAINTENANCE</li>
    <li>UNKNOWN</li>
</ul>

<blockquote>
<b>Tip: </b> Make sure to static import <code>Capability</code> and <code>CapabilityStatus</code>
in order to keep your code as concise and readable as possible, like in the previous example, with:
<pre class="brush:groovy">
    import static com.google.appengine.api.capabilities.Capability.*
    import static com.google.appengine.api.capabilities.CapabilityStatus.*
</pre>
</blockquote>

<p>
Additionally, instead of comparing explicitely against a specific <code>CapabilityStatus</code>,
<b>Gaelyk</b> provides a coercion of the status to a boolean (also called "Groovy Truth").
This allows you to write simpler conditionals:
</p>

<pre class="brush:groovy">
    import static com.google.appengine.api.capabilities.Capability.*
    import static com.google.appengine.api.capabilities.CapabilityStatus.*

    if (capabilities[DATASTORE] && capabilities[DATASTORE_WRITE]) {
        // write something into the datastore
    } else {
        // redirect the user to a page with a nice maintenance message
    }
</pre>

<blockquote>
<b>Note: </b> Only the <code>ENABLED</code> and <code>SCHEDULED_MAINTENACE</code> statuses are considered to be <code>true</code>,
whereas all the other statuses are considered to be <code>false</code>.
</blockquote>

<a name="urlfetch"></a>
<h2>URLFetch Service improvements</h2>

<p>
Google App Engine offers the URLFetch Service to interact with remote servers,
to post to or to fetch content out of external websites.
Often, using the URL directly with Groovy's <code>getBytes()</code> or <code>getText()</code> methods is enough,
and transparently uses the URLFetch Service under the hood.
But sometimes, you need a bit more control of the requests you're making to remote servers,
for example for setting specific headers, for posting custom payloads, making asynchronous requests, etc.
<b>Gaelyk</b> 0.5 provides a convenient integration of the service with a groovier flavor.
</p>

<blockquote>
<b>Note: </b> You may also want to have a look at HTTPBuilder's
<a href="http://groovy.codehaus.org/modules/http-builder/doc/httpurlclient.html">HttpURLClient</a>
for a richer HTTP client library that is compatible with Google App Engine.
</blockquote>

<p>
<b>Gaelyk</b> decorates the URL class with 5 new methods, for the 5 HTTP methods GET, POST, PUT, DELETE, HEAD
which can take an optional map for customizing the call:
</p>

<ul>
    <li><code>url.get()</code></li>
    <li><code>url.post()</code></li>
    <li><code>url.put()</code></li>
    <li><code>url.delete()</code></li>
    <li><code>url.head()</code></li>
</ul>

<p>
Those methods return an
<code><a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/urlfetch/HTTPResponse.html">HTTPResponse</a></code>
or a <code><a href="http://download.oracle.com/javase/6/docs/api/java/util/concurrent/Future.html">Future&lt;HTTPResponse&gt;</a></code>
if the <code>async</code> option is set to true.
</p>

<p>
Let's start with a simple example, say, you want to get the <b>Gaelyk</b> home page content:
</p>

<pre class="brush:groovy">
    URL url = new URL('http://gaelyk.appspot.com')

    def response = url.get()

    assert response.responseCode == 200
    assert response.text.contains('Gaelyk')
</pre>

<p>
As you can see above,
<b>Gaelyk</b> adds a <code>getText()</code> and <code>getText(String encoding)</code> method to HTTPResponse,
so that it is easier to get textual content from remote servers &mdash;
<code>HTTPResponse</code> only provided a <code>getContent()</code> method that returns a byte array.
</p>

<p>
If you wanted to make an asynchronous call, you could do:
</p>

<pre class="brush:groovy">
    def future = url.get(async: true)
    def response = future.get()
</pre>

<a name="urlfetch-options"></a>
<h3>Allowed options</h3>

<p>
Several options are allowed as arguments of the 5 methods.
</p>

<ul>
    <li>
        <tt>allowTruncate</tt>:
        a boolean (false by default), to explicit if we want an exception to be thrown if the reponse exceeds the 1MB quota limit
    </li>
    <li>
        <tt>followRedirects</tt>:
        a boolean (true by default), to specify if we want to allow the request to follow redirects
    </li>
    <li>
        <tt>deadline</tt>: a double (default to 10), the number of seconds to wait for a request to succeed
    </li>
    <li>
        <tt>headers</tt>: a map of headers
    </li>
    <li>
        <tt>payload</tt>: a byte array for the binary payload you want to post or put
    </li>
    <li>
        <tt>params</tt>: a map of query parameters
    </li>
    <li>
        <tt>async</tt>: a boolean (false by defauly), to specify you want to do an asynchronous call or not
    </li>
</ul>

<p>
To finish on the URLFetch Service support, we can have a look at another example using some of the options above:
</p>

<pre class="brush:groovy">
    URL googleSearch = "http://www.google.com/search".toURL()
    HTTPResponse response = googleSearch.get(params: [q: 'Gaelyk'],
            headers: ['User-Agent': 'Mozilla/5.0 (Linux; X11)'])

    assert response.statusCode == 200
    assert response.text.contains('http://gaelyk.appspot.com')
    assert response.headersMap.'Content-Type' == 'text/html; charset=utf-8'
</pre>

<blockquote>
<b>Note: </b> <code>response.statusCode</code> is a synonym of <code>response.responseCode</code>.
And notice the convenient <code>response.headersMap</code> shortcut which returns a convenient
<code>Map&lt;String, String&gt;</code> of headers instead of SDK's <code>response.headers</code>'s
<code>List&lt;HTTPHeader&gt;</code>.
</blockquote>


<a name="channel"></a>
<h2>Channel Service improvements</h2>

<p>
For your Comet-style applications, Google App Engine provides its
<a href="http://code.google.com/appengine/docs/java/channel/">Channel service</a>.
The API being very small, beyond adding the <code>channel</code> binding variable,
<b>Gaelyk</b> only adds an additional shortcut method for sending message
with the same <code>send</code> name as Jabber and Email support (for consistency),
but without the need of creating an instance of <code>ChannelMessage</code>:
</p>

<pre class="brush:groovy">
    def clientId = "1234"
    channel.createChannel(clientId)
    channel.send clientId, "hello"
</pre>


<a name="backend"></a>
<h2>Backend service support</h2>

<p>
The backend service support is quite minimal, from a <b>Gaelyk</b> perspective,
as only a <code>backends</code> (corresponding to a <code>BackendService</code> instance)
and <code>lifecylce</code> (the <code>LifecycleManager</code>) variables
have been added to the binding of Groovlets and templates.
</p>

<p>
In addition, a method for shutdown hooks was added that allows you to use a closure
instead of a <code>ShutdownHook</code> instance:
</p>

<pre class="brush:groovy">
    lifecycle.shutdownHook = { /* shutting down logic */ }
</pre>