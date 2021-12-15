package ai.datagym.application.labelIteration.entity.geometry;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "point_collection")
public class PointCollection {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;


    @JsonManagedReference(value = "pointCollection_pointPojo")
    @OneToMany(mappedBy = "pointCollection", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "point_uuid")
    private List<PointPojo> points = new ArrayList<>();

    @JsonBackReference(value = "lcEntrySegmentationValue_pointCollection")
    @ManyToOne(targetEntity = LcEntryImageSegmentationValue.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "segmentation_value_id", referencedColumnName = "id",
    foreignKey = @ForeignKey(name = "fk_pointcollection_entryvaluesegmentation"))
    private LcEntryImageSegmentationValue lcEntryImageSegmentationValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<PointPojo> getPoints() {
        return points;
    }

    public void setPoints(List<PointPojo> points) {
        this.points = points;
    }

    public LcEntryImageSegmentationValue getLcEntryImageSegmentationValue() {
        return lcEntryImageSegmentationValue;
    }

    public void setLcEntryImageSegmentationValue(LcEntryImageSegmentationValue lcEntryImageSegmentationValue) {
        this.lcEntryImageSegmentationValue = lcEntryImageSegmentationValue;
    }
}
