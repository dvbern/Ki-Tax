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

package ch.dvbern.ebegu.enums;

import ch.dvbern.ebegu.rules.RuleKey;

/**
 * Dieses Enum dient der Verwaltung von Server Seitigen Uebersetzbaren Messages. Die hier definierten keys sollten im
 * server-messages.properties file uebersetzt werden. Die Optionale Verklinkung mit anderen Enums ist rein informativ
 */
public enum MsgKey {

	ERWERBSPENSUM_GS1_MSG(RuleKey.ERWERBSPENSUM),
	ERWERBSPENSUM_GS2_MSG(RuleKey.ERWERBSPENSUM),
	ERWERBSPENSUM_ANSPRUCH(RuleKey.ERWERBSPENSUM),
	ERWERBSPENSUM_MINIMUM_MSG(RuleKey.ERWERBSPENSUM),
	BETREUUNGSANGEBOT_MSG(RuleKey.BETREUUNGSANGEBOT_TYP),
	EINKOMMEN_MSG(RuleKey.EINKOMMEN),
	EINKOMMEN_VOLLKOSTEN_MSG(RuleKey.EINKOMMEN),
	EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG(RuleKey.EINKOMMEN),
	EINKOMMENSVERSCHLECHTERUNG_NOT_ACCEPT_MSG(RuleKey.EINKOMMEN),
	EINKOMMENSVERSCHLECHTERUNG_ANNULLIERT_MSG(RuleKey.EINKOMMEN),
	EINKOMMEN_SOZIALHILFEEMPFAENGER_MSG(RuleKey.EINKOMMEN),
	UNBEZAHLTER_URLAUB_MSG(RuleKey.UNBEZAHLTER_URLAUB),

	MINDESTALTER_MSG(RuleKey.MINDESTALTER),

	ABWESENHEIT_MSG(RuleKey.ABWESENHEIT),
	BETREUUNG_VOR_BEGU_START(RuleKey.BEGU_STARTDATUM),

	WOHNHAFT_MSG(RuleKey.WOHNHAFT_IM_GLEICHEN_HAUSHALT),
	WOHNSITZ_MSG(RuleKey.WOHNSITZ),
	FACHSTELLE_MSG(RuleKey.FACHSTELLE),
	AUSSERORDENTLICHER_ANSPRUCH_MSG(RuleKey.AUSSERORDENTLICHER_ANSPRUCH),
	EINREICHUNGSFRIST_MSG(RuleKey.EINREICHUNGSFRIST),
	EINREICHUNGSFRIST_VOLLKOSTEN_MSG(RuleKey.EINREICHUNGSFRIST),
	RESTANSPRUCH_MSG(RuleKey.RESTANSPRUCH),
	REDUCKTION_RUECKWIRKEND_MSG(RuleKey.ANSPRUCHSBERECHNUNGSREGELN_MUTATIONEN),
	ANSPRUCHSAENDERUNG_MSG(RuleKey.ANSPRUCHSBERECHNUNGSREGELN_MUTATIONEN),

	STORNIERT_MSG(RuleKey.STORNIERT),
	FAMILIENSITUATION_HEIRAT_MSG(RuleKey.ZIVILSTANDSAENDERUNG),
	FAMILIENSITUATION_TRENNUNG_MSG(RuleKey.ZIVILSTANDSAENDERUNG),

	SCHULSTUFE_VORSCHULE_MSG(RuleKey.SCHULSTUFE),
	SCHULSTUFE_KINDERGARTEN_1_MSG(RuleKey.SCHULSTUFE),
	SCHULSTUFE_KINDERGARTEN_2_MSG(RuleKey.SCHULSTUFE),

	KESB_PLATZIERUNG_MSG(RuleKey.KESB_PLATZIERUNG);



	private RuleKey referencedRuleKey;

	MsgKey(RuleKey referecedRule) {
		this.referencedRuleKey = referecedRule;
	}

	MsgKey() {
	}

	public RuleKey getReferencedRuleKey() {
		return referencedRuleKey;
	}

	public void setReferencedRuleKey(RuleKey referencedRuleKey) {
		this.referencedRuleKey = referencedRuleKey;
	}
}
