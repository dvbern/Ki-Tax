/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EingewoehnungTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.rechner.BGRechnerFactory;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;
import ch.dvbern.ebegu.rules.initalizer.RestanspruchInitializer;
import ch.dvbern.ebegu.rules.initalizer.RestanspruchInitializerVisitor;
import ch.dvbern.ebegu.util.EinschulungstypBgStundenFaktorVisitor;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Fuehrt die eigentlichen Berechnungen durch: Rules und Rechner.
 *  Augelagert zur bessern Testbarkeit
 */
public class BetreuungsgutscheinExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(BetreuungsgutscheinExecutor.class);

	private boolean isDebug = true;

	private Map<EinstellungKey, Einstellung> kibonAbschlussRulesParameters;

	public BetreuungsgutscheinExecutor(boolean isDebug, Map<EinstellungKey, Einstellung> kibonAbschlussRulesParameters) {
		this.isDebug = isDebug;
		this.kibonAbschlussRulesParameters = kibonAbschlussRulesParameters;
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
						"{} ({}: {})",
						rule.getClass().getSimpleName(),
						rule.getRuleKey().name(),
						rule.getRuleType().name());
					for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {
						LOG.info("{}", verfuegungZeitabschnitt);
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
		EingewoehnungTyp eingewoehnungTyp = EingewoehnungTyp.valueOf(kibonAbschlussRulesParameters.get(EinstellungKey.EINGEWOEHNUNG_TYP).getValue());
		EingewoehnungFristRule eingewoehnungFristRule = new EingewoehnungFristRule(locale, isDebug, eingewoehnungTyp);
		AnspruchFristRule anspruchFristRule = new AnspruchFristRule(isDebug);
		AbschlussNormalizer abschlussNormalizerOhneMonate = new AbschlussNormalizer(false, isDebug);
		MonatsRule monatsRule = new MonatsRule(isDebug);
		Boolean anspruchMonatsweise = kibonAbschlussRulesParameters.get(EinstellungKey.ANSPRUCH_MONATSWEISE).getValueAsBoolean();
		MonatsMergerRule monatsMergerRule = new MonatsMergerRule(isDebug, anspruchMonatsweise);
		Boolean pauschaleRueckwirkendAuszahlen = kibonAbschlussRulesParameters.get(EinstellungKey.FKJV_PAUSCHALE_RUECKWIRKEND).getValueAsBoolean();
		MutationsMerger mutationsMerger = new MutationsMerger(locale, isDebug, pauschaleRueckwirkendAuszahlen);
		AbschlussNormalizer abschlussNormalizerMitMonate = new AbschlussNormalizer(!platz.getBetreuungsangebotTyp().isTagesschule(), isDebug);
		// Bei Eingewoehnung ist der Anspruch von einer Monat verlaengt
		zeitabschnitte = eingewoehnungFristRule.executeIfApplicable(platz, zeitabschnitte);
		// Innerhalb eines Monats darf der Anspruch nie sinken
		zeitabschnitte = anspruchFristRule.executeIfApplicable(platz, zeitabschnitte);
		// Falls jetzt noch Abschnitte "gleich" sind, im Sinne der *angezeigten* Daten, diese auch noch mergen
		zeitabschnitte = abschlussNormalizerOhneMonate.executeIfApplicable(platz, zeitabschnitte);
		// Nach dem Durchlaufen aller Rules noch die Monatsstückelungen machen
		zeitabschnitte = monatsRule.executeIfApplicable(platz, zeitabschnitte);
		// Danach den Anspruch nur noch monatsweise berechnet werden
		zeitabschnitte = monatsMergerRule.executeIfApplicable(platz, zeitabschnitte);
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
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {
			var rechnerToUse = BGRechnerFactory.getRechner(kitaxParameter, locale, rechnerRulesForGemeinde, platz, verfuegungZeitabschnitt);
			rechnerToUse.calculate(verfuegungZeitabschnitt, bgRechnerParameterDTO);
		}
	}

	@Nonnull
	public List<VerfuegungZeitabschnitt> executeRestanspruchInitializer(
		@Nonnull AbstractPlatz platz,
		@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte
	) {
		RestanspruchInitializer restanspruchInitializer =
			new RestanspruchInitializerVisitor(isDebug).getRestanspruchInitialzier(platz.extractGesuch().extractMandant());
		return restanspruchInitializer.executeIfApplicable(platz, zeitabschnitte);
	}

	public static void initFaktorBgStunden(
			EinschulungTyp einschulungTyp,
			List<VerfuegungZeitabschnitt> zeitabschnitte,
			Mandant mandant) {
		zeitabschnitte.forEach(verfuegungZeitabschnitt -> {
			final EinschulungstypBgStundenFaktorVisitor einschulungstypBgStundenFaktorVisitor =
					new EinschulungstypBgStundenFaktorVisitor(einschulungTyp);
			verfuegungZeitabschnitt.setBgStundenFaktor(einschulungstypBgStundenFaktorVisitor.getFaktor(mandant));
		});
	}
}
