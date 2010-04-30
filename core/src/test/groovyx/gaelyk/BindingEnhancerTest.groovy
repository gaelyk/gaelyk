package groovyx.gaelyk

import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.memcache.MemcacheService
import com.google.appengine.api.urlfetch.URLFetchService
import com.google.appengine.api.mail.MailService
import com.google.appengine.api.users.UserService
import com.google.appengine.api.images.ImagesService
import com.google.appengine.api.xmpp.XMPPService
import com.google.appengine.api.blobstore.BlobstoreService
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig
import com.google.appengine.tools.development.testing.LocalImagesServiceTestConfig
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig
import com.google.appengine.tools.development.testing.LocalXMPPServiceTestConfig
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig

/**
 * Test the binding enhancer binds the GAE services in the binding.
 *
 * @author Guillaume Laforge
 */
class BindingEnhancerTest extends GroovyTestCase {

    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig(),
            new LocalMemcacheServiceTestConfig(),
            new LocalURLFetchServiceTestConfig(),
            new LocalMailServiceTestConfig(),
            new LocalImagesServiceTestConfig(),
            new LocalUserServiceTestConfig(),
            new LocalTaskQueueTestConfig(),
            new LocalXMPPServiceTestConfig(),
            new LocalBlobstoreServiceTestConfig()
    )

    private Binding binding

    protected void setUp() {
        super.setUp()

        helper.setUp()

        binding = new Binding()
        new GaelykBindingEnhancer(binding).bind()
    }

    protected void tearDown() {
        helper.tearDown()

        super.tearDown()
    }



    void testVariablesPresent() {
        ["datastore", "memcache", "urlFetch", "mail",
                "images", "users", "defaultQueue", "queues",
                "xmpp", "localMode", "blobstore"].each {
            assert binding.variables.containsKey(it)
        }
    }

    void testGaeServicesPresent() {
        assert binding.datastore    instanceof DatastoreService
        assert binding.memcache     instanceof MemcacheService
        assert binding.urlFetch     instanceof URLFetchService
        assert binding.mail         instanceof MailService
        assert binding.images       instanceof ImagesService
        assert binding.users        instanceof UserService
        assert binding.defaultQueue instanceof com.google.appengine.api.labs.taskqueue.Queue
        assert binding.xmpp         instanceof XMPPService
        assert binding.blobstore    instanceof BlobstoreService
    }
}
