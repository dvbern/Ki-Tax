/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;

/**
 * Validator for LastenausgleichTagesschuleAngabenGemeindeContainer
 */
public class CheckLastenausgleichTagesschuleAngabenGemeindeValidator
	implements ConstraintValidator<CheckLastenausgleichTagesschuleAngabenGemeinde, LastenausgleichTagesschuleAngabenGemeindeContainer> {

	public CheckLastenausgleichTagesschuleAngabenGemeindeValidator() {
	}

	@Override
	public void initialize(CheckLastenausgleichTagesschuleAngabenGemeinde constraintAnnotation) {
		// nop
	}

	@Override
	public boolean isValid(@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer stammdaten, ConstraintValidatorContext context) {
		if (stammdaten.getStatus() != LastenausgleichTagesschuleAngabenGemeindeStatus.NEU) {
			// Falls der Status > NEU ist, muss AlleAngabenInKibonErfasst beantwortet sein
			return stammdaten.getAlleAngabenInKibonErfasst() != null;
		}
		return true;
	}
}
