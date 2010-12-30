package groovyx.gaelyk

import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.api.taskqueue.QueueFactory

/**
 * 
 * @author Guillaume Laforge
 */
class QueueAccessorTest extends GroovyTestCase {

    // setup the local environement stub services
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalTaskQueueTestConfig(),
    )

    protected void setUp() {
        super.setUp()

        // setting up the local environment
        helper.setUp()
    }

    protected void tearDown() {
        // uninstalling the local environment
        helper.tearDown()

        super.tearDown()
    }


    void testAccessor() {
        def queues = new QueueAccessor()

        assert queues.default == QueueFactory.defaultQueue
        assert queues.myQueue == QueueFactory.getQueue('myQueue')

        assert queues['default'] == QueueFactory.defaultQueue
        assert queues['myQueue'] == QueueFactory.getQueue('myQueue')
    }
}
