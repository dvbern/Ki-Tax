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

import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_MASSGEBENDES_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM;
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

	@Nonnull
	public List<Rule> configureRulesForMandant(@Nonnull Gemeinde gemeinde, @Nonnull Map<EinstellungKey, Einstellung> ebeguRuleParameter) {
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
			PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM,
			PARAM_MAX_TAGE_ABWESENHEIT,
			GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE);
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
		ErwerbspensumAbschnittRule erwerbspensumAbschnittRule = new ErwerbspensumAbschnittRule(defaultGueltigkeit);
		rules.add(erwerbspensumAbschnittRule);

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
			param_pauschalabzug_pro_person_familiengroesse_6.getValueAsBigDecimal());
		rules.add(familienabzugAbschnittRule);

		// Betreuungsgutscheine Gueltigkeit
		GutscheineStartdatumAbschnittRule gutscheineStartdatumAbschnittRule = new GutscheineStartdatumAbschnittRule(defaultGueltigkeit);
		rules.add(gutscheineStartdatumAbschnittRule);

		// - Betreuungspensum
		BetreuungspensumAbschnittRule betreuungspensumAbschnittRule = new BetreuungspensumAbschnittRule(defaultGueltigkeit);
		rules.add(betreuungspensumAbschnittRule);

		// - Fachstelle
		FachstelleAbschnittRule fachstelleAbschnittRule = new FachstelleAbschnittRule(defaultGueltigkeit);
		rules.add(fachstelleAbschnittRule);

		// - Einkommen / Einkommensverschlechterung / Maximales Einkommen
		EinkommenAbschnittRule einkommenAbschnittRule = new EinkommenAbschnittRule(defaultGueltigkeit);
		rules.add(einkommenAbschnittRule);

		// Wohnsitz (Zuzug und Wegzug)
		WohnsitzAbschnittRule wohnsitzAbschnittRule = new WohnsitzAbschnittRule(defaultGueltigkeit);
		rules.add(wohnsitzAbschnittRule);

		// - Einreichungsfrist
		EinreichungsfristAbschnittRule einreichungsfristAbschnittRule = new EinreichungsfristAbschnittRule(defaultGueltigkeit);
		rules.add(einreichungsfristAbschnittRule);

		// Abwesenheit
		Einstellung abwesenheitMaxDaysParam = einstellungMap.get(EinstellungKey.PARAM_MAX_TAGE_ABWESENHEIT);
		Integer abwesenheitMaxDaysValue = abwesenheitMaxDaysParam.getValueAsInteger();
		AbwesenheitAbschnittRule abwesenheitAbschnittRule = new AbwesenheitAbschnittRule(defaultGueltigkeit, abwesenheitMaxDaysValue);
		rules.add(abwesenheitAbschnittRule);

		// Zivilstandsaenderung
		ZivilstandsaenderungAbschnittRule zivilstandsaenderungAbschnittRule = new ZivilstandsaenderungAbschnittRule(defaultGueltigkeit);
		rules.add(zivilstandsaenderungAbschnittRule);
	}

	private void berechnenAnspruchRegeln(Map<EinstellungKey, Einstellung> einstellungMap) {
		// GRUNDREGELN_CALC: Berechnen / Ändern den Anspruch

		// - Storniert
		StorniertCalcRule storniertCalcRule = new StorniertCalcRule(defaultGueltigkeit);
		rules.add(storniertCalcRule);

		// - Erwerbspensum
		Einstellung maxZuschlagValue = einstellungMap.get(PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM);
		Objects.requireNonNull(maxZuschlagValue, "Parameter PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM muss gesetzt sein");
		ErwerbspensumCalcRule erwerbspensumCalcRule = new ErwerbspensumCalcRule(defaultGueltigkeit, maxZuschlagValue.getValueAsInteger());
		rules.add(erwerbspensumCalcRule);

		// - Fachstelle: Muss zwingend nach Erwerbspensum und Betreuungspensum durchgefuehrt werden
		FachstelleCalcRule fachstelleCalcRule = new FachstelleCalcRule(defaultGueltigkeit);
		rules.add(fachstelleCalcRule);
	}

	private void reduktionsRegeln(Map<EinstellungKey, Einstellung> einstellungMap) {
		// REDUKTIONSREGELN: Setzen Anpsruch auf 0

		// BETREUUNGS GUTSCHEINE START DATUM - Anspruch verfällt, wenn Gutscheine vor dem BetreuungsgutscheineStartdatum
		// der Gemeinde liegen
		GutscheineStartdatumCalcRule gutscheineStartdatumCalcRule = new GutscheineStartdatumCalcRule(defaultGueltigkeit);
		rules.add(gutscheineStartdatumCalcRule);

		// - Einkommen / Einkommensverschlechterung / Maximales Einkommen
		Einstellung paramMassgebendesEinkommenMax = einstellungMap.get(MAX_MASSGEBENDES_EINKOMMEN);
		Objects.requireNonNull(paramMassgebendesEinkommenMax, "Parameter MAX_MASSGEBENDES_EINKOMMEN muss gesetzt sein");
		EinkommenCalcRule maxEinkommenCalcRule = new EinkommenCalcRule(defaultGueltigkeit, paramMassgebendesEinkommenMax.getValueAsBigDecimal());
		rules.add(maxEinkommenCalcRule);

		// Betreuungsangebot Tagesschule nicht berechnen
		BetreuungsangebotTypCalcRule betreuungsangebotTypCalcRule = new BetreuungsangebotTypCalcRule(defaultGueltigkeit);
		rules.add(betreuungsangebotTypCalcRule);

		// Wohnsitz (Zuzug und Wegzug)
		WohnsitzCalcRule wohnsitzCalcRule = new WohnsitzCalcRule(defaultGueltigkeit);
		rules.add(wohnsitzCalcRule);

		// Einreichungsfrist
		EinreichungsfristCalcRule einreichungsfristRule = new EinreichungsfristCalcRule(defaultGueltigkeit);
		rules.add(einreichungsfristRule);

		// Abwesenheit
		AbwesenheitCalcRule abwesenheitCalcRule = new AbwesenheitCalcRule(defaultGueltigkeit);
		rules.add(abwesenheitCalcRule);

		// - Schulstufe des Kindes: Je nach Gemeindeeinstellung wird bis zu einer gewissen STufe ein Gutschein ausgestellt
		Einstellung einstellungBgAusstellenBisStufe = einstellungMap.get(EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE);
		EinschulungTyp bgAusstellenBisUndMitStufe = EinschulungTyp.valueOf(einstellungBgAusstellenBisStufe.getValue());
		SchulstufeCalcRule schulstufeCalcRule = new SchulstufeCalcRule(defaultGueltigkeit, bgAusstellenBisUndMitStufe);
		rules.add(schulstufeCalcRule);

		// - KESB Platzierung: Kein Anspruch, da die KESB den Platz bezahlt
		KesbPlatzierungCalcRule kesbPlatzierungCalcRule = new KesbPlatzierungCalcRule(defaultGueltigkeit);
		rules.add(kesbPlatzierungCalcRule);

		//RESTANSPRUCH REDUKTION limitiert Anspruch auf  minimum(anspruchRest, anspruchPensum)
		RestanspruchLimitCalcRule restanspruchLimitCalcRule = new RestanspruchLimitCalcRule(defaultGueltigkeit);
		rules.add(restanspruchLimitCalcRule);
	}
}
