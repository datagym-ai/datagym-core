package ai.datagym.application.labelTask.controller;

import ai.datagym.application.labelTask.models.viewModels.LabelTaskViewModel;
import ai.datagym.application.labelTask.models.viewModels.UserTaskViewModel;
import ai.datagym.application.labelTask.service.UserTaskService;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/user")
@Validated
public class UserTaskController {
    private final UserTaskService userTaskService;

    @Autowired
    public UserTaskController(UserTaskService userTaskService) {
        this.userTaskService = userTaskService;
    }

    @GetMapping(value = "/taskList")
    public List<UserTaskViewModel> getUserTasks() throws NoSuchMethodException, NoSuchAlgorithmException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        return userTaskService.getUserTasks();
    }

    @GetMapping(value = "/nextTask")
    public LabelTaskViewModel getNextTask() {
        return userTaskService.getNextTask(null);
    }

    @GetMapping(value = "/nextTask/{projectId}")
    public LabelTaskViewModel getNextTaskFromProject(@PathVariable("projectId") @NotBlank @Length(min = 1) String projectId) {
        return userTaskService.getNextTask(projectId);
    }

    @GetMapping(value = "/nextReview/{projectId}")
    public LabelTaskViewModel getNextReviewTaskFromProject(@PathVariable("projectId") @NotBlank @Length(min = 1) String projectId) {
        return userTaskService.getNextReviewTask(projectId);
    }
}
