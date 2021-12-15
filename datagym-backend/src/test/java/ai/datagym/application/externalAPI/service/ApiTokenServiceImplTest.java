package ai.datagym.application.externalAPI.service;

import ai.datagym.application.externalAPI.entity.ApiToken;
import ai.datagym.application.externalAPI.models.bindingModels.ApiTokenCreateBindingModel;
import ai.datagym.application.externalAPI.models.viewModels.ApiTokenViewModel;
import ai.datagym.application.externalAPI.repo.ApiTokenRepository;
import ai.datagym.application.labelConfiguration.service.LabelConfigurationService;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.testUtils.ApiTokenUtils;
import ai.datagym.application.testUtils.ProjectUtils;
import ai.datagym.application.testUtils.SecurityUtils;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.ForbiddenException;
import com.eforce21.lib.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static ai.datagym.application.testUtils.ApiTokenUtils.API_TOKEN_ID;
import static ai.datagym.application.testUtils.ApiTokenUtils.API_TOKEN_NAME;
import static ai.datagym.application.testUtils.ProjectUtils.PROJECT_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class ApiTokenServiceImplTest {
    private ApiTokenService apiTokenService;

    @Value(value = "${datagym.deactivate-limiter}")
    private boolean deactivateLimiter;

    @Mock
    private ApiTokenRepository apiTokenRepositoryMock;

    @Mock
    private LabelConfigurationService labelConfigurationServiceMock;

    @Mock
    private LimitService limitService;

    @BeforeEach
    void setUp() {
        apiTokenService = new ApiTokenServiceImpl(
                apiTokenRepositoryMock,
                labelConfigurationServiceMock,
                limitService,
                deactivateLimiter);
    }

    @Test
    void getApiTokenWhereUserAdmin_When2ApiTokens_2ApiTokens() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        List<ApiToken> testApiTokenViewModels = ApiTokenUtils.createTestApiTokenLists(2);

        //When
        when(apiTokenRepositoryMock.findAllByDeletedIsFalseAndOwner(anyString()))
                .thenReturn(testApiTokenViewModels);

        List<ApiTokenViewModel> apiTokenViewModels = apiTokenService.getApiTokenWhereUserAdmin();

        //Then
        ApiToken expected = testApiTokenViewModels.get(0);
        ApiTokenViewModel actual = apiTokenViewModels.get(0);

        assertEquals(2, apiTokenViewModels.size());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getOwner(), actual.getOwner());
        assertEquals(expected.isDeleted(), actual.isDeleted());

        verify(apiTokenRepositoryMock).findAllByDeletedIsFalseAndOwner(anyString());
        verify(apiTokenRepositoryMock, times(1)).findAllByDeletedIsFalseAndOwner(anyString());
        verifyNoMoreInteractions(apiTokenRepositoryMock);
    }

    @Test
    void getApiTokenWhereUserAdmin_WhenNoApiTokens_returnEmptyList() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //When
        when(apiTokenRepositoryMock.findAllByDeletedIsFalseAndOwner(anyString()))
                .thenReturn(new ArrayList<>());

        List<ApiTokenViewModel> apiTokenViewModels = apiTokenService.getApiTokenWhereUserAdmin();

        //Then
        assertTrue(apiTokenViewModels.isEmpty());

        verify(apiTokenRepositoryMock).findAllByDeletedIsFalseAndOwner(anyString());
        verify(apiTokenRepositoryMock, times(1)).findAllByDeletedIsFalseAndOwner(anyString());
        verifyNoMoreInteractions(apiTokenRepositoryMock);
    }

    @Test
    void getApiTokenWhereUserAdmin_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> apiTokenService.getApiTokenWhereUserAdmin()
        );
    }

    @Test
    void getApiTokenWhereUserAdmin_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(ForbiddenException.class,
                () -> apiTokenService.getApiTokenWhereUserAdmin()
        );
    }

    @Test
    void createApiToken_whenInputIsValidAndUserIsAuthorized_createProject() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        ApiTokenCreateBindingModel testApiTokenCreateBindingModel = ApiTokenUtils.createTestApiTokenCreateBindingModel();
        Project testProject = ProjectUtils.createTestProject(PROJECT_ID);

        when(apiTokenRepositoryMock.saveAndFlush(any(ApiToken.class))).then(returnsFirstArg());

        ApiTokenViewModel apiTokenViewModel = apiTokenService.createApiToken(testApiTokenCreateBindingModel);

        // Then
        assertEquals(testApiTokenCreateBindingModel.getName(), apiTokenViewModel.getName());
        assertEquals(testApiTokenCreateBindingModel.getOwner(), apiTokenViewModel.getOwner());

        verify(apiTokenRepositoryMock).saveAndFlush(any(ApiToken.class));
        verify(apiTokenRepositoryMock, times(1)).saveAndFlush(any(ApiToken.class));
        verifyNoMoreInteractions(apiTokenRepositoryMock);
    }

    @Test
    void createApiToken_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        ApiTokenCreateBindingModel testApiTokenCreateBindingModel = ApiTokenUtils.createTestApiTokenCreateBindingModel();

        //Then
        assertThrows(ForbiddenException.class,
                () -> apiTokenService.createApiToken(testApiTokenCreateBindingModel)
        );
    }

    @Test
    void createApiToken_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(ForbiddenException.class,
                () -> apiTokenService.getApiTokenWhereUserAdmin()
        );
    }

    @Test
    void createApiToken_whenInputIsNull_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        Assertions.assertThrows(NullPointerException.class,
                () -> apiTokenService.createApiToken(null)
        );
    }

    @Test
    void isApiTokenNameUniqueAndDeletedFalse_whenApiTokenNameIsUnique_returnTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        ApiToken apiToken = ApiTokenUtils.createTestApiToken();

        //When
        when(apiTokenRepositoryMock.findByNameAndDeletedFalseAndOwner(anyString(), anyString()))
                .thenReturn(java.util.Optional.of(apiToken));

        boolean actual = apiTokenService.isApiTokenNameUniqueAndDeletedFalse(API_TOKEN_NAME, "eforce");

        //Then
        Assertions.assertFalse(actual);

        verify(apiTokenRepositoryMock).findByNameAndDeletedFalseAndOwner(anyString(), anyString());
        verify(apiTokenRepositoryMock, times(1))
                .findByNameAndDeletedFalseAndOwner(anyString(), anyString());
        verifyNoMoreInteractions(apiTokenRepositoryMock);
    }

    @Test
    void isApiTokenNameUniqueAndDeletedFalse_whenApiTokenNameIsNotUnique_returnFalse() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        ApiToken apiToken = ApiTokenUtils.createTestApiToken();

        //When
        when(apiTokenRepositoryMock.findByNameAndDeletedFalseAndOwner(anyString(), anyString()))
                .thenReturn(java.util.Optional.empty());

        boolean actual = apiTokenService.isApiTokenNameUniqueAndDeletedFalse(API_TOKEN_NAME, "eforce");

        //Then
        Assertions.assertTrue(actual);

        verify(apiTokenRepositoryMock).findByNameAndDeletedFalseAndOwner(anyString(), anyString());
        verify(apiTokenRepositoryMock, times(1))
                .findByNameAndDeletedFalseAndOwner(anyString(), anyString());
        verifyNoMoreInteractions(apiTokenRepositoryMock);
    }

    @Test
    void isApiTokenNameUniqueAndDeletedFalse_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> apiTokenService.isApiTokenNameUniqueAndDeletedFalse(API_TOKEN_NAME, "eforce")
        );
    }

    @Test
    void isApiTokenNameUniqueAndDeletedFalse_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(ForbiddenException.class,
                () -> apiTokenService.isApiTokenNameUniqueAndDeletedFalse(API_TOKEN_NAME, "eforce")
        );
    }

    @Test
    void deleteApiTokenById_whenApiTokenIdIsValid_setDeletedToTrue() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Given
        ApiToken testApiToken = ApiTokenUtils.createTestApiToken();

        //when
        when(apiTokenRepositoryMock.findById(anyString())).thenReturn(java.util.Optional.of(testApiToken));
        when(apiTokenRepositoryMock.saveAndFlush(any(ApiToken.class))).then(returnsFirstArg());

        ApiTokenViewModel actual = apiTokenService.deleteApiTokenById(API_TOKEN_ID);

        //Then
        assertTrue(actual.isDeleted());

        assertNotNull(actual);
        assertEquals(testApiToken.getId(), actual.getId());
        assertEquals(testApiToken.getName(), actual.getName());
        assertEquals(testApiToken.getOwner(), actual.getOwner());
        assertEquals(testApiToken.isDeleted(), actual.isDeleted());

        verify(apiTokenRepositoryMock).saveAndFlush(any(ApiToken.class));
        verify(apiTokenRepositoryMock, times(1))
                .saveAndFlush(any(ApiToken.class));
    }

    @Test
    void deleteApiTokenById_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        //Then
        assertThrows(ForbiddenException.class,
                () -> apiTokenService.deleteApiTokenById(API_TOKEN_ID)
        );
    }

    @Test
    void deleteApiTokenById_whenUserHasWrongAuthScope_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createTestTokenUser();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(ForbiddenException.class,
                () -> apiTokenService.deleteApiTokenById(API_TOKEN_ID)
        );
    }

    @Test
    void deleteApiTokenById_whenApiTokenIdIsNotValid__throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //Then
        assertThrows(NotFoundException.class,
                () -> apiTokenService.deleteApiTokenById("invalid_api_token_id")
        );
    }
}