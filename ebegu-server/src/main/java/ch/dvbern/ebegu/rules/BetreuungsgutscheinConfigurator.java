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

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.rules.familienabzug.AbstractFamilienabzugAbschnittRule;
import ch.dvbern.ebegu.rules.familienabzug.FamilienabzugAbschnittRuleVisitor;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.KitaxUtil;
import ch.dvbern.ebegu.util.RuleParameterUtil;
import com.google.common.base.Enums;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static ch.dvbern.ebegu.enums.EinstellungKey.*;

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
				ANGEBOT_SCHULSTUFE,
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
				GESCHWISTERNBONUS_TYP,
				AUSSERORDENTLICHER_ANSPRUCH_RULE,
				DAUER_BABYTARIF,
				KINDERABZUG_TYP,
				FKJV_TEXTE,
				FACHSTELLEN_TYP,
				GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER,
				ANSPRUCH_AB_X_MONATEN,
				SPRACHFOERDERUNG_BESTAETIGEN,
			    GESUCH_BEENDEN_BEI_TAUSCH_GS2,
				SCHULERGAENZENDE_BETREUUNGEN,
				WEGZEIT_ERWERBSPENSUM,
				ANWESENHEITSTAGE_PRO_MONAT_AKTIVIERT,
				SOZIALVERSICHERUNGSNUMMER_PERIODE,
				HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG_AKTIVIERT
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

		KinderabzugTyp kinderAbzugTyp = KinderabzugTyp.valueOf(ruleParameterUtil.getEinstellung(KINDERABZUG_TYP).getValue());
		AbstractFamilienabzugAbschnittRule familienabzugAbschnittRuleToUse = new FamilienabzugAbschnittRuleVisitor(ruleParameterUtil.getEinstellungen(),
			defaultGueltigkeit, locale).getFamilienabzugAbschnittRule(kinderAbzugTyp);

		addToRuleSetIfRelevantForGemeinde(familienabzugAbschnittRuleToUse, ruleParameterUtil);

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

		// Eingewoehnung Pauschale
		EingewoehnungPauschaleAbschnittRule eingewoehnungPauschaleAbschnittRule =
				new EingewoehnungPauschaleAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(eingewoehnungPauschaleAbschnittRule, ruleParameterUtil);

		// - Pensum Tagesschule
		TagesschuleBetreuungszeitAbschnittRule tagesschuleAbschnittRule =
				new TagesschuleBetreuungszeitAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(tagesschuleAbschnittRule, ruleParameterUtil);

		// - Fachstelle
		FachstelleAbschnittRule fachstelleAbschnittRule = new FachstelleAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(fachstelleAbschnittRule, ruleParameterUtil);

		// - GeschwisterBonus LU
		Einstellung einstellungBgAusstellenBisStufe =
				ruleParameterUtil.getEinstellung(GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE);
		EinschulungTyp bgAusstellenBisUndMitStufe = EinschulungTyp.valueOf(einstellungBgAusstellenBisStufe.getValue());
		GeschwisterbonusLuzernAbschnittRule geschwisterbonusLuzernAbschnittRule =
				new GeschwisterbonusLuzernAbschnittRule(bgAusstellenBisUndMitStufe, defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(geschwisterbonusLuzernAbschnittRule, ruleParameterUtil);

		// - GeschwisterBonus Schwyz
		GeschwisterbonusSchwyzAbschnittRule geschwisterbonusSchwyzAbschnittRule =
			new GeschwisterbonusSchwyzAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(geschwisterbonusSchwyzAbschnittRule, ruleParameterUtil);

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
		Einstellung minimaldauerKonkubinat = ruleParameterUtil.getEinstellung(MINIMALDAUER_KONKUBINAT);
		ZivilstandsaenderungAbschnittRule zivilstandsaenderungAbschnittRule = new ZivilstandsaenderungAbschnittRule(
				defaultGueltigkeit,
				minimaldauerKonkubinat.getValueAsInteger(),
				locale);
		addToRuleSetIfRelevantForGemeinde(zivilstandsaenderungAbschnittRule, ruleParameterUtil);

		// Sozialhilfe
		SozialhilfeAbschnittRule sozialhilfeAbschnittRule = new SozialhilfeAbschnittRule(defaultGueltigkeit, locale);
		addToRuleSetIfRelevantForGemeinde(sozialhilfeAbschnittRule, ruleParameterUtil);

		// FamiliensituationBeendet
		Einstellung gesuchBeendenActivated = ruleParameterUtil.getEinstellung(GESUCH_BEENDEN_BEI_TAUSCH_GS2);
		FamiliensituationBeendetAbschnittRule familiensituationBeendetAbschnittRule =
				new FamiliensituationBeendetAbschnittRule(
					defaultGueltigkeit,
					locale,
					gesuchBeendenActivated.getValueAsBoolean());

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
		Einstellung sprachefoerderungBestaetigen = ruleParameterUtil.getEinstellung(SPRACHFOERDERUNG_BESTAETIGEN);
		FachstelleBernCalcRule fachstelleBernCalcRule =
			new FachstelleBernCalcRule(sprachefoerderungBestaetigen.getValueAsBoolean(), defaultGueltigkeit, locale);
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
		Einstellung angebotSchulstufe = ruleParameterUtil.getEinstellung(ANGEBOT_SCHULSTUFE);
		List<BetreuungsangebotTyp> betreuungsangebotTyps = Arrays.stream(angebotSchulstufe.getValue().split(","))
			.map(angebot -> Enums.getIfPresent(
				BetreuungsangebotTyp.class,
				angebot.stripLeading().stripTrailing()).orNull())
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		SchulstufeCalcRule schulstufeCalcRule =
				new SchulstufeCalcRule(defaultGueltigkeit, bgAusstellenBisUndMitStufe, betreuungsangebotTyps, locale);
		addToRuleSetIfRelevantForGemeinde(schulstufeCalcRule, ruleParameterUtil);

		// - Kind Lebt gar nicht im Hausalt - Betreuungspensum 0
		KinderabzugTyp kinderAbzugTyp = KinderabzugTyp.valueOf(ruleParameterUtil.getEinstellung(KINDERABZUG_TYP).getValue());
		KindAnspruchCalcRule kindAnspruchCalcRule = new KindAnspruchCalcRule(defaultGueltigkeit, locale, kinderAbzugTyp);
		addToRuleSetIfRelevantForGemeinde(kindAnspruchCalcRule, ruleParameterUtil);

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
