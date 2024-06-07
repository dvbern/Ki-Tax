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
import ch.dvbern.ebegu.enums.*;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationRechnerFactory;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rules.util.BemerkungsMerger;
import ch.dvbern.ebegu.rules.util.EinstellungenMap;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.*;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_ERWERBSPENSUM_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_ERWERBSPENSUM_NICHT_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.*;
import static ch.dvbern.ebegu.rules.initalizer.RestanspruchInitializer.createInitialenRestanspruch;
import static ch.dvbern.ebegu.util.Constants.DATE_FORMATTER;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.*;

/**
 * Hilfsklasse fuer Ebegu-Rule-Tests
 */
public final class EbeguRuleTestsHelper {

	private static Gesuchsperiode gesuchsperiodeOfAllTimes = null;
	private static final Map<EinstellungKey, Einstellung> einstellungenGemaessAsiv =
			getEinstellungenConfiguratorAsiv(getGesuchsperiodeOfAllTimes());
	private static final BetreuungsgutscheinConfigurator ruleConfigurator = new BetreuungsgutscheinConfigurator();
	private static final KitaxUebergangsloesungParameter kitaxParams =
			TestDataUtil.geKitaxUebergangsloesungParameter();

	private static Gesuchsperiode getGesuchsperiodeOfAllTimes() {
		if (gesuchsperiodeOfAllTimes == null) {
			gesuchsperiodeOfAllTimes = new Gesuchsperiode();
			gesuchsperiodeOfAllTimes.setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		}
		return gesuchsperiodeOfAllTimes;
	}

	private static final boolean isDebug = false;
	private static BetreuungsgutscheinExecutor executor =
			new BetreuungsgutscheinExecutor(isDebug, einstellungenGemaessAsiv);

	private EbeguRuleTestsHelper() {
	}

