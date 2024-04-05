/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

export enum TSWizardStepName {
    SOZIALDIENSTFALL_ERSTELLEN = 'SOZIALDIENSTFALL_ERSTELLEN',
    GESUCH_ERSTELLEN = 'GESUCH_ERSTELLEN',
    FAMILIENSITUATION = 'FAMILIENSITUATION',
    GESUCHSTELLER = 'GESUCHSTELLER',
    UMZUG = 'UMZUG',
    KINDER = 'KINDER',
    BETREUUNG = 'BETREUUNG',
    ABWESENHEIT = 'ABWESENHEIT',
    ERWERBSPENSUM = 'ERWERBSPENSUM',
    FINANZIELLE_SITUATION = 'FINANZIELLE_SITUATION',
    FINANZIELLE_SITUATION_LUZERN = 'FINANZIELLE_SITUATION_LUZERN',
    FINANZIELLE_SITUATION_SOLOTHURN = 'FINANZIELLE_SITUATION_SOLOTHURN',
    FINANZIELLE_SITUATION_APPENZELL = 'FINANZIELLE_SITUATION_APPENZELL',
    FINANZIELLE_SITUATION_SCHWYZ = 'FINANZIELLE_SITUATION_SCHWYZ',
    EINKOMMENSVERSCHLECHTERUNG = 'EINKOMMENSVERSCHLECHTERUNG',
    EINKOMMENSVERSCHLECHTERUNG_LUZERN = 'EINKOMMENSVERSCHLECHTERUNG_LUZERN',
    EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN = 'EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN',
    EINKOMMENSVERSCHLECHTERUNG_APPENZELL = 'EINKOMMENSVERSCHLECHTERUNG_APPENZELL',
    DOKUMENTE = 'DOKUMENTE',
    FREIGABE = 'FREIGABE',
    VERFUEGEN = 'VERFUEGEN'
}

/**
 * It is crucial that this function returns all elements in the order they will have in the navigation menu.
 * the order of this function will be used to navigate through all steps, so if this order is not correct the
 * navigation won't work as expected.
 */
export function getTSWizardStepNameValues(): Array<TSWizardStepName> {
    return [
        TSWizardStepName.SOZIALDIENSTFALL_ERSTELLEN,
        TSWizardStepName.GESUCH_ERSTELLEN,
        TSWizardStepName.FAMILIENSITUATION,
        TSWizardStepName.GESUCHSTELLER,
        TSWizardStepName.UMZUG,
        TSWizardStepName.KINDER,
        TSWizardStepName.BETREUUNG,
        TSWizardStepName.ABWESENHEIT,
        TSWizardStepName.ERWERBSPENSUM,
        TSWizardStepName.FINANZIELLE_SITUATION,
        TSWizardStepName.FINANZIELLE_SITUATION_LUZERN,
        TSWizardStepName.FINANZIELLE_SITUATION_SOLOTHURN,
        TSWizardStepName.FINANZIELLE_SITUATION_APPENZELL,
        TSWizardStepName.FINANZIELLE_SITUATION_SCHWYZ,
        TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG,
        TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN,
        TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN,
        TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_APPENZELL,
        TSWizardStepName.DOKUMENTE,
        TSWizardStepName.FREIGABE,
        TSWizardStepName.VERFUEGEN
    ];
}

export function getAllWizardStepsWithoutFinSitSteps(): Array<TSWizardStepName> {
    return [
        TSWizardStepName.SOZIALDIENSTFALL_ERSTELLEN,
        TSWizardStepName.GESUCH_ERSTELLEN,
        TSWizardStepName.FAMILIENSITUATION,
        TSWizardStepName.GESUCHSTELLER,
        TSWizardStepName.UMZUG,
        TSWizardStepName.KINDER,
        TSWizardStepName.BETREUUNG,
        TSWizardStepName.ABWESENHEIT,
        TSWizardStepName.ERWERBSPENSUM,
        TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG,
        TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN,
        TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN,
        TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_APPENZELL,
        TSWizardStepName.DOKUMENTE,
        TSWizardStepName.FREIGABE,
        TSWizardStepName.VERFUEGEN
    ];
}
