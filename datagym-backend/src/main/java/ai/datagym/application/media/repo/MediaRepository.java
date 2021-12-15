package ai.datagym.application.media.repo;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.media.entity.MediaSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface MediaRepository extends JpaRepository<Media, String> {
    List<Media> findAllMediaByDeleteTimeBefore(long timeBeforeToDeleteMedia);

    @Query(value = "SELECT i FROM Media AS i " +
            "JOIN i.datasets AS d WHERE d.id = :datasetId AND i.mediaSourceType = :mediaSourceType")
    List<Media> findAllByDatasetsIdAndMediaSourceType(@Param("datasetId") String datasetId,
                                                      @Param("mediaSourceType") MediaSourceType mediaSourceType);

    @Query(value = "SELECT i FROM Media AS i " +
            "JOIN i.datasets AS d WHERE d.id = :datasetId " +
            " AND i.mediaSourceType = :mediaSourceType " +
            " AND i.deleted = FALSE ")
    List<Media> findAllByDatasetsIdAndMediaSourceTypeAndDeletedIsFalse(
            @Param("datasetId") String datasetId,
            @Param("mediaSourceType") MediaSourceType mediaSourceType);

    Integer countAllByDatasetsContainingAndDeletedFalse(Dataset dataset);

    @Query(value = "SELECT i.id FROM Media as i " +
            "JOIN i.datasets AS d WHERE d.id = :datasetId ")
    List<String> findMediaIdsByDataset(@Param("datasetId") String datasetId);

    /**
     * Counts all invalid and not deleted media of a specific dataset and media source type
     *
     * @param dataset         The specific dataset where the media should be located at
     * @param mediaSourceType The specific media type to look for
     * @return Amount of matching media
     */
    @Query(value = "select count(i) from Media as i " +
            "JOIN i.datasets as d WHERE d = :dataset AND i.mediaSourceType = :mediaSourceType" +
            " AND i.valid = false AND i.deleted = false")
    long countInvalidAndUndeletedMedias(@Param("dataset") Dataset dataset, @Param("mediaSourceType") MediaSourceType mediaSourceType);
}
