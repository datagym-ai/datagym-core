package ai.datagym.application.media.controller;

import ai.datagym.application.media.models.viewModels.MediaViewModel;
import ai.datagym.application.media.service.MediaService;
import ai.datagym.application.testUtils.ImageUtils;
import com.eforce21.cloud.login.client.crypt.KeyProviderJwks;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.Set;

import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration
@WebAppConfiguration
@SpringBootTest
@MockBean(KeyProviderJwks.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class MediaControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private MediaService mediaServiceMock;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(print())
                .build();
    }

    @Test
    void givenWac_whenServletContext_thenItProvidesImageController() {
        WebApplicationContext webApplicationContext = mockMvc.getDispatcherServlet().getWebApplicationContext();
        ServletContext servletContext = mockMvc.getDispatcherServlet().getWebApplicationContext().getServletContext();

        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(webApplicationContext.getBean("mediaController"));
    }

    @Test
    void streamImageFile_whenInputsAreValid_200OK() throws Exception {
        mockMvc.perform(get("/api/media/{mediaId}", IMAGE_ID)
                .param("dl", "false"))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        verify(mediaServiceMock).streamMediaFile(idCapture.capture(), any(HttpServletResponse.class), anyBoolean());
        verify(mediaServiceMock, times(1)).streamMediaFile(anyString(), any(HttpServletResponse.class), anyBoolean());
        assertThat(idCapture.getValue()).isEqualTo(IMAGE_ID);

        verifyNoMoreInteractions(mediaServiceMock);
    }

    @Test
    void streamImageFile_whenImageIdIsEmptyString_throwException() throws Exception {
        mockMvc.perform(get("/api/media/{imageId}", "")
                .param("dl", "false"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteImage_whenImageIdIsValid_200OK() throws Exception {
        MediaViewModel testMediaViewModel = ImageUtils.createTestImageViewModel(IMAGE_ID);
        when(mediaServiceMock.deleteMediaFile(anyString(), eq(true))).thenReturn(testMediaViewModel);

        mockMvc.perform(delete("/api/media/{imageId}", IMAGE_ID))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> deleteImageCapture = ArgumentCaptor.forClass(Boolean.class);
        verify(mediaServiceMock, times(1)).deleteMediaFile(idCapture.capture(), deleteImageCapture.capture());
        assertThat(idCapture.getValue()).isEqualTo(IMAGE_ID);
        assertThat(deleteImageCapture.getValue()).isEqualTo(true);

        verifyNoMoreInteractions(mediaServiceMock);
    }

    @Test
    void deleteImage_whenImageIdIsEmptyString_throwException() throws Exception {
        mockMvc.perform(delete("/api/media/{imageId}", ""))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteImageList_whenImageIdIsValid_200OK() throws Exception {
        Set<String> testImageIdSet = ImageUtils.createTestImageIdSet(2);

        // When
        doNothing()
                .when(mediaServiceMock)
                .deleteMediaFileList(anySet(), eq(true));

        String requestBody = objectMapper.writeValueAsString(testImageIdSet);

        mockMvc.perform(delete("/api/media/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ArgumentCaptor<Set<String>> idSetCapture = ArgumentCaptor.forClass(HashSet.class);
        ArgumentCaptor<Boolean> deleteImageCapture = ArgumentCaptor.forClass(Boolean.class);

        verify(mediaServiceMock, times(1))
                .deleteMediaFileList(idSetCapture.capture(), deleteImageCapture.capture());
        assertThat(idSetCapture.getValue().size()).isEqualTo(testImageIdSet.size());
        assertThat(deleteImageCapture.getValue()).isEqualTo(true);

        verifyNoMoreInteractions(mediaServiceMock);
    }

    @Test
    void restoreImage_whenImageIdIsValid_200OK() throws Exception {
        MediaViewModel testMediaViewModel = ImageUtils.createTestImageViewModel(IMAGE_ID);
        when(mediaServiceMock.deleteMediaFile(anyString(), eq(false))).thenReturn(testMediaViewModel);

        mockMvc.perform(delete("/api/media/{imageId}/restore", IMAGE_ID))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<String> idCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> deleteImageCapture = ArgumentCaptor.forClass(Boolean.class);
        verify(mediaServiceMock, times(1)).deleteMediaFile(idCapture.capture(), deleteImageCapture.capture());
        assertThat(idCapture.getValue()).isEqualTo(IMAGE_ID);
        assertThat(deleteImageCapture.getValue()).isEqualTo(false);

        verifyNoMoreInteractions(mediaServiceMock);
    }

    @Test
    void restoreImage_whenImageIdIsEmptyString_throwException() throws Exception {
        Exception resolvedException = mockMvc.perform(delete("/api/media/{imageId}/restore", " "))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }

    @Test
    void permanentDeleteImageFile_whenImageIdIsValid_200OK() throws Exception {
        doNothing().when(mediaServiceMock).permanentDeleteMediaFile(anyString());

        mockMvc
                .perform(delete("/api/media/{id}/deleteFromDb", IMAGE_ID))
                .andDo(print())
                .andExpect(status().isOk());

        verify(mediaServiceMock, times(1)).permanentDeleteMediaFile(anyString());
        verifyNoMoreInteractions(mediaServiceMock);
    }

    @Test
    void permanentDeleteImageFile_whenImageIdIsValid_throwException() throws Exception {
        Exception resolvedException = mockMvc.perform(delete("/api/media/{imageId}/deleteFromDb", " "))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        Assertions.assertEquals(ConstraintViolationException.class, resolvedException.getClass());
    }
}