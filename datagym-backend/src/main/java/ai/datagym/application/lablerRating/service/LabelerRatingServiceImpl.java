package ai.datagym.application.lablerRating.service;

import ai.datagym.application.lablerRating.entity.LabelerRating;
import ai.datagym.application.lablerRating.models.bindingModels.LabelerRatingUpdateBindingModel;
import ai.datagym.application.lablerRating.models.viewModels.LabelerRatingViewModel;
import ai.datagym.application.lablerRating.repo.LabelerRatingRepository;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.repo.ProjectRepository;
import ai.datagym.application.security.util.DataGymSecurity;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static ai.datagym.application.utils.constants.CommonMessages.OAUTH_SCOPE_TYPE;

@Service()
@Transactional(propagation = Propagation.REQUIRED)
public class LabelerRatingServiceImpl implements LabelerRatingService {
    private final LabelerRatingRepository labelerRatingRepository;
    private final ProjectRepository projectRepository;
    private final MediaRepository mediaRepository;

    @Autowired
    public LabelerRatingServiceImpl(LabelerRatingRepository labelerRatingRepository, ProjectRepository projectRepository, MediaRepository mediaRepository) {
        this.labelerRatingRepository = labelerRatingRepository;
        this.projectRepository = projectRepository;
        this.mediaRepository = mediaRepository;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public void addToPositive(LabelerRatingUpdateBindingModel labelerRatingUpdateBindingModel) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        String labelerId = labelerRatingUpdateBindingModel.getLabelerId();
        String projectId = labelerRatingUpdateBindingModel.getProjectId();
        String mediaId = labelerRatingUpdateBindingModel.getMediaId();

        // Check if Project and media exist
        Project projectById = getProjectById(projectId);
        Media mediaById = getMediaById(mediaId);

        // Get the Labeler Rating for the current Labeler and Project
        LabelerRating labelerRating = getRatingByLabelerIdAndProjectIdOrCreate(labelerId, projectById, mediaById);

        int positive = labelerRating.getPositive();
        labelerRating.setPositive(positive + 1);

        labelerRatingRepository.save(labelerRating);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE})
    @Override
    public void addToNegative(LabelerRatingUpdateBindingModel labelerRatingUpdateBindingModel) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        String labelerId = labelerRatingUpdateBindingModel.getLabelerId();
        String projectId = labelerRatingUpdateBindingModel.getProjectId();
        String mediaId = labelerRatingUpdateBindingModel.getMediaId();

        // Check if Project and media exist
        Project projectById = getProjectById(projectId);
        Media mediaById = getMediaById(mediaId);

        // Get the Labeler Rating for the current Labeler and Project
        LabelerRating labelerRating = getRatingByLabelerIdAndProjectIdOrCreate(labelerId, projectById, mediaById);

        int negative = labelerRating.getNegative();
        labelerRating.setNegative(negative + 1);

        labelerRatingRepository.save(labelerRating);
    }

    /**
     * Check if LabelRating for the current labelerId and projectId exists. If not,
     * create new LabelRating
     */
    private boolean checkIfRatingByLabelerIdAndProjectIdAlreadyExists(String labelerId, String projectId, String mediaId) {
        Optional<LabelerRating> optionalLabelerRating = labelerRatingRepository
                .findByLabelerAndProjectIdAndMediaId(labelerId, projectId, mediaId);

        if (optionalLabelerRating.isPresent()) {
            throw new GenericException("rating_alreadyexists", null, null, projectId, labelerId);
        }

        return false;
    }

    /**
     * Create new LabelRating
     */
    private LabelerRatingViewModel createLabelRating(String labelerId, String projectId, String mediaId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE);

        // Check if Project exists
        Project projectById = getProjectById(projectId);
        String owner = projectById.getOwner();

        //Permissions check
        DataGymSecurity.isAdminOrUser(owner, false);

        Media mediaById = getMediaById(mediaId);

        // Check if Rating already exists
        checkIfRatingByLabelerIdAndProjectIdAlreadyExists(labelerId, projectId, mediaId);

        LabelerRating labelerRating = new LabelerRating();

        labelerRating.setLabeler(labelerId);
        labelerRating.setProject(projectById);
        labelerRating.setMedia(mediaById);

        LabelerRating savedRating = labelerRatingRepository.save(labelerRating);

        return LabelerRatingMapper.mapToLabelerRatingViewModel(savedRating);
    }

    /**
     * Get the Labeler Rating for the current Labeler and Project. If a LabelRating is not found, it will be created
     */
    private LabelerRating getRatingByLabelerIdAndProjectIdOrCreate(String labelerId, Project project, Media media) {
        String projectId = project.getId();
        String mediaId = media.getId();

        Optional<LabelerRating> optionalLabelerRating = labelerRatingRepository
                .findByLabelerAndProjectIdAndMediaId(labelerId, projectId, mediaId);

        if (optionalLabelerRating.isEmpty()) {
            LabelerRatingViewModel labelerRatingViewModel = createLabelRating(labelerId, projectId, mediaId);

            return LabelerRatingMapper.mapToLabelerRating(labelerRatingViewModel, project, media);
        }

        return optionalLabelerRating.get();
    }

    private Project getProjectById(String projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project", "id", "" + projectId));
    }

    private Media getMediaById(String mediaId) {
        return mediaRepository.findById(mediaId)
                .orElseThrow(() -> new NotFoundException("media", "id", "" + mediaId));
    }
}
