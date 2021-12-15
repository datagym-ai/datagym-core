package ai.datagym.application.labelIteration.models.bindingModels;

import java.util.ArrayList;
import java.util.List;

public class PointCollectionBindingModel {
    private String id;
    private List<PointPojoBindingModel> points = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<PointPojoBindingModel> getPoints() {
        return points;
    }

    public void setPoints(List<PointPojoBindingModel> points) {
        this.points = points;
    }
}
