package ai.datagym.application.export.util;

import ai.datagym.application.labelIteration.entity.geometry.PointCollection;
import ai.datagym.application.labelIteration.entity.geometry.PointPojo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Disable tempoary because of the opencv error on jenkins
@Disabled
public class SegmentationBitmapTest {

    final int width = 50;
    final int height = 50;

    SegmentationBitmap underTest;

    @BeforeEach
    public void testSetUp() {
        nu.pattern.OpenCV.loadShared();
        underTest = new SegmentationBitmap(height, width);
    }

    @Test
    public void testAddSegmentationsResponseShouldBeUnderTest() {

        // Given
        List<List<PointCollection>> emptyList = new LinkedList<>();

        // Then
        SegmentationBitmap response = underTest.addSegmentations(emptyList);

        assertNotNull(response);
        assertEquals(underTest, response);
    }

    @Test
    public void testAddSegmentationsListIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            // Given
            List<List<PointCollection>> emptyList = null;

            // Then
            underTest.addSegmentations(emptyList);
            fail("NullPointerException should be thrown.");
        });
    }

    @Test
    public void testAddSegmentationsListContainsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {

            // Given
            List<List<PointCollection>> list = new LinkedList<>();
            list.add(null);

            // Then
            underTest.addSegmentations(list);
            fail("NullPointerException should be thrown.");
        });
    }

    @Test
    public void testAddSegmentationResponseShouldBeUnderTest() {
        // Given
        List<PointCollection> emptyList = new LinkedList<>();

        // Then
        SegmentationBitmap response = underTest.addSegmentation(emptyList);

        assertNotNull(response);
        assertEquals(underTest, response);
    }

    @Test
    public void testAddSegmentationListIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {

            // Given
            List<PointCollection> emptyList = null;

            // Then
            underTest.addSegmentation(emptyList);
            fail("NullPointerException should be thrown.");
        });
    }

    @Test
    public void testApplyResponseShouldBeUnderTest() {
        SegmentationBitmap response = underTest.apply();

        assertNotNull(response);
        assertEquals(underTest, response);
    }

    @Test
    public void testGetBytes() {

        // Get the image from the test source folder
        final int expectedSize = 3678;

        byte[] bytes = underTest.getBytes();

        assertNotNull(bytes);
        assertEquals(expectedSize, bytes.length);
    }

    @Test
    public void testImageWithoutSegment() throws IOException {

        BufferedImage img = underTest.asBufferedImage();

        assertNotNull(img);
        assertEquals(width, img.getWidth());
        assertEquals(height, img.getHeight());

        String base64encoded = encodeBase64(underTest.getBytes());
        String base64Image = readBase64Resource("blackWithoutSegment");

        assertEquals(base64Image, base64encoded);
    }

    @Test
    public void testImageWithSegment() throws IOException {

        double[][] coordinates = {
                {10d, 10d},
                {10d, 40d},
                {40d, 40d},
                {40d, 10d},
        };

        PointCollection collection = new PointCollection();
        collection.setPoints(castCoordinates(coordinates));

        underTest.addSegmentation(List.of(collection));
        underTest.apply();
        BufferedImage img = underTest.asBufferedImage();

        assertNotNull(img);
        assertEquals(width, img.getWidth());
        assertEquals(height, img.getHeight());

        String base64encoded = encodeBase64(underTest.getBytes());
        String base64Image = readBase64Resource("blackWithSegment");

        assertEquals(base64Image, base64encoded);
    }

    @Test
    public void testImageWithSegmentAndCutOut() throws IOException {

        double[][] coordinates = {
                {10d, 10d},
                {10d, 40d},
                {40d, 40d},
                {40d, 10d},
        };

        double[][] innerCoordinates = {
                {20d, 20d},
                {20d, 30d},
                {30d, 30d},
                {30d, 20d},
        };

        PointCollection outerCollection = new PointCollection();
        outerCollection.setPoints(castCoordinates(coordinates));
        PointCollection innerCollection = new PointCollection();
        innerCollection.setPoints(castCoordinates(innerCoordinates));

        underTest.addSegmentation(List.of(outerCollection, innerCollection));
        underTest.apply();
        BufferedImage img = underTest.asBufferedImage();

        assertNotNull(img);
        assertEquals(width, img.getWidth());
        assertEquals(height, img.getHeight());

        String base64encoded = encodeBase64(underTest.getBytes());
        String base64Image = readBase64Resource("blackWithSegmentAndCut");

        assertEquals(base64Image, base64encoded);
    }

    private static List<PointPojo> castCoordinates(double[][] coordinates) {
        final List<PointPojo> points = new LinkedList<>();
        for (final double[] coordinate: coordinates) {
            final PointPojo pointPojo = new PointPojo();
            pointPojo.setX(coordinate[0]);
            pointPojo.setY(coordinate[1]);
            points.add(pointPojo);
        }
        return points;
    }

    /**
     * Encode the created bitmap as base64 string
     * @param bytes from resource
     * @return base 64 encoded string.
     */
    private static String encodeBase64(byte[] bytes) {
        byte[] base64bytes = Base64.getEncoder().encode(bytes);
        return new String(base64bytes);
    }

    /**
     * Get the full path name from resource .txt file.
     * @param filename resource name without .txt extension.
     * @return path
     */
    private static String getResourcePath(String filename) {
        return String.format("%s/src/test/resources/segmentationBitmapTest/base64Images/%s.txt", System.getProperty("user.dir"), filename);
    }

    /**
     * Encode the resource as base 64 encoded string from bytes array
     * @param filename the resource name
     * @return base64 string
     * @throws IOException
     */
    private static String readBase64Resource(String filename) throws IOException {
        String fullFileName = getResourcePath(filename);
        Path path = Paths.get(fullFileName);
        return new String(Files.readAllBytes(path));
    }

}
