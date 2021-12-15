package ai.datagym.application.aiseg.model.preLabel;

import java.util.LinkedHashMap;
import java.util.Map;

public class PreLabelClassObjectsList {
    Map<String, PreLabelValueObject> singlePreLabelObject = new LinkedHashMap<>();

    public Map<String, PreLabelValueObject> getSinglePreLabelObject() {
        return singlePreLabelObject;
    }

    public void setSinglePreLabelObject(Map<String, PreLabelValueObject> singlePreLabelObject) {
        this.singlePreLabelObject = singlePreLabelObject;
    }
}
