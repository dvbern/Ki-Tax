package ch.dvbern.ebegu.validators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CheckAhvFormatValidator implements ConstraintValidator<CheckAhvFormat, String> {

	@Override
	public void initialize(CheckAhvFormat constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
		int ahvlenght = 13;
		String startDigits = "756";
		int relevantDigitsSum = 0;
		List<Integer> digits = new ArrayList<>();
		for (char c : s.replace("\\.", "").toCharArray()) {
			digits.add(Integer.parseInt(String.valueOf(c)));
		}

		if (digits.size() != ahvlenght) {
			return false;
		}

		List<Integer> relevantDigits = new ArrayList<>(digits.subList(0, 12));
		Collections.reverse(relevantDigits);

		for (int i = 0; i < relevantDigits.size(); i++) {
			int multiplier = (i % 2 == 0) ? 3 : 1;
			relevantDigitsSum += relevantDigits.get(i) * multiplier;
		}

		int relevantDigitsRounded = (int) Math.ceil(relevantDigitsSum / 10.0) * 10;
		int calculatedDigit = relevantDigitsRounded - relevantDigitsSum;
		int checkDigit = digits.get(12);

		String startDigitsAHV = s.substring(0, 3);

		return checkDigit == calculatedDigit && startDigitsAHV.equals(startDigits);
	}
}
