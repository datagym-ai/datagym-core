package ai.datagym.application.labelTask.repo;

import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.utils.GoogleString;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class LabelTaskCustomRepositoryImpl implements LabelTaskCustomRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<LabelTask> search(String projectId, GoogleString googleString,
                                  LabelTaskState filterLabelTaskState, boolean isMediaDeleted,
                                  int maxResults) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<LabelTask> q = builder.createQuery(LabelTask.class);
        Root<LabelTask> root = q.from(LabelTask.class);

        // Project id
        Path<Project> projectPath = root.get("project");
        Path<String> pathProjectId = projectPath.get("id");

        // Media name
        Path<Media> pathMedia = root.get("media");
        Path<Boolean> pathMediaDeleted = pathMedia.get("deleted");
        Path<String> pathMediaName = pathMedia.get("mediaName");

        // Misc
        Path<LabelTaskState> pathStatus = root.get("labelTaskState");
        Path<String> pathLabeler = root.get("labeler");

        List<Predicate> predicates = new ArrayList<>();

        if (projectId != null) {
            predicates.add(builder.equal(pathProjectId, projectId));
        } else {
            throw new IllegalArgumentException("You need a project-id to filter label tasks!");
        }

        if (filterLabelTaskState != null) {
            predicates.add(builder.equal(pathStatus, filterLabelTaskState));
        }

        predicates.add(builder.equal(pathMediaDeleted, isMediaDeleted));

        if (googleString != null) {
            googleString.getPartsNotNumeric().forEach(x -> {
                String xy = "%" + x + "%";
                Predicate pMediaName = builder.like(builder.lower(pathMediaName), xy);
                Predicate pLabeler = builder.like(builder.lower(pathLabeler), xy);
                predicates.add(builder.or(pMediaName, pLabeler));
            });
        }

        Predicate[] predicatesArray = predicates.toArray(new Predicate[predicates.size()]);
        q.select(root);
        q.where(builder.and(predicatesArray));
        q.orderBy(builder.asc(pathMediaName));

        if (maxResults == 0) {
            maxResults = Integer.MAX_VALUE;
        }
        return entityManager.createQuery(q).setMaxResults(maxResults).getResultList();
    }

    @Override
    public Optional<LabelTask> findByOwnerAndStateAndMediaDeletedFalse(String projectOwner, String labeler) {
        return entityManager.createQuery(
                "SELECT lt " +
                        " FROM LabelTask AS lt " +
                        " WHERE lt.project.owner = :projectOwner AND " +
                        "lt.project.deleted = FALSE AND " +
                        "lt.media.deleted = FALSE AND " +
                        "(lt.preLabelState = 'FINISHED' OR lt.preLabelState = 'WAITING' OR lt.preLabelState is NULL) AND " +
                        "       ((lt.labelTaskState = 'IN_PROGRESS' AND lt.labeler = :labeler) " +
                        "   OR lt.labelTaskState = 'WAITING' OR lt.labelTaskState =  'WAITING_CHANGED') " +
                        " ORDER BY lt.media.mediaName, lt.labelTaskState DESC, lt.preLabelState DESC", LabelTask.class)
                .setParameter("projectOwner", projectOwner)
                .setParameter("labeler", labeler)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public Optional<LabelTask> findByOwnerAndStateAndProjectIdAndMediaDeletedFalse(String projectOwner, String labeler, String projectId) {
        return entityManager.createQuery(
                "SELECT lt " +
                        " FROM LabelTask AS lt " +
                        " WHERE lt.project.id = :projectId AND " +
                        " lt.project.deleted = FALSE AND " +
                        " lt.project.owner = :projectOwner AND " +
                        " lt.media.deleted = FALSE AND " +
                        " (((lt.labelTaskType = 'BENCHMARK_MASTER' OR lt.labelTaskType = 'BENCHMARK_SLAVE') AND " +
                        " lt.labeler = :labeler ) OR lt.labelTaskType = 'DEFAULT') AND " +
                        " (lt.preLabelState = 'FINISHED' OR lt.preLabelState = 'WAITING' OR lt.preLabelState is NULL) AND " +
                        "       ((lt.labelTaskState = 'IN_PROGRESS' AND lt.labeler = :labeler) " +
                        "   OR lt.labelTaskState = 'WAITING' OR lt.labelTaskState =  'WAITING_CHANGED') " +
                        " ORDER BY lt.media.mediaName,  lt.labelTaskState DESC, lt.preLabelState DESC", LabelTask.class)
                .setParameter("projectOwner", projectOwner)
                .setParameter("labeler", labeler)
                .setParameter("projectId", projectId)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public Optional<LabelTask> findNextTaskByCompletedTaskState(String projectOwner) {
        return entityManager.createQuery(
                "SELECT lt " +
                        " FROM LabelTask AS lt " +
                        " WHERE lt.project.owner = :projectOwner AND " +
                        "lt.media.deleted = FALSE AND " +
                        "lt.labelTaskState = 'COMPLETED' " +
                        " ORDER BY lt.id DESC ", LabelTask.class)
                .setParameter("projectOwner", projectOwner)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public Optional<LabelTask> findNextTaskByProjectIdAndCompletedOrSkippedTaskState(String projectOwner, String projectId) {
        return entityManager.createQuery(
                "SELECT lt " +
                        " FROM LabelTask AS lt " +
                        " WHERE lt.project.id = :projectId AND " +
                        " lt.project.owner = :projectOwner AND " +
                        " lt.media.deleted = FALSE AND " +
                        " lt.labelTaskType = 'DEFAULT' AND " +
                        " (lt.labelTaskState = 'COMPLETED' OR lt.labelTaskState = 'SKIPPED') " +
                        " ORDER BY lt.media.mediaName, lt.id DESC ", LabelTask.class)
                .setParameter("projectOwner", projectOwner)
                .setParameter("projectId", projectId)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public int updateLabelTaskStateByProjectIdAndDatasetId(String projectId, String datasetId,
                                                           LabelTaskState oldLabelTaskState, LabelTaskState newLabelTaskState) {
        return entityManager.createQuery("update LabelTask lt set lt.labelTaskState = :newLabelTaskState where " +
                "lt.project.id = :projectId and " +
                "lt.labelTaskState = :oldLabelTaskState and " +
                "lt.media.id in (select i.id from Media i join i.datasets d where d.id = :datasetId)")
                .setParameter("newLabelTaskState", newLabelTaskState)
                .setParameter("datasetId", datasetId)
                .setParameter("projectId", projectId)
                .setParameter("oldLabelTaskState", oldLabelTaskState)
                .executeUpdate();
    }

    @Override
    public void deleteAllLabelTasksAndData(String projectId, String labelIterationId, List<String> mediaIds) {
        if (mediaIds.isEmpty()) {
            return;
        }

        // Write all pending changes to the DB and clear persistence context
        entityManager.flush();
        entityManager.clear();

        // Delete Task from Database
        Query query = entityManager.createQuery("DELETE FROM LabelTask lt where lt.project.id = :projectId AND " +
                "lt.labelIteration.id = :iterationId AND " +
                "lt.media.id in :mediaIds");
        query.setParameter("projectId", projectId);
        query.setParameter("iterationId", labelIterationId);
        query.setParameter("mediaIds", mediaIds);
        query.executeUpdate();

        // Delete all LabelRatings for the media
        query = entityManager.createQuery("DELETE FROM LabelerRating labelerRating where labelerRating.project.id = :projectId AND " +
                "labelerRating.media.id in :mediaIds");
        query.setParameter("projectId", projectId);
        query.setParameter("mediaIds", mediaIds);
        query.executeUpdate();
    }
}
