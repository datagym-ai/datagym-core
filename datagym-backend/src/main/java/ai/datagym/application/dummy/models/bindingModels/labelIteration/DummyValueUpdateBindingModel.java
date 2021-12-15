package ai.datagym.application.dummy.models.bindingModels.labelIteration;

import ai.datagym.application.labelIteration.models.bindingModels.LcEntryValueUpdateBindingModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.modelmapper.ModelMapper;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DummyValueUpdateBindingModel {

    @JsonIgnore
    private String id;

    @JsonIgnore
    private String lcEntryId;

    @NotNull
    @NotEmpty
    private String labelTaskId;

    private String lcEntryValueParentId;

    private String text;
    private String selectKey;
    private Double x;
    private Double y;
    private Double width;
    private Double height;
    private DummyPointPojoBindingModel point;
    private List<DummyPointPojoBindingModel> points = new ArrayList<>();
    private List<String> checkedValues = new ArrayList<>();

    private String entryTypeLcEntry;
    private String entryKeyLcEntry;
    private String entryValueLcEntry;

    private DummyValueUpdateBindingModel parentEntry;
    private List<DummyValueUpdateBindingModel> children = new ArrayList<>();

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

    public List<DummyPointPojoBindingModel> getPoints() {
        return points;
    }

    public void setPoints(List<DummyPointPojoBindingModel> points) {
        this.points = points;
    }

    public DummyValueUpdateBindingModel getParentEntry() {
        return parentEntry;
    }

    public void setParentEntry(DummyValueUpdateBindingModel parentEntry) {
        this.parentEntry = parentEntry;
    }

    public List<DummyValueUpdateBindingModel> getChildren() {
        return children;
    }

    public void setChildren(List<DummyValueUpdateBindingModel> children) {
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

    public String getEntryTypeLcEntry() {
        return entryTypeLcEntry;
    }

    public void setEntryTypeLcEntry(String entryTypeLcEntry) {
        this.entryTypeLcEntry = entryTypeLcEntry;
    }

    public String getEntryKeyLcEntry() {
        return entryKeyLcEntry;
    }

    public void setEntryKeyLcEntry(String entryKeyLcEntry) {
        this.entryKeyLcEntry = entryKeyLcEntry;
    }

    public String getEntryValueLcEntry() {
        return entryValueLcEntry;
    }

    public void setEntryValueLcEntry(String entryValueLcEntry) {
        this.entryValueLcEntry = entryValueLcEntry;
    }

    public DummyPointPojoBindingModel getPoint() {
        return point;
    }

    public void setPoint(DummyPointPojoBindingModel point) {
        this.point = point;
    }

    public String getLabelTaskId() {
        return labelTaskId;
    }

    public void setLabelTaskId(String labelTaskId) {
        this.labelTaskId = labelTaskId;
    }

    public static void initMap(ModelMapper mapper){
        mapper.createTypeMap(DummyValueUpdateBindingModel.class, LcEntryValueUpdateBindingModel.class)
                .addMapping( src -> src.getPoint().getX(),
                        ((destination, value) -> destination.setX((Double) value)))
                .addMapping( src -> src.getPoint().getY(),
                        ((destination, value) -> destination.setY((Double) value)));
    }
}
