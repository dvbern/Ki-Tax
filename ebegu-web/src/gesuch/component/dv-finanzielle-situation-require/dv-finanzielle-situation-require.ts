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

import {IComponentOptions, IController} from 'angular';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSFinSitStatus} from '../../../models/enums/TSFinSitStatus';
import EbeguUtil from '../../../utils/EbeguUtil';
import GesuchModelManager from '../../service/gesuchModelManager';

export class DvFinanzielleSituationRequire implements IComponentOptions {
    public transclude = false;
    public bindings = {
        sozialhilfeBezueger: '=',
        verguenstigungGewuenscht: '=',
        finanzielleSituationRequired: '=',
    };
    public template = require('./dv-finanzielle-situation-require.html');
    public controller = DVFinanzielleSituationRequireController;
    public controllerAs = 'vm';
}

export class DVFinanzielleSituationRequireController implements IController {

    public static $inject: ReadonlyArray<string> = ['EinstellungRS', 'GesuchModelManager'];

    public finanzielleSituationRequired: boolean;
    public areThereOnlySchulamtangebote: boolean;
    public sozialhilfeBezueger: boolean;
    public verguenstigungGewuenscht: boolean;

    public maxMassgebendesEinkommen: string;

    public constructor(
        private readonly einstellungRS: EinstellungRS,
        private readonly gesuchModelManager: GesuchModelManager,
    ) {
    }

    public $onInit(): void {
        this.setFinanziellesituationRequired();
        // Den Parameter fuer das Maximale Einkommen lesen
        this.einstellungRS.findEinstellung(TSEinstellungKey.MAX_MASSGEBENDES_EINKOMMEN,
            this.gesuchModelManager.getDossier().gemeinde,
            this.gesuchModelManager.getGesuchsperiode())
            .then(response => {
                this.maxMassgebendesEinkommen = response.value;
            });
    }

    /**
     * Das Feld verguenstigungGewuenscht wird nur angezeigt, wenn das Feld sozialhilfeBezueger eingeblendet ist und mit
     * nein beantwortet wurde.
     */
    public showVerguenstigungGewuenscht(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.sozialhilfeBezueger) && !this.sozialhilfeBezueger;
    }

    public setFinanziellesituationRequired(): void {
        const required = EbeguUtil.isNotNullOrUndefined(this.sozialhilfeBezueger) && !this.sozialhilfeBezueger
                    && EbeguUtil.isNotNullOrUndefined(this.verguenstigungGewuenscht) && this.verguenstigungGewuenscht;
        // Wenn es sich geändert hat, müssen gewisse Daten gesetzt werden
        if (required !== this.finanzielleSituationRequired) {
            this.gesuchModelManager.getGesuch().finSitStatus = required ? null : TSFinSitStatus.AKZEPTIERT;
        }
        this.finanzielleSituationRequired = required;
    }

    public getMaxMassgebendesEinkommen(): string {
        return this.maxMassgebendesEinkommen;
    }

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }
}
