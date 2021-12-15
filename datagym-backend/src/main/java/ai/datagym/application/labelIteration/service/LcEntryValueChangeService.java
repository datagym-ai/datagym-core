package ai.datagym.application.labelIteration.service;

import ai.datagym.application.labelIteration.models.change.create.LcEntryChangeCreateBindingModel;
import ai.datagym.application.labelIteration.models.change.update.LcEntryChangeUpdateBindingModel;
import ai.datagym.application.labelIteration.models.change.viewModels.LcEntryChangeViewModel;

public interface LcEntryValueChangeService {

    LcEntryChangeViewModel createValueChange(LcEntryChangeCreateBindingModel changeCreate);

    LcEntryChangeViewModel updateValueChange(String changeId, LcEntryChangeUpdateBindingModel changeCreate);

    void deleteValueChange(String changeId);
}
