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
package groovyx.gaelyk.logging

/**
 * Logger accessor to access a logger identified by its name.
 * The logger accessor is available in the binding of groovlets and templates under the <code>logger</code> variable.
 * Thus inside groovlets and templates, you can do:
 * <pre><code>
 *  logger.myLogger.info "an info message"
 *  logger["com.foo.bar"].info "an info message"
 * </code></pre>
 *
 * @author Guillaume Laforge
 */
class LoggerAccessor {

    /**
     * Retrieve a logger by its name using the subscript syntax: <code>logger['logName']</code>.
     *
     * @param loggerName the name of the logger to retrieve
     * @return the logger identified by its name
     */
    def getAt(String loggerName) {
        new GroovyLogger(loggerName)
    }

    /**
     * Retrieve a logger by its name using the property access notation: <code>logger.logName</code>.
     *
     * @param loggerName the name of the logger to retrieve
     * @return the logger identified by its name
     */
    def getProperty(String loggerName) {
        getAt(loggerName)
    }
}