package ai.datagym.application.dataset.service.dataset;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetCreateBindingModel;
import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetUpdateBindingModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetAllViewModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetProjectViewModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetViewModel;
import ai.datagym.application.media.service.MediaMapper;
import ai.datagym.application.project.entity.MediaType;
import ai.datagym.application.project.service.ProjectMapper;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public final class DatasetMapper {


    private DatasetMapper() {
    }

    public static Dataset mapToDataset(@Valid DatasetCreateBindingModel from) {
        Dataset to = new Dataset();

        to.setName(from.getName());
        to.setShortDescription(from.getShortDescription());
        to.setOwner(from.getOwner());
        if (from.getMediaType() == null) {
            to.setMediaType(MediaType.IMAGE);
        } else {
            to.setMediaType(from.getMediaType());
        }
        return to;
    }

    public static Dataset mapToDataset(@Valid DatasetUpdateBindingModel from, Dataset to) {
        to.setName(from.getName());
        to.setShortDescription(from.getShortDescription());

        return to;
    }

    public static DatasetViewModel mapToDatasetViewModel(Dataset from, boolean includeMedia) {
        int projectCount = (int) from.getProjects()
                .stream()
                .filter(project -> !project.isDeleted())
                .count();

        DatasetViewModel to = new DatasetViewModel();
        to.setId(from.getId());
        to.setName(from.getName());
        to.setShortDescription(from.getShortDescription());
        to.setTimestamp(from.getTimestamp());
        to.setDeleted(from.isDeleted());
        to.setDeleteTime(from.getDeleteTime());
        to.setProjectCount(projectCount);
        to.setOwner(from.getOwner());
        to.setMediaType(from.getMediaType());
        if (includeMedia) {
            to.setMedia(MediaMapper.mapToMediaViewModel(from.getMedia()));
        } else {
            to.setMedia(Collections.emptySet());
        }
        return to;
    }

    public static DatasetAllViewModel mapToDatasetAllViewModel(Dataset from, @Nullable ToIntFunction<Dataset> mediaCountResolver) {
        int projectCount = (int) from.getProjects()
                .stream()
                .filter(project -> !project.isDeleted())
                .count();
        Integer mediaCount = 0;

        if (mediaCountResolver != null) {
            mediaCount = mediaCountResolver.applyAsInt(from);
        }

        DatasetAllViewModel to = new DatasetAllViewModel();
        to.setId(from.getId());
        to.setName(from.getName());
        to.setShortDescription(from.getShortDescription());
        to.setTimestamp(from.getTimestamp());
        to.setDeleted(from.isDeleted());
        to.setDeleteTime(from.getDeleteTime());
        to.setMediaCount(mediaCount);
        to.setProjectCount(projectCount);
        to.setOwner(from.getOwner());
        to.setMediaType(from.getMediaType());

        return to;
    }

    public static Set<DatasetAllViewModel> mapToDatasetAllViewModel(Set<Dataset> from, @Nullable ToIntFunction<Dataset> mediaCountResolver) {
        return from.stream()
                .filter(dataset -> !dataset.isDeleted())
                .map(dataset -> DatasetMapper.mapToDatasetAllViewModel(dataset, mediaCountResolver))
                .collect(Collectors.toSet());
    }

    public static DatasetProjectViewModel mapToDatasetProjectViewModel(Dataset from) {
        DatasetProjectViewModel to = new DatasetProjectViewModel();
        to.setId(from.getId());
        to.setName(from.getName());
        to.setShortDescription(from.getShortDescription());
        to.setTimestamp(from.getTimestamp());
        to.setDeleted(from.isDeleted());
        to.setDeleteTime(from.getDeleteTime());
        to.setOwner(from.getOwner());
        to.setMedia(MediaMapper.mapToMediaViewModel(from.getMedia()));
        to.setProjects(ProjectMapper.mapToProjectDatasetViewModel(from.getProjects()));

        return to;
    }
}
