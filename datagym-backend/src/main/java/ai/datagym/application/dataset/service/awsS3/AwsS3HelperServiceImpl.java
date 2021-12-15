package ai.datagym.application.dataset.service.awsS3;

import ai.datagym.application.dataset.config.AwsInternalConfiguration;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static ai.datagym.application.utils.constants.CommonMessages.OAUTH_SCOPE_TYPE;
import static ai.datagym.application.utils.constants.CommonMessages.TOKEN_SCOPE_TYPE;

@Service
@Transactional(propagation = Propagation.REQUIRED)
@ConditionalOnProperty(
        value = "aws.enabled",
        havingValue = "true")
public class AwsS3HelperServiceImpl implements AwsS3HelperService {

    private final AwsInternalConfiguration awsInternalConfiguration;
    private final AmazonS3 internalAmazonS3Client;

    public AwsS3HelperServiceImpl(
            AwsInternalConfiguration awsInternalConfiguration,
            AmazonS3 internalAmazonS3Client) {
        this.awsInternalConfiguration = awsInternalConfiguration;
        this.internalAmazonS3Client = internalAmazonS3Client;
    }

    @Override
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    public URL createAwsPreSignedGetUriInternal(String objectKey) {
        java.util.Date expiration = Date.from(
                LocalDateTime.now()
                        .plusMinutes(awsInternalConfiguration.getInternalSignedUrlValidityMinutes())
                        .toInstant(ZoneOffset.UTC));

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(awsInternalConfiguration.getInternalAwsBucketName(), objectKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        return internalAmazonS3Client.generatePresignedUrl(generatePresignedUrlRequest);
    }

    @Override
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    public void permanentDeleteAwsS3Object(String objectKey) {
        internalAmazonS3Client.deleteObject(awsInternalConfiguration.getInternalAwsBucketName(), objectKey);
    }
}
