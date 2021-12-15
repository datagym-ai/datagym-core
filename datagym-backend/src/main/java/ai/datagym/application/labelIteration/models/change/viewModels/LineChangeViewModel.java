package ai.datagym.application.labelIteration.models.change.viewModels;

import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelIteration.entity.FrameType;
import ai.datagym.application.labelIteration.entity.geometry.SimplePointPojo;

import java.util.List;

public class LineChangeViewModel extends LcEntryChangeViewModel {

    private List<SimplePointPojo> points;

    public LineChangeViewModel(String id, Integer frame, FrameType frameType,
                               List<SimplePointPojo> points) {
        super(id, frame, frameType, LcEntryType.LINE);
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
        return "LineChangeCreateBindingModel{" +
                "points=" + points +
                '}';
    }
}
