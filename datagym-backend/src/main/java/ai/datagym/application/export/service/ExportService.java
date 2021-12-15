package ai.datagym.application.export.service;

import ai.datagym.application.project.entity.Project;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ExportService {
    void exportJsonLabelsByProject(Project project, HttpServletResponse httpServletResponse) throws IOException;
}
