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

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Die Betreuungspensen einer Betreuung duerfen nicht komplett ausserhalb der Gesuchsperiode liegen
 */
public class CheckBetreuungZeitraumInGesuchsperiodeValidator implements ConstraintValidator<CheckBetreuungZeitraumInGesuchsperiode, Betreuung> {

	@Override
	public void initialize(CheckBetreuungZeitraumInGesuchsperiode constraintAnnotation) {
		// nop
	}

	@Override
	public boolean isValid(Betreuung betreuung, ConstraintValidatorContext context) {
		final DateRange gueltigkeitGesuchsperiode = betreuung.extractGesuchsperiode().getGueltigkeit();
		for (BetreuungspensumContainer betreuungspensumContainer : betreuung.getBetreuungspensumContainers()) {
			final DateRange pensumDateRange = betreuungspensumContainer.getBetreuungspensumJA().getGueltigkeit();
			if (!gueltigkeitGesuchsperiode.getOverlap(pensumDateRange).isPresent()) {
				return false;
			}
		}
		return true;
	}
}
