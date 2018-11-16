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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.UnbezahlterUrlaub;

/**
 * Validator fuer unbezahlten Urlaub
 */
public class CheckUnbezahlterUrlaubValidator implements ConstraintValidator<CheckUnbezahlterUrlaub, Erwerbspensum> {

	@Override
	public void initialize(CheckUnbezahlterUrlaub constraintAnnotation) {
		// nop
	}

	@Override
	public boolean isValid(Erwerbspensum erwerbspensum, ConstraintValidatorContext context) {
		if (erwerbspensum != null && erwerbspensum.getUnbezahlterUrlaub() != null) {
			// ab: Muss grösser / gleich sein als das ab des Erwerbspensum
			// bis: Falls beim Erwerbspensum das bis gesetzt ist, darf dieses bis nicht grösser sein
			UnbezahlterUrlaub unbezahlterUrlaub = erwerbspensum.getUnbezahlterUrlaub();
			if (!erwerbspensum.getGueltigkeit().contains(unbezahlterUrlaub.getGueltigkeit())) {
				return false;
			}
			// Muss mind. 3 Monate umfassen
			return !unbezahlterUrlaub.getGueltigkeit().getGueltigBis().minusMonths(3).plusDays(1)
				.isBefore(unbezahlterUrlaub.getGueltigkeit().getGueltigAb());
		}
		return true;
	}
}
