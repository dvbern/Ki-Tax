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
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.EbeguParameter;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.EbeguParameterKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;

import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_MASSGEBENDES_EINKOMMEN_MAX;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6;

/**
 * Configurator, welcher die Regeln und ihre Reihenfolge konfiguriert. Als Parameter erhält er den Mandanten sowie
 * die benötigten Ebegu-Parameter
 */
public class BetreuungsgutscheinConfigurator {

	private final DateRange defaultGueltigkeit = Constants.DEFAULT_GUELTIGKEIT;

	private final List<Rule> rules = new LinkedList<>();

	public List<Rule> configureRulesForMandant(@Nullable Mandant mandant, @Nonnull Map<EbeguParameterKey, EbeguParameter> ebeguRuleParameter) {
		useBernerRules(ebeguRuleParameter);
		return rules;
	}

	public Set<EbeguParameterKey> getRequiredParametersForMandant(@Nullable Mandant mandant) {
		return requiredBernerParameters();
	}

	public Set<EbeguParameterKey> requiredBernerParameters() {
		return EnumSet.of(
			PARAM_MASSGEBENDES_EINKOMMEN_MAX,
			PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,
			PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,
			PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,
			PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,
			PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM);
	}

	private void useBernerRules(Map<EbeguParameterKey, EbeguParameter> ebeguParameter) {

		abschnitteErstellenRegeln(ebeguParameter);
		berechnenAnspruchRegeln(ebeguParameter);
		reduktionsRegeln(ebeguParameter);

	}

