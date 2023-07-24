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

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.IntegrationTyp;

import javax.annotation.Nonnull;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

/**
 * Eine sprachliche Indikation kann erst ab dem zweiten Geburtstag beurteilt werden. Dies wird mit diesem Validator überprüft.
 */
public class CheckFachstellenFromDateValidator implements ConstraintValidator<CheckFachstellenFromDate, KindContainer> {

	public CheckFachstellenFromDateValidator() {
	}

	@Override
	public void initialize(CheckFachstellenFromDate constraintAnnotation) {
		// nop
	}

	@Override
	public boolean isValid(@Nonnull KindContainer kindContainer, ConstraintValidatorContext context) {
		if (kindContainer.getKindJA() == null
			|| kindContainer.getKindJA().getPensumFachstelle().isEmpty()
		) {
			// Kein PensumFachstelle
			return true;
		}
		for (PensumFachstelle pensumFachstelle : kindContainer.getKindJA().getPensumFachstelle()) {
			if (pensumFachstelle.getIntegrationTyp() == IntegrationTyp.SPRACHLICHE_INTEGRATION) {
				final LocalDate geburtsdatumPlusMinAge = kindContainer.getKindJA().getGeburtsdatum().plusYears(2);
				final LocalDate fachstelleFrom = pensumFachstelle.getGueltigkeit().getGueltigAb();
				if (fachstelleFrom.isBefore(geburtsdatumPlusMinAge)) {
					return false;
				}
			}
		}
		return true;
	}
}
