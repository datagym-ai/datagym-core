package ai.datagym.application.export.util;

import ai.datagym.application.labelIteration.entity.geometry.PointCollection;
import ai.datagym.application.labelIteration.entity.geometry.PointPojo;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class SegmentationBitmap {

    public static final int COLOR_BLACK = 0;
    public static final int COLOR_WHITE = 255;

    /**
     * Some default constants to setup the deprecated ctr.
     */
    public static final int IMAGE_GRAY_SCALED = CvType.CV_8UC1;
    public static final int IMAGE_3_CHANNEL = CvType.CV_8UC3;
    public static final int IMAGE_4_CHANNEL = CvType.CV_8UC4;

    private final Mat src;
    private final Scalar white = Scalar.all(COLOR_WHITE);
    private final Scalar black = Scalar.all(COLOR_BLACK);
    private final List<MatOfPoint> outerPoints = new LinkedList<>();
    private final List<MatOfPoint> innerPoints = new LinkedList<>();

    public SegmentationBitmap(int height, int width) {
        loadLibrary();
        src = Mat.zeros(height, width, CvType.CV_8UC1);
    }

    /**
     * @deprecated for development only.
     */
    @Deprecated
    public SegmentationBitmap(int height, int width, int imageType) {
        loadLibrary();
        src = Mat.zeros(height, width, imageType);
    }

    /**
     * @deprecated for development only.
     */
    @Deprecated
    public SegmentationBitmap writeFile(String fileName) {
        loadLibrary();
        Imgcodecs.imwrite(fileName + ".bmp", src);
        return this;
    }

    private static boolean hasLibraryLoaded = false;

    /**
     * Before using {@link ai.datagym.application.export.util.SegmentationBitmap}, call this static method once to
     * load the openCV library. Note this will cause some warnings at startup.
     */
    public static void loadLibrary() {
        if (hasLibraryLoaded) {
            return;
        }
        /*
         * Attempt to load the native library. If the first attempt fails,
         * the native binary will be extracted from the classpath to a temporary location
         * (which gets cleaned up on shutdown) and then another call to load the library is made.
         *
         * Note this method uses reflection to gain access to private memory in {@link ClassLoader}
         * as there's no documented method to augment the library path at runtime.
         *
         * Produces some warnings at startup: 'An illegal reflective access operation has occurred'.
         */
        nu.pattern.OpenCV.loadShared();
        hasLibraryLoaded = true;
    }

    public SegmentationBitmap addSegmentations(List<List<PointCollection>> pointCollections) {
        for (final List<PointCollection> pointCollection : pointCollections) {
            addSegmentation(pointCollection);
        }
        return this;
    }

    public SegmentationBitmap addSegmentation(List<PointCollection> pointCollections) {
        int index = 0;
        for (final PointCollection collection : pointCollections) {
            List<PointPojo> pointListList = collection.getPoints();

            Point[] pointsList = pointListList.stream().map(pointPojo -> new Point(
                    pointPojo.getX(),
                    pointPojo.getY()
            )).toArray(Point[]::new);

            MatOfPoint points = new MatOfPoint(pointsList);

            if (index == 0) {
                outerPoints.add(points);
            } else {
                innerPoints.add(points);
            }

            index++;
        }

        return this;
    }

    public SegmentationBitmap apply() {
        Imgproc.fillPoly(src, outerPoints, white);
        Imgproc.fillPoly(src, innerPoints, black);

        outerPoints.forEach(Mat::release);
        innerPoints.forEach(Mat::release);

        innerPoints.clear();
        outerPoints.clear();

        return this;
    }

    public byte[] getBytes() {
        //Encoding the image
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".bmp", src, matOfByte);
        //Storing the encoded Mat in a byte array
        return matOfByte.toArray();
    }

    public BufferedImage asBufferedImage() throws IOException {
        byte[] segmentationBitmap = getBytes();
        InputStream targetStream = new ByteArrayInputStream(segmentationBitmap);
        return ImageIO.read(targetStream);
    }
}
