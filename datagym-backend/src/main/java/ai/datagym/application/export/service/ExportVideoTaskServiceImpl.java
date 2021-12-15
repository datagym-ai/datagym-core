package ai.datagym.application.export.service;


import ai.datagym.application.export.consumer.DataGymBinFileConsumer;
import ai.datagym.application.export.consumer.DataGymBinFileConsumerImpl;
import ai.datagym.application.labelIteration.entity.FrameType;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.LcEntryValueChange;
import ai.datagym.application.labelIteration.entity.classification.LcEntryCheckListChangeValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntrySelectChangeValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntryTextChangeValue;
import ai.datagym.application.labelIteration.entity.geometry.LcEntryLineChangeValue;
import ai.datagym.application.labelIteration.entity.geometry.LcEntryPointChangeValue;
import ai.datagym.application.labelIteration.entity.geometry.LcEntryPolygonChangeValue;
import ai.datagym.application.labelIteration.entity.geometry.LcEntryRectangleChangeValue;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.security.util.DataGymSecurity;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.lib.exception.GenericException;
import com.eforce21.lib.exception.NotFoundException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ai.datagym.application.utils.constants.CommonMessages.OAUTH_SCOPE_TYPE;
import static ai.datagym.application.utils.constants.CommonMessages.TOKEN_SCOPE_TYPE;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class ExportVideoTaskServiceImpl implements ExportVideoTaskService {


    public static final String VISIBLE_OBJECT_ID = "visibleObjectId";
    public static final int JSON_INDENT_FACTOR = 2;
    private final LabelTaskRepository labelTaskRepository;
    private final LcEntryValueRepository lcEntryValueRepository;

    public ExportVideoTaskServiceImpl(LabelTaskRepository labelTaskRepository,
                                      LcEntryValueRepository lcEntryValueRepository) {
        this.labelTaskRepository = labelTaskRepository;
        this.lcEntryValueRepository = lcEntryValueRepository;
    }

    @AuthUser
    @AuthScope(any = {TOKEN_SCOPE_TYPE, OAUTH_SCOPE_TYPE})
    public void exportSingleVideoTask(String labelTaskId, HttpServletResponse httpServletResponse) throws IOException {
        LabelTask labelTask = getLabelTaskById(labelTaskId);

        // Permissions check
        String owner = labelTask.getProject().getOwner();
        DataGymSecurity.isAdmin(owner, true);

        // Check is video project
        if (labelTask.getProject().getMediaType() != ai.datagym.application.project.entity.MediaType.VIDEO) {
            throw new GenericException("invalid_project_mediatype", null, null);
        }

        DataGymBinFileConsumer dataGymBinFileConsumer = new DataGymBinFileConsumerImpl(httpServletResponse, true);

        // Construct the fileName
        long currentTime = System.currentTimeMillis();
        String exportedFileName = "datagym_export_" + currentTime + "_" + labelTaskId + ".json";

        dataGymBinFileConsumer.onMetaData(exportedFileName, MediaType.APPLICATION_JSON_VALUE);


        String imageId = labelTask.getMedia().getId();
        String iterationId = labelTask.getLabelIteration().getId();

        List<LcEntryValue> allByLabelIterationIdAndImageId = lcEntryValueRepository
                .findAllByLabelIterationIdAndMediaIdAndLcEntryValueParentNull(iterationId, imageId);


        // Frame ID  - Array of visible objects
        Map<Integer, JSONArray> frames = new HashMap<>();
        // ObjectId - Map<Frame, Object>
        Map<String, Map<Integer, JSONObject>> objects = new HashMap<>();

        allByLabelIterationIdAndImageId.forEach(rootGeometry -> {
            String rootGeometryId = rootGeometry.getId();

            // Sort
            List<LcEntryValueChange> lcEntryValueChanges = rootGeometry.getLcEntryValueChanges();
            lcEntryValueChanges.sort(Comparator.comparingInt(LcEntryValueChange::getFrame));

            // Find out in which frames the geometries are visible
            buildJsonFramesPart(frames, rootGeometryId, lcEntryValueChanges);


            // Build a map of geometry changes by frame (e.g. points from polygon, rectangle, ..)
            Map<Integer, JSONObject> geometryChangesPerFrame = buildJsonGeometryChanges(lcEntryValueChanges);

            // Handle classifications
            Map<Integer, JSONObject> classificationChangesPerFrame = buildJsonClassifications(rootGeometry);

            // Combine geometry changes and classification changes
            for (Integer frame : classificationChangesPerFrame.keySet()) {
                // Get or create frame entry for current object
                JSONObject jsonFrameObject = geometryChangesPerFrame.get(frame);
                if (jsonFrameObject == null) {
                    geometryChangesPerFrame.putIfAbsent(frame, new JSONObject());
                    jsonFrameObject = geometryChangesPerFrame.get(frame);
                }

                // Get or create classification-object for current frame
                if (!geometryChangesPerFrame.get(frame).has("classifications")) {
                    jsonFrameObject.put("classifications",
                                        classificationChangesPerFrame.get(frame));
                }
            }

            // Add combined data to the objects stack
            objects.put(rootGeometryId, geometryChangesPerFrame);
        });


        // Construct final Json Object
        JSONObject objectChangesFinal = new JSONObject();
        objects.keySet().forEach(rootGeometryId -> {
            JSONObject fullRootGeometryDataObject = new JSONObject();

            Map<Integer, JSONObject> integerJSONObjectMap = objects.get(rootGeometryId);

            // Go through frames
            integerJSONObjectMap.keySet().forEach(frame -> fullRootGeometryDataObject.put(frame.toString(),
                                                                                          integerJSONObjectMap.get(frame)));


            objectChangesFinal.put(rootGeometryId, fullRootGeometryDataObject);
        });

        JSONObject exportJson = new JSONObject();
        exportJson.put("frames", frames);
        exportJson.put("object", objectChangesFinal);
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpServletResponse.getWriter().write(exportJson.toString(JSON_INDENT_FACTOR));
    }

    @NotNull
    private Map<Integer, JSONObject> buildJsonClassifications(LcEntryValue rootGeometry) {
        List<LcEntryValue> currentGeometryChildren = rootGeometry.getChildren().stream()
                .filter(lcEntryValue -> !lcEntryValue.getLcEntry().getType().isGeometryType())
                .collect(Collectors.toList());
        Map<Integer, JSONObject> classificationChangesPerFrame = new HashMap<>();

        currentGeometryChildren.forEach(singleClassification -> {
            String entryKey = singleClassification.getLcEntry().getEntryKey();
            List<LcEntryValueChange> classificationChanges = singleClassification.getLcEntryValueChanges();

            // Handling nesting level 0
            classificationChanges.forEach(classification -> {
                JSONArray changeJson = generateClassificationJsonObject(classification);

                if (classificationChangesPerFrame.get(classification.getFrame()) == null) {
                    classificationChangesPerFrame.put(classification.getFrame(),
                                                      new JSONObject().put(entryKey, changeJson));
                } else {
                    classificationChangesPerFrame.get(classification.getFrame()).put(entryKey, changeJson);
                }
            });

            // Handle nesting level 1 + 2
            handleClassificationNestingLevelOne(singleClassification.getChildren(), classificationChangesPerFrame);

        });
        return classificationChangesPerFrame;
    }


    /**
     * Creates a map of changes by frame for an specific geometry. A json-object looks like this:
     *
     * <code>
     * "rectangle": [{"w": 627,"x": 386,"h": 714,"y": 0}
     * </code>
     *
     * @param lcEntryValueChanges The specific changes of the root geometry
     * @return
     */
    private Map<Integer, JSONObject> buildJsonGeometryChanges(List<LcEntryValueChange> lcEntryValueChanges) {
        Map<Integer, JSONObject> geometryChangesPerFrame = new HashMap<>();
        lcEntryValueChanges.forEach(geometryChange -> {
            Integer geometryChangeFrame = geometryChange.getFrame();
            String geometryType = geometryChange.getLcEntryRootValue()
                    .getLcEntry().getType().toString().toLowerCase(Locale.ROOT);

            // Array of geometry changes
            JSONArray changesArray = new JSONArray().put(generateRootGeometryJsonObject(geometryChange));

            geometryChangesPerFrame.put(geometryChangeFrame, new JSONObject().put(geometryType, changesArray));
        });
        return geometryChangesPerFrame;
    }

    private void buildJsonFramesPart(Map<Integer, JSONArray> frames, String rootGeometryId,
                                     List<LcEntryValueChange> lcEntryValueChanges) {
        Iterator<LcEntryValueChange> iterator = lcEntryValueChanges.iterator();
        LcEntryValueChange previousChange = null;
        while (iterator.hasNext()) {
            LcEntryValueChange change = iterator.next();
            if (previousChange == null && change.getFrameType() == FrameType.START) {
                previousChange = change;
            }
            if (change.getFrameType() == FrameType.START_END) {
                JSONArray frameJsonObject = frames.get(change.getFrame());
                if (frameJsonObject != null) {
                    frameJsonObject.put(new JSONObject().put(VISIBLE_OBJECT_ID, rootGeometryId));
                } else {
                    frames.put(change.getFrame(), new JSONArray().put(new JSONObject().put(VISIBLE_OBJECT_ID,
                                                                                           rootGeometryId)));
                }
            }
            if (previousChange != null && change.getFrameType() == FrameType.END) {
                int startFrame = previousChange.getFrame();
                int endFrame = change.getFrame();
                IntStream.range(startFrame, endFrame + 1).forEach(value -> {
                    if (frames.get(value) != null) {
                        frames.get(value).put(new JSONObject().put(VISIBLE_OBJECT_ID, rootGeometryId));
                    } else {
                        frames.put(value, new JSONArray().put(new JSONObject().put(VISIBLE_OBJECT_ID, rootGeometryId)));
                    }
                });
                previousChange = null;
            }
        }
    }

    private void handleClassificationNestingLevelOne(List<LcEntryValue> parentClassificationChildren,
                                                     Map<Integer, JSONObject> parentChange) {

        // Handle child classifications
        for (LcEntryValue child : parentClassificationChildren) {
            JSONArray classificationArrayObject;
            for (LcEntryValueChange childChanges : child.getLcEntryValueChanges()) {
                classificationArrayObject = generateClassificationJsonObject(childChanges);

                Integer currentFrame = childChanges.getFrame();
                String parentEntryKey = child.getLcEntryValueParent().getLcEntry().getEntryKey();

                // Check or create the parent object
                JSONObject jsonParent = parentChange
                        .computeIfAbsent(currentFrame, integer -> new JSONObject().put(parentEntryKey,
                                                                                       new JSONArray().put(
                                                                                               "null")));
                jsonParent.getJSONArray(parentEntryKey)
                        .put(new JSONObject().put(child.getLcEntry().getEntryKey(), classificationArrayObject));
            }

            if (!child.getChildren().isEmpty()) {
                handleClassificationNestingLevelTwo(child.getChildren(), parentChange);
            }
        }
    }

    private void handleClassificationNestingLevelTwo(List<LcEntryValue> parentClassificationChildren,
                                                     Map<Integer, JSONObject> parentChange) {
        // Handle child classifications
        for (LcEntryValue child : parentClassificationChildren) {
            JSONArray classificationArrayObject;
            for (LcEntryValueChange childChanges : child.getLcEntryValueChanges()) {
                classificationArrayObject = generateClassificationJsonObject(childChanges);

                Integer currentFrame = childChanges.getFrame();
                String secondParentKey = child.getLcEntryValueParent().getLcEntryValueParent().getLcEntry().getEntryKey();

                // Check or create the "level ONE" parent object
                JSONObject jsonParent = parentChange
                        .computeIfAbsent(currentFrame,
                                         integer -> new JSONObject().put(secondParentKey,
                                                                         new JSONArray().put(
                                                                                 "null")));

                List<JSONObject> listOfParentObjects = new ArrayList<>();

                JSONArray jsonArray = jsonParent.getJSONArray(secondParentKey);

                for (Object object : jsonArray) {
                    if (object instanceof JSONObject) {
                        listOfParentObjects.add((JSONObject) object);
                    }
                }
                Optional<JSONObject> hasParent = listOfParentObjects.stream()
                        .filter(x -> x.has(child.getLcEntryValueParent().getLcEntry().getEntryKey())).findAny();

                // Check or create the "level TWO" parent object
                if (hasParent.isEmpty()) {
                    JSONArray jsonArray1 = new JSONArray();
                    jsonArray1.put("null");
                    jsonArray1.put(new JSONObject().put(child.getLcEntry().getEntryKey(),
                                                        classificationArrayObject));

                    jsonArray.put(new JSONObject().put(child.getLcEntryValueParent().getLcEntry().getEntryKey(),
                                                       jsonArray1));
                } else {
                    JSONArray jsonObject = hasParent.get().getJSONArray(child.getLcEntryValueParent().getLcEntry().getEntryKey());
                    jsonObject.put(new JSONObject().put(child.getLcEntry().getEntryKey(),
                                                        classificationArrayObject));
                }
            }

        }
    }

    private JSONArray generateClassificationJsonObject(LcEntryValueChange classificationChange) {
        JSONArray classificationArrayObject = new JSONArray();
        if (classificationChange instanceof LcEntryCheckListChangeValue) {
            LcEntryCheckListChangeValue lcEntryCheckListChangeValue = (LcEntryCheckListChangeValue) classificationChange;
            List<String> checkedValues = lcEntryCheckListChangeValue.getCheckedValues();
            if (checkedValues.isEmpty()) {
                classificationArrayObject.put("null");
            } else {
                for (String checkedValue : checkedValues) {
                    classificationArrayObject.put(checkedValue);
                }
            }
        } else if (classificationChange instanceof LcEntrySelectChangeValue) {
            LcEntrySelectChangeValue lcEntrySelectChangeValue = (LcEntrySelectChangeValue) classificationChange;
            String selectKey = lcEntrySelectChangeValue.getSelectKey();

            classificationArrayObject.put(selectKey);
        } else if (classificationChange instanceof LcEntryTextChangeValue) {
            LcEntryTextChangeValue lcEntryTextChangeValue = (LcEntryTextChangeValue) classificationChange;
            String text = lcEntryTextChangeValue.getText();

            classificationArrayObject.put(text);
        }
        return classificationArrayObject;
    }

    /**
     * Generates JSON for a SINGLE ROOT GEOMETRY-Value of the current LabelTask and writes it into the Output Stream
     */
    private JSONObject generateRootGeometryJsonObject(LcEntryValueChange change) {
        JSONObject geometryObject = new JSONObject();

        if (change instanceof LcEntryPolygonChangeValue) {
            LcEntryPolygonChangeValue polygonChangeValue = (LcEntryPolygonChangeValue) change;
            polygonChangeValue.getPoints().forEach(point -> {
                geometryObject.put("x", point.getX());
                geometryObject.put("Y", point.getY());
            });
        } else if (change instanceof LcEntryPointChangeValue) {
            LcEntryPointChangeValue lcEntryPointChangeValue = (LcEntryPointChangeValue) change;
            geometryObject.put("x", lcEntryPointChangeValue.getPoint().getX());
            geometryObject.put("Y", lcEntryPointChangeValue.getPoint().getY());
        } else if (change instanceof LcEntryLineChangeValue) {
            LcEntryLineChangeValue lcEntryLineChangeValue = (LcEntryLineChangeValue) change;
            lcEntryLineChangeValue.getPoints().forEach(point -> {
                geometryObject.put("x", point.getX());
                geometryObject.put("Y", point.getY());
            });
        } else if (change instanceof LcEntryRectangleChangeValue) {
            LcEntryRectangleChangeValue lcEntryRectangleChangeValue = (LcEntryRectangleChangeValue) change;

            Double xCoordinate = lcEntryRectangleChangeValue.getX();
            Double yCoordinate = lcEntryRectangleChangeValue.getY();
            Double width = lcEntryRectangleChangeValue.getWidth();
            Double height = lcEntryRectangleChangeValue.getHeight();

            if (xCoordinate != null && yCoordinate != null && xCoordinate >= 0.0 && yCoordinate >= 0.0 &&
                    width != null && height != null && width >= 0.0 && height >= 0.0
            ) {
                geometryObject.put("x", xCoordinate.intValue());
                geometryObject.put("y", yCoordinate.intValue());
                geometryObject.put("w", width.intValue());
                geometryObject.put("h", height.intValue());
            } else {
                geometryObject.put("x", "null");
                geometryObject.put("y", "null");
                geometryObject.put("w", "null");
                geometryObject.put("h", "null");
            }
        }
        return geometryObject;
    }

    private LabelTask getLabelTaskById(String labelTaskId) {
        return labelTaskRepository.findById(labelTaskId)
                .orElseThrow(() -> new NotFoundException("Task", "id", labelTaskId));
    }
}
