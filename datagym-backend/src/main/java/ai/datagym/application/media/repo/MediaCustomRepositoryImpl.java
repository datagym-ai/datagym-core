package ai.datagym.application.media.repo;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.media.entity.MediaSourceType;
import ai.datagym.application.utils.PageReturn;
import ai.datagym.application.utils.PageUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class MediaCustomRepositoryImpl implements MediaCustomRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Media> findByMediaNameAndDeletedFalseAndDatasetId(String mediaName, String datasetId) {
        return entityManager.createQuery("SELECT i FROM Media AS i " +
                "JOIN i.datasets AS d " +
                "WHERE d.id = :datasetId " +
                " AND i.mediaName = :mediaName " +
                " AND i.deleted = FALSE", Media.class)
                .setParameter("datasetId", datasetId)
                .setParameter("mediaName", mediaName)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }


    @Override
    public PageReturn<Media> findUndeletedMediaByDatasetAndNameAndType(@NotNull String datasetId,
                                                                       @Nullable String mediaName,
                                                                       @Nullable MediaSourceType mediaSourceType,
                                                                       Pageable page) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Media> q = builder.createQuery(Media.class);
        Root<Media> root = q.from(Media.class);

        Path<MediaSourceType> pathMediaSourceType = root.get("mediaSourceType");
        Path<String> pathMediaName = root.get("mediaName");
        Path<Boolean> pathMediaDeleted = root.get("deleted");

        List<Predicate> predicateList = new ArrayList<>();
        Predicate matchDataset = createDatasetPredicate(builder, root, datasetId);
        predicateList.add(matchDataset);

        // Filter out deleted media
        predicateList.add(builder.equal(pathMediaDeleted, false));

        if (mediaName != null) {
            predicateList.add(builder.like(builder.lower(pathMediaName), "%" + mediaName + "%"));
        }
        if (mediaSourceType != null) {
            predicateList.add(builder.equal(pathMediaSourceType, mediaSourceType));
        }

        Predicate[] predicates = predicateList.toArray(new Predicate[predicateList.size()]);
        q.select(root);
        q.where(builder.and(predicates));
        q.orderBy(builder.asc(pathMediaName));

        int offset = page.getPageNumber() * page.getPageSize();

        // Create paged elements
        List<Media> resultList = entityManager.createQuery(q)
                .setFirstResult(offset)
                .setMaxResults(page.getPageSize())
                .getResultList();


        // Create Count Query
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Media> mediaRootcount = countQuery.from(Media.class);
        // We need to reassign the join-path cause we create a new query
        predicateList.remove(matchDataset);
        predicateList.add(createDatasetPredicate(builder, mediaRootcount, datasetId));
        countQuery.select(builder.count(mediaRootcount)).where(builder.and(predicateList.toArray(new Predicate[predicateList.size()])));


        long totalElements = entityManager.createQuery(countQuery).getSingleResult();
        Integer totalPages = PageUtils.getTotalPages(totalElements, page.getPageSize());

        return createMediaePage(totalElements, totalPages, resultList);
    }


    private Predicate createDatasetPredicate(CriteriaBuilder builder, Root<Media> root, String datasetId) {
        Join<Media, Dataset> joinDatasets = root.join("datasets");
        Path<String> pathDatasetId = joinDatasets.get("id");
        return builder.equal(pathDatasetId, datasetId);
    }

    private PageReturn<Media> createMediaePage(Long totalElements, Integer totalPages, List<Media> pickUpSlipList) {
        PageReturn<Media> pickUpPage = new PageReturn<>();
        pickUpPage.setTotalElements(totalElements);
        pickUpPage.setTotalPages(totalPages);
        pickUpPage.setElements(pickUpSlipList);
        return pickUpPage;
    }
}
