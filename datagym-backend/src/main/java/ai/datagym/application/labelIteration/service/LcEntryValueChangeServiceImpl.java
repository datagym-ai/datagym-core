package ai.datagym.application.labelIteration.service;


import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.LcEntryValueChange;
import ai.datagym.application.labelIteration.entity.classification.LcEntryCheckListChangeValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntrySelectChangeValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntryTextChangeValue;
import ai.datagym.application.labelIteration.entity.geometry.LcEntryLineChangeValue;
import ai.datagym.application.labelIteration.entity.geometry.LcEntryPointChangeValue;
import ai.datagym.application.labelIteration.entity.geometry.LcEntryPolygonChangeValue;
import ai.datagym.application.labelIteration.entity.geometry.LcEntryRectangleChangeValue;
import ai.datagym.application.labelIteration.factories.ValueChangeViewModelFactory;
import ai.datagym.application.labelIteration.models.change.create.*;
import ai.datagym.application.labelIteration.models.change.update.*;
import ai.datagym.application.labelIteration.models.change.viewModels.LcEntryChangeViewModel;
import ai.datagym.application.labelIteration.repo.LcEntryValueChangeRepository;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.project.entity.MediaType;
import ai.datagym.application.security.util.DataGymSecurity;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static ai.datagym.application.utils.constants.CommonMessages.*;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class LcEntryValueChangeServiceImpl implements LcEntryValueChangeService {

    private final LcEntryValueChangeRepository valueChangeRepository;
    private final LcEntryValueRepository lcEntryValueRepository;
    private final LcEntryValueChangeRepository lcEntryValueChangeRepository;

    public LcEntryValueChangeServiceImpl(LcEntryValueChangeRepository valueChangeRepository,
                                         LcEntryValueRepository lcEntryValueRepository,
                                         LcEntryValueChangeRepository lcEntryValueChangeRepository) {
        this.valueChangeRepository = valueChangeRepository;
        this.lcEntryValueRepository = lcEntryValueRepository;
        this.lcEntryValueChangeRepository = lcEntryValueChangeRepository;
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public LcEntryChangeViewModel createValueChange(LcEntryChangeCreateBindingModel changeCreate) {
        DataGymSecurity.isAuthenticated();

        LcEntryValue entryValue = getLcEntryValue(changeCreate.getLcEntryValueId());
        LcEntryValue entryRootValue = getLcEntryValue(changeCreate.getLcEntryRootParentValueId());

        if (entryRootValue.getLcEntryValueParent() != null) {
            throw new GenericException("invalid_root", null, null);
        }

        if (entryRootValue.getLabelIteration().getProject().getMediaType() != MediaType.VIDEO) {
            throw new GenericException("invalid_project_mediatype", null, null);
        }

        String projectOrganisation = entryRootValue.getLabelIteration().getProject().getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, false);
        String loggedInUserId = DataGymSecurity.getLoggedInUserId();

        LcEntryValueChange changeObject;
        String entryType = entryValue.getLcEntry().getType().name();
        switch (entryType) {
            case SELECT:
                changeObject = new LcEntrySelectChangeValue(entryValue, entryRootValue, changeCreate.getFrameNumber(),
                                                            changeCreate.getFrameType(),
                                                            loggedInUserId,
                                                            ((SelectChangeCreateBindingModel) changeCreate).getSelectKey());
                break;
            case CHECKLIST:
                changeObject = new LcEntryCheckListChangeValue(entryValue, entryRootValue, changeCreate.getFrameNumber(),
                                                               changeCreate.getFrameType(),
                                                               loggedInUserId,
                                                               ((ChecklistChangeCreateBindingModel) changeCreate).getCheckedValues());
                break;
            case FREETEXT:
                changeObject = new LcEntryTextChangeValue(entryValue, entryRootValue, changeCreate.getFrameNumber(),
                                                          changeCreate.getFrameType(),
                                                          loggedInUserId,
                                                          ((TextChangeCreateBindingModel) changeCreate).getText());
                break;
            case POINT:
                changeObject = new LcEntryPointChangeValue(entryValue, entryRootValue, changeCreate.getFrameNumber(),
                                                           changeCreate.getFrameType(),
                                                           loggedInUserId,
                                                           ((PointChangeCreateBindingModel) changeCreate).getPoint());
                break;
            case LINE:
                changeObject = new LcEntryLineChangeValue(entryValue, entryRootValue, changeCreate.getFrameNumber(),
                                                          changeCreate.getFrameType(),
                                                          loggedInUserId,
                                                          ((LineChangeCreateBindingModel) changeCreate).getPoints());
                break;
            case POLYGON:
                changeObject = new LcEntryPolygonChangeValue(entryValue,
                                                             entryRootValue,
                                                             changeCreate.getFrameNumber(),
                                                             changeCreate.getFrameType(),
                                                             loggedInUserId,
                                                             ((PolygonChangeCreateBindingModel) changeCreate).getPoints());
                break;
            case RECTANGLE:
                changeObject = new LcEntryRectangleChangeValue(entryValue,
                                                               entryRootValue,
                                                               changeCreate.getFrameNumber(),
                                                               changeCreate.getFrameType(),
                                                               loggedInUserId,
                                                               ((RectangleChangeCreateBindingModel) changeCreate).getX(),
                                                               ((RectangleChangeCreateBindingModel) changeCreate).getY(),
                                                               ((RectangleChangeCreateBindingModel) changeCreate).getWidth(),
                                                               ((RectangleChangeCreateBindingModel) changeCreate).getHeight());


                break;
            default:
                throw new GenericException("entry_type_not_found", null, null, "entry_type");
        }
        changeObject = lcEntryValueChangeRepository.save(changeObject);


        return ValueChangeViewModelFactory.map(changeObject);

    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public LcEntryChangeViewModel updateValueChange(String changeId, LcEntryChangeUpdateBindingModel updateData) {
        DataGymSecurity.isAuthenticated();

        LcEntryValueChange valueChange = getValueChangeRequired(changeId);

        String projectOrganisation = valueChange.getLcEntryRootValue().getLabelIteration().getProject().getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, false);

        valueChange.setFrame(updateData.getFrameNumber());
        valueChange.setFrameType(updateData.getFrameType());

        String entryType = valueChange.getLcEntryValue().getLcEntry().getType().name();
        switch (entryType) {
            case SELECT:
                SelectChangeUpdateBindingModel updateSelect = (SelectChangeUpdateBindingModel) updateData;
                ((LcEntrySelectChangeValue) valueChange).setSelectKey(updateSelect.getSelectKey());
                break;
            case CHECKLIST:
                ChecklistChangeUpdateBindingModel updateChecklist = (ChecklistChangeUpdateBindingModel) updateData;
                ((LcEntryCheckListChangeValue) valueChange).setCheckedValues(updateChecklist.getCheckedValues());
                break;
            case FREETEXT:
                TextChangeUpdateBindingModel updateFreetext = (TextChangeUpdateBindingModel) updateData;
                ((LcEntryTextChangeValue) valueChange).setText(updateFreetext.getText());
                break;
            case POINT:
                PointChangeUpdateBindingModel updatePoint = (PointChangeUpdateBindingModel) updateData;
                ((LcEntryPointChangeValue) valueChange).setPoint(updatePoint.getPoint());

                break;
            case LINE:
                LineChangeUpdateBindingModel updateLine = (LineChangeUpdateBindingModel) updateData;
                ((LcEntryLineChangeValue) valueChange).setPoints(updateLine.getPoints());
                break;
            case POLYGON:
                PolygonChangeUpdateBindingModel updatePolygon = (PolygonChangeUpdateBindingModel) updateData;
                ((LcEntryPolygonChangeValue) valueChange).setPoints(updatePolygon.getPoints());
                break;
            case RECTANGLE:
                RectangleChangeUpdateBindingModel updateRectangle = (RectangleChangeUpdateBindingModel) updateData;
                LcEntryRectangleChangeValue rectangleChangeValue = (LcEntryRectangleChangeValue) valueChange;
                rectangleChangeValue.setX(updateRectangle.getX());
                rectangleChangeValue.setY(updateRectangle.getY());
                rectangleChangeValue.setWidth(updateRectangle.getWidth());
                rectangleChangeValue.setHeight(updateRectangle.getHeight());
                break;
            default:
                throw new GenericException("entry_type_not_found", null, null, "entry_type");
        }
        valueChange = lcEntryValueChangeRepository.save(valueChange);
        return ValueChangeViewModelFactory.map(valueChange);
    }

    @AuthUser
    @AuthScope(any = {OAUTH_SCOPE_TYPE, TOKEN_SCOPE_TYPE})
    @Override
    public void deleteValueChange(String changeId) {
        DataGymSecurity.isAuthenticated();

        LcEntryValueChange valueChange = getValueChangeRequired(changeId);

        String projectOrganisation = valueChange.getLcEntryRootValue().getLabelIteration().getProject().getOwner();
        DataGymSecurity.isAdminOrUser(projectOrganisation, false);

        valueChangeRepository.delete(valueChange);
    }

    private LcEntryValueChange getValueChangeRequired(String id) {
        return valueChangeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Change", "id", "" + id));
    }

    private LcEntryValue getLcEntryValue(String lcEntryValueId) {
        return lcEntryValueRepository.findById(lcEntryValueId)
                .orElseThrow(() -> new NotFoundException("Label Entry Value", "id", "" + lcEntryValueId));
    }
}
