package ai.datagym.application.projectReviewer.repo;

import ai.datagym.application.projectReviewer.entity.ProjectReviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ProjectReviewerRepository extends JpaRepository<ProjectReviewer, String> {
}
