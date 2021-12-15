package ai.datagym.application.aiseg.entity;

public enum PreLabelModelName {

    RESNET50_COCO_BEST("maskrcnn_resnet50_coco_v0.2.0.h5");

    private String modelName;

    PreLabelModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }

}
