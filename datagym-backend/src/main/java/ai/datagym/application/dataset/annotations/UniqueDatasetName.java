package ai.datagym.application.dataset.annotations;

import org.springframework.stereotype.Component;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
@Constraint(validatedBy = UniqueDatasetNameValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueDatasetName {
    String message() default "Dataset name already exists.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
