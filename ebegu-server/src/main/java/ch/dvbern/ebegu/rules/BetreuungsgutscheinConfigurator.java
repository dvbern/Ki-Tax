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
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.enums.DemoFeatureTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.KitaxUtil;
import ch.dvbern.ebegu.util.RuleParameterUtil;

import static ch.dvbern.ebegu.enums.EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM;
import static ch.dvbern.ebegu.enums.EinstellungKey.ANSPRUCH_AB_X_MONATEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.ANSPRUCH_MONATSWEISE;
import static ch.dvbern.ebegu.enums.EinstellungKey.AUSSERORDENTLICHER_ANSPRUCH_RULE;
import static ch.dvbern.ebegu.enums.EinstellungKey.DAUER_BABYTARIF;
import static ch.dvbern.ebegu.enums.EinstellungKey.EINGEWOEHNUNG_TYP;
import static ch.dvbern.ebegu.enums.EinstellungKey.ERWERBSPENSUM_ZUSCHLAG;
import static ch.dvbern.ebegu.enums.EinstellungKey.FACHSTELLEN_TYP;
import static ch.dvbern.ebegu.enums.EinstellungKey.FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF;
import static ch.dvbern.ebegu.enums.EinstellungKey.FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM;
import static ch.dvbern.ebegu.enums.EinstellungKey.FKJV_PAUSCHALE_BEI_ANSPRUCH;
import static ch.dvbern.ebegu.enums.EinstellungKey.FKJV_PAUSCHALE_RUECKWIRKEND;
import static ch.dvbern.ebegu.enums.EinstellungKey.FKJV_TEXTE;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GESCHWISTERNBONUS_AKTIVIERT;
import static ch.dvbern.ebegu.enums.EinstellungKey.KINDERABZUG_TYP;
import static ch.dvbern.ebegu.enums.EinstellungKey.KITAPLUS_ZUSCHLAG_AKTIVIERT;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_MASSGEBENDES_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.MINIMALDAUER_KONKUBINAT;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_ERWERBSPENSUM_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_ERWERBSPENSUM_NICHT_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_MAX_TAGE_ABWESENHEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6;

/**
 * Configurator, welcher die Regeln und ihre Reihenfolge konfiguriert. Als Parameter erhält er den Mandanten sowie
 * die benötigten Ebegu-Parameter
 */
public class BetreuungsgutscheinConfigurator {

	private final DateRange defaultGueltigkeit = Constants.DEFAULT_GUELTIGKEIT;

	private final List<Rule> rules = new LinkedList<>();

	private Locale locale;

	@Nonnull
	public List<Rule> configureRulesForMandant(
			@Nonnull Gemeinde gemeinde,
			@Nonnull RuleParameterUtil ruleParameterUtil
	) {
		this.locale = ruleParameterUtil.getLocale();
		useRulesOfGemeinde(gemeinde, ruleParameterUtil);
		return rules;
	}

	public Set<EinstellungKey> getRequiredParametersForGemeinde() {
		return EnumSet.of(
				MAX_MASSGEBENDES_EINKOMMEN,
				PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,
				PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,
				PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,
				PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,
				PARAM_MAX_TAGE_ABWESENHEIT,
				GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE,
				MIN_ERWERBSPENSUM_EINGESCHULT,
				MIN_ERWERBSPENSUM_NICHT_EINGESCHULT,
				GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT,
				GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT,
				ERWERBSPENSUM_ZUSCHLAG,
				GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED,
				GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT,
				GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED,
				GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED,
				GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN,
				GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN,
				GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT,
				GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT,
				GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT,
				GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT,
				FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM,
				FKJV_PAUSCHALE_BEI_ANSPRUCH,
				FKJV_PAUSCHALE_RUECKWIRKEND,
				FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF,
				EINGEWOEHNUNG_TYP,
				ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
				MINIMALDAUER_KONKUBINAT,
				ANSPRUCH_MONATSWEISE,
				KITAPLUS_ZUSCHLAG_AKTIVIERT,
				GESCHWISTERNBONUS_AKTIVIERT,
				AUSSERORDENTLICHER_ANSPRUCH_RULE,
				DAUER_BABYTARIF,
				KINDERABZUG_TYP,
				FKJV_TEXTE,
				FACHSTELLEN_TYP,
				GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER,
				ANSPRUCH_AB_X_MONATEN
		);
	}

