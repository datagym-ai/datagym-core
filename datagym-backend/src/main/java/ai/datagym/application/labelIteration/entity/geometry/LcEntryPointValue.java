package ai.datagym.application.labelIteration.entity.geometry;

import ai.datagym.application.labelIteration.entity.LcEntryValue;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "point_value")
public class LcEntryPointValue extends LcEntryValue {
    @Column(name = "x")
    private Double x;

    @Column(name = "y")
    private Double y;

    public LcEntryPointValue() {
        super();
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
}
