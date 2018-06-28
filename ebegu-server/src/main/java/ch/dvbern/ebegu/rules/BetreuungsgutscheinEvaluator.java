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

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.rechner.AbstractBGRechner;
import ch.dvbern.ebegu.rechner.BGRechnerFactory;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rules.initalizer.RestanspruchInitializer;
import ch.dvbern.ebegu.rules.util.BemerkungsMerger;
import ch.dvbern.ebegu.util.BetreuungComparator;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.VerfuegungUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This is the Evaluator that runs all the rules and calculations for a given Antrag to determine the Betreuungsgutschein
 */
public class BetreuungsgutscheinEvaluator {

	private boolean isDebug = true;

	private List<Rule> rules = new LinkedList<>();

	private final RestanspruchInitializer restanspruchInitializer = new RestanspruchInitializer();
	private final MonatsRule monatsRule = new MonatsRule(Constants.DEFAULT_GUELTIGKEIT);
	private final MutationsMerger mutationsMerger = new MutationsMerger();
	private final AbschlussNormalizer abschlussNormalizer = new AbschlussNormalizer();

	public BetreuungsgutscheinEvaluator(List<Rule> rules) {
		this.rules = rules;
	}

	public BetreuungsgutscheinEvaluator(List<Rule> rules, boolean enableDebugOutput) {
		this.rules = rules;
		this.isDebug = enableDebugOutput;
	}

	private final Logger LOG = LoggerFactory.getLogger(BetreuungsgutscheinEvaluator.class.getSimpleName());

