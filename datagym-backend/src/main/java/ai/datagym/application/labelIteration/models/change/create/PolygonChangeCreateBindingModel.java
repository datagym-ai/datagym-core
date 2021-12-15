package ai.datagym.application.labelIteration.models.change.create;

import ai.datagym.application.labelIteration.entity.geometry.SimplePointPojo;

import java.util.List;

public class PolygonChangeCreateBindingModel extends LcEntryChangeCreateBindingModel {

    private List<SimplePointPojo> points;

    public List<SimplePointPojo> getPoints() {
        return points;
    }

    public void setPoints(List<SimplePointPojo> points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "PolygonChangeCreateBindingModel{" +
                "points=" + points +
                '}';
    }
}
