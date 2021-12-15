package ai.datagym.application.labelTask.repo;

import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.entity.LabelTaskType;
import ai.datagym.application.labelTask.entity.PreLabelState;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.media.entity.MediaSourceType;
import ai.datagym.application.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface LabelTaskRepository extends JpaRepository<LabelTask, String>, LabelTaskCustomRepository {
    List<LabelTask> findAllByProjectIdAndLabelTaskStateAndMediaDeleted(String projectId, LabelTaskState labelTaskState, boolean isMediaDeleted);


    @Modifying
    @Query("update LabelTask lt set lt.labelTaskState = :newLabelTaskState where " +
            "lt.project.id = :projectId and " +
            "lt.labelTaskState = :oldLabelTaskState"
    )
    void updateLabelTaskStateByProjectId(@Param("projectId") String projectId,
                                         @Param("oldLabelTaskState") LabelTaskState oldLabelTaskState,
                                         @Param("newLabelTaskState") LabelTaskState newLabelTaskState);

    @Modifying
    @Query("update LabelTask lt set lt.preLabelState = 'WAITING' where " +
            "lt.project.id = :projectId and " +
            "lt.preLabelState IS NULL AND " +
            "lt.labelTaskType = 'DEFAULT' AND " +
            "(lt.labelTaskState = 'WAITING' OR lt.labelTaskState = 'WAITING_CHANGED')"
    )
    void setPrelabelStateWaitingForPrelabeling(@Param("projectId") String projectId);

    @Modifying
    @Query("update LabelTask lt set lt.preLabelState = null where " +
            "lt.project.id = :projectId and " +
            "lt.labelTaskType = 'DEFAULT' AND " +
            "lt.preLabelState = 'WAITING' "
    )
    void setPreLabelStateNullForWaiting(@Param("projectId") String projectId);

    Optional<LabelTask> findByProjectIdAndMediaIdAndLabelIterationIdAndLabelTaskType(String projectId,
                                                                                     String mediaId,
                                                                                     String iterationId,
                                                                                     LabelTaskType labelTaskType);

    @Query(value = "SELECT lt FROM LabelTask AS lt" +
            " WHERE lt.project.labelConfiguration.id = :configId AND" +
            " lt.labelTaskState IN :labelTaskStateList ")
    List<LabelTask> findTasksByLabelConfigurationIdAndTaskState(
            @Param("configId") String configId,
            @Param("labelTaskStateList") List<LabelTaskState> labelTaskStateList);

    @Query(value = "SELECT lt FROM LabelTask AS lt" +
            " WHERE lt.project.id = :projectId AND " +
            " (lt.labelTaskType = 'DEFAULT' OR lt.labelTaskType = 'BENCHMARK_MASTER') AND " +
            " lt.labelTaskState IN :labelTaskStateList AND " +
            "lt.media.deleted = :isMediaDeleted")
    List<LabelTask> findTasksByProjectIdAndTaskStateAndMediaDeleted(
            @Param("projectId") String projectId,
            @Param("labelTaskStateList") List<LabelTaskState> labelTaskStateList,
            @Param("isMediaDeleted") boolean isMediaDeleted);

    @Query(value = "SELECT count(lt) FROM LabelTask AS lt" +
            " WHERE lt.project.id = :projectId AND " +
            " (lt.labelTaskType = 'DEFAULT' OR lt.labelTaskType = 'BENCHMARK_MASTER') AND " +
            " (lt.labelTaskState = 'COMPLETED' OR  lt.labelTaskState = 'SKIPPED' OR lt.labelTaskState = 'REVIEWED' ) AND " +
            "lt.media.deleted = false")
    long countTasksByProjectIdAndTaskStateAndMediaDeleted(@Param("projectId") String projectId);

    @Query(value = "SELECT count(lt) FROM LabelTask AS lt" +
            " WHERE lt.project.id = :projectId AND " +
            " (lt.labelTaskType = 'DEFAULT' OR lt.labelTaskType = 'BENCHMARK_MASTER') AND " +
            " (lt.labelTaskState =  'WAITING' OR lt.labelTaskState = 'WAITING_CHANGED' OR " +
            " (lt.labelTaskState = 'IN_PROGRESS' AND lt.labeler = :labelerId)) AND " +
            " lt.media.deleted = FALSE")
    long countPossibleTasksToLabelForLabeler(
            @Param("projectId") String projectId,
            @Param("labelerId") String labelerId);

    @Query(value = "SELECT count(lt) FROM LabelTask AS lt" +
            " WHERE lt.project.id = :projectId AND " +
            " (lt.labelTaskType = 'DEFAULT' OR lt.labelTaskType = 'BENCHMARK_MASTER') AND " +
            " (lt.labelTaskState =  'SKIPPED' OR lt.labelTaskState = 'COMPLETED') AND " +
            " lt.media.deleted = FALSE AND lt.isBenchmark = FALSE")
    long countPossibleTasksToReview(@Param("projectId") String projectId);

    @Query(value = "SELECT lt FROM LabelTask AS lt JOIN lt.media as i JOIN i.datasets as d" +
            " WHERE lt.project.id = :projectId AND d.id = :datasetId")
    List<LabelTask> findAllByProjectIdAndDatasetId(@Param("projectId") String projectId,
                                                   @Param("datasetId") String datasetId);


    Optional<LabelTask> findLabelTaskByMediaAndProjectAndLabelerAndLabelTaskType(Media media, Project project, String labelerId, LabelTaskType labelTaskType);

    @Query(value = "SELECT lt FROM LabelTask AS lt " +
            " WHERE lt.project.id = :projectId AND " +
            "lt.project.owner = :projectOwner AND " +
            "lt.isBenchmark = :isBenchmark")
    List<LabelTask> findLabelTasksByProjectIdAndProjectOwnerAndIsTaskBenchmark(@Param("projectId") String projectId,
                                                                               @Param("projectOwner") String projectOwner,
                                                                               @Param("isBenchmark") boolean isBenchmark);

    Optional<LabelTask> findFirstByPreLabelStateAndLabelTaskTypeAndLabelTaskStateAndMediaDeleted(PreLabelState prelabelState,
                                                                                                 LabelTaskType labelTaskType,
                                                                                                 LabelTaskState labelTaskState,
                                                                                                 boolean isMediaDeleted);

    @Query(value = "SELECT count(lt) FROM LabelTask as lt " +
            "WHERE lt.project.id = :projectId AND lt.media.deleted = false")
    int countProjectTasksWhereMediasNotDeleted(@Param("projectId") String projectId);

    @Query(value = "SELECT count(lt) FROM LabelTask as lt " +
            "WHERE lt.project.id = :projectId AND lt.media.deleted = false AND lt.labelTaskState = :taskState")
    long countProjectTasksByStateWhereMediasNotDeleted(@Param("projectId") String projectId,
                                                       @Param("taskState") LabelTaskState labelTaskState);

    @Query(value = "SELECT count(lt) FROM LabelTask as lt " +
            "WHERE lt.project.id = :projectId AND lt.media.deleted = false AND lt.media.mediaSourceType = :mediaSourceType")
    long countProjectTasksByMediaTypeWhereMediasNotDeleted(@Param("projectId") String projectId,
                                                           @Param("mediaSourceType") MediaSourceType mediaSourceType);

    @Query(value = "SELECT count(lt) FROM LabelTask as lt " +
            "WHERE lt.project.id = :projectId AND lt.media.deleted = false AND lt.preLabelState = :preLabelState")
    long countProjectTasksWhereMediasNotDeletedByPreLabelState(@Param("projectId") String projectId,
                                                               @Param("preLabelState") PreLabelState preLabelState);

    @Query(value = "SELECT count(lt) FROM LabelTask as lt " +
            "WHERE lt.project.id = :projectId AND lt.media.deleted = false AND lt.labelTaskState = :taskState AND " +
            "lt.preLabelState is null")
    long countProjectTasksByStateAndPrelabelstateNullWhereMediasNotDeleted(@Param("projectId") String projectId,
                                                                           @Param("taskState") LabelTaskState labelTaskState);


    @Query("select lt.id from LabelTask lt where lt.project.id = :projectId")
    List<String> getLabelTaskIdsInProject(@Param("projectId") String projectId);

}
