package ai.datagym.application.labelConfiguration.entity;

public enum LcEntryType {
    POINT, LINE, POLYGON, IMAGE_SEGMENTATION, RECTANGLE, SELECT, CHECKLIST, FREETEXT;

    public boolean isGeometryType() {
        if (this == POINT || this == LINE || this == POLYGON || this == RECTANGLE) {
            return true;
        }
        return false;
    }
}
