package groovyx.gaelyk.extensions;

import com.google.appengine.api.images.CompositeTransform;
import com.google.appengine.api.images.ImagesServiceFactory;

public class ImageTransformationsBuilder {
    public final CompositeTransform compTransf = ImagesServiceFactory.makeCompositeTransform();
    public final boolean lucky                 = true;
    public final boolean flip                  = true;
    
    public CompositeTransform resize(int width, int height)                                    { return ImageExtensions.leftShift(compTransf, ImagesServiceFactory.makeResize(width, height)); }
    public CompositeTransform crop(double leftX, double topY, double rightX, double bottomY)   { return ImageExtensions.leftShift(compTransf, ImagesServiceFactory.makeCrop(leftX, topY, rightX, bottomY)); }
    public CompositeTransform horizontal(boolean flip)                                         { return ImageExtensions.leftShift(compTransf, ImagesServiceFactory.makeHorizontalFlip()); }
    public CompositeTransform vertical(boolean flip)                                           { return ImageExtensions.leftShift(compTransf, ImagesServiceFactory.makeVerticalFlip()); }
    public CompositeTransform rotate(int degrees)                                              { return ImageExtensions.leftShift(compTransf, ImagesServiceFactory.makeRotate(degrees)); }
    public CompositeTransform feeling(boolean lucky)                                           { return ImageExtensions.leftShift(compTransf, ImagesServiceFactory.makeImFeelingLucky()); }
    
}