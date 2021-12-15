package ai.datagym.application.dataset.models.awsS3.viewModels;

public class AwsS3CredentialViewModel {
    private String id;
    private String accessKey;
    private String secretKey;
    private String name;
    private String locationPath;
    private String bucketName;
    private String bucketRegion;
    private String lastError;
    private Long lastErrorTimeStamp;
    private Long lastSynchronized;
    private String datasetId;

    public AwsS3CredentialViewModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
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

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }
}
