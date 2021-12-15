package ai.datagym.application.testUtils;

import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelIteration.entity.LabelIteration;
import ai.datagym.application.labelIteration.entity.classification.LcEntryCheckListValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntrySelectValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntryTextValue;
import ai.datagym.application.labelIteration.entity.geometry.*;
import ai.datagym.application.labelIteration.models.bindingModels.*;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.media.entity.Media;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ai.datagym.application.testUtils.ImageUtils.IMAGE_ID;
import static ai.datagym.application.testUtils.LabelConfigurationUtils.LC_CONFIG_ID;
import static ai.datagym.application.testUtils.LabelIterationUtils.LC_ITERATION_ID;
import static ai.datagym.application.testUtils.LabelTaskUtils.LABEL_TASK_ID;
import static ai.datagym.application.testUtils.LcEntryUtils.LC_ENTRY_ID;

public class LcEntryValueUtils {
    public static final String LC_ENTRY_VALUE_ID = "TestId " + UUID.randomUUID();
    private static final Long TIME = System.currentTimeMillis();

    public static LcEntryValueUpdateBindingModel createTestLcEntryValueUpdateBindingModel(
            String id,
            String lcEntryId,
            String parentId,
            String text,
            String selectKey,
            Double x,
            Double y,
            Double width,
            Double height,
            List<String> checkedValues,
            List<PointPojoBindingModel> points,
            LcEntryValueUpdateBindingModel parentEntry,
            List<LcEntryValueUpdateBindingModel> children,
            boolean valid) {
        return new LcEntryValueUpdateBindingModel() {{
            setId(id);
            setLcEntryId(lcEntryId);
            setLcEntryValueParentId(parentId);
            setText(text);
            setSelectKey(selectKey);
            setX(x);
            setY(y);
            setWidth(width);
            setHeight(height);
            setCheckedValues(checkedValues);
            setPoints(points);
            setParentEntry(parentEntry);
            setChildren(children);
            setValid(valid);
            setLabelTaskId(LABEL_TASK_ID);
        }};
    }

