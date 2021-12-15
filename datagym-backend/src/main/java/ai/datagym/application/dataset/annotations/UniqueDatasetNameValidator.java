package ai.datagym.application.dataset.annotations;

import ai.datagym.application.dataset.models.dataset.bindingModels.DatasetCreateBindingModel;
import ai.datagym.application.dataset.service.dataset.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class UniqueDatasetNameValidator implements ConstraintValidator<UniqueDatasetName, Object> {
    private final DatasetService datasetService;

    @Autowired
    public UniqueDatasetNameValidator(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @Override
    public void initialize(UniqueDatasetName constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext constraintValidatorContext) {
        if (obj instanceof DatasetCreateBindingModel) {
            DatasetCreateBindingModel datasetCreateBindingModel = (DatasetCreateBindingModel) obj;
            String name = datasetCreateBindingModel.getName();
            String owner = datasetCreateBindingModel.getOwner();

            constraintValidatorContext.buildConstraintViolationWithTemplate("Dataset name already exists")
                    .addPropertyNode("name")
                    .addConstraintViolation();

            return datasetService.isDatasetNameUniqueAndDeletedFalse(name, owner);
        }

        return false;
    }
}

