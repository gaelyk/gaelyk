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
import com.google.appengine.api.files.FileService
import com.google.appengine.api.backends.BackendService
import com.google.appengine.api.LifecycleManager
import com.google.appengine.api.taskqueue.Queue

/**
 * @author Vladimir Orany
 * @author Guillaume Laforge
 */
class BindingsInjectionInClassesTest extends GroovyTestCase {

    void testInjection() {
        def obj = new GroovyShell().evaluate '''
			import groovyx.gaelyk.GaelykBindings

			@GaelykBindings
			class TestEnhanced {}
			new TestEnhanced()
		'''

        [
                datastore		: DatastoreService,
                memcache		: MemcacheService,
                urlFetch		: URLFetchService,
                mail			: MailService,
                images  		: ImagesServiceWrapper,
                users			: UserService,
                user			: User,
                defaultQueue	: Queue,
                queues  		: QueueAccessor,
                xmpp			: XMPPService,
                localMode		: Boolean,
                blobstore		: BlobstoreService,
                app 			: Map,
                logger  		: LoggerAccessor,
                oauth			: OAuthService,
                namespace		: Class,
                capabilities	: CapabilitiesService,
                channel 		: ChannelService,
                files			: FileService,
                backends		: BackendService,
                lifecycle		: LifecycleManager

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
