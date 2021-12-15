package ai.datagym.application.lablerRating.service;

import ai.datagym.application.lablerRating.models.bindingModels.LabelerRatingUpdateBindingModel;

public interface LabelerRatingService {
    void addToPositive(LabelerRatingUpdateBindingModel labelerRatingUpdateBindingModel);

    void addToNegative(LabelerRatingUpdateBindingModel labelerRatingUpdateBindingModel);
}
