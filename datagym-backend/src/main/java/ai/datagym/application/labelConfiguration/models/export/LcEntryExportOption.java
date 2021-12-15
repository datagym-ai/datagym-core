package ai.datagym.application.labelConfiguration.models.export;

import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntryChecklist;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntrySelect;

import java.util.Map;

public class LcEntryExportOption extends LcEntryExport {

    private Map<String, String> options;

    public LcEntryExportOption(final LcEntry source) {
        super(source);

        if (source instanceof LcEntryChecklist) {
            options = ((LcEntryChecklist) source).getOptions();
            return;
        }
        if (source instanceof LcEntrySelect) {
            options = ((LcEntrySelect) source).getOptions();
            return;
        }

        throw new IllegalArgumentException();
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }
}
