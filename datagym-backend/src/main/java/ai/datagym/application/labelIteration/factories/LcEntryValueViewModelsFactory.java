package ai.datagym.application.labelIteration.factories;

import ai.datagym.application.labelConfiguration.entity.classification.LcEntryChecklist;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntryFreeText;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntrySelect;
import ai.datagym.application.labelConfiguration.entity.geometry.*;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntryCheckListValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntrySelectValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntryTextValue;
import ai.datagym.application.labelIteration.entity.geometry.*;
import ai.datagym.application.labelIteration.models.change.viewModels.*;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;
import ai.datagym.application.labelIteration.models.viewModels.classification.ChecklistValueViewModel;
import ai.datagym.application.labelIteration.models.viewModels.classification.FreetextValueViewModel;
import ai.datagym.application.labelIteration.models.viewModels.classification.SelectValueViewModel;
import ai.datagym.application.labelIteration.models.viewModels.geometry.*;
import com.eforce21.lib.exception.GenericException;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class LcEntryValueViewModelsFactory {
    private static final String SELECT = "SELECT";
    private static final String CHECKLIST = "CHECKLIST";
    private static final String FREETEXT = "FREETEXT";
    private static final String POINT = "POINT";
    private static final String LINE = "LINE";
    private static final String POLYGON = "POLYGON";
    private static final String IMAGE_SEGMENTATION = "IMAGE_SEGMENTATION";
    private static final String RECTANGLE = "RECTANGLE";

    private LcEntryValueViewModelsFactory() {
    }

    public static LcEntryValueViewModel createLcEntryValueViewModel(LcEntryValue lcEntryValue) {
        String entryTypeToLowercase = lcEntryValue.getLcEntry().getType().name().toLowerCase();
        String entryType = entryTypeToLowercase.toUpperCase();

        String id = lcEntryValue.getId();

        String lcEntryValueParentId = null;
        if (lcEntryValue.getLcEntryValueParent() != null) {
            lcEntryValueParentId = lcEntryValue.getLcEntryValueParent().getId();
        }

        String labelIterationId = lcEntryValue.getLabelIteration().getId();
        String mediaId = lcEntryValue.getMedia().getId();
        String lcEntryId = lcEntryValue.getLcEntry().getId();
        Long timestamp = lcEntryValue.getTimestamp();
        String labeler = lcEntryValue.getLabeler();
        String configurationId = lcEntryValue.getLcEntry().getConfiguration().getId();
        String entryTypeLcEntry = lcEntryValue.getLcEntry().getType().name().toUpperCase();
        String entryKeyLcEntry = lcEntryValue.getLcEntry().getEntryKey();
        String entryValueLcEntry = lcEntryValue.getLcEntry().getEntryValue();
        boolean valid = lcEntryValue.isValid();
        String comment = lcEntryValue.getComment();
        List<LcEntryValueViewModel> children = new ArrayList<>();

        switch (entryType) {
            case SELECT:
                if (lcEntryValue instanceof LcEntrySelectValue) {
                    LcEntrySelectValue lcEntrySelectValue = (LcEntrySelectValue) lcEntryValue;
                    LcEntrySelect lcEntrySelect = (LcEntrySelect) Hibernate.unproxy(lcEntryValue.getLcEntry());
                    List<SelectChangeViewModel> selectChanges = lcEntryValue.getLcEntryValueChanges().stream()
                            .map(change -> (SelectChangeViewModel) ValueChangeViewModelFactory.map(change))
                            .collect(Collectors.toList());

                    String selectKey = lcEntrySelectValue.getSelectKey();
                    boolean required = lcEntrySelect.isRequired();

                    return new SelectValueViewModel(
                            id, lcEntryValueParentId, labelIterationId, mediaId, lcEntryId, timestamp, labeler,
                            configurationId, entryTypeLcEntry, entryKeyLcEntry, entryValueLcEntry, valid, comment,
                            children, selectKey, required, selectChanges);
                }
                break;
            case CHECKLIST:
                if (lcEntryValue instanceof LcEntryCheckListValue) {
                    LcEntryCheckListValue lcEntryCheckListValue = (LcEntryCheckListValue) lcEntryValue;
                    LcEntryChecklist lcEntryChecklist = (LcEntryChecklist) Hibernate.unproxy(lcEntryValue.getLcEntry());
                    List<ChecklistChangeViewModel> checklistChanges = lcEntryValue.getLcEntryValueChanges().stream()
                            .map(change -> (ChecklistChangeViewModel) ValueChangeViewModelFactory.map(change))
                            .collect(Collectors.toList());

                    List<String> checkedValues = lcEntryCheckListValue.getCheckedValues();
                    boolean required = lcEntryChecklist.isRequired();

                    return new ChecklistValueViewModel(
                            id, lcEntryValueParentId, labelIterationId, mediaId, lcEntryId, timestamp, labeler,
                            configurationId, entryTypeLcEntry, entryKeyLcEntry, entryValueLcEntry, valid, comment,
                            children, checkedValues, required, checklistChanges);

                }
                break;
            case FREETEXT:
                if (lcEntryValue instanceof LcEntryTextValue) {
                    LcEntryTextValue lcEntryTextValue = (LcEntryTextValue) lcEntryValue;
                    LcEntryFreeText lcEntryFreeText = (LcEntryFreeText) Hibernate.unproxy(lcEntryValue.getLcEntry());
                    List<TextChangeViewModel> textChanges = lcEntryValue.getLcEntryValueChanges().stream()
                            .map(change -> (TextChangeViewModel) ValueChangeViewModelFactory.map(change))
                            .collect(Collectors.toList());

                    String text = lcEntryTextValue.getText();
                    boolean required = lcEntryFreeText.isRequired();

                    return new FreetextValueViewModel(
                            id, lcEntryValueParentId, labelIterationId, mediaId, lcEntryId, timestamp, labeler,
                            configurationId, entryTypeLcEntry, entryKeyLcEntry, entryValueLcEntry, valid, comment,
                            children, text, required, textChanges);
                }
                break;
            case POINT:
                if (lcEntryValue instanceof LcEntryPointValue) {
                    LcEntryPointValue lcEntryPointValue = (LcEntryPointValue) lcEntryValue;
                    LcEntryPoint lcEntryPoint = (LcEntryPoint) Hibernate.unproxy(lcEntryValue.getLcEntry());
                    List<PointChangeViewModel> pointChanges = lcEntryValue.getLcEntryValueChanges().stream()
                            .map(change -> (PointChangeViewModel) ValueChangeViewModelFactory.map(change))
                            .collect(Collectors.toList());

                    String color = lcEntryPoint.getColor();
                    String shortcut = lcEntryPoint.getShortcut();

                    String pointId = lcEntryPointValue.getId();
                    Double x = lcEntryPointValue.getX();
                    Double y = lcEntryPointValue.getY();
                    PointPojoViewModel pointPojoViewModel = createPointPojoViewModel(pointId, x, y);

                    return new PointValueViewModel(
                            id, lcEntryValueParentId, labelIterationId, mediaId, lcEntryId, timestamp, labeler,
                            configurationId, entryTypeLcEntry, entryKeyLcEntry, entryValueLcEntry, valid, comment,
                            children, pointPojoViewModel, color, shortcut, pointChanges);
                }
                break;
            case POLYGON:
                if (lcEntryValue instanceof LcEntryPolygonValue) {
                    LcEntryPolygonValue lcEntryPolygonValue = (LcEntryPolygonValue) lcEntryValue;
                    LcEntryPolygon lcEntryPolygon = (LcEntryPolygon) Hibernate.unproxy(lcEntryValue.getLcEntry());
                    List<PolygonChangeViewModel> polygonChanges = lcEntryValue.getLcEntryValueChanges().stream()
                            .map(change -> (PolygonChangeViewModel) ValueChangeViewModelFactory.map(change))
                            .collect(Collectors.toList());

                    String color = lcEntryPolygon.getColor();
                    String shortcut = lcEntryPolygon.getShortcut();

                    List<PointPojoViewModel> pointPojoViewModels = lcEntryPolygonValue.getPoints().stream().map(
                            pointPojo -> {
                                String pointPojoId = pointPojo.getId();
                                Double pointPojoX = pointPojo.getX();
                                Double pointPojoY = pointPojo.getY();
                                return createPointPojoViewModel(pointPojoId, pointPojoX, pointPojoY);
                            }).collect(Collectors.toList());

                    return new PolygonValueViewModel(
                            id, lcEntryValueParentId, labelIterationId, mediaId, lcEntryId, timestamp, labeler,
                            configurationId, entryTypeLcEntry, entryKeyLcEntry, entryValueLcEntry, valid, comment,
                            children, pointPojoViewModels, color, shortcut, polygonChanges);
                }
                break;
            case IMAGE_SEGMENTATION:
                if (lcEntryValue instanceof LcEntryImageSegmentationValue) {
                    LcEntryImageSegmentationValue lcEntrySegmentationValue = (LcEntryImageSegmentationValue) lcEntryValue;
                    LcEntryImageSegmentation lcEntryPolygon = (LcEntryImageSegmentation) Hibernate.unproxy(lcEntryValue.getLcEntry());

                    String color = lcEntryPolygon.getColor();
                    String shortcut = lcEntryPolygon.getShortcut();

                    List<PointCollectionViewModel> pointsCollection = lcEntrySegmentationValue.getPointsCollection().stream().map(
                            pointPojo -> {
                                PointCollectionViewModel pointCollectionViewModel = new PointCollectionViewModel();
                                pointCollectionViewModel.setId(pointPojo.getId());
                                pointCollectionViewModel.setPoints(pointPojo.getPoints().stream().map(point -> createPointPojoViewModel(
                                        point.getId(),
                                        point.getX(),
                                        point.getY())).collect(Collectors.toList()));
                                return pointCollectionViewModel;
                            }).collect(Collectors.toList());

                    return new ImageSegmentationValueViewModel(
                            id, lcEntryValueParentId, labelIterationId, mediaId, lcEntryId, timestamp, labeler,
                            configurationId, entryTypeLcEntry, entryKeyLcEntry, entryValueLcEntry, valid, comment,
                            children, pointsCollection, color, shortcut);
                }
                break;
            case RECTANGLE:
                if (lcEntryValue instanceof LcEntryRectangleValue) {
                    LcEntryRectangleValue lcEntryRectangleValue = (LcEntryRectangleValue) lcEntryValue;
                    LcEntryRectangle lcEntryRectangle = (LcEntryRectangle) Hibernate.unproxy(lcEntryValue.getLcEntry());
                    List<RectangleChangeViewModel> rectagleChanges = lcEntryValue.getLcEntryValueChanges().stream()
                            .map(change -> (RectangleChangeViewModel) ValueChangeViewModelFactory.map(change))
                            .collect(Collectors.toList());

                    String color = lcEntryRectangle.getColor();
                    String shortcut = lcEntryRectangle.getShortcut();

                    Double width = lcEntryRectangleValue.getWidth();
                    Double height = lcEntryRectangleValue.getHeight();

                    String pointId = lcEntryRectangleValue.getId();
                    Double x = lcEntryRectangleValue.getX();
                    Double y = lcEntryRectangleValue.getY();
                    PointPojoViewModel pointPojoViewModel = createPointPojoViewModel(pointId, x, y);

                    return new RectangleValueViewModel(
                            id, lcEntryValueParentId, labelIterationId, mediaId, lcEntryId, timestamp, labeler,
                            configurationId, entryTypeLcEntry, entryKeyLcEntry, entryValueLcEntry, valid, comment,
                            children, pointPojoViewModel, width, height, color, shortcut, rectagleChanges);
                }
                break;
            case LINE:
                if (lcEntryValue instanceof LcEntryLineValue) {
                    LcEntryLineValue lcEntryLineValue = (LcEntryLineValue) lcEntryValue;
                    LcEntryLine lcEntryLine = (LcEntryLine) Hibernate.unproxy(lcEntryValue.getLcEntry());
                    List<LineChangeViewModel> lineChanges = lcEntryValue.getLcEntryValueChanges().stream()
                            .map(change -> (LineChangeViewModel) ValueChangeViewModelFactory.map(change))
                            .collect(Collectors.toList());

                    String color = lcEntryLine.getColor();
                    String shortcut = lcEntryLine.getShortcut();

                    List<PointPojoViewModel> pointPojoViewModels = lcEntryLineValue.getPoints().stream().map(pointPojo -> {
                        String pointPojoId = pointPojo.getId();
                        Double pointPojoX = pointPojo.getX();
                        Double pointPojoY = pointPojo.getY();
                        return createPointPojoViewModel(pointPojoId, pointPojoX, pointPojoY);
                    }).collect(Collectors.toList());

                    return new LineValueViewModel(
                            id, lcEntryValueParentId, labelIterationId, mediaId, lcEntryId, timestamp, labeler,
                            configurationId, entryTypeLcEntry, entryKeyLcEntry, entryValueLcEntry, valid, comment,
                            children, pointPojoViewModels, color, shortcut, lineChanges);
                }
                break;
            default:
                throw new GenericException("entry_type_not_found", null, null, "entry type");
        }

        throw new GenericException("entry_type_not_found", null, null, "entry type");
    }

    private static PointPojoViewModel createPointPojoViewModel(String pointId, Double x, Double y) {
        return new PointPojoViewModel(pointId, x, y);
    }

}
