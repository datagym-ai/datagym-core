package ai.datagym.application.labelIteration.entity.geometry;

import ai.datagym.application.labelIteration.entity.FrameType;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.LcEntryValueChange;
import ai.datagym.application.labelIteration.entity.converter.SimplePointsConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "point_change_value")
public class LcEntryPointChangeValue extends LcEntryValueChange {
    @Convert(converter = SimplePointsConverter.class)
    @Column(name = "point")
    private SimplePointPojo point;

    public LcEntryPointChangeValue() {
        super();
    }

    public LcEntryPointChangeValue(LcEntryValue lcEntryValue,
                                   LcEntryValue lcEntryRootValue,
                                   Integer frame, FrameType frameType,
                                   String labeler,
                                   SimplePointPojo point) {
        super(lcEntryValue, lcEntryRootValue, frame, frameType, labeler);
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
        return "LcEntryPointChangeValue{" +
                "point=" + point +
                '}';
    }
}
