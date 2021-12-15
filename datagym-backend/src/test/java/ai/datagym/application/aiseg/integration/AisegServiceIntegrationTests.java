package ai.datagym.application.aiseg.integration;

import ai.datagym.application.Application;
import ai.datagym.application.aiseg.client.AiSegClient;
import ai.datagym.application.aiseg.model.aiseg.AiSegCalculate;
import ai.datagym.application.aiseg.model.aiseg.AiSegPoint;
import ai.datagym.application.aiseg.model.aiseg.AiSegPrefetch;
import ai.datagym.application.aiseg.model.aiseg.AiSegResponse;
import ai.datagym.application.aiseg.service.AiSegService;
import ai.datagym.application.utils.constants.CommonMessages;
import com.eforce21.cloud.login.client.crypt.KeyProviderJwks;
import com.eforce21.lib.exception.GenericException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import retrofit2.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
@MockBean(KeyProviderJwks.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@Disabled
public class AisegServiceIntegrationTests {
    private static final String NORMAL_SIZED_JPG_IMAGE_PATH = System.getProperty("user.dir") + "/src/test/resources/aisegIntegrationTests/base64Images/normal_sized_jpg_image_640x480.txt";
    private static final String NORMAL_SIZED_JPG_IMAGE_ID = "normal_sized_jpg_image_id";

    private static final String NORMAL_SIZED_PNG_IMAGE_PATH = System.getProperty("user.dir") + "/src/test/resources/aisegIntegrationTests/base64Images/normal_sized_png_image_640x325.txt";
    private static final String NORMAL_SIZED_PNG_IMAGE_ID = "normal_sized_png_mage_id";

    private static final String EXIF_ROTATED_IMAGE_PATH = System.getProperty("user.dir") + "/src/test/resources/aisegIntegrationTests/base64Images/exif_rotated_image_960x629.txt";
    private static final String EXIF_ROTATED_IMAGE_ID = "exif_rotated_image_id";

    private static final String TINY_IMAGE_PATH = System.getProperty("user.dir") + "/src/test/resources/aisegIntegrationTests/base64Images/tiny_png_image_200x200.txt";
    private static final String TINY_IMAGE_ID = "tiny_image_id";

    private static final String HUGE_IMAGE_PATH = System.getProperty("user.dir") + "/src/test/resources/aisegIntegrationTests/base64Images/huge_sized_image_2109x3035.txt";
    private static final String HUGE_IMAGE_ID = "huge_image_id";

    private static final String GREYSCALED_IMAGE_PATH = System.getProperty("user.dir") + "/src/test/resources/aisegIntegrationTests/base64Images/greyscaled_image_640x480.txt";
    private static final String GREYSCALED_IMAGE_ID = "greyscaled_image_id";

    private static final String INVALID_IMAGE_PATH = System.getProperty("user.dir") + "/src/test/resources/aisegIntegrationTests/base64Images/invalid_image.txt";
    private static final String INVALID_IMAGE_ID = "invalid_image_id";

    @Autowired
    private AiSegService aiSegService;

    @Autowired
    private AiSegClient aiSegClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Tests with a normal sized JPG Image
     */
    @Test
    public void testNormalSizedJPGImage_point_withOnePositivePoint_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(NORMAL_SIZED_JPG_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(NORMAL_SIZED_JPG_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(232, 211);
        List<Integer> negativePointsCoordinates = new ArrayList<>();

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = new ArrayList<>();

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(NORMAL_SIZED_JPG_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(NORMAL_SIZED_JPG_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(NORMAL_SIZED_JPG_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testNormalSizedJPGImage_points_withFivePositivePointsAndFiveNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(NORMAL_SIZED_JPG_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(NORMAL_SIZED_JPG_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(455, 272, 490, 248, 549, 235, 578, 270, 513, 294);
        List<Integer> negativePointsCoordinates = List.of(446, 337, 419, 260, 473, 230, 550, 223, 601, 313);

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(NORMAL_SIZED_JPG_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(NORMAL_SIZED_JPG_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(NORMAL_SIZED_JPG_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testNormalSizedJPGImage_rect_withOnePositivePointAndFourNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(NORMAL_SIZED_JPG_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(NORMAL_SIZED_JPG_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(126, 296);
        List<Integer> negativePointsCoordinates = List.of(36, 238, 215, 238, 36, 355, 215, 355);

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(NORMAL_SIZED_JPG_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(NORMAL_SIZED_JPG_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(NORMAL_SIZED_JPG_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testNormalSizedJPGImage_brush_withManyPositivePointsAndZeroNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(NORMAL_SIZED_JPG_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(NORMAL_SIZED_JPG_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(282, 261, 282, 276, 261, 291, 259, 309, 280, 341, 290, 317, 312, 367, 349, 353, 345, 347, 358, 327, 344, 300, 379, 277, 335, 263, 318, 251, 325, 248, 302, 263, 290, 268, 282, 281, 263, 279, 296, 293, 277, 296, 281, 343, 293, 356, 298, 350, 335, 371, 366, 344, 377, 320, 380, 295, 378, 274, 368, 259, 317, 251, 301, 247, 299, 245, 288, 254, 286, 238);
        List<Integer> negativePointsCoordinates = new ArrayList<>();

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(NORMAL_SIZED_JPG_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(NORMAL_SIZED_JPG_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(NORMAL_SIZED_JPG_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    /**
     * Tests with a normal sized PNG Image
     */
    @Test
    public void testNormalSizedPNGImage_point_withOnePositivePoint_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(NORMAL_SIZED_PNG_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(NORMAL_SIZED_PNG_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(175, 172);
        List<Integer> negativePointsCoordinates = new ArrayList<>();

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = new ArrayList<>();

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(NORMAL_SIZED_PNG_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(NORMAL_SIZED_PNG_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(NORMAL_SIZED_PNG_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testNormalSizedPNGImage_points_withFivePositivePointsAndFiveNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(NORMAL_SIZED_PNG_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(NORMAL_SIZED_PNG_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(155, 181, 73, 189, 160, 89, 251, 89, 217, 190);
        List<Integer> negativePointsCoordinates = List.of(93, 292, 10, 112, 105, 40, 330, 38, 283, 238);

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(NORMAL_SIZED_PNG_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(NORMAL_SIZED_PNG_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(NORMAL_SIZED_PNG_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testNormalSizedPNGImage_rect_withOnePositivePointAndFourNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(NORMAL_SIZED_PNG_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(NORMAL_SIZED_PNG_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(158, 145);
        List<Integer> negativePointsCoordinates = List.of(7, 27, 308, 27, 7, 264, 308, 264);

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(NORMAL_SIZED_PNG_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(NORMAL_SIZED_PNG_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(NORMAL_SIZED_PNG_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testNormalSizedPNGImage_brush_withManyPositivePointsAndZeroNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(NORMAL_SIZED_PNG_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(NORMAL_SIZED_PNG_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(40, 164, 78, 203, 92, 241, 181, 261, 229, 222, 291, 141, 287, 70, 200, 42, 135, 100, 30, 120, 42, 185, 59, 222, 134, 245, 204, 232, 221, 186, 273, 103, 242, 35, 187, 78, 112, 89, 34, 143, 38, 156);
        List<Integer> negativePointsCoordinates = new ArrayList<>();

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(NORMAL_SIZED_PNG_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(NORMAL_SIZED_PNG_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(NORMAL_SIZED_PNG_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    /**
     * Tests with an exif rotated Image
     */
    @Test
    public void testExifRotatedImage_point_withOnePositivePoint_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(EXIF_ROTATED_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(EXIF_ROTATED_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(419, 332);
        List<Integer> negativePointsCoordinates = new ArrayList<>();

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = new ArrayList<>();

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(EXIF_ROTATED_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(EXIF_ROTATED_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(EXIF_ROTATED_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testExifRotatedImage_points_withFivePositivePointsAndFiveNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(EXIF_ROTATED_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(EXIF_ROTATED_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(389, 286, 528, 288, 719, 328, 404, 371, 264, 422);
        List<Integer> negativePointsCoordinates = List.of(162, 213, 469, 142, 835, 262, 672, 413, 285, 497);

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(EXIF_ROTATED_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(EXIF_ROTATED_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(EXIF_ROTATED_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testExifRotatedImage_rect_withOnePositivePointAndFourNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(EXIF_ROTATED_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(EXIF_ROTATED_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(126, 296);
        List<Integer> negativePointsCoordinates = List.of(87, 129, 925, 129, 87, 487, 925, 487);

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(EXIF_ROTATED_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(EXIF_ROTATED_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(EXIF_ROTATED_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testExifRotatedImage_brush_withManyPositivePointsAndZeroNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(EXIF_ROTATED_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(EXIF_ROTATED_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(195, 355, 220, 360, 226, 382, 305, 409, 358, 409, 428, 377, 524, 339, 613, 387, 673, 326, 731, 341, 771, 284, 645, 278, 537, 272, 398, 243, 316, 248, 224, 258, 193, 346, 213, 324, 216, 378, 269, 382, 375, 382, 390, 378, 500, 395, 566, 360, 667, 355, 711, 318, 771, 293, 680, 281, 561, 251, 416, 270, 327, 224, 230, 254, 194, 284, 200, 300);
        List<Integer> negativePointsCoordinates = new ArrayList<>();

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(EXIF_ROTATED_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(EXIF_ROTATED_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(EXIF_ROTATED_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    /**
     * Tests with a tiny Image
     */
    @Test
    public void testTinyImage_point_withOnePositivePoint_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(TINY_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(TINY_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(30, 49);
        List<Integer> negativePointsCoordinates = new ArrayList<>();

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = new ArrayList<>();

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(TINY_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(TINY_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(TINY_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testTinyImage_points_withFivePositivePointsAndFiveNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(TINY_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(TINY_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(118, 80, 132, 79, 130, 87, 124, 98, 117, 100);
        List<Integer> negativePointsCoordinates = List.of(103, 90, 106, 74, 131, 69, 138, 93, 130, 104);

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(TINY_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(TINY_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(TINY_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testTinyImage_rect_withOnePositivePointAndFourNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(TINY_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(TINY_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(72, 92);
        List<Integer> negativePointsCoordinates = List.of(53, 76, 91, 76, 53, 107, 91, 107);

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(TINY_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(TINY_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(TINY_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testTinyImage_brush_withManyPositivePointsAndZeroNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(TINY_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(TINY_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(119, 121, 116, 113, 121, 115, 124, 119, 133, 132, 132, 137, 136, 131, 120, 128, 116, 124, 122, 120, 120, 113, 125, 113, 137, 116, 133, 120, 125, 125, 131, 131, 125, 131, 120, 128, 115, 131, 114, 110, 108, 116);
        List<Integer> negativePointsCoordinates = new ArrayList<>();

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(TINY_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(TINY_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(TINY_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    /**
     * Tests with a huge Image
     */
    @Test
    public void testHugeImage_point_withOnePositivePoint_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(HUGE_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(HUGE_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(303, 733);
        List<Integer> negativePointsCoordinates = new ArrayList<>();

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = new ArrayList<>();

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(HUGE_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(HUGE_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(HUGE_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testHugeImage_points_withFivePositivePointsAndFiveNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(HUGE_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(HUGE_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(1199, 700, 1419, 751, 1414, 873, 1246, 882, 1162, 803);
        List<Integer> negativePointsCoordinates = List.of(1050, 817, 1069, 649, 1321, 621, 1578, 873, 1368, 999);

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(HUGE_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(HUGE_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(HUGE_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testHugeImage_rect_withOnePositivePointAndFourNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(HUGE_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(HUGE_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(772, 1393);
        List<Integer> negativePointsCoordinates = List.of(532, 1171, 1013, 1171, 532, 1615, 1013, 1615);

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(HUGE_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(HUGE_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(HUGE_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testHugeImage_brush_withManyPositivePointsAndZeroNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(HUGE_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(HUGE_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(1251, 1134, 1442, 1246, 1410, 1363, 1522, 1326, 1391, 1410, 1489, 1489, 1270, 1396, 1125, 1470, 1270, 1447, 1260, 1251, 1344, 1129, 1419, 1372, 1517, 1344, 1391, 1545, 1242, 1503, 1256, 1517, 1363, 1456, 1270, 1382, 1270, 1185, 1400, 1274);
        List<Integer> negativePointsCoordinates = new ArrayList<>();

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(HUGE_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(HUGE_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(HUGE_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    /**
     * Tests with a grey scaled Image
     */
    @Test
    public void testGreyscaledImage_point_withOnePositivePoint_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(GREYSCALED_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(GREYSCALED_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(313, 296);
        List<Integer> negativePointsCoordinates = new ArrayList<>();

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = new ArrayList<>();

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(GREYSCALED_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(GREYSCALED_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(GREYSCALED_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testGreyscaledImage_points_withFivePositivePointsAndFiveNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(GREYSCALED_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(GREYSCALED_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(73, 262, 133, 258, 163, 281, 123, 295, 97, 281);
        List<Integer> negativePointsCoordinates = List.of(46, 318, 34, 250, 75, 236, 175, 251, 186, 335);

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(GREYSCALED_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(GREYSCALED_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(GREYSCALED_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testGreyscaledImage_rect_withOnePositivePointAndFourNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(GREYSCALED_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(GREYSCALED_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(526, 287);
        List<Integer> negativePointsCoordinates = List.of(422, 243, 631, 243, 422, 330, 631, 330);

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(GREYSCALED_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(GREYSCALED_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(GREYSCALED_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    @Test
    public void testGreyscaledImage_brush_withManyPositivePointsAndZeroNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(GREYSCALED_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(GREYSCALED_IMAGE_ID, base64Image);

        // Prepare(save locally) the current Image for Aiseg
        aiSegClient.prepare(aiSegPrefetch).execute();

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(377, 184, 386, 226, 398, 213, 409, 238, 441, 220, 436, 217, 418, 201, 426, 209, 408, 187, 392, 169, 383, 178, 380, 204, 411, 209, 422, 216, 433, 219, 447, 220, 457, 210, 455, 196, 417, 208, 381, 187, 388, 172, 363, 203);
        List<Integer> negativePointsCoordinates = new ArrayList<>();

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(GREYSCALED_IMAGE_ID);

        // Send calculate request to Aiseg
        AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                .execute()
                .body();

        // Delete the image from the local Aiseg-folder after the end of the calculation
        Response<Void> execute = aiSegClient.finish(GREYSCALED_IMAGE_ID).execute();

        // Then
        assertNotNull(responseBody);
        assertNotNull(responseBody.getImageId());
        assertEquals(GREYSCALED_IMAGE_ID, responseBody.getImageId());
        assertFalse(responseBody.getResult().isEmpty());
    }

    /**
     * Tests with an invalid Image or invalid Points
     */
    @Test
    public void testWithInvalidImage_point_withOnePositivePoint_errorResponseFromLoadBalancer() throws IOException {
        Assertions.assertThrows(GenericException.class, () -> {
            // Get the image from the test source folder
            Path path = Paths.get(INVALID_IMAGE_PATH);
            String base64Image = new String(Files.readAllBytes(path));

            AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(INVALID_IMAGE_ID, base64Image);

            // Prepare(save locally) the current Image for Aiseg
            aiSegClient.prepare(aiSegPrefetch).execute();

            // Create the AiSegPoints
            List<Integer> positivePointsCoordinates = List.of(313, 296);
            List<Integer> negativePointsCoordinates = new ArrayList<>();

            List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
            List<AiSegPoint> negativePoints = new ArrayList<>();

            // Create the request body for the current test
            AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
            aiSegCalculate.setImageId(INVALID_IMAGE_ID);

            // Send calculate request to Aiseg
            Response<AiSegResponse> executeCalculate = aiSegClient.calculate(aiSegCalculate)
                    .execute();

            if (!executeCalculate.isSuccessful()) {
                throw new GenericException(CommonMessages.AISEG_COMMUNICATION_ERROR, null, null);
            }
        });
    }

    @Test
    public void testWithInvalidPoints_points_withOneNegativePoint_errorResponseFromLoadBalancer() throws IOException {
        Assertions.assertThrows(GenericException.class, () -> {
            // Get the image from the test source folder
            Path path = Paths.get(NORMAL_SIZED_JPG_IMAGE_PATH);
            String base64Image = new String(Files.readAllBytes(path));

            AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(NORMAL_SIZED_JPG_IMAGE_ID, base64Image);

            // Prepare(save locally) the current Image for Aiseg
            aiSegClient.prepare(aiSegPrefetch).execute();

            // Create the AiSegPoints
            List<Integer> positivePointsCoordinates = new ArrayList<>();
            List<Integer> negativePointsCoordinates = List.of(399, 333);

            List<AiSegPoint> positivePoints = new ArrayList<>();
            List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

            // Create the request body for the current test
            AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
            aiSegCalculate.setImageId(NORMAL_SIZED_JPG_IMAGE_ID);

            // Send calculate request to Aiseg
            Response<AiSegResponse> executeCalculate = aiSegClient.calculate(aiSegCalculate)
                    .execute();

            if (!executeCalculate.isSuccessful()) {
                throw new GenericException(CommonMessages.AISEG_COMMUNICATION_ERROR, null, null);
            }
        });
    }


    @Test
    public void testWithInvalidPoints_points_withPointOutsideTheImage_errorResponseFromLoadBalancer() throws IOException {
        Assertions.assertThrows(GenericException.class, () -> {
            // Get the image from the test source folder
            Path path = Paths.get(NORMAL_SIZED_JPG_IMAGE_PATH);
            String base64Image = new String(Files.readAllBytes(path));

            AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(NORMAL_SIZED_JPG_IMAGE_ID, base64Image);

            // Prepare(save locally) the current Image for Aiseg
            aiSegClient.prepare(aiSegPrefetch).execute();

            // Create the AiSegPoints
            List<Integer> positivePointsCoordinates = List.of(455, 272, 490, 248, 549, 235, 578, 270, 15399, 15333);
            List<Integer> negativePointsCoordinates = List.of(446, 337, 419, 260, 473, 230, 550, 223, 601, 313);


            List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
            List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

            // Create the request body for the current test
            AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
            aiSegCalculate.setImageId(NORMAL_SIZED_JPG_IMAGE_ID);

            // Send calculate request to Aiseg
            Response<AiSegResponse> executeCalculate = aiSegClient.calculate(aiSegCalculate)
                    .execute();

            if (!executeCalculate.isSuccessful()) {
                throw new GenericException(CommonMessages.AISEG_COMMUNICATION_ERROR, null, null);
            }
        });
    }

    /**
     * Load Tests of Aiseg - calls 200 times all three methods "prepare", "calculate" and "finish"
     * with 5 positive and 5 negative points
     */
    @Test
    @Disabled
    public void loadTest_NormalSizedJPGImage_points_withFivePositivePointsAndFiveNegativePoints_200() throws IOException {
        // Get the image from the test source folder
        Path path = Paths.get(NORMAL_SIZED_JPG_IMAGE_PATH);
        String base64Image = new String(Files.readAllBytes(path));

        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(NORMAL_SIZED_JPG_IMAGE_ID, base64Image);

        // Create the AiSegPoints
        List<Integer> positivePointsCoordinates = List.of(455, 272, 490, 248, 549, 235, 578, 270, 513, 294);
        List<Integer> negativePointsCoordinates = List.of(446, 337, 419, 260, 473, 230, 550, 223, 601, 313);

        List<AiSegPoint> positivePoints = createAisegPoints(positivePointsCoordinates);
        List<AiSegPoint> negativePoints = createAisegPoints(negativePointsCoordinates);

        // Create the request body for the current test
        AiSegCalculate aiSegCalculate = createAisegCalculateRequest(positivePoints, negativePoints);
        aiSegCalculate.setImageId(NORMAL_SIZED_JPG_IMAGE_ID);

        for (int i = 0; i < 200; i++) {
            // Prepare(save locally) the current Image for Aiseg
            aiSegClient.prepare(aiSegPrefetch).execute();

            // Send calculate request to Aiseg
            AiSegResponse responseBody = aiSegClient.calculate(aiSegCalculate)
                    .execute()
                    .body();

            // Delete the image from the local Aiseg-folder after the end of the calculation
            Response<Void> execute = aiSegClient.finish(NORMAL_SIZED_JPG_IMAGE_ID).execute();

            // Then
            assertNotNull(responseBody);
            assertNotNull(responseBody.getImageId());
            assertEquals(NORMAL_SIZED_JPG_IMAGE_ID, responseBody.getImageId());
            assertFalse(responseBody.getResult().isEmpty());
        }
    }

    private AiSegCalculate createAisegCalculateRequest(List<AiSegPoint> positivePoints, List<AiSegPoint> negativePoints) {
        AiSegCalculate aiSegCalculate = new AiSegCalculate();

        int numPoints = 40;

        aiSegCalculate.setEnvironment("integration_test");
        aiSegCalculate.setNumPoints(numPoints);
        aiSegCalculate.setPositivePoints(positivePoints);
        aiSegCalculate.setNegativePoints(negativePoints);

        return aiSegCalculate;
    }

    private List<AiSegPoint> createAisegPoints(List<Integer> pointList) {
        List<AiSegPoint> result = new ArrayList<>();

        for (int i = 0; i < pointList.size(); i += 2) {
            AiSegPoint aiSegPoint = new AiSegPoint(pointList.get(i), pointList.get(i + 1));
            result.add(aiSegPoint);
        }

        return result;
    }
}
