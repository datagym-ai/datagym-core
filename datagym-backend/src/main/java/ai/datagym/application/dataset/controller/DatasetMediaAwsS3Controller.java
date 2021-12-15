package ai.datagym.application.dataset.controller;

import ai.datagym.application.dataset.service.awsS3.DatasetAwsS3MediaService;
import ai.datagym.application.errorHandling.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(value = "/api/dataset")
public class DatasetMediaAwsS3Controller {

    private final Optional<DatasetAwsS3MediaService> awsS3MediaService;

    public DatasetMediaAwsS3Controller(@Autowired(required = false) Optional<DatasetAwsS3MediaService> awsS3MediaService) {
        this.awsS3MediaService = awsS3MediaService;
    }

    @GetMapping("/{datasetId}/signedUri")
    public String createAwsPreSignedUploadURI(@PathVariable("datasetId") String datasetId,
                                              @RequestParam("filename") String filename) {
        return awsS3MediaService.orElseThrow(() -> new ServiceUnavailableException("aws")).createAwsPreSignedUploadURI(datasetId, filename);
    }

    @PostMapping("/{datasetId}/confirmUri")
    public void confirmPreSignedUrlUpload(@PathVariable("datasetId") String datasetId,
                                          @RequestBody String preSignedUrl) {
        awsS3MediaService.orElseThrow(() -> new ServiceUnavailableException("aws")).confirmPreSignedUrlUpload(datasetId, preSignedUrl);

    }
}
