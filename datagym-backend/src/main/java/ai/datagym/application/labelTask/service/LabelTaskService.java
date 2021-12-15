package ai.datagym.application.labelTask.service;

import ai.datagym.application.labelIteration.entity.LabelIteration;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskCompleteBindingModel;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskMoveAllBindingModel;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskReviewBindingModel;
import ai.datagym.application.labelTask.models.viewModels.LabelModeDataViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskCompleteViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.project.entity.Project;

import java.io.IOException;
import java.util.List;

public interface LabelTaskService {
    LabelTask createLabelTask(String projectId, String mediaId, String iterationId);

    /**
     * Creates a label task without additional security checks!
     *
     * @param project        The specific project
     * @param media          The specific media
     * @param labelIteration The specific label iteration
     * @return Instance of {@link LabelTask}
     */
    LabelTask createLabelTaskInternal(Project project, Media media, LabelIteration labelIteration);

    /**
     * Creates a label task object <b>which is not persisted! (no repository.save)!</b>. It gets called through an
     * additional transaction where a repository.save() would lead into a duplicate session error!
     *
     * @param project        The specific project
     * @param media          The specific media
     * @param labelIteration The specific label iteration
     * @return
     */
    LabelTask createLabelTaskInternalNoSave(Project project, Media media, LabelIteration labelIteration);

    /**
     * Delete all LabelTasks from the Project{@param projectById} and media, that is within {@param media}
     *
     * @param project
     * @param media
     */
    void deleteAllLabelTasksFromDataset(Project project, List<String> media);

    void deleteLabelTaskByIdInternal(String taskId);

    LabelTaskViewModel moveTaskStateIfUserIsAdmin(String labelTaskId, LabelTaskState toTaskState) throws IOException;

    void moveAllTasks(LabelTaskMoveAllBindingModel labelTaskMoveAllBindingModel);

    LabelModeDataViewModel getTask(String taskId);

    void skipTask(String taskId) throws IOException;

    LabelTaskCompleteViewModel completeTask(String taskId, LabelTaskCompleteBindingModel labelTaskCompleteBindingModel) throws IOException;

    void moveTaskToReviewed(String taskId) throws IOException;

    void reviewCompletion(LabelTaskReviewBindingModel labelTaskReviewBindingModel, boolean success) throws IOException;

    void activateBenchmark(String taskId);

    void deactivateBenchmark(String taskId);

    LabelTaskViewModel resetLabeler(String taskId);
}
