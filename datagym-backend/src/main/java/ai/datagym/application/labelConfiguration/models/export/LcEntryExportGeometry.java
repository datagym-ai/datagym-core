package ai.datagym.application.labelConfiguration.models.export;

import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelConfiguration.entity.geometry.LcEntryGeometry;

public class LcEntryExportGeometry extends LcEntryExport {

    private String color;
    private String shortcut;


    public LcEntryExportGeometry(final LcEntry source) {
        super(source);

        if (!(source instanceof LcEntryGeometry)) {
            throw new IllegalArgumentException();
        }

        color = ((LcEntryGeometry) source).getColor();
        shortcut = ((LcEntryGeometry) source).getShortcut();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getShortcut() {
        return shortcut;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }
}
