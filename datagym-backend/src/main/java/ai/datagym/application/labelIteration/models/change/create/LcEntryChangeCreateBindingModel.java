package ai.datagym.application.labelIteration.models.change.create;

import ai.datagym.application.labelIteration.entity.FrameType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.validation.constraints.NotNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PointChangeCreateBindingModel.class, name = "POINT"),
        @JsonSubTypes.Type(value = LineChangeCreateBindingModel.class, name = "LINE"),
        @JsonSubTypes.Type(value = PolygonChangeCreateBindingModel.class, name = "POLYGON"),
        @JsonSubTypes.Type(value = RectangleChangeCreateBindingModel.class, name = "RECTANGLE"),
        @JsonSubTypes.Type(value = SelectChangeCreateBindingModel.class, name = "SELECT"),
        @JsonSubTypes.Type(value = ChecklistChangeCreateBindingModel.class, name = "CHECKLIST"),
        @JsonSubTypes.Type(value = TextChangeCreateBindingModel.class, name = "FREETEXT"),
})
public abstract class LcEntryChangeCreateBindingModel {

    @NotNull
    private String lcEntryValueId;

    @NotNull
    private String lcEntryRootParentValueId;

    private Integer frameNumber;

    private FrameType frameType;

    public String getLcEntryValueId() {
        return lcEntryValueId;
    }

    public void setLcEntryValueId(String lcEntryValueId) {
        this.lcEntryValueId = lcEntryValueId;
    }

    public String getLcEntryRootParentValueId() {
        return lcEntryRootParentValueId;
    }

    public void setLcEntryRootParentValueId(String lcEntryRootParentValueId) {
        this.lcEntryRootParentValueId = lcEntryRootParentValueId;
    }

    public Integer getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(Integer frameNumber) {
        this.frameNumber = frameNumber;
    }

    public FrameType getFrameType() {
        return frameType;
    }

    public void setFrameType(FrameType frameType) {
        this.frameType = frameType;
    }

    @Override
    public String toString() {
        return "LcEntryChangeCreateBindingModel{" +
                "lcEntryValueId='" + lcEntryValueId + '\'' +
                ", lcEntryRootParentValueId='" + lcEntryRootParentValueId + '\'' +
                ", frame=" + frameNumber +
                ", frameType=" + frameType +
                '}';
    }
}
