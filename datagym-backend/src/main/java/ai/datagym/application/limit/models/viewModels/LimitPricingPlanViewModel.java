package ai.datagym.application.limit.models.viewModels;

public class LimitPricingPlanViewModel {
    private String id;
    private String organisationId;
    private String pricingPlanType;
    private int projectLimit;
    private int projectUsed;
    private int labelLimit;
    private int labelRemaining;
    private long storageLimit;
    private long storageUsed;
    private int aiSegLimit;
    private int aiSegRemaining;
    private boolean apiAccess;
    private boolean externalStorage;
    private Long lastReset;
    private long timestamp;

    public LimitPricingPlanViewModel() {
    }

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

    public String getPricingPlanType() {
        return pricingPlanType;
    }

    public void setPricingPlanType(String pricingPlanType) {
        this.pricingPlanType = pricingPlanType;
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
}
