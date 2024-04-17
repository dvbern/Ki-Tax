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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.Verfuegung;

/**
 * Eine Verfuegung muss zwingend mit einem Platz verknuepft sein. Dieser kann eine Betreuung
 * oder eine AnmeldungFerieninsel sein. Es darf nur ein Platz verknuepft sein
 */
public class CheckVerfuegungPlatzValidator implements ConstraintValidator<CheckVerfuegungPlatz, Verfuegung> {

	@Override
	public boolean isValid(Verfuegung verfuegung, ConstraintValidatorContext context) {
		boolean isBetreuung = verfuegung.getBetreuung() != null;
		boolean isTagesschule = verfuegung.getAnmeldungTagesschule() != null;
		return isBetreuung ^ isTagesschule;     // XOR
	}
}
