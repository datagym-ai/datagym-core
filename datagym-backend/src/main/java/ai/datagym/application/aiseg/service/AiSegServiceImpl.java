package ai.datagym.application.aiseg.service;

import ai.datagym.application.aiseg.client.AiSegClient;
import ai.datagym.application.aiseg.common.Base64ImageConsumer;
import ai.datagym.application.aiseg.entity.PreLabelModelName;
import ai.datagym.application.aiseg.model.aiseg.AiSegCalculate;
import ai.datagym.application.aiseg.model.aiseg.AiSegPrefetch;
import ai.datagym.application.aiseg.model.aiseg.AiSegResponse;
import ai.datagym.application.aiseg.model.preLabel.PreLabelRequest;
import ai.datagym.application.dataset.entity.DatasetAwsS3UserCredentials;
import ai.datagym.application.errorHandling.ServiceUnavailableException;
import ai.datagym.application.labelIteration.entity.LabelSource;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.geometry.LcEntryPolygonValue;
import ai.datagym.application.labelIteration.entity.geometry.LcEntryRectangleValue;
import ai.datagym.application.labelIteration.entity.geometry.PointPojo;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueCreateBindingModel;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelIteration.service.LcEntryValueService;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.media.entity.*;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.prelLabeling.entity.PreLabelMappingEntry;
import ai.datagym.application.security.util.DataGymSecurity;
import ai.datagym.application.utils.constants.CommonMessages;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.lib.bin.file.entity.BinFileEntity;
import com.eforce21.lib.bin.file.service.BinFileService;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import com.eforce21.lib.exception.SystemException;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import retrofit2.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

import static ai.datagym.application.utils.constants.CommonMessages.*;

@Service
@Transactional(propagation = Propagation.REQUIRED)
@ConditionalOnProperty(
        value = "aiseglb.enabled",
        havingValue = "true")
