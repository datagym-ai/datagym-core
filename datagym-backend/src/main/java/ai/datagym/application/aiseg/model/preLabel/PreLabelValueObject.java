package ai.datagym.application.aiseg.model.preLabel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PreLabelValueObject {
    private List<Map<String, Integer>> geometry = new ArrayList<>();
    private Map<String, Integer> classifications = new LinkedHashMap<>();

    public List<Map<String, Integer>> getGeometry() {
        return geometry;
    }

    public void setGeometry(List<Map<String, Integer>> geometry) {
        this.geometry = geometry;
    }

    public Map<String, Integer> getClassifications() {
        return classifications;
    }

    public void setClassifications(Map<String, Integer> classifications) {
        this.classifications = classifications;
    }
}
