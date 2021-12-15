package ai.datagym.application.externalAPI.annotations;

import ai.datagym.application.externalAPI.models.bindingModels.ApiTokenCreateBindingModel;
import ai.datagym.application.externalAPI.service.ApiTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class UniqueApiTokenNameValidator implements ConstraintValidator<UniqueApiTokenName, Object> {
    private final ApiTokenService apiTokenService;

    @Autowired
    public UniqueApiTokenNameValidator(ApiTokenService apiTokenService) {
        this.apiTokenService = apiTokenService;
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext constraintValidatorContext) {
        if(obj instanceof ApiTokenCreateBindingModel){
            ApiTokenCreateBindingModel apiTokenCreateBindingModel = (ApiTokenCreateBindingModel) obj;
            String name = apiTokenCreateBindingModel.getName();
            String owner = apiTokenCreateBindingModel.getOwner();

            boolean projectNameUniqueAndDeletedFalse = apiTokenService.isApiTokenNameUniqueAndDeletedFalse(name, owner);

            constraintValidatorContext.buildConstraintViolationWithTemplate("ApiToken name already exists")
                    .addPropertyNode("name")
                    .addConstraintViolation();

            return projectNameUniqueAndDeletedFalse;
        }

        return false;
    }
}

