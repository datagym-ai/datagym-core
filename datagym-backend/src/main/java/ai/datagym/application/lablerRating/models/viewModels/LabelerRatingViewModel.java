package ai.datagym.application.lablerRating.models.viewModels;

public class LabelerRatingViewModel {
    private String id;
    private String projectId;
    private String labelerId;
    private int positive;
    private int negative;

    public LabelerRatingViewModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getLabelerId() {
        return labelerId;
    }

    public void setLabelerId(String labelerId) {
        this.labelerId = labelerId;
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
}
