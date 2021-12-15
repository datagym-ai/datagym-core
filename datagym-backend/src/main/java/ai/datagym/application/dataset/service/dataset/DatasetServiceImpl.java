package ai.datagym.application.dataset.service.dataset;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetCreateBindingModel;
import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetFilterAndPageParam;
import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetUpdateBindingModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetAllViewModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetProjectViewModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetViewModel;
import ai.datagym.application.dataset.repo.DatasetRepository;
import ai.datagym.application.labelConfiguration.service.LabelConfigurationService;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.service.LabelTaskService;
import ai.datagym.application.limit.entity.DataGymPlan;
import ai.datagym.application.limit.exception.LimitException;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.media.common.BufferedImageConsumer;
import ai.datagym.application.media.entity.*;
import ai.datagym.application.media.models.viewModels.MediaViewModel;
import ai.datagym.application.media.models.viewModels.UrlImageUploadViewModel;
import ai.datagym.application.media.repo.MediaCustomRepository;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.media.service.MediaMapper;
import ai.datagym.application.media.validate.ImageValidator;
import ai.datagym.application.project.entity.MediaType;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.repo.ProjectRepository;
import ai.datagym.application.security.util.DataGymSecurity;
import ai.datagym.application.utils.PageReturn;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.bin.file.entity.BinFileEntity;
import com.eforce21.lib.bin.file.service.BinFileService;
import com.eforce21.lib.exception.AlreadyExistsException;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import com.eforce21.lib.exception.ValidationException;
import io.micrometer.core.instrument.Metrics;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletInputStream;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ai.datagym.application.utils.constants.CommonMessages.*;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class DatasetServiceImpl implements DatasetService {
    private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    private static final int TIME_BEFORE_DELETE_DATASET = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetServiceImpl.class);
    private static final String DATASET_PLACEHOLDER = "dataset";
    private static final String DUMMY_DATASET_ERROR_PLACEHOLDER = "dummy_dataset_limit";

    private final DateTimeFormatter dateFormat8 = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private final DatasetRepository datasetRepository;
    private final BinFileService binFileService;
    private final MediaRepository mediaRepository;
    private final MediaCustomRepository mediaCustomRepository;
    private final LabelTaskService labelTaskService;
    private final Tika tika;
    private final LimitService limitService;
    private final LabelConfigurationService labelConfigurationService;
    private final ProjectRepository projectRepository;


    private boolean deactivateLimiter;

    @Autowired
    public DatasetServiceImpl(DatasetRepository datasetRepository,
                              BinFileService binFileService,
                              MediaRepository mediaRepository,
                              MediaCustomRepository mediaCustomRepository,
                              LabelTaskService labelTaskService,
                              Tika tika, LimitService limitService,
                              LabelConfigurationService labelConfigurationService,
                              @Value(value = "${datagym.deactivate-limiter}") boolean deactivateLimiter,
                              ProjectRepository projectRepository) {
        this.datasetRepository = datasetRepository;
        this.binFileService = binFileService;
        this.mediaRepository = mediaRepository;
        this.mediaCustomRepository = mediaCustomRepository;
        this.labelTaskService = labelTaskService;
        this.tika = tika;
        this.limitService = limitService;
        this.labelConfigurationService = labelConfigurationService;
        this.deactivateLimiter = deactivateLimiter;
        this.projectRepository = projectRepository;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE, BASIC_SCOPE_TYPE})
    @Override
    public DatasetViewModel createDataset(@Valid DatasetCreateBindingModel datasetCreateBindingModel, boolean createDummyDataset) {
        String datasetOwner = datasetCreateBindingModel.getOwner();
        String datasetName = datasetCreateBindingModel.getName();

        //Permissions check
        DataGymSecurity.isAdmin(datasetOwner, false);

        // Get Pricing Plan Type for the current Organisation
        String pricingPlanType = limitService.getLimitsByOrgId(datasetOwner).getPricingPlanType();

        if (datasetCreateBindingModel.getMediaType() == MediaType.VIDEO &&
                !pricingPlanType.equals(DataGymPlan.TEAM_PRO.name())) {
            throw new GenericException("video_not_allowed", null, null);
        }

        if (!createDummyDataset) {
            List<String> forbiddenKeyWords = labelConfigurationService.getForbiddenKeyWords();

            if (forbiddenKeyWords.contains(datasetName)) {
                throw new GenericException("forbidden_keyword", null, null, datasetName);
            }
        }

        Dataset dataset = DatasetMapper.mapToDataset(datasetCreateBindingModel);

        long currentTime = System.currentTimeMillis();
        dataset.setTimestamp(currentTime);

        Dataset createdDataset = datasetRepository.saveAndFlush(dataset);

        // Metrics update - increaseProjectsCount
        increaseDatasetCount(datasetName, datasetOwner);

        return DatasetMapper.mapToDatasetViewModel(createdDataset, false);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public DatasetViewModel updateDataset(String id, @Valid DatasetUpdateBindingModel datasetUpdateBindingModel) {
        Dataset datasetById = getDatasetById(id);

        //Permissions check
        String owner = datasetById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        String datasetName = datasetById.getName();

        if (DUMMY_DATASET_ONE_PLACEHOLDER.equals(datasetName) || DUMMY_DATASET_TWO_PLACEHOLDER.equals(datasetName)) {
            throw new GenericException(DUMMY_DATASET_ERROR_PLACEHOLDER, null, null);
        }

        String currentDatasetName = datasetById.getName();
        String newDatasetName = datasetUpdateBindingModel.getName();

        if (!currentDatasetName.equals(newDatasetName)) {
            Optional<Dataset> datasetByName = datasetRepository.findByNameAndDeletedFalseAndOwner(newDatasetName, owner);

            if (datasetByName.isPresent()) {
                throw new AlreadyExistsException("Dataset", "name", newDatasetName);
            }
        }

        Dataset dataset = DatasetMapper.mapToDataset(datasetUpdateBindingModel, datasetById);

        long currentTime = System.currentTimeMillis();
        dataset.setTimestamp(currentTime);

        Dataset updatedDataset = datasetRepository.saveAndFlush(dataset);
        return DatasetMapper.mapToDatasetViewModel(updatedDataset, false);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE})
    @Override
    public DatasetViewModel getDataset(String id, boolean includeMedia) {
        Dataset datasetById = getDatasetById(id);

        //Permissions check
        String owner = datasetById.getOwner();
        DataGymSecurity.isAdminOrUser(owner, true);

        if (!datasetById.isDeleted()) {
            DatasetViewModel datasetViewModel = DatasetMapper.mapToDatasetViewModel(datasetById, includeMedia);

            // Deactivate pricing limits for the Unit-Tests
            if (!deactivateLimiter) {
                //Check if Organisation-PricingPlan allows upload of Public Urls
                String pricingPlanType = limitService.getLimitsByOrgId(owner).getPricingPlanType();

                datasetViewModel.setAllowPublicUrls(true);

                if (DataGymPlan.FREE_DEVELOPER.name().equals(pricingPlanType)) {
                    datasetViewModel.setAllowPublicUrls(false);
                }
            }


            return datasetViewModel;
        }

        throw new NotFoundException(DATASET_PLACEHOLDER, "id", "" + id);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE})
    @Override
    public PageReturn<MediaViewModel> getDatasetMedia(String id, DatasetFilterAndPageParam filterAndPageParam) {
        Dataset datasetById = getDatasetById(id);

        //Permissions check
        String owner = datasetById.getOwner();
        DataGymSecurity.isAdminOrUser(owner, true);

        if (datasetById.isDeleted()) {
            throw new NotFoundException(DATASET_PLACEHOLDER, "id", "" + id);
        }

        Pageable pageable;
        if (filterAndPageParam.getPageIndex() == null || filterAndPageParam.getNumberOfElementsPerPage() == null) {
            pageable = PageRequest.of(0, filterAndPageParam.getNumberOfElementsPerPage());
        } else {
            pageable = PageRequest.of(filterAndPageParam.getPageIndex(), filterAndPageParam.getNumberOfElementsPerPage());
        }

        PageReturn<Media> mediaByDatasetAndNameAndType = mediaCustomRepository.findUndeletedMediaByDatasetAndNameAndType(datasetById.getId(), filterAndPageParam.getMediaName(),
                filterAndPageParam.getMediaSourceType(), pageable);

        List<MediaViewModel> collect = mediaByDatasetAndNameAndType.getElements().stream()
                .map(MediaMapper::mapToMediaViewModel).
                        collect(Collectors.toList());

        PageReturn<MediaViewModel> pageReturnTo = new PageReturn<>();
        pageReturnTo.setTotalElements(mediaByDatasetAndNameAndType.getTotalElements());
        pageReturnTo.setTotalPages(mediaByDatasetAndNameAndType.getTotalPages());
        pageReturnTo.setElements(collect);


        return pageReturnTo;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public DatasetProjectViewModel getDatasetWithProjects(String datasetId) {
        Dataset datasetById = getDatasetById(datasetId);

        //Permissions check
        String owner = datasetById.getOwner();
        DataGymSecurity.isAdminOrUser(owner, false);

        if (!datasetById.isDeleted()) {
            return DatasetMapper.mapToDatasetProjectViewModel(datasetById);
        }

        throw new NotFoundException(DATASET_PLACEHOLDER, "id", "" + datasetId);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public List<DatasetAllViewModel> getAllDatasetsWithoutOrg() {
        //Permissions check
        DataGymSecurity.isAuthenticated();

        OauthUser user = SecurityContext.get();
        Map<String, String> orgs = user.orgs();

        List<DatasetAllViewModel> result = new ArrayList<>();

        for (Map.Entry<String, String> orgsEntry : orgs.entrySet()) {
            String owner = orgsEntry.getKey();

            List<DatasetAllViewModel> currentOrgDatasetViewModels = datasetRepository
                    .findAllByDeletedIsFalseAndOwner(owner)
                    .stream()
                    .map(dataset -> DatasetMapper.mapToDatasetAllViewModel(dataset, mediaRepository::countAllByDatasetsContainingAndDeletedFalse))
                    .collect(Collectors.toList());

            result.addAll(currentOrgDatasetViewModels);
        }

        return result;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public List<DatasetAllViewModel> getAllDatasets(String owner) {
        List<DatasetAllViewModel> result;

        if (owner == null) {
            result = this.getAllDatasetsWithoutOrg();
        } else {
            DataGymSecurity.isAdminOrUser(owner, false);
            DataGymSecurity.isUserInCurrentOrg(owner);

            result = datasetRepository
                    .findAllByDeletedIsFalseAndOwner(owner).stream()
                    .filter(dataset ->
                                    !dataset.getName().equals(DUMMY_DATASET_ONE_PLACEHOLDER) &&
                                            !dataset.getName().equals(DUMMY_DATASET_TWO_PLACEHOLDER)
                    )
                    .map(dataset -> DatasetMapper.mapToDatasetAllViewModel(dataset,
                            mediaRepository::countAllByDatasetsContainingAndDeletedFalse))
                    .collect(Collectors.toList());
        }

        return result;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public List<DatasetAllViewModel> getProjectSuitableDatasets(String projectId) {
        Project project = getProjectById(projectId);

        DataGymSecurity.isAdminOrUser(project.getOwner(), false);
        DataGymSecurity.isUserInCurrentOrg(project.getOwner());

        return datasetRepository
                .findAllByDeletedIsFalseAndOwnerAndProjectsNotContainingAndMediaType(project.getOwner(),
                                                                                     project,
                                                                                     project.getMediaType())
                .stream()
                .filter(dataset ->
                                !dataset.getName().equals(DUMMY_DATASET_ONE_PLACEHOLDER) &&
                                        !dataset.getName().equals(DUMMY_DATASET_TWO_PLACEHOLDER)
                )
                .map(dataset -> DatasetMapper.mapToDatasetAllViewModel(dataset,
                        mediaRepository::countAllByDatasetsContainingAndDeletedFalse))
                .collect(Collectors.toList());
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public List<DatasetViewModel> getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin() {
        //Permissions check
        DataGymSecurity.isAuthenticated();

        OauthUser user = SecurityContext.get();
        Map<String, String> orgs = user.orgs();

        List<DatasetViewModel> result = new ArrayList<>();

        for (Map.Entry<String, String> orgsEntry : orgs.entrySet()) {
            String currentUserOrg = orgsEntry.getKey();

            boolean isAdminInCurrentOrg = DataGymSecurity.checkIfUserIsAdmin(currentUserOrg, false);

            if (isAdminInCurrentOrg) {
                List<DatasetViewModel> currentOrgDatasetViewModels = datasetRepository
                        .findAllByDeletedIsFalseAndOwner(currentUserOrg)
                        .stream()
                        .map(dataset -> DatasetMapper.mapToDatasetViewModel(dataset, true))
                        .collect(Collectors.toList());

                result.addAll(currentOrgDatasetViewModels);
            }
        }

        return result;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public DatasetViewModel deleteDatasetById(String id, boolean deleteDataset) {
        Dataset datasetById = getDatasetById(id);

        //Permissions check
        String owner = datasetById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        // Sets isDeleted to the value of "deleteDataset"
        datasetById.setDeleted(deleteDataset);
        Long currentTime = null;

        if (deleteDataset) {
            currentTime = System.currentTimeMillis();
        } else {
            String datasetName = datasetById.getName();
            List<Dataset> datasetList = datasetRepository.findAllByName(datasetName);

            if (datasetList.size() > 1) {
                String uuid = UUID.randomUUID().toString();
                if (datasetName.length() > 90) {
                    datasetName = datasetName.substring(0, 89) + "_" + uuid;
                } else {
                    datasetName = datasetName + "_" + uuid;
                }

                datasetById.setName(datasetName);
            }
        }

        datasetById.setDeleteTime(currentTime);

        // Sets isDeleted on all media in the dataset to the value of "deleteDataset"
        Long finalCurrentTime = currentTime;
        datasetById.getMedia().forEach(media -> {
            media.setDeleted(deleteDataset);
            media.setDeleteTime(finalCurrentTime);
        });

        Dataset deletedDataset = datasetRepository.saveAndFlush(datasetById);
        return DatasetMapper.mapToDatasetViewModel(deletedDataset, false);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public void permanentDeleteDatasetFromDB(String datasetId) {
        Dataset datasetById = getDatasetById(datasetId);

        //Permissions check
        String owner = datasetById.getOwner();
        DataGymSecurity.isRoot(owner, false);

        removeMediaAndProjectsFromDataset(datasetById);

        // Delete the dataset
        datasetRepository.delete(datasetById);

        LOGGER.info("Deleted Permanent a Dataset with datasetId : {}", datasetId);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public void permanentDeleteDatasetFromDB(Dataset dataset, boolean isCronJob) {
        //Permissions check
        if (!isCronJob) {
            String owner = dataset.getOwner();
            DataGymSecurity.isRoot(owner, false);
        }

        removeMediaAndProjectsFromDataset(dataset);

        String datasetId = dataset.getId();

        // Delete the dataset
        datasetRepository.delete(dataset);

        String deleteMessage = "Deleted Permanent a Dataset with datasetId : {}";
        if (isCronJob) {
            deleteMessage = "DatasetScheduleJob " + deleteMessage;
        }

        LOGGER.info(deleteMessage, datasetId);
    }

    /**
     * 1. Removes the Relationship between Dataset and Media.
     * The media will be permanent deleted from the Database, if the media is only in the current Dataset.
     * 2. Removes the Relationship between Project and Dataset.
     *
     * @param dataset
     */
    private void removeMediaAndProjectsFromDataset(Dataset dataset) {
        // Remove Relationship between Project and Media
        dataset.getMedia().forEach((Media media) -> {
            if (media.getDatasets().size() == 1) {
                media.getDatasets().remove(dataset);

                if (media instanceof LocalImage) {
                    LocalImage localImage = (LocalImage) media;
                    BinFileEntity binFileEntity = localImage.getBinFileEntity();
                    binFileService.delete(binFileEntity);
                }

                mediaRepository.delete(media);
            } else {
                media.getDatasets().remove(dataset);
            }
        });

        // Remove Relationship between Project and Dataset
        dataset.getProjects().forEach(project -> project.getDatasets().remove(dataset));
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public boolean isDatasetNameUnique(String datasetName) {
        return datasetRepository
                .findByName(datasetName).isEmpty();
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE, BASIC_SCOPE_TYPE})
    @Override
    public boolean isDatasetNameUniqueAndDeletedFalse(String datasetName, String owner) {
        return datasetRepository
                .findByNameAndDeletedFalseAndOwner(datasetName, owner).isEmpty();
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public MediaViewModel createImageFile(String datasetId, String filename, ServletInputStream inputStream) {
        Dataset datasetById = getDatasetById(datasetId);
        String datasetByIdName = datasetById.getName();

        //Permissions check
        String owner = datasetById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        // Check Pricing Plan Limits
        if (DUMMY_DATASET_ONE_PLACEHOLDER.equals(datasetByIdName) || DUMMY_DATASET_TWO_PLACEHOLDER.equals(datasetByIdName)) {
            throw new GenericException(DUMMY_DATASET_ERROR_PLACEHOLDER, null, null);
        }

        // Check if mediaName is unique
        boolean isLocalImageNameUnique = isLocalImageNameUniqueAndDeletedFalse(filename, datasetId);

        if (!isLocalImageNameUnique) {
            throw new AlreadyExistsException("Image", "name", filename);
        }

        BinFileEntity binFileEntity = binFileService.create(filename, inputStream, ALLOWED_IMAGE_MIME_PATTERNS);

        // Check Pricing Plan Limits
        long imageSizeBytes = binFileEntity.getSize();
        long imageSizeKB = (long) (Math.ceil(imageSizeBytes * 0.001));
        limitService.increaseUsedStorage(owner, imageSizeKB);

        // Metrics update - Increase Used Storage
        changeUsedStorage(owner, imageSizeKB, MediaSourceType.LOCAL.name());

        BufferedImage imageInstance = getBufferedImageFromEntity(binFileEntity);

        LocalImage image = new LocalImage();
        image.setMediaSourceType(MediaSourceType.LOCAL);
        image.setBinFileEntity(binFileEntity);
        image.setWidth(imageInstance.getWidth());
        image.setHeight(imageInstance.getHeight());
        image.setMediaName(binFileEntity.getName());
        datasetById.getMedia().add(image);

        long currentTime = System.currentTimeMillis();
        image.setTimestamp(currentTime);

        image.getDatasets().add(datasetById);

        Media createdMediaImage = mediaRepository.saveAndFlush(image);

        datasetById.getProjects().forEach(project -> {

            LabelTask labelTask = labelTaskService.createLabelTaskInternal(project, createdMediaImage, project.getLabelIteration());
            createdMediaImage.getLabelTasks().add(labelTask);
        });

        return MediaMapper.mapToMediaViewModel(createdMediaImage);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE, BASIC_SCOPE_TYPE})
    @Override
    public List<UrlImageUploadViewModel> createImagesByShareableLink(String datasetId, Set<String> imageUrlSet, boolean dummyProjectImages) throws LimitException {
        Dataset datasetById = getDatasetById(datasetId);
        String datasetByIdName = datasetById.getName();

        //Permissions check
        String owner = datasetById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        if (!dummyProjectImages && (DUMMY_DATASET_ONE_PLACEHOLDER.equals(datasetByIdName) || DUMMY_DATASET_TWO_PLACEHOLDER.equals(
                datasetByIdName))) {
            throw new GenericException(DUMMY_DATASET_ERROR_PLACEHOLDER, null, null);
        }
        if (datasetById.getMediaType().equals(MediaType.VIDEO)) {
            throw new GenericException("url_not_allowed_mediatype", null, null);
        }

        // Deactivate pricing limits for the Unit-Tests
        if (!deactivateLimiter && !dummyProjectImages) {

            //Check if Organisation-PricingPlan allows upload of Public-Urls
            String pricingPlanType = limitService.getLimitsByOrgId(owner).getPricingPlanType();

            // Check Pricing Plan Limits
            if (DataGymPlan.FREE_DEVELOPER.name().equals(pricingPlanType)) {
                throw new GenericException("public_urls_upload_not_allowed", null, null);
            }

        }

        List<UrlImageUploadViewModel> urlImageUploadViewModels = new ArrayList<>();

        List<Media> urlImagesFromCurrentDataset = mediaRepository
                .findAllByDatasetsIdAndMediaSourceType(datasetId, MediaSourceType.SHAREABLE_LINK);

        for (String imageUrl : imageUrlSet) {
            UrlImageUploadViewModel urlImageUploadViewModel = new UrlImageUploadViewModel();
            urlImageUploadViewModel.setImageUrl(imageUrl);

            boolean imageUrlExists = checkIfImageUrlExists(
                    imageUrl,
                    urlImagesFromCurrentDataset,
                    urlImageUploadViewModels,
                    urlImageUploadViewModel);

            if (imageUrlExists) {
                continue;
            }

            UrlImage urlImage = new UrlImage();

            try {
                URL url = new URL(imageUrl);

                // Validate the mime type of the current Image
                validateImageType(imageUrl);

                urlImage.setMediaSourceType(MediaSourceType.SHAREABLE_LINK);
                urlImage.setUrl(imageUrl);

                int indexOfLastBackSlash = imageUrl.lastIndexOf('/');
                int indexOfFirstQuestionMark = imageUrl.indexOf('?');

                if (indexOfFirstQuestionMark > 0 && indexOfFirstQuestionMark > indexOfLastBackSlash) {
                    String mediaName = imageUrl.substring(indexOfLastBackSlash + 1, indexOfFirstQuestionMark);
                    urlImage.setMediaName(mediaName);
                } else {
                    String mediaName = imageUrl.substring(indexOfLastBackSlash + 1);
                    urlImage.setMediaName(mediaName);
                }

                long currentTime = System.currentTimeMillis();
                urlImage.setTimestamp(currentTime);

                datasetById.getMedia().add(urlImage);
                urlImage.getDatasets().add(datasetById);

                Media createdMedia = mediaRepository.save(urlImage);

                datasetById.getProjects().forEach(project -> {
                    LabelTask labelTask = labelTaskService.createLabelTaskInternal(project, createdMedia, project.getLabelIteration());
                    createdMedia.getLabelTasks().add(labelTask);
                });

                urlImageUploadViewModel.setInternal_media_ID(createdMedia.getId());
                urlImageUploadViewModel.setExternal_media_ID(createdMedia.getMediaName());

                urlImageUploadViewModel.setMediaUploadStatus(MediaUploadStatus.SUCCESS.name());

            } catch (ValidationException ve) {
                urlImageUploadViewModel.setMediaUploadStatus(MediaUploadStatus.UNSUPPORTED_MIME_TYPE.name());
            } catch (IOException e) {
                urlImageUploadViewModel.setMediaUploadStatus(MediaUploadStatus.FAILED.name());
            }

            urlImageUploadViewModels.add(urlImageUploadViewModel);

            // Metrics update - Increase Used Storage
            String imageUploadStatus = urlImageUploadViewModel.getMediaUploadStatus();

            if (MediaUploadStatus.SUCCESS.name().equals(imageUploadStatus)) {
                changeUsedStorage(owner, 0, MediaSourceType.SHAREABLE_LINK.name());
            }
        }

        return urlImageUploadViewModels;
    }

    /**
     * Validate the mime type of the current Image
     */
    private void validateImageType(String imageUrl) {
        int indexOfLastPoint = imageUrl.lastIndexOf('.');

        String imageExtension = imageUrl.substring(indexOfLastPoint + 1);

        // Validate Mime Type
        ImageValidator.validateMimes(imageExtension, ALLOWED_IMAGE_EXTENSIONS);
    }

    private boolean checkIfImageUrlExists(String imageUrl,
                                          List<Media> urlImagesFromCurrentDataset,
                                          List<UrlImageUploadViewModel> urlImageUploadViewModels,
                                          UrlImageUploadViewModel urlImageUploadViewModel) {

        Optional<Media> imageOptional = urlImagesFromCurrentDataset.stream().filter(image -> {
            UrlImage urlImage = (UrlImage) image;
            String url = urlImage.getUrl();
            return imageUrl.equalsIgnoreCase(url) && !image.isDeleted();
        }).findAny();

        if (imageOptional.isPresent()) {
            urlImageUploadViewModel.setMediaUploadStatus(MediaUploadStatus.DUPLICATE.name());
            urlImageUploadViewModels.add(urlImageUploadViewModel);
            return true;
        }

        return false;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public BufferedImage getBufferedImageFromEntity(BinFileEntity binFileEntity) {
        // Ready image meta data
        BufferedImageConsumer consumer = new BufferedImageConsumer();
        binFileService.consume(binFileEntity, consumer);
        return consumer.getBufferedImage();
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public List<MediaViewModel> getAllMedia(String datasetId) {
        Dataset datasetById = getDatasetById(datasetId);

        //Permissions check
        String owner = datasetById.getOwner();
        DataGymSecurity.isAdminOrUser(owner, false);

        List<Media> allMediaByDatasetId = datasetRepository.getAllMediasByDatasetId(datasetId);

        return MediaMapper.mapToMediaViewModel(allMediaByDatasetId);
    }

    private Dataset getDatasetById(String datasetId) {
        return datasetRepository
                .findById(datasetId)
                .orElseThrow(() -> new NotFoundException(DATASET_PLACEHOLDER, "id", "" + datasetId));
    }

    private boolean isLocalImageNameUniqueAndDeletedFalse(String imageName, String datasetId) {
        return mediaCustomRepository
                .findByMediaNameAndDeletedFalseAndDatasetId(imageName, datasetId).isEmpty();
    }

    // Metrics update - increaseProjectsCount
    private void increaseDatasetCount(String datasetName, String datasetOrg) {
        Metrics.summary("datagym.dataset.summary",
                "dataset.name", datasetName, "dataset.org", datasetOrg)
                .record(1.0);
    }

    // Metrics update - change Used Storage
    private void changeUsedStorage(String organisationId, long imageSizeKB, String mediaSourceType) {
        String loggedInUserId = DataGymSecurity.getLoggedInUserId();

        Metrics.summary("datagym.images.upload.summary", "labeler",
                loggedInUserId, "organisation", organisationId, "mediaSourceType", mediaSourceType)
                .record(imageSizeKB);
    }

    private Project getProjectById(String projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project", "id", "" + projectId));
    }

    // Start time: every day at 02:00 AM
    //@Scheduled(cron = "0 0 2 * * * ")
    public void datasetScheduleJob() {
        LocalDateTime localDateTime = LocalDateTime.now();

        // minus * days
        localDateTime = localDateTime.minusDays(TIME_BEFORE_DELETE_DATASET);

        // convert LocalDateTime to date
        long timeBeforeToDeleteDatasets = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String formatLocalDateTime = dateFormat8.format(localDateTime);

        datasetRepository.findAllDatasetByDeleteTimeBefore(timeBeforeToDeleteDatasets)
                .forEach(dataset -> permanentDeleteDatasetFromDB(dataset, true));

        LOGGER.info("Deleted all Datasets with delete_time before : {}", formatLocalDateTime);
    }

}
