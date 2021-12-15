package ai.datagym.application.labelIteration.entity.geometry;

import ai.datagym.application.labelIteration.entity.FrameType;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.LcEntryValueChange;
import ai.datagym.application.labelIteration.entity.converter.SimplePointsListConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue(value = "line_change_value")
public class LcEntryLineChangeValue extends LcEntryValueChange {

    @Lob
    @Convert(converter = SimplePointsListConverter.class)
    @Column(name = "points")
    private List<SimplePointPojo> points = new ArrayList<>();

    public LcEntryLineChangeValue() {
        super();
    }

    public LcEntryLineChangeValue(LcEntryValue lcEntryValue,
                                  LcEntryValue lcEntryRootValue,
                                  Integer frame, FrameType frameType,
                                  String labeler,
                                  List<SimplePointPojo> points) {
        super(lcEntryValue, lcEntryRootValue, frame, frameType, labeler);
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
        return "LcEntryLineChangeValue{" +
                "points=" + points +
                '}';
    }
}
