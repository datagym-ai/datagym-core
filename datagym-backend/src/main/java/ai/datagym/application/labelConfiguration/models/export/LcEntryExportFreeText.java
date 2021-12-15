package ai.datagym.application.labelConfiguration.models.export;

import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntryFreeText;

public class LcEntryExportFreeText extends LcEntryExport {

    private Integer maxLength;

    public LcEntryExportFreeText(final LcEntry source) {
        super(source);

        if (!(source instanceof LcEntryFreeText)) {
            throw new IllegalArgumentException();
        }

        maxLength = ((LcEntryFreeText) source).getMaxLength();
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }


}
