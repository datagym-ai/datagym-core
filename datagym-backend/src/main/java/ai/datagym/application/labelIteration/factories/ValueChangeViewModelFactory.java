package ai.datagym.application.labelIteration.factories;

import ai.datagym.application.labelIteration.entity.LcEntryValueChange;
import ai.datagym.application.labelIteration.entity.classification.LcEntryCheckListChangeValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntrySelectChangeValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntryTextChangeValue;
import ai.datagym.application.labelIteration.entity.geometry.LcEntryLineChangeValue;
import ai.datagym.application.labelIteration.entity.geometry.LcEntryPointChangeValue;
import ai.datagym.application.labelIteration.entity.geometry.LcEntryPolygonChangeValue;
import ai.datagym.application.labelIteration.entity.geometry.LcEntryRectangleChangeValue;
import ai.datagym.application.labelIteration.models.change.viewModels.*;
import com.eforce21.lib.exception.GenericException;

import static ai.datagym.application.utils.constants.CommonMessages.*;

public class ValueChangeViewModelFactory {

    public static LcEntryChangeViewModel map(LcEntryValueChange from) {
        String entryType = from.getLcEntryValue().getLcEntry().getType().name();

        switch (entryType) {
            case SELECT:
                LcEntrySelectChangeValue selectChangeValue = (LcEntrySelectChangeValue) from;
                return new SelectChangeViewModel(selectChangeValue.getId(),
                                                 selectChangeValue.getFrame(),
                                                 selectChangeValue.getFrameType(),
                                                 selectChangeValue.getSelectKey());
            case CHECKLIST:
                LcEntryCheckListChangeValue checkListChangeValue = (LcEntryCheckListChangeValue) from;
                return new ChecklistChangeViewModel(checkListChangeValue.getId(),
                                                    checkListChangeValue.getFrame(),
                                                    checkListChangeValue.getFrameType(),
                                                    checkListChangeValue.getCheckedValues());
            case FREETEXT:
                LcEntryTextChangeValue textChangeValue = (LcEntryTextChangeValue) from;
                return new TextChangeViewModel(textChangeValue.getId(),
                                               textChangeValue.getFrame(),
                                               textChangeValue.getFrameType(),
                                               textChangeValue.getText());
            case POINT:
                LcEntryPointChangeValue pointChangeValue = (LcEntryPointChangeValue) from;
                return new PointChangeViewModel(pointChangeValue.getId(),
                                                pointChangeValue.getFrame(),
                                                pointChangeValue.getFrameType(),
                                                pointChangeValue.getPoint());
            case LINE:
                LcEntryLineChangeValue lineChangeValue = (LcEntryLineChangeValue) from;
                return new LineChangeViewModel(lineChangeValue.getId(),
                                               lineChangeValue.getFrame(),
                                               lineChangeValue.getFrameType(),
                                               lineChangeValue.getPoints());
            case POLYGON:
                LcEntryPolygonChangeValue polygonChangeValue = (LcEntryPolygonChangeValue) from;
                return new PolygonChangeViewModel(polygonChangeValue.getId(),
                                                  polygonChangeValue.getFrame(),
                                                  polygonChangeValue.getFrameType(),
                                                  polygonChangeValue.getPoints());
            case RECTANGLE:
                LcEntryRectangleChangeValue rectangleChangeValue = (LcEntryRectangleChangeValue) from;
                return new RectangleChangeViewModel(rectangleChangeValue.getId(),
                                                    rectangleChangeValue.getFrame(),
                                                    rectangleChangeValue.getFrameType(),
                                                    rectangleChangeValue.getX(),
                                                    rectangleChangeValue.getY(),
                                                    rectangleChangeValue.getWidth(),
                                                    rectangleChangeValue.getHeight());
            default:
                throw new GenericException("entry_type_not_found", null, null, "entry_type");
        }
    }
}
