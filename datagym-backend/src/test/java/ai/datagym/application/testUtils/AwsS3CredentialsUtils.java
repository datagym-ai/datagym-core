package ai.datagym.application.testUtils;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.entity.DatasetAwsS3UserCredentials;
import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateBindingModel;
import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateBucketBindingModel;
import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateKeysBindingModel;
import ai.datagym.application.dataset.models.awsS3.viewModels.AwsS3CredentialViewModel;
import ai.datagym.application.dataset.models.awsS3.viewModels.AwsS3SyncViewModel;
import ai.datagym.application.media.entity.MediaUploadStatus;
import ai.datagym.application.media.models.viewModels.AwsS3ImageUploadViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ai.datagym.application.testUtils.DatasetUtils.DATASET_ID;

public class AwsS3CredentialsUtils {
    public static final String AWS_S3_CREDENTIALS_ID = "TestId " + UUID.randomUUID();
    public static final String AWS_S3_CREDENTIALS_NAME = "TestName " + UUID.randomUUID();
    public static final String AWS_S3_BUCKET_REGION = "EU_CENTRAL_1";
    public static final String AWS_S3_ACCESS_KEY= "AWS_S3_ACCESS_KEY";
    public static final String AWS_S3_SECRET_KEY= "AWS_S3_SECRET_KEY";
    public static final String AWS_S3_AWS_KEY= "aws_key";

    private static final Long TIME = System.currentTimeMillis();

    public static DatasetAwsS3UserCredentials createTestAwsS3Credentials() {
        Dataset testDataset = DatasetUtils.createTestDataset(DATASET_ID);

        return new DatasetAwsS3UserCredentials() {{
            setId(AWS_S3_CREDENTIALS_ID);
            setName(AWS_S3_CREDENTIALS_NAME);
            setLocationPath("");
            setBucketName("dataGym-test-bucket");
            setBucketRegion(AWS_S3_BUCKET_REGION);
            setLastError(null);
            setLastErrorTimeStamp(null);
            setLastSynchronized(null);
            setAccessKey(AWS_S3_ACCESS_KEY);
            setSecretKey(AWS_S3_SECRET_KEY);
            setAwsS3Images(new ArrayList<>());
            setDataset(testDataset);
        }};
    }

    public static AwsS3CredentialViewModel createTestAwsS3CredentialViewModel() {
        return new AwsS3CredentialViewModel() {{
            setId(AWS_S3_CREDENTIALS_ID);
            setName(AWS_S3_CREDENTIALS_NAME);
            setLocationPath("");
            setBucketName("dataGym-test-bucket");
            setBucketRegion(AWS_S3_BUCKET_REGION);
            setLastError(null);
            setLastErrorTimeStamp(null);
            setLastSynchronized(null);
            setDatasetId(DATASET_ID);
            setAccessKey(AWS_S3_ACCESS_KEY);
        }};
    }

    public static AwsS3CredentialsUpdateBindingModel createTestAwsS3CredentialsUpdateBindingModel() {
        return new AwsS3CredentialsUpdateBindingModel() {{
            setAccessKey(AWS_S3_ACCESS_KEY);
            setSecretKey(AWS_S3_SECRET_KEY);
            setName(AWS_S3_CREDENTIALS_NAME);
            setLocationPath("");
            setBucketName("dataGym-test-bucket");
            setBucketRegion(AWS_S3_BUCKET_REGION);
        }};
    }

    public static AwsS3CredentialsUpdateKeysBindingModel createTestAwsS3CredentialsUpdateKeysBindingModel() {
        return new AwsS3CredentialsUpdateKeysBindingModel() {{
            setAccessKey(AWS_S3_ACCESS_KEY);
            setSecretKey(AWS_S3_SECRET_KEY);
        }};
    }


    public static AwsS3CredentialsUpdateBucketBindingModel createTestAwsS3CredentialsUpdateBucketBindingModel() {
        return new AwsS3CredentialsUpdateBucketBindingModel() {{
            setName(AWS_S3_CREDENTIALS_NAME);
            setLocationPath("");
            setBucketName("dataGym-test-bucket");
            setBucketRegion(AWS_S3_BUCKET_REGION);
        }};
    }

    public static AwsS3SyncViewModel createTestAwsS3CredentialSyncViewModel() {
        return new AwsS3SyncViewModel() {{
            setAddedS3Images(new ArrayList<>());
            setDeletedS3Images(new ArrayList<>());
            setUploadFailedS3Images(new ArrayList<>());
            setLastError(null);
            setLastErrorTimeStamp(null);
            setLastSynchronized(null);
            setSyncError(null);
        }};
    }

    public static List<AwsS3ImageUploadViewModel> createTestAwsS3CredentialSyncViewModels(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new AwsS3ImageUploadViewModel() {{
                    setAwsKey(AWS_S3_AWS_KEY + count + ".jpg");
                    setLastError(null);
                    setLastErrorTimeStamp(null);
                    setMediaUploadStatus(MediaUploadStatus.SUCCESS.name());
                }})
                .collect(Collectors.toList());
    }
}
