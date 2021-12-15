package ai.datagym.application.prelLabeling.repo;

import ai.datagym.application.prelLabeling.entity.PreLabelMappingEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface PreLabelMappingEntryRepository extends JpaRepository<PreLabelMappingEntry, String> {
}
