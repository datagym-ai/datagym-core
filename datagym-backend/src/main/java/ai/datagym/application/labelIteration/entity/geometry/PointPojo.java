package ai.datagym.application.labelIteration.entity.geometry;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "point_pojo", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
public class PointPojo {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;

    @Column(name = "x")
    private Double x;

    @Column(name = "y")
    private Double y;

    @JsonBackReference(value = "lcEntryLineValue_pointPojo")
    @ManyToOne(targetEntity = LcEntryLineValue.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "line_value_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_pointpojo_entryvalueline"))
    private LcEntryLineValue lcEntryLineValue;

    @JsonBackReference(value = "lcEntryPolyValue_pointPojo")
    @ManyToOne(targetEntity = LcEntryPolygonValue.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "polygon_value_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_pointpojo_entryvaluepoly"))
    private LcEntryPolygonValue lcEntryPolygonValue;

    @JsonBackReference(value = "pointCollection_pointPojo")
    @ManyToOne(targetEntity = PointCollection.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "point_collection_id", referencedColumnName = "id",
     foreignKey = @ForeignKey(name = "fk_pointpojo_pointcollection"))
    private PointCollection pointCollection;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public LcEntryLineValue getLcEntryLineValue() {
        return lcEntryLineValue;
    }

    public void setLcEntryLineValue(LcEntryLineValue lcEntryLineValue) {
        this.lcEntryLineValue = lcEntryLineValue;
    }

    public LcEntryPolygonValue getLcEntryPolygonValue() {
        return lcEntryPolygonValue;
    }

    public void setLcEntryPolygonValue(LcEntryPolygonValue lcEntryPolygonValue) {
        this.lcEntryPolygonValue = lcEntryPolygonValue;
    }

    public PointCollection getPointCollection() {
        return pointCollection;
    }

    public void setPointCollection(PointCollection pointCollection) {
        this.pointCollection = pointCollection;
    }
}
