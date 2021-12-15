package ai.datagym.application.labelTask.repo;

import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.utils.GoogleString;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LabelTaskCustomRepository {
    List<LabelTask> search(String projectId, GoogleString googleString, LabelTaskState labelTaskState, boolean isMediaDeleted, int maxResults);

    Optional<LabelTask> findByOwnerAndStateAndProjectIdAndMediaDeletedFalse(String projectOwner, String labeler, String projectId);

    Optional<LabelTask> findByOwnerAndStateAndMediaDeletedFalse(@Param("projectOwner") String projectOwner, @Param("labeler") String labeler);

    Optional<LabelTask> findNextTaskByCompletedTaskState(String projectOwner);

    Optional<LabelTask> findNextTaskByProjectIdAndCompletedOrSkippedTaskState(String projectOwner, String projectId);

    /**
     * Updates a label task state in a performance way
     *
     * @param projectId         The specific project id
     * @param datasetId         The specific dataset id
     * @param oldLabelTaskState The old label task state (where-clause)
     * @param newLabelTaskState The new label task state
     * @return Integer of how many entries gets deleted
     */
    int updateLabelTaskStateByProjectIdAndDatasetId(String projectId, String datasetId,
                                                    LabelTaskState oldLabelTaskState, LabelTaskState newLabelTaskState);

    /**
     * Removes tasks, values and ratings based on the media ids of a specific project
     *
     * @param projectId        The specific project id
     * @param labelIterationId The specific label iteration id
     * @param mediaIds         A list with the media keys
     */
    void deleteAllLabelTasksAndData(String projectId, String labelIterationId, List<String> mediaIds);
}
