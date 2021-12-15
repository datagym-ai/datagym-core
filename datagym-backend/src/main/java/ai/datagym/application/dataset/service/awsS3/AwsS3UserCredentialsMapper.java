package ai.datagym.application.dataset.service.awsS3;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.entity.DatasetAwsS3UserCredentials;
import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateBindingModel;
import ai.datagym.application.dataset.models.awsS3.viewModels.AwsS3CredentialViewModel;


public final class AwsS3UserCredentialsMapper {
    private AwsS3UserCredentialsMapper() {
    }

    public static DatasetAwsS3UserCredentials mapToAwsS3Credentials(AwsS3CredentialsUpdateBindingModel from,
                                                                    Dataset dataset) {
        DatasetAwsS3UserCredentials to = new DatasetAwsS3UserCredentials();

        to.setName(from.getName());
        to.setSecretKey(from.getSecretKey());
        to.setAccessKey(from.getAccessKey());
        to.setLocationPath(from.getLocationPath());
        to.setBucketName(from.getBucketName());
        to.setBucketRegion(from.getBucketRegion());
        to.setDataset(dataset);

        return to;
    }

    public static DatasetAwsS3UserCredentials updateAwsS3Credentials(AwsS3CredentialsUpdateBindingModel from,
                                                                     DatasetAwsS3UserCredentials current) {
        current.setName(from.getName());
        current.setSecretKey(from.getSecretKey());
        current.setAccessKey(from.getAccessKey());
        current.setLocationPath(from.getLocationPath());
        current.setBucketName(from.getBucketName());
        current.setBucketRegion(from.getBucketRegion());

        return current;
    }

    public static AwsS3CredentialViewModel mapToAwsS3CredentialViewModel(DatasetAwsS3UserCredentials from) {
        AwsS3CredentialViewModel to = new AwsS3CredentialViewModel();

        String secretKeyStars = "*".repeat(from.getSecretKey().length());

        to.setId(from.getId());
        to.setName(from.getName());
        to.setLocationPath(from.getLocationPath());
        to.setBucketName(from.getBucketName());
        to.setBucketRegion(from.getBucketRegion());
        to.setLastError(from.getLastError());
        to.setLastErrorTimeStamp(from.getLastErrorTimeStamp());
        to.setLastSynchronized(from.getLastSynchronized());
        to.setDatasetId(from.getDataset().getId());
        to.setAccessKey(from.getAccessKey());
        to.setSecretKey(secretKeyStars);

        return to;
    }
}
