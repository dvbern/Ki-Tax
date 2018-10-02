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

import IComponentOptions = angular.IComponentOptions;
import IFormController = angular.IFormController;
import {StateService} from '@uirouter/core';
import {IController} from 'angular';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import GesuchRS from '../../../../gesuch/service/gesuchRS.rest';
import TSAntragStatusHistory from '../../../../models/TSAntragStatusHistory';
import TSDossier from '../../../../models/TSDossier';
import TSGesuch from '../../../../models/TSGesuch';
import EbeguUtil from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import AntragStatusHistoryRS from '../../../core/service/antragStatusHistoryRS.rest';
import {IVerlaufStateParams} from '../../verlauf.route';

export class VerlaufViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./verlaufView.html');
    public controller = VerlaufViewController;
    public controllerAs = 'vm';
}

export class VerlaufViewController implements IController {

    public static $inject: string[] = ['$state', '$stateParams', 'AuthServiceRS', 'GesuchRS', 'AntragStatusHistoryRS',
        'EbeguUtil'];

    public form: IFormController;
    public dossier: TSDossier;
    public gesuche: { [gesuchId: string]: string } = {};
    public itemsByPage: number = 20;
    public readonly TSRoleUtil = TSRoleUtil;
    public verlauf: Array<TSAntragStatusHistory>;

    public constructor(private readonly $state: StateService,
                       private readonly $stateParams: IVerlaufStateParams,
                       private readonly authServiceRS: AuthServiceRS,
                       private readonly gesuchRS: GesuchRS,
                       private readonly antragStatusHistoryRS: AntragStatusHistoryRS,
                       private readonly ebeguUtil: EbeguUtil) {
    }

    public $onInit(): void {
        if (!this.$stateParams.gesuchId) {
            this.cancel();
            return;
        }

        this.gesuchRS.findGesuch(this.$stateParams.gesuchId).then((gesuchResponse: TSGesuch) => {
            this.dossier = gesuchResponse.dossier;
            const gesuchsperiode = gesuchResponse.gesuchsperiode;
            if (this.dossier === undefined) {
                this.cancel();
            }
            this.antragStatusHistoryRS.loadAllAntragStatusHistoryByGesuchsperiode(this.dossier, gesuchsperiode)
                .then((response: TSAntragStatusHistory[]) => {
                    this.verlauf = response;
                });
            this.gesuchRS.getAllAntragDTOForDossier(this.dossier.id).then(response => {
                response.forEach(item => {
                    this.gesuche[item.antragId] = this.ebeguUtil.getAntragTextDateAsString(
                        item.antragTyp,
                        item.eingangsdatum,
                        item.laufnummer
                    );
                });
            });
        });
    }

    public getVerlaufList(): Array<TSAntragStatusHistory> {
        return this.verlauf;
    }

    public getFallId(): string {
        if (this.dossier && this.dossier.fall) {
            return this.dossier.fall.id;
        }
        return '';
    }

    public cancel(): void {
        this.$state.go('pendenzen.list-view');
    }

    public getGesuch(gesuchid: string): TSGesuch {
        this.gesuchRS.findGesuch(gesuchid).then(response => {
            return response;
        });
        return undefined;
    }
}
