package ai.datagym.application.media.service;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.repo.AwsS3UserCredentialsRepository;
import ai.datagym.application.dataset.service.awsS3.AwsS3HelperService;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.media.entity.LocalImage;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.media.entity.UrlImage;
import ai.datagym.application.media.models.viewModels.MediaViewModel;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.testUtils.BinfileEntityUtils;
import ai.datagym.application.testUtils.DatasetUtils;
import ai.datagym.application.testUtils.ImageUtils;
import ai.datagym.application.testUtils.SecurityUtils;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.bin.file.entity.BinFileEntity;
import com.eforce21.lib.bin.file.model.BinFileConsumerHttp;
import com.eforce21.lib.bin.file.service.BinFileService;
import com.eforce21.lib.exception.ForbiddenException;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import org.apache.tika.Tika;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static ai.datagym.application.testUtils.DatasetUtils.DATASET_ID;
import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;
import static ai.datagym.application.testUtils.ImageUtils.TEST_URL_IMAGE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class MediaServiceImplTest {
    private MediaService mediaService;

    private MockHttpServletResponse httpServletResponseMock = new MockHttpServletResponse();

    @Mock
    private BinFileService binFileServiceMock;

    @Mock
    private MediaRepository mediaRepositoryMock;

    @Mock
    private LimitService limitServiceMock;

    @Mock
    private Tika tikaMock;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private LabelTaskRepository labelTaskRepositoryMock;

    @Mock
    private AwsS3UserCredentialsRepository awsS3UserCredentialsRepositoryMock;

    @Mock
    private AwsS3HelperService awsS3HelperServiceMock;

    @BeforeEach
    void setUp() {
        mediaService = new MediaServiceImpl(
                applicationContext,
                mediaRepositoryMock,
                binFileServiceMock,
                tikaMock,
                limitServiceMock,
                labelTaskRepositoryMock,
                awsS3UserCredentialsRepositoryMock,
                Optional.of(awsS3HelperServiceMock));
    }

    @Test
    void streamImageFile_whenInputIsValidAndLocalImage_streamImageFile() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        LocalImage testLocalImage = ImageUtils.createTestLocalImage();
        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();

        testLocalImage.setBinFileEntity(testBinFileEntity);
        testDataset.getMedia().add(testLocalImage);
        testLocalImage.getDatasets().add(testDataset);

        // When
        when(mediaRepositoryMock.findById(any())).thenReturn(java.util.Optional.of(testLocalImage));

        doAnswer((Answer) invocation -> {
            Object arg0 = invocation.getArgument(0);
            Object arg1 = invocation.getArgument(1);

            assertEquals(testBinFileEntity, arg0);
            return null;
        }).when(binFileServiceMock).consume(any(), any());

        mediaService.streamMediaFile(IMAGE_ID, httpServletResponseMock, false);

        // Then
        verify(binFileServiceMock, times(1))
                .consume(any(BinFileEntity.class), any(BinFileConsumerHttp.class));
    }

    @Test
    void streamImageFile_whenImageIdIsInvalid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        BinFileConsumerHttp testBinFileConsumerHttp = ImageUtils.createTestBinFileConsumerHttp();

        Assertions.assertThrows(NotFoundException.class,
                () -> mediaService.streamMediaFile("invalid_image_id", httpServletResponseMock, false)
        );
    }

    @Test
    void streamImageFile_whenInputIsValidAndUrlImage_streamImageFile() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        UrlImage testUrlImage = ImageUtils.createTestUrlImage();

        URL url = new URL(TEST_URL_IMAGE_URL);
        testUrlImage.setUrl(TEST_URL_IMAGE_URL);

        testDataset.getMedia().add(testUrlImage);
        testUrlImage.getDatasets().add(testDataset);

        // When
        when(mediaRepositoryMock.findById(any())).thenReturn(java.util.Optional.of(testUrlImage));

        when(tikaMock.detect(url)).thenReturn("image/png");

        ServletOutputStream outputStream = httpServletResponseMock.getOutputStream();

        mediaService.streamMediaFile(IMAGE_ID, httpServletResponseMock, false);

        // Then
        verify(tikaMock, times(1)).detect(any(URL.class));
    }

    @Test
    void streamImageFile_whenUrlImageAndImageUrlIsNotValidUrl_throwException() throws IOException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        UrlImage testUrlImage = ImageUtils.createTestUrlImage();

        testUrlImage.setUrl("invalid_url");

        testDataset.getMedia().add(testUrlImage);
        testUrlImage.getDatasets().add(testDataset);

        // When
        when(mediaRepositoryMock.findById(any())).thenReturn(java.util.Optional.of(testUrlImage));

        Assertions.assertThrows(GenericException.class,
                () -> mediaService.streamMediaFile(IMAGE_ID, httpServletResponseMock, false)
        );
    }

