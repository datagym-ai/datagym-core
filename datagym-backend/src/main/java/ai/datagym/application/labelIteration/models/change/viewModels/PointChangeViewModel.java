package ai.datagym.application.labelIteration.models.change.viewModels;

import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelIteration.entity.FrameType;
import ai.datagym.application.labelIteration.entity.geometry.SimplePointPojo;

public class PointChangeViewModel extends LcEntryChangeViewModel {

    private SimplePointPojo point;

    public PointChangeViewModel(String id, Integer frame, FrameType frameType,
                                SimplePointPojo point) {
        super(id, frame, frameType, LcEntryType.POINT);
        this.point = point;
    }

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
