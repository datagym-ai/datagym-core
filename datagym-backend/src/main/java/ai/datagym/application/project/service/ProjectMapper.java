package ai.datagym.application.project.service;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.service.dataset.DatasetMapper;
import ai.datagym.application.project.entity.MediaType;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.project.models.bindingModels.ProjectCreateBindingModel;
import ai.datagym.application.project.models.bindingModels.ProjectUpdateBindingModel;
import ai.datagym.application.project.models.viewModels.ProjectDashboardViewModel;
import ai.datagym.application.project.models.viewModels.ProjectDatasetViewModel;
import ai.datagym.application.project.models.viewModels.ProjectViewModel;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public final class ProjectMapper {

    private ProjectMapper() {
    }

    public static Project mapToProject(@Valid ProjectCreateBindingModel from) {
        Project to = new Project();

        to.setName(from.getName());
        to.setDescription(from.getDescription());
        to.setShortDescription(from.getShortDescription());
        to.setOwner(from.getOwner());
        if (from.getMediaType() == null) {
            to.setMediaType(MediaType.IMAGE);
        } else {
            to.setMediaType(from.getMediaType());
        }

        return to;
    }

    public static Project mapToProject(@Valid ProjectUpdateBindingModel from, Project to) {
        to.setName(from.getName());
        to.setDescription(from.getDescription());
        to.setShortDescription(from.getShortDescription());
        return to;
    }

    public static ProjectViewModel mapToProjectViewModel(Project from, @Nullable ToIntFunction<Dataset> mediaCountResolver) {
        ProjectViewModel to = new ProjectViewModel();
        to.setId(from.getId());
        to.setName(from.getName());
        to.setDescription(from.getDescription());
        to.setShortDescription(from.getShortDescription());
        to.setTimestamp(from.getTimestamp());
        to.setDeleted(from.isDeleted());
        to.setDeleteTime(from.getDeleteTime());
        to.setPinned(from.isPinned());
        to.setDatasets(DatasetMapper.mapToDatasetAllViewModel(from.getDatasets(), mediaCountResolver));
        to.setLabelConfigurationId(from.getLabelConfiguration().getId());
        to.setLabelIterationId(from.getLabelIteration().getId());
        to.setOwner(from.getOwner());
        to.setReviewActivated(from.isReviewActivated());
        to.setMediaType(from.getMediaType());
        return to;
    }

    public static Set<ProjectDatasetViewModel> mapToProjectDatasetViewModel(Set<Project> from) {
        return from.stream()
                .filter(project -> !project.isDeleted())
                .map(ProjectMapper::mapToProjectDatasetViewModel)
                .collect(Collectors.toSet());
    }

    public static ProjectDatasetViewModel mapToProjectDatasetViewModel(Project from) {
        ProjectDatasetViewModel to = new ProjectDatasetViewModel();
        to.setId(from.getId());
        to.setName(from.getName());
        to.setDescription(from.getDescription());
        to.setShortDescription(from.getShortDescription());
        to.setTimestamp(from.getTimestamp());
        to.setDeleted(from.isDeleted());
        to.setDeleteTime(from.getDeleteTime());
        to.setPinned(from.isPinned());
        to.setOwner(from.getOwner());
        to.setReviewActivated(from.isReviewActivated());
        to.setMediaType(from.getMediaType());
        return to;
    }

    public static ProjectDashboardViewModel mapToProjectDashboardViewModel(Project from, @Nullable ToIntFunction<Dataset> mediaCountResolver) {
        ProjectDashboardViewModel to = new ProjectDashboardViewModel();

        to.setId(from.getId());
        to.setName(from.getName());
        to.setDescription(from.getDescription());
        to.setShortDescription(from.getShortDescription());
        to.setTimestamp(from.getTimestamp());
        to.setDeleted(from.isDeleted());
        to.setDeleteTime(from.getDeleteTime());
        to.setPinned(from.isPinned());
        to.setDatasets(DatasetMapper.mapToDatasetAllViewModel(from.getDatasets(), mediaCountResolver));
        to.setLabelConfigurationId(from.getLabelConfiguration().getId());
        to.setLabelIterationId(from.getLabelIteration().getId());
        to.setOwner(from.getOwner());
        to.setReviewActivated(from.isReviewActivated());
        to.setMediaType(from.getMediaType());

        return to;
    }
}
