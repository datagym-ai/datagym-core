package ai.datagym.application.export.service;

import ai.datagym.application.export.consumer.DataGymBinFileConsumer;
import ai.datagym.application.export.consumer.DataGymBinFileConsumerImpl;
import ai.datagym.application.labelConfiguration.entity.LcEntry;
import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelConfiguration.repo.LcEntryRepository;
import ai.datagym.application.labelIteration.entity.LcEntryValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntryCheckListValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntrySelectValue;
import ai.datagym.application.labelIteration.entity.classification.LcEntryTextValue;
import ai.datagym.application.labelIteration.entity.geometry.*;
import ai.datagym.application.labelIteration.repo.LcEntryValueRepository;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.repo.LabelTaskRepository;
import ai.datagym.application.media.entity.AwsS3Video;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.project.entity.Project;
import ai.datagym.application.security.util.DataGymSecurity;
import com.eforce21.cloud.login.client.aop.AuthScope;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.lib.exception.GenericException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static ai.datagym.application.utils.constants.CommonMessages.OAUTH_SCOPE_TYPE;
import static ai.datagym.application.utils.constants.CommonMessages.TOKEN_SCOPE_TYPE;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class ExportServiceImpl implements ExportService {
    private static final String ENTRY_TYPE_NOT_FOUND = "entry_type_not_found";
    private static final String ENTRY_TYPE = "entry type";

    private static final String POINT = "POINT";
    private static final String LINE = "LINE";
    private static final String POLYGON = "POLYGON";
    private static final String RECTANGLE = "RECTANGLE";

    private final LabelTaskRepository labelTaskRepository;
    private final LcEntryValueRepository lcEntryValueRepository;
    private final LcEntryRepository lcEntryRepository;
    private final ObjectMapper objectMapper;

    @Value("${datagym.api-url}")
    private String apiBaseUrl;

    public ExportServiceImpl(LabelTaskRepository labelTaskRepository,
                             LcEntryValueRepository lcEntryValueRepository,
                             LcEntryRepository lcEntryRepository,
                             ObjectMapper objectMapper) {
        this.labelTaskRepository = labelTaskRepository;
        this.lcEntryValueRepository = lcEntryValueRepository;
        this.lcEntryRepository = lcEntryRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Exports JSON-File with all {@link LcEntryValue}s of all {@link LabelTask}s with {@link LabelTaskState} 'COMPLETED', 'REVIEWED'
     * or 'SKIPPED' in the current project{@param Project}.
     */
    @AuthUser
    @AuthScope(any = {TOKEN_SCOPE_TYPE, OAUTH_SCOPE_TYPE})
    @Override
    public void exportJsonLabelsByProject(Project project, HttpServletResponse response) throws IOException {
        //Permissions check
        String projectOrganisation = project.getOwner();
        DataGymSecurity.isAdmin(projectOrganisation, false);

        DataGymBinFileConsumer dataGymBinFileConsumer = new DataGymBinFileConsumerImpl(response, true);

        // Construct the fileName
        long currentTime = System.currentTimeMillis();
        String projectName = project.getName();
        String projectId = project.getId();
        String exportedFileName = "datagym_export_" + currentTime + "_" + projectName + ".json";

        dataGymBinFileConsumer.onMetaData(exportedFileName, MediaType.APPLICATION_JSON_VALUE);

        List<LabelTaskState> labelTaskStateList = new ArrayList<>();
        labelTaskStateList.add(LabelTaskState.COMPLETED);
        labelTaskStateList.add(LabelTaskState.SKIPPED);
        labelTaskStateList.add(LabelTaskState.REVIEWED);

        List<LabelTask> allByProjectId = labelTaskRepository
                .findTasksByProjectIdAndTaskStateAndMediaDeleted(
                        projectId, labelTaskStateList, false);


        ServletOutputStream outputStream = response.getOutputStream();
        boolean isImageProject = project.getMediaType() == ai.datagym.application.project.entity.MediaType.IMAGE;
        generateOutputJson(allByProjectId, outputStream, project, isImageProject);
    }

    /**
     * Creates a new Instance of the JsonGenerator, Generates Wrapper-JSON-Object for the export
     * and writes it into the Output Stream
     */
    private void generateOutputJson(List<LabelTask> allByProjectId, ServletOutputStream outputStream, Project project,
                                    boolean isImageProject) throws IOException {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Create a JsonGenerator instance
        try (JsonGenerator jsonGenerator = objectMapper.getFactory().createGenerator(outputStream)) {

            // Configure the JsonGenerator to pretty print the output
            jsonGenerator.useDefaultPrettyPrinter();

            // Write the start array token
            jsonGenerator.writeStartArray();

            if (!allByProjectId.isEmpty()) {
                // Generate a List with all LcEntry-Classes and LcEntry-Types in the Project Label-Configuration
                jsonGenerator.writeStartObject();
                generateLabelClassificationJson(jsonGenerator, project);
                jsonGenerator.writeEndObject();

                // Iterate over the Tasks and write each value as a JSON object
                for (LabelTask labelTask : allByProjectId) {
                    jsonGenerator.writeStartObject();

                    exportTask(jsonGenerator, labelTask, isImageProject);

                    jsonGenerator.writeEndObject();
                }

            }

            // Write the end array token
            jsonGenerator.writeEndArray();
        }
    }

    /**
     * Generates JSON-Object with the current Project-Configuration and writes it into the Output Stream
     */
    private void generateLabelClassificationJson(JsonGenerator jsonGenerator, Project project) throws IOException {
        jsonGenerator.writeFieldName("label_classes");
        jsonGenerator.writeStartArray();

        String configId = project.getLabelConfiguration().getId();

        List<LcEntry> lcEntries = lcEntryRepository
                .findAllByConfigurationId(configId)
                .stream()
                .sorted(Comparator.comparing(LcEntry::getType))
                .collect(Collectors.toList());

        for (LcEntry lcEntry : lcEntries) {
            String className = lcEntry.getEntryKey();
            String entryType = lcEntry.getType().name();

            jsonGenerator.writeStartObject();

            jsonGenerator.writeStringField("class_name", className);
            if (entryType.equals(POLYGON) || entryType.equals(POINT) || entryType.equals(LINE) || entryType.equals(
                    RECTANGLE)) {
                jsonGenerator.writeStringField("geometry_type", entryType.toLowerCase());
            } else {
                jsonGenerator.writeStringField("classification_type", entryType.toLowerCase());
            }

            jsonGenerator.writeEndObject();
        }

        jsonGenerator.writeEndArray();
    }

    /**
     * Assembles the Export-JSON-Object for a single Task
     */
    private void exportTask(JsonGenerator jsonGenerator, LabelTask labelTask, boolean isImageProject)
            throws IOException {
        generateTaskHeaderJson(jsonGenerator, labelTask, isImageProject);
        if (isImageProject) {
            generateSegmentationUrls(jsonGenerator, labelTask);
            generateGlobalClassificationsJson(jsonGenerator, labelTask);
            generateLabelsJson(jsonGenerator, labelTask);
        }
    }

    /**
     * Generates JSON-Object for the Image-Id, Image-Name, Labeler and LabelTaskState
     * of the current LabelTask and writes it into the Output Stream
     */
    private void generateTaskHeaderJson(JsonGenerator jsonGenerator, LabelTask labelTask, boolean isImageProject)
            throws IOException {
        Media media = labelTask.getMedia();

        String mediaName = media.getMediaName();
        String mediaId = media.getId();
        String labeler = labelTask.getLabeler();
        String taskState = labelTask.getLabelTaskState().name().toLowerCase();


        jsonGenerator.writeStringField("internal_media_ID", mediaId);
        jsonGenerator.writeStringField("external_media_ID", mediaName);
        jsonGenerator.writeStringField("labeler", labeler);
        jsonGenerator.writeStringField("status", taskState);

        // For video-projects
        if (!isImageProject) {
            String divider;
            if (apiBaseUrl.endsWith("/")) {
                divider = "";
            } else {
                divider = "/";
            }
            String baseUrl = apiBaseUrl + divider + "exportVideoTask/" + labelTask.getId() + "/";
            jsonGenerator.writeStringField("task_export_url", baseUrl);
            if (media instanceof AwsS3Video) {
                AwsS3Video s3Video = (AwsS3Video) labelTask.getMedia();
                jsonGenerator.writeStringField("video_height", s3Video.getHeight().toString());
                jsonGenerator.writeStringField("video_width", s3Video.getWidth().toString());
                jsonGenerator.writeStringField("video_total_frames", s3Video.getTotalFrames().toString());
                jsonGenerator.writeStringField("video_frame_rate", s3Video.getrFrameRate());
                jsonGenerator.writeStringField("video_duration", s3Video.getDuration().toString());
            }
        }
    }

    /**
     * Generates JSON-Object for the segmentation urls to generate / stream the bitmaps
     * of the current LabelTask and writes it into the Output Stream. The url to the bitmaps
     * is like "/export/bitmap/{taskId}/{lcEntryId}"
     */
    private void generateSegmentationUrls(JsonGenerator jsonGenerator, LabelTask labelTask) throws IOException {

        List<String> entryKeys = labelTask.getEntryValues().stream()
                .filter(LcEntryValue::isValid)
                .map(LcEntryValue::getLcEntry)
                .filter(value -> value.getType() == LcEntryType.IMAGE_SEGMENTATION)
                .map(LcEntry::getEntryKey)
                .collect(Collectors.toList());

        if (entryKeys.isEmpty()) {
            return;
        }

        String divider;
        if (apiBaseUrl.endsWith("/")) {
            divider = "";
        } else {
            divider = "/";
        }
        String baseUrl = apiBaseUrl + divider + "export/bitmap/" + labelTask.getId() + "/";
        Set<String> entryIds = new HashSet<>(entryKeys);
        Set<String> urls = entryIds.stream().map(entryId ->
                                                         baseUrl + URLEncoder.encode(entryId, StandardCharsets.UTF_8)
        ).collect(Collectors.toSet());

        jsonGenerator.writeFieldName("segmentations");
        jsonGenerator.writeStartArray();
        for (String url : urls) {
            jsonGenerator.writeString(url);
        }
        jsonGenerator.writeEndArray();
    }

    /**
     * Generates JSON from the LcEntryValues for the Global Classifications
     * of the current LabelTask and writes it into the Output Stream
     */
    private void generateGlobalClassificationsJson(JsonGenerator jsonGenerator, LabelTask labelTask)
            throws IOException {
        String iterationId = labelTask.getLabelIteration().getId();
        String mediaId = labelTask.getMedia().getId();
        String labelTaskId = labelTask.getId();

        List<LcEntryType> classificationLcEntryTypes = new ArrayList<>();
        classificationLcEntryTypes.add(LcEntryType.CHECKLIST);
        classificationLcEntryTypes.add(LcEntryType.FREETEXT);
        classificationLcEntryTypes.add(LcEntryType.SELECT);

        List<LcEntryValue> rootClassificationValues = lcEntryValueRepository
                .getAllRootValuesFromType(iterationId, mediaId, classificationLcEntryTypes, labelTaskId);

        jsonGenerator.writeFieldName("global_classifications");
        jsonGenerator.writeStartObject();

        generateClassificationsTreeJson(jsonGenerator, rootClassificationValues);

        jsonGenerator.writeEndObject();
    }

    /**
     * Iterates over all rootClassificationValues
     */
    private void generateClassificationsTreeJson(JsonGenerator jsonGenerator,
                                                 List<LcEntryValue> rootClassificationValues) throws IOException {
        int treeHeight = 1;

        for (int i = 0; i < rootClassificationValues.size(); i++) {
            LcEntryValue currentRootClassificationEntryValue = rootClassificationValues.get(i);

            generateClassificationValues(currentRootClassificationEntryValue, treeHeight, jsonGenerator);
        }
    }

    /**
     * Iterates over the current element {@param node} and his children and
     * generates JSON-Object from the export-keys of the LcEntryEntries and LcEntryValues
     */
    private void generateClassificationValues(LcEntryValue node, int treeHeight, JsonGenerator jsonGenerator)
            throws IOException {
        if (node != null) {
            String entryKey = node.getLcEntry().getEntryKey();

            jsonGenerator.writeFieldName(entryKey);
            jsonGenerator.writeStartArray();

            generateClassificationsExportValuesJSON(node, jsonGenerator);
        }

        List<LcEntryValue> children = Objects.requireNonNull(node).getChildren();
        for (LcEntryValue child : children) {

            int newTreeHeight = treeHeight + 1;
            if (newTreeHeight > 4) {
                throw new GenericException("config_depth", null, null);
            }

            if (child.getChildren() != null) {
                // Recursive call - Keep converting until no more children
                jsonGenerator.writeStartObject();

                generateClassificationValues(child, newTreeHeight, jsonGenerator);

                jsonGenerator.writeEndObject();
            }
        }

        jsonGenerator.writeEndArray();
    }

    /**
     * Generates JSON-Objects from the export-keys of the LcEntryValues
     */
    private void generateClassificationsExportValuesJSON(LcEntryValue lcEntryValue, JsonGenerator jsonGenerator)
            throws IOException {
        LcEntryType type = lcEntryValue.getLcEntry().getType();

        switch (type) {
            case CHECKLIST:
                if (lcEntryValue instanceof LcEntryCheckListValue) {
                    LcEntryCheckListValue lcEntryCheckListValue = (LcEntryCheckListValue) lcEntryValue;
                    List<String> checkedValues = lcEntryCheckListValue.getCheckedValues();

                    if (checkedValues.isEmpty()) {
                        jsonGenerator.writeNull();
                    } else {
                        for (String checkedValue : checkedValues) {
                            jsonGenerator.writeString(checkedValue);
                        }
                    }
                }
                break;
            case SELECT:
                if (lcEntryValue instanceof LcEntrySelectValue) {
                    LcEntrySelectValue lcEntrySelectValue = (LcEntrySelectValue) lcEntryValue;
                    String selectKey = lcEntrySelectValue.getSelectKey();

                    jsonGenerator.writeString(selectKey);
                }
                break;
            case FREETEXT:
                if (lcEntryValue instanceof LcEntryTextValue) {
                    LcEntryTextValue lcEntryTextValue = (LcEntryTextValue) lcEntryValue;
                    String text = lcEntryTextValue.getText();

                    jsonGenerator.writeString(text);
                }
                break;
            default:
                throw new GenericException(ENTRY_TYPE_NOT_FOUND, null, null, ENTRY_TYPE);
        }
    }

    /**
     * Generates JSON-Object from the LcEntryValues for the Geometries of the current LabelTask
     * and writes it into the Output Stream
     */
    private void generateLabelsJson(JsonGenerator jsonGenerator, LabelTask labelTask) throws IOException {
        String iterationId = labelTask.getLabelIteration().getId();
        String imageId = labelTask.getMedia().getId();
        String labelTaskId = labelTask.getId();

        List<LcEntryType> geometryLcEntryTypes = new ArrayList<>();
        geometryLcEntryTypes.add(LcEntryType.LINE);
        geometryLcEntryTypes.add(LcEntryType.POINT);
        geometryLcEntryTypes.add(LcEntryType.POLYGON);
        geometryLcEntryTypes.add(LcEntryType.RECTANGLE);
        geometryLcEntryTypes.add(LcEntryType.IMAGE_SEGMENTATION);

        List<LcEntryValue> rootGeometryValues = lcEntryValueRepository
                .getAllRootValuesFromType(iterationId, imageId, geometryLcEntryTypes, labelTaskId);

        Map<String, List<LcEntryValue>> rootGeometryValuesGroupedByEntryKey = rootGeometryValues
                .stream()
                .collect(Collectors.groupingBy(lcEntryValue -> lcEntryValue.getLcEntry().getEntryKey()));


        jsonGenerator.writeFieldName("labels");
        jsonGenerator.writeStartObject();

        generateGeometriesJson(jsonGenerator, rootGeometryValuesGroupedByEntryKey);

        jsonGenerator.writeEndObject();
    }

    /**
     * Generates JSON from the Geometry-LcEntryValues of the current LabelTask and writes it into the Output Stream
     */
    private void generateGeometriesJson(JsonGenerator jsonGenerator,
                                        Map<String, List<LcEntryValue>> rootGeometryValuesGroupedByEntryKey)
            throws IOException {
        for (Map.Entry<String, List<LcEntryValue>> currentGeometryClassEntry : rootGeometryValuesGroupedByEntryKey.entrySet()) {
            String currentGeometryClassName = currentGeometryClassEntry.getKey();
            List<LcEntryValue> currentGeometryClassValues = currentGeometryClassEntry.getValue();

            jsonGenerator.writeFieldName(currentGeometryClassName);

            jsonGenerator.writeStartArray();
            generateGeometryValuesTreeJson(jsonGenerator, currentGeometryClassValues);
            jsonGenerator.writeEndArray();
        }
    }

    /**
     * Iterates over the ROOT-GEOMETRY-Values of the current LabelTask
     */
    private void generateGeometryValuesTreeJson(JsonGenerator jsonGenerator,
                                                List<LcEntryValue> currentGeometryClassValues) throws IOException {

        for (LcEntryValue currentGeometryClassValue : currentGeometryClassValues) {
            jsonGenerator.writeStartObject();
            LcEntryType type = currentGeometryClassValue.getLcEntry().getType();

            // Generates JSON-Object for the Geometry
            switch (type) {
                case RECTANGLE:
                    jsonGenerator.writeFieldName("rectangle");
                    break;
                case POLYGON:
                    jsonGenerator.writeFieldName("polygon");
                    break;
                case LINE:
                    jsonGenerator.writeFieldName("line");
                    break;
                case POINT:
                    jsonGenerator.writeFieldName("point");
                    break;
                case IMAGE_SEGMENTATION:
                    jsonGenerator.writeFieldName("image_segmentation");
                    break;
                default:
                    throw new GenericException(ENTRY_TYPE_NOT_FOUND, null, null, ENTRY_TYPE);
            }
            generateCurrentGeometryValuesJson(jsonGenerator, currentGeometryClassValue);


            // Generates JSON-Object for the nested Classifications in the current Geometry
            jsonGenerator.writeFieldName("classifications");
            jsonGenerator.writeStartObject();

            // Without nested geometries
            List<LcEntryValue> currentGeometryChildren = currentGeometryClassValue.getChildren().stream()
                    .filter(lcEntryValue -> !lcEntryValue.getLcEntry().getType().isGeometryType())
                    .collect(Collectors.toList());
            generateClassificationsTreeJson(jsonGenerator, currentGeometryChildren);

            jsonGenerator.writeEndObject();


            // Handle nested geometries (only possible on "second" nested level == without a parent)
            if (currentGeometryClassValue.getLcEntryValueParent() == null) {
                // Generates JSON-Object for the nested geometries in the current Geometry
                jsonGenerator.writeFieldName("nested_geometries");
                jsonGenerator.writeStartObject();

                Map<String, List<LcEntryValue>> collect = currentGeometryClassValue.getChildren().stream()
                        .filter(lcEntryValue -> lcEntryValue.getLcEntry().getType().isGeometryType())
                        .collect(Collectors.groupingBy(lc -> lc.getLcEntry().getEntryKey()));

                this.generateGeometriesJson(jsonGenerator, collect);
                jsonGenerator.writeEndObject();
            }


            jsonGenerator.writeEndObject();
        }
    }

    /**
     * Generates JSON for a SINGLE ROOT GEOMETRY-Value of the current LabelTask and writes it into the Output Stream
     */
    private void generateCurrentGeometryValuesJson(JsonGenerator jsonGenerator, LcEntryValue lcEntryValue)
            throws IOException {
        LcEntryType type = lcEntryValue.getLcEntry().getType();

        jsonGenerator.writeStartArray();

        switch (type) {
            case POINT:
                if (lcEntryValue instanceof LcEntryPointValue) {
                    LcEntryPointValue lcEntryPointValue = (LcEntryPointValue) lcEntryValue;

                    Double x = lcEntryPointValue.getX();
                    Double y = lcEntryPointValue.getY();

                    generatePointJsonObject(jsonGenerator, x, y);
                }
                break;
            case LINE:
                if (lcEntryValue instanceof LcEntryLineValue) {
                    LcEntryLineValue lcEntryLineValue = (LcEntryLineValue) lcEntryValue;

                    for (PointPojo pointPojo : lcEntryLineValue.getPoints()) {
                        Double x = pointPojo.getX();
                        Double y = pointPojo.getY();

                        generatePointJsonObject(jsonGenerator, x, y);
                    }
                }
                break;
            case POLYGON:
                if (lcEntryValue instanceof LcEntryPolygonValue) {
                    LcEntryPolygonValue lcEntryPolygonValue = (LcEntryPolygonValue) lcEntryValue;

                    for (PointPojo pointPojo : lcEntryPolygonValue.getPoints()) {
                        Double x = pointPojo.getX();
                        Double y = pointPojo.getY();

                        generatePointJsonObject(jsonGenerator, x, y);
                    }
                }
                break;
            case RECTANGLE:
                if (lcEntryValue instanceof LcEntryRectangleValue) {
                    LcEntryRectangleValue lcEntryRectangleValue = (LcEntryRectangleValue) lcEntryValue;

                    Double x = lcEntryRectangleValue.getX();
                    Double y = lcEntryRectangleValue.getY();
                    Double width = lcEntryRectangleValue.getWidth();
                    Double height = lcEntryRectangleValue.getHeight();

                    jsonGenerator.writeStartObject();

                    if (x != null && y != null && x >= 0.0 && y >= 0.0 &&
                            width != null && height != null && width >= 0.0 && height >= 0.0
                    ) {
                        jsonGenerator.writeNumberField("x", x.intValue());
                        jsonGenerator.writeNumberField("y", y.intValue());
                        jsonGenerator.writeNumberField("w", width.intValue());
                        jsonGenerator.writeNumberField("h", height.intValue());
                    } else {
                        jsonGenerator.writeNullField("x");
                        jsonGenerator.writeNullField("y");
                        jsonGenerator.writeNullField("w");
                        jsonGenerator.writeNullField("h");
                    }

                    jsonGenerator.writeEndObject();
                }
                break;
            case IMAGE_SEGMENTATION:
                if (lcEntryValue instanceof LcEntryImageSegmentationValue) {
                    LcEntryImageSegmentationValue lcEntryImageSegmentationValue = (LcEntryImageSegmentationValue) lcEntryValue;
                    for (PointCollection pointCollection : lcEntryImageSegmentationValue.getPointsCollection()) {
                        jsonGenerator.writeStartArray();
                        for (PointPojo pointPojo : pointCollection.getPoints()) {
                            Double x = pointPojo.getX();
                            Double y = pointPojo.getY();

                            generatePointJsonObject(jsonGenerator, x, y);
                        }
                        jsonGenerator.writeEndArray();
                    }
                }
                break;
            default:
                throw new GenericException(ENTRY_TYPE_NOT_FOUND, null, null, ENTRY_TYPE);
        }

        jsonGenerator.writeEndArray();
    }

    private void generatePointJsonObject(JsonGenerator jsonGenerator, Double x, Double y) throws IOException {
        jsonGenerator.writeStartObject();

        if (x != null && y != null && x >= 0.0 && y >= 0.0) {
            jsonGenerator.writeNumberField("x", x.intValue());
            jsonGenerator.writeNumberField("y", y.intValue());
        } else {
            jsonGenerator.writeNullField("x");
            jsonGenerator.writeNullField("y");
        }

        jsonGenerator.writeEndObject();
    }
}
