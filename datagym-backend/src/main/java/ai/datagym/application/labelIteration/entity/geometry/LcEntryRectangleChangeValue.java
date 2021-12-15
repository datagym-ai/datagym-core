package ai.datagym.application.labelIteration.entity.geometry;

import ai.datagym.application.labelIteration.entity.FrameType;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.LcEntryValueChange;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "rectangle_change_value")
public class LcEntryRectangleChangeValue extends LcEntryValueChange {
    @Column(name = "x")
    private Double x;

    @Column(name = "y")
    private Double y;

    @Column(name = "width")
    private Double width;

    @Column(name = "height")
    private Double height;

    public LcEntryRectangleChangeValue() {
        super();
    }

    public LcEntryRectangleChangeValue(LcEntryValue lcEntryValue,
                                       LcEntryValue lcEntryRootValue,
                                       Integer frame, FrameType frameType,
                                       String labeler, Double x, Double y, Double width,
                                       Double height) {
        super(lcEntryValue, lcEntryRootValue, frame, frameType, labeler);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "LcEntryRectangleChangeValue{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
