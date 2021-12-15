package ai.datagym.application.labelConfiguration.repo;

import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface LcEntryRepository extends JpaRepository<LcEntry, String> {
    Optional<LcEntry> findByEntryKeyAndConfigurationIdAndParentEntryId(String lcEntryKey, String configId, String lcEntryParentId);

    List<LcEntry> findAllByParentEntryIsNullAndConfigurationId(String labelConfigId);

    @Query(value = "SELECT e FROM LcEntry AS e " +
            "WHERE e.configuration.id = :configId " +
            "  AND e.parentEntry.id IS NULL " +
            "  AND e.type IN :lcEntryTypeList")
    List<LcEntry> findAllClassificationLcEntriesWithLcEntryTypes(
            @Param(value = "configId") String configId,
            @Param(value = "lcEntryTypeList") List<LcEntryType> lcEntryTypeList);

    List<LcEntry> findAllByConfigurationId(String configId);

    void deleteLcEntriesByConfigurationId(String configId);

}
