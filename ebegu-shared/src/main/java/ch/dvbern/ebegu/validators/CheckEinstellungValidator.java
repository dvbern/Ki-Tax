/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.enums.EinstellungKey;

public class CheckEinstellungValidator implements ConstraintValidator<CheckEinstellung, Einstellung> {

	@Override
	public void initialize(CheckEinstellung constraintAnnotation) {
		// nop
	}

	/**
	 * For each EinstellungKey we can check in this method if the given value is right or not.
	 */
	@Override
	public boolean isValid(@Nonnull Einstellung instance, @Nullable ConstraintValidatorContext context) {
		if (instance.getKey() == EinstellungKey.BEGU_ANBIETEN_AB) {
			// date must be at the first day of any month
			return instance.getValueAsDate().getDayOfMonth() == 1;
		}
		return true;
	}
}
