package ai.datagym.application.labelIteration.models.bindingModels;

public class PointPojoBindingModel {
    private String id;

    private Double x;
    private Double y;

    public PointPojoBindingModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
