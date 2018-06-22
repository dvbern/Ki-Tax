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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.Dossier;

/**
 * Validator der prueft, dass der eingegebene Verantwortlicher die richtige Role hat. SuperAdmin wird auch als Role zugelassen,
 * damit man auch Testfaelle erzeugen kann.
 */
public class CheckVerantwortlicherValidatorTS implements ConstraintValidator<CheckVerantwortlicherTS, Dossier> {

	@Override
	public void initialize(CheckVerantwortlicherTS constraintAnnotation) {
		// nop
	}

	@Override
	public boolean isValid(Dossier instance, ConstraintValidatorContext context) {
		return instance.getVerantwortlicherTS() == null || instance.getVerantwortlicherTS().getRole().isRoleSchulamt()
			|| instance.getVerantwortlicherTS().getRole().isSuperadmin();
	}
}
