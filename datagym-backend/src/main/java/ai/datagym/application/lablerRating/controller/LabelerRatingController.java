package ai.datagym.application.lablerRating.controller;

import ai.datagym.application.lablerRating.models.bindingModels.LabelerRatingUpdateBindingModel;
import ai.datagym.application.lablerRating.service.LabelerRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/rating")
@Validated
public class LabelerRatingController {
    private final LabelerRatingService labelerRatingService;

    @Autowired
    public LabelerRatingController(LabelerRatingService labelerRatingService) {
        this.labelerRatingService = labelerRatingService;
    }

    @PutMapping("/positive")
    public void addToPositive(@RequestBody @Valid LabelerRatingUpdateBindingModel labelerRatingUpdateBindingModel) {
        labelerRatingService.addToPositive(labelerRatingUpdateBindingModel);
    }

    @PutMapping("/negative")
    public void addToNegative(@RequestBody @Valid LabelerRatingUpdateBindingModel labelerRatingUpdateBindingModel) {
        labelerRatingService.addToNegative(labelerRatingUpdateBindingModel);
    }
}
