/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
class ImagesServiceWrapper {

    @Delegate(deprecated = true) ImagesService service = ImagesServiceFactory.imagesService

    def methodMissing(String name, args) {
        // special case for makeImage which takes byte[] as param
        // for some reason, we have to force coercion to byte array
        if (name == "makeImage") {
            return ImagesServiceFactory.makeImage(args[0] as byte[])
        }

        ImagesServiceFactory."$name"(*args)
    }
}
