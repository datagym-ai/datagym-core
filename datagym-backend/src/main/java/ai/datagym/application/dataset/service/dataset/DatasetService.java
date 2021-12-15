package ai.datagym.application.dataset.service.dataset;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetCreateBindingModel;
import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetFilterAndPageParam;
import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetUpdateBindingModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetAllViewModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetProjectViewModel;
import ai.datagym.application.dataset.models.dataset.viewModels.DatasetViewModel;
import ai.datagym.application.media.models.viewModels.MediaViewModel;
import ai.datagym.application.media.models.viewModels.UrlImageUploadViewModel;
import ai.datagym.application.utils.PageReturn;
import com.eforce21.lib.bin.file.entity.BinFileEntity;

import javax.servlet.ServletInputStream;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;

public interface DatasetService {
    DatasetViewModel createDataset(DatasetCreateBindingModel datasetCreateBindingModel, boolean createDummyDataset);

    DatasetViewModel updateDataset(String id, DatasetUpdateBindingModel datasetUpdateBindingModel);

    DatasetViewModel getDataset(String id, boolean includeMedia);

    PageReturn<MediaViewModel> getDatasetMedia(String id, DatasetFilterAndPageParam filterAndPageParam);

    List<DatasetAllViewModel> getAllDatasets(String org);

    List<DatasetAllViewModel> getProjectSuitableDatasets(String projectId);

    List<DatasetAllViewModel> getAllDatasetsWithoutOrg();

    DatasetViewModel deleteDatasetById(String id, boolean deleteDataset);

    void permanentDeleteDatasetFromDB(String id);

    void permanentDeleteDatasetFromDB(Dataset dataset, boolean isCronJob);

    boolean isDatasetNameUnique(String projectName);

    boolean isDatasetNameUniqueAndDeletedFalse(String datasetName, String owner);

    MediaViewModel createImageFile(String datasetId, String filename, ServletInputStream inputStream);

    BufferedImage getBufferedImageFromEntity(BinFileEntity binFileEntity);

    List<MediaViewModel> getAllMedia(String datasetId);

    DatasetProjectViewModel getDatasetWithProjects(String datasetId);

    List<DatasetViewModel> getAllDatasetsFromOrganisationAndLoggedInUserIsAdmin();

    List<UrlImageUploadViewModel> createImagesByShareableLink(String datasetId, Set<String> imageUrlSet, boolean dummyProjectImages);
}
