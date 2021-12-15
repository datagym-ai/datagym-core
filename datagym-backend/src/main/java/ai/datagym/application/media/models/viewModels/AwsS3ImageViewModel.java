package ai.datagym.application.media.models.viewModels;

import ai.datagym.application.media.entity.InvalidMediaReason;

public class AwsS3ImageViewModel extends AwsS3MediaViewModel {

    public AwsS3ImageViewModel(String id,
                               Long timestamp,
                               String mediaSourceType,
                               String mediaName,
                               boolean valid,
                               InvalidMediaReason reason,
                               String awsKey,
                               String lastError,
                               Long lastErrorTimeStamp) {
        super(id, timestamp, mediaSourceType, mediaName, valid, reason, awsKey, lastError, lastErrorTimeStamp);
    }
}
