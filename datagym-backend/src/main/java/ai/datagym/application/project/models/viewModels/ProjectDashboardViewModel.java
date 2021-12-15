package ai.datagym.application.project.models.viewModels;

import ai.datagym.application.dataset.models.dataset.viewModels.DatasetMediaStatusViewModel;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.media.entity.MediaSourceType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectDashboardViewModel extends ProjectViewModel {
    private int countTasks;
    private int countDatasets;
    private long approvedReviewPerformance;
    private long declinedReviewPerformance;
    private long countInvalidImages;
    private boolean hasLabelConfiguration;
    private String currentPlan;
    private Map<LabelTaskState, Long> taskStatus;
    private Map<MediaSourceType, Long> taskMediaDetail;
    private List<DatasetMediaStatusViewModel> datasetMediaStatuses;

    public ProjectDashboardViewModel() {
        this.taskStatus = new HashMap<>();
        this.taskMediaDetail = new HashMap<>();
    }

    public int getCountTasks() {
        return countTasks;
    }

    public void setCountTasks(int countTasks) {
        this.countTasks = countTasks;
    }

    public int getCountDatasets() {
        return countDatasets;
    }

    public void setCountDatasets(int countDatasets) {
        this.countDatasets = countDatasets;
    }

    public long getApprovedReviewPerformance() {
        return approvedReviewPerformance;
    }

    public void setApprovedReviewPerformance(long approvedReviewPerformance) {
        this.approvedReviewPerformance = approvedReviewPerformance;
    }

    public long getDeclinedReviewPerformance() {
        return declinedReviewPerformance;
    }

    public void setDeclinedReviewPerformance(long declinedReviewPerformance) {
        this.declinedReviewPerformance = declinedReviewPerformance;
    }

    public boolean isHasLabelConfiguration() {
        return hasLabelConfiguration;
    }

    public void setHasLabelConfiguration(boolean hasLabelConfiguration) {
        this.hasLabelConfiguration = hasLabelConfiguration;
    }

    public Map<LabelTaskState, Long> getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(Map<LabelTaskState, Long> taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Map<MediaSourceType, Long> getTaskMediaDetail() {
        return taskMediaDetail;
    }

    public void setTaskMediaDetail(Map<MediaSourceType, Long> taskMediaDetail) {
        this.taskMediaDetail = taskMediaDetail;
    }

    public String getCurrentPlan() {
        return currentPlan;
    }

    public void setCurrentPlan(String currentPlan) {
        this.currentPlan = currentPlan;
    }

    public List<DatasetMediaStatusViewModel> getDatasetMediaStatuses() {
        return datasetMediaStatuses;
    }

    public void setDatasetMediaStatuses(List<DatasetMediaStatusViewModel> datasetMediaStatuses) {
        this.datasetMediaStatuses = datasetMediaStatuses;
    }

    public long getCountInvalidImages() {
        return countInvalidImages;
    }

    public void setCountInvalidImages(long countInvalidImages) {
        this.countInvalidImages = countInvalidImages;
    }
}
