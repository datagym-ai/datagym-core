package ai.datagym.application.dataset.entity;

import ai.datagym.application.media.entity.Media;
import ai.datagym.application.project.entity.MediaType;
import ai.datagym.application.project.entity.Project;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "dataset", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
public class Dataset {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "short_description", length = 128)
    private String shortDescription;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted;

    @Column(name = "delete_time")
    private Long deleteTime;

    @JsonManagedReference
    @ManyToMany()
    @JoinTable(name = "dataset_media",
            joinColumns = @JoinColumn(name = "dataset_id", referencedColumnName = "id"),
            foreignKey = @ForeignKey(name = "fk_dataset_media"),
            inverseJoinColumns = @JoinColumn(name = "media_id", referencedColumnName = "id"),
            inverseForeignKey = @ForeignKey(name = "fk_media_dataset"))
    private Set<Media> media = new HashSet<>();

    @JsonBackReference
    @ManyToMany(mappedBy = "datasets", targetEntity = Project.class)
    private Set<Project> projects = new HashSet<>();

    @Column(name = "owner_id", nullable = false)
    private String owner;

    @JsonManagedReference(value = "dataset_aws_credentials")
    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DatasetAwsS3UserCredentials> datasetAwsS3UserCredentials = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false,
            columnDefinition = "ENUM('IMAGE', 'VIDEO') default 'IMAGE'")
    private MediaType mediaType = MediaType.IMAGE;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Long getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Long deleteTime) {
        this.deleteTime = deleteTime;
    }

    public Set<Media> getMedia() {
        return media;
    }

    public void setMedia(Set<Media> media) {
        this.media = media;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<DatasetAwsS3UserCredentials> getAwsS3Credentials() {
        return datasetAwsS3UserCredentials;
    }

    public void setAwsS3Credentials(List<DatasetAwsS3UserCredentials> datasetAwsS3UserCredentials) {
        this.datasetAwsS3UserCredentials = datasetAwsS3UserCredentials;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String toString() {
        return "Dataset{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", timestamp=" + timestamp +
                ", deleted=" + deleted +
                ", deleteTime=" + deleteTime +
                ", media=" + media +
                ", projects=" + projects +
                ", owner='" + owner + '\'' +
                ", awsS3Credentials=" + datasetAwsS3UserCredentials +
                ", mediaType=" + mediaType +
                '}';
    }
}
