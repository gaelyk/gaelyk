/*
 * Copyright 2009 the original author or authors.
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
package groovyx.gaelyk.servlet

import com.google.appengine.api.mail.MailService.Message
import com.google.appengine.api.mail.MailService
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.Transaction
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.labs.taskqueue.Queue
import com.google.appengine.api.labs.taskqueue.TaskHandle
import com.google.appengine.api.labs.taskqueue.TaskOptions

/**
 * Category methods decorating the Google App Engine SDK classes
 * adding new shortcut methods to simplify the usage of the SDK
 * from within Groovy servlets and templates.
 *
 * @author Guillaume Laforge
 */
class GaelykCategory {

    // ----------------------------------------------------------------
    // Category methods dedicated to the Mail service
    // ----------------------------------------------------------------

    /**
     * Create a <code>MailService.Message</code> out of Map parameters.
     * Each map key must correspond to a valid property on the message object.
     */

    private static Message createMessageFromMap(Map m) {
        def msg = new Message()
        m.each {k, v ->
            if (v instanceof String) v = [v]
            msg[k] = v
        }
        return msg
    }

    /**
     * Additional <code>send()</code> method taking a map as parameter.
     * The map can contain the normal properties of the
     * <code>MailService.Message</code> class.
     *
     * @throws groovy.lang.MissingPropertyException when the key doesn't correspond
     * to a property of the <code>MailService.Message</code> class.
     */
    static void send(MailService mailService, Map m) {
        Message msg = createMessageFromMap(m)
        mailService.send msg
    }


    /**
     * Additional <code>sendToAdmins()</code> method for sending emails to the application admins. 
     * This method is taking a map as parameter.
     * The map can contain the normal properties of the
     * <code>MailService.Message</code> class.
     *
     * @param
     *
     * @throws groovy.lang.MissingPropertyException when the key doesn't correspond
     * to a property of the <code>MailService.Message</code> class.
     */
    static void sendToAdmins(MailService mailService, Map m) {
        Message msg = createMessageFromMap(m)
        mailService.sendToAdmins msg 
    }

    // ----------------------------------------------------------------
    // Category methods dedicated to the low-level DataStore service
    // ----------------------------------------------------------------

    /**
     * Provides a shortcut notation to get a property of an entity.
     * Instead of writing
     * <code>entity.getProperty('propertyName')</code>
     * You can use the shortcut
     * <code>entity['propertyName']</code>
     */
    static Object getAt(Entity entity, String name) {
        entity.getProperty(name)
    }

    /**
     * Provides a shortcut notation to get a property of an entity.
     * Instead of writing
     * <code>entity.getProperty('propertyName')</code>
     * You can use the shortcut
     * <code>entity.propertyName</code>
     */
    static Object get(Entity entity, String name) {
        entity.getProperty(name)
    }

    /**
     * Provides a shortcut notation to set a property of an entity.
     * Instead of writing
     * <code>entity.setProperty('propertyName', value)</code>
     * You can use the shortcut
     * <code>entity.propertyName = value</code>
     * Or
     * <code>entity['propertyName'] = value</code>
     */
    static Object setAt(Entity entity, String name, Object value) {
        entity.setProperty(name, value)
    }

    /**
     * Provides a shortcut notation to set a property of an entity.
     * Instead of writing
     * <code>entity.setProperty('propertyName', value)</code>
     * You can use the shortcut
     * <code>entity.propertyName = value</code>
     * Or
     * <code>entity['propertyName'] = value</code>
     */
    static Object set(Entity entity, String name, Object value) {
        entity.setProperty(name, value)
    }

    /**
     * Save this entity in the data store.
     * Usage: <code>entity.save()</code>
     */
    static Object save(Entity entity) {
        DatastoreServiceFactory.datastoreService.put(entity)
    }

    /**
     * Delete this entity from the data store.
     * Usage: <code>entity.delete()</code>
     */
    static Object delete(Entity entity) {
        DatastoreServiceFactory.datastoreService.delete(entity.key)
    }

    /**
     * With this method, transaction handling is done transparently.
     * The transaction is committed if the closure executed properly.
     * The transaction is rollbacked if anything went wrong.
     * You can use this method as follows:
     * <code>
     * datastoreService.withTransaction { transaction ->
     *     // do something in that transaction
     * }
     * </code>
     */
    static Transaction withTransaction(DatastoreService service, Closure c) {
        Transaction transaction = service.beginTransaction()
        try {
            // pass the transaction as single parameter of the closure
            c(transaction)
            // commit the transaction if the closure executed without throwing an exception
            transaction.commit()
        } catch (e) {
            // rollback on error
            transaction.rollback()
            // rethrow the exception
            throw e
        }
    }



    /**
     * Shorcut to get the name of the Queue.
     * <p>
     * Instead of having to call <code>queue.getQueueName()</code> or <code>queue.queueName</code>,
     * you can use the syntax <code>queue.name</code> which is more concise.
     *
     * @return the name of the queue
     */
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
        def options = TaskOptions.Builder.withDefaults()
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
            } else if (key == 'method') {
                if (value instanceof TaskOptions.Method) {
                    options = options.method(value)
                } else if(value in ['GET', 'POST', 'PUT', 'DELETE', 'HEAD']) {
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
    static TaskHandle leftShift(Queue selfQueue, Map params) {
        GaelykCategory.add(selfQueue, params)
    }
}