	/**
	 * Berechnet nur die Familiengroesse und Abzuege fuer den Print der Familiensituation, es muss min eine Betreuung existieren
	 */
	public Verfuegung evaluateFamiliensituation(Gesuch gesuch) {

		// Wenn diese Methode aufgerufen wird, muss die Berechnung der Finanzdaten bereits erfolgt sein:
		if (gesuch.getFinanzDatenDTO() == null) {
			throw new IllegalStateException("Bitte zuerst die Finanzberechnung ausführen! -> FinanzielleSituationRechner.calculateFinanzDaten()");
		}
		List<Rule> rulesToRun = findRulesToRunForPeriode(gesuch.getGesuchsperiode());

		// Fuer die Familiensituation ist die Betreuung nicht relevant. Wir brauchen aber eine, da die Signatur der Rules
		// mit Betreuungen funktioniert. Wir nehmen einfach die erste von irgendeinem Kind, das heisst ohne betreuung koennen wir nicht berechnen
		Betreuung firstBetreuungOfGesuch = getFirstBetreuungOfGesuch(gesuch);

		// Die Initialen Zeitabschnitte erstellen (1 pro Gesuchsperiode)
		List<VerfuegungZeitabschnitt> zeitabschnitte = createInitialenRestanspruch(gesuch.getGesuchsperiode());

		if (firstBetreuungOfGesuch != null) {
			for (Rule rule : rulesToRun) {
				// Nur ausgewaehlte Rules verwenden
				if (rule.isRelevantForFamiliensituation()) {
					zeitabschnitte = rule.calculate(firstBetreuungOfGesuch, zeitabschnitte);
				}
			}
			// Nach dem Durchlaufen aller Rules noch die Monatsstückelungen machen
			zeitabschnitte = monatsRule.createVerfuegungsZeitabschnitte(firstBetreuungOfGesuch, zeitabschnitte);
		} else {
			LOG.warn("Keine Betreuung vorhanden kann Familiengroesse und Abzuege nicht berechnen");
		}

		// Eine neue (nirgends angehaengte) Verfügung erstellen
		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitabschnitte);
		return verfuegung;
	}

	@SuppressWarnings({ "OverlyComplexMethod", "OverlyNestedMethod", "PMD.NcssMethodCount" })
	public void evaluate(Gesuch gesuch, BGRechnerParameterDTO bgRechnerParameterDTO) {

		// Wenn diese Methode aufgerufen wird, muss die Berechnung der Finanzdaten bereits erfolgt sein:
		if (gesuch.getFinanzDatenDTO() == null) {
			throw new IllegalStateException("Bitte zuerst die Finanzberechnung ausführen! -> FinanzielleSituationRechner.calculateFinanzDaten()");
		}
		List<Rule> rulesToRun = findRulesToRunForPeriode(gesuch.getGesuchsperiode());
		List<KindContainer> kinder = new ArrayList<>(gesuch.getKindContainers());
		Collections.sort(kinder);
		for (KindContainer kindContainer : kinder) {
			// Pro Kind werden (je nach Angebot) die Anspruchspensen aufsummiert. Wir müssen uns also nach jeder Betreuung
			// den "Restanspruch" merken für die Berechnung der nächsten Betreuung,
			// am Schluss kommt dann jeweils eine Reduktionsregel die den Anspruch auf den Restanspruch beschraenkt
			List<VerfuegungZeitabschnitt> restanspruchZeitabschnitte = createInitialenRestanspruch(gesuch.getGesuchsperiode());

			// Betreuungen werden einzeln berechnet, reihenfolge ist wichtig (sortiert mit comperator gem regel EBEGU-561)
			List<Betreuung> betreuungen = new ArrayList<>(kindContainer.getBetreuungen());
			betreuungen.sort(new BetreuungComparator());

			for (Betreuung betreuung : betreuungen) {

				if (!betreuung.getBetreuungsangebotTyp().isSchulamt()) {
					//initiale Restansprueche vorberechnen
					if (betreuung.getBetreuungsstatus() != null) {
						if ((betreuung.getBetreuungsstatus() == Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG
							&& betreuung.getVerfuegungOrVorgaengerVerfuegung() == null)
							|| betreuung.getBetreuungsstatus() == Betreuungsstatus.NICHT_EINGETRETEN) {
							// es kann sein dass eine neue Betreuung in der Mutation abgelehnt wird, dann gibts keinen Vorgaenger und keine aktuelle
							//verfuegung und wir muessen keinenr restanspruch berechnen (vergl EBEGU-890)
							continue;
						} else if (betreuung.getBetreuungsstatus().isGeschlossenJA()) {
							// Verfuegte Betreuungen duerfen nicht neu berechnet werden
							LOG.info("Betreuung ist schon verfuegt. Keine Neuberechnung durchgefuehrt");
							// Restanspruch muss mit Daten von Verfügung für nächste Betreuung richtig gesetzt werden
							restanspruchZeitabschnitte = getRestanspruchForVerfuegteBetreung(betreuung);
							continue;
						}
					}

					// Die Initialen Zeitabschnitte sind die "Restansprüche" aus der letzten Betreuung
					List<VerfuegungZeitabschnitt> zeitabschnitte = restanspruchZeitabschnitte;
					if (isDebug) {
						LOG.info("BG-Nummer: {}", betreuung.getBGNummer());
						LOG.info("{}: ", RestanspruchInitializer.class.getSimpleName());
						for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {
							LOG.info(verfuegungZeitabschnitt.toString());
						}
					}

					for (Rule rule : rulesToRun) {
						zeitabschnitte = rule.calculate(betreuung, zeitabschnitte);
						if (isDebug) {
							LOG.info("{} ({}: {}" + ')', rule.getClass().getSimpleName(), rule.getRuleKey().name(), rule.getRuleType().name());
							for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {
								LOG.info(verfuegungZeitabschnitt.toString());
							}
						}
					}
					// Nach der Abhandlung dieser Betreuung die Restansprüche für die nächste Betreuung extrahieren
					restanspruchZeitabschnitte = restanspruchInitializer.createVerfuegungsZeitabschnitte(betreuung, zeitabschnitte);

					// Falls jetzt noch Abschnitte "gleich" sind, im Sinne der *angezeigten* Daten, diese auch noch mergen
					zeitabschnitte = abschlussNormalizer.mergeGleicheSichtbareDaten(zeitabschnitte);

					// Nach dem Durchlaufen aller Rules noch die Monatsstückelungen machen
					zeitabschnitte = monatsRule.createVerfuegungsZeitabschnitte(betreuung, zeitabschnitte);

					// Ganz am Ende der Berechnung mergen wir das aktuelle Ergebnis mit der Verfügung des letzten Gesuches
					zeitabschnitte = mutationsMerger.createVerfuegungsZeitabschnitte(betreuung, zeitabschnitte);

					// Die Verfügung erstellen
					if (betreuung.getVerfuegung() == null) {
						Verfuegung verfuegung = new Verfuegung();
						betreuung.setVerfuegung(verfuegung);
						verfuegung.setBetreuung(betreuung);
					}

					// Den richtigen Rechner anwerfen
					AbstractBGRechner rechner = BGRechnerFactory.getRechner(betreuung);
					if (rechner != null) {
						for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {
							rechner.calculate(verfuegungZeitabschnitt, betreuung.getVerfuegung(), bgRechnerParameterDTO);
						}
					}
					// Und die Resultate in die Verfügung schreiben
					betreuung.getVerfuegung().setZeitabschnitte(zeitabschnitte);
					String bemerkungenToShow = BemerkungsMerger.evaluateBemerkungenForVerfuegung(zeitabschnitte);
					betreuung.getVerfuegung().setGeneratedBemerkungen(bemerkungenToShow);

					// Ueberpruefen, ob sich die Verfuegungsdaten veraendert haben
					VerfuegungUtil.setIsSameVerfuegungsdaten(betreuung.getVerfuegung());

					// Zahlungsstatus aus vorgaenger uebernehmen
					VerfuegungUtil.setZahlungsstatus(betreuung.getVerfuegung());
				}
			}
		}
	}

	/**
	 * Wenn eine Verfuegung schon Freigegeben ist wird sei nicht mehr neu berechnet, trotzdem muessen wir den Restanspruch
	 * beruecksichtigen
	 */
	@Nonnull
	private List<VerfuegungZeitabschnitt> getRestanspruchForVerfuegteBetreung(Betreuung betreuung) {
		List<VerfuegungZeitabschnitt> restanspruchZeitabschnitte;
		Verfuegung verfuegungForRestanspruch = betreuung.getVerfuegungOrVorgaengerVerfuegung();
		if (verfuegungForRestanspruch == null) {
			String message = "Ungueltiger Zustand, geschlossene  Betreuung ohne Verfuegung oder Vorgaengerverfuegung (" + betreuung.getId() + ')';
			throw new EbeguRuntimeException("getRestanspruchForVerfuegteBetreung", message);
		}
		restanspruchZeitabschnitte = restanspruchInitializer.createVerfuegungsZeitabschnitte(
			verfuegungForRestanspruch.getBetreuung(), verfuegungForRestanspruch.getZeitabschnitte());

		return restanspruchZeitabschnitte;
	}

	private List<Rule> findRulesToRunForPeriode(Gesuchsperiode gesuchsperiode) {
		List<Rule> rulesForGesuchsperiode = new LinkedList<>();
		for (Rule rule : rules) {
			if (rule.isValid(gesuchsperiode.getGueltigkeit().getGueltigAb())) {
				rulesForGesuchsperiode.add(rule);
			} else {
				LOG.debug("Rule did not aply to Gesuchsperiode {}", rule);

			}
		}
		return rulesForGesuchsperiode;
	}

	public static List<VerfuegungZeitabschnitt> createInitialenRestanspruch(Gesuchsperiode gesuchsperiode) {
		List<VerfuegungZeitabschnitt> restanspruchZeitabschnitte = new ArrayList<>();
		VerfuegungZeitabschnitt initialerRestanspruch = new VerfuegungZeitabschnitt(gesuchsperiode.getGueltigkeit());
		initialerRestanspruch.setAnspruchspensumRest(-1); // Damit wir erkennen, ob schon einmal ein "Rest" durch eine Rule gesetzt wurde
		restanspruchZeitabschnitte.add(initialerRestanspruch);
		return restanspruchZeitabschnitte;
	}

	@SuppressWarnings("LoopStatementThatDoesntLoop")
	private Betreuung getFirstBetreuungOfGesuch(Gesuch gesuch) {
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				return betreuung;
			}
		}
		return null;
	}
}
