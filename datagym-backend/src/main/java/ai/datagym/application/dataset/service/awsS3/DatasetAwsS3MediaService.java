package ai.datagym.application.dataset.service.awsS3;

public interface DatasetAwsS3MediaService {

    /**
     * Generates a pre-signed amazon s3 put url for the client-side-upload.
     *
     * @param datasetId The specific dataset id where the image should be uploaded
     * @param filename  The specific filename
     * @return Pre-signed upload url
     */
    String createAwsPreSignedUploadURI(String datasetId, String filename);

    /**
     * Checks if the client-side aws s3 upload was successful. If so,
     * - creates a media entry
     * - create the label task
     *
     * @param datasetId    The specific dataset id
     * @param preSignedUrl The specific used pre-signed url
     */
    void confirmPreSignedUrlUpload(String datasetId, String preSignedUrl);

}