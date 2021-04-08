/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import ch.dvbern.ebegu.entities.Gesuch;
import org.apache.commons.lang.StringUtils;

/**
 * Dieser Validator prüft, dass bei Online-Gesuchen für den GS 1 eine E-Mail Adresse erfasst wurde
 */
@SuppressWarnings({ "ConstantConditions", "PMD.CollapsibleIfStatements" })
public class CheckEmailGesuchstellerValidator implements ConstraintValidator<CheckEmailGesuchsteller, Gesuch> {

	@Override
	public void initialize(CheckEmailGesuchsteller constraintAnnotation) {
		//nop
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public boolean isValid(Gesuch gesuch, ConstraintValidatorContext context) {
		boolean valid = true;
		if (gesuch.getEingangsart().isOnlineGesuch() && !gesuch.getFall().isSozialdienstFall()) {
			if (gesuch.getGesuchsteller1() != null && gesuch.getGesuchsteller1().getGesuchstellerJA() != null) {
				return StringUtils.isNotEmpty(gesuch.getGesuchsteller1().getGesuchstellerJA().getMail());
			}
		}
		return valid;
	}
}
