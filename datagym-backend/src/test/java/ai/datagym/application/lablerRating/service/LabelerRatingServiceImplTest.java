package ai.datagym.application.lablerRating.service;

import ai.datagym.application.lablerRating.entity.LabelerRating;
import ai.datagym.application.lablerRating.models.bindingModels.LabelerRatingUpdateBindingModel;
import ai.datagym.application.lablerRating.repo.LabelerRatingRepository;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.repo.ProjectRepository;
import ai.datagym.application.testUtils.ImageUtils;
import ai.datagym.application.testUtils.LabelRatingUtils;
import ai.datagym.application.testUtils.ProjectUtils;
import ai.datagym.application.testUtils.SecurityUtils;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.ForbiddenException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class LabelerRatingServiceImplTest {
    private LabelerRatingService labelerRatingService;

    @Mock
    private LabelerRatingRepository labelerRatingRepositoryMock;

    @Mock
    private ProjectRepository projectRepositoryMock;

    @Mock
    private MediaRepository mediaRepositoryMock;

    @BeforeEach
    void setUp() {
        labelerRatingService = new LabelerRatingServiceImpl(
                labelerRatingRepositoryMock,
                projectRepositoryMock,
                mediaRepositoryMock
        );
    }

    @Test
    void addToPositive_whenLabelerRatingUpdateBindingModelIsValidAndRatingExists_addToPositive() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LabelerRatingUpdateBindingModel ratingUpdateBindingModel = LabelRatingUtils.createTestLabelerRatingUpdateBindingModel();
        LabelerRating testLabelerRating = LabelRatingUtils.createTestLabelerRating();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);

        //When
        when(projectRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(labelerRatingRepositoryMock
                .findByLabelerAndProjectIdAndMediaId(anyString(), anyString(), anyString()))
                .thenReturn(java.util.Optional.of(testLabelerRating));

        when(mediaRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        labelerRatingService.addToPositive(ratingUpdateBindingModel);

        // Then
        ArgumentCaptor<LabelerRating> labelRatingCapture = ArgumentCaptor.forClass(LabelerRating.class);
        verify(labelerRatingRepositoryMock, times(1)).save(labelRatingCapture.capture());
        Assertions.assertThat(labelRatingCapture.getValue().getPositive()).isEqualTo(1);
    }

    @Test
    void addToPositive_whenLabelerRatingUpdateBindingModelIsValidAndRatingIsNotExisting_createLabelRatingAndAddToPositive() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LabelerRatingUpdateBindingModel ratingUpdateBindingModel = LabelRatingUtils.createTestLabelerRatingUpdateBindingModel();
        LabelerRating testLabelerRating = LabelRatingUtils.createTestLabelerRating();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);

        //When
        when(projectRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(labelerRatingRepositoryMock
                .save(any(LabelerRating.class)))
                .thenReturn(testLabelerRating);

        when(mediaRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        labelerRatingService.addToPositive(ratingUpdateBindingModel);

        // Then
        ArgumentCaptor<LabelerRating> labelRatingCapture = ArgumentCaptor.forClass(LabelerRating.class);
        verify(labelerRatingRepositoryMock, times(2)).save(labelRatingCapture.capture());
        Assertions.assertThat(labelRatingCapture.getValue().getPositive()).isEqualTo(1);
    }

    @Test
    void addToPositive_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        LabelerRatingUpdateBindingModel ratingUpdateBindingModel = LabelRatingUtils.createTestLabelerRatingUpdateBindingModel();

        assertThrows(ForbiddenException.class,
                () -> labelerRatingService.addToPositive(ratingUpdateBindingModel)
        );
    }

    @Test
    void addToPositive_whenUserIsNotAdminOrUser_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Given
        LabelerRatingUpdateBindingModel ratingUpdateBindingModel = LabelRatingUtils.createTestLabelerRatingUpdateBindingModel();
        LabelerRating testLabelerRating = LabelRatingUtils.createTestLabelerRating();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("datagym");

        //When
        when(projectRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(labelerRatingRepositoryMock
                .save(any(LabelerRating.class)))
                .thenReturn(testLabelerRating);


        assertThrows(ForbiddenException.class,
                () -> labelerRatingService.addToPositive(ratingUpdateBindingModel)
        );
    }

    @Test
    void addToPositive_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LabelerRatingUpdateBindingModel ratingUpdateBindingModel = LabelRatingUtils.createTestLabelerRatingUpdateBindingModel();
        LabelerRating testLabelerRating = LabelRatingUtils.createTestLabelerRating();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");

        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);

        //When
        when(projectRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(labelerRatingRepositoryMock
                .save(any(LabelerRating.class)))
                .thenReturn(testLabelerRating);

        when(mediaRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        assertThrows(ForbiddenException.class,
                () -> labelerRatingService.addToPositive(ratingUpdateBindingModel)
        );
    }

    @Test
    void addToPositive_whenUserHasWrongScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Given
        LabelerRatingUpdateBindingModel ratingUpdateBindingModel = LabelRatingUtils.createTestLabelerRatingUpdateBindingModel();

        assertThrows(ForbiddenException.class,
                () -> labelerRatingService.addToPositive(ratingUpdateBindingModel)
        );
    }

    @Test
    void addToNegative_whenLabelerRatingUpdateBindingModelIsValidAndRatingExists_addToNegative() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LabelerRatingUpdateBindingModel ratingUpdateBindingModel = LabelRatingUtils.createTestLabelerRatingUpdateBindingModel();
        LabelerRating testLabelerRating = LabelRatingUtils.createTestLabelerRating();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);

        //When
        when(projectRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(labelerRatingRepositoryMock
                .findByLabelerAndProjectIdAndMediaId(anyString(), anyString(), anyString()))
                .thenReturn(java.util.Optional.of(testLabelerRating));

        when(mediaRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        labelerRatingService.addToNegative(ratingUpdateBindingModel);

        // Then
        ArgumentCaptor<LabelerRating> labelRatingCapture = ArgumentCaptor.forClass(LabelerRating.class);
        verify(labelerRatingRepositoryMock, times(1)).save(labelRatingCapture.capture());
        Assertions.assertThat(labelRatingCapture.getValue().getNegative()).isEqualTo(1);
    }

    @Test
    void addToNegative_whenLabelerRatingUpdateBindingModelIsValidAndRatingIsNotExisting_createLabelRatingAndAddToNegative() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LabelerRatingUpdateBindingModel ratingUpdateBindingModel = LabelRatingUtils.createTestLabelerRatingUpdateBindingModel();
        LabelerRating testLabelerRating = LabelRatingUtils.createTestLabelerRating();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);

        //When
        when(projectRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(labelerRatingRepositoryMock
                .save(any(LabelerRating.class)))
                .thenReturn(testLabelerRating);

        when(mediaRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        labelerRatingService.addToNegative(ratingUpdateBindingModel);

        // Then
        ArgumentCaptor<LabelerRating> labelRatingCapture = ArgumentCaptor.forClass(LabelerRating.class);
        verify(labelerRatingRepositoryMock, times(2)).save(labelRatingCapture.capture());
        Assertions.assertThat(labelRatingCapture.getValue().getNegative()).isEqualTo(1);
    }

    @Test
    void addToNegative_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Given
        LabelerRatingUpdateBindingModel ratingUpdateBindingModel = LabelRatingUtils.createTestLabelerRatingUpdateBindingModel();

        assertThrows(ForbiddenException.class,
                () -> labelerRatingService.addToNegative(ratingUpdateBindingModel)
        );
    }

    @Test
    void addToNegative_whenUserIsNotAdminOrUser_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Given
        LabelerRatingUpdateBindingModel ratingUpdateBindingModel = LabelRatingUtils.createTestLabelerRatingUpdateBindingModel();
        LabelerRating testLabelerRating = LabelRatingUtils.createTestLabelerRating();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("datagym");

        //When
        when(projectRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(labelerRatingRepositoryMock
                .save(any(LabelerRating.class)))
                .thenReturn(testLabelerRating);


        assertThrows(ForbiddenException.class,
                () -> labelerRatingService.addToNegative(ratingUpdateBindingModel)
        );
    }

    @Test
    void addToNegative_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        LabelerRatingUpdateBindingModel ratingUpdateBindingModel = LabelRatingUtils.createTestLabelerRatingUpdateBindingModel();
        LabelerRating testLabelerRating = LabelRatingUtils.createTestLabelerRating();
        Media testMedia = ImageUtils.createTestImage(IMAGE_ID);

        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);
        testProject.setOwner("test_org");

        //When
        when(projectRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testProject));

        when(labelerRatingRepositoryMock
                .save(any(LabelerRating.class)))
                .thenReturn(testLabelerRating);

        when(mediaRepositoryMock
                .findById(anyString()))
                .thenReturn(java.util.Optional.of(testMedia));

        assertThrows(ForbiddenException.class,
                () -> labelerRatingService.addToNegative(ratingUpdateBindingModel)
        );
    }

    @Test
    void addToNegative_whenUserHasWrongScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Given
        LabelerRatingUpdateBindingModel ratingUpdateBindingModel = LabelRatingUtils.createTestLabelerRatingUpdateBindingModel();

        assertThrows(ForbiddenException.class,
                () -> labelerRatingService.addToNegative(ratingUpdateBindingModel)
        );
    }
}