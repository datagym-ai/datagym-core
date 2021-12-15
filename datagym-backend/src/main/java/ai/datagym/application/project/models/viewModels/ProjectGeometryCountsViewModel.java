package ai.datagym.application.project.models.viewModels;

import ai.datagym.application.labelIteration.models.viewModels.geometry.IGeometryCountViewModel;

import java.util.List;

public class ProjectGeometryCountsViewModel {
    private long geometryCountTotal;
    private List<IGeometryCountViewModel> geometryCounts;


    public ProjectGeometryCountsViewModel(long geometryCountTotal, List<IGeometryCountViewModel> geometryCounts) {
        this.geometryCountTotal = geometryCountTotal;
        this.geometryCounts = geometryCounts;
    }

    public long getGeometryCountTotal() {
        return geometryCountTotal;
    }

    public void setGeometryCountTotal(long geometryCountTotal) {
        this.geometryCountTotal = geometryCountTotal;
    }

    public List<IGeometryCountViewModel> getGeometryCounts() {
        return geometryCounts;
    }

    public void setGeometryCounts(List<IGeometryCountViewModel> geometryCounts) {
        this.geometryCounts = geometryCounts;
    }
}
