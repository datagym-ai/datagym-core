package ai.datagym.application.superAdmin.service;

import ai.datagym.application.dataset.models.dataset.viewModels.DatasetAllViewModel;
import ai.datagym.application.dataset.repo.DatasetRepository;
import ai.datagym.application.dataset.service.dataset.DatasetMapper;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.media.repo.MediaRepository;
import ai.datagym.application.project.models.viewModels.ProjectViewModel;
import ai.datagym.application.project.repo.ProjectRepository;
import ai.datagym.application.project.service.ProjectMapper;
import ai.datagym.application.security.util.DataGymSecurity;
import ai.datagym.application.user.service.UserInformationService;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ai.datagym.application.utils.constants.CommonMessages.*;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class SuperAdminServiceImpl implements SuperAdminService {
    private final ProjectRepository projectRepository;
    private final DatasetRepository datasetRepository;
    private final LabelTaskRepository labelTaskRepository;
    private final UserInformationService userInformationService;
    private final MediaRepository mediaRepository;

    @Autowired
    public SuperAdminServiceImpl(ProjectRepository projectRepository,
                                 DatasetRepository datasetRepository,
                                 LabelTaskRepository labelTaskRepository,
                                 UserInformationService userInformationService,
                                 MediaRepository mediaRepository) {
        this.projectRepository = projectRepository;
        this.datasetRepository = datasetRepository;
        this.labelTaskRepository = labelTaskRepository;
        this.userInformationService = userInformationService;
        this.mediaRepository = mediaRepository;
    }

    @AuthUser
    @AuthScope(any = {SUPER_ADMIN_SCOPE_TYPE})
    @Override
    public List<ProjectViewModel> getAllProjectFromDb() {
        DataGymSecurity.isAuthenticatedAndHasAnyScope(SUPER_ADMIN_SCOPE_TYPE);

        return projectRepository
                .findAllByDeletedIsFalse()
                .stream()
                .filter(project ->
                        !project.getName().equals(DUMMY_PROJECT_PLACEHOLDER)
                )
                .map(currentProject -> {
                    ProjectViewModel projectViewModel = ProjectMapper.mapToProjectViewModel(currentProject, mediaRepository::countAllByDatasetsContainingAndDeletedFalse);

                    boolean projectExportable = isProjectExportable(currentProject.getId());
                    projectViewModel.setExportable(projectExportable);
                    return projectViewModel;
                })
                .map(currentProject -> {currentProject.setOrgData(userInformationService.getOrgDataMin(currentProject.getOwner())); return currentProject;})
                .sorted(Comparator.comparing(ProjectViewModel::getTimestamp))
                .collect(Collectors.toList());
    }

    @AuthUser
    @AuthScope(any = {SUPER_ADMIN_SCOPE_TYPE})
    @Override
    public List<DatasetAllViewModel> getAllDatasetsFromDb() {
        DataGymSecurity.isAuthenticatedAndHasAnyScope(SUPER_ADMIN_SCOPE_TYPE);

        return datasetRepository
                .findAllByDeletedIsFalse()
                .stream()
                .filter(dataset ->
                        !dataset.getName().equals(DUMMY_DATASET_ONE_PLACEHOLDER) &&
                                !dataset.getName().equals(DUMMY_DATASET_TWO_PLACEHOLDER)
                )
                .map(dataset -> DatasetMapper.mapToDatasetAllViewModel(dataset, mediaRepository::countAllByDatasetsContainingAndDeletedFalse))
                .map(dataset -> {dataset.setOrgData(userInformationService.getOrgDataMin(dataset.getOwner())); return dataset;})
                .sorted(Comparator.comparing(DatasetAllViewModel::getTimestamp))
                .collect(Collectors.toList());
    }

    private boolean isProjectExportable(String projectId) {
        long countExportableTasks = labelTaskRepository.countTasksByProjectIdAndTaskStateAndMediaDeleted(projectId);
        return countExportableTasks > 0;
    }
}
