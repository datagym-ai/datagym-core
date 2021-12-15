package ai.datagym.application.media.service;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.entity.DatasetAwsS3UserCredentials;
import ai.datagym.application.dataset.repo.AwsS3UserCredentialsRepository;
import ai.datagym.application.dataset.service.awsS3.AwsS3HelperService;
import ai.datagym.application.errorHandling.ServiceUnavailableException;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.media.entity.*;
import ai.datagym.application.media.models.viewModels.MediaViewModel;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.media.validate.ImageValidator;
import ai.datagym.application.security.util.DataGymSecurity;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.bin.file.entity.BinFileEntity;
import com.eforce21.lib.bin.file.model.BinFileConsumerHttp;
import com.eforce21.lib.bin.file.service.BinFileService;
import com.eforce21.lib.exception.*;
import io.micrometer.core.instrument.Metrics;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static ai.datagym.application.media.entity.InvalidMediaReason.*;
import static ai.datagym.application.utils.constants.CommonMessages.*;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class MediaServiceImpl implements MediaService {
    private static final String MEDIA_ENTITY_NAME = "media";
    private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    private static final int TIME_BEFORE_DELETE_MEDIA = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaServiceImpl.class);

    private final ApplicationContext applicationContext;
    private final DateTimeFormatter dateFormat8 = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private final MediaRepository mediaRepository;
    private final BinFileService binFileService;
    private final Tika tika;
    private final LimitService limitService;
    private final LabelTaskRepository labelTaskRepository;
    private final AwsS3UserCredentialsRepository awsS3UserCredentialsRepository;
    private final Optional<AwsS3HelperService> awsS3HelperService;

    @Autowired
    public MediaServiceImpl(ApplicationContext applicationContext,
                            MediaRepository mediaRepository,
                            BinFileService binFileService,
                            Tika tika,
                            LimitService limitService,
                            LabelTaskRepository labelTaskRepository,
                            AwsS3UserCredentialsRepository awsS3UserCredentialsRepository,
                            @Autowired(required = false) Optional<AwsS3HelperService> awsS3HelperService) {
        this.applicationContext = applicationContext;
        this.mediaRepository = mediaRepository;
        this.binFileService = binFileService;
        this.tika = tika;
        this.limitService = limitService;
        this.labelTaskRepository = labelTaskRepository;
        this.awsS3UserCredentialsRepository = awsS3UserCredentialsRepository;
        this.awsS3HelperService = awsS3HelperService;
    }


    /**
     * Fetching the Spring Proxy of the class in the class itself and use it to call methods on it rather than "this".
     * This Proxy will be used to create a new Transaction (similar to the Propagation Mode - Propagation.REQUIRES_NEW)
     * within a class annotated with @Transactional(propagation = Propagation.REQUIRED)
     */
    private MediaServiceImpl getSpringProxy() {
        return applicationContext.getBean(this.getClass());
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE})
    @Override
    public String streamMediaFile(String mediaId, HttpServletResponse response, boolean downloadFile)
            throws IOException {
        DataGymSecurity.isAuthenticated();
        Media media = getMediaById(mediaId);

        // Permissions check
        isCurrentUserPermittedToManipulateMedia(media, false);

        if ((media instanceof LocalImage) && !media.isDeleted()) {   // Stream LocalImage

            BinFileConsumerHttp binFileConsumerHttp = new BinFileConsumerHttp(response, downloadFile);

            LocalImage localImage = (LocalImage) media;

            BinFileEntity binFileEntity = localImage.getBinFileEntity();

            binFileService.consume(binFileEntity, binFileConsumerHttp);
            return "";

        } else if ((media instanceof UrlImage) && !media.isDeleted()) { // Stream UrlImage
            UrlImage urlImage = (UrlImage) media;
            String urlString = urlImage.getUrl();

            try {
                URL url = createUrl(urlString);

                // Get mimeType of the Image
                String mimeType = tika.detect(url);

                // Validate Mime Type
                ImageValidator.validateMimes(mimeType, ALLOWED_IMAGE_MIME_PATTERNS);

                String formatName = mimeType.split("/")[1].toLowerCase();

                // Read a Image from the Url
                BufferedImage img = ImageIO.read(url);

                // Stream the Image
                response.setHeader("Content-Type", mimeType);
                ImageIO.write(img, formatName, response.getOutputStream());
                return "";

            } catch (ValidationException ve) {
                // Set the LabelTaskState of all labelTasks for the current ImageUrl auf SKIPPED
                getSpringProxy().setLabelTaskStateToSkippedAndImageToInvalid(urlImage, INVALID_MIME_TYPE);
                throw ve;
            } catch (FileNotFoundException | UnknownHostException fnf) {
                // Set the LabelTaskState of all labelTasks for the current ImageUrl auf SKIPPED
                getSpringProxy().setLabelTaskStateToSkippedAndImageToInvalid(urlImage, INVALID_URL);

                ValidationException ve = new ValidationException();
                String imageUrl = urlImage.getUrl();
                ve.addDetail(new Detail("data", "Invalid image url", imageUrl));
                throw ve;
            } catch (IOException e) {
                throw new GenericException("file_stream", "Error by streaming image file", e);
            }
        } else if ((media instanceof AwsS3Media) && !media.isDeleted()) { // Stream AwsS3Media
            AwsS3Media awsS3Image = (AwsS3Media) media;

            try {
                // When no credentials are stored it is located on the datagym aws and not a customer aws s3
                if (awsS3Image.getCredentials() == null) {
                    URL url = awsS3HelperService.orElseThrow(() -> new ServiceUnavailableException("aws")).createAwsPreSignedGetUriInternal(awsS3Image.getAwsKey());
                    return url.toString();
                } else {
                    //Get AwsS3Credentials from the Data Base
                    DatasetAwsS3UserCredentials credentials = awsS3Image.getCredentials();
                    String accessKey = credentials.getAccessKey();
                    String secretKey = credentials.getSecretKey();
                    String bucketRegion = credentials.getBucketRegion();
                    String bucketName = credentials.getBucketName();

                    // Create Aws Credentials Instance
                    AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

                    AmazonS3 amazonS3client = AmazonS3ClientBuilder
                            .standard()
                            .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                            .withRegion(Regions.valueOf(bucketRegion))
                            .build();

                    S3Object object = amazonS3client.getObject(bucketName, awsS3Image.getAwsKey());
                    S3ObjectInputStream objectContent = object.getObjectContent();

                    // Get mimeType of the Image
                    String mimeType = object.getObjectMetadata().getContentType();

                    // Validate Mime Type
                    ImageValidator.validateMimes(mimeType, ALLOWED_IMAGE_MIME_PATTERNS);

                    String formatName = mimeType.split("/")[1].toLowerCase();

                    // Read a Image from the Url
                    BufferedImage img = ImageIO.read(objectContent);

                    // Stream the Image
                    response.setHeader("Content-Type", mimeType);
                    ImageIO.write(img, formatName, response.getOutputStream());
                    return "";
                }

            } catch (ValidationException ve) {
                // Set the LabelTaskState of all labelTasks for the current ImageUrl auf SKIPPED
                getSpringProxy().setLabelTaskStateToSkippedAndImageToInvalid(awsS3Image, INVALID_MIME_TYPE);
                throw ve;
            } catch (AmazonServiceException ase) {
                // The call was transmitted successfully, but Amazon S3 couldn't process
                // it, so it returned an error response.
                awsS3Image.setLastError(ase.getErrorMessage());
                awsS3Image.setLastErrorTimeStamp(System.currentTimeMillis());

                // Set the LabelTaskState of all labelTasks for the current ImageUrl auf SKIPPED
                getSpringProxy().setLabelTaskStateToSkippedAndImageToInvalid(awsS3Image, AWS_ERROR);

                ValidationException ve = new ValidationException();
                String imageUrl = awsS3Image.getAwsKey();
                String errorMessage = ase.getErrorMessage();
                ve.addDetail(new Detail("data", "Amazon S3 Service Exception", errorMessage, imageUrl));
                throw ve;
            } catch (SdkClientException e) {
                // Amazon S3 couldn't be contacted for a response, or the client
                // couldn't parse the response from Amazon S3.
                awsS3Image.setLastError(e.getMessage());
                awsS3Image.setLastErrorTimeStamp(System.currentTimeMillis());

                ValidationException ve = new ValidationException();
                String errorMessage = e.getMessage();
                ve.addDetail(new Detail("data", "Amazon Client Exception", errorMessage));
                throw ve;
            } catch (Exception e) {
                throw new GenericException("file_stream", null, null);
            }
        }
        throw new NotFoundException(MEDIA_ENTITY_NAME, "id", "" + mediaId);
    }

    /**
     * Set the LabelTaskState of all labelTasks for the current ImageUrl auf SKIPPED
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setLabelTaskStateToSkippedAndImageToInvalid(Media media, InvalidMediaReason reason) {
        List<LabelTask> labelTasks = media.getLabelTasks();

        // Set the Image to invalid and the reason for that
        media.setValid(false);
        media.setInvalidMediaReason(reason);
        mediaRepository.saveAndFlush(media);

        // Iterate over all labelTasks for the current ImageUrl and set their LabelTaskState to SKIPPED
        for (LabelTask labelTask : labelTasks) {
            labelTask.setLabelTaskState(LabelTaskState.SKIPPED);
            labelTaskRepository.saveAndFlush(labelTask);
        }
    }

    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public URL createUrl(String urlString) throws MalformedURLException {
        return new URL(urlString);
    }

    /**
     * Sets deleted-property of the current Image to the value of {@param deleteMedia}
     */
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public MediaViewModel deleteMediaFile(String mediaId, boolean deleteMedia) {
        DataGymSecurity.isAuthenticated();
        Media mediaById = getMediaById(mediaId);

        // Permissions check
        String owner = isCurrentUserPermittedToManipulateMedia(mediaById, false);
        DataGymSecurity.isAdmin(owner, false);

        boolean deleted = mediaById.isDeleted();

        if (deleted == deleteMedia) {
            throw new GenericException("image_delete", null, null, mediaId);
        }

        mediaById.setDeleted(deleteMedia);
        Long currentTime = null;

        if (deleteMedia) {
            currentTime = System.currentTimeMillis();

            // Check Pricing Plan Limits
            decreaseUsedStorage(owner, mediaById);
        } else {
            // Check Pricing Plan Limits
            increaseUsedStorage(owner, mediaById);
        }

        mediaById.setDeleteTime(currentTime);

        Media savedMedia = mediaRepository.save(mediaById);
        return MediaMapper.mapToMediaViewModel(savedMedia);
    }

    /**
     * Sets deleted-property of all Images from {@param mediaIdSet} to the value of {@param deleteMedia}
     */
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void deleteMediaFileList(Set<String> mediaIdSet, boolean deleteMedia) {
        // Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE);

        // Get all media from the Database
        List<Media> allMediaById = getAllMediaById(mediaIdSet);

        for (Media mediaById : allMediaById) {
            // Permissions check
            String owner = isCurrentUserPermittedToManipulateMedia(mediaById, false);
            DataGymSecurity.isAdmin(owner, false);

            boolean deleted = mediaById.isDeleted();

            if (deleted == deleteMedia) {
                throw new GenericException("image_delete", null, null, mediaById.getId());
            }

            mediaById.setDeleted(deleteMedia);
            Long currentTime = null;

            if (deleteMedia) {
                currentTime = System.currentTimeMillis();

                // Check Pricing Plan Limits
                decreaseUsedStorage(owner, mediaById);
            } else {
                // Check Pricing Plan Limits
                increaseUsedStorage(owner, mediaById);
            }

            mediaById.setDeleteTime(currentTime);
        }
    }

    /**
     * Deletes permanent an media from all Datasets and from the database
     */
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public void permanentDeleteMediaFile(String mediaId) {
        DataGymSecurity.isAuthenticated();

        Media mediaById = getMediaById(mediaId);
        permanentDeleteMediaFile(mediaById, false);
    }

    /**
     * Deletes permanent an media from all Datasets and from the database
     */
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public void permanentDeleteMediaFile(Media media, boolean isCronJob) {
        // Permissions check
        String owner = isCurrentUserPermittedToManipulateMedia(media, isCronJob);
        if (!isCronJob) {
            DataGymSecurity.isRoot(owner, false);
        }
        boolean mediaFromDummyProject = isMediaFromDummyProject(media);
        boolean deleted = media.isDeleted();

        if (media instanceof LocalImage) {
            LocalImage localImage = (LocalImage) media;
            BinFileEntity binFileEntity = localImage.getBinFileEntity();

            // Check Pricing Plan Limits
            if (!deleted && !mediaFromDummyProject) {
                decreaseUsedStorage(owner, localImage);
            }

            binFileService.delete(binFileEntity);
        } else if (media instanceof AwsS3Video) {
            AwsS3Video awsS3video = (AwsS3Video) media;
            awsS3HelperService.orElseThrow(() -> new ServiceUnavailableException("aws")).permanentDeleteAwsS3Object(awsS3video.getAwsKey());

            // Check Pricing Plan Limits
            if (!deleted && !mediaFromDummyProject) {
                decreaseUsedStorage(owner, awsS3video);
            }
        }

        media.getDatasets().forEach(dataset -> dataset.getMedia().remove(media));

        String mediaId = media.getId();

        mediaRepository.delete(media);

        String deleteMessage = "Deleted Permanent an Image with mediaId : {}";
        if (isCronJob) {
            deleteMessage = "MediaScheduleJob " + deleteMessage;
        }

        LOGGER.info(deleteMessage, mediaId);
    }

    /**
     * Check Pricing Plan Limits - Increase Used Storage
     */
    private void increaseUsedStorage(String owner, Media media) {
        // Check if Image is from Dummy_Dataset_One or Dummy_Dataset_Two
        boolean imageFromDummyProject = isMediaFromDummyProject(media);

        if (!imageFromDummyProject && media instanceof LocalImage) {
            LocalImage localImage = (LocalImage) media;

            long imageSizeBytes = localImage.getBinFileEntity().getSize();
            long imageSizeKB = (long) (Math.ceil(imageSizeBytes * 0.001));
            limitService.increaseUsedStorage(owner, imageSizeKB);

            // Metrics update - Increase Used Storage
            changeUsedStorage(owner, imageSizeKB);
        }
    }

    /**
     * Check Pricing Plan Limits - Decrease Used Storage
     */
    private void decreaseUsedStorage(String owner, Media media) {
        // Check if Image is from Dummy_Dataset_One or Dummy_Dataset_Two
        boolean imageFromDummyProject = isMediaFromDummyProject(media);

        // Check Pricing Plan Limits, if Image is not from Dummy_Dataset_One or Dummy_Dataset_Two
        if (!imageFromDummyProject && (media instanceof LocalImage)) {
            LocalImage localImage = (LocalImage) media;

            long imageSizeBytes = localImage.getBinFileEntity().getSize();
            long imageSizeKB = (long) (Math.ceil(imageSizeBytes * 0.001));
            limitService.decreaseUsedStorage(owner, imageSizeKB);

            // Metrics update - Decrease Used Storage
            changeUsedStorage(owner, imageSizeKB * -1);
        } else if (!imageFromDummyProject && (media instanceof AwsS3Video)) {
            AwsS3Video localImage = (AwsS3Video) media;

            long imageSizeBytes = localImage.getSize();
            long imageSizeKB = (long) (Math.ceil(imageSizeBytes * 0.001));
            limitService.decreaseUsedStorage(owner, imageSizeKB);

            // Metrics update - Decrease Used Storage
            changeUsedStorage(owner, imageSizeKB * -1);
        }
    }

    // Metrics update - change Used Storage
    private void changeUsedStorage(String organisationId, long imageSizeKB) {
        String loggedInUserId = DataGymSecurity.getLoggedInUserId();

        Metrics.summary("datagym.images.size.sum", "labeler",
                        loggedInUserId, "organisation", organisationId)
                .record(imageSizeKB);

    }

    /**
     * Check if media is from Dummy_Project
     */
    private boolean isMediaFromDummyProject(Media media) {
        // Check if media is from Dummy_Dataset_One or Dummy_Dataset_Two
        return media
                .getDatasets()
                .stream()
                .anyMatch(dataset ->
                        "Dummy_Dataset_One".equals(dataset.getName()) || "Dummy_Dataset_Two".equals(dataset.getName()));
    }

    private Media getMediaById(String mediaId) {
        return mediaRepository.findById(mediaId)
                .orElseThrow(() -> new NotFoundException(MEDIA_ENTITY_NAME, "id", "" + mediaId));
    }

    private List<Media> getAllMediaById(Set<String> mediaIdSet) {
        List<Media> allMediaById = mediaRepository.findAllById(mediaIdSet);

        if (mediaIdSet.size() != allMediaById.size()) {
            throw new GenericException("invalid_imageId_in_list", null, null);
        }

        return allMediaById;
    }

    /**
     * Iterates over all datasets, in which the current image is part of,
     * and compares the organisations of the current logged in user with
     * the organisation of the current dataset.
     *
     * @param mediaById - Instance of {@link Media}
     * @param isCronJob - used from the Cronjob to delete Images without OauthUser-permissions
     * @return the Organisation-ID if there is a match between both organisations
     * @throws ForbiddenException when after the end of the iteration over all image-datasets isn't any match
     */
    private String isCurrentUserPermittedToManipulateMedia(Media mediaById, boolean isCronJob) {
        OauthUser oauthUser = SecurityContext.get();

        Set<Dataset> datasets = mediaById.getDatasets();

        for (Dataset dataset : datasets) {
            String owner = dataset.getOwner();

            if (isCronJob || (oauthUser != null && oauthUser.orgs().containsKey(owner) || (oauthUser != null && oauthUser.scopes().contains(
                    SUPER_ADMIN_SCOPE_TYPE)))) {
                return owner;
            }
        }

        throw new ForbiddenException();
    }

    private Optional<DatasetAwsS3UserCredentials> getAwsS3CredentialsByDatasetId(String datasetId) {
        return awsS3UserCredentialsRepository
                .findAwsS3UserCredentialsByDatasetId(datasetId);
    }


    // Start time: every 30 seconds
//    @Scheduled(cron = "*/30 * * * * *")

    // Start time: every day at 02:00 AM
    @Scheduled(cron = "0 0 2 * * * ")
    public void mediaScheduleJob() {
        LocalDateTime localDateTime = LocalDateTime.now();

        // minus * days
        localDateTime = localDateTime.minusDays(TIME_BEFORE_DELETE_MEDIA);

        // convert LocalDateTime to date
        long timeBeforeToDeleteMedia = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String formatLocalDateTime = dateFormat8.format(localDateTime);

        mediaRepository.findAllMediaByDeleteTimeBefore(timeBeforeToDeleteMedia)
                .forEach(media -> permanentDeleteMediaFile(media, true));

        LOGGER.info("Deleted all media with delete_time before : {}", formatLocalDateTime);
    }
}

