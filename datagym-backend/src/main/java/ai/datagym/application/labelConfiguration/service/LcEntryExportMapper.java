package ai.datagym.application.labelConfiguration.service;

import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelConfiguration.models.export.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 'Convert' the given list of entries with new ones without ids or timestamps.
 */
public class LcEntryExportMapper {

    public static List<LcEntryExport> convert(final Set<LcEntry> entries) {
        return convert(new ArrayList<>(entries));
    }

    public static List<LcEntryExport> convert(final List<LcEntry> entries) {
        return entries.stream().map(LcEntryExportMapper::convertEntry).collect(Collectors.toList());
    }

    private static LcEntryExport convertEntry(final LcEntry entry) {
        return LcEntryExportFactory.cast(entry);
    }

}
