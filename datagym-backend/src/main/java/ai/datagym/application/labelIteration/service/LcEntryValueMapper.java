package ai.datagym.application.labelIteration.service;

import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntryCheckListValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntrySelectValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntryTextValue;
import ai.datagym.application.labelIteration.entity.geometry.*;
import ai.datagym.application.labelIteration.models.bindingModels.*;
import ai.datagym.application.security.util.DataGymSecurity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class LcEntryValueMapper {
    private static final long CURRENT_TIME = System.currentTimeMillis();

    private LcEntryValueMapper() {
    }

    static LcEntryPointValue mapToPointValue(LcEntryValueUpdateBindingModel from) {
        LcEntryPointValue to = new LcEntryPointValue();

        to.setId(from.getId());
        to.setLabeler(DataGymSecurity.getLoggedInUserId());
        to.setTimestamp(CURRENT_TIME);
        to.setX(from.getX());
        to.setY(from.getY());
        to.setChildren(new ArrayList<>());
        to.setValid(from.isValid());
        to.setComment(from.getComment());

        return to;
    }

    static LcEntryLineValue mapToLineValue(LcEntryValueUpdateBindingModel from) {
        LcEntryLineValue to = new LcEntryLineValue();
        List<PointPojo> pointPojos = mapToPointPojo(from.getPoints(), to);

        to.setId(from.getId());
        to.setLabeler(DataGymSecurity.getLoggedInUserId());
        to.setTimestamp(CURRENT_TIME);
        to.setPoints(pointPojos);
        to.setChildren(new ArrayList<>());
        to.setValid(from.isValid());
        to.setComment(from.getComment());

        return to;
    }

    static LcEntryPolygonValue mapToPolygonValue(LcEntryValueUpdateBindingModel from) {
        LcEntryPolygonValue to = new LcEntryPolygonValue();
        List<PointPojo> pointPojos = mapToPointPojo(from.getPoints(), to);

        to.setId(from.getId());
        to.setLabeler(DataGymSecurity.getLoggedInUserId());
        to.setTimestamp(CURRENT_TIME);
        to.setPoints(pointPojos);
        to.setChildren(new ArrayList<>());
        to.setValid(from.isValid());
        to.setComment(from.getComment());

        return to;
    }

    static LcEntryImageSegmentationValue mapToImageSegmentationValue(LcEntryValueUpdateBindingModel from) {
        LcEntryImageSegmentationValue to = new LcEntryImageSegmentationValue();
        List<PointCollection> segmentationPointsCollection = from.getPointsCollection().stream().map(point -> {
            PointCollection pointCollection = new PointCollection();
            pointCollection.setId(point.getId());
            pointCollection.setLcEntryImageSegmentationValue(to);
            pointCollection.setPoints(point.getPoints().stream().map(spoint -> {
                PointPojo pointPojo = new PointPojo();
                pointPojo.setX(spoint.getX());
                pointPojo.setY(spoint.getY());
                pointPojo.setId(spoint.getId());
                pointPojo.setPointCollection(pointCollection);
                return pointPojo;
            }).collect(Collectors.toList()));

            return pointCollection;
        }).collect(Collectors.toList());

        to.setId(from.getId());
        to.setLabeler(DataGymSecurity.getLoggedInUserId());
        to.setTimestamp(CURRENT_TIME);
        to.setPointsCollection(segmentationPointsCollection);
        to.setChildren(new ArrayList<>());
        to.setValid(from.isValid());
        to.setComment(from.getComment());

        return to;
    }

    private static List<PointPojo> mapToPointPojo(List<PointPojoBindingModel> pointPojoBindingModels, LcEntryValue lcEntryValue) {
        List<PointPojo> pointPojos = new ArrayList<>();
        pointPojoBindingModels.forEach(pojoBindingModel -> {
            PointPojo pointPojo = new PointPojo();
            pointPojo.setX(pojoBindingModel.getX());
            pointPojo.setY(pojoBindingModel.getY());
            pointPojo.setId(pojoBindingModel.getId());
            if (lcEntryValue instanceof LcEntryPolygonValue) {
                LcEntryPolygonValue lcEntryPolygonValue = (LcEntryPolygonValue) lcEntryValue;
                pointPojo.setLcEntryPolygonValue(lcEntryPolygonValue);
            } else if (lcEntryValue instanceof LcEntryLineValue) {
                LcEntryLineValue lcEntryLineValue = (LcEntryLineValue) lcEntryValue;
                pointPojo.setLcEntryLineValue(lcEntryLineValue);
            }

            pointPojos.add(pointPojo);
        });

        return pointPojos;
    }

    static LcEntryRectangleValue mapToRectangleValue(LcEntryValueUpdateBindingModel from) {
        LcEntryRectangleValue to = new LcEntryRectangleValue();

        to.setId(from.getId());
        to.setLabeler(DataGymSecurity.getLoggedInUserId());
        to.setTimestamp(CURRENT_TIME);
        to.setX(from.getX());
        to.setY(from.getY());
        to.setWidth(from.getWidth());
        to.setHeight(from.getHeight());
        to.setChildren(new ArrayList<>());
        to.setValid(from.isValid());
        to.setComment(from.getComment());

        return to;
    }

    static LcEntrySelectValue mapToSelectValue(LcEntryValueUpdateBindingModel from) {
        LcEntrySelectValue to = new LcEntrySelectValue();

        to.setId(from.getId());
        to.setLabeler(DataGymSecurity.getLoggedInUserId());
        to.setTimestamp(CURRENT_TIME);
        to.setSelectKey(from.getSelectKey());
        to.setChildren(new ArrayList<>());
        to.setValid(from.isValid());
        to.setComment(from.getComment());

        return to;
    }

    static LcEntryCheckListValue mapToCheckListValue(LcEntryValueUpdateBindingModel from) {
        LcEntryCheckListValue to = new LcEntryCheckListValue();

        to.setId(from.getId());
        to.setLabeler(DataGymSecurity.getLoggedInUserId());
        to.setTimestamp(CURRENT_TIME);
        to.setCheckedValues(from.getCheckedValues());
        to.setChildren(new ArrayList<>());
        to.setValid(from.isValid());
        to.setComment(from.getComment());

        return to;
    }

    static LcEntryTextValue mapToTextValue(LcEntryValueUpdateBindingModel from) {
        LcEntryTextValue to = new LcEntryTextValue();

        to.setId(from.getId());
        to.setLabeler(DataGymSecurity.getLoggedInUserId());
        to.setTimestamp(CURRENT_TIME);
        to.setText(from.getText());
        to.setChildren(new ArrayList<>());
        to.setValid(from.isValid());
        to.setComment(from.getComment());

        return to;
    }

    public static LcEntryValueCreateBindingModel mapToLcEntryValueCreateBindingModel(LcEntryValueExtendBindingModel from) {
        LcEntryValueCreateBindingModel to = new LcEntryValueCreateBindingModel();

        to.setLcEntryId(from.getLcEntryId());
        to.setLcEntryValueParentId(null);
        to.setMediaId(from.getMediaId());
        to.setIterationId(from.getIterationId());
        to.setLabelTaskId(from.getLabelTaskId());

        return to;
    }

    public static LcEntryValueExtendBindingModel mapToLcEntryValueExtendBindingModel(LcEntryValueExtendAllBindingModel from) {
        LcEntryValueExtendBindingModel to = new LcEntryValueExtendBindingModel();

        to.setMediaId(from.getMediaId());
        to.setIterationId(from.getIterationId());
        to.setLabelTaskId(from.getLabelTaskId());
        to.setLcEntryParentId(null);
        to.setLcEntryId(null);

        return to;
    }
}
