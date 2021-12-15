package ai.datagym.application.testUtils;

import ai.datagym.application.labelConfiguration.entity.LabelConfiguration;
import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntryChecklist;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntryFreeText;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntrySelect;
import ai.datagym.application.labelConfiguration.entity.geometry.LcEntryLine;
import ai.datagym.application.labelConfiguration.entity.geometry.LcEntryPoint;
import ai.datagym.application.labelConfiguration.entity.geometry.LcEntryPolygon;
import ai.datagym.application.labelConfiguration.entity.geometry.LcEntryRectangle;
import ai.datagym.application.labelConfiguration.models.bindingModels.LcEntryUpdateBindingModel;
import ai.datagym.application.labelConfiguration.models.viewModels.LcEntryViewModel;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LcEntryUtils {
    public static final String LC_ENTRY_ID = "TestId " + UUID.randomUUID();
    private static final Long TIME = System.currentTimeMillis();

    public static LcEntryUpdateBindingModel createTestLcEntryUpdateBindingModel(
            String id,
            String entryKey,
            String entryValue,
            String type,
            String color,
            String shortcut,
            Integer maxLength,
            boolean required,
            Map<String, String> options,
            LcEntryUpdateBindingModel parent) {
        return new LcEntryUpdateBindingModel() {{
            setId(id);
            setEntryKey(entryKey);
            setEntryValue(entryValue);
            setType(type);
            setColor(color);
            setShortcut(shortcut);
            setParentEntry(parent);
            setChildren(new ArrayList<>());
            setMaxLength(maxLength);
            setOptions(options);
            setRequired(required);
        }};
    }

    public static List<LcEntryUpdateBindingModel> createTestLcEntryUpdateBindingModels(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new LcEntryUpdateBindingModel() {{
                    setId(null);
                    setEntryKey("entryKey_updated " + index);
                    setEntryValue("entryValue_updated " + index);
                    setType("line");
                    setColor("blue " + index);
                    setShortcut("" + index);
                    setParentEntry(null);
                    setChildren(new ArrayList<>());
                    setMaxLength(null);
                    setOptions(null);
                    setRequired(true);
                }})
                .collect(Collectors.toList());
    }

    public static LcEntryLine createTestLcEntryLine(LabelConfiguration testLabelConfiguration) {
        return
                new LcEntryLine() {{
                    setId(LC_ENTRY_ID);
                    setEntryKey("entryKey");
                    setEntryValue("entryValue");
                    setType(LcEntryType.LINE);
                    setColor("blue");
                    setShortcut("1");
                    setChildren(new ArrayList<>());
                    setTimestamp(TIME);
                    setConfiguration(testLabelConfiguration);
                }};
    }

    public static LcEntryPoint createTestLcEntryPoint(LabelConfiguration testLabelConfiguration) {
        return
                new LcEntryPoint() {{
                    setId(LC_ENTRY_ID);
                    setEntryKey("entryKey");
                    setEntryValue("entryValue");
                    setType(LcEntryType.POINT);
                    setColor("blue");
                    setShortcut("2");
                    setChildren(new ArrayList<>());
                    setTimestamp(TIME);
                    setConfiguration(testLabelConfiguration);
                }};
    }

    public static LcEntryPolygon createTestLcEntryPolygon(LabelConfiguration testLabelConfiguration) {
        return
                new LcEntryPolygon() {{
                    setId(LC_ENTRY_ID);
                    setEntryKey("entryKey");
                    setEntryValue("entryValue");
                    setType(LcEntryType.POLYGON);
                    setColor("blue");
                    setShortcut("3");
                    setChildren(new ArrayList<>());
                    setTimestamp(TIME);
                    setConfiguration(testLabelConfiguration);
                }};
    }

    public static LcEntryRectangle createTestLcEntryRectangle(LabelConfiguration testLabelConfiguration) {
        return
                new LcEntryRectangle() {{
                    setId(LC_ENTRY_ID);
                    setEntryKey("entryKey");
                    setEntryValue("entryValue");
                    setType(LcEntryType.RECTANGLE);
                    setColor("blue");
                    setShortcut("4");
                    setChildren(new ArrayList<>());
                    setTimestamp(TIME);
                    setConfiguration(testLabelConfiguration);
                }};
    }

    public static LcEntrySelect createTestLcEntrySelect(LabelConfiguration testLabelConfiguration) {
        Map<String, String> options = new HashMap<>();
        options.put("selectKey", "selectValue");

        return
                new LcEntrySelect() {{
                    setId(LC_ENTRY_ID);
                    setEntryKey("entryKey");
                    setEntryValue("entryValue");
                    setType(LcEntryType.SELECT);
                    setChildren(new ArrayList<>());
                    setTimestamp(TIME);
                    setConfiguration(testLabelConfiguration);
                    setRequired(true);
                    setOptions(options);
                }};
    }

    public static LcEntryChecklist createTestLcEntryChecklist(LabelConfiguration testLabelConfiguration) {
        Map<String, String> options = new HashMap<>();
        options.put("checkListKey1", "checkListValue1");
        options.put("checkListKey2", "checkListValue2");
        options.put("checkListKey3", "checkListValue3");

        return
                new LcEntryChecklist() {{
                    setId(LC_ENTRY_ID);
                    setEntryKey("entryKey");
                    setEntryValue("entryValue");
                    setType(LcEntryType.CHECKLIST);
                    setChildren(new ArrayList<>());
                    setTimestamp(TIME);
                    setConfiguration(testLabelConfiguration);
                    setRequired(true);
                    setOptions(options);
                }};
    }

    public static LcEntryFreeText createTestLcEntryText(LabelConfiguration testLabelConfiguration) {
        return
                new LcEntryFreeText() {{
                    setId(LC_ENTRY_ID);
                    setEntryKey("entryKey");
                    setEntryValue("entryValue");
                    setType(LcEntryType.FREETEXT);
                    setChildren(new ArrayList<>());
                    setTimestamp(TIME);
                    setConfiguration(testLabelConfiguration);
                    setRequired(true);
                    setMaxLength(15);
                }};
    }

    public static List<LcEntry> createTestLcEntryGeometryList(int count, LcEntryType geometryType, LabelConfiguration testLabelConfiguration) {
        return IntStream.range(0, count)
                .mapToObj(index -> new LcEntryLine() {{
                    setId(LC_ENTRY_ID + index);
                    setEntryKey("entryKey " + index);
                    setEntryValue("entryValue " + index);
                    setType(geometryType);
                    setColor("blue " + index);
                    setShortcut("" + index);
                    setChildren(new ArrayList<>());
                    setTimestamp(TIME);
                    setConfiguration(testLabelConfiguration);
                }})
                .collect(Collectors.toList());
    }

    public static List<LcEntry> createTestLcEntryClassificationList(int count, LcEntryType classificationType, LabelConfiguration testLabelConfiguration) {
        return IntStream.range(0, count)
                .mapToObj(index -> new LcEntrySelect() {{
                    setId(LC_ENTRY_ID + index);
                    setEntryKey("entryKey " + index);
                    setEntryValue("entryValue " + index);
                    setType(classificationType);
                    setChildren(new ArrayList<>());
                    setTimestamp(TIME);
                    setConfiguration(testLabelConfiguration);
                    setRequired(true);
                    setOptions(new HashMap<>());
                }})
                .collect(Collectors.toList());
    }

    public static List<LcEntryViewModel> createTestLcEntryViewModels(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new LcEntryViewModel() {{
                    setId(LC_ENTRY_ID + index);
                    setEntryKey("entryKey " + index);
                    setEntryValue("entryValue " + index);
                    setType(LcEntryType.LINE);
                    setColor("blue " + index);
                    setShortcut("" + index);
                    setChildren(new ArrayList<>());
                    setMaxLength(null);
                    setOptions(new HashMap<>());
                    setRequired(false);
                    setTimestamp(TIME);
                }})
                .collect(Collectors.toList());
    }
}
