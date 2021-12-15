package ai.datagym.application.dataset.service.awsS3;

import java.io.IOException;
import java.net.URL;

public interface AwsS3HelperService {

    /**
     * Creates a aws s3 get url to fetch the media file.
     * <b>Please ensure that the request/user is sufficient permitted/validated!</b>
     *
     * @param objectKey The specific aws object key.
     * @return Pre-signed get url
     */
    URL createAwsPreSignedGetUriInternal(String objectKey) throws IOException;


    void permanentDeleteAwsS3Object(String objectKey);

}
