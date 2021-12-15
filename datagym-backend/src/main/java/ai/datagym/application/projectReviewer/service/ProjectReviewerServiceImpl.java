package ai.datagym.application.projectReviewer.service;

import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.repo.ProjectRepository;
import ai.datagym.application.projectReviewer.entity.ProjectReviewer;
import ai.datagym.application.projectReviewer.models.bindingModels.ProjectReviewerCreateBindingModel;
import ai.datagym.application.projectReviewer.models.viewModels.ProjectReviewerViewModel;
import ai.datagym.application.projectReviewer.repo.ProjectReviewerRepository;
import ai.datagym.application.security.models.viewModles.UserMinInfoViewModel;
import ai.datagym.application.security.service.UserInfoService;
import ai.datagym.application.security.util.DataGymSecurity;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ai.datagym.application.utils.constants.CommonMessages.BASIC_SCOPE_TYPE;
import static ai.datagym.application.utils.constants.CommonMessages.OAUTH_SCOPE_TYPE;
import static ai.datagym.application.utils.constants.CommonMessages.SUPER_ADMIN_SCOPE_TYPE;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class ProjectReviewerServiceImpl implements ProjectReviewerService {
    private final ProjectReviewerRepository projectReviewerRepository;
    private final ProjectRepository projectRepository;
    private final UserInfoService userInfoService;

    @Autowired
    public ProjectReviewerServiceImpl(ProjectReviewerRepository projectReviewerRepository, ProjectRepository projectRepository, UserInfoService userInfoService) {
        this.projectReviewerRepository = projectReviewerRepository;
        this.projectRepository = projectRepository;
        this.userInfoService = userInfoService;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE})
    @Override
    public ProjectReviewerViewModel createReviewer(ProjectReviewerCreateBindingModel projectReviewerCreateBindingModel) throws IOException {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE);

        String projectId = projectReviewerCreateBindingModel.getProjectId();
        String userId = projectReviewerCreateBindingModel.getUserId();

        Project projectById = getProjectById(projectId);
        String owner = projectById.getOwner();

        //Permissions check
        DataGymSecurity.isAdmin(owner, false);

        // Get All Users from the current Organisation
        List<UserMinInfoViewModel> allUsersFromOrg = getAllUsersFromOrg(owner);

        // Check if user with userId exists
        UserMinInfoViewModel userMinInfoViewModel = checkIfUserIdIsValidAndReturnUserInfo(userId, allUsersFromOrg);

        // Check if user with userId is already Reviewer for the Current Project
        checkIfUserIsAlreadyReviewerForCurrentProject(projectById, userId);

        ProjectReviewer projectReviewer = ProjectReviewerModelMapper.mapToProjectReviewer(projectReviewerCreateBindingModel);

        long currentTime = System.currentTimeMillis();
        projectReviewer.setTimestamp(currentTime);

        // Add the Project to the current ProjectReviewer
        projectReviewer.setProject(projectById);

        ProjectReviewer savedReviewer = projectReviewerRepository.save(projectReviewer);

        return ProjectReviewerModelMapper.mapToProjectReviewerViewModel(savedReviewer, projectId, userMinInfoViewModel);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE})
    @Override
    public void deleteReviewerFromProject(String reviewerId) {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE);

        ProjectReviewer reviewer = getReviewerById(reviewerId);
        Project projectById = reviewer.getProject();

        // Permissions check
        String owner = projectById.getOwner();
        DataGymSecurity.isAdmin(owner, false);

        // Remove the current Reviewer from the Project
        projectById.getReviewers().remove(reviewer);

        // Delete the current Reviewer
        projectReviewerRepository.delete(reviewer);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE})
    @Override
    public List<ProjectReviewerViewModel> getAllReviewerForProject(String projectId) throws IOException {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE, SUPER_ADMIN_SCOPE_TYPE);

        Project projectById = getProjectById(projectId);
        String owner = projectById.getOwner();

        //Permissions check
        DataGymSecurity.isAdmin(owner, true);

        // Get All Users from the current Organisation
        List<UserMinInfoViewModel> allUsersFromOrg = getAllUsersFromOrg(owner);

        return ProjectReviewerModelMapper.mapToProjectReviewerViewModelList(projectById, allUsersFromOrg);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE})
    @Override
    public List<UserMinInfoViewModel> getAllPossibleReviewerForProject(String projectId) throws IOException {
        //Permissions check
        DataGymSecurity.isAuthenticatedAndHasAnyScope(OAUTH_SCOPE_TYPE, BASIC_SCOPE_TYPE);

        // // Disallow open core
        DataGymSecurity.disallowOnOpenCore();

        Project projectById = getProjectById(projectId);
        String owner = projectById.getOwner();

        //Permissions check
        DataGymSecurity.isAdmin(owner, false);

        // Get All Users from the current Organisation
        List<UserMinInfoViewModel> allUsersFromOrg = getAllUsersFromOrg(owner);

        // Get the UserIds of all Reviewers for the current Project
        List<String> allReviewerUserIdsForProject = projectById.getReviewers().stream()
                .map(ProjectReviewer::getUserId)
                .collect(Collectors.toList());

        return allUsersFromOrg.stream()
                .filter(userMinInfoViewModel -> !allReviewerUserIdsForProject.contains(userMinInfoViewModel.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Fetch all Users in the current Organisation (Project-Organisation) from the Login System
     */
    private List<UserMinInfoViewModel> getAllUsersFromOrg(String organisationId) throws IOException {
        return userInfoService.getAllUsersFromOrg(organisationId, null);
    }

    /**
     * Check if user with userId is already Reviewer for the Current Project
     */
    private void checkIfUserIsAlreadyReviewerForCurrentProject(Project projectById, String userId) {
        Optional<ProjectReviewer> optionalProjectReviewer = projectById.getReviewers().stream()
                .filter(currentReviewerId -> currentReviewerId.getUserId().equals(userId)).findFirst();

        if (optionalProjectReviewer.isPresent()) {
            throw new GenericException("user_is_already_reviewer", null, null, projectById.getName(), userId);
        }
    }

    private ProjectReviewer getReviewerById(String reviewerId) {
        return projectReviewerRepository
                .findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("Reviewer", "id", "" + reviewerId));
    }

    private Project getProjectById(String projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project", "id", "" + projectId));
    }

    /**
     * Check if user with userId exists and return UserMinInfoViewModel for this User
     */
    private UserMinInfoViewModel checkIfUserIdIsValidAndReturnUserInfo(String userId, List<UserMinInfoViewModel> allUsersFromOrg) {
        Optional<UserMinInfoViewModel> optionalUserMinInfoViewModel = allUsersFromOrg
                .stream()
                .filter(userMinInfo -> userMinInfo.getId().equals(userId))
                .findFirst();

        if(optionalUserMinInfoViewModel.isEmpty()){
            throw new NotFoundException("User", "id", "" + userId);
        }

        return optionalUserMinInfoViewModel.get();
    }
}
