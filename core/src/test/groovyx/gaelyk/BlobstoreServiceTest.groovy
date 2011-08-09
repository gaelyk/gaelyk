package groovyx.gaelyk

import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalFileServiceTestConfig
import com.google.appengine.api.files.FileServiceFactory
import com.google.appengine.api.blobstore.ByteRange
import com.google.appengine.api.files.FinalizationException
import com.google.appengine.tools.development.testing.LocalImagesServiceTestConfig
import com.google.appengine.api.images.Image.Format
import javax.servlet.http.HttpServletResponse
import com.google.appengine.api.blobstore.BlobstoreServicePb.BlobstoreService
import com.google.appengine.api.blobstore.BlobstoreServiceFactory
import com.google.appengine.api.blobstore.BlobInfo

/**
 * Blobstore and File services related tests
 *
 * @author Guillaume Laforge
 */
class BlobstoreServiceTest extends GroovyTestCase {
    // setup the local environement stub services
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalBlobstoreServiceTestConfig(),
            new LocalFileServiceTestConfig(),
            new LocalImagesServiceTestConfig()
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

    void testCreateBlobAndReadContent() {
        def files = FileServiceFactory.fileService

        use(GaelykCategory) {
            def file = files.createNewBlobFile("text/plain", "todo.txt")
            file.withWriter(locked: true, finalize: true) { writer ->
                writer << "Do the washing-up"
            }

            def key = file.blobKey

            assert key.filename == "todo.txt"
            assert key.contentType == "text/plain"
            assert key.creation > new Date() - 1
            assert key.size == 17

            key.withReader { reader ->
                assert reader.text == "Do the washing-up"
            }

            key.delete()
        }
    }

    void testMultipleWrites() {
        def files = FileServiceFactory.fileService

        use(GaelykCategory) {
            def file = files.createNewBlobFile("text/plain", "todo2.txt")

            file.withWriter(finalize: false) { writer ->
                writer << "Switch off the lights\n"
            }

            // the file is not finalized yet, so can't access the blob key
            assert !file.blobKey

            file.withWriter { writer ->
                writer << "Do the washing-up"
            }

            try {
                file.withWriter { writer ->
                    writer << "Finalized file?"
                }
            } catch(FinalizationException fe) {
                // an finalization exception is thrown as the file was finalized,
                // so we can't append to it anymore
                assert fe
            }

            def key = file.blobKey

            // the blob key is not null as the file was finalized with the default write above
            assert key

            assert key.filename == "todo2.txt"
            assert key.contentType == "text/plain"
            assert key.creation > new Date() - 1
            assert key.size == 39

            key.withReader { reader ->
                assert reader.text == "Switch off the lights\nDo the washing-up"
            }

            key.delete()
        }
    }

    void testCreateBlobAndReadBinaryContent() {
        def files = FileServiceFactory.fileService

        use(GaelykCategory) {
            def file = files.createNewBlobFile("application/octet-stream", "dummy.bin")

            file.withOutputStream(locked: true, finalize: false) { stream ->
                stream << "abcdefghij".bytes
            }

            file.withOutputStream { stream ->
                stream << "0123456789".bytes
            }

            def key = file.blobKey

            assert key.filename == "dummy.bin"
            assert key.contentType == "application/octet-stream"
            assert key.creation > new Date() - 1
            assert key.size == 20

            key.withStream { InputStream stream ->
                assert stream.newReader().text == "abcdefghij0123456789"
            }

            assert new String(key.fetchData(12, 14)) == "234"
            assert new String(key.fetchData(2..4)) == "cde"
            assert new String(key.fetchData(8..14 as ByteRange)) == "ij01234"

            file.delete()
        }
    }

    void testFileFromPathShortcut() {
        def files = FileServiceFactory.fileService

        use(GaelykCategory) {
            def file = files.createNewBlobFile("text/plain", "counting.txt")
            file.withWriter { writer ->
                writer << "One, two, three"
            }

            def path = file.fullPath

            def secondFile = files.fromPath(path)
            secondFile.blobKey.withReader { reader ->
                assert reader.text == "One, two, three"
            }

            file.delete()

            def thirdFile = files.fromPath(path)
            assert thirdFile.fullPath == path
        }
    }

