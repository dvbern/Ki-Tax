package ch.dvbern.ebegu.validators;

import java.util.Objects;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.Gesuch;

public class CheckAhvGesuchstellerValidator implements ConstraintValidator<CheckAhvGesuchsteller, Gesuch> {

	@Override
	public void initialize(CheckAhvGesuchsteller constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(Gesuch gesuch, ConstraintValidatorContext constraintValidatorContext) {
		if (gesuch.getGesuchsteller2() == null || gesuch.getGesuchsteller1() == null) {
			return true;
		}

		if (gesuch.getGesuchsteller2().getGesuchstellerJA().getSozialversicherungsnummer() != null) {
			return !Objects.equals(gesuch.getGesuchsteller2().getGesuchstellerJA().getSozialversicherungsnummer(),
				gesuch.getGesuchsteller1().getGesuchstellerJA().getSozialversicherungsnummer());
		} else {
			return true;
		}
	}
}

