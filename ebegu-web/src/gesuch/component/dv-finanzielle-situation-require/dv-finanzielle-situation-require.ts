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

import {IComponentOptions} from 'angular';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import GesuchModelManager from '../../service/gesuchModelManager';


export class DvFinanzielleSituationRequire implements IComponentOptions {
    transclude = false;
    bindings = {
        areThereOnlySchulamtangebote: '=',
        sozialhilfeBezueger: '=',
        verguenstigungGewuenscht: '=',
        finanzielleSituationRequired: '='
    };
    template = require('./dv-finanzielle-situation-require.html');
    controller = DVFinanzielleSituationRequireController;
    controllerAs = 'vm';
}

export class DVFinanzielleSituationRequireController {

    static $inject: ReadonlyArray<string> = ['EinstellungRS', 'GesuchModelManager'];

    finanzielleSituationRequired: boolean;
    areThereOnlySchulamtangebote: boolean;
    sozialhilfeBezueger: boolean;
    verguenstigungGewuenscht: boolean;

    maxMassgebendesEinkommen: string;

    constructor(
        private readonly einstellungRS: EinstellungRS,
        private readonly gesuchModelManager: GesuchModelManager) {
    }

    $onInit() {
        this.setFinanziellesituationRequired();
        // Den Parameter fuer das Maximale Einkommen lesen
        this.einstellungRS.findEinstellung(TSEinstellungKey.PARAM_MASSGEBENDES_EINKOMMEN_MAX, undefined, this.gesuchModelManager.getGesuchsperiode()).then(response => {
            this.maxMassgebendesEinkommen = response.value;
        });
    }

    /**
     * Das Feld sozialhilfeBezueger muss nur angezeigt werden, wenn es ein rein Schulamtgesuch ist.
     */
    public showSozialhilfeBezueger(): boolean {
        return this.areThereOnlySchulamtangebote;
    }

    /**
     * Das Feld verguenstigungGewuenscht wird nur angezeigt, wenn das Feld sozialhilfeBezueger eingeblendet ist und mit nein beantwortet wurde.
     */
    public showVerguenstigungGewuenscht(): boolean {
        return this.showSozialhilfeBezueger() && this.sozialhilfeBezueger === false;
    }

    public setFinanziellesituationRequired(): void {
        this.finanzielleSituationRequired = !this.showSozialhilfeBezueger()
            || (this.showVerguenstigungGewuenscht() && this.verguenstigungGewuenscht === true);
    }

    public getMaxMassgebendesEinkommen(): string {
        return this.maxMassgebendesEinkommen;
    }

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }
}
