package ai.datagym.application.limit.models;

import ai.datagym.application.limit.entity.DataGymPlan;

public class DataGymPlanDetails {
    private DataGymPlan type;

    private int projects;

    private int labels;

    // Storage in GB
    private int storage;

    private int aiseg;

    private boolean apiAccess;

    private boolean externalStorage;

    public DataGymPlanDetails(DataGymPlan type) {
        this.type = type;
    }

    public DataGymPlan getType() {
        return type;
    }

    public void setType(DataGymPlan type) {
        this.type = type;
    }

    public int getProjects() {
        return projects;
    }

    public void setProjects(int projects) {
        this.projects = projects;
    }

    public int getLabels() {
        return labels;
    }

    public void setLabels(int labels) {
        this.labels = labels;
    }

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
    }

    public int getAiseg() {
        return aiseg;
    }

    public void setAiseg(int aiseg) {
        this.aiseg = aiseg;
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

    @Override
    public String toString() {
        return "DataGymPlan{" +
                "type=" + type +
                ", projects=" + projects +
                ", labels=" + labels +
                ", storage=" + storage +
                ", aiseg=" + aiseg +
                ", apiAccess=" + apiAccess +
                ", externalStorage=" + externalStorage +
                '}';
    }
}
