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
import java.util.Objects;

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
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;
import ch.dvbern.ebegu.rechner.rules.ZusaetzlicherBabyGutscheinRechnerRule;
import ch.dvbern.ebegu.rechner.rules.ZusaetzlicherGutscheinGemeindeRechnerRule;
import ch.dvbern.ebegu.rules.initalizer.RestanspruchInitializer;
import ch.dvbern.ebegu.rules.util.BemerkungsMerger;
import ch.dvbern.ebegu.util.BetreuungComparator;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.VerfuegungUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Evaluator that runs all the rules and calculations for a given Antrag to determine the
 * Betreuungsgutschein
 */
public class BetreuungsgutscheinEvaluator {

	private static final Logger LOG = LoggerFactory.getLogger(BetreuungsgutscheinEvaluator.class);

	private boolean isDebug = true;

	private final List<Rule> rules;

	private final BetreuungsgutscheinExecutor executor;

	public BetreuungsgutscheinEvaluator(List<Rule> rules) {
		this.rules = rules;
		executor = new BetreuungsgutscheinExecutor(true);
	}

	public BetreuungsgutscheinEvaluator(List<Rule> rules, boolean enableDebugOutput) {
		this.rules = rules;
		this.isDebug = enableDebugOutput;
		executor = new BetreuungsgutscheinExecutor(isDebug);
	}