	private void abschnitteErstellenRegeln(Map<EbeguParameterKey, EbeguParameter> ebeguParameter) {
		// GRUNDREGELN_DATA: Abschnitte erstellen

		// - Erwerbspensum: Erstellt die grundlegenden Zeitschnitze (keine Korrekturen, nur einfügen)
		ErwerbspensumAbschnittRule erwerbspensumAbschnittRule = new ErwerbspensumAbschnittRule(defaultGueltigkeit);
		rules.add(erwerbspensumAbschnittRule);

		//Familenabzug: Berechnet den Familienabzug aufgrund der Familiengroesse
		EbeguParameter param_pauschalabzug_pro_person_familiengroesse_3 = ebeguParameter.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3);
		Objects.requireNonNull(param_pauschalabzug_pro_person_familiengroesse_3, "Parameter PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3 muss gesetzt sein");
		EbeguParameter param_pauschalabzug_pro_person_familiengroesse_4 = ebeguParameter.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4);
		Objects.requireNonNull(param_pauschalabzug_pro_person_familiengroesse_4, "Parameter PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4 muss gesetzt sein");
		EbeguParameter param_pauschalabzug_pro_person_familiengroesse_5 = ebeguParameter.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5);
		Objects.requireNonNull(param_pauschalabzug_pro_person_familiengroesse_5, "Parameter PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5 muss gesetzt sein");
		EbeguParameter param_pauschalabzug_pro_person_familiengroesse_6 = ebeguParameter.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6);
		Objects.requireNonNull(param_pauschalabzug_pro_person_familiengroesse_6, "Parameter PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6 muss gesetzt sein");

		FamilienabzugAbschnittRule familienabzugAbschnittRule = new FamilienabzugAbschnittRule(defaultGueltigkeit,
			param_pauschalabzug_pro_person_familiengroesse_3.getValueAsBigDecimal(),
			param_pauschalabzug_pro_person_familiengroesse_4.getValueAsBigDecimal(),
			param_pauschalabzug_pro_person_familiengroesse_5.getValueAsBigDecimal(),
			param_pauschalabzug_pro_person_familiengroesse_6.getValueAsBigDecimal());
		rules.add(familienabzugAbschnittRule);

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

		// Mindestalter Kind
		MindestalterAbschnittRule mindestalterAbschnittRule = new MindestalterAbschnittRule(defaultGueltigkeit);
		rules.add(mindestalterAbschnittRule);

		// Abwesenheit
		AbwesenheitAbschnittRule abwesenheitAbschnittRule = new AbwesenheitAbschnittRule(defaultGueltigkeit);
		rules.add(abwesenheitAbschnittRule);

		// Zivilstandsaenderung
		ZivilstandsaenderungAbschnittRule zivilstandsaenderungAbschnittRule = new ZivilstandsaenderungAbschnittRule(defaultGueltigkeit);
		rules.add(zivilstandsaenderungAbschnittRule);
	}

	private void berechnenAnspruchRegeln(Map<EbeguParameterKey, EbeguParameter> ebeguParameter) {
		// GRUNDREGELN_CALC: Berechnen / Ändern den Anspruch

		// - Storniert
		StorniertCalcRule storniertCalcRule = new StorniertCalcRule(defaultGueltigkeit);
		rules.add(storniertCalcRule);

		// - Erwerbspensum
		EbeguParameter maxZuschlagValue = ebeguParameter.get(PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM);
		Objects.requireNonNull(maxZuschlagValue, "Parameter PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM muss gesetzt sein");
		ErwerbspensumCalcRule erwerbspensumCalcRule = new ErwerbspensumCalcRule(defaultGueltigkeit, maxZuschlagValue.getValueAsInteger());
		rules.add(erwerbspensumCalcRule);

		// - Betreuungspensum
		BetreuungspensumCalcRule betreuungspensumCalcRule = new BetreuungspensumCalcRule(defaultGueltigkeit);
		rules.add(betreuungspensumCalcRule);

		// - Fachstelle: Muss zwingend nach Erwerbspensum und Betreuungspensum durchgefuehrt werden
		FachstelleCalcRule fachstelleCalcRule = new FachstelleCalcRule(defaultGueltigkeit);
		rules.add(fachstelleCalcRule);
	}

	private void reduktionsRegeln(Map<EbeguParameterKey, EbeguParameter> ebeguParameter) {
		// REDUKTIONSREGELN: Setzen Anpsruch auf 0

		// - Einkommen / Einkommensverschlechterung / Maximales Einkommen
		EbeguParameter paramMassgebendesEinkommenMax = ebeguParameter.get(PARAM_MASSGEBENDES_EINKOMMEN_MAX);
		Objects.requireNonNull(paramMassgebendesEinkommenMax, "Parameter PARAM_MASSGEBENDES_EINKOMMEN_MAX muss gesetzt sein");
		EinkommenCalcRule maxEinkommenCalcRule = new EinkommenCalcRule(defaultGueltigkeit, paramMassgebendesEinkommenMax.getValueAsBigDecimal());
		rules.add(maxEinkommenCalcRule);

		// Betreuungsangebot Tagesschule nicht berechnen
		BetreuungsangebotTypCalcRule betreuungsangebotTypCalcRule = new BetreuungsangebotTypCalcRule(defaultGueltigkeit);
		rules.add(betreuungsangebotTypCalcRule);

		// Mindestalter Kind
		MindestalterCalcRule mindestalterRule = new MindestalterCalcRule(defaultGueltigkeit);
		rules.add(mindestalterRule);

		// Wohnsitz (Zuzug und Wegzug)
		WohnsitzCalcRule wohnsitzCalcRule = new WohnsitzCalcRule(defaultGueltigkeit);
		rules.add(wohnsitzCalcRule);

		// Einreichungsfrist
		EinreichungsfristCalcRule einreichungsfristRule = new EinreichungsfristCalcRule(defaultGueltigkeit);
		rules.add(einreichungsfristRule);

		// Mindestalter Kind
		MindestalterCalcRule mindestalterCalcRule = new MindestalterCalcRule(defaultGueltigkeit);
		rules.add(mindestalterCalcRule);

		// Abwesenheit
		AbwesenheitCalcRule abwesenheitCalcRule = new AbwesenheitCalcRule(defaultGueltigkeit);
		rules.add(abwesenheitCalcRule);

		//RESTANSPRUCH REDUKTION limitiert Anspruch auf  minimum(anspruchRest, anspruchPensum)
		RestanspruchLimitCalcRule restanspruchLimitCalcRule = new RestanspruchLimitCalcRule(defaultGueltigkeit);
		rules.add(restanspruchLimitCalcRule);

	}

}
