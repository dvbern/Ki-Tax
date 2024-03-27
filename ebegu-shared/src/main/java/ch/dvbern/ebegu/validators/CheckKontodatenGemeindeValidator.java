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

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Validator for Betreuungspensen, checks that the entered betreuungspensum is bigger than the minimum
 * that is allowed for the Betreungstyp for a given date
 */
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CheckKontodatenGemeindeValidator implements ConstraintValidator<CheckKontodatenGemeinde, GemeindeStammdaten> {

	private final PrincipalBean principal;

	@Override
	public void initialize(CheckKontodatenGemeinde constraintAnnotation) {
		// nop
	}

	@Override
	public boolean isValid(@Nonnull GemeindeStammdaten stammdaten, ConstraintValidatorContext context) {

		// Mandant does not have edit permissions for these fields, so they must be able to
		// save them empty
		if (this.principal.isCallerInAnyOfRole(UserRole.getMandantRoles())) {
			return true;
		}

		if (!stammdaten.getGemeinde().isAngebotBG()) {
			// Keine Kontodaten benötigt
			return true;
		}

		return !StringUtils.isEmpty(stammdaten.getKontoinhaber())
			&& !StringUtils.isEmpty(stammdaten.getBic())
			&& stammdaten.getIban() != null;
	}
}