    public static List<LcEntryValueUpdateBindingModel> createTestLcEntryValueUpdateBindingModels(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new LcEntryValueUpdateBindingModel() {{
                    setId(LC_ENTRY_VALUE_ID + index);
                    setLcEntryId(LC_ENTRY_ID);
                    setLcEntryValueParentId(LC_ENTRY_VALUE_ID + index);
                    setText(null);
                    setSelectKey(null);
                    setX(0.0);
                    setY(0.0);
                    setWidth(0.0);
                    setHeight(0.0);
                    setCheckedValues(new ArrayList<>());
                    setPoints(new ArrayList<>());
                    setParentEntry(null);
                    setChildren(new ArrayList<>());
                    setValid(false);
                    setLabelTaskId(LABEL_TASK_ID);
                }})
                .collect(Collectors.toList());
    }

    public static LcEntryValueChangeValueClassBindingModel createTestLcEntryValueChangeValueClassBindingModel() {
        return new LcEntryValueChangeValueClassBindingModel() {{
                    setNewLcEntryId(LC_ENTRY_ID);
                }};
    }

    public static LcEntryValueCreateBindingModel createTestLcEntryValueCreateBindingModel(
            String lcEntryId,
            String iterationId,
            String imageId,
            String parentId) {
        return new LcEntryValueCreateBindingModel() {{
            setLcEntryId(lcEntryId);
            setIterationId(iterationId);
            setMediaId(imageId);
            setLcEntryValueParentId(parentId);
            setLabelTaskId(LABEL_TASK_ID);
        }};
    }

    public static LcEntryValueExtendBindingModel createTestLcEntryValueExtendBindingModel(
            String iterationId,
            String imageId,
            String lcEntryId,
            String lcEntryParentId) {
        return new LcEntryValueExtendBindingModel() {{
            setIterationId(iterationId);
            setMediaId(imageId);
            setLcEntryId(lcEntryId);
            setLcEntryParentId(lcEntryParentId);
            setLabelTaskId(LABEL_TASK_ID);
        }};
    }

    public static LcEntryValueExtendAllBindingModel createTestLcEntryValueExtendAllBindingModel(
            String iterationId,
            String imageId) {
        return new LcEntryValueExtendAllBindingModel() {{
            setIterationId(iterationId);
            setMediaId(imageId);
            setLabelTaskId(LABEL_TASK_ID);
        }};
    }

    public static LcEntryLineValue createTestLcEntryLineValue(LabelIteration labelIteration, Media media, LcEntry lineLcEntry) {
        List<PointPojo> pointPojoList = PointPojoUtils.createPointPojoList(3);
        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        return
                new LcEntryLineValue() {{
                    setId(LC_ENTRY_VALUE_ID);
                    setLabelIteration(labelIteration);
                    setLabeler("eforce21");
                    setMedia(media);
                    setLcEntry(lineLcEntry);
                    setLcEntryValueParent(null);
                    setChildren(new ArrayList<>());
                    setTimestamp(TIME);
                    setPoints(pointPojoList);
                    setLabelTask(labelTask);
                }};
    }

    public static LcEntryPointValue createTestLcEntryPointValue(LabelIteration labelIteration, Media media, LcEntry pointLcEntry) {
        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        return
                new LcEntryPointValue() {{
                    setId(LC_ENTRY_VALUE_ID);
                    setLabelIteration(labelIteration);
                    setLabeler("eforce21");
                    setMedia(media);
                    setLcEntry(pointLcEntry);
                    setLcEntryValueParent(null);
                    setChildren(new ArrayList<>());
                    setTimestamp(TIME);
                    setX(1.5);
                    setY(2.5);
                    setLabelTask(labelTask);
                }};
    }

    public static LcEntryPolygonValue createTestLcEntryPolyValue(LabelIteration labelIteration, Media media, LcEntry polygonLcEntry) {
        List<PointPojo> pointPojoList = PointPojoUtils.createPointPojoList(4);
        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        return
                new LcEntryPolygonValue() {{
                    setId(LC_ENTRY_VALUE_ID);
                    setLabelIteration(labelIteration);
                    setLabeler("eforce21");
                    setMedia(media);
                    setLcEntry(polygonLcEntry);
                    setLcEntryValueParent(null);
                    setChildren(new ArrayList<>());
                    setTimestamp(TIME);
                    setPoints(pointPojoList);
                    setLabelTask(labelTask);
                }};
    }

    public static LcEntryRectangleValue createTestLcEntryRectangleValue(LabelIteration labelIteration, Media media, LcEntry rectangleLcEntry) {
        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        return
                new LcEntryRectangleValue() {{
                    setId(LC_ENTRY_VALUE_ID);
                    setLabelIteration(labelIteration);
                    setLabeler("eforce21");
                    setMedia(media);
                    setLcEntry(rectangleLcEntry);
                    setLcEntryValueParent(null);
                    setChildren(new ArrayList<>());
                    setTimestamp(TIME);
                    setX(1.5);
                    setY(2.5);
                    setWidth(10.7);
                    setHeight(10.5);
                    setLabelTask(labelTask);
                }};
    }

    public static LcEntrySelectValue createTestLcEntrySelectValue(LabelIteration labelIteration, Media media, LcEntry selectLcEntry) {
        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        return
                new LcEntrySelectValue() {{
                    setId(LC_ENTRY_VALUE_ID);
                    setLabelIteration(labelIteration);
                    setLabeler("eforce21");
                    setMedia(media);
                    setLcEntry(selectLcEntry);
                    setLcEntryValueParent(null);
                    setChildren(new ArrayList<>());
                    setTimestamp(TIME);
                    setSelectKey("selectKey");
                    setLabelTask(labelTask);
                }};
    }

    public static LcEntryCheckListValue createTestLcEntryCheckListValue(LabelIteration labelIteration, Media media, LcEntry checkListLcEntry) {
        List<String> checkListValues = new ArrayList<>();
        checkListValues.add("checkListKey1");
        checkListValues.add("checkListKey2");

        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        return
                new LcEntryCheckListValue() {{
                    setId(LC_ENTRY_VALUE_ID);
                    setLabelIteration(labelIteration);
                    setLabeler("eforce21");
                    setMedia(media);
                    setLcEntry(checkListLcEntry);
                    setLcEntryValueParent(null);
                    setChildren(new ArrayList<>());
                    setTimestamp(TIME);
                    setCheckedValues(checkListValues);
                    setLabelTask(labelTask);
                }};
    }

    public static LcEntryTextValue createTestLcEntryTextValue(LabelIteration labelIteration, Media media, LcEntry textLcEntry) {
        LabelTask labelTask = LabelTaskUtils.createTestLabelTask("eForce21");

        return
                new LcEntryTextValue() {{
                    setId(LC_ENTRY_VALUE_ID);
                    setLabelIteration(labelIteration);
                    setLabeler("eforce21");
                    setMedia(media);
                    setLcEntry(textLcEntry);
                    setLcEntryValueParent(null);
                    setChildren(new ArrayList<>());
                    setTimestamp(TIME);
                    setText("text");
                    setLabelTask(labelTask);
                }};
    }

    public static List<LcEntryValueViewModel> createLcEntryValueViewModelList(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new LcEntryValueViewModel() {{
                    setId(LC_ENTRY_VALUE_ID + index);
                    setLcEntryValueParentId(LC_ENTRY_VALUE_ID + index + 0);
                    setLabelIterationId(LC_ITERATION_ID);
                    setConfigurationId(LC_CONFIG_ID);
                    setMediaId(IMAGE_ID);
                    setLcEntryId(LC_ENTRY_ID + index);
                    setEntryTypeLcEntry("SELECT");
                    setEntryKeyLcEntry("entryKey" + index);
                    setEntryValueLcEntry("entryValue" + index);
                    setLabeler("eforce21");
                    setTimestamp(TIME);
                    setChildren(new ArrayList<>());
                }})
                .collect(Collectors.toList());
    }

    public static LcEntryValueViewModel createLcEntryValueViewModel() {
                return new LcEntryValueViewModel() {{
                    setId(LC_ENTRY_VALUE_ID);
                    setLcEntryValueParentId(LC_ENTRY_VALUE_ID);
                    setLabelIterationId(LC_ITERATION_ID);
                    setConfigurationId(LC_CONFIG_ID);
                    setMediaId(IMAGE_ID);
                    setLcEntryId(LC_ENTRY_ID);
                    setEntryTypeLcEntry("SELECT");
                    setEntryKeyLcEntry("entryKey");
                    setEntryValueLcEntry("entryValue");
                    setLabeler("eforce21");
                    setTimestamp(TIME);
                    setChildren(new ArrayList<>());
                }};
    }
}
