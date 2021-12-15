package ai.datagym.application.dataset.service.video;

import ai.datagym.application.dataset.models.video.ExtractedVideoMetadataTO;

import java.io.IOException;

public interface VideoMetadataService {

    /**
     * Extracts specific video meta data information with help of ffprobe.
     *
     * @param uri The specific public accessible URI
     * @return Instance of {@link ExtractedVideoMetadataTO}
     * @throws IOException
     */
    ExtractedVideoMetadataTO fetchMetaDataFromUrl(String uri) throws IOException;
}
