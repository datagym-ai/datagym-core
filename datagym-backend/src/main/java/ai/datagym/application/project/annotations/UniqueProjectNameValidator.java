package ai.datagym.application.project.annotations;

import ai.datagym.application.project.models.bindingModels.ProjectCreateBindingModel;
import ai.datagym.application.project.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class UniqueProjectNameValidator implements ConstraintValidator<UniqueProjectName, Object> {
    private final ProjectService projectService;

    @Autowired
    public UniqueProjectNameValidator(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext constraintValidatorContext) {
        if(obj instanceof ProjectCreateBindingModel){
            ProjectCreateBindingModel projectCreateBindingModel = (ProjectCreateBindingModel) obj;
            String name = projectCreateBindingModel.getName();
            String owner = projectCreateBindingModel.getOwner();

            boolean projectNameUniqueAndDeletedFalse = projectService.isProjectNameUniqueAndDeletedFalse(name, owner);

            constraintValidatorContext.buildConstraintViolationWithTemplate("Project name already exists")
                    .addPropertyNode("name")
                    .addConstraintViolation();

            return projectNameUniqueAndDeletedFalse;
        }

        return false;
    }
}

