package ai.datagym.application.labelIteration.models.viewModels.geometry;

import ai.datagym.application.labelIteration.models.change.viewModels.PolygonChangeViewModel;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;

import java.util.ArrayList;
import java.util.List;

public class PolygonValueViewModel extends LcEntryValueViewModel {
    private List<PointPojoViewModel> points = new ArrayList<>();
    private String color;
    private String shortcut;

    public PolygonValueViewModel() {
    }

    public PolygonValueViewModel(String id, String lcEntryValueParentId, String labelIterationId, String mediaId,
                                 String lcEntryId,
                                 Long timestamp, String labeler, String configurationId, String entryTypeLcEntry,
                                 String entryKeyLcEntry, String entryValueLcEntry, boolean valid, String comment,
                                 List<LcEntryValueViewModel> children, List<PointPojoViewModel> points, String color,
                                 String shortcut,
                                 List<PolygonChangeViewModel> changes) {
        super(id, lcEntryValueParentId, labelIterationId, mediaId, lcEntryId, timestamp, labeler, configurationId,
                entryTypeLcEntry, entryKeyLcEntry, entryValueLcEntry, valid, comment, children, changes);
        this.points = points;
        this.color = color;
        this.shortcut = shortcut;
    }

    public List<PointPojoViewModel> getPoints() {
        return points;
    }

    public void setPoints(List<PointPojoViewModel> points) {
        this.points = points;
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
