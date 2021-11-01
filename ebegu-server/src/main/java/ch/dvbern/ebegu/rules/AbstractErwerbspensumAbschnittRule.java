/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Abstract class for AbschnittRules that are related to the Erwerbspensum and share some common methods
 */
public abstract class AbstractErwerbspensumAbschnittRule extends AbstractAbschnittRule {

	protected AbstractErwerbspensumAbschnittRule(
		@Nonnull RuleKey ruleKey,
		@Nonnull RuleType ruleType,
		@Nonnull RuleValidity ruleValidity,
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale
	) {
		super(ruleKey, ruleType, ruleValidity, validityPeriod, locale);
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		List<VerfuegungZeitabschnitt> erwerbspensumAbschnitte = new ArrayList<>();
		Gesuch gesuch = platz.extractGesuch();
		if (gesuch.getGesuchsteller1() != null) {
			erwerbspensumAbschnitte.addAll(getErwerbspensumAbschnittForGesuchsteller(gesuch,
				gesuch.getGesuchsteller1(),false));
		}
		if (gesuch.getGesuchsteller2() != null) {
			erwerbspensumAbschnitte.addAll(getErwerbspensumAbschnittForGesuchsteller(gesuch,
				gesuch.getGesuchsteller2(), true));
		}
		return erwerbspensumAbschnitte;
	}

	protected abstract List<VerfuegungZeitabschnitt> getErwerbspensumAbschnittForGesuchsteller(
		@Nonnull Gesuch gesuch,
		@Nonnull GesuchstellerContainer gesuchsteller,
		boolean gs2
	);

	protected void getGueltigkeitFromFamiliensituation(
		@Nonnull DateRange gueltigkeit,
		@Nonnull Familiensituation familiensituationErstgesuch,
		@Nonnull Familiensituation familiensituation
	) {
		LocalDate familiensituationGueltigAb = familiensituation.getAenderungPer();
		if (familiensituationGueltigAb != null) {
			// Die Familiensituation wird immer fruehestens per nächsten Monat angepasst!
			LocalDate familiensituationStichtag = getStichtagForEreignis(familiensituationGueltigAb);
			if (!familiensituationErstgesuch.hasSecondGesuchsteller(gueltigkeit.getGueltigBis())
				&& familiensituation.hasSecondGesuchsteller(gueltigkeit.getGueltigBis())) {
				// 1GS to 2GS
				if (gueltigkeit.getGueltigBis().isAfter(familiensituationStichtag)
					&& gueltigkeit.getGueltigAb().isBefore(familiensituationStichtag)) {

					gueltigkeit.setGueltigAb(familiensituationStichtag);
				}
			} else if (familiensituationErstgesuch.hasSecondGesuchsteller(gueltigkeit.getGueltigBis())
				&& !familiensituation.hasSecondGesuchsteller(gueltigkeit.getGueltigBis())
				&& gueltigkeit.getGueltigAb().isBefore(familiensituationStichtag)
				&& gueltigkeit.getGueltigBis().isAfter(familiensituationStichtag)) {

				// 2GS to 1GS
				gueltigkeit.setGueltigBis(familiensituationStichtag.minusDays(1));
			}
		}
	}
}
