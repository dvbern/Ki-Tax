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

import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.enums.IntegrationTyp;

import javax.annotation.Nonnull;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.util.Collection;

/**
 * Eine sprachliche Indikation kann erst ab dem zweiten Geburtstag beurteilt werden. Dies wird mit diesem Validator überprüft.
 */
public class CheckFachstellenFromDateValidator implements ConstraintValidator<CheckFachstellenFromDate, KindContainer> {

	@Override
	public boolean isValid(@Nonnull KindContainer kindContainer, ConstraintValidatorContext context) {
		if (kindContainer.getKindJA() == null
			|| kindContainer.getKindJA().getPensumFachstelle().isEmpty()
			|| isAllFachstelleNull(kindContainer.getKindJA().getPensumFachstelle())
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

	private boolean isAllFachstelleNull(Collection<PensumFachstelle> pensumFachstellen) {
		return pensumFachstellen.stream().noneMatch(pensumFachstelle -> pensumFachstelle.getFachstelle() != null);
	}
}
