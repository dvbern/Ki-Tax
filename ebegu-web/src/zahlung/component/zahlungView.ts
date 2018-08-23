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

import {StateService} from '@uirouter/core';
import {IComponentOptions, IController} from 'angular';
import {DownloadRS} from '../../app/core/service/downloadRS.rest';
import {ReportRS} from '../../app/core/service/reportRS.rest';
import ZahlungRS from '../../app/core/service/zahlungRS.rest';
import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../models/enums/TSRole';
import {TSZahlungsstatus} from '../../models/enums/TSZahlungsstatus';
import TSDownloadFile from '../../models/TSDownloadFile';
import TSZahlung from '../../models/TSZahlung';
import EbeguUtil from '../../utils/EbeguUtil';
import {IZahlungsauftragStateParams} from '../zahlung.route';

export class ZahlungViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = require('./zahlungView.html');
    controller = ZahlungViewController;
    controllerAs = 'vm';
}

export class ZahlungViewController implements IController {

    static $inject: string[] = ['ZahlungRS', 'CONSTANTS', '$stateParams', '$state', 'DownloadRS', 'ReportRS', 'AuthServiceRS'];

    private zahlungen: Array<TSZahlung>;

    itemsByPage: number = 20;

    constructor(private readonly zahlungRS: ZahlungRS,
                private readonly CONSTANTS: any,
                private readonly $stateParams: IZahlungsauftragStateParams,
                private readonly $state: StateService,
                private readonly downloadRS: DownloadRS,
                private readonly reportRS: ReportRS,
                private readonly authServiceRS: AuthServiceRS) {
    }

    public $onInit() {
        if (this.$stateParams.zahlungsauftragId && this.authServiceRS.getPrincipal()) {
            switch (this.authServiceRS.getPrincipal().getCurrentRole()) {
                case TSRole.ADMIN_INSTITUTION:
                case TSRole.SACHBEARBEITER_INSTITUTION:
                case TSRole.ADMIN_TRAEGERSCHAFT:
                case TSRole.SACHBEARBEITER_TRAEGERSCHAFT:
                    this.zahlungRS.getZahlungsauftragInstitution(this.$stateParams.zahlungsauftragId).then((response) => {
                        this.zahlungen = response.zahlungen;
                    });
                    break;
                case TSRole.SUPER_ADMIN:
                case TSRole.ADMIN_BG:
                case TSRole.SACHBEARBEITER_BG:
                case TSRole.ADMIN_GEMEINDE:
                case TSRole.SACHBEARBEITER_GEMEINDE:
                case TSRole.JURIST:
                case TSRole.REVISOR:
                    this.zahlungRS.getZahlungsauftrag(this.$stateParams.zahlungsauftragId).then((response) => {
                        this.zahlungen = response.zahlungen;
                    });
                    break;
                default:
                    break;
            }
        }
    }

    private gotToUebersicht(): void {
        this.$state.go('zahlungsauftrag.view');
    }

    public downloadDetails(zahlung: TSZahlung) {
        const win: Window = this.downloadRS.prepareDownloadWindow();
        this.reportRS.getZahlungReportExcel(zahlung.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(() => {
                win.close();
            });
    }

    public bestaetigen(zahlung: TSZahlung) {
        console.log('bestaetigen');
        this.zahlungRS.zahlungBestaetigen(zahlung.id).then((response: TSZahlung) => {
            const index = EbeguUtil.getIndexOfElementwithID(response, this.zahlungen);
            if (index > -1) {
                this.zahlungen[index] = response;
            }
            EbeguUtil.handleSmarttablesUpdateBug(this.zahlungen);
        });
    }

    // noinspection JSMethodCanBeStatic
    public isBestaetigt(zahlungstatus: TSZahlungsstatus): boolean {
        return zahlungstatus === TSZahlungsstatus.BESTAETIGT;
    }
}
