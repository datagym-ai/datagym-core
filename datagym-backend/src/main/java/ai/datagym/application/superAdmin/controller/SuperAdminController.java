package ai.datagym.application.superAdmin.controller;

import ai.datagym.application.dataset.models.dataset.viewModels.DatasetAllViewModel;
import ai.datagym.application.project.models.viewModels.ProjectViewModel;
import ai.datagym.application.superAdmin.service.SuperAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/superadmin")
@Validated
public class SuperAdminController {
    private final SuperAdminService superAdminService;

    @Autowired
    public SuperAdminController(SuperAdminService superAdminService) {
        this.superAdminService = superAdminService;
    }

    @GetMapping("/project")
    public List<ProjectViewModel> getAllProjectFromDb() {
        return superAdminService.getAllProjectFromDb();
    }

    @GetMapping("/dataset")
    public List<DatasetAllViewModel> getAllDatasetsFromDb() {
        return superAdminService.getAllDatasetsFromDb();
    }
}