    void testBlobImage() {
        def files = FileServiceFactory.fileService

        use(GaelykCategory) {
            def file = files.createNewBlobFile("application/octet-stream", "dummy.bin")

            def img = new File('../graphics/gaelyk-small-favicon.png')
            byte[] bytes = img.bytes

            file.withOutputStream { stream ->
                stream << bytes
            }

            def key = file.blobKey

            assert key.image

            // currently failing but should not
            // till the local image service allows to retrieve image metadata
            // from the blobstore stored images
            shouldFail {
                assert key.image.imageData == bytes
                assert key.image.format == Format.PNG
                assert key.image.width == 32
                assert key.image.height == 32
            }
        }
    }

    void testServeMethods() {
        def files = FileServiceFactory.fileService

        use(GaelykCategory) {
            def file = files.createNewBlobFile("application/octet-stream", "dummy.bin")

            def img = new File('../graphics/gaelyk-small-favicon.png')
            byte[] bytes = img.bytes

            file.withOutputStream { stream ->
                stream << bytes
            }

            def key = file.blobKey

            def response = [
                    isCommitted: { -> false },
                    setStatus: { int status -> assert status == 200 },
                    setHeader: { String name, String value ->
                        if (name == "X-AppEngine-BlobKey") {
                            assert value == key.keyString
                        } else if (name == "X-AppEngine-BlobRange") {
                            assert value == "bytes=0-10"
                        } else {
                            throw new RuntimeException("Didn't expect key $name and value $value")
                        }
                    }
            ] as HttpServletResponse

            key.serve(response)
            key.serve(response, 0..10)
            key.serve(response, 0..10 as ByteRange)
        }
    }

    void testReadingFromAFile() {
        def files = FileServiceFactory.fileService

        use(GaelykCategory) {
            def file = files.createNewBlobFile("text/plain", "todo.txt")
            file.withWriter { writer ->
                writer << "Do the washing-up"
            }

            file.withReader { reader ->
                assert reader.text == "Do the washing-up"
            }

            file.delete()
        }
    }

    void testReadingFromAFileWithAnInputStream() {
        def files = FileServiceFactory.fileService

        use(GaelykCategory) {
            def file = files.createNewBlobFile("text/plain", "todo.txt")
            file.withOutputStream { OutputStream stream ->
                stream << "Do the washing-up".bytes
            }

            file.withInputStream { BufferedInputStream stream ->
                assert stream.newReader().text == "Do the washing-up"
            }

            file.delete()
        }
    }

    void testGetFileOnBlobKey() {
        def files = FileServiceFactory.fileService

        use(GaelykCategory) {
            def file = files.createNewBlobFile("text/plain", "dummy.txt")
            file.withOutputStream { OutputStream stream ->
                stream << "dummy".bytes
            }
            def key = file.blobKey

            assert key.file.fullPath == file.fullPath
            assert key.file.toString() == file.toString()
        }
    }
    
    void testEachAndCollectOnBlobstore() {
        def blobstore = BlobstoreServiceFactory.blobstoreService
        def files = FileServiceFactory.fileService

        use(GaelykCategory) {
            files.createNewBlobFile("text/plain", "one.txt")  .withOutputStream { it << "one" }
            files.createNewBlobFile("text/plain", "two.txt")  .withOutputStream { it << "two" }
            files.createNewBlobFile("text/plain", "three.txt").withOutputStream { it << "three" }

            def fileNames = []
            blobstore.each { BlobInfo info -> fileNames << info.filename }

            assert fileNames.sort() == ["one.txt", "two.txt", "three.txt"].sort()

            def contentTypes = blobstore.collect { BlobInfo info -> info.contentType }

            assert contentTypes.every { it == "text/plain" }
        }
    }

}
