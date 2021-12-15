package ai.datagym.application.projectReviewer.models.viewModels;

import ai.datagym.application.security.models.viewModles.UserMinInfoViewModel;

public class ProjectReviewerViewModel {
    private String reviewerId;
    private UserMinInfoViewModel userInfo;
    private String projectId;
    private Long timeStamp;

    public ProjectReviewerViewModel() {
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public UserMinInfoViewModel getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserMinInfoViewModel userInfo) {
        this.userInfo = userInfo;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
