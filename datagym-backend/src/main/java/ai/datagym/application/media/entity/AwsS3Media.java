package ai.datagym.application.media.entity;

import ai.datagym.application.dataset.entity.DatasetAwsS3UserCredentials;
import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;


@Entity
public abstract class AwsS3Media extends Media {
    @Column(name = "aws_e_tag")
    private String awsETag;

    @Column(name = "aws_key")
    private String awsKey;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "last_error_timeStamp")
    private Long lastErrorTimeStamp;

    // If the credentials are "null" the file is uploaded into the dataygm.ai owned aws s3
    @JsonBackReference(value = "awsCredentials_awsS3Images")
    @ManyToOne(optional = true, targetEntity = DatasetAwsS3UserCredentials.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "awsS3Credentials_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_awsS3Image_awscredentials"))
    private DatasetAwsS3UserCredentials credentials;

    public AwsS3Media() {
        super();
    }

    public String getAwsETag() {
        return awsETag;
    }

    public void setAwsETag(String awsETag) {
        this.awsETag = awsETag;
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

    public DatasetAwsS3UserCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(DatasetAwsS3UserCredentials credentials) {
        this.credentials = credentials;
    }
}
