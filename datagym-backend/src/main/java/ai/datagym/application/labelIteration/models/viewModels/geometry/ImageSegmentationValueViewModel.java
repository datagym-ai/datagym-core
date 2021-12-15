package ai.datagym.application.labelIteration.models.viewModels.geometry;

import ai.datagym.application.labelIteration.models.viewModels.LcEntryValueViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageSegmentationValueViewModel extends LcEntryValueViewModel{
    private List<PointCollectionViewModel> pointsCollection = new ArrayList<>();
    private String color;
    private String shortcut;

    public ImageSegmentationValueViewModel() {
    }

    public ImageSegmentationValueViewModel(String id, String lcEntryValueParentId, String labelIterationId, String mediaId, String lcEntryId,
                                           Long timestamp, String labeler, String configurationId, String entryTypeLcEntry,
                                           String entryKeyLcEntry, String entryValueLcEntry, boolean valid, String comment,
                                           List<LcEntryValueViewModel> children, List<PointCollectionViewModel> pointsCollection, String color, String shortcut) {
        super(id, lcEntryValueParentId, labelIterationId, mediaId, lcEntryId, timestamp, labeler, configurationId,
                entryTypeLcEntry, entryKeyLcEntry, entryValueLcEntry, valid, comment, children, Collections.emptyList());
        this.pointsCollection = pointsCollection;
        this.color = color;
        this.shortcut = shortcut;
    }

    public List<PointCollectionViewModel> getPointsCollection() {
        return pointsCollection;
    }

    public void setPointsCollection(List<PointCollectionViewModel> pointsCollection) {
        this.pointsCollection = pointsCollection;
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
