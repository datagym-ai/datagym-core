package ai.datagym.application.labelTask.service;

import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.labelTask.models.viewModels.UserTaskViewModel;

import java.util.List;

public interface UserTaskService {
    List<UserTaskViewModel> getUserTasks();

    LabelTaskViewModel getNextTask(String projectId);

    Integer getNumberOfCompletedTasks(String configId);

    Integer getNumberOfReviewedTasks(String configId);

    void changeTaskStateAfterLabelConfigurationUpdate(String configId);

    void checkIfDummyProjectIsCreated(String owner);

    LabelTaskViewModel getNextReviewTask(String projectId);
}
