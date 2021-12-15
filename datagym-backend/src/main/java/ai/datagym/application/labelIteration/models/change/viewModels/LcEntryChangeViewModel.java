package ai.datagym.application.labelIteration.models.change.viewModels;

import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelIteration.entity.FrameType;

public abstract class LcEntryChangeViewModel {

    private String id;

    private Integer frameNumber;

    private FrameType type;

    private String kind;

    public LcEntryChangeViewModel(String id, Integer frameNumber,
                                  FrameType type, LcEntryType kind) {
        this.id = id;
        this.frameNumber = frameNumber;
        this.type = type;
        this.kind = kind.name();
    }

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

    public FrameType getType() {
        return type;
    }

    public void setType(FrameType type) {
        this.type = type;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setKind(LcEntryType type) {
        this.kind = type.name();
    }

    @Override
    public String toString() {
        return "LcEntryChangeUpdateBindingModel{" +
                "id='" + id + '\'' +
                ", frame=" + frameNumber +
                ", frameType=" + type +
                '}';
    }
}
