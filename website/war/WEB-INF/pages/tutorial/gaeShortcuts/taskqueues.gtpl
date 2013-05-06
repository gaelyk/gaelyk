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
    // add a task to the queue synchronously
    TaskHandle handle = queue.add countdownMillis: 1000, url: "/task/dailyEmail",
        taskName: "dailyNewsletter",
        method: 'PUT', params: [date: '20101214'],
        payload: content, retryOptions: RetryOptions.Builder.withDefaults()
        
    // add a task to the queue asynchronously
    Future&lt;TaskHandle&gt; future = queue.addAsync countdownMillis: 1000, url: "/task/dailyEmail",
        taskName: "dailyNewsletter",
        method: 'PUT', params: [date: '20101214'],
        payload: content, retryOptions: RetryOptions.Builder.withDefaults()
</pre>

<p>
There is also a variant with an overloaded <code>&lt;&lt;</code> operator for the second one:
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