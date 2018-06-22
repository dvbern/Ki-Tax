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
import ILogService = angular.ILogService;
import ITimeoutService = angular.ITimeoutService;
import {StateService} from '@uirouter/core';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import BetreuungRS from '../../../core/service/betreuungRS.rest';
import {DownloadRS} from '../../../core/service/downloadRS.rest';
import DossierRS from '../../../gesuch/service/dossierRS.rest';
import {TSBetreuungsstatus} from '../../../models/enums/TSBetreuungsstatus';
import TSAntragStatusHistory from '../../../models/TSAntragStatusHistory';
import TSBetreuung from '../../../models/TSBetreuung';
import TSDossier from '../../../models/TSDossier';
import TSDownloadFile from '../../../models/TSDownloadFile';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {IAlleVerfuegungenStateParams} from '../../alleVerfuegungen.route';

let template = require('./alleVerfuegungenView.html');
require('./alleVerfuegungenView.less');

export class AlleVerfuegungenViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = AlleVerfuegungenViewController;
    controllerAs = 'vm';
}

export class AlleVerfuegungenViewController {

    dossier: TSDossier;
    alleVerfuegungen: Array<any> = [];
    itemsByPage: number = 20;
    TSRoleUtil = TSRoleUtil;

    static $inject: string[] = ['$state', '$stateParams', 'AuthServiceRS', 'BetreuungRS',
        'DownloadRS', '$log', '$timeout', 'DossierRS'];

    /* @ngInject */
    constructor(private $state: StateService, private $stateParams: IAlleVerfuegungenStateParams,
                private authServiceRS: AuthServiceRS, private betreuungRS: BetreuungRS, private downloadRS: DownloadRS,
                private $log: ILogService, private $timeout: ITimeoutService, private dossierRS: DossierRS) {
    }

    $onInit() {
        if (this.$stateParams.dossierId) {
            this.dossierRS.findDossier(this.$stateParams.dossierId).then((response: TSDossier) => {
                this.dossier = response;
                if (this.dossier === undefined) {
                    this.cancel();
                }
                this.betreuungRS.findAllBetreuungenWithVerfuegungForDossier(this.dossier.id).then((response) => {
                    response.forEach((item) => {
                        this.alleVerfuegungen.push(item);
                    });
                    this.alleVerfuegungen = response;
                });
            });
        } else {
            this.cancel();
        }
    }

    public getAlleVerfuegungen(): Array<TSAntragStatusHistory> {
        return this.alleVerfuegungen;
    }

    public openVerfuegung(betreuungNummer: string, kindNummer: number, gesuchId: string): void {
        if (betreuungNummer && kindNummer && gesuchId) {
            this.$state.go('gesuch.verfuegenView', {
                betreuungNumber: betreuungNummer,
                kindNumber: kindNummer,
                gesuchId: gesuchId
            });
        }
    }

    public cancel(): void {
        if (this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getGesuchstellerOnlyRoles())) {
            this.$state.go('gesuchstellerDashboard');
        } else {
            this.$state.go('pendenzen');
        }
    }

    public showVerfuegungPdfLink(betreuung: TSBetreuung): boolean {
        return !(TSBetreuungsstatus.NICHT_EINGETRETEN === betreuung.betreuungsstatus);
    }

    public openVerfuegungPDF(betreuung: TSBetreuung): void {
        let win: Window = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getAccessTokenVerfuegungGeneratedDokument(betreuung.gesuchId,
            betreuung.id, false, '')
            .then((downloadFile: TSDownloadFile) => {
                this.$log.debug('accessToken: ' + downloadFile.accessToken);
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch((ex) => {
                win.close();
                this.$log.error('An error occurred downloading the document, closing download window.');
            });
    }

    public openNichteintretenPDF(betreuung: TSBetreuung): void {
        let win: Window = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getAccessTokenNichteintretenGeneratedDokument(betreuung.id, false)
            .then((downloadFile: TSDownloadFile) => {
                this.$log.debug('accessToken: ' + downloadFile.accessToken);
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch((ex) => {
                win.close();
                this.$log.error('An error occurred downloading the document, closing download window.');
            });
    }

    $postLink() {
        this.$timeout(() => {
            EbeguUtil.selectFirst();
        }, 500); // this is the only way because it needs a little until everything is loaded
    }
}
