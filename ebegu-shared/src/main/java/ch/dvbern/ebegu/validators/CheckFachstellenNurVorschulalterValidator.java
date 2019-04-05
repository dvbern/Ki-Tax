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
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.EinschulungTyp;

/**
 *  Fachstellen dürfen nur im Vorschulalter gesetzt werden: Eine soziale oder sprachliche Indikation
 *  nach Artikel 34d Absatz 1 Buchstabe f ASIV liegt vor bei einem Kind, das noch nicht in die Volksschule
 *  eingetreten ist. Dies wird mit diesem Validator überprüft.
 */
public class CheckFachstellenNurVorschulalterValidator implements ConstraintValidator<CheckFachstellenNurVorschulalter, KindContainer> {

	public CheckFachstellenNurVorschulalterValidator() {
	}

	@Override
	public void initialize(CheckFachstellenNurVorschulalter constraintAnnotation) {
		// nop
	}

	@Override
	public boolean isValid(@Nonnull KindContainer kindContainer, ConstraintValidatorContext context) {
		if (kindContainer.getKindJA() == null
			|| kindContainer.getKindJA().getPensumFachstelle() == null
			|| kindContainer.getKindJA().getPensumFachstelle().getFachstelle() == null
		) {
			// Kein PensumFachstelle
			return true;
		}
		return kindContainer.getKindJA().getEinschulungTyp() == EinschulungTyp.VORSCHULALTER;
	}
}
