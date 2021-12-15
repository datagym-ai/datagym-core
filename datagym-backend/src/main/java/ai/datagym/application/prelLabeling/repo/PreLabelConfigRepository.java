package ai.datagym.application.prelLabeling.repo;

import ai.datagym.application.prelLabeling.entity.PreLabelConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface PreLabelConfigRepository extends JpaRepository<PreLabelConfiguration, String> {

    Optional<PreLabelConfiguration> findByProjectId(String projectId);

}
