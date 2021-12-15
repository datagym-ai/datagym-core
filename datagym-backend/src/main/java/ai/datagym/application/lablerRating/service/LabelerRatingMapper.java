package ai.datagym.application.lablerRating.service;

import ai.datagym.application.lablerRating.entity.LabelerRating;
import ai.datagym.application.lablerRating.models.viewModels.LabelerRatingViewModel;
import ai.datagym.application.media.entity.Media;
import ai.datagym.application.project.entity.Project;

public final class LabelerRatingMapper {
    private LabelerRatingMapper() {
    }

    public static LabelerRatingViewModel mapToLabelerRatingViewModel(LabelerRating from) {
        LabelerRatingViewModel to = new LabelerRatingViewModel();

        to.setId(from.getId());
        to.setLabelerId(from.getLabeler());
        to.setProjectId(from.getProject().getId());
        to.setPositive(from.getPositive());
        to.setNegative(from.getNegative());

        return to;
    }

    public static LabelerRating mapToLabelerRating(LabelerRatingViewModel from, Project projectById, Media media) {
        LabelerRating to = new LabelerRating();

        to.setId(from.getId());
        to.setLabeler(from.getLabelerId());
        to.setProject(projectById);
        to.setMedia(media);
        to.setPositive(from.getPositive());
        to.setNegative(from.getNegative());

        return to;
    }
}
