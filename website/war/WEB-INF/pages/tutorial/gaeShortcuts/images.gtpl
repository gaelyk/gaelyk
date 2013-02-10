<a name="images"></a>
<h2>Images service enhancements</h2>

<a name="images-wrapper"></a>
<h3>The images service and service factory wrapper</h3>

<p>
The Google App Engine SDK is providing two classes for handling images:
</p>

<ul>
    <li>
        <code><a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/images/ImagesServiceFactory.html">ImageServiceFactory</a></code>
        is used to retrieve the Images service, to create images (from blobs, byte arrays), and to make transformation operations.
    </li>
    <li>
        <code><a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/images/ImagesService.html">ImageService</a></code>
        is used for applying transforms to images, create composite images, serve images, etc.
    </li>
</ul>

<p>
Very quickly, as you use the images handling capabilities of the API,
you quickly end up jumping between the factory and the service class all the time.
But thanks to <b>Gaelyk</b>, both <code>ImagesServiceFactory</code> and <code>ImagesService</code> are combined into one.
So you can call any method on either of them on the same <code>images</code> instance available in your groovlets and templates.
</p>

<pre class="brush:groovy">
    // retrieve an image stored in the blobstore
    def image = images.makeImageFromBlob(blob)

    // apply a resize transform on the image to create a thumbnail
    def thumbnail = images.applyTransform(images.makeResize(260, 260), image)

    // serve the binary data of the image to the servlet output stream
    sout << thumbnail.imageData
</pre>

<p>
On the first line above, we created the image out of the blobstore using the images service,
but there is also a more rapid shortcut for retrieving an image when given a blob key:
</p>

<pre class="brush:groovy">
    def blobKey = ...
    def image = blobKey.image
</pre>

<blockquote>
    <b>Note: </b> <code>blobKey.image</code> creates an image object with only the blob key set.
    It's not retrieving the actual image right away, nor its properties like its dimensions.
    See this Gaelyk <a href="https://github.com/glaforge/gaelyk/issues/161">issue</a> for more information,
    or this Google App Engine <a href="http://code.google.com/p/googleappengine/issues/detail?id=5452">issue</a>.
</blockquote>

<p>
In case you have a file or a byte array representing your image, you can also easily instanciate an <code>Image</code> with:
</p>

<pre class="brush:groovy">
    // from a byte array
    byte[] byteArray = ...
    def image = byteArray.image

    // from a file directly
    image = new File('/images/myimg.png').image
</pre>

<a name="image-dsl"></a>
<h3>An image manipulation language</h3>

<p>
The images service permits the manipulation of images by applying various transforms,
like resize, crop, flip (vertically or horizontally), rotate, and even an "I'm feeling lucky" transform!
The <b>Gaelyk</b> image manipulation DSL allows to simplify the combination of such operations=
</p>

<pre class="brush:groovy">
    blobKey.image.transform {
        resize 100, 100
        crop 0.1, 0.1, 0.9, 0.9
        horizontal flip
        vertical flip
        rotate 90
        feeling lucky
    }
</pre>

<p>
The benefit of this approach is that transforms are combined within a single composite transform,
which will be applied in one row to the original image, thus saving on CPU computation.
But if you just need to make one transform, you can also call new methods on <code>Image</code> as follows:
</p>

<pre class="brush:groovy">
    def image = ...

    def thumbnail   = image.resize(100, 100)
    def cropped     = image.crop(0.1, 0.1, 0.9, 0.9)
    def hmirror     = image.horizontalFlip()
    def vmirror     = image.verticalFlip()
    def rotated     = image.rotate(90)
    def lucky       = image.imFeelingLucky()
</pre>
