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

package ch.dvbern.ebegu.validators.iban;

import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

public class CheckIBANNotQRValidator implements ConstraintValidator<CheckIBANNotQR, IBAN> {

	private static final Pattern QR_PATTERN = Pattern.compile("(LI|CH)\\d{2}3[01]\\d{3}\\w{12}");

	@Override
	public boolean isValid(@Nullable IBAN iban, @Nullable ConstraintValidatorContext constraintValidatorContext) {
		// we should allow nullable iban fields
		if (iban == null) {
			return true;
		}
		return !QR_PATTERN.matcher(iban.getIban()).matches();
	}
}
