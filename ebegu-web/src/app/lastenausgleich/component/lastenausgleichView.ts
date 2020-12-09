/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {IComponentOptions, IController} from 'angular';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {RemoveDialogController} from '../../../gesuch/dialog/RemoveDialogController';
import {TSRole} from '../../../models/enums/TSRole';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSLastenausgleich} from '../../../models/TSLastenausgleich';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvDialog} from '../../core/directive/dv-dialog/dv-dialog';
import {LogFactory} from '../../core/logging/LogFactory';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {LastenausgleichRS} from '../../core/service/lastenausgleichRS.rest';
import IFormController = angular.IFormController;
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTemplate = require('../../../gesuch/dialog/removeDialogTemplate.html');

const LOG = LogFactory.createLog('LastenausgleichViewController');

export class LastenausgleichViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./lastenausgleichView.html');
    public controller = LastenausgleichViewController;
    public controllerAs = 'vm';
}

export class LastenausgleichViewController implements IController {

    public static $inject: string[] = [
        'LastenausgleichRS',
        'DvDialog',
        '$translate',
        'DownloadRS',
        'AuthServiceRS',
    ];

    public jahr: number;
    public selbstbehaltPro100ProzentPlatz: number;
    public lastenausgleiche: TSLastenausgleich[] = [];

    public form: IFormController;

    public constructor(
        private readonly lastenausgleichRS: LastenausgleichRS,
        private readonly dvDialog: DvDialog,
        private readonly $translate: ITranslateService,
        private readonly downloadRS: DownloadRS,
        private readonly authServiceRS: AuthServiceRS,
    ) {
    }

    public $onInit(): void {
        const dataPromise = this.authServiceRS.getPrincipal().hasRole(TSRole.SACHBEARBEITER_GEMEINDE) ?
            this.lastenausgleichRS.getGemeindeLastenausgleiche() : this.lastenausgleichRS.getAllLastenausgleiche();

        dataPromise.then((response: TSLastenausgleich[]) => {
            this.lastenausgleiche = response;
        });
    }

    public createLastenausgleich(): void {
        if (!this.form.$valid) {
            return;
        }
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            title: this.$translate.instant('LASTENAUSGLEICH_ERSTELLEN_TITLE'),
            deleteText: this.$translate.instant('LASTENAUSGLEICH_ERSTELLEN_INFO'),
            parentController: undefined,
            elementID: undefined,
        }).then(() => {
            this.lastenausgleichRS.createLastenausgleich(this.jahr, this.selbstbehaltPro100ProzentPlatz)
                .then((response: TSLastenausgleich) => {
                    this.lastenausgleiche.push(response);
                });
        }, err => {
            LOG.error(err);
        });
    }

    public downloadExcel(lastenausgleich: TSLastenausgleich): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.lastenausgleichRS.getLastenausgleichReportExcel(lastenausgleich.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(() => {
                win.close();
            });
    }

    public downloadCsv(lastenausgleich: TSLastenausgleich): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.lastenausgleichRS.getLastenausgleichReportCSV(lastenausgleich.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(() => {
                win.close();
            });
    }

    public isRemoveAllowed(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }


    public canDownloadCSV(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }

    public removeLastenausgleich(lastenausgleich: TSLastenausgleich): void {
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            title: 'LASTENAUSGLEICH_LOESCHEN_DIALOG_TITLE',
            deleteText: 'LASTENAUSGLEICH_LOESCHEN_DIALOG_TEXT',
            parentController: this,
        }).then(() => {   // User confirmed removal
            this.lastenausgleichRS.removeLastenausgleich(lastenausgleich.id).then(() => {
                this.$onInit();
            });
        });
    }
}
