package groovyx.gaelyk

import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig
import com.google.appengine.api.taskqueue.QueueFactory
import com.google.appengine.api.taskqueue.RetryOptions

/**
 * @author Guillaume Laforge
 */
class QueueTaskTest extends GroovyTestCase {
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalTaskQueueTestConfig()
    )

    protected void setUp() {
        super.setUp()
        helper.setUp()
    }

    protected void tearDown() {
        helper.tearDown()
        super.tearDown()
    }

    void testTaskCreation() {
        def queue = QueueFactory.defaultQueue
        queue.add countdownMillis: 1000, url: "/task/dailyEmail",
                taskName: "dailyNewsletter1",
                method: 'PUT', params: [date: '20090914'],
                payload: "some content",
                headers: [info: 'some-header-info']
        retryOptions: [
                taskRetryLimit: 10,
                taskAgeLimitSeconds: 100,
                minBackoffSeconds: 40,
                maxBackoffSeconds: 50,
                maxDoublings: 15
        ]

        queue << [
                countdownMillis: 1000, url: "/task/dailyEmail",
                taskName: "dailyNewsletter2",
                method: 'PUT', params: [date: '20090914'],
                payload: "some content", retryOptions: RetryOptions.Builder.withDefaults()
        ]
    }
    
    void testTaskCreationAsync() {
        def queue = QueueFactory.defaultQueue
        queue.addAsync countdownMillis: 1000, url: "/task/dailyEmail",
                taskName: "dailyNewsletter1",
                method: 'PUT', params: [date: '20090914'],
                payload: "some content",
                headers: [info: 'some-header-info']
        retryOptions: [
                taskRetryLimit: 10,
                taskAgeLimitSeconds: 100,
                minBackoffSeconds: 40,
                maxBackoffSeconds: 50,
                maxDoublings: 15
        ]

        queue.addAsync countdownMillis: 1000, url: "/task/dailyEmail",
                taskName: "dailyNewsletter2",
                method: 'PUT', params: [date: '20090914'],
                payload: "some content", retryOptions: RetryOptions.Builder.withDefaults()
    }
}
