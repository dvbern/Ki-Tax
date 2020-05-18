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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.rules.initalizer.RestanspruchInitializer;
import ch.dvbern.ebegu.rules.util.BemerkungsMerger;
import ch.dvbern.ebegu.rules.util.EinstellungenMap;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.MathUtil;

import static ch.dvbern.ebegu.enums.EinstellungKey.ERWERBSPENSUM_ZUSCHLAG;
import static ch.dvbern.ebegu.enums.EinstellungKey.FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION;
import static ch.dvbern.ebegu.enums.EinstellungKey.FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION;
import static ch.dvbern.ebegu.enums.EinstellungKey.FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION;
import static ch.dvbern.ebegu.enums.EinstellungKey.FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_HAUPTMAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_NEBENMAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_HAUPTMAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_NEBENMAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_HAUPTMAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_NEBENMAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_TAGESSCHULE_TAGIS_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_MASSGEBENDES_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_SCHULE_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_SCHULE_PRO_TG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_ERWERBSPENSUM_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_ERWERBSPENSUM_NICHT_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_MASSGEBENDES_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_TARIF;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_VERGUENSTIGUNG_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_VERGUENSTIGUNG_PRO_TG;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSSTUNDEN_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSTAGE_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSTAGE_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_MAX_TAGE_ABWESENHEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PENSUM_KITA_MIN;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PENSUM_TAGESELTERN_MIN;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PENSUM_TAGESSCHULE_MIN;
import static ch.dvbern.ebegu.enums.EinstellungKey.ZUSCHLAG_BEHINDERUNG_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.ZUSCHLAG_BEHINDERUNG_PRO_TG;
import static ch.dvbern.ebegu.rules.BetreuungsgutscheinEvaluator.createInitialenRestanspruch;
import static ch.dvbern.ebegu.util.Constants.DATE_FORMATTER;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.EINSTELLUNG_MAX_EINKOMMEN;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.EINSTELLUNG_MIN_ERWERBSPENSUM_EINGESCHULT;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.EINSTELLUNG_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6;

/**
 * Hilfsklasse fuer Ebegu-Rule-Tests
 */
public final class EbeguRuleTestsHelper {

	private static Gesuchsperiode gesuchsperiodeOfAllTimes = null;
	private static Map<EinstellungKey, Einstellung> einstellungenGemaessAsiv = getEinstellungenConfiguratorAsiv(getGesuchsperiodeOfAllTimes());
	private static BetreuungsgutscheinConfigurator ruleConfigurator = new BetreuungsgutscheinConfigurator();
	private static KitaxUebergangsloesungParameter kitaxParams = TestDataUtil.geKitaxUebergangsloesungParameter();


	private static Gesuchsperiode getGesuchsperiodeOfAllTimes() {
		if (gesuchsperiodeOfAllTimes == null) {
			gesuchsperiodeOfAllTimes = new Gesuchsperiode();
			gesuchsperiodeOfAllTimes.setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		}
		return gesuchsperiodeOfAllTimes;
	}

	private static final AnspruchFristRule anspruchFristRule = new AnspruchFristRule();
	private static final AbschlussNormalizer abschlussNormalizerKeepMonate = new AbschlussNormalizer(true);
	private static final AbschlussNormalizer abschlussNormalizerDismissMonate = new AbschlussNormalizer(false);
	private static final MutationsMerger mutationsMerger = new MutationsMerger(Locale.GERMAN);
	private static final MonatsRule monatsRule = new MonatsRule();
	private static final RestanspruchInitializer restanspruchInitializer = new RestanspruchInitializer();

	private EbeguRuleTestsHelper() {
	}

