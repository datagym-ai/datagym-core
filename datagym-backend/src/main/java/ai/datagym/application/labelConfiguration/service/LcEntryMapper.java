package ai.datagym.application.labelConfiguration.service;

import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntryChecklist;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntryFreeText;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntrySelect;
import ai.datagym.application.labelConfiguration.entity.geometry.*;
import ai.datagym.application.labelConfiguration.models.bindingModels.LcEntryUpdateBindingModel;
import com.eforce21.lib.exception.GenericException;

import java.util.ArrayList;
import java.util.HashSet;

public final class LcEntryMapper {
    private static final long CURRENT_TIME = System.currentTimeMillis();

    private LcEntryMapper() {
    }

    public static LcEntryPoint mapToPoint(LcEntryUpdateBindingModel from) {
        LcEntryPoint to = new LcEntryPoint();

        to.setId(from.getId());
        to.setEntryKey(from.getEntryKey().toLowerCase());
        to.setEntryValue(from.getEntryValue());
        to.setType(LcEntryType.valueOf(from.getType().toUpperCase()));
        to.setColor(from.getColor());
        to.setShortcut(from.getShortcut());
        to.setChildren(new ArrayList<>());
        to.setTimestamp(CURRENT_TIME);
        to.setEntryValues(new HashSet<>());

        return to;
    }

    public static LcEntryLine mapToLine(LcEntryUpdateBindingModel from) {
        LcEntryLine to = new LcEntryLine();

        to.setId(from.getId());
        to.setEntryKey(from.getEntryKey().toLowerCase());
        to.setEntryValue(from.getEntryValue());
        to.setType(LcEntryType.valueOf(from.getType().toUpperCase()));
        to.setColor(from.getColor());
        to.setShortcut(from.getShortcut());
        to.setChildren(new ArrayList<>());
        to.setTimestamp(CURRENT_TIME);
        to.setEntryValues(new HashSet<>());

        return to;
    }

    public static LcEntryPolygon mapToPolygon(LcEntryUpdateBindingModel from) {
        LcEntryPolygon to = new LcEntryPolygon();

        to.setId(from.getId());
        to.setEntryKey(from.getEntryKey().toLowerCase());
        to.setEntryValue(from.getEntryValue());
        to.setType(LcEntryType.valueOf(from.getType().toUpperCase()));
        to.setColor(from.getColor());
        to.setShortcut(from.getShortcut());
        to.setChildren(new ArrayList<>());
        to.setTimestamp(CURRENT_TIME);
        to.setEntryValues(new HashSet<>());

        return to;
    }

    public static LcEntryImageSegmentation mapToSegmentation(LcEntryUpdateBindingModel from) {
        LcEntryImageSegmentation to = new LcEntryImageSegmentation();

        to.setId(from.getId());
        to.setEntryKey(from.getEntryKey().toLowerCase());
        to.setEntryValue(from.getEntryValue());
        to.setType(LcEntryType.valueOf(from.getType().toUpperCase()));
        to.setColor(from.getColor());
        to.setShortcut(from.getShortcut());
        to.setChildren(new ArrayList<>());
        to.setTimestamp(CURRENT_TIME);
        to.setEntryValues(new HashSet<>());

        return to;
    }

    public static LcEntryRectangle mapToRectangle(LcEntryUpdateBindingModel from) {
        LcEntryRectangle to = new LcEntryRectangle();

        to.setId(from.getId());
        to.setEntryKey(from.getEntryKey().toLowerCase());
        to.setEntryValue(from.getEntryValue());
        to.setType(LcEntryType.valueOf(from.getType().toUpperCase()));
        to.setColor(from.getColor());
        to.setShortcut(from.getShortcut());
        to.setChildren(new ArrayList<>());
        to.setTimestamp(CURRENT_TIME);
        to.setEntryValues(new HashSet<>());

        return to;
    }

    public static LcEntrySelect mapToSelect(LcEntryUpdateBindingModel from) {
        LcEntrySelect to = new LcEntrySelect();

        to.setId(from.getId());
        to.setEntryKey(from.getEntryKey().toLowerCase());
        to.setEntryValue(from.getEntryValue());
        to.setType(LcEntryType.valueOf(from.getType().toUpperCase()));
        to.setRequired(from.isRequired());
        to.setOptions(from.getOptions());
        to.setChildren(new ArrayList<>());
        to.setTimestamp(CURRENT_TIME);
        to.setEntryValues(new HashSet<>());

        return to;
    }

    public static LcEntryChecklist mapToCheckList(LcEntryUpdateBindingModel from) {
        LcEntryChecklist to = new LcEntryChecklist();

        to.setId(from.getId());
        to.setEntryKey(from.getEntryKey().toLowerCase());
        to.setEntryValue(from.getEntryValue());
        to.setType(LcEntryType.valueOf(from.getType().toUpperCase()));
        to.setRequired(from.isRequired());
        to.setOptions(from.getOptions());
        to.setChildren(new ArrayList<>());
        to.setTimestamp(CURRENT_TIME);
        to.setEntryValues(new HashSet<>());

        return to;
    }

    public static LcEntryFreeText mapToText(LcEntryUpdateBindingModel from) {
        String entryType = from.getType().toUpperCase();

        if(from.getMaxLength() == null || from.getMaxLength() < 1){
            throw new GenericException("entry_classification_incorrect", null, null, entryType);
        }

        LcEntryFreeText to = new LcEntryFreeText();

        to.setId(from.getId());
        to.setEntryKey(from.getEntryKey().toLowerCase());
        to.setEntryValue(from.getEntryValue());
        to.setType(LcEntryType.valueOf(from.getType().toUpperCase()));
        to.setRequired(from.isRequired());
        to.setChildren(new ArrayList<>());
        to.setMaxLength(from.getMaxLength());
        to.setTimestamp(CURRENT_TIME);
        to.setEntryValues(new HashSet<>());

        return to;
    }
}
