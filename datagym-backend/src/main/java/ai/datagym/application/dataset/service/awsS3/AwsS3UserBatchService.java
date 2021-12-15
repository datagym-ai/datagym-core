package ai.datagym.application.dataset.service.awsS3;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.entity.DatasetAwsS3UserCredentials;
import ai.datagym.application.dataset.models.awsS3.viewModels.AwsS3SyncViewModel;
import ai.datagym.application.dataset.repo.AwsS3UserCredentialsRepository;
import ai.datagym.application.dataset.repo.DatasetRepository;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.labelTask.service.LabelTaskService;
import ai.datagym.application.media.entity.AwsS3Image;
import ai.datagym.application.media.entity.InvalidMediaReason;
import ai.datagym.application.media.entity.MediaSourceType;
import ai.datagym.application.media.entity.MediaUploadStatus;
import ai.datagym.application.media.models.viewModels.AwsS3ImageUploadViewModel;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.media.validate.ImageValidator;
import ai.datagym.application.project.entity.Project;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import com.eforce21.lib.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static ai.datagym.application.utils.constants.CommonMessages.ALLOWED_IMAGE_EXTENSIONS;

@Service
public class AwsS3UserBatchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AwsS3UserBatchService.class);

    @Autowired
    private LabelTaskService labelTaskService;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private LabelTaskRepository labelTaskRepository;

    @Autowired
    private AwsS3UserCredentialsRepository awsS3UserCredentialsRepository;

    @Autowired
    private DatasetRepository datasetRepository;


    /**
     * Try to save locally the current AWS-S3-Object as AwsS3Image
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void batchCreateAwsS3Images(List<S3ObjectSummary> s3ObjectSummaries,
                                       AwsS3SyncViewModel awsS3SyncViewModel,
                                       String datasetId) {

        Dataset datasetById = getDatasetById(datasetId);

        DatasetAwsS3UserCredentials credentials = getAwsS3CredentialsByDatasetId(datasetId)
                .orElseThrow(() -> new GenericException("not_found_aws_credentials", null, null, datasetId));

        for (S3ObjectSummary objectSummary : s3ObjectSummaries) {
            AwsS3ImageUploadViewModel awsS3ImageUploadViewModel = new AwsS3ImageUploadViewModel();

            AwsS3Image createdAwsS3Image = new AwsS3Image();

            String awsETag = objectSummary.getETag();
            String awsKey = objectSummary.getKey();

            long currentTime = System.currentTimeMillis();
            createdAwsS3Image.setTimestamp(currentTime);

            try {
                // Validate the mime type of the current media
                validateImageType(awsKey);

                createdAwsS3Image.setMediaSourceType(MediaSourceType.AWS_S3);
                createdAwsS3Image.setAwsETag(awsETag);
                createdAwsS3Image.setAwsKey(awsKey);

                int indexOfLastBackSlash = awsKey.lastIndexOf('/');
                int indexOfFirstQuestionMark = awsKey.indexOf('?');

                if (indexOfFirstQuestionMark > 0 && indexOfFirstQuestionMark > indexOfLastBackSlash) {
                    String mediaName = awsKey.substring(indexOfLastBackSlash + 1, indexOfFirstQuestionMark);
                    createdAwsS3Image.setMediaName(mediaName);
                } else {
                    String mediaName = awsKey.substring(indexOfLastBackSlash + 1);
                    createdAwsS3Image.setMediaName(mediaName);
                }

                // Add media to the current Dataset
                createdAwsS3Image.getDatasets().add(datasetById);

                // Add the Credentials to the current media
                createdAwsS3Image.setCredentials(credentials);

                createdAwsS3Image = mediaRepository.save(createdAwsS3Image);

                credentials.getAwsS3Images().add(createdAwsS3Image);
                datasetById.getMedia().add(createdAwsS3Image);

                for (Project project : datasetById.getProjects()) {
                    LabelTask labelTask =
                            labelTaskService.createLabelTaskInternalNoSave(project, createdAwsS3Image,
                                    project.getLabelIteration());
                    labelTaskRepository.save(labelTask);
                    createdAwsS3Image.getLabelTasks().add(labelTask);
                }


                awsS3ImageUploadViewModel.setMediaUploadStatus(MediaUploadStatus.SUCCESS.name());

            } catch (ValidationException ve) {
                awsS3ImageUploadViewModel.setMediaUploadStatus(MediaUploadStatus.FAILED.name());
                awsS3ImageUploadViewModel.setLastErrorTimeStamp(currentTime);
                awsS3ImageUploadViewModel.setLastError(InvalidMediaReason.INVALID_MIME_TYPE.name());
            } catch (Exception e) {
                awsS3ImageUploadViewModel.setMediaUploadStatus(MediaUploadStatus.FAILED.name());
                awsS3ImageUploadViewModel.setLastErrorTimeStamp(currentTime);
                awsS3ImageUploadViewModel.setLastError(InvalidMediaReason.AWS_ERROR.name());
                LOGGER.error("Error by creating a aws s3 image", e);
            }

            awsS3ImageUploadViewModel.setAwsKey(awsKey);


            if (awsS3ImageUploadViewModel.getMediaUploadStatus().equals(MediaUploadStatus.SUCCESS.name())) {
                awsS3SyncViewModel.getAddedS3Images().add(awsS3ImageUploadViewModel);
            } else {
                awsS3SyncViewModel.getUploadFailedS3Images().add(awsS3ImageUploadViewModel);
                awsS3SyncViewModel.setLastError(awsS3ImageUploadViewModel.getLastError());
                awsS3SyncViewModel.setLastErrorTimeStamp(awsS3ImageUploadViewModel.getLastErrorTimeStamp());
            }
        }
    }

    private void validateImageType(String imageUrl) {
        int indexOfLastPoint = imageUrl.lastIndexOf('.');

        String imageExtension = imageUrl.substring(indexOfLastPoint + 1);

        // Validate Mime Type
        ImageValidator.validateMimes(imageExtension, ALLOWED_IMAGE_EXTENSIONS);
    }


    private Optional<DatasetAwsS3UserCredentials> getAwsS3CredentialsByDatasetId(String datasetId) {
        return awsS3UserCredentialsRepository
                .findAwsS3UserCredentialsByDatasetId(datasetId);
    }

    private Dataset getDatasetById(String datasetId) {
        return datasetRepository
                .findById(datasetId)
                .orElseThrow(() -> new NotFoundException("dataset", "id", "" + datasetId));
    }
}
