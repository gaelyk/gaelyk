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

import groovy.transform.CompileStatic

import java.util.logging.*

/**
 * Simple wrapper for <code>java.util.logging.Logger</code>.
 * This logger can log correct class/method names even if used in Groovy.
 * <p>
 * Implementation idea found <a href="http://d.hatena.ne.jp/ksky/20100517/p1">here</a>.
 *
 * @author ksky@jggug.org (original implementation)
 * @author Guillaume Laforge (minor groovyfications and comments)
 */
class GroovyLogger {
    /** list of packages we're not interested in */
    static final EXCLUDE_LIST = [
            // ignore the GroovyLogger class itself
            GroovyLogger.class.name,
            // ignore the Groovy classes
            "groovy.", "org.codehaus.groovy.", "gjdk.groovy.",
            // ignore the JDK classes
            "java.", "javax.", "sun.",
            // Ignore Google App Engine logs
            "com.google.appengine.",
            "com.google.apphosting."
    ]

    /** The underlying logger used for logging */
    @Delegate Logger logger

    /** If the logger is for a groovlet or template */
    private boolean groovletOrTemplate

    /**
     * Groovy-friendly logger.
     *
     * @param name the name of the logger
     * @param groovletOrTemplate false by default, but can be set to true to say it's a logger for templates or groovlets
     */
    // TODO @CompileStatic
    GroovyLogger(String name, boolean groovletOrTemplate = false) {
        logger = Logger.getLogger(name)
        this.groovletOrTemplate = groovletOrTemplate
    }

    @CompileStatic
    static GroovyLogger forTemplateUri(String uri) {
        new GroovyLogger("gaelyk.template.${uri[1..<uri.lastIndexOf('.')].replaceAll(/\//, '.')}", true)
    }

    @CompileStatic
    static GroovyLogger forGroovletUri(String uri) {
        new GroovyLogger("gaelyk.groovlet.${uri[1..<uri.lastIndexOf('.')].replaceAll(/\//, '.') - 'WEB-INF.groovy.'}", true)
    }

    /**
     * @return the name of the logger
     */
    @CompileStatic
    String getName() { logger.name }

    /**
     * @return the name of the logger
     */
    @CompileStatic
    String toString() { "[GroovyLogger $logger.name" }

    /**
     * Log a message
     *
     * @param level the level of logging
     * @param msg the message to log
     */
    @CompileStatic
    protected void log(Level level, String msg) {
        if (groovletOrTemplate) {
            // if it's a Groovlet or Template, just use the logger name
            // as otherwise, the class name and method name aren't really significant
            // as they are just generated groovy scripts
           logger.logp(level, logger.name, "", msg)
        } else {
            def stack = Thread.currentThread().stackTrace
            // find the proper caller class and method
            def caller = stack.find { StackTraceElement elem ->
                EXCLUDE_LIST.every { String exclude -> !elem.className.startsWith(exclude) }
            }
            logger.logp(level, caller.className, caller.methodName, msg)
        }
    }

    @CompileStatic void severe (String msg) { log(Level.SEVERE,  msg) }
    @CompileStatic void warning(String msg) { log(Level.WARNING, msg) }
    @CompileStatic void info   (String msg) { log(Level.INFO,    msg) }
    @CompileStatic void config (String msg) { log(Level.CONFIG,  msg) }
    @CompileStatic void fine   (String msg) { log(Level.FINE,    msg) }
    @CompileStatic void finer  (String msg) { log(Level.FINER,   msg) }
    @CompileStatic void finest (String msg) { log(Level.FINEST,  msg) }
}