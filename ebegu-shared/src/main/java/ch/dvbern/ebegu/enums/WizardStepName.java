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

/**
 * Enum fuer die Namen des WizardSteps
 */
public enum WizardStepName {
	SOZIALDIENSTFALL_ERSTELLEN,
	GESUCH_ERSTELLEN,
	FAMILIENSITUATION,
	GESUCHSTELLER,
	UMZUG,
	KINDER,
	BETREUUNG,
	ABWESENHEIT,
	ERWERBSPENSUM,
	FINANZIELLE_SITUATION,
	FINANZIELLE_SITUATION_LUZERN,
	FINANZIELLE_SITUATION_SOLOTHURN,
	FINANZIELLE_SITUATION_APPENZELL,
	EINKOMMENSVERSCHLECHTERUNG,
	EINKOMMENSVERSCHLECHTERUNG_LUZERN,
	EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN,
	EINKOMMENSVERSCHLECHTERUNG_APPENZELL,
	DOKUMENTE,
	FREIGABE,
	VERFUEGEN;

	public boolean isFinSitWizardStepName() {
		return FINANZIELLE_SITUATION == this
				|| FINANZIELLE_SITUATION_LUZERN == this
				|| FINANZIELLE_SITUATION_SOLOTHURN == this
				|| FINANZIELLE_SITUATION_APPENZELL == this;
	}

	public boolean isEKVWizardStepName() {
		return EINKOMMENSVERSCHLECHTERUNG == this
			|| EINKOMMENSVERSCHLECHTERUNG_LUZERN == this
			|| EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN == this;
	}
}
