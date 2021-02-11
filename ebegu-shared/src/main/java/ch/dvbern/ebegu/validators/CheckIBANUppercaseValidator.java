/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.validators;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

public class CheckIBANUppercaseValidator implements ConstraintValidator<CheckIBANUppercase, IBAN> {

	@Override
	public void initialize(CheckIBANUppercase constraintAnnotation) {
		// nop
	}

	@Override
	public boolean isValid(IBAN iban, ConstraintValidatorContext constraintValidatorContext) {
		String ibanString = iban.getIban();
		for(int i = 0; i < iban.getIban().length(); i++) {
			char current = ibanString.charAt(i);
			if(Character.isLetter(current) && Character.isLowerCase(current)) {
				return false;
			}
		}
		return true;
	}
}
