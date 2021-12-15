package ai.datagym.application.labelIteration.models.change.viewModels;

import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelIteration.entity.FrameType;

public class RectangleChangeViewModel extends LcEntryChangeViewModel {
    private Double x;

    private Double y;

    private Double width;

    private Double height;

    public RectangleChangeViewModel(String id, Integer frame,
                                    FrameType frameType, Double x,
                                    Double y, Double width, Double height) {
        super(id, frame, frameType, LcEntryType.RECTANGLE);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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

    @Override
    public String toString() {
        return "RectangleChangeCreateBindingModel{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
