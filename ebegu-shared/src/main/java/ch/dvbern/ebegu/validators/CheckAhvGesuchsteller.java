package ch.dvbern.ebegu.validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckAhvGesuchstellerValidator.class)
public @interface CheckAhvGesuchsteller {

	String message() default "{ahv_gesuchsteller}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
