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
@DiscriminatorValue(value = "segmentation_value")
@JsonIgnoreProperties("inspection")
public class LcEntryImageSegmentationValue extends LcEntryValue {
    @JsonManagedReference(value = "lcEntrySegmentationValue_pointCollection")
    @OneToMany(mappedBy = "lcEntryImageSegmentationValue", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "pointcollection_uuid")
    private List<PointCollection> pointsCollection = new ArrayList<>();

    public LcEntryImageSegmentationValue() {
        super();
    }

    public List<PointCollection> getPointsCollection() {
        return pointsCollection;
    }

    public void setPointsCollection(List<PointCollection> pointsCollection) {
        this.pointsCollection = pointsCollection;
    }
}
