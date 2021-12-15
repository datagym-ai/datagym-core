package ai.datagym.application.aiseg.model.preLabel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreLabelResponse {
  private  Map<String, List<PreLabelClassObjectsList>> preLabelGeometryClass = new HashMap<>();

  public Map<String, List<PreLabelClassObjectsList>> getPreLabelGeometryClass() {
    return preLabelGeometryClass;
  }

  public void setPreLabelGeometryClass(Map<String, List<PreLabelClassObjectsList>> preLabelGeometryClass) {
    this.preLabelGeometryClass = preLabelGeometryClass;
  }
}
