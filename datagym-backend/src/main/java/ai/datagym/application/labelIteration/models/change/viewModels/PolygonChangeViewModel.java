package ai.datagym.application.labelIteration.models.change.viewModels;

import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelIteration.entity.FrameType;
import ai.datagym.application.labelIteration.entity.geometry.SimplePointPojo;

import java.util.List;

public class PolygonChangeViewModel extends LcEntryChangeViewModel {

    private List<SimplePointPojo> points;

    public PolygonChangeViewModel(String id, Integer frame,
                                  FrameType frameType,
                                  List<SimplePointPojo> points) {
        super(id, frame, frameType, LcEntryType.POLYGON);
        this.points = points;
    }

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
