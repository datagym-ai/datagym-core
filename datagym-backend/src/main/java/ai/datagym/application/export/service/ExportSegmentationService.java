package ai.datagym.application.export.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ExportSegmentationService {

    void streamSegmentationBitmap(String taskId, String lcEntryKey, HttpServletResponse response) throws IOException;

}
