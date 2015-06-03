package groovyx.gaelyk

import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.memcache.MemcacheService
import com.google.appengine.api.urlfetch.URLFetchService
import com.google.appengine.api.mail.MailService
import com.google.appengine.api.users.UserService
import com.google.appengine.api.users.User
import com.google.appengine.api.xmpp.XMPPService
import com.google.appengine.api.blobstore.BlobstoreService
import groovyx.gaelyk.logging.LoggerAccessor
import com.google.appengine.api.oauth.OAuthService
import com.google.appengine.api.capabilities.CapabilitiesService
import com.google.appengine.api.channel.ChannelService
import com.google.appengine.api.LifecycleManager
import com.google.appengine.api.taskqueue.Queue
import com.google.appengine.api.prospectivesearch.ProspectiveSearchService
import com.google.appengine.tools.development.testing.LocalProspectiveSearchServiceTestConfig
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalXMPPServiceTestConfig
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig
import com.google.appengine.tools.development.testing.LocalImagesServiceTestConfig
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper

/**
 * @author Vladimir Orany
 * @author Guillaume Laforge
 */
class BindingsInjectionInClassesTest extends GroovyTestCase {

    // setup the local environement stub services
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig(),
            new LocalMemcacheServiceTestConfig(),
            new LocalURLFetchServiceTestConfig(),
            new LocalMailServiceTestConfig(),
            new LocalImagesServiceTestConfig(),
            new LocalUserServiceTestConfig(),
            new LocalTaskQueueTestConfig(),
            new LocalXMPPServiceTestConfig(),
            new LocalBlobstoreServiceTestConfig(),
            new LocalProspectiveSearchServiceTestConfig()
    )

    private Binding binding

    protected void setUp() {
        super.setUp()

        // setting up the local environment
        helper.setUp()

        binding = new Binding()
        GaelykBindingEnhancer.bind(binding)
    }

    protected void tearDown() {
        // uninstalling the local environment
        try {
            helper.tearDown()
        } catch (Throwable t) {
            System.err.println("Something bad happened while tearing down the helpers (${t.message})")
            t.printStackTrace()
        }

        super.tearDown()
    }

    void testInjection() {
        def obj = new GroovyShell().evaluate '''
			import groovyx.gaelyk.GaelykBindings

			@GaelykBindings
			class TestEnhanced {}
			new TestEnhanced()
		'''

        [
                datastore		  : DatastoreService,
                memcache		  : MemcacheService,
                urlFetch		  : URLFetchService,
                mail			  : MailService,
                images  		  : ImagesServiceWrapper,
                users			  : UserService,
                user			  : User,
                defaultQueue	  : Queue,
                queues  		  : QueueAccessor,
                xmpp			  : XMPPService,
                localMode		  : Boolean,
                blobstore		  : BlobstoreService,
                app 			  : Map,
                logger  		  : LoggerAccessor,
                oauth			  : OAuthService,
                namespace		  : Class,
                capabilities	  : CapabilitiesService,
                channel 		  : ChannelService,
                prospectiveSearch : ProspectiveSearchService,
                lifecycle		  : LifecycleManager

        ].each { property, clazz ->
            assert obj.metaClass.getMetaProperty(property)?.type == clazz
        }
    }

    void testExistingPropertyShouldntBeOverriden() {
        def obj = new GroovyShell().evaluate '''
			@groovyx.gaelyk.GaelykBindings
			class TestEnhanced {
				Map datastore
			}
			new TestEnhanced()
		'''

		assert obj.hasProperty('datastore')
		assert obj.metaClass.getMetaProperty('datastore').getType() == Map
    }
}
