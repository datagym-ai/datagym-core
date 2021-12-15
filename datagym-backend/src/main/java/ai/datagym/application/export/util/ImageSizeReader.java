package ai.datagym.application.export.util;

import ai.datagym.application.dataset.entity.DatasetAwsS3UserCredentials;
import ai.datagym.application.media.entity.*;
import ai.datagym.application.media.validate.ImageValidator;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.eforce21.lib.exception.Detail;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.ValidationException;
import org.apache.tika.Tika;
import org.hibernate.Hibernate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static ai.datagym.application.utils.constants.CommonMessages.ALLOWED_IMAGE_MIME_PATTERNS;

public class ImageSizeReader {

    private final Tika tika;

    public ImageSizeReader(Tika tika) {
        this.tika = tika;
    }

    public ImageSize getSize(final Media source) {
        MediaSourceType mediaSourceType = source.getMediaSourceType();
        switch (mediaSourceType) {
            case LOCAL:
                return getSize((LocalImage) Hibernate.unproxy(source));
            case AWS_S3:
                return getSize((AwsS3Image) Hibernate.unproxy(source));
            case SHAREABLE_LINK:
                return getSize((UrlImage) Hibernate.unproxy(source));
            default:
        }

        throw new GenericException("image_type_not_found", null, null, "image type");
    }

    public ImageSize getSize(final LocalImage source) {
        LocalImage localImage = (LocalImage) Hibernate.unproxy(source);
        int width = localImage.getWidth();
        int height = localImage.getHeight();

        return new ImageSize(width, height);
    }

    public ImageSize getSize(final UrlImage source) {
        String urlString = source.getUrl();

        try {
            URL url = new URL(urlString);

            // Get mimeType of the Image
            String mimeType = tika.detect(url);

            // Validate Mime Type
            ImageValidator.validateMimes(mimeType, ALLOWED_IMAGE_MIME_PATTERNS);

            // Read a Image from the Url
            BufferedImage img = ImageIO.read(url);
            return getSize(img);
        } catch (MalformedURLException e) {
            ValidationException ve = new ValidationException();
            ve.addDetail(new Detail("data", "Invalid image url", urlString));
            throw ve;
        } catch (IOException e) {
            throw new GenericException("file_stream", null, e);
        }
    }

    public ImageSize getSize(final AwsS3Image source) {

        //Get AwsS3Credentials from the Data Base
        DatasetAwsS3UserCredentials credentials = source.getCredentials();

        String bucketName = credentials.getBucketName();
        String accessKey = credentials.getAccessKey();
        String secretKey = credentials.getSecretKey();
        String bucketRegion = credentials.getBucketRegion();

        try {
            // Create Aws Credentials Instance
            AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

            AmazonS3 amazonS3client = AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(Regions.valueOf(bucketRegion))
                    .build();

            S3Object object = amazonS3client.getObject(bucketName, source.getAwsKey());
            S3ObjectInputStream objectContent = object.getObjectContent();

            // Get mimeType of the Image
            String mimeType = object.getObjectMetadata().getContentType();

            // Validate Mime Type
            ImageValidator.validateMimes(mimeType, ALLOWED_IMAGE_MIME_PATTERNS);

            // Read a Image from the Url
            BufferedImage img = ImageIO.read(objectContent);

            // Release resources
            object.close();

            return getSize(img);
        } catch (IOException e) {
            throw new GenericException("file_stream", null, e);
        }
    }

    public ImageSize getSize(final BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        return new ImageSize(width, height);
    }

}
