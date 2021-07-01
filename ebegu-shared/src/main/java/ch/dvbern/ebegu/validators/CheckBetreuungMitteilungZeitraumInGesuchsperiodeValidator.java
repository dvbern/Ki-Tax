/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Die Betreuungspensen einer BetreuungMitteilung duerfen nicht komplett ausserhalb der Gesuchsperiode liegen
 */
public class CheckBetreuungMitteilungZeitraumInGesuchsperiodeValidator implements ConstraintValidator<CheckBetreuungMitteilungZeitraumInGesuchsperiode, Betreuungsmitteilung> {

	@Override
	public void initialize(CheckBetreuungMitteilungZeitraumInGesuchsperiode constraintAnnotation) {
		// nop
	}

	@Override
	public boolean isValid(Betreuungsmitteilung betreuungMitteilung, ConstraintValidatorContext context) {
		assert betreuungMitteilung.getBetreuung() != null;
		final DateRange gueltigkeitGesuchsperiode = betreuungMitteilung.getBetreuung().extractGesuchsperiode().getGueltigkeit();
		for (BetreuungsmitteilungPensum betreuungsmitteilungPensum : betreuungMitteilung.getBetreuungspensen()) {
			final DateRange pensumDateRange = betreuungsmitteilungPensum.getGueltigkeit();
			if (!gueltigkeitGesuchsperiode.getOverlap(pensumDateRange).isPresent()) {
				return false;
			}
		}
		return true;
	}
}
