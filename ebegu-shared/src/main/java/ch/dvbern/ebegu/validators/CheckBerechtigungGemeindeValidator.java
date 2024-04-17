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
import ch.dvbern.ebegu.util.ValidationMessageUtil;

/**
 * Prueft, dass bei allen Gemeinde-abhaengigen Rollen mindestens eine Gemeinde gesetzt ist:
 * - UserRole.ADMIN_BG
 * - UserRole.SACHBEARBEITER_BG
 * - UserRole.ADMIN_TS
 * - UserRole.SACHBEARBEITER_TS
 * - ADMIN_GEMEINDE
 * - SACHBEARBEITER_GEMEINDE
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

	/**
	 * Prueft ob die Berechtigung gueltig ist
	 */
	@Override
	public boolean isValid(Berechtigung berechtigung, @Nullable ConstraintValidatorContext context) {
		if (berechtigung.getRole().isRoleGemeindeabhaengig() && berechtigung.getGemeindeList().isEmpty()) {
			setConstraintViolationMessage(context, true);
			return false;

		}
		if (!berechtigung.getRole().isRoleGemeindeabhaengig() && !berechtigung.getGemeindeList().isEmpty()) {
			setConstraintViolationMessage(context, false);
			return false;
		}
		return true;
	}

	private void setConstraintViolationMessage(@Nullable ConstraintValidatorContext context, boolean isGemeindeAbhaengig) {
		if (context != null) {
			String message = ValidationMessageUtil.getMessage("invalid_berechtigung_gemeinde_rules"); //by default gemeinde not allowed
			if (isGemeindeAbhaengig) {
				message = ValidationMessageUtil.getMessage("invalid_berechtigung_keine_gemeinde_rules");
			}
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(message)
				.addConstraintViolation();
		}
	}
}
