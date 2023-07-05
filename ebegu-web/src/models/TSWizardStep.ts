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

import {TSWizardStepName} from './enums/TSWizardStepName';
import {TSWizardStepStatus} from './enums/TSWizardStepStatus';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';

export class TSWizardStep extends TSAbstractMutableEntity {

    private _gesuchId: string;
    private _wizardStepName: TSWizardStepName;
    private _wizardStepStatus: TSWizardStepStatus;
    private _bemerkungen: string;
    private _verfuegbar: boolean;
    private _bemerkungenDossier: string;

    public constructor() {
        super();
    }

    public get gesuchId(): string {
        return this._gesuchId;
    }

    public set gesuchId(value: string) {
        this._gesuchId = value;
    }

    public get wizardStepName(): TSWizardStepName {
        return this._wizardStepName;
    }

    public set wizardStepName(value: TSWizardStepName) {
        this._wizardStepName = value;
    }

    public get wizardStepStatus(): TSWizardStepStatus {
        return this._wizardStepStatus;
    }

    public set wizardStepStatus(value: TSWizardStepStatus) {
        this._wizardStepStatus = value;
    }

    public get bemerkungen(): string {
        return this._bemerkungen;
    }

    public set bemerkungen(value: string) {
        this._bemerkungen = value;
    }

    public get verfuegbar(): boolean {
        return this._verfuegbar;
    }

    public set verfuegbar(value: boolean) {
        this._verfuegbar = value;
    }

    get bemerkungenDossier(): string {
        return this._bemerkungenDossier;
    }

    set bemerkungenDossier(value: string) {
        this._bemerkungenDossier = value;
    }
}
