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
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSFinSitStatus} from '../../../models/enums/TSFinSitStatus';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import ITranslateService = angular.translate.ITranslateService;

export class DvFinanzielleSituationRequire implements IComponentOptions {
    public transclude = false;
    public bindings = {
        sozialhilfeBezueger: '=',
        antragNurFuerBehinderungszuschlag: '=',
        finanzielleSituationRequired: '=',
        form: '=',
    };
    public template = require('./dv-finanzielle-situation-require.html');
    public controller = DVFinanzielleSituationRequireController;
    public controllerAs = 'vm';
}

export class DVFinanzielleSituationRequireController implements IController {

    public static $inject: ReadonlyArray<string> = ['EinstellungRS', 'GesuchModelManager', '$translate'];

    public finanzielleSituationRequired: boolean;
    public sozialhilfeBezueger: boolean;
    public antragNurFuerBehinderungszuschlag: boolean;

    public maxMassgebendesEinkommen: string;

    public form: IFormController;

    public constructor(
        private readonly einstellungRS: EinstellungRS,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly $translate: ITranslateService,
    ) {
    }

    public $onInit(): void {
        this.setFinanziellesituationRequired();
        // Den Parameter fuer das Maximale Einkommen lesen
        this.einstellungRS.findEinstellung(TSEinstellungKey.MAX_MASSGEBENDES_EINKOMMEN,
            this.gesuchModelManager.getDossier().gemeinde.id,
            this.gesuchModelManager.getGesuchsperiode().id)
            .then(response => {
                this.maxMassgebendesEinkommen = response.value;
            });
    }

    /**
     * Das Feld antragNurFuerBehinderungszuschlag wird nur angezeigt, wenn das Feld sozialhilfeBezueger eingeblendet ist und mit
     * nein beantwortet wurde UND wenn mindestens eine Betreuung mit besonderem Betreuungsaufwand zu irgendeinem Zeitpunkt
     * erfasst war.
     */
    public showNurPauschaleFuerBesondereBeduerfnisseGewuenscht(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.sozialhilfeBezueger)
            && !this.sozialhilfeBezueger
            && this.gesuchModelManager.getGesuch()
            && this.gesuchModelManager.getGesuch().extractFamiliensituation()
            && this.gesuchModelManager.getGesuch().extractFamiliensituation().behinderungszuschlagFuerMindEinKindEinmalBeantragt;
    }

    public setFinanziellesituationRequired(): void {
        const required = EbeguUtil.isFinanzielleSituationRequired(this.sozialhilfeBezueger, this.antragNurFuerBehinderungszuschlag);
        // Wenn es sich geändert hat, müssen gewisse Daten gesetzt werden
        if (required !== this.finanzielleSituationRequired && this.gesuchModelManager.getGesuch()) {
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

    public isKorrekturModusJugendamt(): boolean {
        return this.gesuchModelManager.isKorrekturModusJugendamt();
    }

    public getFalseOptionLabel(): string {
        return this.$translate.instant('FINANZIELLE_SITUATION_NUR_BEHINDERUNGSZUSCHLAG_GEWUENSCHT_NEIN',
            {maxEinkommen: this.maxMassgebendesEinkommen});
    }
}
