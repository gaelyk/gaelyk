package groovyx.gaelyk

import com.google.appengine.api.images.ImagesServiceFactory
import com.google.appengine.api.images.ImagesService

/**
 * Wrapper class that provides all the methods of both the <code>ImageService</code>
 * and <code>ImageServiceFactory</code>, to conveniently use only one instance to do all the image operations.
 *
 * @author Guillaume Laforge
 */
@Singleton
class ImagesServiceWrapper implements ImagesService {
    @Delegate ImagesService service = ImagesServiceFactory.imagesService

    def methodMissing(String name, args) {
        // special case for makeImage which takes byte[] as param
        // for some reason, we have to force coercion to byte array
        if (name == "makeImage") {
            return ImagesServiceFactory.makeImage(args[0] as byte[])
        }

        ImagesServiceFactory."$name"(*args)
    }
}
