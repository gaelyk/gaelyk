package groovyx.gaelyk.cache

import javax.servlet.http.HttpServletResponse

/**
 * @author Guillaume Laforge
 */
class CachedResponseTest extends GroovyTestCase {

    void testCachedResponseWriter() {
        def response = [:] as HttpServletResponse

        def cachedResponse = new CachedResponse(response)

        cachedResponse.writer.print "hello"
        cachedResponse.writer.flush()

        assert cachedResponse.output.toByteArray() == "hello".getBytes()

    }

    void testCachedResponseOutputStream() {
        def response = [:] as HttpServletResponse

        def cachedResponse = new CachedResponse(response)

        cachedResponse.outputStream.write "goodbye".getBytes()
        cachedResponse.outputStream.flush()
        cachedResponse.outputStream.close()

        assert cachedResponse.output.toByteArray() == "goodbye".getBytes()

    }

    void testCachedResponseOutputStreamWriteAnInt() {
        def response = [:] as HttpServletResponse

        def cachedResponse = new CachedResponse(response)

        cachedResponse.outputStream.write 17
        cachedResponse.outputStream.flush()
        cachedResponse.outputStream.close()

        assert cachedResponse.output.toByteArray()[0] == 17

    }
}
