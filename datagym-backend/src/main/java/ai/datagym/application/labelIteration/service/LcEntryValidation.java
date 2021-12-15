package ai.datagym.application.labelIteration.service;

import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntryChecklist;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntryFreeText;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntrySelect;
import ai.datagym.application.labelConfiguration.entity.geometry.*;
import ai.datagym.application.labelIteration.entity.FrameType;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.LcEntryValueChange;
import ai.datagym.application.labelIteration.entity.classification.*;
import ai.datagym.application.labelIteration.entity.geometry.*;
import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueUpdateBindingModel;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.lib.exception.GenericException;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static ai.datagym.application.utils.constants.CommonMessages.*;

@Service
public class LcEntryValidation {
    private static final String ENTRY_VALUE_EXCEPTION = "entry_value_validation";

    private final LcEntryValueRepository lcEntryValueRepository;

    public LcEntryValidation(
            LcEntryValueRepository lcEntryValueRepository) {
        this.lcEntryValueRepository = lcEntryValueRepository;
    }

    /**
     * Utility method to throw a exception
     *
     * @param throwExceptions Should the method throw an exception
     * @param entryType       The specific entry type like "RECTANGLE" or "FREETEXT"
     * @param lcEntryValueId  The specific lcEntryValueId for debugging purposes
     */
    private static void throwEntryValueValidationException(boolean throwExceptions, String entryType,
                                                           String lcEntryValueId) {
        if (throwExceptions) {
            throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, entryType, lcEntryValueId);
        }
    }

    /**
     * Check if the geometry-values containing an equal amount of start-end frame types or
     * at least a start_end-object
     *
     * @param lcEntryValue The specific lcEntryValue from a video project to validate.
     */
    public void validateFrameTypeCountPlausibility(LcEntryValue lcEntryValue) {
        if (lcEntryValue.getLcEntryValueParent() == null) {
            long countStartEnd = lcEntryValue.getLcEntryValueChanges().stream()
                    .filter(p -> p.getFrameType() == FrameType.START_END)
                    .count();

            long changeStart = lcEntryValue.getLcEntryValueChanges().stream()
                    .filter(p -> p.getFrameType() == FrameType.START)
                    .count();
            long changeEnd = lcEntryValue.getLcEntryValueChanges().stream()
                    .filter(p -> p.getFrameType() == FrameType.END)
                    .count();

            // There are no change objects
            if ((changeStart == 0 && changeEnd == 0) && countStartEnd == 0) {
                lcEntryValue.setValid(false);
                lcEntryValueRepository.save(lcEntryValue);
                throw new GenericException(ENTRY_VALUE_EXCEPTION,
                                           null,
                                           null,
                                           lcEntryValue.getLcEntry().getType().name(),
                                           lcEntryValue.getId());
            }
            // Mismatch between start-/end change objects
            if (changeStart != changeEnd) {
                throw new GenericException(ENTRY_VALUE_EXCEPTION,
                                           null,
                                           null,
                                           lcEntryValue.getLcEntry().getType().name(),
                                           lcEntryValue.getId());
            }

        }
    }

    /**
     * Validation of an lcEntryValue
     * - Checking required flag
     * - Checking plausible coordinates / points
     * - Checking valid keys (e.g select, checkbox)
     * - more...
     *
     * @param lcEntryValue    The specific lcEntryValue to validate
     * @param throwExceptions If the method gets fired from a frontend-action (e.g task-completion) there should be
     *                        visible errors for the user. For machine-tasks like the external-api or during a
     *                        label-configuration update the process should go on without throwing an error.
     */
    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    public void validateSingleEntryValueBeforeTaskCompletion(LcEntryValue lcEntryValue, boolean throwExceptions) {
        LcEntry lcEntry = (LcEntry) Hibernate.unproxy(lcEntryValue.getLcEntry());
        String entryType = lcEntryValue.getLcEntry().getType().name();

        switch (entryType) {
            case SELECT:
                if (lcEntry instanceof LcEntrySelect && lcEntryValue instanceof LcEntrySelectValue) {
                    LcEntrySelect lcEntrySelect = (LcEntrySelect) lcEntry;
                    LcEntrySelectValue lcEntrySelectValue = (LcEntrySelectValue) lcEntryValue;

                    boolean required = lcEntrySelect.isRequired();
                    String selectKey = lcEntrySelectValue.getSelectKey();

                    if (required && selectKey == null) {
                        lcEntrySelectValue.setValid(false);
                        lcEntryValueRepository.save(lcEntrySelectValue);
                        throwEntryValueValidationException(throwExceptions, entryType, lcEntryValue.getId());
                        return;
                    }

                    Map<String, String> selectOptions = lcEntrySelect.getOptions();

                    if ((required && !selectOptions.containsKey(selectKey))
                            || (!required && (!selectOptions.containsKey(selectKey) && selectKey != null))) {
                        lcEntrySelectValue.setValid(false);
                        lcEntrySelectValue.setSelectKey(null);
                        lcEntryValueRepository.save(lcEntrySelectValue);
                        throwEntryValueValidationException(throwExceptions, entryType, lcEntryValue.getId());
                        return;
                    }

                    if (!lcEntrySelectValue.isValid()) {
                        lcEntrySelectValue.setValid(true);
                        lcEntryValueRepository.save(lcEntrySelectValue);
                    }
                }
                break;
            case CHECKLIST:
                if (lcEntry instanceof LcEntryChecklist && lcEntryValue instanceof LcEntryCheckListValue) {
                    LcEntryChecklist lcEntryChecklist = (LcEntryChecklist) lcEntry;
                    LcEntryCheckListValue lcEntryCheckListValue = (LcEntryCheckListValue) lcEntryValue;

                    boolean required = lcEntryChecklist.isRequired();
                    List<String> checkedValues = lcEntryCheckListValue.getCheckedValues();
                    int checkListValuesSize = checkedValues.size();

                    if (required && checkListValuesSize == 0) {
                        lcEntryCheckListValue.setValid(false);
                        lcEntryValueRepository.save(lcEntryCheckListValue);
                        throwEntryValueValidationException(throwExceptions, entryType, lcEntryValue.getId());
                        return;
                    }

                    Map<String, String> checkListOptions = lcEntryChecklist.getOptions();

                    for (String checkedValue : checkedValues) {
                        if (required && !checkListOptions.containsKey(checkedValue)) {
                            lcEntryCheckListValue.setValid(false);
                            lcEntryValueRepository.save(lcEntryCheckListValue);
                            throwEntryValueValidationException(throwExceptions, entryType, lcEntryValue.getId());
                            return;
                        }
                    }

                    if (!lcEntryCheckListValue.isValid()) {
                        lcEntryCheckListValue.setValid(true);
                        lcEntryValueRepository.save(lcEntryCheckListValue);
                    }
                }
                break;
            case FREETEXT:
                if (lcEntry instanceof LcEntryFreeText && lcEntryValue instanceof LcEntryTextValue) {
                    LcEntryFreeText lcEntryFreeText = (LcEntryFreeText) lcEntry;
                    LcEntryTextValue lcEntryTextValue = (LcEntryTextValue) lcEntryValue;

                    boolean required = lcEntryFreeText.isRequired();
                    Integer maxLength = lcEntryFreeText.getMaxLength();
                    String freeText = lcEntryTextValue.getText();

                    if ((required && (freeText == null || freeText.length() > maxLength || freeText.length() <= 0)) ||
                            (freeText != null && !required && freeText.length() > maxLength)) {
                        lcEntryTextValue.setValid(false);
                        lcEntryValueRepository.save(lcEntryTextValue);
                        throwEntryValueValidationException(throwExceptions, entryType, lcEntryValue.getId());
                        return;
                    }

                    if (!lcEntryTextValue.isValid()) {
                        lcEntryTextValue.setValid(true);
                        lcEntryValueRepository.save(lcEntryTextValue);
                    }
                }
                break;
            case POINT:
                if (lcEntry instanceof LcEntryPoint && lcEntryValue instanceof LcEntryPointValue) {
                    LcEntryPointValue lcEntryPointValue = (LcEntryPointValue) lcEntryValue;

                    Double x = lcEntryPointValue.getX();
                    Double y = lcEntryPointValue.getY();

                    validatePoint(x, y, entryType, lcEntryPointValue, throwExceptions);

                    if (!lcEntryPointValue.isValid()) {
                        lcEntryPointValue.setValid(true);
                        lcEntryValueRepository.save(lcEntryPointValue);
                    }
                }
                break;
            case LINE:
                if (lcEntry instanceof LcEntryLine && lcEntryValue instanceof LcEntryLineValue) {
                    LcEntryLineValue lcEntryLineValue = (LcEntryLineValue) lcEntryValue;

                    List<PointPojo> points = lcEntryLineValue.getPoints();

                    if (points.size() < 2) {
                        lcEntryLineValue.setValid(false);
                        lcEntryValueRepository.save(lcEntryLineValue);
                        throwEntryValueValidationException(throwExceptions, entryType, lcEntryValue.getId());
                        return;
                    }

                    points.forEach(pointPojo -> {
                        Double x = pointPojo.getX();
                        Double y = pointPojo.getY();

                        validatePoint(x, y, entryType, lcEntryLineValue, throwExceptions);
                    });


                    if (!lcEntryLineValue.isValid()) {
                        lcEntryLineValue.setValid(true);
                        lcEntryValueRepository.save(lcEntryLineValue);
                    }
                }
                break;
            case POLYGON:
                if (lcEntry instanceof LcEntryPolygon && lcEntryValue instanceof LcEntryPolygonValue) {
                    LcEntryPolygonValue lcEntryPolygonValue = (LcEntryPolygonValue) lcEntryValue;

                    List<PointPojo> polygonPoints = lcEntryPolygonValue.getPoints();

                    if (polygonPoints.size() < 3) {
                        lcEntryPolygonValue.setValid(false);
                        lcEntryValueRepository.save(lcEntryPolygonValue);
                        throwEntryValueValidationException(throwExceptions, entryType, lcEntryValue.getId());
                        return;
                    }

                    polygonPoints.forEach(pointPojo -> {
                        Double x = pointPojo.getX();
                        Double y = pointPojo.getY();

                        validatePoint(x, y, entryType, lcEntryPolygonValue, throwExceptions);
                    });

                    if (!lcEntryPolygonValue.isValid()) {
                        lcEntryPolygonValue.setValid(true);
                        lcEntryValueRepository.save(lcEntryPolygonValue);
                    }
                }
                break;
            case IMAGE_SEGMENTATION:
                if (lcEntry instanceof LcEntryImageSegmentation && lcEntryValue instanceof LcEntryImageSegmentationValue) {
                    LcEntryImageSegmentationValue lcEntrySegmentationValue = (LcEntryImageSegmentationValue) lcEntryValue;

                    List<PointCollection> segmentationPointsCollection = lcEntrySegmentationValue.getPointsCollection();

                    if (segmentationPointsCollection.isEmpty()) {
                        lcEntrySegmentationValue.setValid(false);
                        lcEntryValueRepository.save(lcEntrySegmentationValue);
                        throwEntryValueValidationException(throwExceptions, entryType, lcEntryValue.getId());
                        return;
                    }


                    segmentationPointsCollection.forEach(pointPojo -> {
                        pointPojo.getPoints().forEach(p -> validatePoint(p.getX(),
                                                                         p.getY(),
                                                                         entryType,
                                                                         lcEntrySegmentationValue,
                                                                         throwExceptions));
                    });

                    if (!lcEntrySegmentationValue.isValid()) {
                        lcEntrySegmentationValue.setValid(true);
                        lcEntryValueRepository.save(lcEntrySegmentationValue);
                    }
                }
                break;
            case RECTANGLE:
                if (lcEntry instanceof LcEntryRectangle && lcEntryValue instanceof LcEntryRectangleValue) {
                    LcEntryRectangleValue lcEntryRectangleValue = (LcEntryRectangleValue) lcEntryValue;

                    Double x = lcEntryRectangleValue.getX();
                    Double y = lcEntryRectangleValue.getY();
                    Double height = lcEntryRectangleValue.getHeight();
                    Double width = lcEntryRectangleValue.getWidth();

                    if (x == null || y == null || height == null || width == null) {
                        lcEntryRectangleValue.setValid(false);
                        lcEntryValueRepository.save(lcEntryRectangleValue);
                        throwEntryValueValidationException(throwExceptions, entryType, lcEntryValue.getId());
                        return;
                    }

                    if (!lcEntryRectangleValue.isValid()) {
                        lcEntryRectangleValue.setValid(true);
                        lcEntryValueRepository.save(lcEntryRectangleValue);
                    }
                }
                break;

            default:
                throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, entryType);
        }
    }

    public void validateSingleVideoEntryValueBeforeTaskCompletion(LcEntryValueChange lcEntryValueChange) {
        LcEntry lcEntry = lcEntryValueChange.getLcEntryValue().getLcEntry();
        String entryType = lcEntryValueChange.getLcEntryValue().getLcEntry().getType().name();

        switch (entryType) {
            case SELECT:
                if (lcEntry instanceof LcEntrySelect && lcEntryValueChange instanceof LcEntrySelectChangeValue) {
                    LcEntrySelect lcEntrySelect = (LcEntrySelect) lcEntry;
                    LcEntrySelectChangeValue lcEntrySelectValue = (LcEntrySelectChangeValue) lcEntryValueChange;

                    boolean required = lcEntrySelect.isRequired();
                    String selectKey = lcEntrySelectValue.getSelectKey();

                    if (required && selectKey == null) {
                        throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, entryType);
                    }

                    Map<String, String> selectOptions = lcEntrySelect.getOptions();

                    if ((required && !selectOptions.containsKey(selectKey))
                            || (!required && (!selectOptions.containsKey(selectKey) && selectKey != null))) {
                        lcEntrySelectValue.getLcEntryValue().setValid(false);
                        lcEntryValueRepository.save(lcEntrySelectValue.getLcEntryValue());
                        throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, entryType);
                    }

                    boolean valid = lcEntrySelectValue.getLcEntryValue().isValid();

                    if (!valid) {
                        lcEntrySelectValue.getLcEntryValue().setValid(true);
                        lcEntryValueRepository.save(lcEntrySelectValue.getLcEntryValue());
                    }
                }
                break;
            case CHECKLIST:
                if (lcEntry instanceof LcEntryChecklist && lcEntryValueChange instanceof LcEntryCheckListChangeValue) {
                    LcEntryChecklist lcEntryChecklist = (LcEntryChecklist) lcEntry;
                    LcEntryCheckListChangeValue lcEntryCheckListValue = (LcEntryCheckListChangeValue) lcEntryValueChange;

                    boolean required = lcEntryChecklist.isRequired();
                    List<String> checkedValues = lcEntryCheckListValue.getCheckedValues();
                    int checkListValuesSize = checkedValues.size();

                    if (required && checkListValuesSize == 0) {
                        lcEntryCheckListValue.getLcEntryValue().setValid(false);
                        lcEntryValueRepository.save(lcEntryCheckListValue.getLcEntryValue());
                        throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, entryType);
                    }

                    Map<String, String> checkListOptions = lcEntryChecklist.getOptions();

                    for (String checkedValue : checkedValues) {
                        if (required && !checkListOptions.containsKey(checkedValue)) {
                            lcEntryCheckListValue.getLcEntryValue().setValid(false);
                            lcEntryValueRepository.save(lcEntryCheckListValue.getLcEntryValue());
                            throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, entryType);
                        }
                    }

                    boolean valid = lcEntryCheckListValue.getLcEntryValue().isValid();

                    if (!valid) {
                        lcEntryCheckListValue.getLcEntryValue().setValid(true);
                        lcEntryValueRepository.save(lcEntryCheckListValue.getLcEntryValue());
                    }
                }
                break;
            case FREETEXT:
                if (lcEntry instanceof LcEntryFreeText && lcEntryValueChange instanceof LcEntryTextChangeValue) {
                    LcEntryFreeText lcEntryFreeText = (LcEntryFreeText) lcEntry;
                    LcEntryTextChangeValue lcEntryTextValue = (LcEntryTextChangeValue) lcEntryValueChange;

                    boolean required = lcEntryFreeText.isRequired();
                    Integer maxLength = lcEntryFreeText.getMaxLength();
                    String freeText = lcEntryTextValue.getText();

                    if ((required && (freeText == null || freeText.length() > maxLength || freeText.length() <= 0)) ||
                            (freeText != null && !required && freeText.length() > maxLength)) {

                        lcEntryTextValue.getLcEntryValue().setValid(false);
                        lcEntryValueRepository.save(lcEntryTextValue.getLcEntryValue());
                        throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, entryType);
                    }

                    boolean valid = lcEntryTextValue.getLcEntryValue().isValid();

                    if (!valid) {
                        lcEntryTextValue.getLcEntryValue().setValid(true);
                        lcEntryValueRepository.save(lcEntryTextValue.getLcEntryValue());
                    }
                }
                break;
            case POINT:
                if (lcEntry instanceof LcEntryPoint && lcEntryValueChange instanceof LcEntryPointChangeValue) {
                    LcEntryPointChangeValue lcEntryPointValue = (LcEntryPointChangeValue) lcEntryValueChange;

                    Double x = lcEntryPointValue.getPoint().getX();
                    Double y = lcEntryPointValue.getPoint().getY();

                    validatePoint(x, y, entryType, lcEntryPointValue.getLcEntryValue(), true);

                    boolean valid = lcEntryPointValue.getLcEntryValue().isValid();

                    if (!valid) {
                        lcEntryPointValue.getLcEntryValue().setValid(true);
                        lcEntryValueRepository.save(lcEntryPointValue.getLcEntryValue());
                    }
                }
                break;
            case LINE:
                if (lcEntry instanceof LcEntryLine && lcEntryValueChange instanceof LcEntryLineChangeValue) {
                    LcEntryLineChangeValue lcEntryLineValue = (LcEntryLineChangeValue) lcEntryValueChange;

                    List<SimplePointPojo> points = lcEntryLineValue.getPoints();

                    if (points.size() < 2) {
                        throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, entryType);
                    }

                    points.forEach(pointPojo -> {
                        Double x = pointPojo.getX();
                        Double y = pointPojo.getY();

                        validatePoint(x, y, entryType, lcEntryLineValue.getLcEntryValue(), true);
                    });

                    boolean valid = lcEntryLineValue.getLcEntryValue().isValid();

                    if (!valid) {
                        lcEntryLineValue.getLcEntryValue().setValid(true);
                        lcEntryValueRepository.save(lcEntryLineValue.getLcEntryValue());
                    }
                }
                break;
            case POLYGON:
                if (lcEntry instanceof LcEntryPolygon && lcEntryValueChange instanceof LcEntryPolygonChangeValue) {
                    LcEntryPolygonChangeValue lcEntryPolygonValue = (LcEntryPolygonChangeValue) lcEntryValueChange;

                    List<SimplePointPojo> polygonPoints = lcEntryPolygonValue.getPoints();

                    if (polygonPoints.size() < 3) {
                        throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, entryType);
                    }

                    polygonPoints.forEach(pointPojo -> {
                        Double x = pointPojo.getX();
                        Double y = pointPojo.getY();

                        validatePoint(x, y, entryType, lcEntryPolygonValue.getLcEntryValue(), true);
                    });

                    boolean valid = lcEntryPolygonValue.getLcEntryValue().isValid();

                    if (!valid) {
                        lcEntryPolygonValue.getLcEntryValue().setValid(true);
                        lcEntryValueRepository.save(lcEntryPolygonValue.getLcEntryValue());
                    }
                }
                break;
            case RECTANGLE:
                if (lcEntry instanceof LcEntryRectangle && lcEntryValueChange instanceof LcEntryRectangleChangeValue) {
                    LcEntryRectangleChangeValue lcEntryRectangleValue = (LcEntryRectangleChangeValue) lcEntryValueChange;

                    Double x = lcEntryRectangleValue.getX();
                    Double y = lcEntryRectangleValue.getY();
                    Double height = lcEntryRectangleValue.getHeight();
                    Double width = lcEntryRectangleValue.getWidth();

                    if (x == null || y == null || height == null || width == null) {
                        lcEntryRectangleValue.getLcEntryValue().setValid(false);
                        lcEntryValueRepository.save(lcEntryRectangleValue.getLcEntryValue());
                        throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, entryType);
                    }

                    boolean valid = lcEntryRectangleValue.getLcEntryValue().isValid();

                    if (!valid) {
                        lcEntryRectangleValue.getLcEntryValue().setValid(true);
                        lcEntryValueRepository.save(lcEntryRectangleValue.getLcEntryValue());
                    }
                }
                break;

            default:
                throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, entryType);
        }
    }

    public void validateRequiredEntryValues(LcEntry lcEntry, LcEntryValueUpdateBindingModel node, String entryType) {
        switch (entryType) {
            case SELECT:
                if (lcEntry instanceof LcEntrySelect) {
                    LcEntrySelect lcEntrySelect = (LcEntrySelect) lcEntry;
                    boolean required = lcEntrySelect.isRequired();
                    String selectKey = node.getSelectKey();
                    if (required && selectKey == null) {
                        throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, entryType);
                    }
                }
                break;
            case CHECKLIST:
                if (lcEntry instanceof LcEntryChecklist) {
                    LcEntryChecklist lcEntryChecklist = (LcEntryChecklist) lcEntry;
                    boolean required = lcEntryChecklist.isRequired();
                    int checkListValuesSize = node.getCheckedValues().size();
                    if (required && checkListValuesSize == 0) {
                        throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, entryType);
                    }
                }
                break;
            case FREETEXT:
                if (lcEntry instanceof LcEntryFreeText) {
                    LcEntryFreeText lcEntryFreeText = (LcEntryFreeText) lcEntry;
                    boolean required = lcEntryFreeText.isRequired();
                    Integer maxLength = lcEntryFreeText.getMaxLength();
                    String freeText = node.getText();

                    if ((required && (freeText == null || freeText.length() > maxLength || freeText.length() <= 0)) ||
                            (freeText != null && !required && freeText.length() > maxLength)) {
                        throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, entryType);
                    }
                }
                break;
            default:
                break;
        }
    }


    private void validatePoint(Double x, Double y, String entryType, LcEntryValue lcEntryValue,
                               boolean throwException) {
        boolean valid = lcEntryValue.isValid();
        if (x == null || y == null) {
            if (valid) {
                lcEntryValue.setValid(false);
                lcEntryValueRepository.save(lcEntryValue);
            }

            if (throwException) {
                throw new GenericException(ENTRY_VALUE_EXCEPTION, null, null, entryType);
            }
        }
    }

}
