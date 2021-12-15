package ai.datagym.application.export.service;

import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.testUtils.ProjectUtils;
import ai.datagym.application.testUtils.SecurityUtils;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.ForbiddenException;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;

@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
public class ExportSegmentationServiceImplTest {
    private String loggedInUserId;

    LabelTaskRepository labelTaskRepositoryMock;
    LcEntryValueRepository lcEntryValueRepositoryMock;
    Tika tikaMock;

    ExportSegmentationService underTest;

    @BeforeEach
    public void setUp() {

        tikaMock = Mockito.mock(Tika.class);
        labelTaskRepositoryMock = Mockito.mock(LabelTaskRepository.class);
        lcEntryValueRepositoryMock = Mockito.mock(LcEntryValueRepository.class);

        underTest = new ExportSegmentationServiceImpl(
                lcEntryValueRepositoryMock,
                labelTaskRepositoryMock,
                tikaMock
        );

        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);
        loggedInUserId = oauthUser.id();
    }

    @Test
    public void testWrongPermissions() throws IOException {
        SecurityContext.clear();

        String taskId = "not used here";
        String lcEntryKey = "not used here";
        String expectedErrorMessage = "Forbidden.";
        final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        try {
            underTest.streamSegmentationBitmap(taskId, lcEntryKey, response);
            fail("ForbiddenException should be thrown.");
        } catch (ForbiddenException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void testWrongProjectOwner() throws IOException {
        // Given
        final String taskId = "mocked task id";
        final String lcEntryKey = "not-existing";
        final String anotherOwner = "Another owner";
        final String expectedErrorKey = "ex_forbidden";
        final String expectedErrorMessage = "Forbidden.";
        final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        final LabelTask labelTask = Mockito.mock(LabelTask.class);
        final Project project = ProjectUtils.createTestProject(PROJECT_ID);
        project.setOwner(anotherOwner);

        // When
        Mockito.when(labelTaskRepositoryMock.findById(taskId))
                .thenReturn(Optional.of(labelTask));
        Mockito.when(labelTask.getProject())
                .thenReturn(project);
        Mockito.when(labelTask.getLabelTaskState())
                .thenReturn(LabelTaskState.COMPLETED);

        try {
            underTest.streamSegmentationBitmap(taskId, lcEntryKey, response);
            fail("ForbiddenException should be thrown.");
        } catch (ForbiddenException e) {
            assertEquals(expectedErrorKey, e.getKey());
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void testInvalidTaskId() throws IOException {
        // Given
        String taskId = "not-existing";
        String lcEntryKey = "not used here";
        String expectedErrorMessage = "Item task with id not-existing not found.";
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        // When
        try {
            underTest.streamSegmentationBitmap(taskId, lcEntryKey, response);
            // Then
            fail("NotFoundException should be thrown.");
        } catch (NotFoundException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void testInvalidLabelTaskStates() throws IOException {
        // Given
        String taskId = "mocked task id";
        String lcEntryKey = "not used here";
        String genericExceptionKey = "ex_gen_wrong_label_task_state";
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        LabelTask labelTask = Mockito.mock(LabelTask.class);

        List<LabelTaskState> allowedLabelTaskStates = new ArrayList<>();
        allowedLabelTaskStates.add(LabelTaskState.COMPLETED);
        allowedLabelTaskStates.add(LabelTaskState.SKIPPED);
        allowedLabelTaskStates.add(LabelTaskState.REVIEWED);

        LabelTaskState[] states = LabelTaskState.values();

        // When
        Mockito.when(labelTaskRepositoryMock.findById(taskId))
                .thenReturn(Optional.of(labelTask));
        Mockito.when(labelTask.getId()).thenReturn(taskId);

        int counter = 0;
        for (final LabelTaskState state: LabelTaskState.values()) {
            // Here we only want to test *all* invalid label task states.
            if (allowedLabelTaskStates.contains(state)) {
                continue;
            }
            // Set the invalid state.
            labelTask.setLabelTaskState(state);

            try {
                underTest.streamSegmentationBitmap(taskId, lcEntryKey, response);
                fail("GenericException should be thrown.");
            } catch (GenericException e) {
                assertEquals(genericExceptionKey, e.getKey());
                assertNotNull(e.getParams());
                assertEquals(1, e.getParams().size());
                assertEquals(taskId, e.getParams().get(0));
            }

            counter++;
        }

        // Also check that at least one invalid state was tested.
        final int iterations = states.length - allowedLabelTaskStates.size();
        assertTrue(iterations > 0);
        assertEquals(iterations, counter);
        Mockito.verify(labelTask, times(iterations)).getId();
    }

    @Test
    public void testInvalidEntryKey() throws IOException {
        // Given
        final String taskId = "mocked task id";
        final String lcEntryKey = "not-existing";
        final String expectedErrorMessage = String.format("Item lcEntry with entry_key %s not found.", lcEntryKey);
        final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        final LabelTask labelTask = Mockito.mock(LabelTask.class);
        final Project project = ProjectUtils.createTestProject(PROJECT_ID);
        project.setOwner(loggedInUserId);

        // When
        Mockito.when(labelTaskRepositoryMock.findById(taskId))
                .thenReturn(Optional.of(labelTask));
        Mockito.when(labelTask.getProject())
                .thenReturn(project);
        Mockito.when(labelTask.getLabelTaskState())
                .thenReturn(LabelTaskState.COMPLETED);

        try {
            underTest.streamSegmentationBitmap(taskId, lcEntryKey, response);
            fail("NotFoundException should be thrown.");
        } catch (NotFoundException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void testDeletedImage() throws IOException {
        // Given
        final String taskId = "mocked task id";
        final String lcEntryKey = "existingEntryId";
        final String deletedImageId = "deleted image";
        final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        final LabelTask labelTask = Mockito.mock(LabelTask.class);
        final Media media = Mockito.mock(Media.class);
        final String expectedErrorMessage = String.format("Item image with id %s not found.", deletedImageId);
        final LcEntry lcEntry = Mockito.mock(LcEntry.class);
        final Project project = ProjectUtils.createTestProject(PROJECT_ID);
        project.setOwner(loggedInUserId);
        project.getLabelConfiguration().setEntries(Set.of(lcEntry));

        // When
        Mockito.when(labelTaskRepositoryMock.findById(taskId)).thenReturn(Optional.of(labelTask));
        Mockito.when(labelTask.getProject()).thenReturn(project);
        Mockito.when(labelTask.getLabelTaskState()).thenReturn(LabelTaskState.COMPLETED);
        Mockito.when(labelTask.getMedia()).thenReturn(media);
        Mockito.when(lcEntry.getEntryKey()).thenReturn(lcEntryKey);
        Mockito.when(media.isDeleted()).thenReturn(true);
        Mockito.when(media.getId()).thenReturn(deletedImageId);

        try {
            underTest.streamSegmentationBitmap(taskId, lcEntryKey, response);
            fail("NotFoundException should be thrown.");
        } catch (NotFoundException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    // Disable tempoary because of the opencv error on jenkins
    /*
    @Test
    @Disabled
    public void testStreamSegmentationBitmap() throws IOException {
        // Given
        final int width = 50;
        final int height = 50;
        final String taskId = "mocked task id";
        final String lcEntryKey = "existingEntryId";
        final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        final LabelTask labelTask = Mockito.mock(LabelTask.class);
        final LcEntry lcEntry = Mockito.mock(LcEntry.class);
        final ServletOutputStream outputStream = Mockito.mock(ServletOutputStream.class);

        final Project project = ProjectUtils.createTestProject(PROJECT_ID);
        project.setOwner(loggedInUserId);
        project.getLabelConfiguration().setEntries(Set.of(lcEntry));


        final LocalImage image = new LocalImage();
        image.setMediaSourceType(MediaSourceType.LOCAL);
        image.setWidth(width);
        image.setHeight(height);
        image.setDeleted(false);

        // When
        Mockito.when(labelTaskRepositoryMock.findById(taskId)).thenReturn(Optional.of(labelTask));
        Mockito.when(labelTask.getProject()).thenReturn(project);
        Mockito.when(labelTask.getLabelTaskState()).thenReturn(LabelTaskState.COMPLETED);
        Mockito.when(labelTask.getMedia()).thenReturn(image);
        Mockito.when(lcEntry.getEntryKey()).thenReturn(lcEntryKey);
        Mockito.when(response.getOutputStream()).thenReturn(outputStream);

        underTest.streamSegmentationBitmap(taskId, lcEntryKey, response);

        // Then
        Mockito.verify(response, times(1)).setHeader("Content-Type", "image/bmp");
        Mockito.verify(response, times(1)).getOutputStream();
    }*/
}
