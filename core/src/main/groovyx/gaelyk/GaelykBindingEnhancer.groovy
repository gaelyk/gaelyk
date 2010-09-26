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
package groovyx.gaelyk

import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.mail.MailServiceFactory
import com.google.appengine.api.memcache.MemcacheServiceFactory
import com.google.appengine.api.urlfetch.URLFetchServiceFactory
import com.google.appengine.api.users.UserService
import com.google.appengine.api.users.UserServiceFactory
import com.google.appengine.api.labs.taskqueue.QueueFactory
import com.google.appengine.api.xmpp.XMPPServiceFactory
import com.google.appengine.api.blobstore.BlobstoreServiceFactory
import com.google.appengine.api.utils.SystemProperty
import groovyx.gaelyk.logging.LoggerAccessor
import com.google.appengine.api.oauth.OAuthServiceFactory
import com.google.appengine.api.NamespaceManager

/**
 * Class responsible for adding adding Google App Engine related services into the binding of Groovlets and Templates.
 *
 * @author Marcel Overdijk
 * @author Guillaume Laforge
 */
class GaelykBindingEnhancer {

    /**
     * Bind the various Google App Engine services and variables
     *
     * @param binding Binding in which to bind the GAE services and variables
     */
    static void bind(Binding binding) {
        // bind google app engine services
        binding.setVariable("datastore", DatastoreServiceFactory.datastoreService)
        binding.setVariable("memcache", MemcacheServiceFactory.memcacheService)
        binding.setVariable("urlFetch", URLFetchServiceFactory.URLFetchService)
        binding.setVariable("mail", MailServiceFactory.mailService)
        binding.setVariable("images", ImagesServiceWrapper.instance)
        
        // bind user service and current user
        UserService userService = UserServiceFactory.userService
        binding.setVariable("users", userService)
        binding.setVariable("user", userService.getCurrentUser())

        // New in GAE SDK 1.2.5: task queues
        binding.setVariable("defaultQueue", QueueFactory.defaultQueue)
        binding.setVariable("queues", new QueueAccessor())

        // New in GAE SDK 1.2.5: XMPP support
        binding.setVariable("xmpp", XMPPServiceFactory.XMPPService)

        // Tells whether the application is running in local development mode
        // or is deployed on Google's cloud
        binding.setVariable("localMode", SystemProperty.environment.value() == SystemProperty.Environment.Value.Development)

        // New in GAE SDK 1.3.0: blobstore support
        binding.setVariable("blobstore", BlobstoreServiceFactory.blobstoreService)

        // Since GAE SDK 1.3.3.1: special system properties
        binding.setVariable("app", [
                env: [
                        name: SystemProperty.environment.value(),
                        version: SystemProperty.version.get(),
                ],
                id: SystemProperty.applicationId.get(),
                version: SystemProperty.applicationVersion.get()
        ])

        // Add a logger variable to easily access any logger
        binding.setVariable("logger", new LoggerAccessor())

        binding.setVariable("oauth", OAuthServiceFactory.OAuthService)

        // Namespace added in SDK 1.3.7
        binding.setVariable("namespace", NamespaceManager)

    }
}
