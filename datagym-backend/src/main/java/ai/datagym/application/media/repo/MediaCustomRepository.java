package ai.datagym.application.media.repo;

import ai.datagym.application.media.entity.Media;
import ai.datagym.application.media.entity.MediaSourceType;
import ai.datagym.application.utils.PageReturn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MediaCustomRepository {
    Optional<Media> findByMediaNameAndDeletedFalseAndDatasetId(String mediaName, String datasetId);

    /**
     * Search for undeleted media with the given parameters
     *
     * @param datasetId       The specific dataset id
     * @param mediaName       The specific media name
     * @param mediaSourceType The specific media source type
     * @param page            The specific page object
     * @return Instance of {@link PageReturn} of {@link Media}
     */
    PageReturn<Media> findUndeletedMediaByDatasetAndNameAndType(@NotNull String datasetId, @Nullable String mediaName, @Nullable MediaSourceType mediaSourceType, Pageable page);
}
