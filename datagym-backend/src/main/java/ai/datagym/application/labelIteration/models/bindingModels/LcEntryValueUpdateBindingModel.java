package ai.datagym.application.labelIteration.models.bindingModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LcEntryValueUpdateBindingModel {
    private String id;

    @NotNull
    @NotEmpty
    private String lcEntryId;

    @NotNull
    @NotEmpty
    private String labelTaskId;

    @Size(max = 128, message = "Comment must be less than 128 characters")
    private String comment;

    private String lcEntryValueParentId;

    private String text;

    @Pattern(regexp = "^[a-zA-Z0-9_ -]*$")
    private String selectKey;
    private Double x;
    private Double y;
    private Double width;
    private Double height;
    private List<String> checkedValues = new ArrayList<>();
    private List<PointPojoBindingModel> points = new ArrayList<>();
    private List<PointCollectionBindingModel> pointsCollection = new ArrayList<>();

    private LcEntryValueUpdateBindingModel parentEntry;
    private List<LcEntryValueUpdateBindingModel> children = new ArrayList<>();

    private boolean valid = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLcEntryId() {
        return lcEntryId;
    }

    public void setLcEntryId(String lcEntryId) {
        this.lcEntryId = lcEntryId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSelectKey() {
        return selectKey;
    }

    public void setSelectKey(String selectKey) {
        this.selectKey = selectKey;
    }

    public List<String> getCheckedValues() {
        return checkedValues;
    }

    public void setCheckedValues(List<String> checkedValues) {
        this.checkedValues = checkedValues;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public List<PointPojoBindingModel> getPoints() {
        return points;
    }

    public void setPoints(List<PointPojoBindingModel> points) {
        this.points = points;
    }

    public List<PointCollectionBindingModel> getPointsCollection() {
        return pointsCollection;
    }

    public void setPointsCollection(List<PointCollectionBindingModel> pointsCollection) {
        this.pointsCollection = pointsCollection;
    }

    public LcEntryValueUpdateBindingModel getParentEntry() {
        return parentEntry;
    }

    public void setParentEntry(LcEntryValueUpdateBindingModel parentEntry) {
        this.parentEntry = parentEntry;
    }

    public List<LcEntryValueUpdateBindingModel> getChildren() {
        return children;
    }

    public void setChildren(List<LcEntryValueUpdateBindingModel> children) {
        this.children = children;
    }

    public String getLcEntryValueParentId() {
        return lcEntryValueParentId;
    }

    public void setLcEntryValueParentId(String lcEntryValueParentId) {
        this.lcEntryValueParentId = lcEntryValueParentId;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getLabelTaskId() {
        return labelTaskId;
    }

    public void setLabelTaskId(String labelTaskId) {
        this.labelTaskId = labelTaskId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
