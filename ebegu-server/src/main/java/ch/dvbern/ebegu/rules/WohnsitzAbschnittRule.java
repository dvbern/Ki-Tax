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
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Regel für Wohnsitz in Bern (Zuzug und Wegzug):
 * - Durch Adresse definiert
 * - Anspruch vom ersten Tag des Zuzugs
 * - Anspruch bis 2 Monate nach Wegzug, auf Ende Monat
 * Verweis 16.8 Der zivilrechtliche Wohnsitz
 */
public class WohnsitzAbschnittRule extends AbstractAbschnittRule {

	private static final Logger LOG = LoggerFactory.getLogger(WohnsitzAbschnittRule.class);

	public WohnsitzAbschnittRule(@Nonnull DateRange validityPeriod) {
		super(RuleKey.WOHNSITZ, RuleType.GRUNDREGEL_DATA, validityPeriod);
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull Betreuung betreuung) {
		List<VerfuegungZeitabschnitt> analysedAbschnitte = new ArrayList<>();
		Gesuch gesuch = betreuung.extractGesuch();
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
				if (isWohnsitzNichtInGemeinde(lastZeitAbschnitt) != isWohnsitzNichtInGemeinde(zeitabschnitt)) {
					// Es hat geaendert. Was war es fuer eine Anpassung?
					if (isWohnsitzNichtInGemeinde(zeitabschnitt)) {
						// Es ist ein Wegzug
						LOG.info("Wegzug");
						LocalDate stichtagEndeAnspruch = zeitabschnitt.getGueltigkeit().getGueltigAb().with(TemporalAdjusters.lastDayOfMonth());
						lastZeitAbschnitt.getGueltigkeit().setGueltigBis(stichtagEndeAnspruch);
						if (zeitabschnitt.getGueltigkeit().getGueltigBis().isAfter(stichtagEndeAnspruch.plusDays(1))) {
							zeitabschnitt.getGueltigkeit().setGueltigAb(stichtagEndeAnspruch.plusDays(1));
							result.add(zeitabschnitt);
						}
					} else {
						// Es ist ein Zuzug
						LOG.info("Zuzug");
						result.add(zeitabschnitt);
					}
				} else {
					// Dieser Fall sollte gar nicht eintreten, da die Zeitabschnitte vorher gemergt wurden!
					LOG.info("Zweiter Adressen-Abschnitt mit gleichen Daten: Dieser Fall sollte gar nicht eintreten, da die Zeitabschnitte vorher gemergt wurden!");
					result.add(zeitabschnitt);
				}
			}
			lastZeitAbschnitt = zeitabschnitt;
		}
		return result;
	}

	private boolean isWohnsitzNichtInGemeinde(VerfuegungZeitabschnitt zeitabschnitt) {
		return (zeitabschnitt.isWohnsitzNichtInGemeindeGS1() && zeitabschnitt.isWohnsitzNichtInGemeindeGS2());
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
				if (gs1) {
					VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt(gesuchstellerAdresse.extractGueltigkeit());
					zeitabschnitt.setWohnsitzNichtInGemeindeGS1(gesuchstellerAdresse.extractIsNichtInGemeinde());
					adressenZeitabschnitte.add(zeitabschnitt);
				} else { // gs2
					final DateRange gueltigkeit = new DateRange(gesuchstellerAdresse.extractGueltigkeit());
					Familiensituation familiensituation = gesuch.extractFamiliensituation();
					Objects.requireNonNull(familiensituation);
					LocalDate familiensituationGueltigAb = familiensituation.getAenderungPer();
					if (familiensituationGueltigAb != null) {

						// Die Familiensituation wird immer fruehestens per nächsten Monat angepasst!
						LocalDate familiensituationStichtag = getStichtagForEreignis(familiensituationGueltigAb);

						// from 1GS to 2GS
						Familiensituation familiensituationErstgesuch = gesuch.extractFamiliensituationErstgesuch();
						Objects.requireNonNull(familiensituationErstgesuch);
						if (!familiensituationErstgesuch.hasSecondGesuchsteller() && familiensituation.hasSecondGesuchsteller()) {
							if (gueltigkeit.getGueltigBis().isAfter(familiensituationStichtag)) {
								if (gueltigkeit.getGueltigAb().isBefore(familiensituationStichtag)) {
									gueltigkeit.setGueltigAb(familiensituationStichtag);
								}
								createZeitabschnittForGS2(adressenZeitabschnitte, gesuchstellerAdresse.extractIsNichtInGemeinde(), gueltigkeit);
							}
						}
						// from 2GS to 1GS
						else if (familiensituationErstgesuch.hasSecondGesuchsteller() && !familiensituation
							.hasSecondGesuchsteller()
							&& (gueltigkeit.getGueltigAb().isBefore(familiensituationStichtag))) {

							if (!gueltigkeit.getGueltigBis().isBefore(familiensituationStichtag)) {
								gueltigkeit.setGueltigBis(familiensituationStichtag.minusDays(1));
							}
							createZeitabschnittForGS2(adressenZeitabschnitte, gesuchstellerAdresse.extractIsNichtInGemeinde(), gueltigkeit);
						}
					} else {
						createZeitabschnittForGS2(adressenZeitabschnitte, gesuchstellerAdresse.extractIsNichtInGemeinde(), gueltigkeit);
					}
				}
			});
		return adressenZeitabschnitte;
	}

	private void createZeitabschnittForGS2(List<VerfuegungZeitabschnitt> adressenZeitabschnitte, boolean nichtInGemeinde,
		DateRange gueltigkeit) {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt(gueltigkeit);
		zeitabschnitt.setWohnsitzNichtInGemeindeGS2(nichtInGemeinde);
		adressenZeitabschnitte.add(zeitabschnitt);
	}
}
