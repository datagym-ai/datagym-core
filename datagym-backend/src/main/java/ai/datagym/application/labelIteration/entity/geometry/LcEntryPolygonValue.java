package ai.datagym.application.labelIteration.entity.geometry;

import ai.datagym.application.labelIteration.entity.LcEntryValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue(value = "poly_value")
@JsonIgnoreProperties("inspection")
public class LcEntryPolygonValue extends LcEntryValue {
    @JsonManagedReference(value = "lcEntryPolyValue_pointPojo")
    @OneToMany(mappedBy = "lcEntryPolygonValue", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "point_uuid")
    private List<PointPojo> points = new ArrayList<>();

    public LcEntryPolygonValue() {
        super();
    }

    public List<PointPojo> getPoints() {
        return points;
    }

    public void setPoints(List<PointPojo> points) {
        this.points = points;
    }
}