	@Nonnull
	public static List<VerfuegungZeitabschnitt> runSingleAbschlussRule(
		@Nonnull AbstractAbschlussRule abschlussRule, @Nonnull AbstractPlatz platz, @Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte
	) {
		List<VerfuegungZeitabschnitt> result = abschlussRule.executeIfApplicable(platz, zeitabschnitte);
		// wird eigentlich nur noch in Tests verwendet, welche nicht direkt den Rechner aufrufen
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : result) {
			verfuegungZeitabschnitt.initBGCalculationResult();
		}
		return result;
	}

	public static List<VerfuegungZeitabschnitt> calculate(AbstractPlatz betreuung, @Nonnull Map<EinstellungKey, Einstellung> einstellungenGemeinde) {
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte = createInitialenRestanspruch(betreuung.extractGesuchsperiode(), false);
		TestDataUtil.calculateFinanzDaten(betreuung.extractGesuch());
		return calculate(betreuung, initialenRestanspruchAbschnitte, einstellungenGemeinde);
	}

	public static List<VerfuegungZeitabschnitt> calculate(AbstractPlatz betreuung) {
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte = createInitialenRestanspruch(betreuung.extractGesuchsperiode(), false);
		TestDataUtil.calculateFinanzDaten(betreuung.extractGesuch());
		return calculate(betreuung, initialenRestanspruchAbschnitte, einstellungenGemaessAsiv);
	}

	public static List<VerfuegungZeitabschnitt> calculateInklAllgemeineRegeln(Betreuung betreuung) {
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte = createInitialenRestanspruch(betreuung.extractGesuchsperiode(), false);
		TestDataUtil.calculateFinanzDaten(betreuung.extractGesuch());
		return calculateInklAllgemeineRegeln(betreuung, initialenRestanspruchAbschnitte);
	}

	/**
	 * Testhilfsmethode die eine Betreuung so berechnet als haette es vorher schon eine Betreuung gegeben welche einen Teil des anspruchs
	 * aufgebraucht hat, es wird  als bestehnder Restnanspruch der Wert von existingRestanspruch genommen
	 */
	public static List<VerfuegungZeitabschnitt> calculateWithRemainingRestanspruch(Betreuung betreuung, int existingRestanspruch) {
		List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte = createInitialenRestanspruch(betreuung.extractGesuchsperiode(), false);
		TestDataUtil.calculateFinanzDaten(betreuung.extractGesuch());
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : initialenRestanspruchAbschnitte) {
			verfuegungZeitabschnitt.setAnspruchspensumRestForAsivAndGemeinde(existingRestanspruch);
		}
		return calculate(betreuung, initialenRestanspruchAbschnitte, einstellungenGemaessAsiv);
	}

	@Nonnull
	private static List<VerfuegungZeitabschnitt> calculate(AbstractPlatz betreuung, List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte, @Nonnull Map<EinstellungKey, Einstellung> einstellungenGemeinde) {
		return calculateAllRules(betreuung, einstellungenGemeinde, initialenRestanspruchAbschnitte, false);
	}

	@Nonnull
	private static List<VerfuegungZeitabschnitt> calculateInklAllgemeineRegeln(Betreuung betreuung, List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte) {
		return calculateAllRules(betreuung, einstellungenGemaessAsiv, initialenRestanspruchAbschnitte, true);
	}

	@Nonnull
	private static List<VerfuegungZeitabschnitt> calculateAllRules(
		@Nonnull AbstractPlatz platz,
		@Nonnull Map<EinstellungKey, Einstellung> einstellungenGemeinde,
		@Nonnull List<VerfuegungZeitabschnitt> initialenRestanspruchAbschnitte,
		boolean doMonatsstueckelungen
	) {
		final List<Rule> rules = ruleConfigurator.configureRulesForMandant(
			platz.extractGemeinde(), einstellungenGemeinde, kitaxParams, Locale.GERMAN);

		List<VerfuegungZeitabschnitt> result = initialenRestanspruchAbschnitte;

		for (Rule rule : rules) {
			result = rule.calculate(platz, result);
		}

		result = anspruchFristRule.executeIfApplicable(platz, result);
		// Der RestanspruchInitializer erstellt Restansprueche, darf nicht das Resultat ueberschreiben!
		restanspruchInitializer.executeIfApplicable(platz, result);
		result = abschlussNormalizerDismissMonate.executeIfApplicable(platz, result);
		if (doMonatsstueckelungen) {
			result = monatsRule.executeIfApplicable(platz, result);
		}
		result = mutationsMerger.executeIfApplicable(platz, result);
		result = abschlussNormalizerKeepMonate.executeIfApplicable(platz, result);
		BemerkungsMerger.prepareGeneratedBemerkungen(result);

		result.forEach(VerfuegungZeitabschnitt::initBGCalculationResult);
		return result;
	}

	public static Map<EinstellungKey, Einstellung> getEinstellungenConfiguratorAsiv(@Nonnull Gesuchsperiode gesuchsperiode) {
		EinstellungenMap einstellungenMap = new EinstellungenMap();

		einstellungenMap.addEinstellung(MAX_MASSGEBENDES_EINKOMMEN, EINSTELLUNG_MAX_EINKOMMEN, gesuchsperiode);
		einstellungenMap.addEinstellung(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3, PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3, gesuchsperiode);
		einstellungenMap.addEinstellung(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4, PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4, gesuchsperiode);
		einstellungenMap.addEinstellung(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5, PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5, gesuchsperiode);
		einstellungenMap.addEinstellung(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6, PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6, gesuchsperiode);
		einstellungenMap.addEinstellung(PARAM_MAX_TAGE_ABWESENHEIT, "30", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE, EinschulungTyp.KINDERGARTEN2.name(), gesuchsperiode);
		einstellungenMap.addEinstellung(MIN_ERWERBSPENSUM_EINGESCHULT, EINSTELLUNG_MIN_ERWERBSPENSUM_EINGESCHULT, gesuchsperiode);
		einstellungenMap.addEinstellung(MIN_ERWERBSPENSUM_NICHT_EINGESCHULT, EINSTELLUNG_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT, gesuchsperiode);
		// Gemaess ASIV: Wir nehmen eben den ASIV Wert!
		einstellungenMap.addEinstellung(GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT, EINSTELLUNG_MIN_ERWERBSPENSUM_EINGESCHULT, gesuchsperiode);
		// Gemaess ASIV: Wir nehmen eben den ASIV Wert!
		einstellungenMap.addEinstellung(GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT, EINSTELLUNG_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT, gesuchsperiode);
		einstellungenMap.addEinstellung(ERWERBSPENSUM_ZUSCHLAG, "20", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT, "0", gesuchsperiode);

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
		einstellungenMap.addEinstellung(MAX_VERGUENSTIGUNG_SCHULE_PRO_TG, "75", gesuchsperiode);
		einstellungenMap.addEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD, "11.90", gesuchsperiode);
		einstellungenMap.addEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD, "8.50", gesuchsperiode);
		einstellungenMap.addEinstellung(MAX_VERGUENSTIGUNG_SCHULE_PRO_STD, "8.50", gesuchsperiode);
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
		einstellungenMap.addEinstellung(GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB, DATE_FORMATTER.format(gesuchsperiode.getGueltigkeit().getGueltigAb()), gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB, DATE_FORMATTER.format(gesuchsperiode.getGueltigkeit().getGueltigAb()), gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG, DATE_FORMATTER.format(gesuchsperiode.getGueltigkeit().getGueltigAb()), gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_TAGESSCHULE_TAGIS_ENABLED, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA, "0.00", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO, "0.00", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA, EinschulungTyp.VORSCHULALTER.name(), gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO, EinschulungTyp.VORSCHULALTER.name(), gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA, "0.00", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO, "0.00", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_HAUPTMAHLZEIT, "0", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_NEBENMAHLZEIT, "0", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN, "50000", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_HAUPTMAHLZEIT, "0", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_NEBENMAHLZEIT, "0", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN, "70000", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_HAUPTMAHLZEIT, "0", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_NEBENMAHLZEIT, "0", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED, "false", gesuchsperiode);
		einstellungenMap.addEinstellung(GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED, "false", gesuchsperiode);

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
		einstellungenGemeinde.get(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA).setValue(EinschulungTyp.VORSCHULALTER.name());
		einstellungenGemeinde.get(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO).setValue(EinschulungTyp.VORSCHULALTER.name());
		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED).setValue("true");
		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_HAUPTMAHLZEIT).setValue("6");
		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_NEBENMAHLZEIT).setValue("3");
		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN).setValue("50000");
		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_HAUPTMAHLZEIT).setValue("3");
		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_NEBENMAHLZEIT).setValue("1");
		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN).setValue("70000");
		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_HAUPTMAHLZEIT).setValue("0");
		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_NEBENMAHLZEIT).setValue("0");
		einstellungenGemeinde.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED).setValue("true");
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
		Map<EinstellungKey, Einstellung> einstellungen = EbeguRuleTestsHelper.getEinstellungenConfiguratorAsiv(gesuchsperiode);
		// ... und fuer Rechner
		einstellungen.putAll(EbeguRuleTestsHelper.getEinstellungeRechnerAsiv(gesuchsperiode));
		// ... und fuer die Rules
		einstellungen.putAll(EbeguRuleTestsHelper.getEinstellungenRulesAsiv(gesuchsperiode));
		return einstellungen;
	}

	public static List<VerfuegungZeitabschnitt> initializeRestanspruchForNextBetreuung(Betreuung currentBetreuung, List<VerfuegungZeitabschnitt> zeitabschnitte) {
		return restanspruchInitializer.executeIfApplicable(currentBetreuung, zeitabschnitte);
	}

	public static Betreuung createBetreuungWithPensum(
		LocalDate von, LocalDate bis,
		BetreuungsangebotTyp angebot,
		int pensum,
		BigDecimal monatlicheBetreuungskosten
	) {
		return createBetreuungWithPensum(von, bis, angebot, pensum, monatlicheBetreuungskosten, 0, 0);
	}

	public static Betreuung createBetreuungWithPensum(
		LocalDate von, LocalDate bis,
		BetreuungsangebotTyp angebot,
		int pensum,
		BigDecimal monatlicheBetreuungskosten,
		Integer monatlicheHauptmahlzeiten,
		Integer monatlicheNebenmahlzeiten
	) {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
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
