package ai.datagym.application.project.repo;

import ai.datagym.application.dataset.entity.Dataset;
import ai.datagym.application.project.entity.MediaType;
import ai.datagym.application.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ProjectRepository extends JpaRepository<Project, String> {
    List<Project> findAllByDeletedIsFalseAndOwner(String ownerId);

    List<Project> findAllByDeletedIsFalseAndOwnerAndDatasetsNotContainsAndMediaType(String ownerId,
                                                                                    Dataset dataset,
                                                                                    MediaType mediaType
    );

    List<Project> findAllByDeletedIsFalse();

    List<Project> findAllByDeleteTimeBeforeAndDeletedTrue(Long timeToDelete);

    Optional<Project> findByName(String name);

    Optional<Project> findByIdAndDeletedIsFalse(String projectId);

    Optional<Project> findByNameAndDeletedFalseAndOwner(String name, String owner);

    List<Project> findAllByName(String name);

    List<Project> findAllByOwnerAndDeletedIsFalse(String owner);

    Optional<Project> findByOwnerAndName(String owner, String projectName);
}
