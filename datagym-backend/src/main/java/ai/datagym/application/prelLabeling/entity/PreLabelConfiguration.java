package ai.datagym.application.prelLabeling.entity;

import ai.datagym.application.project.entity.Project;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
import java.util.List;

@Entity
@Table(name = "pre_label_configuration", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
public class PreLabelConfiguration {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;

    @Column(name = "activate_state", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean activateState = false;

    @JsonBackReference
    @OneToOne(mappedBy = "preLabelConfiguration", fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    private Project project;

    @JsonManagedReference(value = "preLabelConfig_preLabelMapping")
    @OneToMany(mappedBy = "preLabelConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PreLabelMappingEntry> mappings = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActivateState() {
        return activateState;
    }

    public void setActivateState(boolean activateState) {
        this.activateState = activateState;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<PreLabelMappingEntry> getMappings() {
        return mappings;
    }

    public void setMappings(List<PreLabelMappingEntry> mappings) {
        this.mappings = mappings;
    }
}
