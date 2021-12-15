package ai.datagym.application.limit.service;

import ai.datagym.application.accountmanagement.client.model.FeatureTO;
import ai.datagym.application.limit.models.DataGymPlanDetails;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataGymPlanDetailsMapper {

    public static void map(List<FeatureTO> from, DataGymPlanDetails to) throws NumberFormatException{
        Map<String, FeatureTO> featureNameMap = from.stream().collect(Collectors.toMap(FeatureTO::getName, feature -> feature));
        if (featureNameMap.get("PROJECTS") != null) {
            to.setProjects(Integer.parseInt(featureNameMap.get("PROJECTS").getValue()));
        }
        if (featureNameMap.get("LABELS") != null) {
            to.setLabels(Integer.parseInt(featureNameMap.get("LABELS").getValue()));
        }
        if (featureNameMap.get("STORAGE") != null) {
            to.setStorage(Integer.parseInt(featureNameMap.get("STORAGE").getValue()));
        }
        if (featureNameMap.get("AISEG") != null) {
            to.setAiseg(Integer.parseInt(featureNameMap.get("AISEG").getValue()));
        }
        if (featureNameMap.get("API_ACCESS") != null) {
            to.setApiAccess(Boolean.parseBoolean(featureNameMap.get("API_ACCESS").getValue()));
        }
        if (featureNameMap.get("EXTERNAL_STORAGE") != null) {
            to.setExternalStorage(Boolean.parseBoolean(featureNameMap.get("EXTERNAL_STORAGE").getValue()));
        }
    }
}
