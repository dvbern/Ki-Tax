/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;
import static java.util.Objects.requireNonNull;

/**
 * Regel für Wohnsitz in Bern (Zuzug und Wegzug):
 * - Durch Adresse definiert
 * - Anspruch vom ersten Tag des Zuzugs
 * - Anspruch bis 2 Monate nach Wegzug, auf Ende Monat
 * Verweis 16.8 Der zivilrechtliche Wohnsitz
 */
public class WohnsitzAbschnittRule extends AbstractAbschnittRule {

	public WohnsitzAbschnittRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.WOHNSITZ, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		List<VerfuegungZeitabschnitt> analysedAbschnitte = new ArrayList<>();
		Gesuch gesuch = platz.extractGesuch();
		if (gesuch.getGesuchsteller1() != null) {
			List<VerfuegungZeitabschnitt> adressenAbschnitte = new ArrayList<>();
			adressenAbschnitte.addAll(getAdresseAbschnittForGesuchsteller(gesuch, gesuch.getGesuchsteller1(), true));
			analysedAbschnitte.addAll(analyseAdressAbschnitte(adressenAbschnitte));
		}
		if (gesuch.getGesuchsteller2() != null) {
			List<VerfuegungZeitabschnitt> adressenAbschnitte = new ArrayList<>();
			adressenAbschnitte.addAll(getAdresseAbschnittForGesuchsteller(gesuch, gesuch.getGesuchsteller2(), false));
			analysedAbschnitte.addAll(analyseAdressAbschnitte(adressenAbschnitte));
		}
		return analysedAbschnitte;
	}

	private List<VerfuegungZeitabschnitt> analyseAdressAbschnitte(List<VerfuegungZeitabschnitt> adressenAbschnitte) {
		List<VerfuegungZeitabschnitt> result = new ArrayList<>();
		List<VerfuegungZeitabschnitt> zeitabschnittList = mergeZeitabschnitte(adressenAbschnitte);
		VerfuegungZeitabschnitt lastZeitAbschnitt = null;
		boolean isFirstAbschnitt = true;
		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnittList) {
			if (isFirstAbschnitt) {
				// Der erste Abschnitt. Wir wissen noch nicht, ob Zuzug oder Wegzug
				isFirstAbschnitt = false;
				result.add(zeitabschnitt);
			} else {
				// Dies ist mindestens die zweite Adresse -> pruefen, ob sich an der Wohnsitz-Situation etwas geaendert hat.
				boolean lastNichtInGemeindeAsiv = lastZeitAbschnitt.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1();
				boolean newNichtInGemeindeAsiv = zeitabschnitt.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1();
				boolean lastNichtInGemeindeGemeinde = lastZeitAbschnitt.getBgCalculationInputGemeinde().isWohnsitzNichtInGemeindeGS1();
				boolean newNichtInGemeindeGemeinde = zeitabschnitt.getBgCalculationInputGemeinde().isWohnsitzNichtInGemeindeGS1();
				boolean changedAsiv = lastNichtInGemeindeAsiv != newNichtInGemeindeAsiv;
				boolean changedGemeinde = lastNichtInGemeindeGemeinde != newNichtInGemeindeGemeinde;
				if (changedAsiv || changedGemeinde) {

					// Es hat geaendert. Was war es fuer eine Anpassung?
					if ((changedAsiv && newNichtInGemeindeAsiv) || (changedGemeinde && newNichtInGemeindeGemeinde)) {
						// Es ist ein Wegzug
						LocalDate stichtagEndeAnspruch = zeitabschnitt.getGueltigkeit().getGueltigAb().with(TemporalAdjusters.lastDayOfMonth());
						lastZeitAbschnitt.getGueltigkeit().setGueltigBis(stichtagEndeAnspruch);
						if (zeitabschnitt.getGueltigkeit().getGueltigBis().isAfter(stichtagEndeAnspruch.plusDays(1))) {
							zeitabschnitt.getGueltigkeit().setGueltigAb(stichtagEndeAnspruch.plusDays(1));
							result.add(zeitabschnitt);
						}
					} else {
						// Es ist ein Zuzug
						result.add(zeitabschnitt);
					}
				} else {
					result.add(zeitabschnitt);
				}
			}
			lastZeitAbschnitt = zeitabschnitt;
		}
		return result;
	}

	/**
	 * geht durch die Adressen des Gesuchstellers und gibt Abschnitte zurueck
	 */
	@Nonnull
	private List<VerfuegungZeitabschnitt> getAdresseAbschnittForGesuchsteller(@Nonnull Gesuch gesuch, @Nonnull GesuchstellerContainer gesuchsteller, boolean gs1) {
		List<VerfuegungZeitabschnitt> adressenZeitabschnitte = new ArrayList<>();
		List<GesuchstellerAdresseContainer> gesuchstellerAdressen = gesuchsteller.getAdressen();
		gesuchstellerAdressen.stream()
			.filter(gesuchstellerAdresse -> !gesuchstellerAdresse.extractIsKorrespondenzAdresse() && !gesuchstellerAdresse.extractIsRechnungsAdresse())
			.forEach(gesuchstellerAdresse -> {
				final DateRange gsAdresseGueltigkeit = gesuchstellerAdresse.extractGueltigkeit();
				requireNonNull(gsAdresseGueltigkeit);
				if (gs1) {
					VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(gsAdresseGueltigkeit);
					zeitabschnitt.setWohnsitzNichtInGemeindeGS1ForAsivAndGemeinde(gesuchstellerAdresse.extractIsNichtInGemeinde());
					adressenZeitabschnitte.add(zeitabschnitt);
				} else { // gs2
					final DateRange gueltigkeit = new DateRange(gsAdresseGueltigkeit);
					Familiensituation familiensituation = gesuch.extractFamiliensituation();
					requireNonNull(familiensituation);
					LocalDate familiensituationGueltigAb = familiensituation.getAenderungPer();
					if (familiensituationGueltigAb != null) {

						// Die Familiensituation wird immer fruehestens per nächsten Monat angepasst!
						LocalDate familiensituationStichtag = getStichtagForEreignis(familiensituationGueltigAb);

						// from 1GS to 2GS
						Familiensituation familiensituationErstgesuch = gesuch.extractFamiliensituationErstgesuch();
						requireNonNull(familiensituationErstgesuch);
						LocalDate bis = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();
						if (!familiensituationErstgesuch.hasSecondGesuchsteller(bis)
							&& familiensituation.hasSecondGesuchsteller(bis)) {
							if (gueltigkeit.getGueltigBis().isAfter(familiensituationStichtag)) {
								if (gueltigkeit.getGueltigAb().isBefore(familiensituationStichtag)) {
									gueltigkeit.setGueltigAb(familiensituationStichtag);
								}
								createZeitabschnittForGS2(adressenZeitabschnitte, gueltigkeit);
							}
						}
						// from 2GS to 1GS
						else if (familiensituationErstgesuch.hasSecondGesuchsteller(bis)
							&& !familiensituation.hasSecondGesuchsteller(bis)
							&& (gueltigkeit.getGueltigAb().isBefore(familiensituationStichtag))) {

							if (!gueltigkeit.getGueltigBis().isBefore(familiensituationStichtag)) {
								gueltigkeit.setGueltigBis(familiensituationStichtag.minusDays(1));
							}
							createZeitabschnittForGS2(adressenZeitabschnitte, gueltigkeit);
						}
					} else {
						createZeitabschnittForGS2(adressenZeitabschnitte, gueltigkeit);
					}
				}
			});
		return adressenZeitabschnitte;
	}

	private void createZeitabschnittForGS2(List<VerfuegungZeitabschnitt> adressenZeitabschnitte, DateRange gueltigkeit) {
		VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(gueltigkeit);
		adressenZeitabschnitte.add(zeitabschnitt);
	}
}
