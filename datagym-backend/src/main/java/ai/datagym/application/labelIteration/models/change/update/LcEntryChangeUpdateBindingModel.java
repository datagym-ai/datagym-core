package ai.datagym.application.labelIteration.models.change.update;

import ai.datagym.application.labelIteration.entity.FrameType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.validation.constraints.NotNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PointChangeUpdateBindingModel.class, name = "POINT"),
        @JsonSubTypes.Type(value = LineChangeUpdateBindingModel.class, name = "LINE"),
        @JsonSubTypes.Type(value = PolygonChangeUpdateBindingModel.class, name = "POLYGON"),
        @JsonSubTypes.Type(value = RectangleChangeUpdateBindingModel.class, name = "RECTANGLE"),
        @JsonSubTypes.Type(value = SelectChangeUpdateBindingModel.class, name = "SELECT"),
        @JsonSubTypes.Type(value = ChecklistChangeUpdateBindingModel.class, name = "CHECKLIST"),
        @JsonSubTypes.Type(value = TextChangeUpdateBindingModel.class, name = "FREETEXT"),
})
public abstract class LcEntryChangeUpdateBindingModel {

    @NotNull
    private String id;

    @NotNull
    private Integer frameNumber;

    @NotNull
    private FrameType frameType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        return "LcEntryChangeUpdateBindingModel{" +
                "id='" + id + '\'' +
                ", frame=" + frameNumber +
                ", frameType=" + frameType +
                '}';
    }
}
