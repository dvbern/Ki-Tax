/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AnspruchAbAlterAbschnittRule extends AbstractAbschnittRule {

	private final int requiredAgeInMonths;

	protected AnspruchAbAlterAbschnittRule(
			@Nonnull DateRange validityPeriod,
			@Nonnull Locale locale,
			int requiredAgeInMonths) {
		super(RuleKey.ANSPRUCH_AB_X_MONATEN, RuleType.GRUNDREGEL_CALC, RuleValidity.ASIV, validityPeriod, locale);
		this.requiredAgeInMonths = requiredAgeInMonths;
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		if (requiredAgeInMonths <= 0) {
			return new ArrayList<>();
		}
		var birthDay = platz.getKind().getKindJA().getGeburtsdatum();
		var firstDayOfAnspruch = birthDay.plusMonths(this.requiredAgeInMonths);

		if (!platz.extractGesuchsperiode().getGueltigkeit().contains(firstDayOfAnspruch)) {
			return new ArrayList<>();
		}
		final LocalDate firstDayOfGP = platz.extractGesuchsperiode().getGueltigkeit().getGueltigAb();

		if (firstDayOfAnspruch.isAfter(firstDayOfGP)) {
			final LocalDate lastDayOfGP = platz.extractGesuchsperiode().getGueltigkeit().getGueltigBis();
			VerfuegungZeitabschnitt noAnspruchZeitabschnitt =
					createZeitabschnittWithinValidityPeriodOfRule(
							firstDayOfGP,
							firstDayOfAnspruch.isAfter(lastDayOfGP) ? lastDayOfGP : firstDayOfAnspruch.minusDays(1));
			noAnspruchZeitabschnitt.setRequiredAgeForAnspruchNotReached(true);
			return Arrays.asList(noAnspruchZeitabschnitt);
		}
		return new ArrayList<>();
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes();
	}

}