public class AiSegServiceImpl implements AiSegService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiSegServiceImpl.class);
    public static final String VIDEO_FRAME_SPLIT = "_f_";

    private final Optional<AiSegClient> aiSegClient;
    private final BinFileService binFileService;
    private final MediaRepository mediaRepository;
    private final LimitService limitService;
    private final LcEntryValueService lcEntryValueService;
    private final LcEntryValueRepository lcEntryValueRepository;

    @Value("${aiseglb.environment}")
    private String loadbalancerEnvironment;

    // Constructor without aiseg-client
    public AiSegServiceImpl(BinFileService binFileService,
                            @Autowired(required = false) Optional<AiSegClient> aiSegClient,
                            MediaRepository mediaRepository,
                            LimitService limitService,
                            LcEntryValueService lcEntryValueService,
                            LcEntryValueRepository lcEntryValueRepository) {
        this.binFileService = binFileService;
        this.aiSegClient = aiSegClient;
        this.mediaRepository = mediaRepository;
        this.limitService = limitService;
        this.lcEntryValueService = lcEntryValueService;
        this.lcEntryValueRepository = lcEntryValueRepository;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void prepare(String mediaId, @Nullable Integer frameNumber, @Nullable String dataUri) {
        // Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE, TOKEN_SCOPE_TYPE);

        if (mediaId.equals("demo-1")) {
            try {
                String demoImageB64 = Base64.getEncoder().encodeToString(
                        Files.readAllBytes(ResourceUtils.getFile("classpath:demo-3.jpg").toPath()));
                AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(mediaId, demoImageB64);
                aiSegClient.orElseThrow(() -> new ServiceUnavailableException("ai")).prepare(aiSegPrefetch).execute();
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (dataUri == null) {
            Base64ImageConsumer base64ImageConsumer = getBase64ImageConsumerFromImage(mediaId, false);
            prefetch(mediaId, base64ImageConsumer.getBase64Image());
        } else {
            // Enstrip the type/encoding headers from the base64EncodedJpegImage uri
            final int dataStartIndex = dataUri.indexOf(",") + 1;
            final String base64EncodedJpegImage = dataUri.substring(dataStartIndex);

            // Adding the frame number to the media id is important to identify the correct frame screenshot
            String prefetchMediaId = mediaId;
            if (frameNumber != null) {
                prefetchMediaId = prefetchMediaId + VIDEO_FRAME_SPLIT + frameNumber;
            }
            prefetch(prefetchMediaId, base64EncodedJpegImage);
        }
    }

    private Base64ImageConsumer getBase64ImageConsumerFromImage(String mediaId, boolean cronJob) {
        Media imageById = getMediaById(mediaId);
        MediaSourceType mediaSourceType = imageById.getMediaSourceType();

        // Check if Image is from Dummy_Project
        checkIfMediaFromDummyProject(imageById);

        if (!cronJob) {
            // Check Pricing Limits
            checkPricingPlanLimits(imageById);
        }

        if (mediaSourceType.equals(MediaSourceType.LOCAL)) {
            LocalImage localImage = (LocalImage) Hibernate.unproxy(imageById);

            BinFileEntity binFileEntity = localImage.getBinFileEntity();
            Base64ImageConsumer base64ImageConsumer = new Base64ImageConsumer();
            binFileService.consume(binFileEntity, base64ImageConsumer);

            return base64ImageConsumer;
        } else if (mediaSourceType.equals(MediaSourceType.SHAREABLE_LINK)) {
            UrlImage urlImage = (UrlImage) Hibernate.unproxy(imageById);
            String imageUrl = urlImage.getUrl();

            try (InputStream inputStream = new URL(imageUrl).openStream()) {
                Base64ImageConsumer base64ImageConsumer = new Base64ImageConsumer();
                base64ImageConsumer.onStream(inputStream);

                return base64ImageConsumer;
            } catch (IOException e) {
                LOGGER.error("Failed during file streaming", e);
                throw new GenericException("file_stream", null, null);
            }
        } else if (mediaSourceType.equals(MediaSourceType.AWS_S3)) {
            if (imageById instanceof AwsS3Video) {
                throw new SystemException("Videos cant be parsed to Base64", null);
            }
            AwsS3Image awsS3Image = (AwsS3Image) Hibernate.unproxy(imageById);

            //Get AwsS3Credentials from the Data Base
            DatasetAwsS3UserCredentials credentials = awsS3Image.getCredentials();

            String bucketName = credentials.getBucketName();
            String accessKey = credentials.getAccessKey();
            String secretKey = credentials.getSecretKey();
            String bucketRegion = credentials.getBucketRegion();

            try {
                // Create Aws Credentials Instance
                AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

                AmazonS3 amazonS3client = AmazonS3ClientBuilder
                        .standard()
                        .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                        .withRegion(Regions.valueOf(bucketRegion))
                        .build();

                S3Object object = amazonS3client.getObject(bucketName, awsS3Image.getAwsKey());
                S3ObjectInputStream objectContent = object.getObjectContent();

                Base64ImageConsumer base64ImageConsumer = new Base64ImageConsumer();
                base64ImageConsumer.onStream(objectContent);

                // Release resources
                object.close();

                return base64ImageConsumer;
            } catch (AmazonServiceException ase) {
                // The call was transmitted successfully, but Amazon S3 couldn't process
                // it, so it returned an error response.
                awsS3Image.setLastError(ase.getErrorMessage());
                awsS3Image.setLastErrorTimeStamp(System.currentTimeMillis());

                LOGGER.error("Failed during file streaming", ase);

                throw new GenericException("file_stream", null, null);
            } catch (Exception e) {
                // Amazon S3 couldn't be contacted for a response, or the client
                // couldn't parse the response from Amazon S3.
                awsS3Image.setLastError(e.getMessage());
                awsS3Image.setLastErrorTimeStamp(System.currentTimeMillis());

                LOGGER.error("Failed during file streaming", e);

                throw new GenericException("file_stream", null, null);
            }
        }

        throw new GenericException("file_stream", null, null);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public AiSegResponse calculate(AiSegCalculate aiSegCalculate) {
        // Set the environment-string for the metrics
        aiSegCalculate.setEnvironment(loadbalancerEnvironment);

        String imageId = aiSegCalculate.getImageId();
        Media mediaById = getMediaById(imageId);

        if (mediaById instanceof AwsS3Video) {
            if (aiSegCalculate.getFrameNumber() == null) {
                throw new SystemException("Frame number must be provided for video projects!", null);
            } else {
                String videoImageId = aiSegCalculate.getImageId() + VIDEO_FRAME_SPLIT + aiSegCalculate.getFrameNumber();
                aiSegCalculate.setImageId(videoImageId);
            }
        }

        // Check if Image is from Dummy_Project
        checkIfMediaFromDummyProject(mediaById);

        // Check Pricing Limits
        checkPricingPlanLimits(mediaById);

        try {
            Response<AiSegResponse> execute = aiSegClient.orElseThrow(() -> new ServiceUnavailableException("ai")).calculate(aiSegCalculate).execute();
            if (execute.isSuccessful()) {

                // Decrease remaining AiSeg usages
                decreaseAiSegRemaining(mediaById);

                return execute.body();
            } else {
                if (execute.code() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
                    LOGGER.error("AiSeg, no available gpus");
                    throw new GenericException(CommonMessages.AISEG_GPU_BUSY, null, null);
                } else {
                    LOGGER.error("Unhandled aiseg response code");
                    throw new GenericException(CommonMessages.AISEG_COMMUNICATION_ERROR, null, null);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to calculate image", e);
            throw new GenericException(CommonMessages.AISEG_COMMUNICATION_ERROR, null, null);
        }
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void finish(String mediaId) {
        Media imageById = getMediaById(mediaId);

        // Check if Image is from Dummy_Project
        checkIfMediaFromDummyProject(imageById);

        try {
            aiSegClient.orElseThrow(() -> new ServiceUnavailableException("ai")).finish(mediaId).execute();
        } catch (IOException e) {
            LOGGER.error("AiSeg communication error", e);
            throw new GenericException(CommonMessages.AISEG_COMMUNICATION_ERROR, null, null);
        }
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void finishUserSession(String userSessionUUID) {
        try {
            aiSegClient.orElseThrow(() -> new ServiceUnavailableException("ai")).finishUserSession(userSessionUUID).execute();
        } catch (IOException e) {
            LOGGER.error("AiSeg communication error", e);
            throw new GenericException(CommonMessages.AISEG_COMMUNICATION_ERROR, null, null);
        }
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void finishFrameImage(String mediaId, Integer frameNumber) {
        Media mediaById = getMediaById(mediaId);

        if (mediaById instanceof AwsS3Video) {
            // Check if Image is from Dummy_Project
            checkIfMediaFromDummyProject(mediaById);

            String finishMediaId = mediaId + VIDEO_FRAME_SPLIT + frameNumber;

            try {
                aiSegClient.orElseThrow(() -> new ServiceUnavailableException("ai")).finish(finishMediaId).execute();
            } catch (IOException e) {
                LOGGER.error("AiSeg communication error", e);
                throw new GenericException(CommonMessages.AISEG_COMMUNICATION_ERROR, null, null);
            }
        } else {
            throw new SystemException("This method only allows video frames to finish!", null);
        }
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public void preLabelImage(Media media,
                              Map<String, Map<String, String>> requestedClasses,
                              LabelTask labelTask,
                              List<PreLabelMappingEntry> mappings) {

        // PreLabel the Image
        Map<String, List<Map<String, Object>>> response = calculatePreLabel(media, requestedClasses);

        String mediaId = media.getId();
        String labelTaskId = labelTask.getId();
        String labelIterationId = labelTask.getProject().getLabelIteration().getId();

        for (Map.Entry<String, List<Map<String, Object>>> responseEntry : response.entrySet()) {
            String currentRequestedModel = responseEntry.getKey();
            List<Map<String, Object>> currentValueObjectList = responseEntry.getValue();

            Optional<PreLabelMappingEntry> optionalCurrentMapping = mappings
                    .stream()
                    .filter(mapping -> mapping.getPreLabelModel().equals(currentRequestedModel))
                    .findFirst();

            if (optionalCurrentMapping.isPresent()) {
                for (Map<String, Object> currentValueObject : currentValueObjectList) {
                    Object geometry = currentValueObject.get("geometry");

                    if (geometry instanceof List) {
                        List<Map<String, Integer>> coordinates = (List<Map<String, Integer>>) geometry;

                        PreLabelMappingEntry labelMappingEntry = optionalCurrentMapping.get();
                        String entryId = labelMappingEntry.getLcEntry().getId();

                        // create ValuesTree
                        LcEntryValue lcEntryValue = createLcEntryValue(mediaId, entryId, labelIterationId, labelTask);

                        if (lcEntryValue instanceof LcEntryRectangleValue) {
                            Map<String, Integer> currentRectAngleCoordinates = coordinates.get(0);
                            Integer x = currentRectAngleCoordinates.get("x");
                            Integer y = currentRectAngleCoordinates.get("y");
                            Integer width = currentRectAngleCoordinates.get("w");
                            Integer height = currentRectAngleCoordinates.get("h");

                            LcEntryRectangleValue lcEntryRectangleValue = (LcEntryRectangleValue) lcEntryValue;
                            lcEntryRectangleValue.setX(Double.valueOf(x));
                            lcEntryRectangleValue.setY(Double.valueOf(y));
                            lcEntryRectangleValue.setHeight(Double.valueOf(height));
                            lcEntryRectangleValue.setWidth(Double.valueOf(width));

                            lcEntryRectangleValue.setLabelSource(LabelSource.AI_PRE_LABEL);
                            lcEntryRectangleValue.setLabeler(LabelSource.AI_PRE_LABEL.name());
                        }

                        if (lcEntryValue instanceof LcEntryPolygonValue) {
                            LcEntryPolygonValue lcEntryPolygonValue = (LcEntryPolygonValue) lcEntryValue;

                            List<PointPojo> polygonPoints = new ArrayList<>();
                            ((List<Map<String, Integer>>) geometry).forEach(polygonPoint -> {
                                PointPojo point = new PointPojo();
                                point.setX(polygonPoint.get("x").doubleValue());
                                point.setY(polygonPoint.get("y").doubleValue());
                                point.setId(null);
                                point.setLcEntryPolygonValue(lcEntryPolygonValue);
                                polygonPoints.add(point);
                            });

                            // Use clear() and addAll() instead of setPoints(), because  Hibernate is unable to track changes to that collection
                            // if you set it to a new object
                            lcEntryPolygonValue.getPoints().clear();
                            lcEntryPolygonValue.getPoints().addAll(polygonPoints);

                            lcEntryPolygonValue.setLabelSource(LabelSource.AI_PRE_LABEL);
                            lcEntryPolygonValue.setLabeler(LabelSource.AI_PRE_LABEL.name());
                        }

                        // Validate the current Value
                        lcEntryValueService.traverseAndValidateLcEntryValue(lcEntryValue);

                        LOGGER.info("PreLabeling: Added Geometry with preLabelModel: {} to LabelTask with id: {}",
                                currentRequestedModel, labelTaskId);
                    }
                }

            } else {
                LOGGER.error("PreLabeling: Invalid preLabelModel: {}", currentRequestedModel);
            }
        }
    }

    private Map<String, List<Map<String, Object>>> calculatePreLabel(Media media,
                                                                     Map<String, Map<String, String>> requestedClasses) {
        String imageId = media.getId();

        Base64ImageConsumer base64ImageConsumer = getBase64ImageConsumerFromImage(imageId, true);
        String base64Image = base64ImageConsumer.getBase64Image();

        PreLabelRequest preLabelCalculateRequest = new PreLabelRequest(
                imageId,
                base64Image,
                PreLabelModelName.RESNET50_COCO_BEST.getModelName(),
                requestedClasses,
                loadbalancerEnvironment);

        // Check if Image is from Dummy_Project
        checkIfMediaFromDummyProject(media);

        try {
            Response<Map<String, List<Map<String, Object>>>> execute = aiSegClient
                    .orElseThrow(() -> new ServiceUnavailableException("ai"))
                    .preLabelImage(preLabelCalculateRequest)
                    .execute();

            if (execute.isSuccessful()) {
                return execute.body();
            } else {
                if (execute.code() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
                    LOGGER.error("AiSeg, no available gpus");
                    throw new GenericException(CommonMessages.AISEG_GPU_BUSY, null, null);
                } else {
                    LOGGER.error("Unhandled aiseg response code");
                    throw new GenericException(CommonMessages.AISEG_COMMUNICATION_ERROR, null, null);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to calculate media", e);
            throw new GenericException(CommonMessages.AISEG_COMMUNICATION_ERROR, null, null);
        }
    }

    /**
     * Create new LcEntryValue for the current Geometry
     */
    private LcEntryValue createLcEntryValue(String mediaId,
                                            String lcEntryId,
                                            String labelIterationId,
                                            LabelTask labelTask) {

        LcEntryValueCreateBindingModel lcEntryValueCreateBindingModel = new LcEntryValueCreateBindingModel();
        lcEntryValueCreateBindingModel.setMediaId(mediaId);
        lcEntryValueCreateBindingModel.setIterationId(labelIterationId);
        lcEntryValueCreateBindingModel.setLcEntryId(lcEntryId);
        lcEntryValueCreateBindingModel.setLcEntryValueParentId(null);
        lcEntryValueCreateBindingModel.setLabelTaskId(labelTask.getId());

        // Create Geometry with LcEntryKey equals to currentRootClassificationEntryKey
        return lcEntryValueService.createLcEntryValueTreeGetRootInternal(lcEntryId, lcEntryValueCreateBindingModel);
    }

    private void prefetch(String mediaId, String base64Image) {
        AiSegPrefetch aiSegPrefetch = new AiSegPrefetch(mediaId, base64Image);
        try {
            aiSegClient.orElseThrow(() -> new ServiceUnavailableException("ai")).prepare(aiSegPrefetch).execute();
        } catch (IOException e) {
            LOGGER.error("Failed to prepare media", e);
            throw new GenericException(CommonMessages.AISEG_COMMUNICATION_ERROR, null, null);
        }
    }

    private Media getMediaById(String mediaId) {
        return mediaRepository.findById(mediaId)
                .orElseThrow(() -> new NotFoundException("media", "id", "" + mediaId));
    }

    /**
     * Check Pricing Limits
     */
    private void checkPricingPlanLimits(Media mediaById) {
        Optional<LabelTask> taskOptional = mediaById.getLabelTasks().stream().findAny();

        if (taskOptional.isPresent()) {
            LabelTask labelTask = taskOptional.get();
            String projectOrg = labelTask.getProject().getOwner();

            limitService.checkAiSegLimits(projectOrg);
        }
    }

    private void decreaseAiSegRemaining(Media mediaById) throws IOException {
        Optional<LabelTask> taskOptional = mediaById.getLabelTasks().stream().findAny();

        if (taskOptional.isPresent()) {
            LabelTask labelTask = taskOptional.get();
            String projectOrg = labelTask.getProject().getOwner();

            limitService.decreaseAiSegRemaining(projectOrg);
        }
    }

    /**
     * Check if media is from Dummy_Project
     */
    private void checkIfMediaFromDummyProject(Media mediaById) {
        boolean isDummyDataset = mediaById
                .getDatasets()
                .stream()
                .anyMatch(dataset ->
                        "Dummy_Dataset_One".equals(dataset.getName()) || "Dummy_Dataset_Two".equals(dataset.getName()));

        if (isDummyDataset) {
            throw new GenericException("dummy_project_limit", null, null);
        }
    }

    private LcEntryValue getLcEntryValue(String lcEntryValueId) {
        return lcEntryValueRepository.findById(lcEntryValueId)
                .orElseThrow(() -> new NotFoundException("Label Entry Value", "id", "" + lcEntryValueId));
    }
}
