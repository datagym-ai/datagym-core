package ai.datagym.application.labelTask.service;

import ai.datagym.application.labelTask.entity.LabelTask;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;

public final class LabelTaskMapper {
    private LabelTaskMapper() {
    }

    public static LabelTaskViewModel mapToLabelTaskViewModel(LabelTask from) {
        LabelTaskViewModel to = new LabelTaskViewModel();

        to.setTaskId(from.getId());
        to.setMediaId(from.getMedia().getId());
        to.setMediaName(from.getMedia().getMediaName());
        to.setIterationId(from.getLabelIteration().getId());
        to.setIterationRun(from.getLabelIteration().getRun());
        to.setLabeler(from.getLabeler());
        to.setLabelTaskState(from.getLabelTaskState().name());
        to.setProjectId(from.getProject().getId());
        to.setProjectName(from.getProject().getName());
        to.setReviewComment(from.getReviewComment());
        to.setBenchmark(from.isBenchmark());
        to.setLabelTaskType(from.getLabelTaskType().name());
        to.setPreLabelState(from.getPreLabelState());
        to.setHasJsonUpload(from.hasJsonUpload());

        return to;
    }
}
