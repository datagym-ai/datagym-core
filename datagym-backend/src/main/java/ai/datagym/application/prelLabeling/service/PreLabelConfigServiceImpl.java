package ai.datagym.application.prelLabeling.service;

import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelConfiguration.models.viewModels.LcEntryPreLabelViewModel;
import ai.datagym.application.labelConfiguration.repo.LcEntryRepository;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.entity.LabelTaskType;
import ai.datagym.application.labelTask.entity.PreLabelState;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.limit.entity.DataGymPlan;
import ai.datagym.application.limit.entity.Limit;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.prelLabeling.entity.PreLabelConfiguration;
import ai.datagym.application.prelLabeling.entity.PreLabelMappingEntry;
import ai.datagym.application.prelLabeling.models.bindingModels.PreLabelConfigUpdateBindingModel;
import ai.datagym.application.prelLabeling.models.bindingModels.PreLabelLabelMappingsBindingModel;
import ai.datagym.application.prelLabeling.models.viewModels.PreLabelInfoViewModel;
import ai.datagym.application.prelLabeling.models.viewModels.PreLabelMappingEntryViewModel;
import ai.datagym.application.prelLabeling.repo.PreLabelConfigRepository;
import ai.datagym.application.prelLabeling.repo.PreLabelMappingEntryRepository;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.repo.ProjectRepository;
import ai.datagym.application.security.util.DataGymSecurity;
import ai.datagym.application.security.util.DataGymSecurityUtils;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static ai.datagym.application.utils.constants.CommonMessages.OAUTH_SCOPE_TYPE;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class PreLabelConfigServiceImpl implements PreLabelConfigService {
    public static final String NETWORK_CLASSES_LOADING = "network_classes_loading";
    private static final Logger LOGGER = LoggerFactory.getLogger(PreLabelConfigServiceImpl.class);

    private final PreLabelConfigRepository preLabelConfigRepository;
    private final ProjectRepository projectRepository;
    private final PreLabelMappingEntryRepository preLabelMappingEntryRepository;
    private final LimitService limitService;
    private final LcEntryRepository lcEntryRepository;
    private final LabelTaskRepository labelTaskRepository;
    private final PreLabelScheduleService preLabelScheduleService;
    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;
    private final ApplicationContext applicationContext;

    @Autowired
    public PreLabelConfigServiceImpl(PreLabelConfigRepository preLabelConfigRepository,
                                     ProjectRepository projectRepository,
                                     PreLabelMappingEntryRepository preLabelMappingEntryRepository, LimitService limitService,
                                     LcEntryRepository lcEntryRepository,
                                     LabelTaskRepository labelTaskRepository,
                                     @Autowired(required = false) PreLabelScheduleService preLabelScheduleService, ObjectMapper objectMapper,
                                     ModelMapper modelMapper, ApplicationContext applicationContext) {
        this.preLabelConfigRepository = preLabelConfigRepository;
        this.preLabelMappingEntryRepository = preLabelMappingEntryRepository;
        this.lcEntryRepository = lcEntryRepository;
        this.labelTaskRepository = labelTaskRepository;
        this.preLabelScheduleService = preLabelScheduleService;
        this.objectMapper = objectMapper;
        this.projectRepository = projectRepository;
        this.limitService = limitService;
        this.modelMapper = modelMapper;
        this.applicationContext = applicationContext;
    }

    private PreLabelConfigServiceImpl getSpringProxy() {
        return applicationContext.getBean(this.getClass());
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public PreLabelInfoViewModel getPreLabelInfoByProject(String projectId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        // Disallow open core
        DataGymSecurity.disallowOnOpenCore();

        Project projectById = getProjectById(projectId);

        //Permissions check
        String owner = projectById.getOwner();
        DataGymSecurity.isAdmin(owner, true);

        // Get Pricing Plan Type for the current Organisation
        String pricingPlanType = limitService.getLimitsByOrgId(owner).getPricingPlanType();

        //Get AiSegLimit and AiSegRemainung for current organisation
        Limit limitByOrganisationId = limitService.getLimitByOrganisationIdRequired(owner);
        int aiSegLimit = limitByOrganisationId.getAiSegLimit();
        int aiSegRemaining = limitByOrganisationId.getAiSegRemaining();

        if (!pricingPlanType.equals(DataGymPlan.TEAM_PRO.name())) {
            throw new GenericException("prelabeling_not_allowed", null, null);
        }

        PreLabelConfiguration preLabelConfiguration = getPreLabelConfigByProjectId(projectById);

        PreLabelInfoViewModel preLabelViewModel = new PreLabelInfoViewModel();

        List<LcEntryPreLabelViewModel> availableGeometries = projectById.getLabelConfiguration()
                .getEntries()
                .stream()
                .filter(lcEntry -> lcEntry.getParentEntry() == null)
                .filter(lcEntry -> lcEntry.getType().equals(LcEntryType.RECTANGLE) || lcEntry.getType().equals(LcEntryType.POLYGON))
                .map(lcEntry -> modelMapper.map(lcEntry, LcEntryPreLabelViewModel.class))
                .collect(Collectors.toList());

        long countWaiting = labelTaskRepository.countProjectTasksWhereMediasNotDeletedByPreLabelState(projectId, PreLabelState.WAITING);

        long countReady = labelTaskRepository.countProjectTasksByStateAndPrelabelstateNullWhereMediasNotDeleted(projectId, LabelTaskState.WAITING) +
                labelTaskRepository.countProjectTasksByStateAndPrelabelstateNullWhereMediasNotDeleted(projectId, LabelTaskState.WAITING_CHANGED);

        long countFinished = labelTaskRepository.countProjectTasksWhereMediasNotDeletedByPreLabelState(projectId, PreLabelState.FINISHED);

        long countFailed = labelTaskRepository.countProjectTasksWhereMediasNotDeletedByPreLabelState(projectId, PreLabelState.FAILED);

        List<PreLabelMappingEntryViewModel> mappingEntryViewModels = preLabelConfiguration.getMappings()
                .stream()
                .map(PreLabelingMapper::mapToPreLabelMappingEntryViewModel)
                .collect(Collectors.toList());

        preLabelViewModel.setAvailableGeometries(availableGeometries);
        preLabelViewModel.setActivePreLabeling(preLabelConfiguration.isActivateState());
        preLabelViewModel.setCountWaitingTasks(countWaiting);
        preLabelViewModel.setCountReadyTasks(countReady);
        preLabelViewModel.setCountFinishedTasks(countFinished);
        preLabelViewModel.setCountFailedTasks(countFailed);
        preLabelViewModel.setAiSegLimit(aiSegLimit);
        preLabelViewModel.setAiSegRemaining(aiSegRemaining);
        preLabelViewModel.setPreLabelMappings(mappingEntryViewModels);

        try {
            // Get the URL of network_classes.json
            URL projectResourceUrl = PreLabelConfigServiceImpl.class
                    .getClassLoader()
                    .getResource("preLabeling/network_classes.json");

            // Get the current Network Classes
            TypeReference<LinkedHashMap<String, String>> typeRef = new TypeReference<>() {
            };
            LinkedHashMap<String, String> availableNetworkClasses = objectMapper.readValue(projectResourceUrl, typeRef);

            preLabelViewModel.setAvailableNetworkClasses(availableNetworkClasses);
        } catch (Exception e) {
            throw new GenericException(NETWORK_CLASSES_LOADING, null, null);
        }

        return preLabelViewModel;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public PreLabelInfoViewModel updatePreLabelConfigByProject(String projectId, PreLabelConfigUpdateBindingModel preLabelConfigUpdateBindingModel) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);
        // Disallow open core
        DataGymSecurity.disallowOnOpenCore();

        Project projectById = getProjectById(projectId);

        //Permissions check
        String owner = projectById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        // Get Pricing Plan Type for the current Organisation
        String pricingPlanType = limitService.getLimitsByOrgId(owner).getPricingPlanType();

        if (!pricingPlanType.equals(DataGymPlan.TEAM_PRO.name())) {
            throw new GenericException("prelabeling_not_allowed", null, null);
        }

        PreLabelConfiguration preLabelConfiguration = getPreLabelConfigByProjectId(projectById);

        boolean activateState = preLabelConfigUpdateBindingModel.isActivateState();
        preLabelConfiguration.setActivateState(activateState);

        Map<String, List<PreLabelLabelMappingsBindingModel>> mappings = preLabelConfigUpdateBindingModel.getMappings();

        if (mappings != null) updatePreLabelConfigMapping(preLabelConfiguration, mappings);

        if (activateState) {
            // Update preLabelTaskState from NULL to "WAITING" for all ("LabelTaskState.WAITING" or
            // "LabelTaskState.WAITING_CHANGED") && "LabelTaskType.DEFAULT" Tasks
            labelTaskRepository.setPrelabelStateWaitingForPrelabeling(projectId);
        } else {
            // Update preLabelTaskState from "WAITING" to NULL for all Tasks
            labelTaskRepository.setPreLabelStateNullForWaiting(projectId);
        }

        return getPreLabelInfoByProject(projectId);
    }

    private void updatePreLabelConfigMapping(PreLabelConfiguration preLabelConfiguration, Map<String, List<PreLabelLabelMappingsBindingModel>> mappings) {
        // Update PreLabelConfiguration with the new PreLabelMappingEntries
        preLabelConfiguration.getMappings().clear();

        for (Map.Entry<String, List<PreLabelLabelMappingsBindingModel>> labelMappingsBindingModelEntry : mappings.entrySet()) {
            String lcEntryId = labelMappingsBindingModelEntry.getKey();

            LcEntry lcEntryById = getLcEntryById(lcEntryId);

            lcEntryById.getPreLabelMappingEntries().clear();

            List<PreLabelLabelMappingsBindingModel> preLabelLabelMappingsBindingModels = labelMappingsBindingModelEntry.getValue();

            for (PreLabelLabelMappingsBindingModel bindingModel : preLabelLabelMappingsBindingModels) {
                String preLabelClassKey = bindingModel.getPreLabelClassKey();
                String preLabelModel = bindingModel.getPreLabelModel();

                createPreLabelMappingEntry(preLabelConfiguration, lcEntryById, preLabelClassKey, preLabelModel);
            }
        }
    }

    private void createPreLabelMappingEntry(PreLabelConfiguration preLabelConfiguration,
                                            LcEntry lcEntryById,
                                            String preLabelClassKey,
                                            String preLabelModel) {
        PreLabelMappingEntry labelMappingEntry = new PreLabelMappingEntry();

        labelMappingEntry.setLcEntry(lcEntryById);
        labelMappingEntry.setPreLabelClassKey(preLabelClassKey);
        labelMappingEntry.setPreLabelModel(preLabelModel);
        labelMappingEntry.setPreLabelConfig(preLabelConfiguration);

        preLabelConfiguration.getMappings().add(labelMappingEntry);
        lcEntryById.getPreLabelMappingEntries().add(labelMappingEntry);

        preLabelMappingEntryRepository.save(labelMappingEntry);
    }

    private PreLabelConfiguration getPreLabelConfigByProjectId(Project project) {
        String projectId = project.getId();

        Optional<PreLabelConfiguration> optionalPreLabelConfig = preLabelConfigRepository
                .findByProjectId(projectId);

        if (optionalPreLabelConfig.isEmpty()) {
            PreLabelConfiguration preLabelConfiguration = new PreLabelConfiguration();
            preLabelConfiguration.setActivateState(false);
            preLabelConfiguration.setMappings(new ArrayList<>());
            preLabelConfiguration.setProject(project);
            PreLabelConfiguration savedPreLabelConfiguration = preLabelConfigRepository.saveAndFlush(preLabelConfiguration);

            project.setPreLabelConfiguration(savedPreLabelConfiguration);

            return savedPreLabelConfiguration;
        }

        return optionalPreLabelConfig.get();
    }

    private Project getProjectById(String projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() -> new NotFoundException("project", "id", "" + projectId));
    }

    private LcEntry getLcEntryById(String entryId) {
        return lcEntryRepository.findById(entryId)
                .orElseThrow(() -> new NotFoundException("Entry", "id", "" + entryId));
    }

    //Disable scheduler by default
    @Scheduled(cron = "${aiseglb.prelabel.scheduler:-}")
    @Transactional(propagation = Propagation.NEVER)
    public void preLabelScheduleJob() {
        // Create OauthUser for the Authentication of the CronJob
        OauthUser oauthUser = DataGymSecurityUtils.createOauthUserWithTwoOrgsAndWithValues();
        SecurityContext.set(oauthUser);

        boolean present;

        do {
            present = getSpringProxy().preLabelNextTask();
        } while (present);
    }


    public boolean preLabelNextTask() {
        // Additional check if scheduler is available
        if (this.preLabelScheduleService == null) {
            return false;
        }
        Optional<LabelTask> nextLabelTaskToBePreLabeled = labelTaskRepository
                .findFirstByPreLabelStateAndLabelTaskTypeAndLabelTaskStateAndMediaDeleted(
                        PreLabelState.WAITING,
                        LabelTaskType.DEFAULT,
                        LabelTaskState.WAITING,
                        false);

        boolean present = nextLabelTaskToBePreLabeled.isPresent();

        if (!present) {
            return false;
        }

        LabelTask labelTask = nextLabelTaskToBePreLabeled.get();
        Media media = labelTask.getMedia();
        String mediaId = media.getId();
        String projectId = labelTask.getProject().getId();
        Project projectById = getProjectById(projectId);
        String owner = projectById.getOwner();

        try {
            List<PreLabelMappingEntry> mappings = labelTask
                    .getProject()
                    .getPreLabelConfiguration()
                    .getMappings();

            Map<String, Map<String, String>> requestedClasses = mappings
                    .stream()
                    .collect(Collectors.toMap(PreLabelMappingEntry::getPreLabelClassKey,
                            PreLabelMappingEntry::getClassTypeMapping));

            LOGGER.info("PreLabeling: Start preLabeling LabelTask with Id: {}", labelTask.getId());

            Limit limitByOrganisationId = limitService.getLimitByOrganisationIdRequired(owner);

            int aiSegRemaining = limitByOrganisationId.getAiSegRemaining();

            if (aiSegRemaining > 0 || aiSegRemaining == -1) {
                // PreLabeling the current Image
                preLabelScheduleService.preLabelMedia(media, requestedClasses, labelTask, mappings, owner);
            }else{
                // Update labelTask from "WAITING" to NULL for this task
                labelTask.setPreLabelState(null);
            }
            LOGGER.info("PreLabeling: End preLabeling LabelTask with Id: {}", labelTask.getId());

        } catch (Exception e) {
            LOGGER.error("PreLabeling: Failed during preLabeling media with Id: {}, LabelTaskId: {}, errorMessage: {}", mediaId, labelTask.getId(), e.getMessage());
        }

        return true;
    }
}
