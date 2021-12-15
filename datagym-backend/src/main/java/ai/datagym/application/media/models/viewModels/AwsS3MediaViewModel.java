package ai.datagym.application.media.models.viewModels;

import ai.datagym.application.media.entity.InvalidMediaReason;

public abstract class AwsS3MediaViewModel extends MediaViewModel {
    private String awsKey;
    private String lastError;
    private Long lastErrorTimeStamp;

    public AwsS3MediaViewModel() {
    }

    public AwsS3MediaViewModel(String id, Long timestamp, String mediaSourceType, String mediaName, boolean valid,
                               InvalidMediaReason reason) {
        super(id, timestamp, mediaSourceType, mediaName, valid, reason);
    }

    public AwsS3MediaViewModel(String id, Long timestamp, String mediaSourceType, String mediaName, boolean valid,
                               InvalidMediaReason reason, String awsKey, String lastError,
                               Long lastErrorTimeStamp) {
        super(id, timestamp, mediaSourceType, mediaName, valid, reason);
        this.awsKey = awsKey;
        this.lastError = lastError;
        this.lastErrorTimeStamp = lastErrorTimeStamp;
    }

    public String getAwsKey() {
        return awsKey;
    }

    public void setAwsKey(String awsKey) {
        this.awsKey = awsKey;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Long getLastErrorTimeStamp() {
        return lastErrorTimeStamp;
    }

    public void setLastErrorTimeStamp(Long lastErrorTimeStamp) {
        this.lastErrorTimeStamp = lastErrorTimeStamp;
    }
}
