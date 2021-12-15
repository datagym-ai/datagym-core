package ai.datagym.application.labelIteration.models.viewModels.geometry;

import ai.datagym.application.labelIteration.models.change.viewModels.RectangleChangeViewModel;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;

import java.util.List;

public class RectangleValueViewModel extends LcEntryValueViewModel {
    private PointPojoViewModel point;
    private Double width;
    private Double height;
    private String color;
    private String shortcut;

    public RectangleValueViewModel() {
    }

    public RectangleValueViewModel(String id, String lcEntryValueParentId, String labelIterationId, String mediaId,
                                   String lcEntryId,
                                   Long timestamp, String labeler, String configurationId, String entryTypeLcEntry,
                                   String entryKeyLcEntry, String entryValueLcEntry, boolean valid, String comment,
                                   List<LcEntryValueViewModel> children, PointPojoViewModel point, Double width,
                                   Double height, String color, String shortcut,
                                   List<RectangleChangeViewModel> changes) {
        super(id, lcEntryValueParentId, labelIterationId, mediaId, lcEntryId, timestamp, labeler, configurationId,
                entryTypeLcEntry, entryKeyLcEntry, entryValueLcEntry, valid, comment, children, changes);
        this.point = point;
        this.width = width;
        this.height = height;
        this.color = color;
        this.shortcut = shortcut;
    }

    public PointPojoViewModel getPoint() {
        return point;
    }

    public void setPoint(PointPojoViewModel point) {
        this.point = point;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getShortcut() {
        return shortcut;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }
}
