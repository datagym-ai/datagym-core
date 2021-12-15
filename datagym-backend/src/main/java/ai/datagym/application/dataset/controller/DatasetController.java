package ai.datagym.application.dataset.controller;

import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetCreateBindingModel;
import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetFilterAndPageParam;
import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetUpdateBindingModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetAllViewModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetProjectViewModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetViewModel;
import ai.datagym.application.dataset.service.dataset.DatasetService;
import ai.datagym.application.media.models.viewModels.MediaViewModel;
import ai.datagym.application.media.models.viewModels.UrlImageUploadViewModel;
import ai.datagym.application.utils.PageReturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/dataset")
@Validated
public class DatasetController {
    private final DatasetService datasetService;

    @Autowired
    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @PostMapping()
    public DatasetViewModel createDataset(@RequestBody @Valid DatasetCreateBindingModel datasetCreateBindingModel) {
        return datasetService.createDataset(datasetCreateBindingModel, false);
    }

    @PutMapping("/{id}")
    public DatasetViewModel updateDataset(
            @PathVariable("id") String id,
            @RequestBody @Valid DatasetUpdateBindingModel datasetUpdateBindingModel) {
        return datasetService.updateDataset(id, datasetUpdateBindingModel);
    }

    @GetMapping("/{id}")
    public DatasetViewModel getDataset(@PathVariable("id") @NotNull String id) {
        return datasetService.getDataset(id, false);
    }

    @GetMapping("/{id}/media")
    public PageReturn<MediaViewModel> getDatasetMedia(@PathVariable("id") @NotNull String id,
                                                      DatasetFilterAndPageParam filterAndPageParam) {
        return datasetService.getDatasetMedia(id, filterAndPageParam);
    }

    @GetMapping()
    public List<DatasetAllViewModel> getAllDatasets(@RequestParam(name = "org", required = false) String org) {
        return datasetService.getAllDatasets(org);
    }

    @GetMapping("/projectSuitable")
    public List<DatasetAllViewModel> getProjectSuitableDatasets(
            @RequestParam(name = "projectId", required = false) String projectId) {
        return datasetService.getProjectSuitableDatasets(projectId);
    }

    @GetMapping("/admin")
    public List<DatasetViewModel> getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin() {
        return datasetService.getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin();
    }

    @DeleteMapping("/{id}")
    public DatasetViewModel deleteDataset(@PathVariable @NotNull String id) {
        return datasetService.deleteDatasetById(id, true);
    }

    @DeleteMapping("/{id}/restore")
    public DatasetViewModel restoreDataset(@PathVariable @NotNull String id) {
        return datasetService.deleteDatasetById(id, false);
    }

    @DeleteMapping("/{id}/deleteFromDb")
    public void permanentDeleteDatasetFromDb(@PathVariable @NotNull String id) {
        datasetService.permanentDeleteDatasetFromDB(id);
    }

    @PostMapping("/{datasetId}/file")
    public MediaViewModel createImageFile(HttpServletRequest req,
                                          @PathVariable("datasetId") String datasetId) throws IOException {
        String filename = new String(Base64.getDecoder().decode(req.getHeader("X-filename")), StandardCharsets.UTF_8);

        return datasetService.createImageFile(datasetId, filename, req.getInputStream());
    }

    @PostMapping("/{datasetId}/url")
    public List<UrlImageUploadViewModel> createImagesByShareableLink(@PathVariable("datasetId") String datasetId,
                                                                     @RequestBody @Valid Set<String> imageUrlSet) {
        return datasetService.createImagesByShareableLink(datasetId, imageUrlSet, false);
    }

    @GetMapping("/{datasetId}/file")
    public List<MediaViewModel> getAllMedia(@PathVariable("datasetId") String datasetId) {
        return datasetService.getAllMedia(datasetId);
    }

    @GetMapping("/{datasetId}/project")
    public DatasetProjectViewModel getDatasetWithAllProjects(@PathVariable("datasetId") String datasetId) {
        return datasetService.getDatasetWithProjects(datasetId);
    }
}
