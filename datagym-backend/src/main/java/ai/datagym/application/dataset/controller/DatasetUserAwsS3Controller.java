package ai.datagym.application.dataset.controller;


import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateBindingModel;
import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateBucketBindingModel;
import ai.datagym.application.dataset.models.awsS3.bindingModels.AwsS3CredentialsUpdateKeysBindingModel;
import ai.datagym.application.dataset.models.awsS3.viewModels.AwsS3CredentialViewModel;
import ai.datagym.application.dataset.models.awsS3.viewModels.AwsS3SyncViewModel;
import ai.datagym.application.dataset.service.awsS3.AwsS3UserCredentialsService;
import ai.datagym.application.dataset.service.awsS3.DatasetAwsS3UserSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/dataset")
@Validated
public class DatasetUserAwsS3Controller {
    private final AwsS3UserCredentialsService awsS3UserCredentialsService;
    private final DatasetAwsS3UserSyncService datasetAwsS3UserSyncService;

    @Autowired
    public DatasetUserAwsS3Controller(AwsS3UserCredentialsService awsS3UserCredentialsService,
                                      DatasetAwsS3UserSyncService datasetAwsS3UserSyncService) {
        this.awsS3UserCredentialsService = awsS3UserCredentialsService;
        this.datasetAwsS3UserSyncService = datasetAwsS3UserSyncService;
    }

    @GetMapping("/{datasetId}/aws")
    public AwsS3CredentialViewModel getAwsS3CredentialsByDatasetId(@PathVariable("datasetId") String datasetId) {
        return awsS3UserCredentialsService.getAwsS3Credentials(datasetId);
    }

    @PostMapping("/{datasetId}/aws")
    public AwsS3CredentialViewModel createOrUpdateAwsS3Credentials(@PathVariable("datasetId") String datasetId,
                                                           @RequestBody @Valid AwsS3CredentialsUpdateBindingModel awsS3CredentialsUpdateBindingModel) {
        return awsS3UserCredentialsService.updateAwsS3Credentials(datasetId, awsS3CredentialsUpdateBindingModel);
    }

    @PutMapping("/{datasetId}/aws/keys")
    public AwsS3CredentialViewModel updateAwsS3Keys(@PathVariable("datasetId") String datasetId,
                                                           @RequestBody @Valid AwsS3CredentialsUpdateKeysBindingModel awsS3CredentialsUpdateKeysBindingModel) {
        return awsS3UserCredentialsService.updateAwsS3Keys(datasetId, awsS3CredentialsUpdateKeysBindingModel);
    }

    @PutMapping("/{datasetId}/aws/bucket")
    public AwsS3CredentialViewModel updateAwsS3Bucket(@PathVariable("datasetId") String datasetId,
                                                    @RequestBody @Valid AwsS3CredentialsUpdateBucketBindingModel awsS3CredentialsUpdateBucketBindingModel) {
        return awsS3UserCredentialsService.updateAwsS3Bucket(datasetId, awsS3CredentialsUpdateBucketBindingModel);
    }

    @PostMapping("/{datasetId}/aws/sync")
    public AwsS3SyncViewModel syncDatasetWithAws(@PathVariable("datasetId") String datasetId) {
        return datasetAwsS3UserSyncService.syncDatasetWithAws(datasetId);
    }
}
