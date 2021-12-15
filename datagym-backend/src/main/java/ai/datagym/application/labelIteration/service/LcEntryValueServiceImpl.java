package ai.datagym.application.labelIteration.service;

import ai.datagym.application.labelConfiguration.entity.LabelConfiguration;
import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelConfiguration.entity.geometry.*;
import ai.datagym.application.labelConfiguration.repo.LabelConfigurationRepository;
import ai.datagym.application.labelConfiguration.repo.LcEntryRepository;
import ai.datagym.application.labelIteration.entity.LabelIteration;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.LcEntryValueChange;
import ai.datagym.application.labelIteration.entity.classification.LcEntryCheckListValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntrySelectValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntryTextValue;
import ai.datagym.application.labelIteration.entity.geometry.*;
import ai.datagym.application.labelIteration.factories.LcEntryValueViewModelsFactory;
import ai.datagym.application.labelIteration.models.bindingModels.*;
import ai.datagym.application.labelIteration.models.viewModels.LabelIterationViewModel;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;
import ai.datagym.application.labelIteration.repo.LabelIterationRepository;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelIteration.repo.PointPojoRepository;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.project.entity.MediaType;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.security.util.DataGymSecurity;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.lib.exception.AlreadyExistsException;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import io.micrometer.core.instrument.Metrics;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static ai.datagym.application.utils.constants.CommonMessages.*;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class LcEntryValueServiceImpl implements LcEntryValueService {
    private static final String ENTRY_VALUE_EXCEPTION = "entry_value_validation";
    private static final String ENTRY_DEFINITION_INCORRECT = "entry_definition_incorrect";
    private static final String ENTRY_TYPE_NOT_FOUND = "entry_type_not_found";
    private static final String ENTRY_TYPE = "entry type";
    private static final String ENTRY_TYPE_NOT_GEOMETRY = "entry_not_geometry";

    private boolean isDummyProject = false;

    private final LcEntryValueRepository lcEntryValueRepository;
    private final LabelIterationRepository labelIterationRepository;
    private final LcEntryRepository lcEntryRepository;
    private final MediaRepository mediaRepository;
    private final PointPojoRepository pointPojoRepository;
    private final LabelConfigurationRepository labelConfigurationRepository;
    private final LabelTaskRepository labelTaskRepository;
    private final LcEntryValidation lcEntryValidation;

    public LcEntryValueServiceImpl(LcEntryValueRepository lcEntryValueRepository,
                                   LabelIterationRepository labelIterationRepository,
                                   LcEntryRepository lcEntryRepository,
                                   MediaRepository mediaRepository,
                                   PointPojoRepository pointPojoRepository,
                                   LabelConfigurationRepository labelConfigurationRepository,
                                   LabelTaskRepository labelTaskRepository,
                                   LcEntryValidation lcEntryValidation) {
        this.lcEntryValueRepository = lcEntryValueRepository;
        this.labelIterationRepository = labelIterationRepository;
        this.lcEntryRepository = lcEntryRepository;
        this.mediaRepository = mediaRepository;
        this.pointPojoRepository = pointPojoRepository;
        this.labelConfigurationRepository = labelConfigurationRepository;
        this.labelTaskRepository = labelTaskRepository;
        this.lcEntryValidation = lcEntryValidation;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE})
    @Override
    public LabelIterationViewModel getLabelIterationValues(String iterationId, String mediaId, String taskId) {
        //Permissions check
        DataGymSecurity.isAuthenticated();

        LabelIteration labelIterationById = getLabelIterationById(iterationId);

        //Permissions check
        String projectOrganisation = labelIterationById.getProject().getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, true);

        getMediaById(mediaId);

        List<LcEntryValueViewModel> allRootLcEntryValues = getAllRootLcEntryValues(iterationId, mediaId, taskId);

        LabelIterationViewModel labelIterationViewModel = new LabelIterationViewModel();
        labelIterationViewModel.setId(iterationId);
        labelIterationViewModel.setProjectId(labelIterationById.getProject().getId());
        labelIterationViewModel.setRun(labelIterationById.getRun());
        labelIterationViewModel.setEntryValues(allRootLcEntryValues);

        return labelIterationViewModel;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public LcEntryValueViewModel createLcEntryValue(LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel) {
        //Permissions check
        DataGymSecurity.isAuthenticated();

        String iterationId = lcEntryValueCreateBindingModel.getIterationId();
        LabelIteration labelIterationById = getLabelIterationById(iterationId);

        //Permissions check
        String projectOrganisation = labelIterationById.getProject().getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, false);

        LcEntryValue createdLcEntryValue = createLcSingleEntryValue(lcEntryValueCreateBindingModel);

        return LcEntryValueViewModelsFactory.createLcEntryValueViewModel(createdLcEntryValue);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE})
    @Override
    public List<LcEntryValueViewModel> createGlobalClassificationsValuesGetRoots(String labelConfigurationId,
                                                                                 LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel) {
        List<LcEntryValue> globalClassificationValuesInternal =
                createGlobalClassificationValuesGetRootsInternal(labelConfigurationId, lcEntryValueCreateBindingModel);

        List<LcEntryValueViewModel> lcEntryValueViewModels = new ArrayList<>();

        for (LcEntryValue lcEntryValue : globalClassificationValuesInternal) {
            List<LcEntryValueViewModel> mapToLcEntryValueViewModelList = mapToLcEntryValueViewModel(lcEntryValue,
                                                                                                    new ArrayList<>(),
                                                                                                    null);

            LcEntryValueViewModel currentLcEntryValueViewModel = mapToLcEntryValueViewModelList.stream()
                    .filter(lcEntryValueViewModel -> lcEntryValueViewModel.getLcEntryValueParentId() == null)
                    .findFirst()
                    .orElseThrow(() -> new GenericException(ENTRY_TYPE_NOT_FOUND, null, null, ENTRY_TYPE));

            lcEntryValueViewModels.add(currentLcEntryValueViewModel);
        }

        return lcEntryValueViewModels;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE})
    @Override
    public List<LcEntryValue> createGlobalClassificationValuesGetRootsInternal(String labelConfigurationId,
                                                                               LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel) {
        //Permissions check
        DataGymSecurity.isAuthenticated();

        String iterationId = lcEntryValueCreateBindingModel.getIterationId();
        String taskId = lcEntryValueCreateBindingModel.getLabelTaskId();
        LabelIteration labelIterationById = getLabelIterationById(iterationId);

        //Permissions check
        String projectOrganisation = labelIterationById.getProject().getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, true);

        // Check if configId, taskId and mediaId are valid
        findLabelConfigurationById(labelConfigurationId);
        getLabelTaskById(taskId);
        String mediaId = lcEntryValueCreateBindingModel.getMediaId();
        getMediaById(mediaId);

        // Types of global classification
        List<LcEntryType> lcEntryTypeList = new ArrayList<>();
        lcEntryTypeList.add(LcEntryType.SELECT);
        lcEntryTypeList.add(LcEntryType.FREETEXT);
        lcEntryTypeList.add(LcEntryType.CHECKLIST);

        // Get all root global classifications (they dont have a parent)
        List<LcEntry> rootGlobalClassificationEntries = lcEntryRepository
                .findAllClassificationLcEntriesWithLcEntryTypes(labelConfigurationId, lcEntryTypeList);

        List<LcEntryValue> lcEntryValueViewModels = new ArrayList<>();

        // Collect root global classification entries by creating or fetching them
        for (LcEntry rootGlobalClassificationEntry : rootGlobalClassificationEntries) {
            String lcEntryId = rootGlobalClassificationEntry.getId();

            List<LcEntryValue> rootGlobalClassificationEntryValue =
                    lcEntryValueRepository.findByLcEntryIdAndMediaIdAndLabelTaskId(lcEntryId, mediaId, taskId);

            if (rootGlobalClassificationEntryValue.isEmpty()) {
                lcEntryValueCreateBindingModel.setLcEntryId(lcEntryId);
                lcEntryValueCreateBindingModel.setLcEntryValueParentId(null);
                lcEntryValueCreateBindingModel.setLcEntryValueParent(null);
                LcEntryValue lcEntryValueTree = createLcEntryValueTreeGetRootInternal(lcEntryId,
                                                                                      lcEntryValueCreateBindingModel);
                if (lcEntryValueTree != null) {
                    lcEntryValueViewModels.add(lcEntryValueTree);
                }
            } else {
                LcEntryValue lcEntryValue = rootGlobalClassificationEntryValue.stream()
                        .filter(lcEntryValue1 -> lcEntryValue1.getLcEntryValueParent() == null)
                        .findFirst()
                        .orElseThrow(() -> new GenericException(ENTRY_TYPE_NOT_FOUND, null, null, ENTRY_TYPE));

                lcEntryValueViewModels.add(lcEntryValue);
            }
        }
        return lcEntryValueViewModels;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public LcEntryValueViewModel createLcEntryValueTree(String lcEntryId,
                                                        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE);

        String lcEntryIdBindingModelId = lcEntryValueCreateBindingModel.getLcEntryId();

        if (!lcEntryId.equals(lcEntryIdBindingModelId)) {
            throw new GenericException(ENTRY_DEFINITION_INCORRECT, null, null, lcEntryIdBindingModelId);
        }

        LcEntryValue rootNode = createLcEntryValueTreeGetRootInternal(lcEntryId, lcEntryValueCreateBindingModel);

        List<LcEntryValue> lcEntryValues = new ArrayList<>();
        if (rootNode != null) {
            lcEntryValues.add(rootNode);
        }

        List<LcEntryValueViewModel> lcEntryValueViewModels = mapLcEntryValueToLcEntryValueViewModel(lcEntryValues,
                                                                                                    lcEntryId);

        if (!lcEntryValueViewModels.isEmpty()) {
            return lcEntryValueViewModels.get(0);
        }

        throw new GenericException(ENTRY_DEFINITION_INCORRECT, null, null, lcEntryIdBindingModelId);
    }

    public LcEntryValue createLcEntryValueTreeGetRootInternal(String lcEntryId,
                                                              LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel) {
        LcEntry labelConfigurationEntry = getLcEntryById(lcEntryId);

        List<LcEntryValue> lcEntryValueTreeFromRoot =
                createLcEntryValueTreeFromRoot(labelConfigurationEntry,
                                               lcEntryValueCreateBindingModel,
                                               new ArrayList<>());

        // Return root element
        return lcEntryValueTreeFromRoot.stream()
                .filter(lcEntryValue -> lcEntryValue.getLcEntry().getType().isGeometryType() ||
                        // For global classification
                        lcEntryValue.getLcEntryValueParent() == null)
                .findFirst()
                .orElse(null);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public LabelIterationViewModel extendAllConfigEntryValues(String configId,
                                                              LcEntryValueExtendAllBindingModel lcEntryValueExtendAllBindingModel) {
        //Permissions check
        DataGymSecurity.isAuthenticated();

        String mediaId = lcEntryValueExtendAllBindingModel.getMediaId();
        String iterationId = lcEntryValueExtendAllBindingModel.getIterationId();
        String taskId = lcEntryValueExtendAllBindingModel.getLabelTaskId();

        LabelIteration labelIterationById = getLabelIterationById(iterationId);

        //Permissions check
        String projectOrganisation = labelIterationById.getProject().getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, true);

        List<LcEntry> allByParentEntryIsNullAndConfigurationId = lcEntryRepository
                .findAllByParentEntryIsNullAndConfigurationId(configId);

        LcEntryValueExtendBindingModel lcEntryValueExtendBindingModel =
                LcEntryValueMapper.mapToLcEntryValueExtendBindingModel(lcEntryValueExtendAllBindingModel);

        for (LcEntry lcEntry : allByParentEntryIsNullAndConfigurationId) {
            String lcEntryId = lcEntry.getId();
            lcEntryValueExtendBindingModel.setLcEntryId(lcEntryId);
            extendValueTree(lcEntryId, lcEntryValueExtendBindingModel);
        }

        return getLabelIterationValues(iterationId, mediaId, taskId);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public void extendValueTree(String lcEntryId, LcEntryValueExtendBindingModel lcEntryValueExtendBindingModel) {
        //Permissions check
        DataGymSecurity.isAuthenticated();

        String lcEntryIdBindingModelId = lcEntryValueExtendBindingModel.getLcEntryId();

        if (!lcEntryId.equals(lcEntryIdBindingModelId)) {
            throw new GenericException(ENTRY_DEFINITION_INCORRECT, null, null, lcEntryIdBindingModelId);
        }

        LcEntry parentLcEntry = null;
        String lcEntryParentId = lcEntryValueExtendBindingModel.getLcEntryParentId();
        if (lcEntryParentId != null) {
            parentLcEntry = getLcEntryById(lcEntryParentId);
        }

        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel =
                LcEntryValueMapper.mapToLcEntryValueCreateBindingModel(lcEntryValueExtendBindingModel);

        LcEntry lcEntryById = getLcEntryById(lcEntryId);

        updateLcEntryValueTreeFromRoot(lcEntryById, parentLcEntry, lcEntryValueCreateBindingModel, new ArrayList<>());
    }

    private List<LcEntryValue> updateLcEntryValueTreeFromRoot(LcEntry node, LcEntry parentLcEntry,
                                                              LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel,
                                                              List<LcEntryValue> flatList) {
        if (node != null) {
            String lcEntryId = node.getId();
            getLcEntryById(lcEntryId);

            String mediaId = lcEntryValueCreateBindingModel.getMediaId();
            String labelTaskId = lcEntryValueCreateBindingModel.getLabelTaskId();

            List<LcEntryValue> findValueByLcEntryIdIdAndMediaId = lcEntryValueRepository.findByLcEntryIdAndMediaIdAndLabelTaskId(
                    lcEntryId,
                    mediaId,
                    labelTaskId);

            if (findValueByLcEntryIdIdAndMediaId.isEmpty()) {
                if (parentLcEntry != null) {
                    String parentLcEntryId = parentLcEntry.getId();

                    List<LcEntryValue> findParentValueByLcEntryId = lcEntryValueRepository.findByLcEntryIdAndMediaIdAndLabelTaskId(
                            parentLcEntryId,
                            mediaId,
                            labelTaskId);

                    for (LcEntryValue parentValue : findParentValueByLcEntryId) {
                        lcEntryValueCreateBindingModel.setLcEntryId(lcEntryId);
                        lcEntryValueCreateBindingModel.setLcEntryValueParentId(parentValue.getId());
                        lcEntryValueCreateBindingModel.setLcEntryValueParent(parentValue);

                        createLcEntryValueTreeFromRoot(node, lcEntryValueCreateBindingModel, new ArrayList<>());
                    }
                }
            }
        }

        List<LcEntry> children = Objects.requireNonNull(node).getChildren();

        for (LcEntry child : children) {
            // Do not create the value tree for nested geometry types because they can created multiple times
            if (child.getType().isGeometryType()) {
                continue;
            }
            updateLcEntryValueTreeFromRoot(child, node, lcEntryValueCreateBindingModel, flatList);
        }

        return flatList;
    }

    private List<LcEntryValue> createLcEntryValueTreeFromRoot(LcEntry node,
                                                              LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel,
                                                              List<LcEntryValue> flatList) {
        LcEntryValue currentEntryValue = null;

        if (node != null) {
            currentEntryValue = createLcSingleEntryValue(lcEntryValueCreateBindingModel);

            if (lcEntryValueCreateBindingModel.getLcEntryValueParent() != null) {
                currentEntryValue.setLcEntryValueParent(lcEntryValueCreateBindingModel.getLcEntryValueParent());
                lcEntryValueCreateBindingModel.getLcEntryValueParent().getChildren().add(currentEntryValue);
            }
        }

        List<LcEntry> children = Objects.requireNonNull(node).getChildren();

        for (LcEntry child : children) {
            // Do not create the value tree for nested geometry types because they can created multiple times
            if (child.getType().isGeometryType()) {
                continue;
            }

            String childId = child.getId();
            lcEntryValueCreateBindingModel.setLcEntryId(childId);
            lcEntryValueCreateBindingModel.setLcEntryValueParentId(currentEntryValue.getId());
            lcEntryValueCreateBindingModel.setLcEntryValueParent(currentEntryValue);

            createLcEntryValueTreeFromRoot(child, lcEntryValueCreateBindingModel, flatList);
        }

        // stop or exit condition
        flatList.add(currentEntryValue);

        return flatList;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void deleteLcValue(String lcValueId) {
        //Permissions check
        DataGymSecurity.isAuthenticated();

        LcEntryValue lcEntryValue = getLcEntryValue(lcValueId);

        //Permissions check
        String projectOrganisation = lcEntryValue.getLabelIteration().getProject().getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, false);

        String type = lcEntryValue.getLcEntry().getType().name();

        // Classifications are not supported here.
        if (isClassification(type)) {
            throw new GenericException("entry_value_delete", null, null, lcValueId);
        }

        // A geometry type must be the root element or a direct child of an geometry type.
        // Only one nested level is supported.
        LcEntryValue parentValue = lcEntryValue.getLcEntryValueParent();
        if (parentValue != null && !isGeometry(parentValue.getLcEntry().getType().name())) {
            throw new GenericException("entry_value_delete", null, null, lcValueId);
        }

        // Metrics update - increaseCountLcEntryValue
        Project project = lcEntryValue.getLabelIteration().getProject();
        String projectName = project.getName();
        String projectId = project.getId();

        increaseDeletedCount(type, projectName, projectId);

        // Delete lcEntryValue
        lcEntryValueRepository.delete(lcEntryValue);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public LcEntryValueViewModel updateSingleLcEntryValue(String lcValueId,
                                                          LcEntryValueUpdateBindingModel lcEntryUpdateBindingModel) {
        //Permissions check
        DataGymSecurity.isAuthenticated();

        String lcEntryUpdateBindingModelId = lcEntryUpdateBindingModel.getId();

        if (!lcValueId.equals(lcEntryUpdateBindingModelId)) {
            throw new GenericException(ENTRY_DEFINITION_INCORRECT, null, null, lcEntryUpdateBindingModelId);
        }

        LcEntryValue lcEntryValueFromDb = getLcEntryValue(lcValueId);

        //Permissions check
        String projectOrganisation = lcEntryValueFromDb.getLabelIteration().getProject().getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, false);

        LcEntryValue lcEntryValue = updateSingleLcEntryValueInternal(lcEntryUpdateBindingModel, lcEntryValueFromDb);

        // Validate Value before saving
//        validateSingleEntryValueBeforeTaskCompletion(mappedLcValue);
//        validateSingleEntryValue(mappedLcValue);


        return LcEntryValueViewModelsFactory.createLcEntryValueViewModel(lcEntryValue);
    }

    @NotNull
    private LcEntryValue updateSingleLcEntryValueInternal(LcEntryValueUpdateBindingModel lcEntryUpdateBindingModel,
                                                          LcEntryValue lcEntryValueFromDb) {
        String lcEntryId = lcEntryUpdateBindingModel.getLcEntryId();
        LcEntry lcEntryById = getLcEntryById(lcEntryId);

        String lcEntryType = lcEntryById.getType().name().toUpperCase();

        LcEntryValue mappedLcValue = mapToLcEntryValueObject(lcEntryUpdateBindingModel, lcEntryType);
        mappedLcValue.setMedia(lcEntryValueFromDb.getMedia());
        mappedLcValue.setLcEntry(lcEntryValueFromDb.getLcEntry());
        mappedLcValue.setLabelIteration(lcEntryValueFromDb.getLabelIteration());
        mappedLcValue.setChildren(lcEntryValueFromDb.getChildren());

        // Set LabelTask to the current LcEntryValue
        LabelTask labelTaskById = getLabelTaskById(lcEntryUpdateBindingModel.getLabelTaskId());
        mappedLcValue.setLabelTask(labelTaskById);

        String parentId = lcEntryUpdateBindingModel.getLcEntryValueParentId();
        if (parentId != null) {
            LcEntryValue lcEntryParentValue = getLcEntryValue(parentId);
            mappedLcValue.setLcEntryValueParent(lcEntryParentValue);
        }
        return lcEntryValueRepository.save(mappedLcValue);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE})
    @Override
    public LabelIterationViewModel updateLcEntryValues(String iterationId, String mediaId, String taskId,
                                                       List<LcEntryValueUpdateBindingModel> lcEntryUpdateBindingModelList,
                                                       boolean isCurrentProjectDummy) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE);

        this.isDummyProject = isCurrentProjectDummy;

        LabelIteration labelIterationById = getLabelIterationById(iterationId);

        //Permissions check
        String projectOrganisation = labelIterationById.getProject().getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, false);

        Media mediaById = getMediaById(mediaId);

        Set<String> entryValueIdsFromNewConfig = validateSingleEntryValueBeforeTaskCompletion(
                lcEntryUpdateBindingModelList);

        int treeHeight = 1;

        lcEntryUpdateBindingModelList.forEach(currentLcEntryValueUpdateBindingModel -> {
            String lcEntryId = currentLcEntryValueUpdateBindingModel.getLcEntryId();
            getLcEntryById(lcEntryId);

            convertAndUpdateEntries(currentLcEntryValueUpdateBindingModel,
                    new ArrayList<>(),
                    null,
                    labelIterationById,
                    mediaById,
                    treeHeight,
                    entryValueIdsFromNewConfig);
        });

        return getLabelIterationValues(iterationId, mediaId, taskId);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void validateEntryValuesBeforeTaskCompletion(String iterationId, String mediaId) {
        //Permissions check
        DataGymSecurity.isAuthenticated();

        LabelIteration labelIterationById = getLabelIterationById(iterationId);
        MediaType projectMediaType = labelIterationById.getProject().getMediaType();

        //Permissions check
        String projectOrganisation = labelIterationById.getProject().getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, false);


        List<LcEntryValue> allByLabelIterationIdAndMediaId = lcEntryValueRepository
                .findAllByLabelIterationIdAndMediaId(iterationId, mediaId);

        if (projectMediaType == MediaType.IMAGE) {
            allByLabelIterationIdAndMediaId.forEach(x -> lcEntryValidation.validateSingleEntryValueBeforeTaskCompletion(
                    x,
                    true));
        } else if (projectMediaType == MediaType.VIDEO) {
            allByLabelIterationIdAndMediaId.forEach(e -> {
                lcEntryValidation.validateFrameTypeCountPlausibility(e);
                for (LcEntryValueChange lcEntryValueChange : e.getLcEntryValueChanges()) {
                    lcEntryValidation.validateSingleVideoEntryValueBeforeTaskCompletion(lcEntryValueChange);
                }
            });
        }
    }

    /**
     * Creates new LcEntryValue-Tree from a LcEntryValue and a LcEntry.
     * The old LcEntryValue will be deleted.
     * The new LcEntryValue keeps only the Geometry Coordinates from the old one.
     * The old and new LcEntryValues must be a Geometry and have the same LcEntryType
     */
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public LcEntryValueViewModel changeTypeOfSingleLabelValue(String lcValueId,
                                                              LcEntryValueChangeValueClassBindingModel lcEntryValueChangeValueClassBindingModel) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE);

        // Get the Information about the current LcEntryValue and his LcEntry
        LcEntryValue oldLcEntryValue = getLcEntryValue(lcValueId);

        //Permissions check
        String projectOrganisation = oldLcEntryValue.getLabelIteration().getProject().getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, false);

        // Get the Information about the current LcEntryValue and his LcEntry
        String oldLcEntryId = oldLcEntryValue.getLcEntry().getId();
        String oldLcEntryType = oldLcEntryValue.getLcEntry().getType().name();

        String labelIterationId = oldLcEntryValue.getLabelIteration().getId();
        String mediaId = oldLcEntryValue.getMedia().getId();
        LabelTask labelTask = oldLcEntryValue.getLabelTask();
        LcEntryValue parentLcEntryValue = oldLcEntryValue.getLcEntryValueParent();

        // Check if the oldLcEntryType is a Geometry
        checkIfLcEntryTypeIsGeometry(oldLcEntryType, oldLcEntryId);

        // Get the Information about the new LcEntry
        String newLcEntryId = lcEntryValueChangeValueClassBindingModel.getNewLcEntryId();
        LcEntry newLcEntry = getLcEntryById(newLcEntryId);
        String newLcEntryType = newLcEntry.getType().name();

        // Check if the newLcEntryType is a Geometry
        checkIfLcEntryTypeIsGeometry(newLcEntryType, newLcEntryId);


        // Check if the new LcEntryType equals the current LcEntryType. If so create new LcEntryValue Tree, else throw Exception
        if (!oldLcEntryType.equals(newLcEntryType)) {
            throw new GenericException("entry_types_not_the_same", null, null);
        }

        // Check if the new LcEntry and the old LcEntry are the same.
        if (newLcEntryId.equals(oldLcEntryId)) {
            throw new GenericException("entry_types_are_the_same", null, null);
        }

        // Create LcEntryValueCreateBindingModel for the new LcEntryValue
        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = new LcEntryValueCreateBindingModel();
        lcEntryValueCreateBindingModel.setLcEntryId(newLcEntryId);
        lcEntryValueCreateBindingModel.setIterationId(labelIterationId);
        lcEntryValueCreateBindingModel.setMediaId(mediaId);
        lcEntryValueCreateBindingModel.setLabelTaskId(labelTask.getId());

        if (parentLcEntryValue != null) {
            lcEntryValueCreateBindingModel.setLcEntryValueParent(parentLcEntryValue);
            lcEntryValueCreateBindingModel.setLcEntryValueParentId(parentLcEntryValue.getId());
        }

        // Create LcEntryValueTree for the new LcEntryValue
        LcEntryValue lcEntryValue = createLcEntryValueTreeGetRootInternal(newLcEntryId, lcEntryValueCreateBindingModel);

        // Create LcEntryValueUpdateBindingModel
        LcEntryValueUpdateBindingModel updateBindingModel = createLcEntryValueUpdateBindingModelFromLcEntryValueAndLcEntry(
                oldLcEntryValue,
                newLcEntry,
                lcEntryValue.getId());

        // traverse down the tree and validate the Values
        traverseAndValidateLcEntryValue(lcEntryValue);

        // Update the new created Geometry-LcEntryValue with the coordinates from the old LcEntryValue
        LcEntryValue updatedLcEntryValue = updateSingleLcEntryValueInternal(updateBindingModel, lcEntryValue);

        // Create a LcEntryValueViewModel for the current Geometry and it's children
        LcEntryValueViewModel newGeometryValueViewModel = mapRootLcEntryValueToLcEntryValueViewModel(updatedLcEntryValue);

        // Delete the old LcEntryValue
        deleteLcValue(lcValueId);

        return newGeometryValueViewModel;
    }

    /**
     * Recursive method is used to traverse down the tree and validate the Values
     **/
    @Override
    public void traverseAndValidateLcEntryValue(LcEntryValue node) {
        if (node != null) {
            lcEntryValidation.validateSingleEntryValueBeforeTaskCompletion(node, false);
        }

        List<LcEntryValue> children = Objects.requireNonNull(node).getChildren();
        for (LcEntryValue child : children) {
            if (child.getChildren() != null) {
                traverseAndValidateLcEntryValue(child);
            }
        }
    }

    /**
     * Create a LcEntryValueViewModel for the current Root Node and it's children
     *
     * @param lcEntryValue LcEntryValue to be converted into a LcEntryValueViewModel
     */
    private LcEntryValueViewModel mapRootLcEntryValueToLcEntryValueViewModel(LcEntryValue lcEntryValue) {
        List<LcEntryValueViewModel> lcEntryValueViewModels = mapToLcEntryValueViewModel(lcEntryValue,
                                                                                        new ArrayList<>(),
                                                                                        null);

        return lcEntryValueViewModels.stream()
                .filter(lcEntryValueViewModel -> lcEntryValueViewModel.getLcEntryValueParentId() == null ||
                        (isGeometry(lcEntryValueViewModel.getEntryTypeLcEntry())
                                && lcEntryValueViewModel.getLcEntryId().equals(lcEntryValue.getLcEntry().getId())))
                .findFirst()
                .orElseThrow(() -> new GenericException(ENTRY_TYPE_NOT_FOUND, null, null, ENTRY_TYPE));
    }

    /**
     * Create LcEntryValueUpdateBindingModel from lcEntryValue and lcEntry
     *
     * @param lcEntryValue       the old LcEntryValue that contains the Geometry Coordinates
     * @param lcEntry            the target LcEntry for the conversion of the Geometries
     * @param currentRootValueId the Id of the Root-Element of the new created LcEntryValueTre for the Geometry-conversion
     */
    private LcEntryValueUpdateBindingModel createLcEntryValueUpdateBindingModelFromLcEntryValueAndLcEntry(
            LcEntryValue lcEntryValue, LcEntry lcEntry, String currentRootValueId) {
        String entryType = lcEntry.getType().name();

        LcEntryValueUpdateBindingModel lcEntryValueUpdateBindingModel = new LcEntryValueUpdateBindingModel();
        lcEntryValueUpdateBindingModel.setId(currentRootValueId);
        lcEntryValueUpdateBindingModel.setLcEntryId(lcEntry.getId());
        lcEntryValueUpdateBindingModel.setValid(lcEntryValue.isValid());
        lcEntryValueUpdateBindingModel.setLabelTaskId(lcEntryValue.getLabelTask().getId());
        // For nested geometries.
        if (lcEntryValue.getLcEntryValueParent() != null) {
            lcEntryValueUpdateBindingModel.setLcEntryValueParentId(lcEntryValue.getLcEntryValueParent().getId());
        }

        switch (entryType) {
            case POINT:
                if (lcEntry instanceof LcEntryPoint && lcEntryValue instanceof LcEntryPointValue) {
                    LcEntryPointValue lcEntryPointValue = (LcEntryPointValue) lcEntryValue;

                    // Set the Coordinates of the Rectangle
                    lcEntryValueUpdateBindingModel.setX(lcEntryPointValue.getX());
                    lcEntryValueUpdateBindingModel.setY(lcEntryPointValue.getY());

                    return lcEntryValueUpdateBindingModel;
                }

                throw new GenericException(ENTRY_TYPE_NOT_GEOMETRY, null, null, ENTRY_TYPE);

            case LINE:
                if (lcEntry instanceof LcEntryLine && lcEntryValue instanceof LcEntryLineValue) {
                    LcEntryLineValue lcEntryLineValue = (LcEntryLineValue) lcEntryValue;

                    // Convert all PointPojo to PointPojoBindingModel
                    List<PointPojo> linePoints = lcEntryLineValue.getPoints();
                    List<PointPojoBindingModel> pointPojoBindingModels = mapToPointPojoBindingModel(linePoints);

                    // Set the Coordinates of the point
                    lcEntryValueUpdateBindingModel.setPoints(pointPojoBindingModels);

                    return lcEntryValueUpdateBindingModel;
                }

                throw new GenericException(ENTRY_TYPE_NOT_GEOMETRY, null, null, ENTRY_TYPE);

            case POLYGON:
                if (lcEntry instanceof LcEntryPolygon && lcEntryValue instanceof LcEntryPolygonValue) {
                    LcEntryPolygonValue lcEntryPolygonValue = (LcEntryPolygonValue) lcEntryValue;

                    // Convert all PointPojo to PointPojoBindingModel
                    List<PointPojo> polygonPoints = lcEntryPolygonValue.getPoints();
                    List<PointPojoBindingModel> pointPojoBindingModels = mapToPointPojoBindingModel(polygonPoints);

                    // Set the Coordinates of the Polygon
                    lcEntryValueUpdateBindingModel.setPoints(pointPojoBindingModels);

                    return lcEntryValueUpdateBindingModel;
                }

                throw new GenericException(ENTRY_TYPE_NOT_GEOMETRY, null, null, ENTRY_TYPE);

            case IMAGE_SEGMENTATION:
                if (lcEntry instanceof LcEntryImageSegmentation && lcEntryValue instanceof LcEntryImageSegmentationValue) {
                    LcEntryImageSegmentationValue lcEntrySegmentationValue = (LcEntryImageSegmentationValue) lcEntryValue;

                    // Convert all PointPojo to PointPojoBindingModel
                    List<PointCollection> segmentationPointsCollection = lcEntrySegmentationValue.getPointsCollection();
                    List<PointCollectionBindingModel> segmentationPointsModel = mapToPointCollectionBindingModel(
                            segmentationPointsCollection);

                    // Set the Coordinates of the Polygon
                    lcEntryValueUpdateBindingModel.setPointsCollection(segmentationPointsModel);

                    return lcEntryValueUpdateBindingModel;
                }

                throw new GenericException(ENTRY_TYPE_NOT_GEOMETRY, null, null, ENTRY_TYPE);

            case RECTANGLE:
                if (lcEntry instanceof LcEntryRectangle && lcEntryValue instanceof LcEntryRectangleValue) {
                    LcEntryRectangleValue lcEntryRectangleValue = (LcEntryRectangleValue) lcEntryValue;

                    // Set the Coordinates of the Rectangle
                    lcEntryValueUpdateBindingModel.setX(lcEntryRectangleValue.getX());
                    lcEntryValueUpdateBindingModel.setY(lcEntryRectangleValue.getY());
                    lcEntryValueUpdateBindingModel.setWidth(lcEntryRectangleValue.getWidth());
                    lcEntryValueUpdateBindingModel.setHeight(lcEntryRectangleValue.getHeight());

                    return lcEntryValueUpdateBindingModel;
                }

                throw new GenericException(ENTRY_TYPE_NOT_GEOMETRY, null, null, ENTRY_TYPE);
            default:
                throw new GenericException(ENTRY_TYPE_NOT_GEOMETRY, null, null, ENTRY_TYPE);
        }
    }

    /**
     * Convert PointPojo to PointPojoBindingModel
     */
    private List<PointPojoBindingModel> mapToPointPojoBindingModel(List<PointPojo> polygonPoints) {
        List<PointPojoBindingModel> pointPojos = new ArrayList<>();

        polygonPoints.forEach(pointPojo -> {
            PointPojoBindingModel pointPojoBindingModel = new PointPojoBindingModel();
            pointPojoBindingModel.setX(pointPojo.getX());
            pointPojoBindingModel.setY(pointPojo.getY());
            pointPojoBindingModel.setId(pointPojo.getId());

            pointPojos.add(pointPojoBindingModel);
        });

        return pointPojos;
    }

    /**
     * Convert PointCollection to PointCollectionBindingModel
     */
    private List<PointCollectionBindingModel> mapToPointCollectionBindingModel(List<PointCollection> polygonPoints) {
        List<PointCollectionBindingModel> pointPojos = new ArrayList<>();

        polygonPoints.forEach(pointPojo -> {
            PointCollectionBindingModel pointPojoBindingModel = new PointCollectionBindingModel();
            pointPojoBindingModel.setId(pointPojo.getId());
            pointPojoBindingModel.setPoints(mapToPointPojoBindingModel(pointPojo.getPoints()));

            pointPojos.add(pointPojoBindingModel);
        });

        return pointPojos;
    }

    /**
     * Check if LcEntryType is a Geometry
     */
    private void checkIfLcEntryTypeIsGeometry(String entryType, String lcEntryId) {
        if (!entryType.equals(POINT) && !entryType.equals(LINE) && !entryType.equals(POLYGON) && !entryType.equals(
                RECTANGLE)) {
            throw new GenericException(ENTRY_TYPE_NOT_GEOMETRY, null, null, lcEntryId);
        }
    }

    private List<LcEntryValueViewModel> getAllRootLcEntryValues(String iterationId, String mediaId, String taskId) {
        List<LcEntryValue> allByLabelIterationIdAndMediaId = lcEntryValueRepository
                .findAllByLabelIterationIdAndMediaIdAndLabelTaskIdAndLcEntryValueParentIsNull(iterationId,
                        mediaId,
                        taskId);

        return mapLcEntryRootValueToLcEntryValueViewModel(allByLabelIterationIdAndMediaId);
    }

    private List<LcEntryValueViewModel> mapLcEntryValueToLcEntryValueViewModel(List<LcEntryValue> lcEntryValueList,
                                                                               String lcEntryId) {
        return lcEntryValueList
                .stream()
                .map(lcEntryValue -> {

                         List<LcEntryValueViewModel> lcEntryValueViewModels = mapToLcEntryValueViewModel(lcEntryValue,
                                                                                                         new ArrayList<>(),
                                                                                                         null);

                         return lcEntryValueViewModels.stream()
                                 .filter(lcEntryValueViewModel -> lcEntryValueViewModel.getLcEntryId().equals(lcEntryId))
                                 .findFirst()
                                 .orElseThrow(() -> new GenericException(ENTRY_TYPE_NOT_FOUND, null, null, ENTRY_TYPE));


                     }

                )
                .collect(Collectors.toList());
    }

    private List<LcEntryValueViewModel> mapLcEntryRootValueToLcEntryValueViewModel(
            List<LcEntryValue> lcEntryValueList) {
        return lcEntryValueList
                .stream()
                .map(lcEntryValue -> {

                         List<LcEntryValueViewModel> lcEntryValueViewModels = mapToLcEntryValueViewModel(lcEntryValue,
                                                                                                         new ArrayList<>(),
                                                                                                         null);

                         return lcEntryValueViewModels.stream()
                                 .filter(lcEntryValueViewModel -> lcEntryValueViewModel.getLcEntryValueParentId() == null)
                                 .findFirst()
                                 .orElseThrow(() -> new GenericException(ENTRY_TYPE_NOT_FOUND, null, null, ENTRY_TYPE));


                     }

                )
                .collect(Collectors.toList());
    }

    private List<LcEntryValueViewModel> mapToLcEntryValueViewModel(LcEntryValue node,
                                                                   List<LcEntryValueViewModel> flatList,
                                                                   LcEntryValueViewModel parentEntryValue) {
        LcEntryValueViewModel entryValueObject = null;

        if (node != null) {
            entryValueObject = LcEntryValueViewModelsFactory.createLcEntryValueViewModel(node);
            if (parentEntryValue != null) {
                entryValueObject.setLcEntryValueParentId(parentEntryValue.getId());
                parentEntryValue.getChildren().add(entryValueObject);
            }
        }

        List<LcEntryValue> children = Objects.requireNonNull(node).getChildren();
        for (LcEntryValue child : children) {

            if (child.getChildren() != null) {
                // Recursive call - Keep converting until no more children
                mapToLcEntryValueViewModel(child, flatList, entryValueObject);
            }
        }

        flatList.add(entryValueObject);

        // stop or exit condition
        return flatList;
    }

    private LcEntryValue createLcSingleEntryValue(LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel) {
        LabelIteration labelIterationById = getLabelIterationById(lcEntryValueCreateBindingModel.getIterationId());
        String iterationProjectId = labelIterationById.getProject().getId();

        LabelTask labelTaskById = getLabelTaskById(lcEntryValueCreateBindingModel.getLabelTaskId());

        String mediaId = lcEntryValueCreateBindingModel.getMediaId();
        Media mediaById = getMediaById(mediaId);

        LcEntry lcEntryById = getLcEntryById(lcEntryValueCreateBindingModel.getLcEntryId());
        String lcEntryType = lcEntryById.getType().name().toUpperCase();
        String lcEntryProjectId = lcEntryById.getConfiguration().getProject().getId();

        if (!iterationProjectId.equals(lcEntryProjectId)) {
            throw new GenericException("entry_value_create", null, null, lcEntryType);
        }

        String parentEntryValueId = lcEntryValueCreateBindingModel.getLcEntryValueParentId();
        LcEntry parentEntry = lcEntryById.getParentEntry();
        LcEntryValue parentLcEntryValue = lcEntryValueCreateBindingModel.getLcEntryValueParent();

        // LcEntryValue
        if (parentLcEntryValue == null && parentEntryValueId != null) {
            parentLcEntryValue = getLcEntryValue(parentEntryValueId);
        }

        if (parentLcEntryValue == null && parentEntry != null) {
            throw new GenericException("entry_value_create", null, null, lcEntryType);
        }

        LcEntryValue lcEntryValueObject = createLcEntryValueObject(lcEntryType);
        long time = System.currentTimeMillis();
        String loggedInUserId = DataGymSecurity.getLoggedInUserId();

        lcEntryValueObject.setLabelIteration(labelIterationById);
        lcEntryValueObject.setMedia(mediaById);
        lcEntryValueObject.setLcEntry(lcEntryById);
        lcEntryValueObject.setLabeler(loggedInUserId);
        lcEntryValueObject.setTimestamp(time);
        lcEntryValueObject.setChildren(new ArrayList<>());
        lcEntryValueObject.setLabelTask(labelTaskById);

        if (parentEntryValueId != null) {
            // LcEntry
            String parentEntryId = lcEntryById.getParentEntry().getId();
            LcEntry parentEntryFromDb = getLcEntryById(parentEntryId);
            LcEntryType parentLcEntryType = parentEntryFromDb.getType();

            LcEntryType parentLcEntryValueType = parentLcEntryValue.getLcEntry().getType();
            String parentLcEntryFromLcEntryValueId = parentLcEntryValue.getLcEntry().getId();

            if (!parentLcEntryType.equals(parentLcEntryValueType) || !parentEntryId.equals(
                    parentLcEntryFromLcEntryValueId)) {
                throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, parentLcEntryValueType);
            }

            compareConfigAndValues(parentEntryFromDb, parentLcEntryValue, lcEntryType);

            lcEntryValueObject.setLcEntryValueParent(parentLcEntryValue);
        }

        Project project = lcEntryValueObject.getLabelIteration().getProject();

        // Metrics update - increaseCountLcEntryValue
        String projectName = project.getName();
        String projectId = project.getId();
        increaseCreatedCount(lcEntryType, projectName, projectId);

        lcEntryValueRepository.save(lcEntryValueObject);

        return lcEntryValueObject;
    }

    private void compareConfigAndValues(LcEntry parentEntry, LcEntryValue parentLcEntryValue,
                                        String typeToAddToValues) {
        // Ignore nested geometries because the instances should not match the label configuration
        if (LcEntryType.valueOf(typeToAddToValues).isGeometryType()) {
            return;
        }
        Map<String, Integer> lcEntryTypesMap = new HashMap<>();

        List<LcEntry> parentEntryChildren = parentEntry.getChildren();
        List<LcEntryValue> parentLcEntryValueChildren = parentLcEntryValue.getChildren();

        for (LcEntry parentEntryChild : parentEntryChildren) {
            String type = parentEntryChild.getType().name();

            if (!lcEntryTypesMap.containsKey(type)) {
                lcEntryTypesMap.put(type, 0);
            }

            lcEntryTypesMap.put(type, lcEntryTypesMap.get(type) + 1);
        }

        if (!lcEntryTypesMap.containsKey(typeToAddToValues)) {
            throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, typeToAddToValues);
        }

        for (LcEntryValue parentEntryValueChild : parentLcEntryValueChildren) {
            String type = parentEntryValueChild.getLcEntry().getType().name();

            if (!lcEntryTypesMap.containsKey(type)) {
                lcEntryTypesMap.put(type, 0);
            }

            lcEntryTypesMap.put(type, lcEntryTypesMap.get(type) - 1);
            Integer valuesCount = lcEntryTypesMap.get(type);

            if (typeToAddToValues.equals(type) && valuesCount - 1 < 0) {
                throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, type);
            }
        }
    }

    private List<LcEntryValue> convertAndUpdateEntries(LcEntryValueUpdateBindingModel node,
                                                       List<LcEntryValue> flatList,
                                                       LcEntryValue parentEntryValue,
                                                       LabelIteration labelIteration,
                                                       Media media,
                                                       int treeHeight,
                                                       Set<String> entryIdsFromNewConfig) {
        LcEntryValue entryValueObject = null;

        if (node != null) {
            LcEntry lcEntryById = getLcEntryById(node.getLcEntryId());
            String entryType = lcEntryById.getType().name().toUpperCase();

            String lcEntryProjectId = lcEntryById.getConfiguration().getProject().getId();
            String iterationProjectId = labelIteration.getProject().getId();

            if (!iterationProjectId.equals(lcEntryProjectId)) {
                throw new GenericException(ENTRY_DEFINITION_INCORRECT, null, null, lcEntryById.getId());
            }

            entryValueObject = mapToLcEntryValueObject(node, entryType);

            entryValueObject.setLabelIteration(labelIteration);
            entryValueObject.setMedia(media);
            entryValueObject.setLcEntry(lcEntryById);

            LabelTask labelTaskById = getLabelTaskById(node.getLabelTaskId());
            entryValueObject.setLabelTask(labelTaskById);

            if (parentEntryValue != null) {
                if (entryValueObject.getId() != null && parentEntryValue.getId() == null) {
                    throw new GenericException(ENTRY_DEFINITION_INCORRECT, null, null, entryValueObject.getId());
                }

                if (entryValueObject.getId() == null && parentEntryValue.getId() != null) {
                    LcEntryValue lcParentEntryByValueId = getLcEntryValue(parentEntryValue.getId());
                    compareOldAndUpdatedParent(lcParentEntryByValueId, parentEntryValue);

                    List<LcEntryValue> children = lcParentEntryByValueId.getChildren();
                    Iterator<LcEntryValue> iterator = children.iterator();

                    while (iterator.hasNext()) {
                        LcEntryValue nextChild = iterator.next();

                        if (!entryIdsFromNewConfig.contains(nextChild.getId())) {
                            nextChild.setLcEntryValueParent(null);
                            iterator.remove();
                            lcEntryValueRepository.delete(nextChild);
                            flatList.add(nextChild);
                        }
                    }
                }

                String parentEntryId = lcEntryById.getParentEntry().getId();
                LcEntry parentEntry = getLcEntryById(parentEntryId);
                String lcEntryType = lcEntryById.getType().name().toUpperCase();

                compareConfigAndValues(parentEntry, parentEntryValue, lcEntryType);

                entryValueObject.setLcEntryValueParent(parentEntryValue);
                parentEntryValue.getChildren().add(entryValueObject);
                parentEntryValue.setLabelIteration(labelIteration);
            }
        }

        List<LcEntryValueUpdateBindingModel> children = Objects.requireNonNull(node).getChildren();
        for (LcEntryValueUpdateBindingModel child : children) {

            int newTreeHeight = treeHeight + 1;
            if (newTreeHeight > 4) {
                throw new GenericException("config_depth", null, null);
            }

            if (child.getChildren() != null) {
                // Recursive call - Keep converting until no more children
                convertAndUpdateEntries(child,
                        flatList,
                        entryValueObject,
                        labelIteration,
                        media,
                        newTreeHeight,
                        entryIdsFromNewConfig);
            }
        }

        flatList.add(entryValueObject);

        lcEntryValueRepository.save(entryValueObject);

        // stop or exit condition
        return flatList;
    }

    private void compareOldAndUpdatedParent(LcEntryValue parentEntryValueFromDb,
                                            LcEntryValue parentEntryValueFromUser) {
        if (!parentEntryValueFromDb.getId().equals(parentEntryValueFromUser.getId()) ||
                !parentEntryValueFromDb.getLcEntry().getId().equals(parentEntryValueFromUser.getLcEntry().getId())) {
            throw new GenericException(ENTRY_DEFINITION_INCORRECT, null, null, parentEntryValueFromUser.getId());
        }
    }

    private Set<String> validateSingleEntryValueBeforeTaskCompletion(
            List<LcEntryValueUpdateBindingModel> lcEntryUpdateBindingModelList) {
        Set<String> entryValueIdsSet = new HashSet<>();
        for (int i = 0; i < lcEntryUpdateBindingModelList.size(); i++) {
            LcEntryValueUpdateBindingModel currentRootNode = lcEntryUpdateBindingModelList.get(i);
            flatten(currentRootNode, entryValueIdsSet);
        }

        return entryValueIdsSet;
    }

    /**
     * Recursive method is used to traverse down the tree
     **/
    private Set<String> flatten(LcEntryValueUpdateBindingModel node, Set<String> entryValueIdsSet) {
        if (node != null) {
            LcEntry lcEntryById = getLcEntryById(node.getLcEntryId());
            String entryType = lcEntryById.getType().name().toUpperCase();

            if (!this.isDummyProject && (entryType.equals(SELECT) || entryType.equals(
                    CHECKLIST) || entryType.equals(FREETEXT))) {
                lcEntryValidation.validateRequiredEntryValues(lcEntryById, node, entryType);
            }

            String entryValueId = node.getId();
            String lcEntryId = node.getLcEntryId();

            if (entryValueId != null) {
                LcEntryValue lcEntryValueFromDb = getLcEntryValue(entryValueId);

                if (!entryValueIdsSet.add(entryValueId)) {
                    throw new AlreadyExistsException("Label entry value", "id:", entryValueId);
                }

                if (!lcEntryValueFromDb.getId().equals(entryValueId) || !lcEntryValueFromDb.getLcEntry().getId().equals(
                        lcEntryId)) {
                    throw new GenericException(ENTRY_DEFINITION_INCORRECT, null, null, entryValueId);
                }
            }
        }

        List<LcEntryValueUpdateBindingModel> children = Objects.requireNonNull(node).getChildren();
        for (LcEntryValueUpdateBindingModel child : children) {
            if (child.getChildren() != null) {
                flatten(child, entryValueIdsSet);         // Recursive call - Keep flattening until no more children
            }
        }

        // stop or exit condition
        return entryValueIdsSet;
    }


    private LcEntryValue mapToLcEntryValueObject(LcEntryValueUpdateBindingModel
                                                         lcEntryValueUpdateBindingModel, String entryType) {

        switch (entryType) {
            case POINT:
                return LcEntryValueMapper.mapToPointValue(lcEntryValueUpdateBindingModel);
            case LINE:
                return LcEntryValueMapper.mapToLineValue(lcEntryValueUpdateBindingModel);
            case POLYGON:
                return LcEntryValueMapper.mapToPolygonValue(lcEntryValueUpdateBindingModel);
            case IMAGE_SEGMENTATION:
                return LcEntryValueMapper.mapToImageSegmentationValue(lcEntryValueUpdateBindingModel);
            case RECTANGLE:
                return LcEntryValueMapper.mapToRectangleValue(lcEntryValueUpdateBindingModel);
            case SELECT:
                return LcEntryValueMapper.mapToSelectValue(lcEntryValueUpdateBindingModel);
            case CHECKLIST:
                return LcEntryValueMapper.mapToCheckListValue(lcEntryValueUpdateBindingModel);
            case FREETEXT:
                return LcEntryValueMapper.mapToTextValue(lcEntryValueUpdateBindingModel);
            default:
                throw new GenericException(ENTRY_TYPE_NOT_FOUND, null, null, ENTRY_TYPE);
        }
    }

    @AuthUser
    @Override
    public LcEntryValue createLcEntryValueObject(String entryType) {
        switch (entryType) {
            case POINT:
                return new LcEntryPointValue();
            case LINE:
                return new LcEntryLineValue();
            case POLYGON:
                return new LcEntryPolygonValue();
            case RECTANGLE:
                return new LcEntryRectangleValue();
            case IMAGE_SEGMENTATION:
                return new LcEntryImageSegmentationValue();
            case SELECT:
                return new LcEntrySelectValue();
            case CHECKLIST:
                return new LcEntryCheckListValue();
            case FREETEXT:
                return new LcEntryTextValue();
            default:
                throw new GenericException(ENTRY_TYPE_NOT_FOUND, null, null, ENTRY_TYPE);
        }
    }

    private LabelIteration getLabelIterationById(String iterationId) {
        return labelIterationRepository.findById(iterationId)
                .orElseThrow(() -> new NotFoundException("Label Iteration", "id", "" + iterationId));
    }

    private Media getMediaById(String mediaId) {
        return mediaRepository.findById(mediaId)
                .orElseThrow(() -> new NotFoundException("media", "id", "" + mediaId));
    }

    private LcEntryValue getLcEntryValue(String lcEntryValueId) {
        return lcEntryValueRepository.findById(lcEntryValueId)
                .orElseThrow(() -> new NotFoundException("Label Entry Value", "id", "" + lcEntryValueId));
    }

    private LcEntry getLcEntryById(String entryId) {
        return lcEntryRepository.findById(entryId)
                .orElseThrow(() -> new NotFoundException("Entry", "id", "" + entryId));
    }

    private LabelConfiguration findLabelConfigurationById(String configId) {
        return labelConfigurationRepository.findById(configId)
                .orElseThrow(() -> new NotFoundException("Label Configuration", "id", "" + configId));
    }

    private LabelTask getLabelTaskById(String labelTaskId) {
        return labelTaskRepository
                .findById(labelTaskId)
                .orElseThrow(() -> new NotFoundException("Label Task", "id", "" + labelTaskId));
    }

    private boolean isGeometry(final String type) {
        return type.equals(POINT)
                || type.equals(LINE)
                || type.equals(POLYGON)
                || type.equals(RECTANGLE)
                || type.equals(IMAGE_SEGMENTATION);
    }

    private boolean isClassification(final String type) {
        return !isGeometry(type);
    }

    // Metrics update - increaseCountLcEntryValue
    private void increaseCreatedCount(String lcEntryType, String projectName, String projectId) {
        Metrics.summary("datagym.lcEntry.values.create.summary",
                        "lcEntryType", lcEntryType,
                        "projectName", projectName,
                        "projectId", projectId)
                .record(1.0);
    }

    // Metrics update - increaseCountLcEntryValue
    private void increaseDeletedCount(String lcEntryType, String projectName, String projectId) {
        Metrics.summary("datagym.lcEntry.values.delete.summary",
                        "lcEntryType", lcEntryType,
                        "projectName", projectName,
                        "projectId", projectId)
                .record(1.0);
    }
}
