/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.validators;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuung;

/**
 * Stellt sicher, dass ein Platz das richtige Angebot gesetzt hat.
 */
public class CheckPlatzAndAngebottypValidator implements ConstraintValidator<CheckPlatzAndAngebottyp, AbstractPlatz> {

	@Override
	public boolean isValid(@Nonnull AbstractPlatz platz, @Nullable ConstraintValidatorContext context) {
		if (platz.getBetreuungsangebotTyp().isTagesschule()) {
			return platz instanceof AnmeldungTagesschule;
		} else if (platz.getBetreuungsangebotTyp().isFerieninsel()) {
			return platz instanceof AnmeldungFerieninsel;
		} else {
			return platz instanceof Betreuung;
		}
	}
}
