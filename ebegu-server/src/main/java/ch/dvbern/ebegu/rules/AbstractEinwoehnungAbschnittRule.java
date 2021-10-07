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

package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;

public abstract class AbstractEinwoehnungAbschnittRule extends AbstractErwerbspensumAbschnittRule {
	protected AbstractEinwoehnungAbschnittRule(
		@Nonnull RuleKey ruleKey,
		@Nonnull RuleType ruleType,
		@Nonnull RuleValidity ruleValidity,
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale) {
		super(ruleKey, ruleType, ruleValidity, validityPeriod, locale);
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(
		@Nonnull AbstractPlatz platz) {
		List<VerfuegungZeitabschnitt> erwerbspensumAbschnitte = new ArrayList<>();
		Gesuch gesuch = platz.extractGesuch();
		if (gesuch.getGesuchsteller1() != null) {
			erwerbspensumAbschnitte.addAll(getErwerbspensumAbschnittForGesuchsteller(gesuch,
				gesuch.getGesuchsteller1(), platz, false));
		}
		if (gesuch.getGesuchsteller2() != null) {
			List<VerfuegungZeitabschnitt> verfuegungZeitabschnittGS2 =
				getErwerbspensumAbschnittForGesuchsteller(gesuch,
				gesuch.getGesuchsteller2(), platz, true);
			if (erwerbspensumAbschnitte.isEmpty()) {
				erwerbspensumAbschnitte.addAll(verfuegungZeitabschnittGS2);
			} else {
				LocalDate gueltigAb = erwerbspensumAbschnitte.get(0).getGueltigkeit().getGueltigAb();
				LocalDate gueltigAbGS2 = verfuegungZeitabschnittGS2.get(0).getGueltigkeit().getGueltigAb();
				if(gueltigAb.isEqual(gueltigAbGS2)) {
					erwerbspensumAbschnitte.addAll(verfuegungZeitabschnittGS2);
				}
				else if(gueltigAb.isAfter(gueltigAbGS2)) {
					return verfuegungZeitabschnittGS2;
				}
			}
		}
		return erwerbspensumAbschnitte;
	}
}
