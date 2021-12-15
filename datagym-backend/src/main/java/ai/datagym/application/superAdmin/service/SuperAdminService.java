package ai.datagym.application.superAdmin.service;

import ai.datagym.application.dataset.models.dataset.viewModels.DatasetAllViewModel;
import ai.datagym.application.project.models.viewModels.ProjectViewModel;

import java.util.List;

public interface SuperAdminService {
    List<ProjectViewModel> getAllProjectFromDb();

    List<DatasetAllViewModel> getAllDatasetsFromDb();
}
