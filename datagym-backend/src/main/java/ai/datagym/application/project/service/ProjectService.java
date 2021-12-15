package ai.datagym.application.project.service;

import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.project.models.bindingModels.ProjectCreateBindingModel;
import ai.datagym.application.project.models.bindingModels.ProjectUpdateBindingModel;
import ai.datagym.application.project.models.viewModels.ProjectDashboardViewModel;
import ai.datagym.application.project.models.viewModels.ProjectGeometryCountsViewModel;
import ai.datagym.application.project.models.viewModels.ProjectLabelCountByDayViewModel;
import ai.datagym.application.project.models.viewModels.ProjectViewModel;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface ProjectService {
    /**
     * Get a single project by its id.
     *
     * @param projectId The specific id to search for
     * @return Instance of {@link ProjectViewModel `}
     */
    ProjectViewModel getProject(String projectId);

    /**
     * Creates a project.
     *
     * @param projectCreateBindingModel The specific data for the creation
     * @param createDummyProject
     * @return Instance of {@link ProjectCreateBindingModel}
     */
    ProjectViewModel createProject(ProjectCreateBindingModel projectCreateBindingModel, boolean createDummyProject);

    ProjectViewModel updateProject(String id, ProjectUpdateBindingModel projectUpdateBindingModel);

    List<ProjectViewModel> getAllProjects();

    ProjectViewModel deleteProjectById(String id, boolean deleteProject);

    boolean isProjectNameUniqueAndDeletedFalse(String projectName, String owner);

    ProjectViewModel pinProject(String id, boolean setToPinned);

    void permanentDeleteProjectFromDB(String id);

    void addDataset(String projectId, String datasetId);

    void removeDataset(String projectId, String datasetId);

    List<ProjectViewModel> getAllProjectsFromOrganisation(String orgId);

    List<ProjectViewModel> getSuitableProjectsForDataset(String datasetId);

    List<LabelTaskViewModel> getProjectTasks(String projectId, String filterSearchTerm, LabelTaskState labelTaskState,
                                             int maxResults);

    List<ProjectViewModel> getAllProjectsFromOrganisationAndLoggedInUserIsAdmin();

    void exportProjectLabels(String projectId, HttpServletResponse res) throws IOException;

    void exportVideoTask(String taskId, HttpServletResponse res) throws IOException;

    ProjectViewModel updateReviewActivated(String id, boolean reviewActivated);

    ProjectDashboardViewModel getDashboardData(String projectId);

    ProjectGeometryCountsViewModel getGeometryCounts(String projectId);

    ProjectLabelCountByDayViewModel getGeometryCountsByDay(String projectId);
}
