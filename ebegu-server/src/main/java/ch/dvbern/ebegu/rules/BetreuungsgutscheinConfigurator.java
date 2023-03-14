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
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.KitaxUtil;
import ch.dvbern.ebegu.util.RuleParameterUtil;

import static ch.dvbern.ebegu.enums.EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM;
import static ch.dvbern.ebegu.enums.EinstellungKey.AUSSERORDENTLICHER_ANSPRUCH_RULE;
import static ch.dvbern.ebegu.enums.EinstellungKey.DAUER_BABYTARIF;
import static ch.dvbern.ebegu.enums.EinstellungKey.ERWERBSPENSUM_ZUSCHLAG;
import static ch.dvbern.ebegu.enums.EinstellungKey.FACHSTELLEN_TYP;
import static ch.dvbern.ebegu.enums.EinstellungKey.ANSPRUCH_MONATSWEISE;
import static ch.dvbern.ebegu.enums.EinstellungKey.FKJV_EINGEWOEHNUNG;
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
		@Nonnull RuleParameterUtil ruleParameterUtil,
		@Nonnull Locale inputLocale
	) {
		this.locale = inputLocale;
		useRulesOfGemeinde(gemeinde, ruleParameterUtil.getKitaxUebergangsloesungParameter(), ruleParameterUtil.getEinstellungen());
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
			FKJV_EINGEWOEHNUNG,
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
			GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER
		);
	}

	private void useRulesOfGemeinde(@Nonnull Gemeinde gemeinde, @Nullable KitaxUebergangsloesungParameter kitaxParameterDTO, @Nonnull Map<EinstellungKey, Einstellung> einstellungen) {
		this.rules.clear();
		abschnitteErstellenRegeln(gemeinde, kitaxParameterDTO, einstellungen);
		berechnenAnspruchRegeln(gemeinde, kitaxParameterDTO, einstellungen);
		reduktionsRegeln(einstellungen);
	}

	@SuppressWarnings({"checkstyle:LocalVariableName", "PMD.NcssMethodCount"})
	private void abschnitteErstellenRegeln(
		@Nonnull Gemeinde gemeinde,
		@Nullable KitaxUebergangsloesungParameter kitaxParameterDTO,
		@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		// GRUNDREGELN_DATA: Abschnitte erstellen

		// - Erwerbspensum ASIV: Erstellt die grundlegenden Zeitschnitze (keine Korrekturen, nur einfügen)
		Einstellung zuschlagEWP = einstellungMap.get(ERWERBSPENSUM_ZUSCHLAG);
		Objects.requireNonNull(zuschlagEWP, "Parameter ERWERBSPENSUM_ZUSCHLAG muss gesetzt sein");
		ErwerbspensumAsivAbschnittRule erwerbspensumAsivAbschnittRule = new ErwerbspensumAsivAbschnittRule(defaultGueltigkeit, zuschlagEWP.getValueAsInteger(), locale);
		addToRuleSetIfRelevantForGemeinde(erwerbspensumAsivAbschnittRule, einstellungMap);

		// - Erwerbspensum: Erweiterung fuer Gemeinden
		Einstellung param_MaxAbzugFreiwilligenarbeit = einstellungMap.get(EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT);
		Objects.requireNonNull(param_MaxAbzugFreiwilligenarbeit, "Parameter GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT muss gesetzt sein");
		if (kitaxParameterDTO != null && KitaxUtil.isGemeindeWithKitaxUebergangsloesung(gemeinde)) {
			// Fuer die Stadt Bern gibt es die Rule mit verschiedenen Parameter: Vor dem Stichtag und nach dem Stichtag
			// Regel 1: Gemaess FEBR bis vor dem Stichtag: Der Maximalwert ist 0
			DateRange vorStichtag = new DateRange(defaultGueltigkeit.getGueltigAb(), kitaxParameterDTO.getStadtBernAsivStartDate().minusDays(1));
			ErwerbspensumGemeindeAbschnittRule ewpBernAbschnittRuleVorStichtag = new ErwerbspensumGemeindeAbschnittRule(
				vorStichtag, 0, 0, locale);
			addToRuleSetIfRelevantForGemeinde(ewpBernAbschnittRuleVorStichtag, einstellungMap);
			// Nach dem Stichtag gilt die Regel gemaess Konfiguration
			DateRange nachStichtag = new DateRange(kitaxParameterDTO.getStadtBernAsivStartDate(), defaultGueltigkeit.getGueltigBis());
			ErwerbspensumGemeindeAbschnittRule ewpBernAbschnittRuleNachStichtag = new ErwerbspensumGemeindeAbschnittRule(
				nachStichtag, zuschlagEWP.getValueAsInteger(), param_MaxAbzugFreiwilligenarbeit.getValueAsInteger(), locale);
			addToRuleSetIfRelevantForGemeinde(ewpBernAbschnittRuleNachStichtag, einstellungMap);
		} else {
			// Fuer alle anderen Gemeinden gibt es nur *eine* Rule
			ErwerbspensumGemeindeAbschnittRule erwerbspensumGmdeAbschnittRule = new ErwerbspensumGemeindeAbschnittRule(
				defaultGueltigkeit, zuschlagEWP.getValueAsInteger(), param_MaxAbzugFreiwilligenarbeit.getValueAsInteger(), locale);
			addToRuleSetIfRelevantForGemeinde(erwerbspensumGmdeAbschnittRule, einstellungMap);
		}

		// - Unbezahlter Urlaub
		UnbezahlterUrlaubAbschnittRule unbezahlterUrlaubAbschnittRule = new UnbezahlterUrlaubAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(unbezahlterUrlaubAbschnittRule, einstellungMap);

		//Familenabzug: Berechnet den Familienabzug aufgrund der Familiengroesse
		Einstellung param_pauschalabzug_pro_person_familiengroesse_3 = einstellungMap.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3);
		Objects.requireNonNull(param_pauschalabzug_pro_person_familiengroesse_3, "Parameter PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3 muss gesetzt sein");
		Einstellung param_pauschalabzug_pro_person_familiengroesse_4 = einstellungMap.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4);
		Objects.requireNonNull(param_pauschalabzug_pro_person_familiengroesse_4, "Parameter PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4 muss gesetzt sein");
		Einstellung param_pauschalabzug_pro_person_familiengroesse_5 = einstellungMap.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5);
		Objects.requireNonNull(param_pauschalabzug_pro_person_familiengroesse_5, "Parameter PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5 muss gesetzt sein");
		Einstellung param_pauschalabzug_pro_person_familiengroesse_6 = einstellungMap.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6);
		Objects.requireNonNull(param_pauschalabzug_pro_person_familiengroesse_6, "Parameter PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6 muss gesetzt sein");
		Einstellung param_minimaldauer_konkubinat = einstellungMap.get(MINIMALDAUER_KONKUBINAT);
		Objects.requireNonNull(param_minimaldauer_konkubinat, "Parameter MINIMALDAUER_KONKUBINAT muss gesetzt sein");
		Einstellung param_kinderabzug_typ = einstellungMap.get(KINDERABZUG_TYP);
		Objects.requireNonNull(param_minimaldauer_konkubinat, "Parameter KINDERABZUG_TYP muss gesetzt sein");

		FamilienabzugAbschnittRule familienabzugAbschnittRule = new FamilienabzugAbschnittRule(defaultGueltigkeit,
			param_pauschalabzug_pro_person_familiengroesse_3.getValueAsBigDecimal(),
			param_pauschalabzug_pro_person_familiengroesse_4.getValueAsBigDecimal(),
			param_pauschalabzug_pro_person_familiengroesse_5.getValueAsBigDecimal(),
			param_pauschalabzug_pro_person_familiengroesse_6.getValueAsBigDecimal(),
			param_minimaldauer_konkubinat.getValueAsInteger(),
			KinderabzugTyp.valueOf(param_kinderabzug_typ.getValue()),
			locale);
		addToRuleSetIfRelevantForGemeinde(familienabzugAbschnittRule, einstellungMap);

		// Betreuungsgutscheine Gueltigkeit
		GutscheineStartdatumAbschnittRule gutscheineStartdatumAbschnittRule = new GutscheineStartdatumAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(gutscheineStartdatumAbschnittRule, einstellungMap);

		// - KindTarif
		Einstellung param_dauerBabyTarif = einstellungMap.get(DAUER_BABYTARIF);
		Objects.requireNonNull(param_dauerBabyTarif, "Parameter DAUER_BABYTARIF muss gesetzt sein");
		KindTarifAbschnittRule kindTarifAbschnittRule = new KindTarifAbschnittRule(defaultGueltigkeit, locale, param_dauerBabyTarif.getValueAsInteger());
		addToRuleSetIfRelevantForGemeinde(kindTarifAbschnittRule, einstellungMap);

		// Betreuungsangebot
		BetreuungsangebotTypAbschnittRule betreuungsangebotTypAbschnittRule = new BetreuungsangebotTypAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(betreuungsangebotTypAbschnittRule, einstellungMap);

		// - Betreuungspensum
		BetreuungspensumAbschnittRule betreuungspensumAbschnittRule =
			new BetreuungspensumAbschnittRule(defaultGueltigkeit, locale, kitaxParameterDTO);
		addToRuleSetIfRelevantForGemeinde(betreuungspensumAbschnittRule, einstellungMap);

		// - Pensum Tagesschule
		TagesschuleBetreuungszeitAbschnittRule tagesschuleAbschnittRule = new TagesschuleBetreuungszeitAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(tagesschuleAbschnittRule, einstellungMap);

		// - Fachstelle
		FachstelleAbschnittRule fachstelleAbschnittRule = new FachstelleAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(fachstelleAbschnittRule, einstellungMap);

		// - GeschwisterBonus
		Einstellung einstellungBgAusstellenBisStufe = einstellungMap.get(EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE);
		EinschulungTyp bgAusstellenBisUndMitStufe = EinschulungTyp.valueOf(einstellungBgAusstellenBisStufe.getValue());
		GeschwisterbonusAbschnittRule geschwisterbonusAbschnittRule = new GeschwisterbonusAbschnittRule(bgAusstellenBisUndMitStufe, defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(geschwisterbonusAbschnittRule, einstellungMap);

		// - Ausserordentlicher Anspruch
		AusserordentlicherAnspruchAbschnittRule ausserordntl = new AusserordentlicherAnspruchAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(ausserordntl, einstellungMap);

		// - Einkommen / Einkommensverschlechterung / Maximales Einkommen
		EinkommenAbschnittRule einkommenAbschnittRule = new EinkommenAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(einkommenAbschnittRule, einstellungMap);

		// Wohnsitz (Zuzug und Wegzug)
		WohnsitzAbschnittRule wohnsitzAbschnittRule = new WohnsitzAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(wohnsitzAbschnittRule, einstellungMap);

		// - Einreichungsfrist
		EinreichungsfristAbschnittRule einreichungsfristAbschnittRule = new EinreichungsfristAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(einreichungsfristAbschnittRule, einstellungMap);

		// Abwesenheit
		Einstellung abwesenheitMaxDaysParam = einstellungMap.get(EinstellungKey.PARAM_MAX_TAGE_ABWESENHEIT);
		Integer abwesenheitMaxDaysValue = abwesenheitMaxDaysParam.getValueAsInteger();
		AbwesenheitAbschnittRule abwesenheitAbschnittRule = new AbwesenheitAbschnittRule(defaultGueltigkeit, abwesenheitMaxDaysValue, locale);
		addToRuleSetIfRelevantForGemeinde(abwesenheitAbschnittRule, einstellungMap);

		// Zivilstandsaenderung
		ZivilstandsaenderungAbschnittRule zivilstandsaenderungAbschnittRule = new ZivilstandsaenderungAbschnittRule(defaultGueltigkeit, param_minimaldauer_konkubinat.getValueAsInteger(), locale);
		addToRuleSetIfRelevantForGemeinde(zivilstandsaenderungAbschnittRule, einstellungMap);

		// Sozialhilfe
		SozialhilfeAbschnittRule sozialhilfeAbschnittRule = new SozialhilfeAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(sozialhilfeAbschnittRule, einstellungMap);

		// FamiliensituationBeendet
		FamiliensituationBeendetAbschnittRule familiensituationBeendetAbschnittRule = new FamiliensituationBeendetAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(familiensituationBeendetAbschnittRule, einstellungMap);
	}

	private void berechnenAnspruchRegeln(
		@Nonnull Gemeinde gemeinde,
		@Nullable KitaxUebergangsloesungParameter kitaxParameterDTO,
		@Nonnull Map<EinstellungKey, Einstellung> einstellungMap
	) {
		// GRUNDREGELN_CALC: Berechnen / Ändern den Anspruch

		// - Storniert
		StorniertCalcRule storniertCalcRule = new StorniertCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(storniertCalcRule, einstellungMap);

		// - Erwerbspensum Kanton
		Rule rule = new ErwerbspensumCalcRuleVisitor(einstellungMap, locale).getErwerbspesumCalcRule();
		addToRuleSetIfRelevantForGemeinde(rule, einstellungMap);

		// - KESB Platzierung: Max-Tarif bei Tagesschulen
		KesbPlatzierungTSCalcRule kesbPlatzierungTSCalcRule = new KesbPlatzierungTSCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(kesbPlatzierungTSCalcRule, einstellungMap);

		// - Erwerbspensum Gemeinde
		Einstellung minEWP_nichtEingeschultGmde = einstellungMap.get(GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT);
		Einstellung minEWP_eingeschultGmde = einstellungMap.get(GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT);
		Einstellung paramMinDauerKonkubinat = einstellungMap.get(MINIMALDAUER_KONKUBINAT);
		Objects.requireNonNull(minEWP_nichtEingeschultGmde, "Parameter MIN_ERWERBSPENSUM_NICHT_EINGESCHULT muss gesetzt sein");
		Objects.requireNonNull(minEWP_eingeschultGmde, "Parameter MIN_ERWERBSPENSUM_EINGESCHULT muss gesetzt sein");
		Objects.requireNonNull(paramMinDauerKonkubinat, "Parameter MINIMALDAUER_KONKUBINAT muss gesetzt sein");
		// Im Fall von BERN die Gueltigkeit einfach erst ab Tag X setzen?
		if (kitaxParameterDTO != null && KitaxUtil.isGemeindeWithKitaxUebergangsloesung(gemeinde)) {
			// Fuer die Stadt Bern gibt es die Rule mit verschiedenen Parameter: Vor dem Stichtag und nach dem Stichtag
			// Regel 1: Gemaess FEBR bis vor dem Stichtag
			DateRange vorStichtag = new DateRange(defaultGueltigkeit.getGueltigAb(), kitaxParameterDTO.getStadtBernAsivStartDate().minusDays(1));
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
			DateRange nachStichtag = new DateRange(kitaxParameterDTO.getStadtBernAsivStartDate(), defaultGueltigkeit.getGueltigBis());
			ErwerbspensumGemeindeCalcRule ewpBernCalcRuleNachStichtag = new ErwerbspensumGemeindeCalcRule(
				nachStichtag,
				minEWP_nichtEingeschultGmde.getValueAsInteger(),
				minEWP_eingeschultGmde.getValueAsInteger(),
				paramMinDauerKonkubinat.getValueAsInteger(),
				locale);
			addToRuleSetIfRelevantForGemeinde(ewpBernCalcRuleNachStichtag, einstellungMap);
		} else {
			// Fuer alle anderen Gemeinden gibt es nur *eine* Rule
			ErwerbspensumGemeindeCalcRule erwerbspensumGemeindeCalcRule = new ErwerbspensumGemeindeCalcRule(
				defaultGueltigkeit,
				minEWP_nichtEingeschultGmde.getValueAsInteger(),
				minEWP_eingeschultGmde.getValueAsInteger(),
				paramMinDauerKonkubinat.getValueAsInteger(),
				locale);
			addToRuleSetIfRelevantForGemeinde(erwerbspensumGemeindeCalcRule, einstellungMap);
		}

		// - Fachstelle: Muss zwingend nach Erwerbspensum und Betreuungspensum durchgefuehrt werden
		FachstelleBernCalcRule fachstelleBernCalcRule = new FachstelleBernCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(fachstelleBernCalcRule, einstellungMap);
		FachstelleLuzernCalcRule fachstelleLuzrnCalcRule = new FachstelleLuzernCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(fachstelleLuzrnCalcRule, einstellungMap);

		KitaPlusZuschlagCalcRule kitaPlusZuschlagCalcRule = new KitaPlusZuschlagCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(kitaPlusZuschlagCalcRule, einstellungMap);

		// - Ausserordentlicher Anspruch: Muss am Schluss gemacht werden, da er alle anderen Regeln überschreiben kann.
		// Wir haben je eine Anspruch-Regel für ASIV und FKJV, die entsprechend der Einstellungen aktiv sind
		Einstellung minErwerbspensumNichtEingeschult = getAusserordentlicherAnspruchMinErwerbspensumNichtEingeschult(einstellungMap);
		Einstellung minErwerbspensumEingeschult = getAusserordentlicherAnspruchMinErwerbspensumEingeschult(einstellungMap);
		Einstellung paramMaxDifferenzBeschaeftigungspensum = einstellungMap.get(FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM);
		Objects.requireNonNull(paramMaxDifferenzBeschaeftigungspensum, "Parameter FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM muss gesetzt sein");
		AusserordentlicherAnspruchCalcRule ausserordntlAsiv = new AusserordentlicherAnspruchCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(ausserordntlAsiv, einstellungMap);
		FKJVAusserordentlicherAnspruchCalcRule ausserordntlFkjv = new FKJVAusserordentlicherAnspruchCalcRule(
				minErwerbspensumNichtEingeschult.getValueAsInteger(),
				minErwerbspensumEingeschult.getValueAsInteger(),
				paramMaxDifferenzBeschaeftigungspensum.getValueAsInteger(),
				defaultGueltigkeit,
				locale);
		addToRuleSetIfRelevantForGemeinde(ausserordntlFkjv, einstellungMap);
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

	private void reduktionsRegeln(Map<EinstellungKey, Einstellung> einstellungMap) {
		// REDUKTIONSREGELN: Setzen Anpsruch auf 0

		// BETREUUNGS GUTSCHEINE START DATUM - Anspruch verfällt, wenn Gutscheine vor dem BetreuungsgutscheineStartdatum
		// der Gemeinde liegen
		GutscheineStartdatumCalcRule gutscheineStartdatumCalcRule = new GutscheineStartdatumCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(gutscheineStartdatumCalcRule, einstellungMap);

		// - Einkommen / Einkommensverschlechterung / Maximales Einkommen
		Einstellung paramMassgebendesEinkommenMax = einstellungMap.get(MAX_MASSGEBENDES_EINKOMMEN);
		Einstellung paramMaxEinkommenEKVEinstellung = einstellungMap.get(EinstellungKey.FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF);
		BigDecimal paramMaxEinkommenEKV = null;
		try {
			paramMaxEinkommenEKV = paramMaxEinkommenEKVEinstellung.getValueAsBigDecimal();
		} catch (NumberFormatException e) {
			// if NumberFormatException, param is not set in configuration and rule is not active
		}
		Einstellung paramPauschalBeiAnspruch = einstellungMap.get(FKJV_PAUSCHALE_BEI_ANSPRUCH);
		Objects.requireNonNull(paramMassgebendesEinkommenMax, "Parameter MAX_MASSGEBENDES_EINKOMMEN muss gesetzt sein");
		EinkommenCalcRule maxEinkommenCalcRule = new EinkommenCalcRule(
			defaultGueltigkeit,
			paramMassgebendesEinkommenMax.getValueAsBigDecimal(),
			paramMaxEinkommenEKV,
			paramPauschalBeiAnspruch.getValueAsBoolean(),
			locale);
		addToRuleSetIfRelevantForGemeinde(maxEinkommenCalcRule, einstellungMap);

		// Betreuungsangebot Tagesschule nicht berechnen
		BetreuungsangebotTypCalcRule betreuungsangebotTypCalcRule = new BetreuungsangebotTypCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(betreuungsangebotTypCalcRule, einstellungMap);

		// Wohnsitz (Zuzug und Wegzug)
		WohnsitzCalcRule wohnsitzCalcRule = new WohnsitzCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(wohnsitzCalcRule, einstellungMap);

		// Einreichungsfrist
		EinreichungsfristCalcRule einreichungsfristRule = new EinreichungsfristCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(einreichungsfristRule, einstellungMap);

		// Abwesenheit
		Einstellung abwesenheitMaxDaysParam = einstellungMap.get(EinstellungKey.PARAM_MAX_TAGE_ABWESENHEIT);
		Integer abwesenheitMaxDaysValue = abwesenheitMaxDaysParam.getValueAsInteger();
		AbwesenheitCalcRule abwesenheitCalcRule = new AbwesenheitCalcRule(defaultGueltigkeit, locale, abwesenheitMaxDaysValue);
		addToRuleSetIfRelevantForGemeinde(abwesenheitCalcRule, einstellungMap);

		// - Schulstufe des Kindes: Je nach Gemeindeeinstellung wird bis zu einer gewissen STufe ein Gutschein ausgestellt
		Einstellung einstellungBgAusstellenBisStufe = einstellungMap.get(EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE);
		EinschulungTyp bgAusstellenBisUndMitStufe = EinschulungTyp.valueOf(einstellungBgAusstellenBisStufe.getValue());
		SchulstufeCalcRule schulstufeCalcRule = new SchulstufeCalcRule(defaultGueltigkeit, bgAusstellenBisUndMitStufe, locale);
		addToRuleSetIfRelevantForGemeinde(schulstufeCalcRule, einstellungMap);

		// - KESB Platzierung: Kein Anspruch, da die KESB den Platz bezahlt
		KesbPlatzierungCalcRule kesbPlatzierungCalcRule = new KesbPlatzierungCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(kesbPlatzierungCalcRule, einstellungMap);

		// Sozialhilfeempfänger erhalten keinen Anspruch, wenn entsprechend konfiguriert
		SozialhilfeKeinAnspruchCalcRule
			sozialhilfeCalcRule = new SozialhilfeKeinAnspruchCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(sozialhilfeCalcRule, einstellungMap);

		//RESTANSPRUCH REDUKTION limitiert Anspruch auf  minimum(anspruchRest, anspruchPensum)
		RestanspruchLimitCalcRule restanspruchLimitCalcRule = new RestanspruchLimitCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(restanspruchLimitCalcRule, einstellungMap);

		// Verfuegungsbemerkung
		VerfuegungsBemerkungCalcRule bemerkungCalcRule = new VerfuegungsBemerkungCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(bemerkungCalcRule, einstellungMap);

		FamiliensituationBeendetCalcRule familiensituationBeendetCalcRule = new FamiliensituationBeendetCalcRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(familiensituationBeendetCalcRule, einstellungMap);
	}

	private void addToRuleSetIfRelevantForGemeinde(@Nonnull Rule rule, @Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		if (rule.isRelevantForGemeinde(einstellungMap)) {
			rules.add(rule);
		}
	}
}
