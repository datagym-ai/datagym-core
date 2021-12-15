package ai.datagym.application.labelIteration.models.change.create;

import ai.datagym.application.labelIteration.entity.geometry.SimplePointPojo;

public class PointChangeCreateBindingModel extends LcEntryChangeCreateBindingModel {

    private SimplePointPojo point;

    public SimplePointPojo getPoint() {
        return point;
    }

    public void setPoint(SimplePointPojo point) {
        this.point = point;
    }

    @Override
    public String toString() {
        return "PointChangeCreateBindingModel{" +
                "point=" + point +
                '}';
    }
}
