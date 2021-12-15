package ai.datagym.application.lablerRating.entity;

import ai.datagym.application.media.entity.Media;
import ai.datagym.application.project.entity.Project;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "labeler_rating", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
public class LabelerRating {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;

    @JsonBackReference(value = "labeler_rating_project")
    @ManyToOne(optional = false, targetEntity = Project.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_labeler_rating_project"))
    private Project project;

    @JsonBackReference(value = "labeler_rating_media")
    @ManyToOne(optional = false, targetEntity = Media.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_labeler_rating_media"))
    private Media media;

    @Column(name = "labeler_id", nullable = false)
    private String labeler;

    @Column(name = "positive")
    private int positive;

    @Column(name = "negative")
    private int negative;

    public LabelerRating() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public String getLabeler() {
        return labeler;
    }

    public void setLabeler(String labeler) {
        this.labeler = labeler;
    }

    public int getPositive() {
        return positive;
    }

    public void setPositive(int positive) {
        this.positive = positive;
    }

    public int getNegative() {
        return negative;
    }

    public void setNegative(int negative) {
        this.negative = negative;
    }

    @Override
    public String toString() {
        return "LabelerRating{" +
                "id='" + id + '\'' +
                ", project=" + project +
                ", media=" + media +
                ", labeler='" + labeler + '\'' +
                ", positive=" + positive +
                ", negative=" + negative +
                '}';
    }
}
