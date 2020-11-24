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

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.KitaxUebergangsloesungInstitutionOeffnungszeiten;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rechner.AbstractRechner;
import ch.dvbern.ebegu.rechner.BGRechnerFactory;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.kitax.EmptyKitaxRechner;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;
import ch.dvbern.ebegu.rules.initalizer.RestanspruchInitializer;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.KitaxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Fuehrt die eigentlichen Berechnungen durch: Rules und Rechner.
 *  Augelagert zur bessern Testbarkeit
 */
public class BetreuungsgutscheinExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(BetreuungsgutscheinExecutor.class);

	private boolean isDebug = true;

	public BetreuungsgutscheinExecutor(boolean isDebug) {
		this.isDebug = isDebug;
	}

	public List<VerfuegungZeitabschnitt> executeRules(
		@Nonnull List<Rule> rulesToRun,
		@Nonnull AbstractPlatz platz,
		@Nonnull List<VerfuegungZeitabschnitt> initialeRestanspruecke
	) {
		return executeRules(rulesToRun, platz, initialeRestanspruecke, false);
	}

	public List<VerfuegungZeitabschnitt> executeRules(
		@Nonnull List<Rule> rulesToRun,
		@Nonnull AbstractPlatz platz,
		@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte,
		boolean calculateOnlyFamiliensituation
	) {
		for (Rule rule : rulesToRun) {
			if (!calculateOnlyFamiliensituation || rule.isRelevantForFamiliensituation()) {
				zeitabschnitte = rule.calculate(platz, zeitabschnitte);
				if (isDebug) {
					LOG.info(
						"{} ({}: {}" + ')',
						rule.getClass().getSimpleName(),
						rule.getRuleKey().name(),
						rule.getRuleType().name());
					for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {
						LOG.info(verfuegungZeitabschnitt.toString());
					}
				}
			}
		}
		return zeitabschnitte;
	}

	@Nonnull
	public List<VerfuegungZeitabschnitt> executeAbschlussRules(
		@Nonnull AbstractPlatz platz,
		@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte,
		@Nonnull Locale locale
	) {
		AnspruchFristRule anspruchFristRule = new AnspruchFristRule(isDebug);
		AbschlussNormalizer abschlussNormalizerOhneMonate = new AbschlussNormalizer(false, isDebug);
		MonatsRule monatsRule = new MonatsRule(isDebug);
		MutationsMerger mutationsMerger = new MutationsMerger(locale, isDebug);
		AbschlussNormalizer abschlussNormalizerMitMonate = new AbschlussNormalizer(!platz.getBetreuungsangebotTyp().isTagesschule(), isDebug);

		// Innerhalb eines Monats darf der Anspruch nie sinken
		zeitabschnitte = anspruchFristRule.executeIfApplicable(platz, zeitabschnitte);
		// Falls jetzt noch Abschnitte "gleich" sind, im Sinne der *angezeigten* Daten, diese auch noch mergen
		zeitabschnitte = abschlussNormalizerOhneMonate.executeIfApplicable(platz, zeitabschnitte);
		// Nach dem Durchlaufen aller Rules noch die Monatsstückelungen machen
		zeitabschnitte = monatsRule.executeIfApplicable(platz, zeitabschnitte);
		// Ganz am Ende der Berechnung mergen wir das aktuelle Ergebnis mit der Verfügung des letzten Gesuches
		zeitabschnitte = mutationsMerger.executeIfApplicable(platz, zeitabschnitte);
		// Falls jetzt wieder Abschnitte innerhalb eines Monats "gleich" sind, im Sinne der *angezeigten*
		// Daten, diese auch noch mergen
		zeitabschnitte = abschlussNormalizerMitMonate.executeIfApplicable(platz, zeitabschnitte);
		return zeitabschnitte;
	}

	public void calculateRechner(
		@Nonnull BGRechnerParameterDTO bgRechnerParameterDTO,
		@Nonnull KitaxUebergangsloesungParameter kitaxParameter,
		@Nonnull Locale locale,
		@Nonnull List<RechnerRule> rechnerRulesForGemeinde,
		@Nonnull AbstractPlatz platz,
		@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte
	) {
		AbstractRechner asivRechner = BGRechnerFactory.getRechner(platz.getBetreuungsangebotTyp(), rechnerRulesForGemeinde);
		final boolean possibleKitaxRechner = KitaxUtil.isGemeindeWithKitaxUebergangsloesung(platz.extractGemeinde())
			&& platz.getBetreuungsangebotTyp().isJugendamt();
		// Den richtigen Rechner anwerfen
		zeitabschnitte.forEach(zeitabschnitt -> {
			// Es kann erst jetzt entschieden werden, welcher Rechner zum Einsatz kommt,
			// da fuer Stadt Bern bis zum Zeitpunkt X der alte Ki-Tax Rechner verwendet werden soll.
			AbstractRechner rechnerToUse = null;
			if (possibleKitaxRechner) {
				if (zeitabschnitt.getGueltigkeit().endsBefore(kitaxParameter.getStadtBernAsivStartDate())) {
					if (zeitabschnitt.getBgCalculationInputGemeinde().isBetreuungInGemeinde()) {
						String kitaName = platz.getInstitutionStammdaten().getInstitution().getName();
						KitaxUebergangsloesungInstitutionOeffnungszeiten oeffnungszeiten = null;
						if (platz.getInstitutionStammdaten().getBetreuungsangebotTyp().isKita()) {
							// Die Oeffnungszeiten sind nur fuer Kitas relevant
							oeffnungszeiten = kitaxParameter.getOeffnungszeiten(kitaName);
						}
						rechnerToUse = BGRechnerFactory.getKitaxRechner(platz, kitaxParameter, oeffnungszeiten, locale);
					} else {
						// Betreuung findet nicht in Gemeinde statt
						rechnerToUse = new EmptyKitaxRechner(locale, MsgKey.ZUSATZGUTSCHEIN_NEIN_NICHT_IN_GEMEINDE);
					}
				} else if (kitaxParameter.isStadtBernAsivConfiguered()) {
					// Es ist Bern, und der Abschnitt liegt nach dem Stichtag. Falls ASIV schon konfiguriert ist,
					// koennen wir den normalen ASIV Rechner verwenden.
					rechnerToUse = asivRechner;
				} else {
					// Auch in diesem Fall muss zumindest ein leeres Objekt erstellt werden. Evtl. braucht es hier einen
					// NullRechner? Wegen Bemerkungen?
					rechnerToUse = new EmptyKitaxRechner(locale, MsgKey.FEBR_INFO_ASIV_NOT_CONFIGUERD);
				}
			} else {
				// Alle anderen rechnen normal mit dem Asiv-Rechner
				rechnerToUse = asivRechner;
			}
			if (rechnerToUse != null) {
				rechnerToUse.calculate(zeitabschnitt, bgRechnerParameterDTO);
			}
		});
	}

	@Nonnull
	public List<VerfuegungZeitabschnitt> executeRestanspruchInitializer(
		@Nonnull AbstractPlatz platz,
		@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte
	) {
		RestanspruchInitializer restanspruchInitializer = new RestanspruchInitializer(isDebug);
		return restanspruchInitializer.executeIfApplicable(platz, zeitabschnitte);
	}
}