	private void useRulesOfGemeinde(@Nonnull Gemeinde gemeinde, @Nonnull RuleParameterUtil ruleParameterUtil) {
		this.rules.clear();
		abschnitteErstellenRegeln(gemeinde, ruleParameterUtil);
		berechnenAnspruchRegeln(gemeinde, ruleParameterUtil);
		reduktionsRegeln(ruleParameterUtil);
	}

	@SuppressWarnings({ "checkstyle:LocalVariableName", "PMD.NcssMethodCount" })
	private void abschnitteErstellenRegeln(@Nonnull Gemeinde gemeinde, @Nonnull RuleParameterUtil ruleParameterUtil) {
		// GRUNDREGELN_DATA: Abschnitte erstellen

		// - Erwerbspensum ASIV: Erstellt die grundlegenden Zeitschnitze (keine Korrekturen, nur einfügen)
		Einstellung zuschlagEWP = ruleParameterUtil.getEinstellung(ERWERBSPENSUM_ZUSCHLAG);
		ErwerbspensumAsivAbschnittRule erwerbspensumAsivAbschnittRule =
				new ErwerbspensumAsivAbschnittRule(defaultGueltigkeit, zuschlagEWP.getValueAsInteger(), locale);
		addToRuleSetIfRelevantForGemeinde(erwerbspensumAsivAbschnittRule, ruleParameterUtil);

		// - Erwerbspensum: Erweiterung fuer Gemeinden
		Einstellung param_MaxAbzugFreiwilligenarbeit =
				ruleParameterUtil.getEinstellung(GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT);
		KitaxUebergangsloesungParameter kitaxParameterDTO = ruleParameterUtil.getKitaxUebergangsloesungParameter();
		if (kitaxParameterDTO != null && KitaxUtil.isGemeindeWithKitaxUebergangsloesung(gemeinde)) {
			// Fuer die Stadt Bern gibt es die Rule mit verschiedenen Parameter: Vor dem Stichtag und nach dem Stichtag
			// Regel 1: Gemaess FEBR bis vor dem Stichtag: Der Maximalwert ist 0
			DateRange vorStichtag = new DateRange(
					defaultGueltigkeit.getGueltigAb(),
					kitaxParameterDTO.getStadtBernAsivStartDate().minusDays(1));
			ErwerbspensumGemeindeAbschnittRule ewpBernAbschnittRuleVorStichtag =
					new ErwerbspensumGemeindeAbschnittRule(
					vorStichtag, 0, 0, locale);
			addToRuleSetIfRelevantForGemeinde(ewpBernAbschnittRuleVorStichtag, ruleParameterUtil);
			// Nach dem Stichtag gilt die Regel gemaess Konfiguration
			DateRange nachStichtag =
					new DateRange(kitaxParameterDTO.getStadtBernAsivStartDate(), defaultGueltigkeit.getGueltigBis());
			ErwerbspensumGemeindeAbschnittRule ewpBernAbschnittRuleNachStichtag =
					new ErwerbspensumGemeindeAbschnittRule(
							nachStichtag,
							zuschlagEWP.getValueAsInteger(),
							param_MaxAbzugFreiwilligenarbeit.getValueAsInteger(),
							locale);
			addToRuleSetIfRelevantForGemeinde(ewpBernAbschnittRuleNachStichtag, ruleParameterUtil);
		} else {
			// Fuer alle anderen Gemeinden gibt es nur *eine* Rule
			ErwerbspensumGemeindeAbschnittRule erwerbspensumGmdeAbschnittRule = new ErwerbspensumGemeindeAbschnittRule(
					defaultGueltigkeit,
					zuschlagEWP.getValueAsInteger(),
					param_MaxAbzugFreiwilligenarbeit.getValueAsInteger(),
					locale);
			addToRuleSetIfRelevantForGemeinde(erwerbspensumGmdeAbschnittRule, ruleParameterUtil);
		}

		// - Unbezahlter Urlaub
		UnbezahlterUrlaubAbschnittRule unbezahlterUrlaubAbschnittRule =
				new UnbezahlterUrlaubAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(unbezahlterUrlaubAbschnittRule, ruleParameterUtil);

		//Familenabzug: Berechnet den Familienabzug aufgrund der Familiengroesse
		Einstellung param_pauschalabzug_pro_person_familiengroesse_3 =
				ruleParameterUtil.getEinstellung(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3);
		Einstellung param_pauschalabzug_pro_person_familiengroesse_4 =
				ruleParameterUtil.getEinstellung(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4);
		Einstellung param_pauschalabzug_pro_person_familiengroesse_5 =
				ruleParameterUtil.getEinstellung(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5);
		Einstellung param_pauschalabzug_pro_person_familiengroesse_6 =
				ruleParameterUtil.getEinstellung(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6);
		Einstellung param_minimaldauer_konkubinat = ruleParameterUtil.getEinstellung(MINIMALDAUER_KONKUBINAT);
		Einstellung param_kinderabzug_typ = ruleParameterUtil.getEinstellung(KINDERABZUG_TYP);

		FamilienabzugAbschnittRule familienabzugAbschnittRule = new FamilienabzugAbschnittRule(
				defaultGueltigkeit,
				param_pauschalabzug_pro_person_familiengroesse_3.getValueAsBigDecimal(),
				param_pauschalabzug_pro_person_familiengroesse_4.getValueAsBigDecimal(),
				param_pauschalabzug_pro_person_familiengroesse_5.getValueAsBigDecimal(),
				param_pauschalabzug_pro_person_familiengroesse_6.getValueAsBigDecimal(),
				param_minimaldauer_konkubinat.getValueAsInteger(),
				KinderabzugTyp.valueOf(param_kinderabzug_typ.getValue()),
				locale);
		addToRuleSetIfRelevantForGemeinde(familienabzugAbschnittRule, ruleParameterUtil);

		// Betreuungsgutscheine Gueltigkeit
		GutscheineStartdatumAbschnittRule gutscheineStartdatumAbschnittRule =
				new GutscheineStartdatumAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(gutscheineStartdatumAbschnittRule, ruleParameterUtil);

		// - KindTarif
		Einstellung param_dauerBabyTarif = ruleParameterUtil.getEinstellung(DAUER_BABYTARIF);
		KindTarifAbschnittRule kindTarifAbschnittRule =
				new KindTarifAbschnittRule(defaultGueltigkeit, locale, param_dauerBabyTarif.getValueAsInteger());
		addToRuleSetIfRelevantForGemeinde(kindTarifAbschnittRule, ruleParameterUtil);

		// Betreuungsangebot
		BetreuungsangebotTypAbschnittRule betreuungsangebotTypAbschnittRule =
				new BetreuungsangebotTypAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(betreuungsangebotTypAbschnittRule, ruleParameterUtil);

		// - Betreuungspensum
		BetreuungspensumAbschnittRule betreuungspensumAbschnittRule =
				new BetreuungspensumAbschnittRule(defaultGueltigkeit, locale, kitaxParameterDTO);
		addToRuleSetIfRelevantForGemeinde(betreuungspensumAbschnittRule, ruleParameterUtil);

		// - Pensum Tagesschule
		TagesschuleBetreuungszeitAbschnittRule tagesschuleAbschnittRule =
				new TagesschuleBetreuungszeitAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(tagesschuleAbschnittRule, ruleParameterUtil);

		// - Fachstelle
		FachstelleAbschnittRule fachstelleAbschnittRule = new FachstelleAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(fachstelleAbschnittRule, ruleParameterUtil);

		// - GeschwisterBonus
		Einstellung einstellungBgAusstellenBisStufe =
				ruleParameterUtil.getEinstellung(GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE);
		EinschulungTyp bgAusstellenBisUndMitStufe = EinschulungTyp.valueOf(einstellungBgAusstellenBisStufe.getValue());
		GeschwisterbonusAbschnittRule geschwisterbonusAbschnittRule =
				new GeschwisterbonusAbschnittRule(bgAusstellenBisUndMitStufe, defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(geschwisterbonusAbschnittRule, ruleParameterUtil);

		// - Ausserordentlicher Anspruch
		AusserordentlicherAnspruchAbschnittRule ausserordntl =
				new AusserordentlicherAnspruchAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(ausserordntl, ruleParameterUtil);

		// - Einkommen / Einkommensverschlechterung / Maximales Einkommen
		EinkommenAbschnittRule einkommenAbschnittRule = new EinkommenAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(einkommenAbschnittRule, ruleParameterUtil);

		// Wohnsitz (Zuzug und Wegzug)
		WohnsitzAbschnittRule wohnsitzAbschnittRule = new WohnsitzAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(wohnsitzAbschnittRule, ruleParameterUtil);

		// - Einreichungsfrist
		EinreichungsfristAbschnittRule einreichungsfristAbschnittRule =
				new EinreichungsfristAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(einreichungsfristAbschnittRule, ruleParameterUtil);

		// Abwesenheit
		Einstellung abwesenheitMaxDaysParam = ruleParameterUtil.getEinstellung(PARAM_MAX_TAGE_ABWESENHEIT);
		Integer abwesenheitMaxDaysValue = abwesenheitMaxDaysParam.getValueAsInteger();
		AbwesenheitAbschnittRule abwesenheitAbschnittRule =
				new AbwesenheitAbschnittRule(defaultGueltigkeit, abwesenheitMaxDaysValue, locale);
		addToRuleSetIfRelevantForGemeinde(abwesenheitAbschnittRule, ruleParameterUtil);

		// Zivilstandsaenderung
		ZivilstandsaenderungAbschnittRule zivilstandsaenderungAbschnittRule = new ZivilstandsaenderungAbschnittRule(
				defaultGueltigkeit,
				param_minimaldauer_konkubinat.getValueAsInteger(),
				locale);
		addToRuleSetIfRelevantForGemeinde(zivilstandsaenderungAbschnittRule, ruleParameterUtil);

		// Sozialhilfe
		SozialhilfeAbschnittRule sozialhilfeAbschnittRule = new SozialhilfeAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(sozialhilfeAbschnittRule, ruleParameterUtil);

		// FamiliensituationBeendet
		FamiliensituationBeendetAbschnittRule familiensituationBeendetAbschnittRule =
				new FamiliensituationBeendetAbschnittRule(
					defaultGueltigkeit,
					locale,
					ruleParameterUtil.isDemoFeatureActivated(DemoFeatureTyp.GESUCH_BEENDEN_FAMSIT));
		addToRuleSetIfRelevantForGemeinde(familiensituationBeendetAbschnittRule, ruleParameterUtil);

		AnspruchAbAlterAbschnittRule
				anspruchAbAlterAbschnittRule = new AnspruchAbAlterAbschnittRule(defaultGueltigkeit, locale,
				ruleParameterUtil.getEinstellung(EinstellungKey.ANSPRUCH_AB_X_MONATEN).getValueAsInteger());
		addToRuleSetIfRelevantForGemeinde(anspruchAbAlterAbschnittRule, ruleParameterUtil);
	}

	private void berechnenAnspruchRegeln(@Nonnull Gemeinde gemeinde, @Nonnull RuleParameterUtil ruleParameterUtil) {
		// GRUNDREGELN_CALC: Berechnen / Ändern den Anspruch

		// - Storniert
		StorniertCalcRule storniertCalcRule = new StorniertCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(storniertCalcRule, ruleParameterUtil);

		// - Erwerbspensum Kanton
		Rule rule = new ErwerbspensumCalcRuleVisitor(
				ruleParameterUtil.getEinstellungen(),
				locale).getErwerbspesumCalcRule();
		addToRuleSetIfRelevantForGemeinde(rule, ruleParameterUtil);

		// - KESB Platzierung: Max-Tarif bei Tagesschulen
		KesbPlatzierungTSCalcRule kesbPlatzierungTSCalcRule = new KesbPlatzierungTSCalcRule(defaultGueltigkeit,
				locale);
		addToRuleSetIfRelevantForGemeinde(kesbPlatzierungTSCalcRule, ruleParameterUtil);

		// - Erwerbspensum Gemeinde
		Einstellung minEWP_nichtEingeschultGmde =
				ruleParameterUtil.getEinstellung(GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT);
		Einstellung minEWP_eingeschultGmde = ruleParameterUtil.getEinstellung(GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT);
		Einstellung paramMinDauerKonkubinat = ruleParameterUtil.getEinstellung(MINIMALDAUER_KONKUBINAT);
		KitaxUebergangsloesungParameter kitaxParameterDTO = ruleParameterUtil.getKitaxUebergangsloesungParameter();
		// Im Fall von BERN die Gueltigkeit einfach erst ab Tag X setzen?
		if (kitaxParameterDTO != null && KitaxUtil.isGemeindeWithKitaxUebergangsloesung(gemeinde)) {
			// Fuer die Stadt Bern gibt es die Rule mit verschiedenen Parameter: Vor dem Stichtag und nach dem Stichtag
			// Regel 1: Gemaess FEBR bis vor dem Stichtag
			DateRange vorStichtag = new DateRange(
					defaultGueltigkeit.getGueltigAb(),
					kitaxParameterDTO.getStadtBernAsivStartDate().minusDays(1));
			ErwerbspensumGemeindeCalcRule ewpBernCalcRuleVorStichtag = new ErwerbspensumGemeindeCalcRule(
					vorStichtag,
					kitaxParameterDTO.getMinEWP(),
					kitaxParameterDTO.getMinEWP(),
					paramMinDauerKonkubinat.getValueAsInteger(),
					locale);
			// Wir muessen die Regel hier manuell hinzufuegen, da wir nicht die ueblichen Einstellungen verwenden!
			// Sonst wird sie bei der Pruefung isRelevantForGemeinde wieder entfernt
			rules.add(ewpBernCalcRuleVorStichtag);
			// Regel 2: Gemaess ASIV ab dem Stichtag
			DateRange nachStichtag =
					new DateRange(kitaxParameterDTO.getStadtBernAsivStartDate(), defaultGueltigkeit.getGueltigBis());
			ErwerbspensumGemeindeCalcRule ewpBernCalcRuleNachStichtag = new ErwerbspensumGemeindeCalcRule(
					nachStichtag,
					minEWP_nichtEingeschultGmde.getValueAsInteger(),
					minEWP_eingeschultGmde.getValueAsInteger(),
					paramMinDauerKonkubinat.getValueAsInteger(),
					locale);
			addToRuleSetIfRelevantForGemeinde(ewpBernCalcRuleNachStichtag, ruleParameterUtil);
		} else {
			// Fuer alle anderen Gemeinden gibt es nur *eine* Rule
			ErwerbspensumGemeindeCalcRule erwerbspensumGemeindeCalcRule = new ErwerbspensumGemeindeCalcRule(
					defaultGueltigkeit,
					minEWP_nichtEingeschultGmde.getValueAsInteger(),
					minEWP_eingeschultGmde.getValueAsInteger(),
					paramMinDauerKonkubinat.getValueAsInteger(),
					locale);
			addToRuleSetIfRelevantForGemeinde(erwerbspensumGemeindeCalcRule, ruleParameterUtil);
		}

		// - Fachstelle: Muss zwingend nach Erwerbspensum und Betreuungspensum durchgefuehrt werden
		FachstelleBernCalcRule fachstelleBernCalcRule = new FachstelleBernCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(fachstelleBernCalcRule, ruleParameterUtil);
		FachstelleLuzernCalcRule fachstelleLuzrnCalcRule = new FachstelleLuzernCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(fachstelleLuzrnCalcRule, ruleParameterUtil);

		KitaPlusZuschlagCalcRule kitaPlusZuschlagCalcRule = new KitaPlusZuschlagCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(kitaPlusZuschlagCalcRule, ruleParameterUtil);

		// - Ausserordentlicher Anspruch: Muss am Schluss gemacht werden, da er alle anderen Regeln überschreiben kann.
		// Wir haben je eine Anspruch-Regel für ASIV und FKJV, die entsprechend der Einstellungen aktiv sind
		Einstellung minErwerbspensumNichtEingeschult =
				getAusserordentlicherAnspruchMinErwerbspensumNichtEingeschult(ruleParameterUtil.getEinstellungen());
		Einstellung minErwerbspensumEingeschult =
				getAusserordentlicherAnspruchMinErwerbspensumEingeschult(ruleParameterUtil.getEinstellungen());
		Einstellung paramMaxDifferenzBeschaeftigungspensum =
				ruleParameterUtil.getEinstellung(FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM);
		AusserordentlicherAnspruchCalcRule ausserordntlAsiv =
				new AusserordentlicherAnspruchCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(ausserordntlAsiv, ruleParameterUtil);
		FKJVAusserordentlicherAnspruchCalcRule ausserordntlFkjv = new FKJVAusserordentlicherAnspruchCalcRule(
				minErwerbspensumNichtEingeschult.getValueAsInteger(),
				minErwerbspensumEingeschult.getValueAsInteger(),
				paramMaxDifferenzBeschaeftigungspensum.getValueAsInteger(),
				defaultGueltigkeit,
				locale);
		addToRuleSetIfRelevantForGemeinde(ausserordntlFkjv, ruleParameterUtil);
	}

	private Einstellung getAusserordentlicherAnspruchMinErwerbspensumNichtEingeschult(Map<EinstellungKey, Einstellung> einstellungMap) {
		Einstellung mandant = einstellungMap.get(MIN_ERWERBSPENSUM_NICHT_EINGESCHULT);
		Einstellung gemeinde = einstellungMap.get(GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT);

		if (gemeinde.getValueAsInteger() != null) {
			return gemeinde;
		}
		Objects.requireNonNull(mandant, "Parameter MIN_ERWERBSPENSUM_NICHT_EINGESCHULT muss gesetzt sein");
		return mandant;
	}

	private Einstellung getAusserordentlicherAnspruchMinErwerbspensumEingeschult(Map<EinstellungKey, Einstellung> einstellungMap) {
		Einstellung mandant = einstellungMap.get(MIN_ERWERBSPENSUM_EINGESCHULT);
		Einstellung gemeinde = einstellungMap.get(GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT);

		if (gemeinde.getValueAsInteger() != null) {
			return gemeinde;
		}
		Objects.requireNonNull(mandant, "Parameter GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT muss gesetzt sein");
		return mandant;
	}

	private void reduktionsRegeln(@Nonnull RuleParameterUtil ruleParameterUtil) {
		// REDUKTIONSREGELN: Setzen Anpsruch auf 0

		// BETREUUNGS GUTSCHEINE START DATUM - Anspruch verfällt, wenn Gutscheine vor dem
		// BetreuungsgutscheineStartdatum
		// der Gemeinde liegen
		GutscheineStartdatumCalcRule gutscheineStartdatumCalcRule =
				new GutscheineStartdatumCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(gutscheineStartdatumCalcRule, ruleParameterUtil);

		// - Einkommen / Einkommensverschlechterung / Maximales Einkommen
		Einstellung paramMassgebendesEinkommenMax = ruleParameterUtil.getEinstellung(MAX_MASSGEBENDES_EINKOMMEN);
		Einstellung paramMaxEinkommenEKVEinstellung =
				ruleParameterUtil.getEinstellung(FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF);
		BigDecimal paramMaxEinkommenEKV = null;
		try {
			paramMaxEinkommenEKV = paramMaxEinkommenEKVEinstellung.getValueAsBigDecimal();
		} catch (NumberFormatException e) {
			// if NumberFormatException, param is not set in configuration and rule is not active
		}
		Einstellung paramPauschalBeiAnspruch = ruleParameterUtil.getEinstellung(FKJV_PAUSCHALE_BEI_ANSPRUCH);
		EinkommenCalcRule maxEinkommenCalcRule = new EinkommenCalcRule(
				defaultGueltigkeit,
				paramMassgebendesEinkommenMax.getValueAsBigDecimal(),
				paramMaxEinkommenEKV,
				paramPauschalBeiAnspruch.getValueAsBoolean(),
				locale);
		addToRuleSetIfRelevantForGemeinde(maxEinkommenCalcRule, ruleParameterUtil);

		// Betreuungsangebot Tagesschule nicht berechnen
		BetreuungsangebotTypCalcRule betreuungsangebotTypCalcRule =
				new BetreuungsangebotTypCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(betreuungsangebotTypCalcRule, ruleParameterUtil);

		// Wohnsitz (Zuzug und Wegzug)
		WohnsitzCalcRule wohnsitzCalcRule = new WohnsitzCalcRule(
				defaultGueltigkeit,
				locale);
		addToRuleSetIfRelevantForGemeinde(wohnsitzCalcRule, ruleParameterUtil);

		// Einreichungsfrist
		EinreichungsfristCalcRule einreichungsfristRule = new EinreichungsfristCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(einreichungsfristRule, ruleParameterUtil);

		// Abwesenheit
		Einstellung abwesenheitMaxDaysParam = ruleParameterUtil.getEinstellung(PARAM_MAX_TAGE_ABWESENHEIT);
		Integer abwesenheitMaxDaysValue = abwesenheitMaxDaysParam.getValueAsInteger();
		AbwesenheitCalcRule abwesenheitCalcRule =
				new AbwesenheitCalcRule(defaultGueltigkeit, locale, abwesenheitMaxDaysValue);
		addToRuleSetIfRelevantForGemeinde(abwesenheitCalcRule, ruleParameterUtil);

		// - Schulstufe des Kindes: Je nach Gemeindeeinstellung wird bis zu einer gewissen STufe ein Gutschein
		// ausgestellt
		Einstellung einstellungBgAusstellenBisStufe =
				ruleParameterUtil.getEinstellung(GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE);
		EinschulungTyp bgAusstellenBisUndMitStufe = EinschulungTyp.valueOf(einstellungBgAusstellenBisStufe.getValue());
		SchulstufeCalcRule schulstufeCalcRule =
				new SchulstufeCalcRule(defaultGueltigkeit, bgAusstellenBisUndMitStufe, locale);
		addToRuleSetIfRelevantForGemeinde(schulstufeCalcRule, ruleParameterUtil);

		// - KESB Platzierung: Kein Anspruch, da die KESB den Platz bezahlt
		KesbPlatzierungCalcRule kesbPlatzierungCalcRule = new KesbPlatzierungCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(kesbPlatzierungCalcRule, ruleParameterUtil);

		// Sozialhilfeempfänger erhalten keinen Anspruch, wenn entsprechend konfiguriert
		SozialhilfeKeinAnspruchCalcRule
				sozialhilfeCalcRule = new SozialhilfeKeinAnspruchCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(sozialhilfeCalcRule, ruleParameterUtil);

		// Kinder erhalten erst ab einem gewissen Altern einen Anspruch, wenn entsprechend konfiguriert
		AnspruchAbAlterCalcRule
				anspruchAbAlterCalcRule = new AnspruchAbAlterCalcRule(defaultGueltigkeit, locale,
				ruleParameterUtil.getEinstellung(EinstellungKey.ANSPRUCH_AB_X_MONATEN).getValueAsInteger());
		addToRuleSetIfRelevantForGemeinde(anspruchAbAlterCalcRule, ruleParameterUtil);

		//RESTANSPRUCH REDUKTION limitiert Anspruch auf  minimum(anspruchRest, anspruchPensum)
		RestanspruchLimitCalcRule restanspruchLimitCalcRule = new RestanspruchLimitCalcRule(defaultGueltigkeit,
				locale);
		addToRuleSetIfRelevantForGemeinde(restanspruchLimitCalcRule, ruleParameterUtil);

		// Verfuegungsbemerkung
		VerfuegungsBemerkungCalcRule bemerkungCalcRule = new VerfuegungsBemerkungCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(bemerkungCalcRule, ruleParameterUtil);

		FamiliensituationBeendetCalcRule familiensituationBeendetCalcRule =
				new FamiliensituationBeendetCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(familiensituationBeendetCalcRule, ruleParameterUtil);
	}

	private void addToRuleSetIfRelevantForGemeinde(@Nonnull Rule rule, @Nonnull RuleParameterUtil ruleParameterUtil) {
		if (rule.isRelevantForGemeinde(ruleParameterUtil.getEinstellungen())) {
			rules.add(rule);
		}
	}
}
