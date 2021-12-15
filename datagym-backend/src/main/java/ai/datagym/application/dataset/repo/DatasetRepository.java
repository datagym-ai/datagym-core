package ai.datagym.application.dataset.repo;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.project.entity.MediaType;
import ai.datagym.application.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface DatasetRepository extends JpaRepository<Dataset, String> {
    List<Dataset> findAllByDeletedIsFalse();

    List<Dataset> findAllByDeletedIsFalseAndOwner(String owner);

    List<Dataset> findAllByDeletedIsFalseAndOwnerAndProjectsNotContainingAndMediaType(String owner, Project project,
                                                                                      MediaType mediaType);

    Optional<Dataset> findByName(String datasetName);

    Optional<Dataset> findByIdAndDeletedIsFalse(String datasetId);

    Optional<Dataset> findByNameAndDeletedFalseAndOwner(String datasetName, String ownerId);

    List<Dataset> findAllDatasetByDeleteTimeBefore(Long timeToDelete);

    @Query(value = "SELECT d.media from Dataset as d " +
            "where d.id = :datasetId ")
    List<Media> getAllMediasByDatasetId(@Param("datasetId") String datasetId);

    List<Dataset> findAllByName(String name);
}
