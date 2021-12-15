package ai.datagym.application.labelTask.models.viewModels;

public class UserTaskViewModel {
    private String projectId;
    private String projectName;
    private String owner;
    private long countWaitingTasks;
    private long countTasksToReview;

    public UserTaskViewModel() {
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getCountWaitingTasks() {
        return countWaitingTasks;
    }

    public void setCountWaitingTasks(long countWaitingTasks) {
        this.countWaitingTasks = countWaitingTasks;
    }

    public long getCountTasksToReview() {
        return countTasksToReview;
    }

    public void setCountTasksToReview(long countTasksToReview) {
        this.countTasksToReview = countTasksToReview;
    }
}
