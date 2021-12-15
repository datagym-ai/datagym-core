package ai.datagym.application.dataset.service.awsS3;

import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateBindingModel;
import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateBucketBindingModel;
import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateKeysBindingModel;
import ai.datagym.application.dataset.models.awsS3.viewModels.AwsS3CredentialViewModel;

public interface AwsS3UserCredentialsService {

    AwsS3CredentialViewModel getAwsS3Credentials(String datasetId);

    AwsS3CredentialViewModel updateAwsS3Credentials(String datasetId,
                                                    AwsS3CredentialsUpdateBindingModel awsS3CredentialsUpdateBindingModel);

    AwsS3CredentialViewModel updateAwsS3Keys(String datasetId,
                                             AwsS3CredentialsUpdateKeysBindingModel awsS3CredentialsUpdateKeysBindingModel);

    AwsS3CredentialViewModel updateAwsS3Bucket(String datasetId,
                                               AwsS3CredentialsUpdateBucketBindingModel awsS3CredentialsUpdateBucketBindingModel);
}
