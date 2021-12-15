package ai.datagym.application.project.controller;

import ai.datagym.application.externalAPI.models.viewModels.ExternalApiSchemaValidationViewModel;
import ai.datagym.application.externalAPI.service.ExternalApiService;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.project.models.bindingModels.ProjectCreateBindingModel;
import ai.datagym.application.project.models.bindingModels.ProjectUpdateBindingModel;
import ai.datagym.application.project.models.viewModels.ProjectDashboardViewModel;
import ai.datagym.application.project.models.viewModels.ProjectGeometryCountsViewModel;
import ai.datagym.application.project.models.viewModels.ProjectLabelCountByDayViewModel;
import ai.datagym.application.project.models.viewModels.ProjectViewModel;
import ai.datagym.application.project.service.ProjectService;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/project")
@Validated
public class ProjectController {
    private final ProjectService projectService;
    private final ExternalApiService externalApiService;

    @Autowired
    public ProjectController(ProjectService projectService, ExternalApiService externalApiService) {
        this.projectService = projectService;
        this.externalApiService = externalApiService;
    }

    @PostMapping()
    public ProjectViewModel createProject(@RequestBody @Valid ProjectCreateBindingModel projectCreateBindingModel) {
        return projectService.createProject(projectCreateBindingModel, false);
    }

    @PutMapping("/{id}")
    public ProjectViewModel updateProject(
            @PathVariable("id") String id,
            @RequestBody @Valid ProjectUpdateBindingModel projectUpdateBindingModel) {
        return projectService.updateProject(id, projectUpdateBindingModel);
    }

    @GetMapping("/{id}")
    public ProjectViewModel getProject(@PathVariable("id") @NotNull String id) {
        return projectService.getProject(id);
    }

    @GetMapping("/{id}/task")
    public List<LabelTaskViewModel> getAllProjectTasks(@PathVariable("id") @NotNull String projectId,
                                                       @RequestParam(value = "search", required = false) @Pattern(regexp = "^([a-zA-Z0-9]|[_ ,.]|[-])*$") String filterSearchTerm,
                                                       @RequestParam(value = "state", required = false) LabelTaskState labelTaskState,
                                                       @RequestParam(value = "limit", required = false, defaultValue = "0") int maxResults) {
        return projectService.getProjectTasks(projectId, filterSearchTerm, labelTaskState, maxResults);
    }

    @GetMapping()
    public List<ProjectViewModel> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/{orgId}/org")
    public List<ProjectViewModel> getAllProjectsFromOrganisation(@PathVariable("orgId") @NotNull String orgId) {
        return projectService.getAllProjectsFromOrganisation(orgId);
    }

    @GetMapping("/suitableDatasetConnections")
    public List<ProjectViewModel> getSuitableProjectsForDataset(@RequestParam("datasetId") @NotNull String datasetId) {
        return projectService.getSuitableProjectsForDataset(datasetId);
    }

    @GetMapping("/admin")
    public List<ProjectViewModel> getAllProjectsFromOrganisationAndLoggedInUserIsAdmin() {
        return projectService.getAllProjectsFromOrganisationAndLoggedInUserIsAdmin();
    }

    @DeleteMapping("/{id}")
    public ProjectViewModel deleteProject(@PathVariable @NotNull String id) {
        return projectService.deleteProjectById(id, true);
    }

    @DeleteMapping("/{id}/restore")
    public ProjectViewModel restoreProject(@PathVariable @NotNull String id) {
        return projectService.deleteProjectById(id, false);
    }

    @DeleteMapping("/{id}/deleteFromDb")
    public void permanentDeleteProjectFromDb(@PathVariable @NotNull String id) {
        projectService.permanentDeleteProjectFromDB(id);
    }

    @PostMapping("/{id}/pin")
    public ProjectViewModel pinProject(@PathVariable("id") @NotNull String id) {
        return projectService.pinProject(id, true);
    }

    @PostMapping("/{id}/unpin")
    public ProjectViewModel unpinProject(@PathVariable("id") @NotNull String id) {
        return projectService.pinProject(id, false);
    }

    @PostMapping("/{projectId}/dataset/{datasetId}")
    public void addDataset(@PathVariable("projectId") @NotBlank @Length(min = 1) String projectId,
                           @PathVariable("datasetId") @NotBlank @Length(min = 1) String datasetId) {
        projectService.addDataset(projectId, datasetId);
    }

    @DeleteMapping("/{projectId}/dataset/{datasetId}/remove")
    public void removeDataset(@PathVariable("projectId") @NotBlank @Length(min = 1) String projectId,
                              @PathVariable("datasetId") @NotBlank @Length(min = 1) String datasetId) {
        projectService.removeDataset(projectId, datasetId);
    }

    /**
     * Exports JSON-File with all {@link ai.datagym.application.labelIteration.entity.LcEntryValue}s of
     * all {@link ai.datagym.application.labelTask.entity.LabelTask}s with {@link LabelTaskState} 'COMPLETED', 'REVIEWED'
     * or 'SKIPPED' in the current project{@param projectId}.
     */
    @GetMapping("/{projectId}/export")
    public void exportLabels(HttpServletResponse res,
                             @NotBlank @Length(min = 1) @PathVariable("projectId") String projectId)
            throws IOException {
        projectService.exportProjectLabels(projectId, res);
    }

    @GetMapping("/exportVideoTask/{taskId}")
    public void exportVideoTask(HttpServletResponse res,
                                @NotBlank @Length(min = 1) @PathVariable("taskId") String taskId) throws IOException {
        projectService.exportVideoTask(taskId, res);
    }

    @PutMapping("/{id}/activate")
    public ProjectViewModel updateReviewActivated(
            @PathVariable("id") String id,
            @RequestParam(value = "reviewActivated", required = false, defaultValue = "false") boolean reviewActivated) {
        return projectService.updateReviewActivated(id, reviewActivated);
    }

    @GetMapping("/{projectId}/dashboard")
    public ProjectDashboardViewModel getDashboardData(@PathVariable("projectId") @NotNull String projectId) {
        return projectService.getDashboardData(projectId);
    }

    @GetMapping("/{projectId}/dashboard/geometryCounts")
    public ProjectGeometryCountsViewModel getGeometryCounts(@PathVariable("projectId") @NotNull String projectId) {
        return projectService.getGeometryCounts(projectId);
    }

    @GetMapping("/{projectId}/dashboard/geometryCountsByDay")
    public ProjectLabelCountByDayViewModel getGeometryCountsByDay(@PathVariable("projectId") @NotNull String projectId) {
        return projectService.getGeometryCountsByDay(projectId);
    }

    @PostMapping("/{projectId}/prediction")
    public ExternalApiSchemaValidationViewModel uploadPredictedValues(HttpServletRequest httpServletRequest,
                                                                      @PathVariable (value = "projectId") String projectId) throws IOException {
        return externalApiService.uploadPredictedValues(projectId, httpServletRequest.getInputStream());
    }
}
