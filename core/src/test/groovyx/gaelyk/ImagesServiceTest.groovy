package groovyx.gaelyk

import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalImagesServiceTestConfig
import com.google.appengine.api.images.Transform
import com.google.appengine.api.images.CompositeTransform
import com.google.appengine.api.images.Image
import com.google.appengine.api.images.Composite
import com.google.appengine.api.blobstore.BlobKey

/**
 * Tests for the various enhancements to the ImagesService
 *
 * @author Guillaume Laforge
 */
class ImagesServiceTest extends GroovyTestCase {
    // setup the local environement so the NamespaceManager is initialized
    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalImagesServiceTestConfig()
    )

    protected void setUp() {
        super.setUp()
        helper.setUp()
    }

    protected void tearDown() {
        super.tearDown()
        helper.tearDown()
    }

    void testImagesServiceFactoryMethodsOnImagesServiceWrapper() {
        def wrapper = ImagesServiceWrapper.instance

        def resizeT = wrapper.makeResize(100, 100)
        def flipT = wrapper.makeVerticalFlip()

        assert resizeT instanceof Transform
        assert flipT instanceof Transform

        assert wrapper.makeHorizontalFlip() instanceof Transform
        assert wrapper.makeCrop(0.0, 0.0, 1.0, 1.0) instanceof Transform
        assert wrapper.makeRotate(90) instanceof Transform
        assert wrapper.makeImFeelingLucky() instanceof Transform

        assert wrapper.makeCompositeTransform() instanceof CompositeTransform
        assert wrapper.makeCompositeTransform([resizeT, flipT]) instanceof CompositeTransform

        def stubImage = [] as Image
        assert wrapper.makeComposite(stubImage, 10, 20, 0.5, Composite.Anchor.BOTTOM_LEFT) instanceof Composite

        def stubBlobKey = new BlobKey("key")
        assert wrapper.makeImageFromBlob(stubBlobKey) instanceof Image

        byte[] bytes = [1, 2, 3, 4]
        assert wrapper.makeImage(bytes) instanceof Image
    }
}
