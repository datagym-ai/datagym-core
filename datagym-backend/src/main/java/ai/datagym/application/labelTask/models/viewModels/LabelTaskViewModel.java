package ai.datagym.application.labelTask.models.viewModels;

import ai.datagym.application.labelTask.entity.PreLabelState;

public class LabelTaskViewModel {
    private String taskId;
    private String projectId;
    private String projectName;
    private String labelTaskState;
    private String labelTaskType;
    private PreLabelState preLabelState;
    private String mediaId;
    private String mediaName;
    private String labeler;
    private String iterationId;
    private String reviewComment;
    private boolean isBenchmark = false;


    private boolean hasJsonUpload = false;
    private int iterationRun;


    public LabelTaskViewModel() {
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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

    public String getLabelTaskState() {
        return labelTaskState;
    }

    public void setLabelTaskState(String labelTaskState) {
        this.labelTaskState = labelTaskState;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getLabeler() {
        return labeler;
    }

    public void setLabeler(String labeler) {
        this.labeler = labeler;
    }

    public String getIterationId() {
        return iterationId;
    }

    public void setIterationId(String iterationId) {
        this.iterationId = iterationId;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public int getIterationRun() {
        return iterationRun;
    }

    public void setIterationRun(int iterationRun) {
        this.iterationRun = iterationRun;
    }

    public boolean isBenchmark() {
        return isBenchmark;
    }

    public void setBenchmark(boolean benchmark) {
        isBenchmark = benchmark;
    }

    public boolean isHasJsonUpload() { return hasJsonUpload; }

    public void setHasJsonUpload(boolean hasJsonUpload) { this.hasJsonUpload = hasJsonUpload; }

    public String getLabelTaskType() {
        return labelTaskType;
    }

    public void setLabelTaskType(String labelTaskType) {
        this.labelTaskType = labelTaskType;
    }

    public PreLabelState getPreLabelState() {
        return preLabelState;
    }

    public void setPreLabelState(PreLabelState preLabelState) {
        this.preLabelState = preLabelState;
    }
}
