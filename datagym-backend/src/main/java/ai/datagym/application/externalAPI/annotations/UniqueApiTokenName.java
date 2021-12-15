package ai.datagym.application.externalAPI.annotations;

import org.springframework.stereotype.Component;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
@Constraint(validatedBy = UniqueApiTokenNameValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueApiTokenName {
    String message() default "ApiToken name already exists.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload()default {};
}
