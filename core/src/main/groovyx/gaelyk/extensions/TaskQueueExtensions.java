/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gaelyk.extensions;

import groovy.lang.Closure;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

/**
 * Taks queue extension methods
 *
 * @author Guillaume Laforge
 */
public class TaskQueueExtensions {
    
    private static final List<String> ALLOWED_METHODS = Arrays.asList(new String[]{ "GET", "POST", "PUT", "DELETE", "HEAD", "PULL"});
    
    /**
     * Shortcut to get the name of the Queue.
     * <p>
     * Instead of having to call <code>queue.getQueueName()</code> or <code>queue.queueName</code>,
     * you can use the syntax <code>queue.name</code> which is more concise.
     *
     * @return the name of the queue
     */
    public static String getName(Queue selfQueue) {
        return selfQueue.getQueueName();
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
    public static TaskHandle add(Queue selfQueue, Map<String, Object> params) {
        return selfQueue.add(buildTaskOptions(params));
    }
    
    /**
     * Adds a new task on the queue asynchronously using a map for holding the task attributes instead of a TaskOptions builder object.
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
     * @return a future TaskHandle instance
     */
    public static Future<TaskHandle> addAsync(Queue selfQueue, Map<String, Object> params) {
        return selfQueue.addAsync(buildTaskOptions(params));
    }

    @SuppressWarnings("unchecked") private static TaskOptions buildTaskOptions(Map<String, Object> params) {
        TaskOptions options = TaskOptions.Builder.withDefaults();
        for(Entry<String, Object> e : params.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            switch (key) {
                case "countdownMillis":
                    options = options.countdownMillis(((Number)value).longValue());
                    break;
                case "etaMillis":
                    options = options.etaMillis(((Number)value).longValue());
                    break;
                case "taskName":
                    options = options.taskName(String.valueOf(value));
                    break;
                case "url":
                    options = options.url(String.valueOf(value));
                    break;
                case "headers":
                    if (value instanceof Map) {
                        for (Entry<String, String> header : ((Map<String, String>)value).entrySet()) {
                            options = options.header(header.getKey(), header.getValue());
                        }
                    } else {
                        throw new RuntimeException("The headers key/value pairs should be passed as a map.");
                    }
                    break;
                case "retryOptions":
                    if (value instanceof Map) {
                        RetryOptions retryOptions = RetryOptions.Builder.withDefaults();
                        for (Entry<String, Object> option : ((Map<String, Object>)value).entrySet()) {
                            switch (option.getKey()) {
                                case "taskRetryLimit":
                                    retryOptions.taskRetryLimit(((Number)option.getValue()).intValue());
                                    break;
                                case "taskAgeLimitSeconds":
                                    retryOptions.taskAgeLimitSeconds(((Number)option.getValue()).intValue());
                                    break;
                                case "minBackoffSeconds":
                                    retryOptions.minBackoffSeconds(((Number)option.getValue()).intValue());
                                    break;
                                case "maxBackoffSeconds":
                                    retryOptions.maxBackoffSeconds(((Number)option.getValue()).intValue());
                                    break;
                                case "maxDoublings":
                                    retryOptions.maxDoublings(((Number)option.getValue()).intValue());
                                    break;
                                default:
                                    throw new RuntimeException(option.getKey() + " is not a valid retry option parameter.");
                            }
                        }
                        options.retryOptions(retryOptions);
                    } else if (value instanceof RetryOptions) {
                        options.retryOptions((RetryOptions) value);
                    } else {
                        throw new RuntimeException("The retry options parameter should either be a map or an instance of RetryOptions.");
                    }
                    break;
                case "method":
                    if (value instanceof TaskOptions.Method) {
                        options = options.method((Method) value);
                    } else if(ALLOWED_METHODS.contains(value)) {
                        options = options.method(TaskOptions.Method.valueOf((String) value));
                    } else {
                        throw new RuntimeException("Not a valid method: " + value);
                    }
                    break;
                case "params":
                    if (value instanceof Map) {
                        for (Entry<String, Object> option : ((Map<String, Object>)value).entrySet()) {
                            options = options.param(option.getKey(), String.valueOf(option.getValue()));
                        }
                    } else {
                        throw new RuntimeException("The params key/value pairs should be passed as a map.");
                    }
                    break;
                    
                case "payload":
                    if (value instanceof List<?>) {
                        List<?> list = (List<?>) value;
                        options = options.payload(String.valueOf(list.get(0)), String.valueOf(list.get(1)));
                    } else if (value instanceof String) {
                        options = options.payload((String)value);
                    } else if (value instanceof Closure) {
                        options = options.payload(DefaultGroovyMethods.asType((Closure<?>)value, DeferredTask.class));
                    } else if (value instanceof DeferredTask) {
                        options = options.payload((DeferredTask)value);
                    } else {
                        options = options.payload(value.toString());
                    }
                    break;
                default:
                    throw new RuntimeException(key + " is not a valid task option.\n" +
                            "Allowed: countdownMillis, etaMillis, taskName, url, headers, methods, params and payload");
            }
        }
        return options;
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
    public static Future<TaskHandle> leftShift(Queue selfQueue, Map<String, Object> params) {
        return addAsync(selfQueue, params);
    }
}
