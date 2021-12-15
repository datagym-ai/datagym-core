package ai.datagym.application.prelLabeling.service;

import ai.datagym.application.prelLabeling.models.bindingModels.PreLabelConfigUpdateBindingModel;
import ai.datagym.application.prelLabeling.models.viewModels.PreLabelInfoViewModel;

public interface PreLabelConfigService {
    PreLabelInfoViewModel getPreLabelInfoByProject(String projectId);

    PreLabelInfoViewModel updatePreLabelConfigByProject(String projectId, PreLabelConfigUpdateBindingModel preLabelConfigUpdateBindingModel);

    void preLabelScheduleJob();
}
