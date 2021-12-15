package ai.datagym.application.labelTask.controller;

import ai.datagym.application.labelTask.entity.LabelTaskState;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskCompleteBindingModel;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskMoveAllBindingModel;
import ai.datagym.application.labelTask.models.bindingModels.LabelTaskReviewBindingModel;
import ai.datagym.application.labelTask.models.viewModels.LabelModeDataViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskCompleteViewModel;
import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.labelTask.service.LabelTaskService;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;

@RestController
@RequestMapping(value = "/api/task")
@Validated
public class LabelTaskController {
    private final LabelTaskService labelTaskService;

    @Autowired
    public LabelTaskController(LabelTaskService labelTaskService) {
        this.labelTaskService = labelTaskService;
    }

    @PutMapping("/{id}/toWaiting")
    public LabelTaskViewModel moveTaskToWaiting(@PathVariable("id") @NotBlank @Length(min = 1) String taskId) throws IOException {
        return labelTaskService.moveTaskStateIfUserIsAdmin(taskId, LabelTaskState.WAITING);
    }

    @PutMapping("/{id}/toBacklog")
    public LabelTaskViewModel moveTaskToBacklog(@PathVariable("id") @NotBlank @Length(min = 1) String taskId) throws IOException {
        return labelTaskService.moveTaskStateIfUserIsAdmin(taskId, LabelTaskState.BACKLOG);
    }

    @PutMapping("/{id}/skipToWC")
    public LabelTaskViewModel moveTaskFromSkippedToWaitingChanged(@PathVariable("id") @NotBlank @Length(min = 1) String taskId) throws IOException {
        return labelTaskService.moveTaskStateIfUserIsAdmin(taskId, LabelTaskState.WAITING_CHANGED);
    }

    @PutMapping("/moveAll")
    public void moveAllTasks(@RequestBody @Valid LabelTaskMoveAllBindingModel labelTaskMoveAllBindingModel) {
        labelTaskService.moveAllTasks(labelTaskMoveAllBindingModel);
    }

    @GetMapping(value = "/{id}")
    public LabelModeDataViewModel getTask(@PathVariable("id") @NotBlank @Length(min = 1) String taskId) {
        return labelTaskService.getTask(taskId);
    }

    @PutMapping("/{id}/skipTask")
    public void skipTask(@PathVariable("id") @NotBlank @Length(min = 1) String taskId) throws IOException {
        labelTaskService.skipTask(taskId);
    }

    @PutMapping("/{id}/completeTask")
    public LabelTaskCompleteViewModel completeTask(@PathVariable("id") @NotBlank @Length(min = 1) String taskId,
                                                   @RequestBody @Valid LabelTaskCompleteBindingModel labelTaskCompleteBindingModel) throws IOException {
        return labelTaskService.completeTask(taskId, labelTaskCompleteBindingModel);
    }

    @PutMapping("/reviewedSuccess")
    public void reviewedSuccess(@RequestBody @Valid LabelTaskReviewBindingModel labelTaskReviewBindingModel) throws IOException {
         labelTaskService.reviewCompletion(labelTaskReviewBindingModel, true);
    }

    @PutMapping("/reviewedFailed")
    public void reviewedFailed(@RequestBody @Valid LabelTaskReviewBindingModel labelTaskReviewBindingModel) throws IOException {
        labelTaskService.reviewCompletion(labelTaskReviewBindingModel, false);
    }

    @PutMapping("/{taskId}/activateBenchmark")
    public void activateBenchmark(@PathVariable("taskId") @NotBlank @Length(min = 1) String taskId){
        labelTaskService.activateBenchmark(taskId);
    }

    @PutMapping("/{taskId}/deactivateBenchmark")
    public void deactivateBenchmark(@PathVariable("taskId") @NotBlank @Length(min = 1) String taskId){
        labelTaskService.deactivateBenchmark(taskId);
    }

    @PutMapping("/{taskId}/resetLabeler")
    public LabelTaskViewModel resetLabeler(@PathVariable("taskId") @NotBlank @Length(min = 1) String taskId){
        return labelTaskService.resetLabeler(taskId);
    }
}
