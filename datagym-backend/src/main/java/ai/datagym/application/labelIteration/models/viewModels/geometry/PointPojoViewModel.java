package ai.datagym.application.labelIteration.models.viewModels.geometry;

public class PointPojoViewModel {
    private String id;
    private Double x;
    private Double y;

    public PointPojoViewModel() {
    }

    public PointPojoViewModel(String id, Double x, Double y) {
        this.id = id;
        this.x = x;
        this.y = y;
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
