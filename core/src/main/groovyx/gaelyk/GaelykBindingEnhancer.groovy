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

import com.google.appengine.api.NamespaceManager
import com.google.appengine.api.blobstore.BlobstoreServiceFactory
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.taskqueue.QueueFactory
import com.google.appengine.api.mail.MailServiceFactory
import com.google.appengine.api.memcache.MemcacheServiceFactory
import com.google.appengine.api.oauth.OAuthServiceFactory
import com.google.appengine.api.urlfetch.URLFetchServiceFactory
import com.google.appengine.api.users.UserServiceFactory
import com.google.appengine.api.utils.SystemProperty
import com.google.appengine.api.xmpp.XMPPServiceFactory

import groovyx.gaelyk.logging.LoggerAccessor
import com.google.appengine.api.channel.ChannelServiceFactory
import com.google.appengine.api.LifecycleManager
import com.google.appengine.api.users.User
import com.google.appengine.api.prospectivesearch.ProspectiveSearchServiceFactory
import com.google.appengine.api.log.LogServiceFactory
import com.google.appengine.api.search.SearchServiceFactory
import javax.servlet.http.HttpServletRequest
import groovy.transform.CompileStatic

/**
 * Class responsible for adding adding Google App Engine related services into the binding of Groovlets and Templates.
 *
 * @author Marcel Overdijk
 * @author Guillaume Laforge
 * @author Benjamin Muschko
 */
@CompileStatic
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
        binding.setVariable("users", UserServiceFactory.userService)
        binding.setVariable("user", getCurrentUser())

        // New in GAE SDK 1.2.5: task queues
        binding.setVariable("defaultQueue", QueueFactory.defaultQueue)
        binding.setVariable("queues", getQueues())

        // New in GAE SDK 1.2.5: XMPP support
        binding.setVariable("xmpp", XMPPServiceFactory.XMPPService)

        // Tells whether the application is running in local development mode
        // or is deployed on Google's cloud
        binding.setVariable("localMode", getLocalMode())

        // New in GAE SDK 1.3.0: blobstore support
        binding.setVariable("blobstore", BlobstoreServiceFactory.blobstoreService)

        // Since GAE SDK 1.3.3.1: special system properties
        binding.setVariable("app", getApp())

        // Add a logger variable to easily access any logger
        binding.setVariable("logger", getLogger())

        binding.setVariable("oauth", OAuthServiceFactory.OAuthService)

        // Namespace added in SDK 1.3.7
        binding.setVariable("namespace", getNamespaceManager())

        // Capabilities service to know the status of the various GAE services
        binding.setVariable("capabilities", CapabilitiesServiceFactory.capabilitiesService)

        // Channel service in SDK 1.4.0 for Comet-style applications
        binding.setVariable("channel", ChannelServiceFactory.channelService)

        // Backend service and Lifecycle manager in SDK 1.5.0
        binding.setVariable("lifecycle", LifecycleManager.instance)

        // Prospective search service in SDK 1.5.4
        binding.setVariable("prospectiveSearch", ProspectiveSearchServiceFactory.prospectiveSearchService)

        // Log service in SDK 1.6
        binding.setVariable("logService", LogServiceFactory.logService)

        // Search service in SDK 1.6.6
        binding.setVariable("search", SearchServiceFactory.searchService)

        // Geo headers
        if(binding.hasVariable('request')) {
            def req = binding.getVariable('request')
            if(req instanceof HttpServletRequest){
                def latlong = req.getHeader('X-AppEngine-CityLatLong')?.split(',').collect { String s -> s.toBigDecimal() } ?: [0,0]
                binding.setVariable("geo", [
                        country: req.getHeader('X-AppEngine-Country'),
                        region:  req.getHeader('X-AppEngine-Region'),
                        city:    req.getHeader('X-AppEngine-City'),
                        latitude: latlong[0],
                        longitude: latlong[1],
                ])
            }
            if (!binding.hasVariable('session') || binding.hasVariable('session') && !binding.getVariable('session')) {
                binding.setVariable('session', new LazySession(req as HttpServletRequest));
            }
        }
    }

    static User getCurrentUser() {
        UserServiceFactory.userService?.currentUser
    }

    static QueueAccessor getQueues() {
        new QueueAccessor()
    }

    static Boolean getLocalMode() {
        SystemProperty.environment.value() == SystemProperty.Environment.Value.Development
    }

    static Map getApp() {
        [
            env: [
                name: SystemProperty.environment.value(),
                version: SystemProperty.version.get(),
            ],
            gaelyk: [
                version: '2.0'
            ],
            id: SystemProperty.applicationId.get(),
            version: SystemProperty.applicationVersion.get()
        ]
    }

    static LoggerAccessor getLogger() {
        new LoggerAccessor()
    }

    static Class getNamespaceManager() {
        NamespaceManager
    }
}
