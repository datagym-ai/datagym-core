package ai.datagym.application.labelIteration.models.change.update;

import ai.datagym.application.labelIteration.entity.geometry.SimplePointPojo;

import java.util.List;

public class LineChangeUpdateBindingModel extends LcEntryChangeUpdateBindingModel {

    private List<SimplePointPojo> points;

    public List<SimplePointPojo> getPoints() {
        return points;
    }

    public void setPoints(List<SimplePointPojo> points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "LineChangeCreateBindingModel{" +
                "points=" + points +
                '}';
    }
}
