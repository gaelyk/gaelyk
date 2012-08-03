package groovyx.gaelyk.extensions

import groovy.transform.CompileStatic
import com.google.appengine.api.taskqueue.Queue
import com.google.appengine.api.taskqueue.TaskHandle
import com.google.appengine.api.taskqueue.TaskOptions
import com.google.appengine.api.taskqueue.RetryOptions
import com.google.appengine.api.taskqueue.DeferredTask

/**
 * Taks queue extension methods
 */
class TaskQueueExtensions {
    /**
     * Shortcut to get the name of the Queue.
     * <p>
     * Instead of having to call <code>queue.getQueueName()</code> or <code>queue.queueName</code>,
     * you can use the syntax <code>queue.name</code> which is more concise.
     *
     * @return the name of the queue
     */
    @CompileStatic
    static String getName(Queue selfQueue) {
        selfQueue.getQueueName()
    }

    /**
     * Add a new task on the queue using a map for holding the task attributes instead of a TaskOptions builder object.
     * <p>
     * Allowed keys are: <ul>
     * <li><code>countdownMillis</code></li>
     * <li><code>etaMillis</code></li>
     * <li><code>headers</code> (a map of key/value pairs)</li>
     * <li><code>method</code> (can be 'GET', 'POST', 'PUT', 'DELETE', 'HEAD' or an enum of TaskOptions.Method)</li>
     * <li><code>params</code> (a map of key/value parameters)</li>
     * <li><code>payload</code></li>
     * <li><code>taskName</code></li>
     * <li><code>url</code></li>
     * </ul>
     *
     * @param params the map of task attributes
     * @return a TaskHandle instance
     */
    static TaskHandle add(Queue selfQueue, Map params) {
        TaskOptions options = TaskOptions.Builder.withDefaults()
        params.each { key, value ->
            if (key in ['countdownMillis', 'etaMillis', 'taskName', 'url']) {
                options = options."$key"(value)
            } else if (key == 'headers') {
                if (value instanceof Map) {
                    value.each { headerKey, headerValue ->
                        options = options.header(headerKey, headerValue)
                    }
                } else {
                    throw new RuntimeException("The headers key/value pairs should be passed as a map.")
                }
            } else if (key == 'retryOptions') {
                if (value instanceof Map) {
                    def retryOptions = RetryOptions.Builder.withDefaults()
                    value.each { retryKey, retryValue ->
                        if (retryKey in ['taskRetryLimit', 'taskAgeLimitSeconds',
                                'minBackoffSeconds', 'maxBackoffSeconds', 'maxDoublings']) {
                            retryOptions."${retryKey}"(retryValue)
                        } else {
                            throw new RuntimeException("'$retryKey' is not a valid retry option parameter.")
                        }
                    }
                    options.retryOptions(retryOptions)
                } else if (value instanceof RetryOptions) {
                    options.retryOptions(value)
                } else {
                    throw new RuntimeException("The retry options parameter should either be a map or an instance of RetryOptions.")
                }
            } else if (key == 'method') {
                if (value instanceof TaskOptions.Method) {
                    options = options.method(value)
                } else if(value in ['GET', 'POST', 'PUT', 'DELETE', 'HEAD', 'PULL']) {
                    options = options.method(TaskOptions.Method.valueOf(value))
                } else {
                    throw new RuntimeException("Not a valid method: $value")
                }
            } else if (key == 'params') {
                if (value instanceof Map) {
                    value.each { paramKey, paramValue ->
                        options = options.param(paramKey, paramValue.toString())
                    }
                } else {
                    throw new RuntimeException("The params key/value pairs should be passed as a map.")
                }
            } else if (key == 'payload') {
                if (value instanceof List) {
                    options = options.payload(*(value.collect { it.toString() }))
                } else if (value instanceof String) {
                    options = options.payload(value)
                } else if (value instanceof Closure) {
                    options = options.payload(value as DeferredTask)
                } else if (value instanceof DeferredTask) {
                    options = options.payload(value)
                } else {
                    options = options.payload(value.toString())
                }
            } else {
                throw new RuntimeException("$key is not a valid task option.\n" +
                        "Allowed: countdownMillis, etaMillis, taskName, url, headers, methods, params and payload")
            }
        }
        return selfQueue.add(options)
    }

    /**
     * Add a new task on the queue using a map for holding the task attributes instead of a TaskOptions builder object.
     * This method adds a <code>&lt;&lt;</code> operator on the <code>Queue</code> for adding new tasks to it.
     * <p>
     * Allowed keys are: <ul>
     * <li><code>countdownMillis</code></li>
     * <li><code>etaMillis</code></li>
     * <li><code>headers</code> (a map of key/value pairs)</li>
     * <li><code>method</code> (can be 'GET', 'POST', 'PUT', 'DELETE', 'HEAD' or an enum of TaskOptions.Method)</li>
     * <li><code>params</code> (a map of key/value parameters)</li>
     * <li><code>payload</code></li>
     * <li><code>taskName</code></li>
     * <li><code>url</code></li>
     * </ul>
     *
     * @param params the map of task attributes
     * @return a TaskHandle instance
     */
    @CompileStatic
    static TaskHandle leftShift(Queue selfQueue, Map params) {
        add(selfQueue, params)
    }
}
