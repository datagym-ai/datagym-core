package ai.datagym.application.labelConfiguration.entity;

import ai.datagym.application.project.entity.Project;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "label_configuration", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LabelConfiguration implements Serializable {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;

    @JsonBackReference
    @OneToOne(mappedBy = "labelConfiguration", fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    private Project project;

    @JsonManagedReference(value = "lcEntry_labelConfiguration")
    @OneToMany(mappedBy = "configuration", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LcEntry> entries = new HashSet<>();

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    public LabelConfiguration() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<LcEntry> getEntries() {
        return entries;
    }

    public void setEntries(Set<LcEntry> entries) {
        this.entries = entries;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "LabelConfiguration{" +
                "id='" + id + '\'' +
                ", project=" + project +
                ", entries=" + entries +
                ", timestamp=" + timestamp +
                '}';
    }
}
