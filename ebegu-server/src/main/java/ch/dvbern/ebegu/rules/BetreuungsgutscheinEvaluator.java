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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.rechner.AbstractBGRechner;
import ch.dvbern.ebegu.rechner.BGCalculationResult;
import ch.dvbern.ebegu.rechner.BGRechnerFactory;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rules.initalizer.RestanspruchInitializer;
import ch.dvbern.ebegu.rules.util.BemerkungsMerger;
import ch.dvbern.ebegu.util.BetreuungComparator;
import ch.dvbern.ebegu.util.VerfuegungUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * This is the Evaluator that runs all the rules and calculations for a given Antrag to determine the Betreuungsgutschein
 */
public class BetreuungsgutscheinEvaluator {

	private static final Logger LOG = LoggerFactory.getLogger(BetreuungsgutscheinEvaluator.class);

	private boolean isDebug = true;

	private final List<Rule> rules;

	public BetreuungsgutscheinEvaluator(List<Rule> rules) {
		this.rules = rules;
	}

	public BetreuungsgutscheinEvaluator(List<Rule> rules, boolean enableDebugOutput) {
		this.rules = rules;
		this.isDebug = enableDebugOutput;
	}


	/**
	 * Berechnet nur die Familiengroesse und Abzuege fuer den Print der Familiensituation, es muss min eine Betreuung existieren
	 */
	@Nonnull
	public Verfuegung evaluateFamiliensituation(Gesuch gesuch, Locale locale, boolean executeMonatsRule) {

		// Wenn diese Methode aufgerufen wird, muss die Berechnung der Finanzdaten bereits erfolgt sein:
		if (gesuch.getFinanzDatenDTO() == null) {
			throw new IllegalStateException("Bitte zuerst die Finanzberechnung ausführen! -> FinanzielleSituationRechner.calculateFinanzDaten()");
		}
		List<Rule> rulesToRun = findRulesToRunForPeriode(gesuch.getGesuchsperiode());

		// Fuer die Familiensituation ist die Betreuung nicht relevant. Wir brauchen aber eine, da die Signatur der Rules
		// mit Betreuungen funktioniert. Wir nehmen einfach die erste von irgendeinem Kind, das heisst ohne betreuung koennen wir nicht berechnen
		// Fuer ein Gesuch im Status KEIN_ANGEBOT wir können keine Betreuung finden, da es keine gibt.
		AbstractPlatz firstBetreuungOfGesuch = gesuch.getStatus() == AntragStatus.KEIN_ANGEBOT
			? null
			: gesuch.getFirstBetreuung();
		// Für die Berechnung der Familiensituation-Finanzen genügt auch eine Tagesschul-Anmeldung
		if (firstBetreuungOfGesuch == null) {
			firstBetreuungOfGesuch = gesuch.getFirstAnmeldung();
		}

		// Die Initialen Zeitabschnitte erstellen (1 pro Gesuchsperiode)
		List<VerfuegungZeitabschnitt> zeitabschnitte = createInitialenRestanspruch(gesuch.getGesuchsperiode());

		if (firstBetreuungOfGesuch != null) {
			for (Rule rule : rulesToRun) {
				// Nur ausgewaehlte Rules verwenden
				if (rule.isRelevantForFamiliensituation()) {
					zeitabschnitte = rule.calculate(firstBetreuungOfGesuch, zeitabschnitte);
				}
			}

			if(executeMonatsRule){
				zeitabschnitte = MonatsRule.execute(zeitabschnitte);
			}

			// Ganz am Ende der Berechnung mergen wir das aktuelle Ergebnis mit der Verfügung des letzten Gesuches
			zeitabschnitte = MutationsMerger.execute(firstBetreuungOfGesuch, zeitabschnitte, locale);

			// Falls jetzt wieder Abschnitte innerhalb eines Monats "gleich" sind, im Sinne der *angezeigten* Daten, diese auch noch mergen
			zeitabschnitte = AbschlussNormalizer.execute(zeitabschnitte, true);

		} else if (gesuch.getStatus() != AntragStatus.KEIN_ANGEBOT) {
			// for Status KEIN_ANGEBOT it makes no sense to log an error because it is not an error
			LOG.info("Keine Betreuung vorhanden kann Familiengroesse und Abzuege nicht berechnen");
		}

		// Eine neue (nirgends angehaengte) Verfügung erstellen
		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitabschnitte);
		return verfuegung;
	}

	@SuppressWarnings({ "OverlyComplexMethod", "PMD.NcssMethodCount" })
	public void evaluate(
		@Nonnull Gesuch gesuch,
		@Nonnull BGRechnerParameterDTO bgRechnerParameterDTO,
		@Nonnull Locale locale) {

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
				if (requireNonNull(betreuung.getBetreuungsangebotTyp()).isSchulamt()) {
					continue;
				}

				//initiale Restansprueche vorberechnen
				if ((betreuung.getBetreuungsstatus() == Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG
					&& betreuung.getVerfuegungOrVorgaengerAusbezahlteVerfuegung() == null)
					|| betreuung.getBetreuungsstatus() == Betreuungsstatus.NICHT_EINGETRETEN) {
					// es kann sein dass eine neue Betreuung in der Mutation abgelehnt wird, dann gibts keinen Vorgaenger und keine aktuelle
					//verfuegung und wir muessen keinen restanspruch berechnen (vergl EBEGU-890)
					continue;
				}
				if (betreuung.getBetreuungsstatus().isGeschlossenJA()) {
					// Verfuegte Betreuungen duerfen nicht neu berechnet werden
					LOG.info("Betreuung ist schon verfuegt. Keine Neuberechnung durchgefuehrt");
					// Restanspruch muss mit Daten von Verfügung für nächste Betreuung richtig gesetzt werden
					restanspruchZeitabschnitte = getRestanspruchForVerfuegteBetreung(betreuung);
					continue;
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

				// Innerhalb eines Monats darf der Anspruch nie sinken
				zeitabschnitte = AnspruchFristRule.execute(zeitabschnitte);

				// Nach der Abhandlung dieser Betreuung die Restansprüche für die nächste Betreuung extrahieren
				restanspruchZeitabschnitte = RestanspruchInitializer.execute(betreuung, zeitabschnitte);

				// Falls jetzt noch Abschnitte "gleich" sind, im Sinne der *angezeigten* Daten, diese auch noch mergen
				zeitabschnitte = AbschlussNormalizer.execute(zeitabschnitte, false);

				// Nach dem Durchlaufen aller Rules noch die Monatsstückelungen machen
				zeitabschnitte = MonatsRule.execute(zeitabschnitte);

				// Ganz am Ende der Berechnung mergen wir das aktuelle Ergebnis mit der Verfügung des letzten Gesuches
				zeitabschnitte = MutationsMerger.execute(betreuung, zeitabschnitte, locale);

				// Falls jetzt wieder Abschnitte innerhalb eines Monats "gleich" sind, im Sinne der *angezeigten* Daten, diese auch noch mergen
				zeitabschnitte = AbschlussNormalizer.execute(zeitabschnitte, true);

				// Die Verfügung erstellen
				if (betreuung.getVerfuegung() == null) {
					Verfuegung verfuegung = new Verfuegung(betreuung);
					betreuung.setVerfuegung(verfuegung);
					verfuegung.setBetreuung(betreuung);
				}

				// Den richtigen Rechner anwerfen
				AbstractBGRechner rechner = BGRechnerFactory.getRechner(betreuung);
				if (rechner != null) {
					for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {
						BGCalculationResult result = rechner.calculate(verfuegungZeitabschnitt, bgRechnerParameterDTO);
						result.toVerfuegungZeitabschnitt(verfuegungZeitabschnitt);
					}
				}
				// Und die Resultate in die Verfügung schreiben
				betreuung.getVerfuegung().setZeitabschnitte(zeitabschnitte);
				String bemerkungenToShow = BemerkungsMerger.evaluateBemerkungenForVerfuegung(zeitabschnitte);
				betreuung.getVerfuegung().setGeneratedBemerkungen(bemerkungenToShow);

				setZahlungRelevanteDaten(betreuung);
			}
		}
	}

	private void setZahlungRelevanteDaten(@Nonnull Betreuung betreuung) {
		if (betreuung.getVerfuegung() == null) {
			return;
		}
		Verfuegung ausbezahlteVorgaenger = betreuung.getVorgaengerAusbezahlteVerfuegung();
		Verfuegung vorgaengerVerfuegung = betreuung.getVorgaengerVerfuegung();

		// Den Zahlungsstatus aus der letzten *ausbezahlten* Verfuegung berechnen
		if (ausbezahlteVorgaenger != null) {
			// Zahlungsstatus aus vorgaenger uebernehmen
			VerfuegungUtil.setZahlungsstatus(betreuung.getVerfuegung(), ausbezahlteVorgaenger);
			VerfuegungUtil.setIsSameAusbezahlteVerguenstigung(betreuung.getVerfuegung(), ausbezahlteVorgaenger);
		}
		// Das Flag "Gleiche Verfügungsdaten" aus der letzten Verfuegung berechnen
		if (vorgaengerVerfuegung != null) {
			// Ueberpruefen, ob sich die Verfuegungsdaten veraendert haben
			VerfuegungUtil.setIsSameVerfuegungsdaten(betreuung.getVerfuegung(), vorgaengerVerfuegung);
		}
	}

	/**
	 * Wenn eine Verfuegung schon Freigegeben ist wird sie nicht mehr neu berechnet, trotzdem muessen wir den Restanspruch
	 * beruecksichtigen
	 */
	@Nonnull
	private List<VerfuegungZeitabschnitt> getRestanspruchForVerfuegteBetreung(Betreuung betreuung) {
		List<VerfuegungZeitabschnitt> restanspruchZeitabschnitte;
		Verfuegung verfuegungForRestanspruch = betreuung.getVerfuegungOrVorgaengerAusbezahlteVerfuegung();
		if (verfuegungForRestanspruch == null) {
			String message = "Ungueltiger Zustand, geschlossene Betreuung ohne Verfuegung oder Vorgaengerverfuegung (" + betreuung.getId() + ')';
			throw new EbeguRuntimeException("getRestanspruchForVerfuegteBetreung", message);
		}
		restanspruchZeitabschnitte = RestanspruchInitializer.execute(
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
}
