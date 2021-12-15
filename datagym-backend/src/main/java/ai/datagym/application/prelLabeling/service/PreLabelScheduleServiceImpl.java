package ai.datagym.application.prelLabeling.service;

import ai.datagym.application.aiseg.service.AiSegService;
import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.entity.PreLabelState;
import ai.datagym.application.limit.service.LimitService;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.prelLabeling.entity.PreLabelMappingEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;
import java.util.Map;

@Service
@Transactional(propagation = Propagation.REQUIRED)
@ConditionalOnProperty(
        value = "aiseglb.enabled",
        havingValue = "true")
public class PreLabelScheduleServiceImpl implements PreLabelScheduleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreLabelScheduleServiceImpl.class);

    private final AiSegService aiSegService;
    private final LimitService limitService;

    @Autowired
    public PreLabelScheduleServiceImpl(AiSegService aiSegService, LimitService limitService) {
        this.aiSegService = aiSegService;
        this.limitService = limitService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void preLabelMedia(Media media, Map<String, Map<String, String>> requestedClasses, LabelTask labelTask, List<PreLabelMappingEntry> mappings, String owner) {
        try {
            labelTask.setPreLabelState(PreLabelState.IN_PROGRESS);
            aiSegService.preLabelImage(media, requestedClasses, labelTask, mappings);
            limitService.decreaseAiSegRemaining(owner);
            labelTask.setPreLabelState(PreLabelState.FINISHED);
        } catch (Exception e) {
            LOGGER.error("PreLabelScheduleServiceImpl: Failed during preLabeling of the current media", e);
            labelTask.setPreLabelState(PreLabelState.FAILED);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }
}
