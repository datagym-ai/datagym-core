package ai.datagym.application.labelIteration.repo;

import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.models.viewModels.geometry.IGeometryCountByDayViewModel;
import ai.datagym.application.labelIteration.models.viewModels.geometry.IGeometryCountViewModel;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface LcEntryValueRepository extends JpaRepository<LcEntryValue, String> {
    List<LcEntryValue> findAllByLabelIterationIdAndMediaIdAndLabelTaskIdAndLcEntryValueParentIsNull(String iterationId,
                                                                                                    String mediaId,
                                                                                                    String taskId);

    List<LcEntryValue> findAllByLabelIterationIdAndMediaId(String iterationId, String mediaId);

    List<LcEntryValue> findAllByLabelIterationIdAndMediaIdAndLcEntryValueParentNull(String iterationId, String mediaId);

    List<LcEntryValue> findByLcEntryId(String lcEntryId);

    Optional<LcEntryValue> findByLcEntryValueParentIdAndLcEntryEntryKey(String lcEntryValueParentId, String lcEntryKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    Optional<LcEntryValue> findById(@NotNull String id);

    List<LcEntryValue> findByLcEntryIdAndMediaIdAndLabelTaskId(String lcEntryId, String mediaId, String taskId);

    @Query(value = "SELECT v FROM LcEntryValue AS v " +
            "WHERE v.labelIteration.id = :iterationId " +
            " AND v.media.id = :mediaId " +
            " AND v.labelTask.id = :labelTaskId " +
            " AND v.lcEntry.parentEntry IS NULL " +
            " AND v.lcEntry.type IN :types")
    List<LcEntryValue> getAllRootValuesFromType(
            @Param("iterationId") String iterationId,
            @Param("mediaId") String mediaId,
            @Param("types") List<LcEntryType> types,
            @Param("labelTaskId") String labelTaskId);

    void deleteAllByLabelIterationIdAndMediaId(String iterationId, String mediaId);

    @Query(value = "SELECT v FROM LcEntryValue AS v " +
            "WHERE v.labelIteration.project.id = :projectId")
    List<LcEntryValue> getAllValuesFromProject(
            @Param("projectId") String projectId);

    @Query(value = "SELECT v.lcEntry.entryValue as lcEntryValue, count(v.lcEntry.id) as lcEntryValueCount " +
            "FROM LcEntryValue AS v " +
            "WHERE v.lcEntry.configuration.id = :lcConfigId AND v.lcEntry.type IN :types " +
            "GROUP BY v.lcEntry.entryValue")
    List<IGeometryCountViewModel> getGeometryCountsByConfigurationIdAndLcEntryType(@Param("lcConfigId") String lcConfigId,
                                                                                   @Param("types") List<LcEntryType> types);

    // Use nativeQuery because JPQL doesn't support date/time methods, like DATE() or FROM_UNIXTIME()
    @Query(value = "SELECT DATE(from_unixtime(timestamp / 1000)) as date, count(lc_entry_value.id) as geometryCount " +
            "FROM datagymdb.lc_entry_value " +
            "WHERE lc_entry_value.label_iteration_id = ?1 " +
            "AND lc_entry_value.label_source != 'AI_PRE_LABEL'" +
            "GROUP BY DATE(from_unixtime(timestamp / 1000)) " +
            "HAVING date > date_sub(curdate(), INTERVAL 30 DAY)", nativeQuery = true)
    List<IGeometryCountByDayViewModel> getGeometryCountByDayForLastMonth(@Param("lcConfigId") String lcConfigId);


    @Query(value = "SELECT v FROM LcEntryValue AS v " +
            "WHERE v.lcEntry.entryKey = :lcEntryKey AND v.lcEntry.type = :entryType AND v.labelTask.id = :taskId")
    List<LcEntryValue> findAllByTaskIdAndEntryKeyAndEntryType(
            @Param("taskId") String taskId,
            @Param("lcEntryKey") String lcEntryKey,
            @Param("entryType") LcEntryType entryType
    );

    void deleteLcEntryValuesByLabelIterationIdAndMediaIdIn(String iterationId, List<String> mediaList);
}
