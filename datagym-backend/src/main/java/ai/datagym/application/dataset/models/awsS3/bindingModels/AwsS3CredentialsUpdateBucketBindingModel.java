package ai.datagym.application.dataset.models.awsS3.bindingModels;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class AwsS3CredentialsUpdateBucketBindingModel {
    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    private String locationPath;

    @NotNull
    @NotEmpty
    private String bucketName;

    @NotNull
    @NotEmpty
    private String bucketRegion;

    public AwsS3CredentialsUpdateBucketBindingModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocationPath() {
        return locationPath;
    }

    public void setLocationPath(String locationPath) {
        this.locationPath = locationPath;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketRegion() {
        return bucketRegion;
    }

    public void setBucketRegion(String bucketRegion) {
        this.bucketRegion = bucketRegion;
    }
}
