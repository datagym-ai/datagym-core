package ai.datagym.application.labelIteration.repo;

import ai.datagym.application.labelIteration.entity.LabelIteration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface LabelIterationRepository extends JpaRepository<LabelIteration, String> {
}
