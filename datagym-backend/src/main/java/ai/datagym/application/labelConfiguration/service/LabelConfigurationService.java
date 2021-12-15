package ai.datagym.application.labelConfiguration.service;

import ai.datagym.application.labelConfiguration.models.bindingModels.LcEntryUpdateBindingModel;
import ai.datagym.application.labelConfiguration.models.export.LcEntryExport;
import ai.datagym.application.labelConfiguration.models.viewModels.LabelConfigurationViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcConfigDeleteViewModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcConfigHasConfigChangedViewModel;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface LabelConfigurationService {
    LabelConfigurationViewModel getLabelConfiguration(String id);

    void createLabelConfiguration();

    List<LcEntryExport> exportLabelConfiguration(String configId, HttpServletResponse res) throws IOException;

    LabelConfigurationViewModel importLabelConfiguration(String configId, List<LcEntryUpdateBindingModel> entries);

    LabelConfigurationViewModel updateLabelConfiguration(String configId, List<LcEntryUpdateBindingModel> lcEntryUpdateBindingModel, boolean changeStatus);

    List<String> getForbiddenKeyWords();

    LcConfigHasConfigChangedViewModel hasConfigChanged(Long lastChangedConfig, String iterationId);

    LcConfigDeleteViewModel clearConfig(String configId);
}
