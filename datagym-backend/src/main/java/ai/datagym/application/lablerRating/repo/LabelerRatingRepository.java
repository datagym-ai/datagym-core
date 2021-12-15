package ai.datagym.application.lablerRating.repo;

import ai.datagym.application.labelTask.repo.LabelTaskCustomRepository;
import ai.datagym.application.lablerRating.entity.LabelerRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface LabelerRatingRepository extends JpaRepository<LabelerRating, String>, LabelTaskCustomRepository {
    Optional<LabelerRating> findByLabelerAndProjectIdAndMediaId(String labelerId, String projectId, String mediaId);

    List<LabelerRating> findLabelerRatingsByProjectIdAndMediaDeleted(String projectId, boolean isMediaDeleted);

}
