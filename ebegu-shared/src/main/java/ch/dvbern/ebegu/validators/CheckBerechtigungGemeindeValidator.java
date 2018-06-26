/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import javax.annotation.Nullable;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.Berechtigung;

/**
 * Prueft, dass bei allen Gemeinde-abhaengigen Rollen mindestens eine Gemeinde gesetzt ist:
 * - UserRole.ADMIN
 * - UserRole.SACHBEARBEITER_JA
 * - UserRole.ADMINISTRATOR_SCHULAMT
 * - UserRole.SCHULAMT
 * - JURIST
 * - REVISOR
 * - STEUERAMT
 *
 * Bei allen anderen Rollen darf keine Gemeinde gesetzt sein
 * - UserRole.SUPER_ADMIN
 * - UserRole.GESUCHSTELLER
 * - UserRole.SACHBEARBEITER_TRAEGERSCHAFT
 * - UserRole.SACHBEARBEITER_INSTITUTION
 */
public class CheckBerechtigungGemeindeValidator implements ConstraintValidator<CheckBerechtigungGemeinde, Berechtigung> {

	@Override
	public void initialize(CheckBerechtigungGemeinde constraintAnnotation) {
		//nop
	}

	/**
	 * Prueft ob die Berechtigung gueltig ist
	 */
	@Override
	public boolean isValid(Berechtigung berechtigung, @Nullable ConstraintValidatorContext context) {
		switch(berechtigung.getRole()) {
		case ADMIN:
		case SACHBEARBEITER_JA:
		case ADMINISTRATOR_SCHULAMT:
		case SCHULAMT:
		case JURIST:
		case REVISOR:
		case STEUERAMT: {
			return berechtigung.getGemeindeList().size() >= 1;
		}
		case SUPER_ADMIN:
		case GESUCHSTELLER:
		case SACHBEARBEITER_TRAEGERSCHAFT:
		case SACHBEARBEITER_INSTITUTION: {
			return berechtigung.getGemeindeList().isEmpty();
		}
		default: {
			return false;
		}
		}
	}
}
