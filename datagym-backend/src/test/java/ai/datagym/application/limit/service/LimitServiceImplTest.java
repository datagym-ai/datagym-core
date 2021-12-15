package ai.datagym.application.limit.service;

import ai.datagym.application.accountmanagement.client.AccountManagementClient;
import ai.datagym.application.limit.entity.DataGymPlan;
import ai.datagym.application.limit.entity.Limit;
import ai.datagym.application.limit.exception.LimitException;
import ai.datagym.application.limit.models.DataGymPlanDetails;
import ai.datagym.application.limit.models.bindingModels.LimitSetPricingPlanBindingModel;
import ai.datagym.application.limit.models.viewModels.LimitPricingPlanViewModel;
import ai.datagym.application.limit.repo.LimitRepository;
import ai.datagym.application.security.service.UserInfoService;
import ai.datagym.application.testUtils.LimitsUtils;
import ai.datagym.application.testUtils.SecurityUtils;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static ai.datagym.application.limit.entity.DataGymPlan.FREE_DEVELOPER;
import static ai.datagym.application.testUtils.LimitsUtils.ORGANISATION_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class LimitServiceImplTest {
    private LimitService limitService;

    @Value(value = "${limit-service.test.datagym.deactivate-limiter}")
    boolean deactivateLimiter;

    @Mock
    private LimitRepository limitRepositoryMock;

    @Mock
    private AccountManagementClient accountManagementClientMock;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        limitService = new LimitServiceImpl(limitRepositoryMock, deactivateLimiter, Optional.of(accountManagementClientMock));

        // Setup DatagymPlan with reflection
        Field datagymPlan = LimitServiceImpl.class.getDeclaredField("datagymPlan");
        datagymPlan.setAccessible(true);

        Map<DataGymPlan, DataGymPlanDetails> datagymPlanMap = new HashMap<>();
        datagymPlanMap.put(FREE_DEVELOPER, new DataGymPlanDetails(FREE_DEVELOPER));

        // Set the private field datagymPlan to the value of datagymPlanMap
        datagymPlan.set(limitService, datagymPlanMap);
    }

    @Test
    void resetPricingPlan_whenInputIsValid_resetPricingPlan() throws NoSuchFieldException, IllegalAccessException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        LimitSetPricingPlanBindingModel tesLimitSetPricingPlanBindingModel = LimitsUtils.createTesLimitSetPricingPlanBindingModel();

        Limit tesLimit = LimitsUtils.createTesLimit();

        // When
        when(limitRepositoryMock.findByOrganisationId(anyString()))
                .thenReturn(java.util.Optional.of(tesLimit));

        limitService.resetPricingPlan(tesLimitSetPricingPlanBindingModel);

        // Then
        verify(limitRepositoryMock, times(1)).findByOrganisationId(anyString());
    }

    @Test
    void resetPricingPlan_whenOrgIdIsInvalid_createNewLimit() throws NoSuchFieldException, IllegalAccessException {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        LimitSetPricingPlanBindingModel tesLimitSetPricingPlanBindingModel = LimitsUtils.createTesLimitSetPricingPlanBindingModel();

        Limit tesLimit = LimitsUtils.createTesLimit();

        limitService.resetPricingPlan(tesLimitSetPricingPlanBindingModel);

        verify(limitRepositoryMock, times(1)).findByOrganisationId(anyString());
    }

    @Test
    void getLimitsByOrgId_whenOrgIdIsInvalid_getLimitsByOrgId() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Limit tesLimit = LimitsUtils.createTesLimit();
        LimitPricingPlanViewModel tesLimitPricingPlanViewModel = LimitsUtils.createTesLimitPricingPlanViewModel();

        //When
        when(limitRepositoryMock.findByOrganisationId(anyString()))
                .thenReturn(java.util.Optional.of(tesLimit));

        LimitPricingPlanViewModel limitsByOrgId = limitService.getLimitsByOrgId(ORGANISATION_ID);

        //Then
        assertNotNull(limitsByOrgId);
        assertEquals(tesLimitPricingPlanViewModel.getOrganisationId(), limitsByOrgId.getOrganisationId());
        assertEquals(tesLimitPricingPlanViewModel.getPricingPlanType(), limitsByOrgId.getPricingPlanType());
        assertEquals(tesLimitPricingPlanViewModel.getProjectLimit(), limitsByOrgId.getProjectLimit());
        assertEquals(tesLimitPricingPlanViewModel.getProjectUsed(), limitsByOrgId.getProjectUsed());
        assertEquals(tesLimitPricingPlanViewModel.getLabelLimit(), limitsByOrgId.getLabelLimit());
        assertEquals(tesLimitPricingPlanViewModel.getLabelRemaining(), limitsByOrgId.getLabelRemaining());
        assertEquals(tesLimitPricingPlanViewModel.getStorageLimit(), limitsByOrgId.getStorageLimit());
        assertEquals(tesLimitPricingPlanViewModel.getStorageUsed(), limitsByOrgId.getStorageUsed());
        assertEquals(tesLimitPricingPlanViewModel.getAiSegLimit(), limitsByOrgId.getAiSegLimit());
        assertEquals(tesLimitPricingPlanViewModel.getAiSegRemaining(), limitsByOrgId.getAiSegRemaining());
        assertEquals(tesLimitPricingPlanViewModel.isApiAccess(), limitsByOrgId.isApiAccess());
        assertEquals(tesLimitPricingPlanViewModel.isExternalStorage(), limitsByOrgId.isExternalStorage());

        verify(limitRepositoryMock).findByOrganisationId(anyString());
        verify(limitRepositoryMock, times(1)).findByOrganisationId(anyString());
        verifyNoMoreInteractions(limitRepositoryMock);
    }

    @Test
    void getLimitsByOrgId_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        assertThrows(ForbiddenException.class,
                () -> limitService.getLimitsByOrgId(ORGANISATION_ID)
        );
    }

    @Test
    void getProject_whenUserIsNotAuthorized_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithInvalidRole();
        SecurityContext.set(oauthUser);

        //When
        assertThrows(ForbiddenException.class,
                () -> limitService.getLimitsByOrgId(ORGANISATION_ID)
        );
    }

    @Test
    void getProject_whenUserIsNotInTheOrg_throwException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        //When
        assertThrows(ForbiddenException.class,
                () -> limitService.getLimitsByOrgId("test_org")
        );
    }

    @Test
    void increaseUsedProjectsCount_whenOrgIdIsInvalid_increaseUsedProjectsCount() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Limit tesLimit = LimitsUtils.createTesLimit();

        //When
        when(limitRepositoryMock.findByOrganisationId(anyString()))
                .thenReturn(java.util.Optional.of(tesLimit));

        limitService.increaseUsedProjectsCount(ORGANISATION_ID);

        //Then
        ArgumentCaptor<Limit> limitArgumentCaptor = ArgumentCaptor.forClass(Limit.class);
        verify(limitRepositoryMock).save(limitArgumentCaptor.capture());
        verify(limitRepositoryMock, times(1)).save(any(Limit.class));

        assertEquals(tesLimit.getProjectUsed(), limitArgumentCaptor.getValue().getProjectUsed());

        verify(limitRepositoryMock).findByOrganisationId(anyString());
        verify(limitRepositoryMock, times(1)).findByOrganisationId(anyString());
    }

    @Test
    void increaseUsedProjectsCount_whenLimitIsReached_throwsException() {
        // Set Security Context
        OauthUser oauthUser = SecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        // Given
        Limit tesLimit = LimitsUtils.createTesLimit();
        tesLimit.setProjectUsed(tesLimit.getProjectLimit());

        //When
        when(limitRepositoryMock.findByOrganisationId(anyString()))
                .thenReturn(java.util.Optional.of(tesLimit));

        assertThrows(LimitException.class,
                () ->limitService.increaseUsedProjectsCount(ORGANISATION_ID)
        );
    }

    @Test
    void increaseUsedProjectsCount_whenUserIsNotAuthenticated_throwException() {
        // Set Security Context
        SecurityContext.set(null);

        assertThrows(ForbiddenException.class,
                () -> limitService.increaseUsedProjectsCount(ORGANISATION_ID)
        );
    }


//    @Test
//    void decreaseUsedProjectsCount() {
//    }
//
//    @Test
//    void increaseUsedLabelsCount() {
//    }
//
//    @Test
//    void decreaseUsedLabelsCount() {
//    }
//
//    @Test
//    void increaseUsedStorage() {
//    }
//
//    @Test
//    void decreaseUsedStorage() {
//    }
//
//    @Test
//    void increaseAiSegRemaining() {
//    }
//
//    @Test
//    void decreaseAiSegRemaining() {
//    }
//
//    @Test
//    void checkAiSegLimits() {
//    }
//
//    @Test
//    void resetFreePlans() {
//    }
}