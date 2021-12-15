package ai.datagym.application.dataset.repo;

import ai.datagym.application.dataset.entity.DatasetAwsS3UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface AwsS3UserCredentialsRepository extends JpaRepository<DatasetAwsS3UserCredentials, String> {

    Optional<DatasetAwsS3UserCredentials> findAwsS3UserCredentialsByDatasetId(String datasetId);
}
