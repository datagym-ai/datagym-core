package ai.datagym.application.labelIteration.models.viewModels.geometry;

import java.util.ArrayList;
import java.util.List;

public class PointCollectionViewModel {
    private String id;
    private List<PointPojoViewModel> points = new ArrayList<>();

    public PointCollectionViewModel() {
    }

    public PointCollectionViewModel(String id, List<PointPojoViewModel> points) {
        this.id = id;
        this.points = points;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<PointPojoViewModel> getPoints() {
        return points;
    }

    public void setPoints(List<PointPojoViewModel> points) {
        this.points = points;
    }
}
