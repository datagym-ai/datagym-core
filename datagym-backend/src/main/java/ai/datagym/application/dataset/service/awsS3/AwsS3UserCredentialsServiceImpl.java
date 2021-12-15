package ai.datagym.application.dataset.service.awsS3;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.entity.DatasetAwsS3UserCredentials;
import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateBindingModel;
import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateBucketBindingModel;
import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateKeysBindingModel;
import ai.datagym.application.dataset.models.awsS3.viewModels.AwsS3CredentialViewModel;
import ai.datagym.application.dataset.repo.AwsS3UserCredentialsRepository;
import ai.datagym.application.dataset.repo.DatasetRepository;
import ai.datagym.application.limit.entity.DataGymPlan;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.security.util.DataGymSecurity;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static ai.datagym.application.utils.constants.CommonMessages.OAUTH_SCOPE_TYPE;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class AwsS3UserCredentialsServiceImpl implements AwsS3UserCredentialsService {
    private final String NOT_FOUND_AWS_CREDENTIALS = "not_found_aws_credentials";

    private final AwsS3UserCredentialsRepository awsS3UserCredentialsRepository;
    private final LimitService limitService;
    private final DatasetRepository datasetRepository;


    @Autowired
    public AwsS3UserCredentialsServiceImpl(AwsS3UserCredentialsRepository awsS3UserCredentialsRepository,
                                           LimitService limitService,
                                           DatasetRepository datasetRepository) {
        this.awsS3UserCredentialsRepository = awsS3UserCredentialsRepository;
        this.limitService = limitService;
        this.datasetRepository = datasetRepository;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public AwsS3CredentialViewModel getAwsS3Credentials(String datasetId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        Dataset datasetById = getDatasetById(datasetId);

        //Permissions check
        String owner = datasetById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        // Get Pricing Plan Type for the current Organisation
        String pricingPlanType = limitService.getLimitsByOrgId(owner).getPricingPlanType();

        if (pricingPlanType.equals(DataGymPlan.FREE_DEVELOPER.name())) {
            throw new GenericException("aws_upload_not_allowed", null, null);
        }

        Optional<DatasetAwsS3UserCredentials> awsS3Credentials = this.getAwsS3CredentialsByDatasetId(datasetId);

        if (awsS3Credentials.isEmpty()) {
            return new AwsS3CredentialViewModel();
        }

        return AwsS3UserCredentialsMapper.mapToAwsS3CredentialViewModel(awsS3Credentials.get());
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public AwsS3CredentialViewModel updateAwsS3Credentials(String datasetId, AwsS3CredentialsUpdateBindingModel awsS3CredentialsUpdateBindingModel) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        Dataset datasetById = getDatasetById(datasetId);

        //Permissions check
        String owner = datasetById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        // Get Pricing Plan Type for the current Organisation
        String pricingPlanType = limitService.getLimitsByOrgId(owner).getPricingPlanType();

        if (pricingPlanType.equals(DataGymPlan.FREE_DEVELOPER.name())) {
            throw new GenericException("aws_upload_not_allowed", null, null);
        }

        Optional<DatasetAwsS3UserCredentials> awsS3CredentialsOptional = this.getAwsS3CredentialsByDatasetId(datasetId);

        if (awsS3CredentialsOptional.isEmpty()) {
            DatasetAwsS3UserCredentials datasetAwsS3UserCredentials = this.createAwsS3Credentials(datasetById,
                                                                                                  awsS3CredentialsUpdateBindingModel);

            DatasetAwsS3UserCredentials savedDatasetAwsS3UserCredentials = awsS3UserCredentialsRepository.save(
                    datasetAwsS3UserCredentials);

            return AwsS3UserCredentialsMapper.mapToAwsS3CredentialViewModel(savedDatasetAwsS3UserCredentials);
        }

        DatasetAwsS3UserCredentials credentials = awsS3CredentialsOptional.get();
        AwsS3UserCredentialsMapper.updateAwsS3Credentials(awsS3CredentialsUpdateBindingModel, credentials);

        return AwsS3UserCredentialsMapper.mapToAwsS3CredentialViewModel(credentials);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public AwsS3CredentialViewModel updateAwsS3Keys(String datasetId, AwsS3CredentialsUpdateKeysBindingModel awsS3CredentialsUpdateKeysBindingModel) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        Dataset datasetById = getDatasetById(datasetId);

        //Permissions check
        String owner = datasetById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        // Get Pricing Plan Type for the current Organisation
        String pricingPlanType = limitService.getLimitsByOrgId(owner).getPricingPlanType();

        if (pricingPlanType.equals(DataGymPlan.FREE_DEVELOPER.name())) {
            throw new GenericException("aws_upload_not_allowed", null, null);
        }

        Optional<DatasetAwsS3UserCredentials> awsS3CredentialsOptional = this.getAwsS3CredentialsByDatasetId(datasetId);

        if (awsS3CredentialsOptional.isEmpty()) {
            throw new GenericException(NOT_FOUND_AWS_CREDENTIALS, null, null, datasetId);
        }

        DatasetAwsS3UserCredentials credentials = awsS3CredentialsOptional.get();

        String accessKey = awsS3CredentialsUpdateKeysBindingModel.getAccessKey();
        String secretKey = awsS3CredentialsUpdateKeysBindingModel.getSecretKey();

        credentials.setAccessKey(accessKey);
        credentials.setSecretKey(secretKey);

        DatasetAwsS3UserCredentials savedDatasetAwsS3UserCredentials = awsS3UserCredentialsRepository.save(credentials);

        return AwsS3UserCredentialsMapper.mapToAwsS3CredentialViewModel(savedDatasetAwsS3UserCredentials);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public AwsS3CredentialViewModel updateAwsS3Bucket(String datasetId, AwsS3CredentialsUpdateBucketBindingModel awsS3CredentialsUpdateBucketBindingModel) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        Dataset datasetById = getDatasetById(datasetId);

        //Permissions check
        String owner = datasetById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        // Get Pricing Plan Type for the current Organisation
        String pricingPlanType = limitService.getLimitsByOrgId(owner).getPricingPlanType();

        if (pricingPlanType.equals(DataGymPlan.FREE_DEVELOPER.name())) {
            throw new GenericException("aws_upload_not_allowed", null, null);
        }

        Optional<DatasetAwsS3UserCredentials> awsS3CredentialsOptional = this.getAwsS3CredentialsByDatasetId(datasetId);

        if (awsS3CredentialsOptional.isEmpty()) {
            throw new GenericException(NOT_FOUND_AWS_CREDENTIALS, null, null, datasetId);
        }

        DatasetAwsS3UserCredentials credentials = awsS3CredentialsOptional.get();

        String bucketName = awsS3CredentialsUpdateBucketBindingModel.getBucketName();
        String bucketRegion = awsS3CredentialsUpdateBucketBindingModel.getBucketRegion();
        String name = awsS3CredentialsUpdateBucketBindingModel.getName();
        String locationPath = awsS3CredentialsUpdateBucketBindingModel.getLocationPath();

        credentials.setBucketName(bucketName);
        credentials.setBucketRegion(bucketRegion);
        credentials.setName(name);
        credentials.setLocationPath(locationPath);

        DatasetAwsS3UserCredentials savedDatasetAwsS3UserCredentials = awsS3UserCredentialsRepository.save(credentials);

        return AwsS3UserCredentialsMapper.mapToAwsS3CredentialViewModel(savedDatasetAwsS3UserCredentials);
    }

    private DatasetAwsS3UserCredentials createAwsS3Credentials(Dataset dataset,
                                                               AwsS3CredentialsUpdateBindingModel awsS3CredentialsUpdateBindingModel) {
        return AwsS3UserCredentialsMapper.mapToAwsS3Credentials(awsS3CredentialsUpdateBindingModel, dataset);
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
