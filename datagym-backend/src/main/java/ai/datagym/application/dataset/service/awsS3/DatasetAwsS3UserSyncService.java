package ai.datagym.application.dataset.service.awsS3;

import ai.datagym.application.dataset.models.awsS3.viewModels.AwsS3SyncViewModel;

public interface DatasetAwsS3UserSyncService {
    AwsS3SyncViewModel syncDatasetWithAws(String datasetId);
}
