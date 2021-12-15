package ai.datagym.application.export.consumer;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

/**
 * Consumer for binaries and its metadata.
 * Note that MetaData must/is always called first to allow writing headers.
 */
public interface DataGymBinFileConsumer {
    HttpServletResponse getResponse();

    void onMetaData(String filename, String mime);

    void onStream(InputStream is);
}
