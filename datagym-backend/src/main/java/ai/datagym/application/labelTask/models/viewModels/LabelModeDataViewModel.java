package ai.datagym.application.labelTask.models.viewModels;

import ai.datagym.application.labelConfiguration.models.viewModels.LabelConfigurationViewModel;
import ai.datagym.application.labelIteration.models.viewModels.LabelIterationViewModel;
import ai.datagym.application.media.models.viewModels.MediaViewModel;
import ai.datagym.application.project.entity.MediaType;

public class LabelModeDataViewModel {
    private String taskId;
    private String labelTaskState;
    private String projectName;
    private LabelConfigurationViewModel labelConfig;
    private LabelIterationViewModel labelIteration;
    private MediaViewModel media;
    private String reviewComment;
    private boolean aiSegLimitReached;
    private Long lastChangedConfig;
    private boolean reviewActivated = false;
    private MediaType projectType = MediaType.IMAGE;
    private String datasetId;
    private String datasetName;

    public LabelModeDataViewModel() {
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getLabelTaskState() {
        return labelTaskState;
    }

    public void setLabelTaskState(String labelTaskState) {
        this.labelTaskState = labelTaskState;
    }

    public LabelConfigurationViewModel getLabelConfig() {
        return labelConfig;
    }

    public void setLabelConfig(LabelConfigurationViewModel labelConfig) {
        this.labelConfig = labelConfig;
    }

    public LabelIterationViewModel getLabelIteration() {
        return labelIteration;
    }

    public void setLabelIteration(LabelIterationViewModel labelIteration) {
        this.labelIteration = labelIteration;
    }

    public MediaViewModel getMedia() {
        return media;
    }

    public void setMedia(MediaViewModel media) {
        this.media = media;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public boolean isAiSegLimitReached() {
        return aiSegLimitReached;
    }

    public void setAiSegLimitReached(boolean aiSegLimitReached) {
        this.aiSegLimitReached = aiSegLimitReached;
    }

    public Long getLastChangedConfig() {
        return lastChangedConfig;
    }

    public void setLastChangedConfig(Long lastChangedConfig) {
        this.lastChangedConfig = lastChangedConfig;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public boolean isReviewActivated() {
        return reviewActivated;
    }

    public void setReviewActivated(boolean reviewActivated) {
        this.reviewActivated = reviewActivated;
    }

    public MediaType getProjectType() {
        return projectType;
    }

    public void setProjectType(MediaType projectType) {
        this.projectType = projectType;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    @Override
    public String toString() {
        return "LabelModeDataViewModel{" +
                "taskId='" + taskId + '\'' +
                ", labelTaskState='" + labelTaskState + '\'' +
                ", projectName='" + projectName + '\'' +
                ", labelConfig=" + labelConfig +
                ", labelIteration=" + labelIteration +
                ", media=" + media +
                ", reviewComment='" + reviewComment + '\'' +
                ", aiSegLimitReached=" + aiSegLimitReached +
                ", lastChangedConfig=" + lastChangedConfig +
                ", reviewActivated=" + reviewActivated +
                ", projectType=" + projectType +
                ", datasetId='" + datasetId + '\'' +
                ", datasetName='" + datasetName + '\'' +
                '}';
    }
}
