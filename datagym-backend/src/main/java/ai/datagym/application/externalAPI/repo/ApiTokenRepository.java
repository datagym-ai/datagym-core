package ai.datagym.application.externalAPI.repo;

import ai.datagym.application.externalAPI.entity.ApiToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ApiTokenRepository extends JpaRepository<ApiToken, String> {
  List<ApiToken> findAllByDeletedIsFalseAndOwner(String owner);

  Optional<ApiToken> findApiTokenByIdAndDeletedFalse(String tokenId);

  Optional<ApiToken> findByNameAndDeletedFalseAndOwner(String name, String owner);
}
