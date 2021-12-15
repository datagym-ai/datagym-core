package ai.datagym.application.limit.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "limits", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
public class Limit {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;

    @Column(name = "organisation_id", nullable = false)
    private String organisationId;

    @Column(name = "project_limit", nullable = false)
    private int projectLimit;

    @Column(name = "project_used", nullable = false)
    private int projectUsed;

    @Column(name = "label_limit", nullable = false)
    private int labelLimit;

    @Column(name = "label_remaining", nullable = false)
    private int labelRemaining;

    @Column(name = "storage_limit", nullable = false)
    private long storageLimit;

    @Column(name = "storage_used", nullable = false)
    private long storageUsed;

    @Column(name = "ai_seg_limit", nullable = false)
    private int aiSegLimit;

    @Column(name = "ai_seg_remaining", nullable = false)
    private int aiSegRemaining;

    @Column(name = "api_access", nullable = false)
    private boolean apiAccess;

    @Column(name = "external_storage", nullable = false)
    private boolean externalStorage;

    @Column(name = "last_reset")
    private Long lastReset;

    @Column(name = "timestamp", nullable = false)
    private long timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_plan_type",
            nullable = false,
            columnDefinition = "ENUM('FREE_DEVELOPER','DEVELOPER_PRO', 'TEAM_PRO') default 'FREE_DEVELOPER'")
    private DataGymPlan dataGymPlan = DataGymPlan.FREE_DEVELOPER;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(String organisationId) {
        this.organisationId = organisationId;
    }

    public int getProjectLimit() {
        return projectLimit;
    }

    public void setProjectLimit(int projectLimit) {
        this.projectLimit = projectLimit;
    }

    public int getProjectUsed() {
        return projectUsed;
    }

    public void setProjectUsed(int projectUsed) {
        this.projectUsed = projectUsed;
    }

    public int getLabelLimit() {
        return labelLimit;
    }

    public void setLabelLimit(int labelLimit) {
        this.labelLimit = labelLimit;
    }

    public int getLabelRemaining() {
        return labelRemaining;
    }

    public void setLabelRemaining(int labelRemaining) {
        this.labelRemaining = labelRemaining;
    }

    public long getStorageLimit() {
        return storageLimit;
    }

    public void setStorageLimit(long storageLimit) {
        this.storageLimit = storageLimit;
    }

    public long getStorageUsed() {
        return storageUsed;
    }

    public void setStorageUsed(long storageUsed) {
        this.storageUsed = storageUsed;
    }

    public int getAiSegLimit() {
        return aiSegLimit;
    }

    public void setAiSegLimit(int aiSegLimit) {
        this.aiSegLimit = aiSegLimit;
    }

    public int getAiSegRemaining() {
        return aiSegRemaining;
    }

    public void setAiSegRemaining(int aiSegRemaining) {
        this.aiSegRemaining = aiSegRemaining;
    }

    public boolean isApiAccess() {
        return apiAccess;
    }

    public void setApiAccess(boolean apiAccess) {
        this.apiAccess = apiAccess;
    }

    public boolean isExternalStorage() {
        return externalStorage;
    }

    public void setExternalStorage(boolean externalStorage) {
        this.externalStorage = externalStorage;
    }

    public Long getLastReset() {
        return lastReset;
    }

    public void setLastReset(Long lastReset) {
        this.lastReset = lastReset;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public DataGymPlan getDataGymPlan() {
        return dataGymPlan;
    }

    public void setDataGymPlan(DataGymPlan dataGymPlan) {
        this.dataGymPlan = dataGymPlan;
    }
}
