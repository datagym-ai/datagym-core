package ai.datagym.application.labelConfiguration.service;

import ai.datagym.application.export.consumer.DataGymBinFileConsumer;
import ai.datagym.application.export.consumer.DataGymBinFileConsumerImpl;
import ai.datagym.application.labelConfiguration.entity.LabelConfiguration;
import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntryChecklist;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntryFreeText;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntrySelect;
import ai.datagym.application.labelConfiguration.models.bindingModels.LcEntryUpdateBindingModel;
import ai.datagym.application.labelConfiguration.models.export.LcEntryExport;
import ai.datagym.application.labelConfiguration.models.viewModels.LabelConfigurationViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcConfigDeleteViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcConfigHasConfigChangedViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcEntryViewModel;
import ai.datagym.application.labelConfiguration.repo.LabelConfigurationRepository;
import ai.datagym.application.labelConfiguration.repo.LcEntryRepository;
import ai.datagym.application.labelIteration.entity.LabelIteration;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.repo.LabelIterationRepository;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelIteration.service.LcEntryValidation;
import ai.datagym.application.labelTask.service.UserTaskService;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.security.util.DataGymSecurity;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.lib.exception.AlreadyExistsException;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import io.micrometer.core.instrument.Metrics;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static ai.datagym.application.utils.constants.CommonMessages.*;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class LabelConfigurationServiceImpl implements LabelConfigurationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LabelConfigurationServiceImpl.class);
    private static final String ENTRY_TYPE_NOT_FOUND_EXCEPTION_MESSAGE = "entry_type_not_found";
    private static final String ENTRY_TYPE = "entry type";
    private static final String LABEL_ENTRY = "Label entry";

    private final LabelConfigurationRepository labelConfigurationRepository;
    private final LcEntryRepository lcEntryRepository;
    private final UserTaskService userTaskService;
    private final ModelMapper modelMapper;
    private final LcEntryValueRepository lcEntryValueRepository;
    private final LabelIterationRepository labelIterationRepository;
    private final LcEntryValidation lcEntryValidation;

    private boolean wereEntriesDeletedFromConfig = false;
    private boolean wereNewEntriesAddedToConfig = false;

    @Autowired
    public LabelConfigurationServiceImpl(LabelConfigurationRepository labelConfigurationRepository,
                                         LcEntryRepository lcEntryRepository,
                                         UserTaskService userTaskService,
                                         ModelMapper modelMapper,
                                         LcEntryValueRepository lcEntryValueRepository,
                                         LabelIterationRepository labelIterationRepository,
                                         LcEntryValidation lcEntryValidation) {
        this.labelConfigurationRepository = labelConfigurationRepository;
        this.lcEntryRepository = lcEntryRepository;
        this.userTaskService = userTaskService;
        this.modelMapper = modelMapper;
        this.lcEntryValueRepository = lcEntryValueRepository;
        this.labelIterationRepository = labelIterationRepository;
        this.lcEntryValidation = lcEntryValidation;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public void createLabelConfiguration() {
        //Permissions check
        DataGymSecurity.isAuthenticated();

        LabelConfiguration labelConfiguration = new LabelConfiguration();

        long currentTime = System.currentTimeMillis();
        labelConfiguration.setTimestamp(currentTime);

        labelConfigurationRepository.save(labelConfiguration);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public List<LcEntryExport> exportLabelConfiguration(String configId, HttpServletResponse response) {

        //Permissions check
        DataGymSecurity.isAuthenticated();

        final LabelConfiguration labelConfiguration = findConfigById(configId);

        //Permissions check
        final String projectOrganisation = labelConfiguration.getProject().getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, false);

        final Set<LcEntry> entries = labelConfiguration.getEntries()
                .stream().filter(entry -> entry.getParentEntry() == null)
                .collect(Collectors.toSet());

        final DataGymBinFileConsumer dataGymBinFileConsumer = new DataGymBinFileConsumerImpl(response, true);

        // Construct the fileName
        final long currentTime = System.currentTimeMillis();
        final String projectName = labelConfiguration.getProject().getName();
        final String exportedFileName = "datagym_export_" + currentTime + "_" + projectName + "_configuration.json";

        dataGymBinFileConsumer.onMetaData(exportedFileName, MediaType.APPLICATION_JSON_VALUE);

        return LcEntryExportMapper.convert(entries);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public LabelConfigurationViewModel importLabelConfiguration(String configId, List<LcEntryUpdateBindingModel> entries) {
        //Permissions check
        DataGymSecurity.isAuthenticated();

        LabelConfiguration configById = findConfigById(configId);

        // If there are any LcEntries in the current Configuration, throw exception
        if (!configById.getEntries().isEmpty()) {
            throw new GenericException("config_not_cleared", null, null, configId);
        }

        return updateLabelConfiguration(configId, entries, true);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public List<String> getForbiddenKeyWords() {
        //Permissions check
        DataGymSecurity.isAuthenticated();

        return Arrays
                .asList("geometry", "geometry_type", "class_name",
                        "classification", "classifications", "global_classifications", "classification_type",
                        "tag", "status", "label", "labeler", "label_classes", "reviewer",
                        "image", "imageId", "external_image_ID", "internal_image_ID",
                        "media", "mediaId", "external_media_ID", "internal_media_ID",
                        "lcentrypoint", "lcentryline", "lcentrypolygon", "lcentryrectangle",
                        "point", "line", "polygon", "rectangle",
                        "lcentrychecklist", "lcentryfreetext,", "lcentryradio", "lcentryselect",
                        "checklist", "freetext,", "radio", "select",
                        "lcentrypointvalue", "lcentrylinevalue", "lcentrypolygonvalue", "lcentryrectanglevalue",
                        "lcentrychecklistvalue", "lcentrytextvalue,", "lcentryradiovalue", "lcentryselectvalue",
                        "Dummy_Project", "Dummy_Dataset_One", "Dummy_Dataset_Two",
                        "Intersections"
                );
    }

    /**
     * Compares the lastChangedConfigTimeFromClient and currentConfigLastChangedTimestamp. Returns whether the LabelConfiguration
     * has changed or not.
     *
     * @param lastChangedConfigTimeFromClient the time, that the Client has send to the Server
     * @param iterationId                     the Id of the Iteration. It is needed to get Timestamp of the current LabelConfiguration
     */

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public LcConfigHasConfigChangedViewModel hasConfigChanged(Long lastChangedConfigTimeFromClient, String iterationId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE);

        // Check if LabelIteration exists
        LabelIteration labelIterationById = getLabelIterationById(iterationId);

        //Permissions check
        String projectOrganisation = labelIterationById.getProject().getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, false);

        // Create new LabelTaskCompleteUpdateModel
        LcConfigHasConfigChangedViewModel lcConfigHasConfigChangedViewModel = new LcConfigHasConfigChangedViewModel();
        lcConfigHasConfigChangedViewModel.setHasLabelConfigChanged(false);

        // Get the time, when the config lastly has been changed
        Long currentConfigLastChangedTimestamp = labelIterationById.getProject().getLabelConfiguration().getTimestamp();

        if (currentConfigLastChangedTimestamp > lastChangedConfigTimeFromClient) {
            lcConfigHasConfigChangedViewModel.setHasLabelConfigChanged(true);
        }

        return lcConfigHasConfigChangedViewModel;
    }

    /**
     * Deletes all LcEntries from the current Configuration. The LcEntryValues, that are connected with this
     * Label Configuration, will be also deleted as consequence of the deleting the LcEntries.
     * The LabelConfiguration-Entity will NOT be deleted. The LabelTaskState of all Tasks with State 'COMPLETED'
     * or 'REVIEWED' will be set to 'WAITING_CHANGED'
     */
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public LcConfigDeleteViewModel clearConfig(String configId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE);

        LabelConfiguration labelConfiguration = findConfigById(configId);

        //Permissions check
        String projectOrganisation = labelConfiguration.getProject().getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, false);

        // Delete all LcEntries for the current Configuration
        lcEntryRepository.deleteLcEntriesByConfigurationId(configId);

        // Change TaskState after clearing the LabelConfiguration
        userTaskService.changeTaskStateAfterLabelConfigurationUpdate(configId);

        LcConfigDeleteViewModel lcConfigDeleteViewModel = new LcConfigDeleteViewModel();
        lcConfigDeleteViewModel.setConfigId(configId);

        return lcConfigDeleteViewModel;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public LabelConfigurationViewModel getLabelConfiguration(String configId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE, TOKEN_SCOPE_TYPE);

        LabelConfiguration labelConfiguration = findConfigById(configId);

        //Permissions check
        String projectOrganisation = labelConfiguration.getProject().getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, true);

        List<LcEntryViewModel> allByParentEntryIsNullViewModels = getAllRootLcEntries(configId);

        LabelConfigurationViewModel labelConfigurationViewModel = new LabelConfigurationViewModel();
        labelConfigurationViewModel.setId(configId);
        labelConfigurationViewModel.setEntries(allByParentEntryIsNullViewModels);
        labelConfigurationViewModel.setProjectId(labelConfiguration.getProject().getId());
        labelConfigurationViewModel.setNumberOfReviewedTasks(userTaskService.getNumberOfReviewedTasks(configId));
        labelConfigurationViewModel.setNumberOfCompletedTasks(userTaskService.getNumberOfCompletedTasks(configId));

        return labelConfigurationViewModel;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public LabelConfigurationViewModel updateLabelConfiguration(String configId,
                                                                List<LcEntryUpdateBindingModel> lcEntryUpdateBindingModelList,
                                                                boolean changeStatus) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, TOKEN_SCOPE_TYPE);

        LabelConfiguration labelConfiguration = findConfigById(configId);

        //Permissions check
        String projectOrganisation = labelConfiguration.getProject().getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, false);

        wereEntriesDeletedFromConfig = false;
        wereNewEntriesAddedToConfig = false;

        Set<String> entryIdsFromNewConfig = validateEntryKeys(lcEntryUpdateBindingModelList);

        removeRootEntries(configId, lcEntryUpdateBindingModelList);

        int treeHeight = 1;

        for (LcEntryUpdateBindingModel currentUpdateEntry : lcEntryUpdateBindingModelList) {
            convertAndUpdateEntries(currentUpdateEntry, new ArrayList<>(), null, labelConfiguration, treeHeight, entryIdsFromNewConfig);
        }

        // Set the timestamp of the LabelConfiguration to the current time
        if (wereNewEntriesAddedToConfig) {
            labelConfiguration.setTimestamp(System.currentTimeMillis());
        }

        if (wereNewEntriesAddedToConfig && changeStatus) {
            // change TaskState after LabelConfiguration update
            userTaskService.changeTaskStateAfterLabelConfigurationUpdate(configId);
        }

        // Get All Values from the Current Project
        String projectId = labelConfiguration.getProject().getId();
        List<LcEntryValue> valuesFromProject = lcEntryValueRepository.getAllValuesFromProject(projectId);

        // Validate All Values from the Current Project
        valuesFromProject.forEach(x -> lcEntryValidation.validateSingleEntryValueBeforeTaskCompletion(x, false));

        return getLabelConfiguration(configId);
    }

    /**
     * Depth-First Search(DFS)-Implementation for converting the LcEntryUpdateBindingModel-Tree to LcEntry-Tree and
     * updating and persisting the LcEntries in the database.
     */
    private List<LcEntry> convertAndUpdateEntries(@Valid LcEntryUpdateBindingModel node, List<LcEntry> flatList, LcEntry parentEntry, LabelConfiguration labelConfiguration, int treeHeight, Set<String> entryIdsFromNewConfig) {
        LcEntry entryObject = null;

        if (node != null) {
            // Only support root and  first nesting level
            if ((treeHeight > 2 && isGeometry(node))) {
                throw new GenericException("geometry_definition_incorrect", null, null, node.getType().toLowerCase());
            }

            // Geometries parent must be also a geometry
            if (parentEntry != null && !isGeometry(parentEntry) && isGeometry(node)) {
                throw new GenericException("geometry_definition_incorrect", null, null, node.getType().toLowerCase());
            }

            entryObject = createEntryObject(node);
            entryObject.setConfiguration(labelConfiguration);

            if (entryObject.getId() != null) {
                LcEntry lcEntryById = findLcEntryById(node.getId());
                deleteEntryChildren(lcEntryById, entryIdsFromNewConfig);

                entryObject.setEntryValues(lcEntryById.getEntryValues());
                entryObject.setPreLabelMappingEntries(lcEntryById.getPreLabelMappingEntries());

                String lcEntryType = lcEntryById.getType().name();

                if ((lcEntryType.equals(SELECT) || lcEntryType.equals(CHECKLIST) || lcEntryType.equals(FREETEXT))
                        && checkIfRequiredHasChanged(lcEntryById, entryObject, lcEntryType)) {
                    LOGGER.info("The required field of a {} has changed from false to true", lcEntryType);

                    wereNewEntriesAddedToConfig = true;
                }
            } else {
                wereNewEntriesAddedToConfig = true;

                // Metrics update - increaseCountLcEntry
                Project project = entryObject.getConfiguration().getProject();
                String projectId = project.getId();
                String projectName = project.getName();
                String lcEntryType = entryObject.getType().name();
                increaseCreatedCountLcEntry(lcEntryType, projectId, projectName);
            }

            if (parentEntry != null) {
                if (entryObject.getId() != null && parentEntry.getId() == null) {
                    throw new GenericException("entry_definition_incorrect", null, null, entryObject.getId());
                }

                if (entryObject.getId() == null && parentEntry.getId() != null) {
                    LcEntry lcEntryById = findLcEntryById(parentEntry.getId());
                    compareOldAndUpdatedParent(lcEntryById, parentEntry);

                    deleteEntryChildren(lcEntryById, entryIdsFromNewConfig);
                }

                entryObject.setParentEntry(parentEntry);
                parentEntry.getChildren().add(entryObject);
                parentEntry.setConfiguration(labelConfiguration);
            }
        }

        List<LcEntryUpdateBindingModel> children = Objects.requireNonNull(node).getChildren();
        for (LcEntryUpdateBindingModel child : children) {

            int newTreeHeight = treeHeight + 1;
            if (newTreeHeight > 4) {
                throw new GenericException("config_depth", null, null);
            }

            if (child.getChildren() != null) {
                // Recursive call - Keep converting until no more children
                convertAndUpdateEntries(child, flatList, entryObject, labelConfiguration, newTreeHeight, entryIdsFromNewConfig);
            }
        }

        flatList.add(entryObject);

        lcEntryRepository.save(entryObject);

        // stop or exit condition
        return flatList;
    }

    private boolean checkIfRequiredHasChanged(LcEntry lcEntryFromDb, LcEntry newLcEntry, String lcEntryType) {
        String entryType = lcEntryType.toUpperCase();

        switch (entryType) {
            case SELECT:
                if (lcEntryFromDb instanceof LcEntrySelect && newLcEntry instanceof LcEntrySelect) {
                    LcEntrySelect lcEntrySelectFromDb = (LcEntrySelect) lcEntryFromDb;
                    LcEntrySelect newLcEntrySelect = (LcEntrySelect) newLcEntry;

                    return !lcEntrySelectFromDb.isRequired() && newLcEntrySelect.isRequired();
                }
                break;
            case CHECKLIST:
                if (lcEntryFromDb instanceof LcEntryChecklist && newLcEntry instanceof LcEntryChecklist) {
                    LcEntryChecklist lcEntryChecklistFromDb = (LcEntryChecklist) lcEntryFromDb;
                    LcEntryChecklist newLcEntryChecklist = (LcEntryChecklist) newLcEntry;

                    return !lcEntryChecklistFromDb.isRequired() && newLcEntryChecklist.isRequired();
                }
                break;
            case FREETEXT:
                if (lcEntryFromDb instanceof LcEntryFreeText && newLcEntry instanceof LcEntryFreeText) {
                    LcEntryFreeText lcEntryFreeTextFromDb = (LcEntryFreeText) lcEntryFromDb;
                    LcEntryFreeText newLcEntryFreeText = (LcEntryFreeText) newLcEntry;

                    return !lcEntryFreeTextFromDb.isRequired() && newLcEntryFreeText.isRequired();
                }
                break;
            default:
                throw new GenericException(ENTRY_TYPE_NOT_FOUND_EXCEPTION_MESSAGE, null, null, ENTRY_TYPE);
        }

        throw new GenericException(ENTRY_TYPE_NOT_FOUND_EXCEPTION_MESSAGE, null, null, ENTRY_TYPE);
    }

    private boolean isGeometry(LcEntryUpdateBindingModel node) {
        return isGeometry(node.getType());
    }

    private boolean isGeometry(LcEntry entry) {
        return isGeometry(entry.getType().toString());
    }

    private boolean isGeometry(final String type) {
        final String nodeType = type.toUpperCase();

        return "LINE".equals(nodeType) || "POLYGON".equals(nodeType) || "POINT".equals(nodeType) || "RECTANGLE".equals(nodeType);
    }

    private void deleteEntryChildren(LcEntry lcEntryById, Set<String> entryIdsFromNewConfig) {
        List<LcEntry> children = lcEntryById.getChildren();
        Iterator<LcEntry> iterator = children.iterator();

        while (iterator.hasNext()) {
            LcEntry nextChild = iterator.next();

            if (!entryIdsFromNewConfig.contains(nextChild.getId())) {
                nextChild.setParentEntry(null);
                iterator.remove();
                lcEntryRepository.delete(nextChild);
                wereEntriesDeletedFromConfig = true;
            }
        }
    }

    private Set<String> validateEntryKeys(List<LcEntryUpdateBindingModel> lcEntryUpdateBindingModelList) {
        // Check if all Root-LcEntryKeys are unique
        validateRootEntryKeys(lcEntryUpdateBindingModelList);

        Set<String> entryIdsSet = new HashSet<>();
        Set<String> shortCutsSet = new HashSet<>();
        List<String> forbiddenKeyWords = getForbiddenKeyWords();
        for (int i = 0; i < lcEntryUpdateBindingModelList.size(); i++) {
            Set<String> entryKeySet = new HashSet<>();

            LcEntryUpdateBindingModel currentRootNode = lcEntryUpdateBindingModelList.get(i);
            flatten(currentRootNode, entryKeySet, entryIdsSet, shortCutsSet, forbiddenKeyWords);
        }

        return entryIdsSet;
    }

    /**
     * Check if all Root-LcEntryKeys are unique
     */
    private void validateRootEntryKeys(List<LcEntryUpdateBindingModel> lcEntryUpdateBindingModelList) {
        Set<String> rootEntriesKeySet = new HashSet<>();

        lcEntryUpdateBindingModelList.forEach(entry -> {
            String rootEntryKey = entry.getEntryKey();
            if (!rootEntriesKeySet.add(rootEntryKey)) {
                throw new AlreadyExistsException(LABEL_ENTRY, "entryKey:", rootEntryKey);
            }
        });
    }

    /**
     * Recursive method is used to traverse down the tree
     **/
    private Set<String> flatten(LcEntryUpdateBindingModel node, Set<String> entryKeySet, Set<String> entryIdsSet, Set<String> shortCutsSet, List<String> forbiddenKeyWords) {
        if (node != null) {
            String entryKey = node.getEntryKey();

            if (forbiddenKeyWords.contains(entryKey)) {
                throw new GenericException("forbidden_keyword", null, null, entryKey);
            }

            if (!entryKeySet.add(entryKey)) {
                throw new AlreadyExistsException(LABEL_ENTRY, "entryKey:", entryKey);
            }

            String entryId = node.getId();
            if (entryId != null) {
                findLcEntryById(entryId);
                if (!entryIdsSet.add(entryId)) {
                    throw new AlreadyExistsException(LABEL_ENTRY, "id:", entryId);
                }
            }

            String shortcut = node.getShortcut();
            if (shortcut != null) {
                if (!shortCutsSet.add(shortcut)) {
                    throw new AlreadyExistsException("Shortcut", "value:", shortcut);
                }
            }
        }

        List<LcEntryUpdateBindingModel> children = Objects.requireNonNull(node).getChildren();
        for (LcEntryUpdateBindingModel child : children) {
            if (child.getChildren() != null) {
                flatten(child, entryKeySet, entryIdsSet, shortCutsSet, forbiddenKeyWords);         // Recursive call - Keep flattening until no more children
            }
        }

        // stop or exit condition
        return entryIdsSet;
    }

    private void compareOldAndUpdatedParent(LcEntry parentEntryFromDb, LcEntry parentEntryFromUser) {
        if (!parentEntryFromDb.getId().equals(parentEntryFromUser.getId()) ||
                !parentEntryFromDb.getEntryKey().equals(parentEntryFromUser.getEntryKey()) ||
                !parentEntryFromDb.getType().name().equals(parentEntryFromUser.getType().name())) {
            throw new GenericException("entry_definition_incorrect", null, null, parentEntryFromUser.getId());
        }
    }

    private void removeRootEntries(String configId, List<LcEntryUpdateBindingModel> lcEntryUpdateBindingModelList) {
        List<LcEntry> allByParentEntryIsNullAndConfigurationId = lcEntryRepository
                .findAllByParentEntryIsNullAndConfigurationId(configId);

        int maxSize = allByParentEntryIsNullAndConfigurationId.size();

        Iterator<LcEntry> lcEntryIterator = allByParentEntryIsNullAndConfigurationId.iterator();
        List<String> entriesToRemoveFromConfigIds = new ArrayList<>(maxSize);

        while (lcEntryIterator.hasNext()) {
            LcEntry next = lcEntryIterator.next();
            entriesToRemoveFromConfigIds.add(next.getId());
        }

        lcEntryUpdateBindingModelList.forEach(lcEntryUpdateBindingModel -> {
                    String id = lcEntryUpdateBindingModel.getId();
                    if (entriesToRemoveFromConfigIds.contains(lcEntryUpdateBindingModel.getId())) {
                        if (id != null) {
                            entriesToRemoveFromConfigIds.remove(id);
                        }
                    }
                }
        );

        entriesToRemoveFromConfigIds.forEach(entriesToRemoveId -> {
            LcEntry lcEntryById = findLcEntryById(entriesToRemoveId);
            lcEntryRepository.delete(lcEntryById);
            wereEntriesDeletedFromConfig = true;
        });
    }

    private LabelConfiguration findConfigById(String configId) {
        return labelConfigurationRepository.findById(configId)
                .orElseThrow(() -> new NotFoundException("Label Configuration", "id", "" + configId));
    }

    private LcEntry findLcEntryById(String entryId) {
        return lcEntryRepository.findById(entryId)
                .orElseThrow(() -> new NotFoundException("Entry", "id", "" + entryId));
    }

    private List<LcEntryViewModel> getAllRootLcEntries(String labelConfigurationId) {
        return lcEntryRepository
                .findAllByParentEntryIsNullAndConfigurationId(labelConfigurationId).stream()
                .map(lcEntry -> {
                    LcEntryViewModel lcEntryViewModel = modelMapper.map(lcEntry, LcEntryViewModel.class);
                    return addParentIdToLcEntry(lcEntryViewModel, null);
                })
                .collect(Collectors.toList());
    }

    /**
     * Recursive method is used to traverse down the tree
     **/
    private LcEntryViewModel addParentIdToLcEntry(LcEntryViewModel node, String parentId) {
        if (node != null && parentId != null) {
            node.setLcEntryParentId(parentId);
        }

        List<LcEntryViewModel> children = Objects.requireNonNull(node).getChildren();
        for (LcEntryViewModel child : children) {
            if (child.getChildren() != null) {
                addParentIdToLcEntry(child, node.getId());         // Recursive call - Keep flattening until no more children
            }
        }

        // stop or exit condition
        return node;
    }

    private LcEntry createEntryObject(LcEntryUpdateBindingModel lcUpdateBindingModel1) {
        String entryType = lcUpdateBindingModel1.getType().toUpperCase();

        switch (entryType) {
            case "POINT":
                return LcEntryMapper.mapToPoint(lcUpdateBindingModel1);
            case "LINE":
                return LcEntryMapper.mapToLine(lcUpdateBindingModel1);
            case "POLYGON":
                return LcEntryMapper.mapToPolygon(lcUpdateBindingModel1);
            case "IMAGE_SEGMENTATION":
                return LcEntryMapper.mapToSegmentation(lcUpdateBindingModel1);
            case "RECTANGLE":
                return LcEntryMapper.mapToRectangle(lcUpdateBindingModel1);
            case "SELECT":
                return LcEntryMapper.mapToSelect(lcUpdateBindingModel1);
            case "CHECKLIST":
                return LcEntryMapper.mapToCheckList(lcUpdateBindingModel1);
            case "FREETEXT":
                return LcEntryMapper.mapToText(lcUpdateBindingModel1);
            default:
                throw new GenericException(ENTRY_TYPE_NOT_FOUND_EXCEPTION_MESSAGE, null, null, ENTRY_TYPE);
        }
    }

    // Metrics update - increaseCreatedCountLcEntry
    private void increaseCreatedCountLcEntry(String lcEntryType, String projectId, String projectName) {
        Metrics.summary("datagym.lcEntry.configs.created.summary",
                "lcEntryType", lcEntryType, "projectId", projectId, "projectName", projectName)
                .record(1.0);
    }

    private LabelIteration getLabelIterationById(String iterationId) {
        return labelIterationRepository.findById(iterationId)
                .orElseThrow(() -> new NotFoundException("Label Iteration", "id", "" + iterationId));
    }
}
