package ai.datagym.application.labelIteration.models.viewModels.geometry;

import ai.datagym.application.labelIteration.models.change.viewModels.PointChangeViewModel;
import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;

import java.util.List;

public class PointValueViewModel extends LcEntryValueViewModel {
    private PointPojoViewModel point;
    private String color;
    private String shortcut;

    public PointValueViewModel() {
    }

    public PointValueViewModel(String id, String lcEntryValueParentId, String labelIterationId, String mediaId,
                               String lcEntryId,
                               Long timestamp, String labeler, String configurationId, String entryTypeLcEntry,
                               String entryKeyLcEntry, String entryValueLcEntry, boolean valid, String comment,
                               List<LcEntryValueViewModel> children, PointPojoViewModel point, String color,
                               String shortcut,
                               List<PointChangeViewModel> changes) {
        super(id, lcEntryValueParentId, labelIterationId, mediaId, lcEntryId, timestamp, labeler, configurationId,
                entryTypeLcEntry, entryKeyLcEntry, entryValueLcEntry, valid, comment, children, changes);
        this.point = point;
        this.color = color;
        this.shortcut = shortcut;
    }

    public PointPojoViewModel getPoint() {
        return point;
    }

    public void setPoint(PointPojoViewModel point) {
        this.point = point;
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
