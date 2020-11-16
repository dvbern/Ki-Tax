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

import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;

public class CheckInstitutionExternalClientDatesOverlappingValidator  implements ConstraintValidator<CheckInstitutionExternalClientDatesOverlapping, Institution> {

	@Override
	public void initialize(CheckInstitutionExternalClientDatesOverlapping constraintAnnotation) {

	}

	@Override
	public boolean isValid(
		Institution institution,
		ConstraintValidatorContext constraintValidatorContext) {
		return !(checkOverlapping(institution.getInstitutionExternalClients()));
	}

	/**
	 * prueft ob es eine ueberschneidung zwischen den Zeitrauemen gibt
	 */
	private boolean checkOverlapping(Set<InstitutionExternalClient> institutionExternalClients) {
		//Achtung hier MUSS instanz verglichen werden
		return institutionExternalClients.stream()
			.anyMatch(o1 -> institutionExternalClients.stream()
				.anyMatch(o2 -> !o1.equals(o2) && o1.getGueltigkeit().intersects(o2.getGueltigkeit())));
	}
}
