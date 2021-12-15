package ai.datagym.application.dataset.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        value = "aws.enabled",
        havingValue = "true")
public class AwsInternalConfiguration {

    @Value(value = "${aws.internalAccessKey}")
    private String internalAwsAccessKey;

    @Value(value = "${aws.internalSecretKey}")
    private String internalAwsSecretKey;

    @Value(value = "${aws.internalRegionName}")
    private String internalAwsRegionName;

    @Value(value = "${aws.internalBucketName}")
    private String internalAwsBucketName;

    @Value(value = "${aws.internalSignedUrlValidityMinutes}")
    private Integer internalSignedUrlValidityMinutes;

    public String getInternalAwsAccessKey() {
        return internalAwsAccessKey;
    }

    public String getInternalAwsSecretKey() {
        return internalAwsSecretKey;
    }

    public String getInternalAwsRegionName() {
        return internalAwsRegionName;
    }

    public String getInternalAwsBucketName() {
        return internalAwsBucketName;
    }

    public Integer getInternalSignedUrlValidityMinutes() {
        return internalSignedUrlValidityMinutes;
    }

    /**
     * Initialize a bean for accessing the amazon s3 bucket for video uploads
     *
     * @return Instance of {@link AmazonS3}
     */
    @Bean
    AmazonS3 internalAmazonS3Client() {
        // Create Aws Credentials Instance
        AWSCredentials awsCredentials = new BasicAWSCredentials(getInternalAwsAccessKey(), getInternalAwsSecretKey());

        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.fromName(getInternalAwsRegionName()))
                .build();
    }
}
