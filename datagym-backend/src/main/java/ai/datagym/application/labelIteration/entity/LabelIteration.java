package ai.datagym.application.labelIteration.entity;

import ai.datagym.application.labelTask.entity.LabelTask;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "label_iteration", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LabelIteration {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;

    @JsonBackReference
    @OneToOne(mappedBy = "labelIteration", fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    private Project project;

    @JsonManagedReference(value = "entryValue_labelIteration")
    @OneToMany(mappedBy = "labelIteration", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LcEntryValue> entryValues = new HashSet<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "labelIteration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabelTask> labelTasks = new ArrayList<>();

    @Column(name = "run", nullable = false)
    private Integer run;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

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

    public Integer getRun() {
        return run;
    }

    public void setRun(Integer run) {
        this.run = run;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Set<LcEntryValue> getEntryValues() {
        return entryValues;
    }

    public void setEntryValues(Set<LcEntryValue> entryValues) {
        this.entryValues = entryValues;
    }

    public List<LabelTask> getLabelTasks() {
        return labelTasks;
    }

    public void setLabelTasks(List<LabelTask> labelTasks) {
        this.labelTasks = labelTasks;
    }
}
