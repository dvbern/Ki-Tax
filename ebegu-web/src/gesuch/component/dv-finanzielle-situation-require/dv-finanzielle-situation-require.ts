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

import {IComponentOptions, IController, IFormController} from 'angular';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {EbeguNumberPipe} from '../../../app/shared/pipe/ebegu-number.pipe';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSFinanzielleSituationTyp} from '../../../models/enums/TSFinanzielleSituationTyp';
import {TSFinSitStatus} from '../../../models/enums/TSFinSitStatus';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {FinanzielleSituationRS} from '../../service/finanzielleSituationRS.rest';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import ITranslateService = angular.translate.ITranslateService;

export class DvFinanzielleSituationRequire implements IComponentOptions {
    public transclude = false;
    public bindings = {
        sozialhilfeBezueger: '=',
        verguenstigungGewuenscht: '=',
        finanzielleSituationRequired: '=',
        areThereAnyBgBetreuungen: '=',
        form: '='
    };
    public template = require('./dv-finanzielle-situation-require.html');
    public controller = DVFinanzielleSituationRequireController;
    public controllerAs = 'vm';
}

const LOG = LogFactory.createLog('DVFinanzielleSituationRequireController');

export class DVFinanzielleSituationRequireController implements IController {

    public static $inject: ReadonlyArray<string> = ['EinstellungRS', 'GesuchModelManager', '$translate', 'FinanzielleSituationRS'];

    public finanzielleSituationRequired: boolean;
    public sozialhilfeBezueger: boolean;
    public verguenstigungGewuenscht: boolean;
    public areThereAnyBgBetreuungen: boolean;
    public isFinSitTypFkjv: boolean = false;

    public maxMassgebendesEinkommen: number;

    public form: IFormController;

    public constructor(
        private readonly einstellungRS: EinstellungRS,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly $translate: ITranslateService,
        private readonly finanzielleSituationRS: FinanzielleSituationRS
    ) {
    }

    public $onInit(): void {
        this.setFinanziellesituationRequired();
        // Den Parameter fuer das Maximale Einkommen lesen
        this.einstellungRS.findEinstellung(TSEinstellungKey.MAX_MASSGEBENDES_EINKOMMEN,
            this.gesuchModelManager.getDossier().gemeinde.id,
            this.gesuchModelManager.getGesuchsperiode().id)
            .subscribe(response => {
                this.maxMassgebendesEinkommen = parseInt(response.value, 10);
            }, error => LOG.error(error));

        this.finanzielleSituationRS.getFinanzielleSituationTyp(this.gesuchModelManager.getGesuchsperiode(),
            this.gesuchModelManager.getGemeinde())
            .subscribe(typ => {
                this.isFinSitTypFkjv = TSFinanzielleSituationTyp.BERN_FKJV === typ;
            }, err => LOG.error(err));
    }

    /**
     * Das Feld verguenstigungGewuenscht wird nur angezeigt,
     * wenn das Feld sozialhilfeBezueger eingeblendet ist und mit nein beantwortet wurde und
     * wenn es sich um keinen reinen BG-Antrag in der FKJV FinSit handelt
     */
    public showFinanzielleSituationDeklarieren(): boolean {
        const isNotSozialhilfeBezueger = EbeguUtil.isNotNullOrUndefined(this.sozialhilfeBezueger)
            && !this.sozialhilfeBezueger;

        if (this.isFinSitTypFkjv) {
            if (isNotSozialhilfeBezueger && !this.areThereAnyBgBetreuungen) {
                return true;
            }
            this.verguenstigungGewuenscht = true;
            this.setFinanziellesituationRequired();
            return false;
        }

        return isNotSozialhilfeBezueger;
    }

    public setFinanziellesituationRequired(): void {
        const required = EbeguUtil.isFinanzielleSituationRequired(this.sozialhilfeBezueger,
            this.verguenstigungGewuenscht);
        // Wenn es sich geändert und nicht den Initialwert "undefined" hat, müssen gewisse Daten gesetzt werden
        if (EbeguUtil.isNotNullOrUndefined(this.finanzielleSituationRequired) &&
            required !== this.finanzielleSituationRequired &&
            this.gesuchModelManager.getGesuch()) {
            this.gesuchModelManager.getGesuch().finSitStatus = required ? null : TSFinSitStatus.AKZEPTIERT;
        }
        this.finanzielleSituationRequired = required;
    }

    public getMaxMassgebendesEinkommen(): string {
        const pipe = new EbeguNumberPipe();
        return pipe.transform(this.maxMassgebendesEinkommen);
    }

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }

    public isKorrekturModusJugendamt(): boolean {
        return this.gesuchModelManager.isKorrekturModusJugendamt();
    }

    public getLabel(): string {
        const key = this.gesuchModelManager.isFKJVTexte ? 'FINANZIELLE_SITUATION_VERGUENSTIGUNG_GEWUENSCHT_FKJV' : 'FINANZIELLE_SITUATION_VERGUENSTIGUNG_GEWUENSCHT';
        return this.$translate.instant(key,
            {maxEinkommen: this.maxMassgebendesEinkommen});
    }

    public getLabelNo(): string {
        if (this.gesuchModelManager.isFKJVTexte) {
            return 'FINANZIELLE_SITUATION_VERGUENSTIGUNG_GEWUENSCHT_NEIN_FKJV';
        }
        return 'FINANZIELLE_SITUATION_VERGUENSTIGUNG_GEWUENSCHT_NEIN';
    }
}
