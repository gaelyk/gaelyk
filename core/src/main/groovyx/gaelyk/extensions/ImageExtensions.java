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
package groovyx.gaelyk.extensions;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

import java.io.File;
import java.io.IOException;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import com.google.appengine.api.images.CompositeTransform;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;

/**
 * Images service extension methods
 *
 * @author Guillaume Laforge
 */
public class ImageExtensions {
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
    public static CompositeTransform leftShift(CompositeTransform leftTransform, Transform rightTransform) {
        return leftTransform.concatenate(rightTransform);
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
    public static CompositeTransform rightShift(CompositeTransform leftTransform, Transform rightTransform) {
        return leftTransform.preConcatenate(rightTransform);
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
    public static Image getImage(byte[] byteArray) {
        return ImagesServiceFactory.makeImage(byteArray);
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
   public  static Image transform(Image selfImage, @DelegatesTo(value=ImageTransformationsBuilder.class, strategy=Closure.DELEGATE_FIRST) Closure<?> c) {
        Closure<?> clone = (Closure<?>) c.clone();
        clone.setResolveStrategy(Closure.DELEGATE_FIRST);

        // create an empty composite transform
        
        ImageTransformationsBuilder builder = new ImageTransformationsBuilder();

        clone.setDelegate(builder);

        // calculate a combined transform
        clone.call();

        // apply the composite transform and generate the resulting image
        return ImagesServiceFactory.getImagesService().applyTransform(builder.compTransf, selfImage);
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
    public static Image resize(Image selfImage, int width, int height) {
        return ImagesServiceFactory.getImagesService().applyTransform(ImagesServiceFactory.makeResize(width, height), selfImage);
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
    public static Image crop(Image selfImage, double leftX, double topY, double rightX, double bottomY) {
        return ImagesServiceFactory.getImagesService().applyTransform(ImagesServiceFactory.makeCrop(leftX, topY, rightX, bottomY), selfImage);
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
    public static Image horizontalFlip(Image selfImage) {
        return ImagesServiceFactory.getImagesService().applyTransform(ImagesServiceFactory.makeHorizontalFlip(), selfImage);
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
    public static Image verticalFlip(Image selfImage) {
        return ImagesServiceFactory.getImagesService().applyTransform(ImagesServiceFactory.makeVerticalFlip(), selfImage);
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
    public static Image rotate(Image selfImage, int degrees) {
        return ImagesServiceFactory.getImagesService().applyTransform(ImagesServiceFactory.makeRotate(degrees), selfImage);
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
    public static Image imFeelingLucky(Image selfImage) {
        return ImagesServiceFactory.getImagesService().applyTransform(ImagesServiceFactory.makeImFeelingLucky(), selfImage);
    }

    /**
     * Create an image from a file.
     *
     * @param f PNG or JPEG file
     * @return an instance of <code>Image</code>
     * @throws IOException 
     */
    public static Image getImage(File f) throws IOException {
        return ImagesServiceFactory.makeImage(ResourceGroovyMethods.getBytes(f));
    }
}
