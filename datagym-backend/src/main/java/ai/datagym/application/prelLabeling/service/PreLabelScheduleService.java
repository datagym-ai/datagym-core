package ai.datagym.application.prelLabeling.service;

import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.prelLabeling.entity.PreLabelMappingEntry;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public interface PreLabelScheduleService{
    void preLabelMedia(Media media, Map<String, Map<String, String>> requestedClasses, LabelTask labelTask, List<PreLabelMappingEntry> mappings, String owner) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException;
}
