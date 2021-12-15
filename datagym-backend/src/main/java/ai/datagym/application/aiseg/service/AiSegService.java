package ai.datagym.application.aiseg.service;

import ai.datagym.application.aiseg.model.aiseg.AiSegCalculate;
import ai.datagym.application.aiseg.model.aiseg.AiSegResponse;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.prelLabeling.entity.PreLabelMappingEntry;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

public interface AiSegService {

    /**
     * Inform the AiSeg-Backend that a new image is in usage
     * - for prefetching / performance reasons
     *
     * @param mediaId     The specific media id
     * @param frameNumber The specific frame number for video projects
     * @param dataUri     The specific dataUri according to RFC2397
     */
    void prepare(String mediaId, @Nullable Integer frameNumber, @Nullable String dataUri);

    /**
     * Send an calculation request to get a polygon
     *
     * @param aiSegCalculate The specific calculation object
     * @return Instance of {@link AiSegResponse} with  the detected polygon
     */
    AiSegResponse calculate(AiSegCalculate aiSegCalculate);

    /**
     * Inform the AiSeg-Backend that the image is no longer in usage
     *
     * @param mediaId The specific media id
     */
    void finish(String mediaId);

    /**
     * Inform the AiSeg-Backend that the object is no longer in usage
     *
     * @param userSessionUUID The specific user session uuid
     */
    void finishUserSession(String userSessionUUID);

    /**
     * Inform the AiSeg-Backend that a specific video frame is no longer in usage
     *
     * @param mediaId     The specific media id
     * @param frameNumber The specific frame number
     */
    void finishFrameImage(String mediaId, Integer frameNumber);

    void preLabelImage(Media mediaId, Map<String, Map<String, String>> requestedClasses, LabelTask labelTask, List<PreLabelMappingEntry> mappings);
}
