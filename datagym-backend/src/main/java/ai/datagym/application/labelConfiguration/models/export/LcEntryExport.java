package ai.datagym.application.labelConfiguration.models.export;

import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelConfiguration.entity.LcEntryType;

import java.util.List;
import java.util.stream.Collectors;

public abstract class LcEntryExport {

    private List<LcEntryExport> children;
    private LcEntryType type;
    private String entryKey;
    private String entryValue;

    public LcEntryExport(final LcEntry source) {
        type = source.getType();
        entryKey = source.getEntryKey();
        entryValue = source.getEntryValue();

        // Just a precaution. In some tests the children may not be set.
        final List<LcEntry> sourceChildren = source.getChildren() != null
                ? source.getChildren()
                : List.of();

        children = sourceChildren.stream()
                .map(LcEntryExportFactory::cast)
                .collect(Collectors.toList());
    }

    public String getEntryKey() {
        return entryKey;
    }

    public void setEntryKey(String entryKey) {
        this.entryKey = entryKey;
    }

    public LcEntryType getType() {
        return type;
    }

    public void setType(LcEntryType type) {
        this.type = type;
    }


    public List<LcEntryExport> getChildren() {
        return children;
    }

    public void setChildren(List<LcEntryExport> children) {
        this.children = children;
    }


    public String getEntryValue() {
        return entryValue;
    }

    public void setEntryValue(String entryValue) {
        this.entryValue = entryValue;
    }
}