	/**
	 * Berechnet nur die Familiengroesse und Abzuege fuer den Print der Familiensituation, es muss min eine Betreuung
	 * existieren
	 */
	@Nonnull
	public Verfuegung evaluateFamiliensituation(@Nonnull Gesuch gesuch, @Nonnull Locale locale) {

		// Wenn diese Methode aufgerufen wird, muss die Berechnung der Finanzdaten bereits erfolgt sein:
		if (gesuch.getFinanzDatenDTO() == null) {
			throw new IllegalStateException(
				"Bitte zuerst die Finanzberechnung ausführen! -> FinanzielleSituationRechner.calculateFinanzDaten()");
		}
		List<Rule> rulesToRun = findRulesToRunForPeriode(gesuch.getGesuchsperiode());

		// Fuer die Familiensituation ist die Betreuung nicht relevant. Wir brauchen aber eine, da die Signatur der
		// Rules mit Betreuungen funktioniert. Wir nehmen einfach die erste von irgendeinem Kind, das heisst ohne
		// betreuung koennen wir nicht berechnen Fuer ein Gesuch im Status KEIN_ANGEBOT wir können keine Betreuung
		// finden, da es keine gibt.
		AbstractPlatz firstBetreuungOfGesuch = gesuch.getStatus() == AntragStatus.KEIN_ANGEBOT
			? null
			: gesuch.getFirstBetreuungOrAnmeldungTagesschule();

		// Die Initialen Zeitabschnitte erstellen (1 pro Gesuchsperiode)
		List<VerfuegungZeitabschnitt> zeitabschnitte = RestanspruchInitializer.createInitialenRestanspruch(gesuch.getGesuchsperiode(), false);

		if (firstBetreuungOfGesuch != null) {

			zeitabschnitte = executor.executeRules(rulesToRun, firstBetreuungOfGesuch, zeitabschnitte, true);

			MonatsRule monatsRule = new MonatsRule();
			MutationsMerger mutationsMerger = new MutationsMerger(locale);
			AbschlussNormalizer abschlussNormalizerMitMonate = new AbschlussNormalizer(true);

			zeitabschnitte = monatsRule.executeIfApplicable(firstBetreuungOfGesuch, zeitabschnitte);
			// Ganz am Ende der Berechnung mergen wir das aktuelle Ergebnis mit der Verfügung des letzten Gesuches
			zeitabschnitte = mutationsMerger.executeIfApplicable(firstBetreuungOfGesuch, zeitabschnitte);
			// Falls jetzt wieder Abschnitte innerhalb eines Monats "gleich" sind, im Sinne der *angezeigten* Daten,
			// diese auch noch mergen
			zeitabschnitte = abschlussNormalizerMitMonate.executeIfApplicable(firstBetreuungOfGesuch, zeitabschnitte);

			zeitabschnitte.forEach(VerfuegungZeitabschnitt::initBGCalculationResult);

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
		@Nonnull KitaxUebergangsloesungParameter kitaxParameter,
		@Nonnull Locale locale) {

		// Wenn diese Methode aufgerufen wird, muss die Berechnung der Finanzdaten bereits erfolgt sein:
		if (gesuch.getFinanzDatenDTO() == null) {
			throw new IllegalStateException(
				"Bitte zuerst die Finanzberechnung ausführen! -> FinanzielleSituationRechner.calculateFinanzDaten()");
		}

		Objects.requireNonNull(kitaxParameter.getStadtBernAsivStartDate(), "Das Startdatum ASIV fuer Bern muss in den ApplicationProperties definiert werden");

		List<Rule> rulesToRun = findRulesToRunForPeriode(gesuch.getGesuchsperiode());
		List<RechnerRule> rechnerRulesForGemeinde = rechnerRulesForGemeinde(bgRechnerParameterDTO, locale);
		List<KindContainer> kinder = new ArrayList<>(gesuch.getKindContainers());
		Collections.sort(kinder);
		for (KindContainer kindContainer : kinder) {
			// Pro Kind werden (je nach Angebot) die Anspruchspensen aufsummiert. Wir müssen uns also nach jeder
			// Betreuung den "Restanspruch" merken für die Berechnung der nächsten Betreuung, am Schluss kommt dann
			// jeweils eine Reduktionsregel die den Anspruch auf den Restanspruch beschraenkt
			List<VerfuegungZeitabschnitt> restanspruchZeitabschnitte =
				RestanspruchInitializer.createInitialenRestanspruch(gesuch.getGesuchsperiode(), !rechnerRulesForGemeinde.isEmpty());

			// Betreuungen werden einzeln berechnet, reihenfolge ist wichtig (sortiert mit comperator gem regel
			// EBEGU-561)
			List<AbstractPlatz> plaetzeList = new ArrayList<>(kindContainer.getBetreuungen());
			plaetzeList.addAll(kindContainer.getAnmeldungenTagesschule());
			plaetzeList.sort(new BetreuungComparator());

			for (AbstractPlatz platz : plaetzeList) {

				boolean isTagesschule = platz.getBetreuungsangebotTyp().isTagesschule();

				//initiale Restansprueche vorberechnen
				if ((platz.getBetreuungsstatus() == Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG
					&& platz.getVerfuegungOrVorgaengerAusbezahlteVerfuegung() == null)
					|| platz.getBetreuungsstatus() == Betreuungsstatus.NICHT_EINGETRETEN) {
					// es kann sein dass eine neue Betreuung in der Mutation abgelehnt wird, dann gibts keinen
					// Vorgaenger und keine aktuelle verfuegung und wir muessen keinen restanspruch berechnen (vergl
					// EBEGU-890)
					continue;
				}
				if (platz.getBetreuungsstatus().isGeschlossenJA() || platz.getBetreuungsstatus().isGeschlossenSchulamt()) {
					// Verfuegte Betreuungen duerfen nicht neu berechnet werden
					LOG.info("Betreuung ist schon verfuegt. Keine Neuberechnung durchgefuehrt");
					if (platz.getBetreuungsstatus().isGeschlossenJA()) {
						// Restanspruch muss mit Daten von Verfügung für nächste Betreuung richtig gesetzt werden
						restanspruchZeitabschnitte = getRestanspruchForVerfuegteBetreung((Betreuung) platz);
					}
					continue;
				}

				// Die Initialen Zeitabschnitte sind die "Restansprüche" aus der letzten Betreuung
				List<VerfuegungZeitabschnitt> zeitabschnitte = restanspruchZeitabschnitte;
				if (isDebug) {
					LOG.info("BG-Nummer: {}", platz.getBGNummer());
					LOG.info("{}: ", RestanspruchInitializer.class.getSimpleName());
					for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {
						LOG.info(verfuegungZeitabschnitt.toString());
					}
				}

				zeitabschnitte = executor.executeRules(rulesToRun, platz, zeitabschnitte);

				// Die Abschluss-Rules ebenfalls ausführen

				AnspruchFristRule anspruchFristRule = new AnspruchFristRule();
				RestanspruchInitializer restanspruchInitializer = new RestanspruchInitializer();
				AbschlussNormalizer abschlussNormalizerOhneMonate = new AbschlussNormalizer(false);
				MonatsRule monatsRule = new MonatsRule();
				MutationsMerger mutationsMerger = new MutationsMerger(locale);
				AbschlussNormalizer abschlussNormalizerMitMonate = new AbschlussNormalizer(!platz.getBetreuungsangebotTyp().isTagesschule());

				// Innerhalb eines Monats darf der Anspruch nie sinken
				zeitabschnitte = anspruchFristRule.executeIfApplicable(platz, zeitabschnitte);
				// Nach der Abhandlung dieser Betreuung die Restansprüche für die nächste Betreuung extrahieren
				restanspruchZeitabschnitte = restanspruchInitializer.executeIfApplicable(platz, zeitabschnitte);
				// Falls jetzt noch Abschnitte "gleich" sind, im Sinne der *angezeigten* Daten, diese auch noch mergen
				zeitabschnitte = abschlussNormalizerOhneMonate.executeIfApplicable(platz, zeitabschnitte);
				// Nach dem Durchlaufen aller Rules noch die Monatsstückelungen machen
				zeitabschnitte = monatsRule.executeIfApplicable(platz, zeitabschnitte);
				// Ganz am Ende der Berechnung mergen wir das aktuelle Ergebnis mit der Verfügung des letzten Gesuches
				zeitabschnitte = mutationsMerger.executeIfApplicable(platz, zeitabschnitte);
				// Falls jetzt wieder Abschnitte innerhalb eines Monats "gleich" sind, im Sinne der *angezeigten*
				// Daten, diese auch noch mergen
				zeitabschnitte = abschlussNormalizerMitMonate.executeIfApplicable(platz, zeitabschnitte);

				// Die Verfügung erstellen
				// Da wir die Verfügung nur beim eigentlichen Verfügen speichern wollen, wird
				// die Berechnung in einer (transienten) Preview-Verfügung geschrieben
				Verfuegung verfuegungPreview = new Verfuegung();
				platz.setVerfuegungPreview(verfuegungPreview);

				executor.calculateRechner(bgRechnerParameterDTO, kitaxParameter, locale, rechnerRulesForGemeinde, platz, zeitabschnitte);

				Verfuegung vorgaengerVerfuegung = platz.getVorgaengerVerfuegung();
				if (vorgaengerVerfuegung != null) {
					usePersistedCalculationResult(zeitabschnitte, vorgaengerVerfuegung);
				}
				// Und die Resultate in die Verfügung schreiben
				verfuegungPreview.setZeitabschnitte(zeitabschnitte);
				String bemerkungenToShow = BemerkungsMerger.evaluateBemerkungenForVerfuegung(zeitabschnitte);
				verfuegungPreview.setGeneratedBemerkungen(bemerkungenToShow);
				if (!isTagesschule) {
					setZahlungRelevanteDaten((Betreuung) platz);
				}
			}
		}
	}

	/**
	 * replaces the calcuation results in {@code zeitabschnitte} with the values of an earlier Verfuegung, in case
	 * there is a matching VerfuegungZeitabschnitt, with a calculation result withing the rounding tolerance.
	 * Thus, a mutation only triggers changes, when there is a signification change.
	 * Prevents changes due to increased rounding precision.
	 */
	private void usePersistedCalculationResult(
		@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte,
		@Nonnull Verfuegung vorgaengerVerfuegung) {

		List<VerfuegungZeitabschnitt> vorgaenger = vorgaengerVerfuegung.getZeitabschnitte();

		// Der folgende Hack gilt nur für die Gesuchsperiode 2019/20 (2018), da die Rundung geändert wurde
		if (vorgaengerVerfuegung.getPlatz().extractGesuchsperiode().getBasisJahr() == 2018) {
			zeitabschnitte
				.forEach(zeitabschnitt -> VerfuegungUtil.findZeitabschnittSameGueltigkeit(vorgaenger, zeitabschnitt)
					.filter(zeitabschnitt::isCloseTo)
					.ifPresent(zeitabschnitt::copyCalculationResult));
		}
	}

	/**
	 * Fuer das Auszahlen ist es relevant ob in einer VorgaengerVerfuegung schon etwas
	 * ausbezahlt wurde. Wir schrieben daher den Zahlungsstatus der alten Zeitabschnitte
	 * in die ueberlappenden neuen Zeitabschnitte
	 * @param betreuung in deren zu berechnenend verfuegung die Zahlungsrelevanten Daten gesetzt wurden
	 */
	private void setZahlungRelevanteDaten(@Nonnull Betreuung betreuung) {
		Verfuegung verfuegungZuBerechnen = betreuung.getVerfuegungOrVerfuegungPreview();
		if (verfuegungZuBerechnen == null) {
			return;
		}
		Verfuegung ausbezahlteVorgaenger = betreuung.getVorgaengerAusbezahlteVerfuegung();
		Verfuegung vorgaengerVerfuegung = betreuung.getVorgaengerVerfuegung();

		// Den Zahlungsstatus aus der letzten *ausbezahlten* Verfuegung berechnen
		if (ausbezahlteVorgaenger != null) {
			// Zahlungsstatus aus vorgaenger uebernehmen
			VerfuegungUtil.setZahlungsstatus(verfuegungZuBerechnen, ausbezahlteVorgaenger);
			VerfuegungUtil.setIsSameAusbezahlteVerguenstigung(verfuegungZuBerechnen, ausbezahlteVorgaenger);
		}
		// Das Flag "Gleiche Verfügungsdaten" aus der letzten Verfuegung berechnen
		if (vorgaengerVerfuegung != null) {
			// Ueberpruefen, ob sich die Verfuegungsdaten veraendert haben
			VerfuegungUtil.setIsSameVerfuegungsdaten(verfuegungZuBerechnen, vorgaengerVerfuegung);
		}
	}

	/**
	 * Wenn eine Verfuegung schon Freigegeben ist wird sie nicht mehr neu berechnet, trotzdem muessen wir den
	 * Restanspruch beruecksichtigen
	 */
	@Nonnull
	private List<VerfuegungZeitabschnitt> getRestanspruchForVerfuegteBetreung(Betreuung betreuung) {
		List<VerfuegungZeitabschnitt> restanspruchZeitabschnitte;
		Verfuegung verfuegungForRestanspruch = betreuung.getVerfuegungOrVorgaengerVerfuegung();
		if (verfuegungForRestanspruch == null) {
			String message = "Ungueltiger Zustand, geschlossene Betreuung ohne Verfuegung oder Vorgaengerverfuegung ("
				+ betreuung.getId()
				+ ')';
			throw new EbeguRuntimeException("getRestanspruchForVerfuegteBetreung", message);
		}
		Objects.requireNonNull(verfuegungForRestanspruch.getBetreuung());
		RestanspruchInitializer restanspruchInitializer = new RestanspruchInitializer();
		restanspruchZeitabschnitte = restanspruchInitializer.executeIfApplicable(
			verfuegungForRestanspruch.getBetreuung(), verfuegungForRestanspruch.getZeitabschnitte());

		return restanspruchZeitabschnitte;
	}

	private List<Rule> findRulesToRunForPeriode(@Nonnull Gesuchsperiode gesuchsperiode) {
		List<Rule> rulesForGesuchsperiode = new LinkedList<>();
		for (Rule rule : rules) {
			// Die Regel muss irgendwann waehrend der Gesuchsperiode gueltig sein, sonst muessen wir sie nicht beachten
			if (rule.isValid(gesuchsperiode.getGueltigkeit())) {
				rulesForGesuchsperiode.add(rule);
			} else {
				LOG.debug("Rule did not aply to Gesuchsperiode {}", rule);
			}
		}
		return rulesForGesuchsperiode;
	}

	private List<RechnerRule> rechnerRulesForGemeinde(@Nonnull BGRechnerParameterDTO bgRechnerParameterDTO, @Nonnull Locale locale) {
		List<RechnerRule> rechnerRules = new LinkedList<>();
		if (bgRechnerParameterDTO.getGemeindeParameter().getGemeindeZusaetzlicherGutscheinEnabled()) {
			rechnerRules.add(new ZusaetzlicherGutscheinGemeindeRechnerRule(locale));
			rechnerRules.add(new ZusaetzlicherBabyGutscheinRechnerRule(locale));
		}
		return rechnerRules;
	}
}
