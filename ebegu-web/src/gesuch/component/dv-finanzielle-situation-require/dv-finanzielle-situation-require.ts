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
import {EbeguParameterRS} from '../../../admin/service/ebeguParameterRS.rest';
import {TSEbeguParameterKey} from '../../../models/enums/TSEbeguParameterKey';
import GesuchModelManager from '../../service/gesuchModelManager';

declare let require: any;
const template = require('./dv-finanzielle-situation-require.html');

export class DvFinanzielleSituationRequire implements IComponentOptions {
    transclude = false;
    bindings: any = {
        areThereOnlySchulamtangebote: '=',
        sozialhilfeBezueger: '=',
        verguenstigungGewuenscht: '=',
        finanzielleSituationRequired: '='
    };
    template = template;
    controller = DVFinanzielleSituationRequireController;
    controllerAs = 'vm';
}

export class DVFinanzielleSituationRequireController {

    static $inject: ReadonlyArray<string> = ['EbeguParameterRS', 'GesuchModelManager'];

    finanzielleSituationRequired: boolean;
    areThereOnlySchulamtangebote: boolean;
    sozialhilfeBezueger: boolean;
    verguenstigungGewuenscht: boolean;

    maxMassgebendesEinkommen: string;

    /* @ngInject */
    constructor(private readonly ebeguParameterRS: EbeguParameterRS, private readonly gesuchModelManager: GesuchModelManager) {
    }

    $onInit() {
        this.setFinanziellesituationRequired();
        // Den Parameter fuer das Maximale Einkommen lesen
        this.ebeguParameterRS.getEbeguParameterByKeyAndDate(
                this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigAb,
                TSEbeguParameterKey.PARAM_MASSGEBENDES_EINKOMMEN_MAX).then(response => {
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
