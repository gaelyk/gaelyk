package groovyx.gaelyk

import com.google.appengine.api.images.ImagesServiceFactory
import com.google.appengine.api.images.ImagesService

/**
 * Wrapper class that provides all the methods of both the <code>ImageService</code>
 * and <code>ImageServiceFactory</code>, to conveniently use only one instance to do all the image operations.
 *
 * @author Guillaume Laforge
 */
class ImagesServiceWrapper {
    @Delegate ImagesService service = ImagesServiceFactory.getImagesService()

    def methodMissing(String name, args) {
        ImagesServiceFactory."$name"(*args)
    }
}
