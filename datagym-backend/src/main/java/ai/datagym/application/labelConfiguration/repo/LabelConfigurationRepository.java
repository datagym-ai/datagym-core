package ai.datagym.application.labelConfiguration.repo;

import ai.datagym.application.labelConfiguration.entity.LabelConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface LabelConfigurationRepository extends JpaRepository<LabelConfiguration, String> {
}