	@Nonnull
	public static List<VerfuegungZeitabschnitt> runSingleAbschlussRule(
			@Nonnull AbstractAbschlussRule abschlussRule,
			@Nonnull AbstractPlatz platz,
			@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte
	) {
		List<VerfuegungZeitabschnitt> result = abschlussRule.executeIfApplicable(platz, zeitabschnitte);
		// wird eigentlich nur noch in Tests verwendet, welche nicht direkt den Rechner aufrufen
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : result) {
			verfuegungZeitabschnitt.initBGCalculationResult();
		}
		return result;
	}

	public static List<VerfuegungZeitabschnitt> calculate(
			AbstractPlatz betreuung,
			@Nonnull Map<EinstellungKey, Einstellung> einstellungenRules,
			@Nonnull Map<EinstellungKey, Einstellung> einstellungenAbschlussRules) {
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte =
				createInitialenRestanspruch(betreuung.extractGesuchsperiode(), false);
		TestDataUtil.calculateFinanzDaten(
				betreuung.extractGesuch(),
				FinanzielleSituationRechnerFactory.getRechner(betreuung.extractGesuch()));
		BetreuungsgutscheinExecutor executorWithSpecificAbschlussRules =
				new BetreuungsgutscheinExecutor(isDebug, einstellungenAbschlussRules);
		return calculate(
				betreuung,
				initialenRestanspruchAbschnitte,
				einstellungenRules,
				executorWithSpecificAbschlussRules);
	}

	public static List<VerfuegungZeitabschnitt> calculate(
			AbstractPlatz betreuung,
			@Nonnull Map<EinstellungKey, Einstellung> einstellungenAbschlussRules,
			boolean doMonatsstueckelung) {
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte =
				createInitialenRestanspruch(betreuung.extractGesuchsperiode(), false);
		TestDataUtil.calculateFinanzDaten(
				betreuung.extractGesuch(),
				FinanzielleSituationRechnerFactory.getRechner(betreuung.extractGesuch()));
		BetreuungsgutscheinExecutor executorWithSpecificAbschlussRules =
				new BetreuungsgutscheinExecutor(isDebug, einstellungenAbschlussRules);
		return calculateAllRules(
				betreuung,
				einstellungenGemaessAsiv,
				initialenRestanspruchAbschnitte,
				executorWithSpecificAbschlussRules,
				doMonatsstueckelung);
	}

	public static List<VerfuegungZeitabschnitt> calculate(
			AbstractPlatz betreuung,
			@Nonnull Map<EinstellungKey, Einstellung> einstellungenRules) {
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte =
				createInitialenRestanspruch(betreuung.extractGesuchsperiode(), false);
		TestDataUtil.calculateFinanzDaten(
				betreuung.extractGesuch(),
				FinanzielleSituationRechnerFactory.getRechner(betreuung.extractGesuch()));
		return calculate(betreuung, initialenRestanspruchAbschnitte, einstellungenRules, executor);
	}

	public static List<VerfuegungZeitabschnitt> calculate(AbstractPlatz betreuung) {
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte =
				createInitialenRestanspruch(betreuung.extractGesuchsperiode(), false);
		TestDataUtil.calculateFinanzDaten(
				betreuung.extractGesuch(),
				FinanzielleSituationRechnerFactory.getRechner(betreuung.extractGesuch()));
		return calculate(betreuung, initialenRestanspruchAbschnitte, einstellungenGemaessAsiv, executor);
	}

	public static List<VerfuegungZeitabschnitt> calculateInklAllgemeineRegeln(Betreuung betreuung) {
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte =
				createInitialenRestanspruch(betreuung.extractGesuchsperiode(), false);
		TestDataUtil.calculateFinanzDaten(
				betreuung.extractGesuch(),
				FinanzielleSituationRechnerFactory.getRechner(betreuung.extractGesuch()));
		return calculateInklAllgemeineRegeln(betreuung, initialenRestanspruchAbschnitte);
	}

	/**
	 * Testhilfsmethode die eine Betreuung so berechnet als haette es vorher schon eine Betreuung gegeben welche einen
	 * Teil des anspruchs
	 * aufgebraucht hat, es wird  als bestehnder Restnanspruch der Wert von existingRestanspruch genommen
	 */
	public static List<VerfuegungZeitabschnitt> calculateWithRemainingRestanspruch(
			Betreuung betreuung,
			int existingRestanspruch) {
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte =
				createInitialenRestanspruch(betreuung.extractGesuchsperiode(), false);
		TestDataUtil.calculateFinanzDaten(
				betreuung.extractGesuch(),
				FinanzielleSituationRechnerFactory.getRechner(betreuung.extractGesuch()));
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : initialenRestanspruchAbschnitte) {
			verfuegungZeitabschnitt.setAnspruchspensumRestForAsivAndGemeinde(existingRestanspruch);
		}
		return calculate(betreuung, initialenRestanspruchAbschnitte, einstellungenGemaessAsiv, executor);
	}

	@Nonnull
	private static List<VerfuegungZeitabschnitt> calculate(
			AbstractPlatz betreuung,
			List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte,
			@Nonnull Map<EinstellungKey, Einstellung> einstellungenGemeinde,
			@Nonnull BetreuungsgutscheinExecutor executorToUse) {
		return calculateAllRules(
				betreuung,
				einstellungenGemeinde,
				initialenRestanspruchAbschnitte,
				executorToUse,
				false);
	}

	@Nonnull
	private static List<VerfuegungZeitabschnitt> calculateInklAllgemeineRegeln(
			Betreuung betreuung,
			List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte) {
		return calculateAllRules(betreuung, einstellungenGemaessAsiv, initialenRestanspruchAbschnitte, executor, true);
	}

	@Nonnull
	private static List<VerfuegungZeitabschnitt> calculateAllRules(
			@Nonnull AbstractPlatz platz,
			@Nonnull Map<EinstellungKey, Einstellung> einstellungenGemeinde,
			@Nonnull List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte,
			@Nonnull BetreuungsgutscheinExecutor executorToUse,
			boolean doMonatsstueckelungen
	) {
		RuleParameterUtil ruleParameterUtil = new RuleParameterUtil(
			einstellungenGemeinde,
			Arrays.asList(DemoFeatureTyp.values()),
			kitaxParams, Constants.
			DEFAULT_LOCALE);
		final List<Rule> rules = ruleConfigurator.configureRulesForMandant(platz.extractGemeinde(), ruleParameterUtil);

		List<VerfuegungZeitabschnitt> result =
				executorToUse.executeRules(rules, platz, initialenRestanspruchAbschnitte);
		final BGRechnerParameterDTO bgRechnerParameterDTO = new BGParameterVisitor().getBGParameterForMandant(platz.extractGesuch().extractMandant());
		// Die Abschluss-Rules ebenfalls ausführen
		result = executorToUse.executeAbschlussRules(platz, result, Locale.GERMAN);
		executorToUse.calculateRechner(
				bgRechnerParameterDTO,
				kitaxParams,
				Locale.GERMAN,
				Collections.emptyList(),
				platz,
				result);

		if (!doMonatsstueckelungen) {
			AbschlussNormalizer abschlussNormalizer = new AbschlussNormalizer(false, isDebug);
			result = abschlussNormalizer.execute(platz, result);
		}

		Mandant mandant = platz.extractGesuch().extractMandant();
		BemerkungsMerger.prepareGeneratedBemerkungen(result, mandant);
		Objects.requireNonNull(platz.getKind().getKindJA().getEinschulungTyp());
		executorToUse.initFaktorBgStunden(platz.getKind().getKindJA().getEinschulungTyp(), result, mandant);
		executorToUse.executeRestanspruchInitializer(platz, result);
		return result;
	}

	public static Map<EinstellungKey, Einstellung> getEinstellungenConfiguratorAsiv(
			@Nonnull Gesuchsperiode gesuchsperiode) {
		EinstellungenMap einstellungenMap = new EinstellungenMap();

		einstellungenMap.addEinstellung(MAX_MASSGEBENDES_EINKOMMEN, EINSTELLUNG_MAX_EINKOMMEN, gesuchsperiode);
		einstellungenMap.addEinstellung(
				PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,
				PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,
				gesuchsperiode);
		einstellungenMap.addEinstellung(
				PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,
				PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,
				gesuchsperiode);
		einstellungenMap.addEinstellung(
				PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,
				PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,
				gesuchsperiode);
		einstellungenMap.addEinstellung(
				PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,
				PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,
				gesuchsperiode);
		einstellungenMap.addEinstellung(PARAM_MAX_TAGE_ABWESENHEIT, "30", gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE,
				EinschulungTyp.KINDERGARTEN2.name(),
				gesuchsperiode);
		einstellungenMap.addEinstellung(
			ANGEBOT_SCHULSTUFE,
			BetreuungsangebotTyp.KITA.name(),
			gesuchsperiode);
		einstellungenMap.addEinstellung(
				MIN_ERWERBSPENSUM_EINGESCHULT,
				EINSTELLUNG_MIN_ERWERBSPENSUM_EINGESCHULT,
				gesuchsperiode);
		einstellungenMap.addEinstellung(
				MIN_ERWERBSPENSUM_NICHT_EINGESCHULT,
				EINSTELLUNG_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT,
				gesuchsperiode);
		einstellungenMap.addEinstellung(FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM, "10", gesuchsperiode);
		einstellungenMap.addEinstellung(DAUER_BABYTARIF, "12", gesuchsperiode);
		// Gemaess ASIV: Wir nehmen eben den ASIV Wert!
		einstellungenMap.addEinstellung(
				GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT,
				EINSTELLUNG_MIN_ERWERBSPENSUM_EINGESCHULT,
				gesuchsperiode);
		// Gemaess ASIV: Wir nehmen eben den ASIV Wert!
		einstellungenMap.addEinstellung(
				GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT,
				EINSTELLUNG_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT,
				gesuchsperiode);
		einstellungenMap.addEinstellung(ERWERBSPENSUM_ZUSCHLAG, "20", gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED,
				"false",
				gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT,
				"0",
				gesuchsperiode);
		// Mahlzeitenverguenstigung
		einstellungenMap.addEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED,
				"false",
				gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT,
				"0",
				gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN,
				"50000",
				gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT,
				"0",
				gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN,
				"70000",
				gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT,
				"0",
				gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT,
				"2",
				gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER, "false", gesuchsperiode);
		// FKJV
		einstellungenMap.addEinstellung(FKJV_PAUSCHALE_BEI_ANSPRUCH, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(FKJV_PAUSCHALE_RUECKWIRKEND, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF, "null", gesuchsperiode);
		einstellungenMap.addEinstellung(EINGEWOEHNUNG_TYP, EingewoehnungTyp.KEINE.toString(), gesuchsperiode);
		einstellungenMap.addEinstellung(ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
				AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name(), gesuchsperiode);
		einstellungenMap.addEinstellung(MINIMALDAUER_KONKUBINAT, "5", gesuchsperiode);
		einstellungenMap.addEinstellung(ANSPRUCH_MONATSWEISE, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(AUSSERORDENTLICHER_ANSPRUCH_RULE, "ASIV", gesuchsperiode);
		einstellungenMap.addEinstellung(KINDERABZUG_TYP, "ASIV", gesuchsperiode);
		einstellungenMap.addEinstellung(FKJV_TEXTE, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(FACHSTELLEN_TYP, "BERN", gesuchsperiode);
		einstellungenMap.addEinstellung(SPRACHFOERDERUNG_BESTAETIGEN, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(GESUCH_BEENDEN_BEI_TAUSCH_GS2, "false", gesuchsperiode);
		// LU
		einstellungenMap.addEinstellung(KITAPLUS_ZUSCHLAG_AKTIVIERT, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(GESCHWISTERNBONUS_TYP, "NONE", gesuchsperiode);
		einstellungenMap.addEinstellung(ANSPRUCH_AB_X_MONATEN, "0", gesuchsperiode);
		einstellungenMap.addEinstellung(SCHULERGAENZENDE_BETREUUNGEN,"false", gesuchsperiode);
		einstellungenMap.addEinstellung(WEGZEIT_ERWERBSPENSUM, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(ANWESENHEITSTAGE_PRO_MONAT_AKTIVIERT,"false", gesuchsperiode);
		einstellungenMap.addEinstellung(SOZIALVERSICHERUNGSNUMMER_PERIODE, "false", gesuchsperiode);

		//SZ
		einstellungenMap.addEinstellung(HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG_AKTIVIERT, "false", gesuchsperiode);

		return einstellungenMap.getEinstellungen();
	}

	public static Map<EinstellungKey, Einstellung> getEinstellungenRulesAsiv(@Nonnull Gesuchsperiode gesuchsperiode) {
		EinstellungenMap einstellungenMap = new EinstellungenMap();

		einstellungenMap.addEinstellung(PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG, "20", gesuchsperiode);
		einstellungenMap.addEinstellung(PARAM_PENSUM_KITA_MIN, "0", gesuchsperiode);
		einstellungenMap.addEinstellung(PARAM_PENSUM_TAGESELTERN_MIN, "0", gesuchsperiode);
		einstellungenMap.addEinstellung(PARAM_PENSUM_TAGESSCHULE_MIN, "0", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_KONTINGENTIERUNG_ENABLED, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG, "150", gesuchsperiode);
		einstellungenMap.addEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG, "100", gesuchsperiode);
		einstellungenMap.addEinstellung(MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG, "75", gesuchsperiode);
		einstellungenMap.addEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD, "11.90", gesuchsperiode);
		einstellungenMap.addEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD, "8.50", gesuchsperiode);
		einstellungenMap.addEinstellung(MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD, "8.50", gesuchsperiode);
		einstellungenMap.addEinstellung(MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD, "8.50", gesuchsperiode);
		einstellungenMap.addEinstellung(MIN_MASSGEBENDES_EINKOMMEN, "43000", gesuchsperiode);
		einstellungenMap.addEinstellung(OEFFNUNGSTAGE_KITA, "240", gesuchsperiode);
		einstellungenMap.addEinstellung(OEFFNUNGSTAGE_TFO, "240", gesuchsperiode);
		einstellungenMap.addEinstellung(OEFFNUNGSSTUNDEN_TFO, "11", gesuchsperiode);
		einstellungenMap.addEinstellung(ZUSCHLAG_BEHINDERUNG_PRO_TG, "50", gesuchsperiode);
		einstellungenMap.addEinstellung(ZUSCHLAG_BEHINDERUNG_PRO_STD, "4.25", gesuchsperiode);
		einstellungenMap.addEinstellung(MIN_VERGUENSTIGUNG_PRO_TG, "7", gesuchsperiode);
		einstellungenMap.addEinstellung(MIN_VERGUENSTIGUNG_PRO_STD, "0.70", gesuchsperiode);
		einstellungenMap.addEinstellung(FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION, "20", gesuchsperiode);
		einstellungenMap.addEinstellung(FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION, "60", gesuchsperiode);
		einstellungenMap.addEinstellung(FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION, "40", gesuchsperiode);
		einstellungenMap.addEinstellung(FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION, "40", gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB,
				DATE_FORMATTER.format(gesuchsperiode.getGueltigkeit().getGueltigAb()),
				gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB,
				DATE_FORMATTER.format(gesuchsperiode.getGueltigkeit().getGueltigAb()),
				gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG,
				DATE_FORMATTER.format(gesuchsperiode.getGueltigkeit().getGueltigAb()),
				gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_TAGESSCHULE_TAGIS_ENABLED, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG,
				"false",
				gesuchsperiode);
		einstellungenMap.addEinstellung(FACHSTELLEN_TYP, "BERN", gesuchsperiode);
		// Zusaetzlicher Gutschein der Gemeinde
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_TYP, "PAUSCHAL", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA, "0.00", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_KITA_MIN, "0", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_KITA_MAX, "0", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_TFO_MIN, "0", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_TFO_MAX, "0", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_MIN_MASSGEBENDES_EINKOMMEN, "0", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_MAX_MASSGEBENDES_EINKOMMEN, "0", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO, "0.00", gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA,
				EinschulungTyp.VORSCHULALTER.name(),
				gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO,
				EinschulungTyp.VORSCHULALTER.name(),
				gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA, "0.00", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO, "0.00", gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT,
				"false",
				gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA,
				"0",
				gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO, "0",
				gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE,
				"0",
				gesuchsperiode);
		einstellungenMap.addEinstellung(
				GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG,
				"160000",
				gesuchsperiode);
		// Schnittstellt Ki-Tax
		einstellungenMap.addEinstellung(GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED, "false", gesuchsperiode);
		// FKJV
		einstellungenMap.addEinstellung(AUSSERORDENTLICHER_ANSPRUCH_RULE, "ASIV", gesuchsperiode);

		return einstellungenMap.getEinstellungen();
	}

	public static Map<EinstellungKey, Einstellung> getEinstellungenRulesParis(@Nonnull Gesuchsperiode gesuchsperiode) {
		final Map<EinstellungKey, Einstellung> einstellungenGemeinde = new HashMap<>();
		einstellungenGemeinde.putAll(getAllEinstellungen(gesuchsperiode));
		einstellungenGemeinde.get(GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED).setValue("true");
		einstellungenGemeinde.get(GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT).setValue("20");
		einstellungenGemeinde.get(GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT).setValue("15");
		einstellungenGemeinde.get(GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT).setValue("30");

		einstellungenGemeinde.get(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED).setValue("true");
		einstellungenGemeinde.get(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA).setValue("11.00");
		einstellungenGemeinde.get(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO).setValue("0.11");
		einstellungenGemeinde.get(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA)
				.setValue(EinschulungTyp.VORSCHULALTER.name());
		einstellungenGemeinde.get(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO)
				.setValue(EinschulungTyp.VORSCHULALTER.name());

		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED).setValue("true");
		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED).setValue("true");
		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT)
				.setValue("6");
		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN).setValue("50000");
		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT)
				.setValue("3");
		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN).setValue("70000");
		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT)
				.setValue("0");
		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT).setValue("2");

		return einstellungenGemeinde;
	}

	public static Map<EinstellungKey, Einstellung> getEinstellungeRechnerAsiv(@Nonnull Gesuchsperiode gesuchsperiode) {
		EinstellungenMap einstellungenMap = new EinstellungenMap();
		einstellungenMap.addEinstellung(MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG, "12.24", gesuchsperiode);
		einstellungenMap.addEinstellung(MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG, "6.11", gesuchsperiode);
		einstellungenMap.addEinstellung(MIN_TARIF, "0.78", gesuchsperiode);
		return einstellungenMap.getEinstellungen();
	}

	public static Map<EinstellungKey, Einstellung> getAllEinstellungen(@Nonnull Gesuchsperiode gesuchsperiode) {
		// Wir brauchen alle Einstellungen fuer den Configurator
		Map<EinstellungKey, Einstellung> einstellungen =
				EbeguRuleTestsHelper.getEinstellungenConfiguratorAsiv(gesuchsperiode);
		// ... und fuer Rechner
		einstellungen.putAll(EbeguRuleTestsHelper.getEinstellungeRechnerAsiv(gesuchsperiode));
		// ... und fuer die Rules
		einstellungen.putAll(EbeguRuleTestsHelper.getEinstellungenRulesAsiv(gesuchsperiode));
		return einstellungen;
	}

	public static List<VerfuegungZeitabschnitt> initializeRestanspruchForNextBetreuung(
			Betreuung currentBetreuung,
			List<VerfuegungZeitabschnitt> zeitabschnitte) {
		return executor.executeRestanspruchInitializer(currentBetreuung, zeitabschnitte);
	}

	public static Betreuung createBetreuungWithPensum(
			LocalDate von, LocalDate bis,
			BetreuungsangebotTyp angebot,
			int pensum,
			BigDecimal monatlicheBetreuungskosten
	) {
		return createBetreuungWithPensum(
				von,
				bis,
				angebot,
				pensum,
				monatlicheBetreuungskosten,
				BigDecimal.ZERO,
				BigDecimal.ZERO);
	}

	public static Betreuung createBetreuungWithPensum(
			LocalDate von, LocalDate bis,
			BetreuungsangebotTyp angebot,
			int pensum,
			BigDecimal monatlicheBetreuungskosten,
			Mandant mandant
	) {
		return createBetreuungWithPensum(
				von,
				bis,
				angebot,
				pensum,
				monatlicheBetreuungskosten,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				mandant);
	}

	public static Betreuung createBetreuungWithPensum(
			LocalDate von, LocalDate bis,
			BetreuungsangebotTyp angebot,
			int pensum,
			BigDecimal monatlicheBetreuungskosten,
			BigDecimal monatlicheHauptmahlzeiten,
			BigDecimal monatlicheNebenmahlzeiten
	) {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		final Gesuch gesuch = betreuung.extractGesuch();
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(angebot);
		betreuung.setBetreuungspensumContainers(new LinkedHashSet<>());
		BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		betreuungspensumContainer.setBetreuung(betreuung);
		DateRange gueltigkeit = new DateRange(von, bis);
		betreuungspensumContainer.setBetreuungspensumJA(new Betreuungspensum(gueltigkeit));
		betreuungspensumContainer.getBetreuungspensumJA().setPensum(MathUtil.DEFAULT.from(pensum));
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheBetreuungskosten(monatlicheBetreuungskosten);
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheHauptmahlzeiten(monatlicheHauptmahlzeiten);
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheNebenmahlzeiten(monatlicheNebenmahlzeiten);
		betreuung.getBetreuungspensumContainers().add(betreuungspensumContainer);

		ErweiterteBetreuungContainer container = TestDataUtil.createDefaultErweiterteBetreuungContainer();
		container.setBetreuung(betreuung);
		betreuung.setErweiterteBetreuungContainer(container);

		return betreuung;
	}

	public static Betreuung createBetreuungWithPensum(
			LocalDate von, LocalDate bis,
			BetreuungsangebotTyp angebot,
			int pensum,
			BigDecimal monatlicheBetreuungskosten,
			BigDecimal monatlicheHauptmahlzeiten,
			BigDecimal monatlicheNebenmahlzeiten,
			Mandant mandant
	) {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false, mandant);
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(angebot);
		betreuung.setBetreuungspensumContainers(new LinkedHashSet<>());
		BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		betreuungspensumContainer.setBetreuung(betreuung);
		DateRange gueltigkeit = new DateRange(von, bis);
		betreuungspensumContainer.setBetreuungspensumJA(new Betreuungspensum(gueltigkeit));
		betreuungspensumContainer.getBetreuungspensumJA().setPensum(MathUtil.DEFAULT.from(pensum));
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheBetreuungskosten(monatlicheBetreuungskosten);
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheHauptmahlzeiten(monatlicheHauptmahlzeiten);
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheNebenmahlzeiten(monatlicheNebenmahlzeiten);
		betreuung.getBetreuungspensumContainers().add(betreuungspensumContainer);

		ErweiterteBetreuungContainer container = TestDataUtil.createDefaultErweiterteBetreuungContainer();
		container.setBetreuung(betreuung);
		betreuung.setErweiterteBetreuungContainer(container);

		return betreuung;
	}
}
