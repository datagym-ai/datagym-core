package ai.datagym.application.dataset.models.awsS3.viewModels;

import ai.datagym.application.media.models.viewModels.AwsS3ImageUploadViewModel;

import java.util.ArrayList;
import java.util.List;

public class AwsS3SyncViewModel {
    private List<AwsS3ImageUploadViewModel> addedS3Images;
    private List<AwsS3ImageUploadViewModel> deletedS3Images;
    private List<AwsS3ImageUploadViewModel> uploadFailedS3Images;
    private String syncError;
    private String lastError;
    private Long lastErrorTimeStamp;
    private Long lastSynchronized;

    public AwsS3SyncViewModel() {
        this.addedS3Images = new ArrayList<>();
        this.deletedS3Images = new ArrayList<>();
        this.uploadFailedS3Images = new ArrayList<>();
    }

    public String getSyncError() {
        return syncError;
    }

    public void setSyncError(String syncError) {
        this.syncError = syncError;
    }

    public List<AwsS3ImageUploadViewModel> getAddedS3Images() {
        return addedS3Images;
    }

    public void setAddedS3Images(List<AwsS3ImageUploadViewModel> addedS3Images) {
        this.addedS3Images = addedS3Images;
    }

    public List<AwsS3ImageUploadViewModel> getDeletedS3Images() {
        return deletedS3Images;
    }

    public void setDeletedS3Images(List<AwsS3ImageUploadViewModel> deletedS3Images) {
        this.deletedS3Images = deletedS3Images;
    }

    public List<AwsS3ImageUploadViewModel> getUploadFailedS3Images() {
        return uploadFailedS3Images;
    }

    public void setUploadFailedS3Images(List<AwsS3ImageUploadViewModel> uploadFailedS3Images) {
        this.uploadFailedS3Images = uploadFailedS3Images;
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

    public Long getLastSynchronized() {
        return lastSynchronized;
    }

    public void setLastSynchronized(Long lastSynchronized) {
        this.lastSynchronized = lastSynchronized;
    }
}
