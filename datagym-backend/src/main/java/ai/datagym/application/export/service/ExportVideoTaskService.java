package ai.datagym.application.export.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ExportVideoTaskService {

    /**
     * Exports a single video task to a readable json format
     *
     * @param labelTaskId         The specific task id
     * @param httpServletResponse
     * @throws IOException
     */
    void exportSingleVideoTask(String labelTaskId, HttpServletResponse httpServletResponse) throws IOException;
}
