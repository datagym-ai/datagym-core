package ai.datagym.application.prelLabeling.service;

import ai.datagym.application.prelLabeling.entity.PreLabelMappingEntry;
import ai.datagym.application.prelLabeling.models.viewModels.PreLabelMappingEntryViewModel;

public final class PreLabelingMapper {
    private PreLabelingMapper() {
    }

    public static PreLabelMappingEntryViewModel mapToPreLabelMappingEntryViewModel(PreLabelMappingEntry from) {
        PreLabelMappingEntryViewModel to = new PreLabelMappingEntryViewModel();
        to.setPreLabelMappingId(from.getId());
        to.setPreLabelClassKey(from.getPreLabelClassKey());
        to.setPreLabelModel(from.getPreLabelModel());
        to.setPreLabelConfigId(from.getPreLabelConfig().getId());
        to.setLcEntryId(from.getLcEntry().getId());

        return to;
    }
}
