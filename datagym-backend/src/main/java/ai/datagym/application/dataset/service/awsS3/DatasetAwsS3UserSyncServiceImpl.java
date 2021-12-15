package ai.datagym.application.dataset.service.awsS3;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.entity.DatasetAwsS3UserCredentials;
import ai.datagym.application.dataset.models.awsS3.viewModels.AwsS3SyncViewModel;
import ai.datagym.application.dataset.repo.AwsS3UserCredentialsRepository;
import ai.datagym.application.dataset.repo.DatasetRepository;
import ai.datagym.application.limit.entity.DataGymPlan;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.media.entity.AwsS3Image;
import ai.datagym.application.media.entity.InvalidMediaReason;
import ai.datagym.application.media.entity.MediaSourceType;
import ai.datagym.application.media.entity.MediaUploadStatus;
import ai.datagym.application.media.models.viewModels.AwsS3ImageUploadViewModel;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.media.service.MediaService;
import ai.datagym.application.project.entity.MediaType;
import ai.datagym.application.security.util.DataGymSecurity;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ai.datagym.application.utils.constants.CommonMessages.OAUTH_SCOPE_TYPE;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class DatasetAwsS3UserSyncServiceImpl implements DatasetAwsS3UserSyncService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetAwsS3UserSyncServiceImpl.class);

    private final MediaRepository mediaRepository;
    private final MediaService mediaService;
    private final AwsS3UserCredentialsRepository awsS3UserCredentialsRepository;
    private final LimitService limitService;
    private final DatasetRepository datasetRepository;
    private final AwsS3UserBatchService awsS3UserBatchService;

    public DatasetAwsS3UserSyncServiceImpl(MediaRepository mediaRepository,
                                           MediaService mediaService,
                                           AwsS3UserCredentialsRepository awsS3UserCredentialsRepository,
                                           LimitService limitService,
                                           DatasetRepository datasetRepository,
                                           AwsS3UserBatchService awsS3UserBatchService) {
        this.mediaRepository = mediaRepository;
        this.mediaService = mediaService;
        this.awsS3UserCredentialsRepository = awsS3UserCredentialsRepository;
        this.limitService = limitService;
        this.datasetRepository = datasetRepository;
        this.awsS3UserBatchService = awsS3UserBatchService;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public AwsS3SyncViewModel syncDatasetWithAws(String datasetId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        Dataset datasetById = getDatasetById(datasetId);

        //Permissions check
        String owner = datasetById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        if (datasetById.getMediaType() == MediaType.VIDEO) {
            throw new GenericException("aws_upload_unsupported_mediatype", null, null);
        }

        // Get Pricing Plan Type for the current Organisation
        String pricingPlanType = limitService.getLimitsByOrgId(owner).getPricingPlanType();

        if (pricingPlanType.equals(DataGymPlan.FREE_DEVELOPER.name())) {
            throw new GenericException("aws_upload_not_allowed", null, null);
        }

        //Get AwsS3Credentials from the Data Base
        DatasetAwsS3UserCredentials datasetAwsS3UserCredentials = getAwsS3CredentialsByDatasetId(datasetId)
                .orElseThrow(() -> new GenericException("not_found_aws_credentials", null, null, datasetId));

        AwsS3SyncViewModel credentialSyncViewModel = new AwsS3SyncViewModel();

        String bucketName = datasetAwsS3UserCredentials.getBucketName();
        String accessKey = datasetAwsS3UserCredentials.getAccessKey();
        String secretKey = datasetAwsS3UserCredentials.getSecretKey();
        String bucketRegion = datasetAwsS3UserCredentials.getBucketRegion();
        String locationPath = datasetAwsS3UserCredentials.getLocationPath();

        try {
            // Create Aws Credentials Instance
            AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

            AmazonS3 amazonS3client = AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(Regions.valueOf(bucketRegion))
                    .build();


            ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                    .withBucketName(bucketName)
                    .withPrefix(locationPath);
            ObjectListing objectListing = null;

            // Todo refactor that there is no awss3image instance -> only the keys needed for futher processing
            // Get All awsS3Images from the current Dataset
            Map<String, AwsS3Image> localAwsS3ImagesKeyMap = mediaRepository
                    .findAllByDatasetsIdAndMediaSourceTypeAndDeletedIsFalse(datasetId, MediaSourceType.AWS_S3)
                    .stream()
                    .map(image -> (AwsS3Image) image)
                    .collect(Collectors.toMap(AwsS3Image::getAwsKey, Function.identity()));

            // Fetch all elements (aws delivers 1000 per iteration)
            do {
                if (objectListing != null) {
                    objectListing = amazonS3client.listNextBatchOfObjects(objectListing);
                } else {
                    objectListing = amazonS3client.listObjects(listObjectsRequest);
                }

                // Get all Object from the current bucket. Filter out all Folders(Objects with "key" that ends with "/")
                List<S3ObjectSummary> collect = objectListing.getObjectSummaries().stream()
                        .filter(s3ObjectSummary -> !s3ObjectSummary.getKey().endsWith("/"))
                        .filter(s3ObjectSummary -> filterAndHandleGlacierStorageType(credentialSyncViewModel, s3ObjectSummary))
                        .filter(s3ObjectSummary -> {
                            if (localAwsS3ImagesKeyMap.get(s3ObjectSummary.getKey()) == null) {
                                return true;
                            } else {
                                localAwsS3ImagesKeyMap.remove(s3ObjectSummary.getKey());
                                return false;
                            }
                        })
                        .collect(Collectors.toList());

                // Iterate over all Files and try to save them as AwsS3Image
                // Self autowire
                awsS3UserBatchService.batchCreateAwsS3Images(
                        collect,
                        credentialSyncViewModel,
                        datasetById.getId());
            } while (objectListing.isTruncated());


            // Check for removed Files from the current Aws-S3-Bucket
            checkIfAnyAwsS3ImagesAreDeletedFromTheBucket(localAwsS3ImagesKeyMap, credentialSyncViewModel);

            long currentTimeMillis = System.currentTimeMillis();
            credentialSyncViewModel.setLastSynchronized(System.currentTimeMillis());
            // Update the AwsS3Credentials entity
            datasetAwsS3UserCredentials.setLastSynchronized(currentTimeMillis);
            datasetAwsS3UserCredentials.setLastError(credentialSyncViewModel.getLastError());
            datasetAwsS3UserCredentials.setLastErrorTimeStamp(credentialSyncViewModel.getLastErrorTimeStamp());

        } catch (AmazonServiceException ase) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            String errorMessage = ase.getErrorMessage();

            credentialSyncViewModel.setSyncError(errorMessage);
            credentialSyncViewModel.setLastErrorTimeStamp(System.currentTimeMillis());
            LOGGER.error("Error by synchronizing aws s3 media", ase);

        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            String errorMessage = e.getMessage();

            credentialSyncViewModel.setSyncError(errorMessage);
            credentialSyncViewModel.setLastErrorTimeStamp(System.currentTimeMillis());
            LOGGER.error("Error by synchronizing aws s3 media", e);
        }

        return credentialSyncViewModel;
    }

    /**
     * We do not support the glacier storage type.
     *
     * @param credentialSyncViewModel
     * @param s
     * @return
     */
    private boolean filterAndHandleGlacierStorageType(AwsS3SyncViewModel credentialSyncViewModel, S3ObjectSummary s) {
        if (s.getStorageClass().equals(StorageClass.Glacier.toString())) {
            AwsS3ImageUploadViewModel awsS3ImageUploadViewModel = new AwsS3ImageUploadViewModel();

            awsS3ImageUploadViewModel.setAwsKey(s.getKey());
            awsS3ImageUploadViewModel.setMediaUploadStatus(MediaUploadStatus.FAILED.name());
            awsS3ImageUploadViewModel.setLastErrorTimeStamp(System.currentTimeMillis());
            awsS3ImageUploadViewModel.setLastError(InvalidMediaReason.UNSUPPORTED_AWS_S3_STORAGE_CLASS.name());

            credentialSyncViewModel.getUploadFailedS3Images().add(awsS3ImageUploadViewModel);
            credentialSyncViewModel.setLastError(awsS3ImageUploadViewModel.getLastError());
            credentialSyncViewModel.setLastErrorTimeStamp(awsS3ImageUploadViewModel.getLastErrorTimeStamp());
            return false;
        }
        return true;
    }


    /**
     * Get All Deleted Files and save them in the AwsS3CredentialSyncViewModel
     */
    private void checkIfAnyAwsS3ImagesAreDeletedFromTheBucket(Map<String, AwsS3Image> localAwsS3ImagesKeyMap,
                                                              AwsS3SyncViewModel credentialSyncViewModel) {

        for (AwsS3Image currentAwsS3Image : localAwsS3ImagesKeyMap.values()) {
            // Set deleted = true for the current AwsS3Image
            String imageId = currentAwsS3Image.getId();
            mediaService.deleteMediaFile(imageId, true);

            AwsS3ImageUploadViewModel awsS3ImageUploadViewModel = new AwsS3ImageUploadViewModel();
            String awsKey = currentAwsS3Image.getAwsKey();

            awsS3ImageUploadViewModel.setAwsKey(awsKey);
            awsS3ImageUploadViewModel.setMediaUploadStatus(MediaUploadStatus.DELETED.name());

            credentialSyncViewModel.getDeletedS3Images().add(awsS3ImageUploadViewModel);
        }
    }

    private Optional<DatasetAwsS3UserCredentials> getAwsS3CredentialsByDatasetId(String datasetId) {
        return awsS3UserCredentialsRepository
                .findAwsS3UserCredentialsByDatasetId(datasetId);
    }


    private Dataset getDatasetById(String datasetId) {
        return datasetRepository
                .findById(datasetId)
                .orElseThrow(() -> new NotFoundException("dataset", "id", "" + datasetId));
    }
}
