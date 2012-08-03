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
package groovyx.gaelyk.extensions

import groovy.transform.CompileStatic
import com.google.appengine.api.images.CompositeTransform
import com.google.appengine.api.images.Transform
import com.google.appengine.api.images.Image
import com.google.appengine.api.images.ImagesServiceFactory

/**
 * Images service extension methods
 *
 * @author Guillaume Laforge
 */
class ImageExtensions {
    /**
     * Use the leftShift operator, <<, to concatenate a transform to the composite transform.
     * <pre><code>
     * def cropTransform = ...
     * def rotateTransform = ...
     *
     * croptTransform << rotateTransform
     * </code></pre>
     * @param leftTransform a transform
     * @param rightTransform another transform
     * @return a composite transform
     */
    @CompileStatic
    static CompositeTransform leftShift(CompositeTransform leftTransform, Transform rightTransform) {
        leftTransform.concatenate(rightTransform)
    }

    /**
     * Use the rightShift operator, >>, to "pre-concatenate" a transform to the composite transform.
     * <pre><code>
     * def cropTransform = ...
     * def rotateTransform = ...
     *
     * croptTransform >> rotateTransform
     * </code></pre>
     * @param leftTransform a transform
     * @param rightTransform another transform
     * @return a composite transform
     */
    @CompileStatic
    static CompositeTransform rightShift(CompositeTransform leftTransform, Transform rightTransform) {
        leftTransform.preConcatenate(rightTransform)
    }

    /**
     * Transform a byte array into an Image.
     *
     * <pre><code>
     * def byteArray = ...
     * def image = byteArray.image
     * </code></pre>
     *
     * @param byteArray a byte array
     * @return an Image
     */
    @CompileStatic
    static Image getImage(byte[] byteArray) {
        ImagesServiceFactory.makeImage(byteArray)
    }


    /**
     * Image transform DSL.
     * <pre><code>
     *  bytes.image.transform {
     *      resize 100, 100
     *      crop 0.1, 0.1, 0.9, 0.9
     *      flip horizontal
     *      flip vertical
     *      rotate 90
     *      feeling lucky
     *  }
     * </code></pre>
     *
     * @param selfImage the image to transform
     * @param c the closure containg the various transform steps
     * @return a transformed image
     */
    static Image transform(Image selfImage, Closure c) {
        Closure clone = c.clone()
        clone.resolveStrategy = Closure.DELEGATE_ONLY

        // create an empty composite transform
        CompositeTransform compTransf = ImagesServiceFactory.makeCompositeTransform()

        clone.delegate = new Expando([
                // methods
                resize:     { width, height ->                compTransf << ImagesServiceFactory.makeResize(width, height) },
                crop:       { leftX, topY, rightX, bottomY -> compTransf << ImagesServiceFactory.makeCrop(leftX, topY, rightX, bottomY) },
                horizontal: { flip ->                         compTransf << ImagesServiceFactory.makeHorizontalFlip() },
                vertical:   { flip ->                         compTransf << ImagesServiceFactory.makeVerticalFlip() },
                rotate:     { degrees ->                      compTransf << ImagesServiceFactory.makeRotate(degrees) },
                feeling:    { luck ->                         compTransf << ImagesServiceFactory.makeImFeelingLucky() },

                // variables
                lucky:          true,
                flip:           true
        ])

        // calculate a combined transform
        clone()

        // apply the composite transform and generate the resulting image
        return ImagesServiceFactory.imagesService.applyTransform(compTransf, selfImage)
    }

    /**
     * Create a new resized image.
     *
     * <pre><code>
     *  def thumbnail = image.resize(100, 100)
     * </code></pre>
     *
     * @param selfImage image to resize
     * @param width new width
     * @param height new height
     * @return a resized image
     */
    @CompileStatic
    static Image resize(Image selfImage, int width, int height) {
        ImagesServiceFactory.imagesService.applyTransform(ImagesServiceFactory.makeResize(width, height), selfImage)
    }

    /**
     * Create a new cropped image.
     *
     * <pre><code>
     *  def cropped = image.crop(0.1, 0.1, 0.9, 0.9)
     * </code></pre>
     *
     * @param selfImage image to crop
     * @param leftX
     * @param topY
     * @param rightX
     * @param bottomY
     * @return a cropped image
     */
    @CompileStatic
    static Image crop(Image selfImage, double leftX, double topY, double rightX, double bottomY) {
        ImagesServiceFactory.imagesService.applyTransform(ImagesServiceFactory.makeCrop(leftX, topY, rightX, bottomY), selfImage)
    }

    /**
     * Create a new image flipped horizontally.
     *
     * <pre><code>
     *  def himage = image.horizontalFlip()
     * </code></pre>
     *
     * @param selfImage image to flip horizontally
     * @return a flipped image
     */
    @CompileStatic
    static Image horizontalFlip(Image selfImage) {
        ImagesServiceFactory.imagesService.applyTransform(ImagesServiceFactory.makeHorizontalFlip(), selfImage)
    }

    /**
     * Create a new image flipped vertically.
     *
     * <pre><code>
     *  def vimage = image.verticalFlip()
     * </code></pre>
     *
     * @param selfImage image to flip vertically
     * @return a flipped image
     */
    @CompileStatic
    static Image verticalFlip(Image selfImage) {
        ImagesServiceFactory.imagesService.applyTransform(ImagesServiceFactory.makeVerticalFlip(), selfImage)
    }

    /**
     * Create a new rotated image.
     *
     * <pre><code>
     *  def rotated = image.rotate(90)
     * </code></pre>
     *
     * @param selfImage image to rotate
     * @param degrees number of degrees to rotate (must be a multiple of 90)
     * @return a rotated image
     */
    @CompileStatic
    static Image rotate(Image selfImage, int degrees) {
        ImagesServiceFactory.imagesService.applyTransform(ImagesServiceFactory.makeRotate(degrees), selfImage)
    }

    /**
     * Create a new image applying the "I'm feeling lucky" transformation.
     *
     * <pre><code>
     *  def adjusted = image.iAmFeelingLucky()
     * </code></pre>
     *
     * @param selfImage image to adjust
     * @return an adjusted image
     */
    @CompileStatic
    static Image imFeelingLucky(Image selfImage) {
        ImagesServiceFactory.imagesService.applyTransform(ImagesServiceFactory.makeImFeelingLucky(), selfImage)
    }

    /**
     * Create an image from a file.
     *
     * @param f PNG or JPEG file
     * @return an instance of <code>Image</code>
     */
    @CompileStatic
    static Image getImage(File f) {
        ImagesServiceFactory.makeImage((byte[])f.bytes)
    }
}
