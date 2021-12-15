package ai.datagym.application.dataset.entity;

import ai.datagym.application.media.entity.AwsS3Media;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "aws_S3_credentials", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
public class DatasetAwsS3UserCredentials {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "location_path", nullable = false)
    private String locationPath;

    @Column(name = "access_key", nullable = false)
    private String accessKey;

    @Column(name = "secret_key", nullable = false)
    private String secretKey;

    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    @Column(name = "bucket_region", nullable = false)
    private String bucketRegion;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "last_error_timestamp")
    private Long lastErrorTimeStamp;

    @Column(name = "last_synchronized")
    private Long lastSynchronized;

    @JsonBackReference(value = "dataset_aws_credentials")
    @ManyToOne(optional = false, targetEntity = Dataset.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_aws_credentials_dataset"))
    private Dataset dataset;

    @JsonManagedReference(value = "awsCredentials_awsS3Images")
    @OneToMany(mappedBy = "credentials", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AwsS3Media> awsS3Images = new ArrayList<>();


    public DatasetAwsS3UserCredentials() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
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

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public String getBucketRegion() {
        return bucketRegion;
    }

    public void setBucketRegion(String bucketRegion) {
        this.bucketRegion = bucketRegion;
    }

    public List<AwsS3Media> getAwsS3Images() {
        return awsS3Images;
    }

    public void setAwsS3Images(List<AwsS3Media> awsS3Images) {
        this.awsS3Images = awsS3Images;
    }

    @Override
    public String toString() {
        return "AwsS3Credentials{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", locationPath='" + locationPath + '\'' +
                ", accessKey='" + accessKey + '\'' +
                ", secretKey='" + secretKey + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", bucketRegion='" + bucketRegion + '\'' +
                ", lastError='" + lastError + '\'' +
                ", lastErrorTimeStamp=" + lastErrorTimeStamp +
                ", lastSynchronized=" + lastSynchronized +
                ", dataset=" + dataset +
                ", awsS3Images=" + awsS3Images +
                '}';
    }
}
