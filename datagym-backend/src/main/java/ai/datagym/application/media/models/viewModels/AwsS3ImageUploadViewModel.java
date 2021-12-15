package ai.datagym.application.media.models.viewModels;

public class AwsS3ImageUploadViewModel {
    private String awsKey;
    private String lastError;
    private Long lastErrorTimeStamp;
    private String mediaUploadStatus;

    public AwsS3ImageUploadViewModel() {
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

    public String getMediaUploadStatus() {
        return mediaUploadStatus;
    }

    public void setMediaUploadStatus(String mediaUploadStatus) {
        this.mediaUploadStatus = mediaUploadStatus;
    }
}
