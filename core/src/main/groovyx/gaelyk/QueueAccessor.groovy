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
package groovyx.gaelyk

import com.google.appengine.api.taskqueue.QueueFactory
import groovy.transform.CompileStatic

/**
 * Holder for queues providing a Groovy map-like syntax for accessing queues.
 * The default queue is named 'default'.
 *
 * @author Guillaume Laforge
 */
@CompileStatic
class QueueAccessor {

    /**
     * Retrieve a queue by its name using the subscript syntax: <code>queues['queueA']</code>.
     *
     * @param queueName the name of the queue to retrieve
     * @return the queue identified by its name
     */
    def getAt(String queueName) {
        if (queueName == "default")
            QueueFactory.getDefaultQueue()
        else
            QueueFactory.getQueue(queueName)
    }

    /**
     * Retrieve a queue by its name using the property access notation: <code>queues.queueA</code>.
     *
     * @param queueName the name of the queue to retrieve
     * @return the queue identified by its name
     */
    def getProperty(String queueName) {
        getAt(queueName)
    }
}