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
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;

import static ch.dvbern.ebegu.enums.EinstellungKey.ERWERBSPENSUM_ZUSCHLAG;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_MASSGEBENDES_EINKOMMEN;
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
		@Nonnull Map<EinstellungKey, Einstellung> ebeguRuleParameter,
		@Nonnull Locale inputLocale
	) {
		this.locale = inputLocale;
		useBernerRules(ebeguRuleParameter);
		return rules;
	}

	public Set<EinstellungKey> getRequiredParametersForGemeinde(@Nonnull Gemeinde gemeinde) {
		return requiredBernerParameters();
	}

	public Set<EinstellungKey> requiredBernerParameters() {
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
			ERWERBSPENSUM_ZUSCHLAG);
	}

	private void useBernerRules(Map<EinstellungKey, Einstellung> einstellungen) {

		abschnitteErstellenRegeln(einstellungen);
		berechnenAnspruchRegeln(einstellungen);
		reduktionsRegeln(einstellungen);

	}

	@SuppressWarnings("checkstyle:LocalVariableName")
	private void abschnitteErstellenRegeln(Map<EinstellungKey, Einstellung> einstellungMap) {
		// GRUNDREGELN_DATA: Abschnitte erstellen

		// - Erwerbspensum: Erstellt die grundlegenden Zeitschnitze (keine Korrekturen, nur einfügen)
		ErwerbspensumAbschnittRule erwerbspensumAbschnittRule = new ErwerbspensumAbschnittRule(defaultGueltigkeit, locale);
		rules.add(erwerbspensumAbschnittRule);

		// - Unbezahlter Urlaub
		UnbezahlterUrlaubAbschnittRule unbezahlterUrlaubAbschnittRule = new UnbezahlterUrlaubAbschnittRule(defaultGueltigkeit, locale);
		rules.add(unbezahlterUrlaubAbschnittRule);

		//Familenabzug: Berechnet den Familienabzug aufgrund der Familiengroesse
		Einstellung param_pauschalabzug_pro_person_familiengroesse_3 = einstellungMap.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3);
		Objects.requireNonNull(param_pauschalabzug_pro_person_familiengroesse_3, "Parameter PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3 muss gesetzt sein");
		Einstellung param_pauschalabzug_pro_person_familiengroesse_4 = einstellungMap.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4);
		Objects.requireNonNull(param_pauschalabzug_pro_person_familiengroesse_4, "Parameter PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4 muss gesetzt sein");
		Einstellung param_pauschalabzug_pro_person_familiengroesse_5 = einstellungMap.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5);
		Objects.requireNonNull(param_pauschalabzug_pro_person_familiengroesse_5, "Parameter PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5 muss gesetzt sein");
		Einstellung param_pauschalabzug_pro_person_familiengroesse_6 = einstellungMap.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6);
		Objects.requireNonNull(param_pauschalabzug_pro_person_familiengroesse_6, "Parameter PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6 muss gesetzt sein");

		FamilienabzugAbschnittRule familienabzugAbschnittRule = new FamilienabzugAbschnittRule(defaultGueltigkeit,
			param_pauschalabzug_pro_person_familiengroesse_3.getValueAsBigDecimal(),
			param_pauschalabzug_pro_person_familiengroesse_4.getValueAsBigDecimal(),
			param_pauschalabzug_pro_person_familiengroesse_5.getValueAsBigDecimal(),
			param_pauschalabzug_pro_person_familiengroesse_6.getValueAsBigDecimal(),
			locale);
		rules.add(familienabzugAbschnittRule);

		// Betreuungsgutscheine Gueltigkeit
		GutscheineStartdatumAbschnittRule gutscheineStartdatumAbschnittRule = new GutscheineStartdatumAbschnittRule(defaultGueltigkeit, locale);
		rules.add(gutscheineStartdatumAbschnittRule);

		// - KindTarif
		KindTarifAbschnittRule kindTarifAbschnittRule = new KindTarifAbschnittRule(defaultGueltigkeit, locale);
		rules.add(kindTarifAbschnittRule);

		// Betreuungsangebot
		BetreuungsangebotTypAbschnittRule betreuungsangebotTypAbschnittRule = new BetreuungsangebotTypAbschnittRule(defaultGueltigkeit, locale);
		rules.add(betreuungsangebotTypAbschnittRule);

		// - Betreuungspensum
		BetreuungspensumAbschnittRule betreuungspensumAbschnittRule = new BetreuungspensumAbschnittRule(defaultGueltigkeit, locale);
		rules.add(betreuungspensumAbschnittRule);

		// - Pensum Tagesschule
		TagesschuleBetreuungszeitAbschnittRule tagesschuleAbschnittRule = new TagesschuleBetreuungszeitAbschnittRule(defaultGueltigkeit, locale);
		rules.add(tagesschuleAbschnittRule);

		// - Fachstelle
		FachstelleAbschnittRule fachstelleAbschnittRule = new FachstelleAbschnittRule(defaultGueltigkeit, locale);
		rules.add(fachstelleAbschnittRule);

		// - Ausserordentlicher Anspruch
		AusserordentlicherAnspruchAbschnittRule ausserordntl = new AusserordentlicherAnspruchAbschnittRule(defaultGueltigkeit, locale);
		rules.add(ausserordntl);

		// - Einkommen / Einkommensverschlechterung / Maximales Einkommen
		EinkommenAbschnittRule einkommenAbschnittRule = new EinkommenAbschnittRule(defaultGueltigkeit, locale);
		rules.add(einkommenAbschnittRule);

		// Wohnsitz (Zuzug und Wegzug)
		WohnsitzAbschnittRule wohnsitzAbschnittRule = new WohnsitzAbschnittRule(defaultGueltigkeit, locale);
		rules.add(wohnsitzAbschnittRule);

		// - Einreichungsfrist
		EinreichungsfristAbschnittRule einreichungsfristAbschnittRule = new EinreichungsfristAbschnittRule(defaultGueltigkeit, locale);
		rules.add(einreichungsfristAbschnittRule);

		// Abwesenheit
		Einstellung abwesenheitMaxDaysParam = einstellungMap.get(EinstellungKey.PARAM_MAX_TAGE_ABWESENHEIT);
		Integer abwesenheitMaxDaysValue = abwesenheitMaxDaysParam.getValueAsInteger();
		AbwesenheitAbschnittRule abwesenheitAbschnittRule = new AbwesenheitAbschnittRule(defaultGueltigkeit, abwesenheitMaxDaysValue, locale);
		rules.add(abwesenheitAbschnittRule);

		// Zivilstandsaenderung
		ZivilstandsaenderungAbschnittRule zivilstandsaenderungAbschnittRule = new ZivilstandsaenderungAbschnittRule(defaultGueltigkeit, locale);
		rules.add(zivilstandsaenderungAbschnittRule);
	}

	private void berechnenAnspruchRegeln(Map<EinstellungKey, Einstellung> einstellungMap) {
		// GRUNDREGELN_CALC: Berechnen / Ändern den Anspruch

		// - Storniert
		StorniertCalcRule storniertCalcRule = new StorniertCalcRule(defaultGueltigkeit, locale);
		rules.add(storniertCalcRule);

		// - Erwerbspensum
		Einstellung zuschlagEWP = einstellungMap.get(ERWERBSPENSUM_ZUSCHLAG);
		Einstellung minEWP_nichtEingeschult = einstellungMap.get(MIN_ERWERBSPENSUM_NICHT_EINGESCHULT);
		Einstellung minEWP_eingeschult = einstellungMap.get(MIN_ERWERBSPENSUM_EINGESCHULT);
		Objects.requireNonNull(zuschlagEWP, "Parameter ERWERBSPENSUM_ZUSCHLAG muss gesetzt sein");
		Objects.requireNonNull(minEWP_nichtEingeschult, "Parameter MIN_ERWERBSPENSUM_NICHT_EINGESCHULT muss gesetzt sein");
		Objects.requireNonNull(minEWP_eingeschult, "Parameter MIN_ERWERBSPENSUM_EINGESCHULT muss gesetzt sein");
		ErwerbspensumCalcRule erwerbspensumCalcRule = new ErwerbspensumCalcRule(
			defaultGueltigkeit,
			zuschlagEWP.getValueAsInteger(),
			minEWP_nichtEingeschult.getValueAsInteger(),
			minEWP_eingeschult.getValueAsInteger(),
			locale);
		rules.add(erwerbspensumCalcRule);

		// - Fachstelle: Muss zwingend nach Erwerbspensum und Betreuungspensum durchgefuehrt werden
		FachstelleCalcRule fachstelleCalcRule = new FachstelleCalcRule(defaultGueltigkeit, locale);
		rules.add(fachstelleCalcRule);

		// - Ausserordentlicher Anspruch: Muss am Schluss gemacht werden, da er alle anderen Regeln überschreiben kann
		AusserordentlicherAnspruchCalcRule ausserordntl = new AusserordentlicherAnspruchCalcRule(defaultGueltigkeit, locale);
		rules.add(ausserordntl);
	}

	private void reduktionsRegeln(Map<EinstellungKey, Einstellung> einstellungMap) {
		// REDUKTIONSREGELN: Setzen Anpsruch auf 0

		// BETREUUNGS GUTSCHEINE START DATUM - Anspruch verfällt, wenn Gutscheine vor dem BetreuungsgutscheineStartdatum
		// der Gemeinde liegen
		GutscheineStartdatumCalcRule gutscheineStartdatumCalcRule = new GutscheineStartdatumCalcRule(defaultGueltigkeit, locale);
		rules.add(gutscheineStartdatumCalcRule);

		// - Einkommen / Einkommensverschlechterung / Maximales Einkommen
		Einstellung paramMassgebendesEinkommenMax = einstellungMap.get(MAX_MASSGEBENDES_EINKOMMEN);
		Objects.requireNonNull(paramMassgebendesEinkommenMax, "Parameter MAX_MASSGEBENDES_EINKOMMEN muss gesetzt sein");
		EinkommenCalcRule maxEinkommenCalcRule = new EinkommenCalcRule(
			defaultGueltigkeit,
			paramMassgebendesEinkommenMax.getValueAsBigDecimal(),
			locale);
		rules.add(maxEinkommenCalcRule);

		// Betreuungsangebot Tagesschule nicht berechnen
		BetreuungsangebotTypCalcRule betreuungsangebotTypCalcRule = new BetreuungsangebotTypCalcRule(defaultGueltigkeit, locale);
		rules.add(betreuungsangebotTypCalcRule);

		// Wohnsitz (Zuzug und Wegzug)
		WohnsitzCalcRule wohnsitzCalcRule = new WohnsitzCalcRule(defaultGueltigkeit, locale);
		rules.add(wohnsitzCalcRule);

		// Einreichungsfrist
		EinreichungsfristCalcRule einreichungsfristRule = new EinreichungsfristCalcRule(defaultGueltigkeit, locale);
		rules.add(einreichungsfristRule);

		// Abwesenheit
		AbwesenheitCalcRule abwesenheitCalcRule = new AbwesenheitCalcRule(defaultGueltigkeit, locale);
		rules.add(abwesenheitCalcRule);

		// - Schulstufe des Kindes: Je nach Gemeindeeinstellung wird bis zu einer gewissen STufe ein Gutschein ausgestellt
		Einstellung einstellungBgAusstellenBisStufe = einstellungMap.get(EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE);
		EinschulungTyp bgAusstellenBisUndMitStufe = EinschulungTyp.valueOf(einstellungBgAusstellenBisStufe.getValue());
		SchulstufeCalcRule schulstufeCalcRule = new SchulstufeCalcRule(defaultGueltigkeit, bgAusstellenBisUndMitStufe, locale);
		rules.add(schulstufeCalcRule);

		// - KESB Platzierung: Kein Anspruch, da die KESB den Platz bezahlt
		KesbPlatzierungCalcRule kesbPlatzierungCalcRule = new KesbPlatzierungCalcRule(defaultGueltigkeit, locale);
		rules.add(kesbPlatzierungCalcRule);

		//RESTANSPRUCH REDUKTION limitiert Anspruch auf  minimum(anspruchRest, anspruchPensum)
		RestanspruchLimitCalcRule restanspruchLimitCalcRule = new RestanspruchLimitCalcRule(defaultGueltigkeit, locale);
		rules.add(restanspruchLimitCalcRule);
	}
}
