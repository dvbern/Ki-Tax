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
package ch.dvbern.ebegu.validators.dateranges;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.GueltigkeitsUtil;

import static java.util.Objects.requireNonNull;

/**
 * Die Betreuungspensen einer BetreuungMitteilung duerfen nicht komplett ausserhalb der Gesuchsperiode liegen
 */
public class CheckBetreuungMitteilungZeitraumInGesuchsperiodeValidator
	implements ConstraintValidator<CheckBetreuungMitteilungZeitraumInGesuchsperiode, Betreuungsmitteilung> {

	@Override
	public boolean isValid(Betreuungsmitteilung betreuungMitteilung, ConstraintValidatorContext context) {
		Betreuung betreuung = requireNonNull(betreuungMitteilung.getBetreuung());
		DateRange gueltigkeitGesuchsperiode = betreuung.extractGesuchsperiode().getGueltigkeit();

		return GueltigkeitsUtil.intersects(betreuungMitteilung.getBetreuungspensen(), gueltigkeitGesuchsperiode);
	}
}
