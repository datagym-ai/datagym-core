package ai.datagym.application.labelConfiguration.models.export;

import ai.datagym.application.labelConfiguration.entity.LcEntry;

public final class LcEntryExportFactory {

    private LcEntryExportFactory() { }

    public static LcEntryExport cast(final LcEntry source) {
        switch (source.getType()) {
            case LINE:
            case POINT:
            case POLYGON:
            case RECTANGLE:
            case IMAGE_SEGMENTATION:
                return new LcEntryExportGeometry(source);
            case FREETEXT:
                return new LcEntryExportFreeText(source);
            case SELECT:
            case CHECKLIST:
                return new LcEntryExportOption(source);
            default:
                // not possible.
        }
        throw new IllegalArgumentException();
    }
}
