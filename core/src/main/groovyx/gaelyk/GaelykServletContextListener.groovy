/*
 * Copyright 2011 the original author or authors.
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

import javax.servlet.ServletContextListener
import javax.servlet.ServletContextEvent
import groovyx.gaelyk.plugins.PluginsHandler

/**
 * Servlet context listener configured on startup for calling the initialization
 * routines of the plugin system.
 *
 * @author Guillaume Laforge
 * @author Marcin Erdmann
 */
class GaelykServletContextListener implements ServletContextListener {

    /**
     * Initialize the plugin system when the context of the application is created
     * @param servletContextEvent
     */
    void contextInitialized(ServletContextEvent servletContextEvent) {
        PluginsHandler.instance.initPlugins(servletContextEvent.servletContext)
    }

    void contextDestroyed(ServletContextEvent servletContextEvent) {
        // nothing special to be done
    }
}