//    @Test
//    void streamImageFile_whenUrlImageAndImageMimeTypeIsUnsupported_throwException() throws IOException {
//        // Set Security Context
//        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
//        SecurityContext.set(oauthUser);
//
//        // Given
//        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
//        UrlImage testUrlImage = ImageUtils.createTestUrlImage();
//
//        testUrlImage.setUrl(UNSUPPORTED_FORMAT_URL_2);
//
//        testDataset.getImages().add(testUrlImage);
//        testUrlImage.getDatasets().add(testDataset);
//
//        // When
//        when(imageRepositoryMock.findById(any())).thenReturn(java.util.Optional.of(testUrlImage));
//
//        Assertions.assertThrows(ValidationException.class,
//                () -> mediaService.streamMediaFile(IMAGE_ID,  httpServletResponseMock, false)
//        );
//    }

    @Test
    void streamImageFile_whenImageIdIsDeletedIsTrue_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        BinFileConsumerHttp testBinFileConsumerHttp = ImageUtils.createTestBinFileConsumerHttp();
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        LocalImage testImage = ImageUtils.createTestLocalImage();
        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();

        testImage.setBinFileEntity(testBinFileEntity);
        testImage.setDeleted(true);

        testDataset.getMedia().add(testImage);
        testImage.getDatasets().add(testDataset);

        // When
        when(mediaRepositoryMock.findById(any())).thenReturn(java.util.Optional.of(testImage));

        Assertions.assertThrows(NotFoundException.class,
                () -> mediaService.streamMediaFile(IMAGE_ID, httpServletResponseMock, false)
        );
    }

    @Test
    void streamImageFile_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        BinFileConsumerHttp testBinFileConsumerHttp = ImageUtils.createTestBinFileConsumerHttp();

        Assertions.assertThrows(ForbiddenException.class,
                () -> mediaService.streamMediaFile(IMAGE_ID, httpServletResponseMock, false)
        );
    }

    @Test
    void streamImageFile_whenUserIsNotPermittedToManipulateImage_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        LocalImage testImage = ImageUtils.createTestLocalImage();
        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();
        testImage.setBinFileEntity(testBinFileEntity);

        // When
        when(mediaRepositoryMock.findById(any())).thenReturn(java.util.Optional.of(testImage));

        Assertions.assertThrows(ForbiddenException.class,
                () -> mediaService.streamMediaFile(IMAGE_ID, httpServletResponseMock, false)
        );
    }

    @Test
    void deleteImageFile_whenImageIdIsValidAndDeleteImageTrue_setDeletedToTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        LocalImage testImage = ImageUtils.createTestLocalImage();
        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();

        testImage.setBinFileEntity(testBinFileEntity);
        testDataset.getMedia().add(testImage);
        testImage.getDatasets().add(testDataset);

        //when
        when(mediaRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testImage));

        doAnswer((Answer) invocation -> {
            Media arg0 = invocation.getArgument(0);

            assertTrue(arg0.isDeleted());
            assertNotNull(arg0.getDeleteTime());
            return arg0;
        }).when(mediaRepositoryMock).save(any(Media.class));

        MediaViewModel actual = mediaService.deleteMediaFile(IMAGE_ID, true);

        //Then
        assertNotNull(actual);

        assertEquals(testImage.getId(), actual.getId());
        assertEquals(testImage.getMediaName(), actual.getMediaName());
        assertEquals(testImage.getMediaSourceType().name(), actual.getMediaSourceType());

        verify(mediaRepositoryMock).save(any());
        verify(mediaRepositoryMock, times(1)).save(any());
    }

    @Test
    void deleteImageFile_whenImageIdIsValidAndDeleteImageFalse_setDeletedToFalse() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        LocalImage testImage = ImageUtils.createTestLocalImage();
        testImage.setDeleted(true);
        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();

        testImage.setBinFileEntity(testBinFileEntity);
        testDataset.getMedia().add(testImage);
        testImage.getDatasets().add(testDataset);

        //when
        when(mediaRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testImage));

        doAnswer((Answer) invocation -> {
            Media arg0 = invocation.getArgument(0, Media.class);

            assertFalse(arg0.isDeleted());
            assertNull(arg0.getDeleteTime());
            return arg0;
        }).when(mediaRepositoryMock).save(any(Media.class));

        MediaViewModel actual = mediaService.deleteMediaFile(IMAGE_ID, false);

        //Then
        assertNotNull(actual);

        assertEquals(testImage.getId(), actual.getId());
        assertEquals(testImage.getMediaName(), actual.getMediaName());
        assertEquals(testImage.getMediaSourceType().name(), actual.getMediaSourceType());

        verify(mediaRepositoryMock).save(any());
        verify(mediaRepositoryMock, times(1)).save(any());
    }

    @Test
    void deleteImageFile_whenImageIdIsNotValidAndDeleteImageTrue_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> mediaService.deleteMediaFile("invalid_image_id", true)
        );
    }

    @Test
    void deleteImageFile_whenImageIdIsNotValidAndDeleteImageFalse_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> mediaService.deleteMediaFile("invalid_image_id", false)
        );
    }

    @Test
    void deleteImageFile_whenUserIsNotPermittedToManipulateImage_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        LocalImage testImage = ImageUtils.createTestLocalImage();
        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();
        testImage.setBinFileEntity(testBinFileEntity);

        // When
        when(mediaRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testImage));

        Assertions.assertThrows(ForbiddenException.class,
                () -> mediaService.deleteMediaFile(IMAGE_ID, false)
        );
    }

    @Test
    void deleteImageFile_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        LocalImage testImage = ImageUtils.createTestLocalImage();
        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();

        testImage.setBinFileEntity(testBinFileEntity);
        testDataset.getMedia().add(testImage);
        testImage.getDatasets().add(testDataset);
        testDataset.setOwner("datagym");

        // When
        when(mediaRepositoryMock.findById(any())).thenReturn(java.util.Optional.of(testImage));

        Assertions.assertThrows(ForbiddenException.class,
                () -> mediaService.deleteMediaFile(IMAGE_ID, false)
        );
    }

    @Test
    void deleteImageFile_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(ForbiddenException.class,
                () -> mediaService.deleteMediaFile(IMAGE_ID, false)
        );
    }

    @Test
    void permanentDeleteImageFileFromDB_whenImageIdIsValid_permanentDeleteImageFileFromDB() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        LocalImage testImage = ImageUtils.createTestLocalImage();
        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();

        testImage.setBinFileEntity(testBinFileEntity);
        testDataset.getMedia().add(testImage);
        testImage.getDatasets().add(testDataset);

        //When
        when(mediaRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testImage));

        mediaService.permanentDeleteMediaFile(DATASET_ID);

        //Then
        verify(mediaRepositoryMock).delete(any(Media.class));
        verify(mediaRepositoryMock, times(1)).delete(any(Media.class));
    }

    @Test
    void permanentDeleteImageFileFromDB_whenImageIdIsNotValid_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> mediaService.permanentDeleteMediaFile(DATASET_ID)
        );
    }

    @Test
    void permanentDeleteImageFileFromDB_whenUserIsNotPermittedToManipulateImage_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        LocalImage testImage = ImageUtils.createTestLocalImage();
        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();
        testImage.setBinFileEntity(testBinFileEntity);

        // When
        when(mediaRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testImage));

        //Then
        assertThrows(ForbiddenException.class,
                () -> mediaService.permanentDeleteMediaFile(DATASET_ID)
        );
    }

    @Test
    void permanentDeleteImageFileFromDB_whenUserIsNotRoot_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        LocalImage testImage = ImageUtils.createTestLocalImage();
        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();

        testImage.setBinFileEntity(testBinFileEntity);
        testDataset.getMedia().add(testImage);
        testImage.getDatasets().add(testDataset);
        testDataset.setOwner("datagym");

        // When
        when(mediaRepositoryMock.findById(any())).thenReturn(java.util.Optional.of(testImage));

        Assertions.assertThrows(ForbiddenException.class,
                () -> mediaService.permanentDeleteMediaFile(DATASET_ID)
        );
    }

    @Test
    void permanentDeleteImageFileFromDB_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        Assertions.assertThrows(ForbiddenException.class,
                () -> mediaService.permanentDeleteMediaFile(DATASET_ID)
        );
    }

    @Test
    void permanentDeleteImageFileFromDB_whenImageObjectIsValid_permanentDeleteImageFileFromDB() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithRootRole();
        SecurityContext.set(oauthUser);

        // Given
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        LocalImage testImage = ImageUtils.createTestLocalImage();
        BinFileEntity testBinFileEntity = BinfileEntityUtils.createTestBinFileEntity();

        testImage.setBinFileEntity(testBinFileEntity);
        testDataset.getMedia().add(testImage);
        testImage.getDatasets().add(testDataset);

        //When
        when(mediaRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testImage));

        mediaService.permanentDeleteMediaFile(testImage, false);

        //Then
        verify(mediaRepositoryMock).delete(any(Media.class));
        verify(mediaRepositoryMock, times(1)).delete(any(Media.class));
    }

    @Test
    void deleteImageFileList_whenImageIdSetIsValidAndDeleteImageTrue_setDeletedToTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Set<String> testImageIdSet = ImageUtils.createTestImageIdSet(2);
        List<Media> testListMedia = ImageUtils.createTestListImages(2);

        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        Media testMedia1 = testListMedia.get(0);
        Media testMedia2 = testListMedia.get(1);

        testDataset.getMedia().add(testMedia1);
        testMedia1.getDatasets().add(testDataset);

        testDataset.getMedia().add(testMedia2);
        testMedia2.getDatasets().add(testDataset);

        //when
        when(mediaRepositoryMock.findAllById(anySet()))
                .thenReturn(testListMedia);

        mediaService.deleteMediaFileList(testImageIdSet, true);

        //Then
        ArgumentCaptor<Set<String>> idSetCapture = ArgumentCaptor.forClass(HashSet.class);

        verify(mediaRepositoryMock, times(1))
                .findAllById(idSetCapture.capture());
        assertThat(idSetCapture.getValue().size()).isEqualTo(testImageIdSet.size());
        verifyNoMoreInteractions(mediaRepositoryMock);
    }

    @Test
    void deleteImageFileList_whenImageIdSetIsValidAndDeleteImageFalse_setDeletedToFasle() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Set<String> testImageIdSet = ImageUtils.createTestImageIdSet(2);
        List<Media> testListMedia = ImageUtils.createTestListImages(2);

        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        Media testMedia1 = testListMedia.get(0);
        Media testMedia2 = testListMedia.get(1);

        testDataset.getMedia().add(testMedia1);
        testMedia1.getDatasets().add(testDataset);
        testMedia1.setDeleted(true);

        testDataset.getMedia().add(testMedia2);
        testMedia2.getDatasets().add(testDataset);
        testMedia2.setDeleted(true);

        //when
        when(mediaRepositoryMock.findAllById(anySet()))
                .thenReturn(testListMedia);

        mediaService.deleteMediaFileList(testImageIdSet, false);

        //Then
        ArgumentCaptor<Set<String>> idSetCapture = ArgumentCaptor.forClass(HashSet.class);

        verify(mediaRepositoryMock, times(1))
                .findAllById(idSetCapture.capture());
        assertThat(idSetCapture.getValue().size()).isEqualTo(testImageIdSet.size());
        verifyNoMoreInteractions(mediaRepositoryMock);
    }

    @Test
    void deleteImageFileList_whenInvalidIdsInTheSet_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Set<String> testImageIdSet = ImageUtils.createTestImageIdSet(2);
        List<Media> testListMedia = ImageUtils.createTestListImages(1);

        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        Media testMedia1 = testListMedia.get(0);

        testDataset.getMedia().add(testMedia1);
        testMedia1.getDatasets().add(testDataset);

        //when
        when(mediaRepositoryMock.findAllById(anySet()))
                .thenReturn(testListMedia);

        //Then
        assertThrows(GenericException.class,
                () -> mediaService.deleteMediaFileList(testImageIdSet, true)
        );
    }

    @Test
    void deleteImageFileList_whenImagesIsDeletedIsAlreadyTrue_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Set<String> testImageIdSet = ImageUtils.createTestImageIdSet(2);
        List<Media> testListMedia = ImageUtils.createTestListImages(2);

        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        Media testMedia1 = testListMedia.get(0);
        Media testMedia2 = testListMedia.get(1);

        testMedia1.setDeleted(true);

        testDataset.getMedia().add(testMedia1);
        testMedia1.getDatasets().add(testDataset);

        testDataset.getMedia().add(testMedia2);
        testMedia2.getDatasets().add(testDataset);

        //when
        when(mediaRepositoryMock.findAllById(anySet()))
                .thenReturn(testListMedia);

        //Then
        assertThrows(GenericException.class,
                () -> mediaService.deleteMediaFileList(testImageIdSet, true)
        );
    }


    @Test
    void deleteImageFileList_whenUserIsNotPermittedToManipulateImage_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Set<String> testImageIdSet = ImageUtils.createTestImageIdSet(2);
        List<Media> testListMedia = ImageUtils.createTestListImages(2);

        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setOwner("test_org");

        Media testMedia1 = testListMedia.get(0);
        Media testMedia2 = testListMedia.get(1);

        testMedia1.setDeleted(true);

        testDataset.getMedia().add(testMedia1);
        testMedia1.getDatasets().add(testDataset);

        testDataset.getMedia().add(testMedia2);
        testMedia2.getDatasets().add(testDataset);

        //when
        when(mediaRepositoryMock.findAllById(anySet()))
                .thenReturn(testListMedia);
        Assertions.assertThrows(ForbiddenException.class,
                () -> mediaService.deleteMediaFileList(testImageIdSet, true)
        );
    }

    @Test
    void deleteImageFileList_whenUserIsNotAdmin_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Set<String> testImageIdSet = ImageUtils.createTestImageIdSet(2);
        List<Media> testListMedia = ImageUtils.createTestListImages(2);

        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);
        testDataset.setOwner("datagym");

        Media testMedia1 = testListMedia.get(0);
        Media testMedia2 = testListMedia.get(1);

        testMedia1.setDeleted(true);

        testDataset.getMedia().add(testMedia1);
        testMedia1.getDatasets().add(testDataset);

        testDataset.getMedia().add(testMedia2);
        testMedia2.getDatasets().add(testDataset);

        //when
        when(mediaRepositoryMock.findAllById(anySet()))
                .thenReturn(testListMedia);
        Assertions.assertThrows(ForbiddenException.class,
                () -> mediaService.deleteMediaFileList(testImageIdSet, true)
        );
    }
}