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
import {EbeguVorlageRS} from '../../../../admin/service/ebeguVorlageRS.rest';
import {RemoveDialogController} from '../../../../gesuch/dialog/RemoveDialogController';
import TSDownloadFile from '../../../../models/TSDownloadFile';
import TSEbeguVorlage from '../../../../models/TSEbeguVorlage';
import TSGesuchsperiode from '../../../../models/TSGesuchsperiode';
import EbeguUtil from '../../../../utils/EbeguUtil';
import {DvDialog} from '../../directive/dv-dialog/dv-dialog';
import {DownloadRS} from '../../service/downloadRS.rest';
import ILogService = angular.ILogService;
import IScope = angular.IScope;

const removeDialogTemplate = require('../../../../gesuch/dialog/removeDialogTemplate.html');

export class DVVorlageListConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {
        ebeguVorlageList: '<',
        isReadonly: '&',
        gesuchsperiode: '<',
        proGesuchsperiode: '<'
    };
    public template = require('./dv-vorlage-list.html');
    public controller = DVVorlageListController;
    public controllerAs = 'vm';
}

export class DVVorlageListController implements IController {

    public static $inject: ReadonlyArray<string> = ['DownloadRS', '$log', 'EbeguVorlageRS', 'DvDialog',
        'EbeguUtil', '$scope'];

    public ebeguVorlageList: TSEbeguVorlage[];
    public isReadonly: () => void;
    public gesuchsperiode: TSGesuchsperiode;
    public proGesuchsperiode: boolean;

    public constructor(private readonly downloadRS: DownloadRS,
                       private readonly $log: ILogService,
                       private readonly ebeguVorlageRS: EbeguVorlageRS,
                       private readonly dvDialog: DvDialog,
                       private readonly ebeguUtil: EbeguUtil,
                       private readonly $scope: IScope) {
    }

    public $onInit(): void {
        this.updateVorlageList();
        if (!this.proGesuchsperiode) {
            return;
        }

        this.$scope.$watch(() => {
            return this.gesuchsperiode;
        }, (newValue, oldValue) => {
            if (newValue !== oldValue) {
                this.updateVorlageList();
            }
        });
    }

    private updateVorlageList(): void {
        if (this.proGesuchsperiode) {
            if (!this.gesuchsperiode) {
                return;
            }

            this.ebeguVorlageRS.getEbeguVorlagenByGesuchsperiode(this.gesuchsperiode.id)
                .then((response: TSEbeguVorlage[]) => {
                    this.ebeguVorlageList = response;
                });
        } else {
            this.ebeguVorlageRS.getEbeguVorlagenWithoutGesuchsperiode()
                .then((response: TSEbeguVorlage[]) => {
                    this.ebeguVorlageList = response;
                });
        }
    }

    public hasVorlage(selectVorlage: TSEbeguVorlage): boolean {
        return !!selectVorlage.vorlage;
    }

    public isListReadonly(): void {
        this.isReadonly();
    }

    public download(ebeguVorlage: TSEbeguVorlage, attachment: boolean): void {
        this.$log.debug('download vorlage ' + ebeguVorlage.vorlage.filename);
        const win = this.downloadRS.prepareDownloadWindow();

        this.downloadRS.getAccessTokenVorlage(ebeguVorlage.vorlage.id)
            .then((downloadFile: TSDownloadFile) => {
                this.$log.debug('accessToken: ' + downloadFile.accessToken);
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, attachment, win);
            })
            .catch(ex => EbeguUtil.handleDownloadError(win, ex));
    }

    public uploadAnhaenge(files: any[], selectEbeguVorlage: TSEbeguVorlage): void {
        this.$log.debug('Uploading files ');

        let gesuchsperiodeID;
        if (this.proGesuchsperiode && this.gesuchsperiode) {
            gesuchsperiodeID = this.gesuchsperiode.id;
        }
        this.ebeguVorlageRS.uploadVorlage(files[0], selectEbeguVorlage, gesuchsperiodeID, this.proGesuchsperiode)
            .then(response => this.addResponseToCurrentList(response));
    }

    private addResponseToCurrentList(response: TSEbeguVorlage): void {
        const returnedDG = angular.copy(response);
        const index = this.getIndexOfElement(returnedDG, this.ebeguVorlageList);

        if (index > -1) {
            this.ebeguVorlageList[index] = returnedDG;
        }
        EbeguUtil.handleSmarttablesUpdateBug(this.ebeguVorlageList);
    }

    private getIndexOfElement(entityToSearch: TSEbeguVorlage, listToSearchIn: TSEbeguVorlage[]): number {
        const idToSearch = entityToSearch.name;
        for (let i = 0; i < listToSearchIn.length; i++) {
            if (listToSearchIn[i].name === idToSearch) {
                return i;
            }
        }
        return -1;
    }

    public remove(ebeguVorlage: TSEbeguVorlage): void {
        this.$log.debug('component -> remove dokument ' + ebeguVorlage.vorlage.filename);
        this.dvDialog.showRemoveDialog(removeDialogTemplate, undefined, RemoveDialogController, {
            deleteText: '',
            title: 'FILE_LOESCHEN',
            parentController: undefined,
            elementID: undefined
        })
            .then(() => {   // User confirmed removal

                this.ebeguVorlageRS.deleteEbeguVorlage(ebeguVorlage.id).then(() => {

                    const index = EbeguUtil.getIndexOfElementwithID(ebeguVorlage, this.ebeguVorlageList);
                    if (index <= -1) {
                        return;
                    }

                    this.$log.debug('remove Vorlage in EbeguVorlage');
                    ebeguVorlage.vorlage = null;
                    this.ebeguVorlageList[index] = ebeguVorlage;
                });
                EbeguUtil.handleSmarttablesUpdateBug(this.ebeguVorlageList);
            });
    }
}
