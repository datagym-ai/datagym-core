package ai.datagym.application.limit.repo;

import ai.datagym.application.limit.entity.DataGymPlan;
import ai.datagym.application.limit.entity.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface LimitRepository extends JpaRepository<Limit, String> {
    Optional<Limit> findByOrganisationId(String organisationId);

    List<Limit> findAllByDataGymPlan(DataGymPlan plan);
}
