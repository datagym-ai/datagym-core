package ai.datagym.application.dataset.service.awsS3;

import ai.datagym.application.dataset.config.AwsInternalConfiguration;
import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.models.video.ExtractedVideoMetadataTO;
import ai.datagym.application.dataset.repo.DatasetRepository;
import ai.datagym.application.dataset.service.video.VideoMetadataService;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.labelTask.service.LabelTaskService;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.media.entity.AwsS3Video;
import ai.datagym.application.media.entity.InvalidMediaReason;
import ai.datagym.application.media.entity.MediaSourceType;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.project.entity.MediaType;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.security.util.DataGymSecurity;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.lib.exception.NotFoundException;
import com.eforce21.lib.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

import static ai.datagym.application.utils.constants.CommonMessages.OAUTH_SCOPE_TYPE;

@Service
@Transactional(propagation = Propagation.REQUIRED)
@ConditionalOnProperty(
        value = "aws.enabled",
        havingValue = "true")
public class DatasetAwsS3MediaServiceImpl implements DatasetAwsS3MediaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetAwsS3MediaServiceImpl.class);


    private final DatasetRepository datasetRepository;
    private final AwsInternalConfiguration awsInternalConfiguration;
    private final AmazonS3 internalAmazonS3Client;
    private LabelTaskService labelTaskService;
    private LabelTaskRepository labelTaskRepository;
    private MediaRepository mediaRepository;
    private VideoMetadataService videoMetadataService;
    private AwsS3HelperService awsS3HelperService;
    private LimitService limitService;

    public DatasetAwsS3MediaServiceImpl(LabelTaskService labelTaskService,
                                        LabelTaskRepository labelTaskRepository,
                                        MediaRepository mediaRepository,
                                        DatasetRepository datasetRepository,
                                        AwsInternalConfiguration awsInternalConfiguration,
                                        AmazonS3 internalAmazonS3Client,
                                        VideoMetadataService videoMetadataService,
                                        AwsS3HelperService awsS3HelperService,
                                        LimitService limitService) {
        this.labelTaskService = labelTaskService;
        this.labelTaskRepository = labelTaskRepository;
        this.mediaRepository = mediaRepository;
        this.datasetRepository = datasetRepository;
        this.awsInternalConfiguration = awsInternalConfiguration;
        this.internalAmazonS3Client = internalAmazonS3Client;
        this.videoMetadataService = videoMetadataService;
        this.awsS3HelperService = awsS3HelperService;
        this.limitService = limitService;
    }

    @Override
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    public String createAwsPreSignedUploadURI(String datasetId, String filename) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        Dataset datasetById = getDatasetById(datasetId);

        if (datasetById.getMediaType() == MediaType.IMAGE) {
            throw new SystemException("Only video datasets support direct aws s3 uploads!", null);
        }

        //Permissions check
        String owner = datasetById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        java.util.Date expiration = Date.from(
                LocalDateTime.now()
                        .plusMinutes(awsInternalConfiguration.getInternalSignedUrlValidityMinutes())
                        .toInstant(ZoneOffset.UTC));

        String filenameDecrypted = new String(Base64.getDecoder().decode(filename), StandardCharsets.UTF_8);
        String preSignedKey = datasetById.getId() + "/" + System.currentTimeMillis() + "_" + filenameDecrypted;
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(awsInternalConfiguration.getInternalAwsBucketName(),
                                                preSignedKey)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(expiration)
                        .withContentType("video/mp4");


        return internalAmazonS3Client.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }

    @Override
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    public void confirmPreSignedUrlUpload(String datasetId, String preSignedUrl) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        Dataset datasetById = getDatasetById(datasetId);

        if (datasetById.getMediaType() != MediaType.VIDEO) {
            throw new SystemException("Only video datasets support direct aws s3 uploads!", null);
        }

        //Permissions check
        String owner = datasetById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        // Extract media name from pre-signed url
        String mediaName;
        int indexOfLastBackSlash = preSignedUrl.lastIndexOf('/');
        int indexOfFirstQuestionMark = preSignedUrl.indexOf('?');
        if (indexOfFirstQuestionMark > 0 && indexOfFirstQuestionMark > indexOfLastBackSlash) {
            mediaName = preSignedUrl.substring(indexOfLastBackSlash + 1, indexOfFirstQuestionMark);
        } else {
            mediaName = preSignedUrl.substring(indexOfLastBackSlash + 1);
        }

        S3Object objectMetadata = internalAmazonS3Client.getObject(awsInternalConfiguration.getInternalAwsBucketName(),
                datasetId + "/" + mediaName);
        AwsS3Video createdAwsS3Video = new AwsS3Video();
        createdAwsS3Video.setMediaSourceType(MediaSourceType.AWS_S3);
        createdAwsS3Video.setAwsETag(objectMetadata.getObjectMetadata().getETag());
        createdAwsS3Video.setAwsKey(objectMetadata.getKey());
        createdAwsS3Video.setMediaName(mediaName);
        createdAwsS3Video.setTimestamp(System.currentTimeMillis());

        // Add media to the current Dataset
        createdAwsS3Video.getDatasets().add(datasetById);

        // Add the Credentials to the current media
        createdAwsS3Video.setCredentials(null);

        // Save video metadata
        try {
            ExtractedVideoMetadataTO videoMetadata = videoMetadataService.fetchMetaDataFromUrl(
                    awsS3HelperService.createAwsPreSignedGetUriInternal(objectMetadata.getKey()).toString());
            createdAwsS3Video.setHeight(videoMetadata.getHeight());
            createdAwsS3Video.setWidth(videoMetadata.getWidth());
            createdAwsS3Video.setTotalFrames(videoMetadata.getTotalFrames());
            createdAwsS3Video.setDuration(videoMetadata.getDuration());
            createdAwsS3Video.setCodecName(videoMetadata.getCodecName());
            createdAwsS3Video.setrFrameRate(videoMetadata.getrFrameRate());
            createdAwsS3Video.setFormatName(videoMetadata.getFormatName());
            createdAwsS3Video.setSize(videoMetadata.getSize());

            // Check Pricing Plan Limits
            long imageSizeBytes = createdAwsS3Video.getSize();
            long imageSizeKB = (long) (Math.ceil(imageSizeBytes * 0.001));
            limitService.increaseUsedStorage(owner, imageSizeKB);

            // Release resources
            objectMetadata.close();

        } catch (IOException e) {
            createdAwsS3Video.setValid(false);
            createdAwsS3Video.setLastErrorTimeStamp(System.currentTimeMillis());
            createdAwsS3Video.setLastError(InvalidMediaReason.AWS_VIDEO_PARSE_FAIL.name());
            LOGGER.error("Error by parsing video metadata. ID: {}, Reason: {}",
                    createdAwsS3Video.getId(),
                    e.getMessage(),
                    e);
        }

        createdAwsS3Video = mediaRepository.save(createdAwsS3Video);

        datasetById.getMedia().add(createdAwsS3Video);

        for (Project project : datasetById.getProjects()) {
            LabelTask labelTask =
                    labelTaskService.createLabelTaskInternalNoSave(project, createdAwsS3Video,
                                                                   project.getLabelIteration());
            labelTaskRepository.save(labelTask);
            createdAwsS3Video.getLabelTasks().add(labelTask);
        }
    }

    private Dataset getDatasetById(String datasetId) {
        return datasetRepository
                .findById(datasetId)
                .orElseThrow(() -> new NotFoundException("dataset", "id", "" + datasetId));
    }
}
