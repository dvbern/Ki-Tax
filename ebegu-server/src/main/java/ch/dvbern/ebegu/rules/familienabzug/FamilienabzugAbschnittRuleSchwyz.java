/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules.familienabzug;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;

public class FamilienabzugAbschnittRuleSchwyz extends AbstractFamilienabzugAbschnittRule {
	protected FamilienabzugAbschnittRuleSchwyz(
		@Nonnull
		Map<EinstellungKey, Einstellung> einstellungMap,
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale) {
		super(einstellungMap, validityPeriod, locale);
	}

	@Override
	protected BigDecimal calculateAbzugAufgrundFamiliengroesse(
		double famGrBeruecksichtigungAbzug,
		int famGrAnzahlEltern) {
		BigDecimal abzugFromServer = MathUtil.DEFAULT.from(6700);
		double famGrNachAbzugAnzahlEltern = famGrBeruecksichtigungAbzug - famGrAnzahlEltern;
		return MathUtil.GANZZAHL.from(new BigDecimal(String.valueOf(famGrNachAbzugAnzahlEltern)).multiply(abzugFromServer));
	}

	@Override
	protected Entry<Double, Integer> addAbzugFromKinder(
		@Nonnull Gesuch gesuch,
		@Nonnull LocalDate stichtag,
		@Nonnull Double famGrBeruecksichtigungAbzug,
		int famGrAnzahlEltern) {
		return addAbzugFromKinderSchwyz(gesuch, stichtag, famGrBeruecksichtigungAbzug, famGrAnzahlEltern);
	}

	private Entry<Double, Integer> addAbzugFromKinderSchwyz(Gesuch gesuch, LocalDate stichtag, Double famGrBeruecksichtigungAbzug, int famGrAnzahlEltern) {
		LocalDate dateToCompare = getRelevantDateForKinder(gesuch.getGesuchsperiode(), stichtag);

		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			Kind kind = kindContainer.getKindJA();
			if (kind != null && (dateToCompare == null || kind.getGeburtsdatum().isBefore(dateToCompare))) {
				Kinderabzug kinderabzug = kind.getKinderabzugErstesHalbjahr();
				if (kinderabzug == Kinderabzug.HALBER_ABZUG) {
					famGrBeruecksichtigungAbzug += 0.5;
				} else if (kinderabzug == Kinderabzug.GANZER_ABZUG) {
					famGrBeruecksichtigungAbzug += 1;
				}
			}
		}
		return new AbstractMap.SimpleEntry(famGrBeruecksichtigungAbzug, famGrAnzahlEltern);
	}
}
