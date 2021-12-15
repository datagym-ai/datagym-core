package ai.datagym.application.project.models.viewModels;

import ai.datagym.application.labelIteration.models.viewModels.geometry.IGeometryCountByDayViewModel;

import java.util.List;

public class ProjectLabelCountByDayViewModel {
    private long labelCountTotal;
    private List<IGeometryCountByDayViewModel> labelCounts;


    public ProjectLabelCountByDayViewModel(long geometryCountTotal, List<IGeometryCountByDayViewModel> geometryCounts) {
        this.labelCountTotal = geometryCountTotal;
        this.labelCounts = geometryCounts;
    }

    public long getLabelCountTotal() {
        return labelCountTotal;
    }

    public void setLabelCountTotal(long labelCountTotal) {
        this.labelCountTotal = labelCountTotal;
    }

    public List<IGeometryCountByDayViewModel> getLabelCounts() {
        return labelCounts;
    }

    public void setLabelCounts(List<IGeometryCountByDayViewModel> labelCounts) {
        this.labelCounts = labelCounts;
    }
}
