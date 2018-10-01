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
import {IComponentOptions, IFormController, ILogService} from 'angular';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {DownloadRS} from '../../../app/core/service/downloadRS.rest';
import GesuchstellerRS from '../../../app/core/service/gesuchstellerRS.rest';
import {UploadRS} from '../../../app/core/service/uploadRS.rest';
import TSDokumenteDTO from '../../../models/dto/TSDokumenteDTO';
import {TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {TSCacheTyp} from '../../../models/enums/TSCacheTyp';
import {TSDokumentGrundTyp} from '../../../models/enums/TSDokumentGrundTyp';
import TSDokument from '../../../models/TSDokument';
import TSDokumentGrund from '../../../models/TSDokumentGrund';
import TSDownloadFile from '../../../models/TSDownloadFile';
import TSGesuch from '../../../models/TSGesuch';
import TSWizardStep from '../../../models/TSWizardStep';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {OkHtmlDialogController} from '../../dialog/OkHtmlDialogController';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import DokumenteRS from '../../service/dokumenteRS.rest';
import GesuchModelManager from '../../service/gesuchModelManager';
import GesuchRS from '../../service/gesuchRS.rest';
import GlobalCacheService from '../../service/globalCacheService';
import WizardStepManager from '../../service/wizardStepManager';
import IPromise = angular.IPromise;
import IQService = angular.IQService;
import IRootScopeService = angular.IRootScopeService;
import ITranslateService = angular.translate.ITranslateService;

const okHtmlDialogTempl = require('../../../gesuch/dialog/okHtmlDialogTemplate.html');
const removeDialogTempl = require('../../dialog/removeDialogTemplate.html');

export class KommentarViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./kommentarView.html');
    public controller = KommentarViewController;
    public controllerAs = 'vm';
}

/**
 * Controller fuer den Kommentare
 */
export class KommentarViewController {

    public static $inject: string[] = ['$log', 'GesuchModelManager', 'GesuchRS', 'DokumenteRS', 'DownloadRS', '$q', 'UploadRS',
        'WizardStepManager', 'GlobalCacheService', 'DvDialog', '$translate', '$window', 'GesuchstellerRS', '$rootScope', '$state', '$mdSidenav'];

    public form: IFormController;
    public dokumentePapiergesuch: TSDokumentGrund;
    public TSRoleUtil = TSRoleUtil;

    public constructor(private readonly $log: ILogService, private readonly gesuchModelManager: GesuchModelManager, private readonly gesuchRS: GesuchRS,
                private readonly dokumenteRS: DokumenteRS, private readonly downloadRS: DownloadRS, private readonly $q: IQService,
                private readonly uploadRS: UploadRS, private readonly wizardStepManager: WizardStepManager, private readonly globalCacheService: GlobalCacheService,
                private readonly dvDialog: DvDialog, private readonly $translate: ITranslateService, private readonly $window: ng.IWindowService, private readonly gesuchstellerRS: GesuchstellerRS,
                private readonly $rootScope: IRootScopeService, private readonly $state: StateService, private readonly $mdSidenav: ng.material.ISidenavService) {

        if (!this.isGesuchUnsaved()) {
            this.getPapiergesuchFromServer();
        }
    }

    private getPapiergesuchFromServer(): IPromise<TSDokumenteDTO> {

        return this.dokumenteRS.getDokumenteByTypeCached(
            this.getGesuch(), TSDokumentGrundTyp.PAPIERGESUCH, this.globalCacheService.getCache(TSCacheTyp.EBEGU_DOCUMENT))
            .then((promiseValue: TSDokumenteDTO) => {

                if (promiseValue.dokumentGruende.length === 1) {
                    this.dokumentePapiergesuch = promiseValue.dokumentGruende[0];
                } else {
                    console.log('Falsche anzahl Dokumente');
                }
                return promiseValue;
            });
    }

    public getGesuch(): TSGesuch {
        return this.gesuchModelManager.getGesuch();
    }

    public toggleEwkSidenav() {
        this.$mdSidenav('ewk').toggle();
    }

    public saveBemerkungZurVerfuegung(): void {
        if (!this.isGesuchUnsaved()) {
            // Bemerkungen auf dem Gesuch werden nur gespeichert, wenn das gesuch schon persisted ist!
            this.gesuchRS.updateBemerkung(this.getGesuch().id, this.getGesuch().bemerkungen);
        }
    }

    public saveBemerkungPruefungSTV(): void {
        if (!this.isGesuchUnsaved()) {
            // Bemerkungen auf dem Gesuch werden nur gespeichert, wenn das gesuch schon persisted ist!
            this.gesuchRS.updateBemerkungPruefungSTV(this.getGesuch().id, this.getGesuch().bemerkungenPruefungSTV);
        }
    }

    public saveStepBemerkung(): void {
        if (!this.isGesuchUnsaved()) {
            this.wizardStepManager.updateCurrentWizardStep();
        }
    }

    public hasPapiergesuch(): boolean {
        if (this.dokumentePapiergesuch) {
            if (this.dokumentePapiergesuch.dokumente && this.dokumentePapiergesuch.dokumente.length !== 0) {
                if (this.dokumentePapiergesuch.dokumente[0].filename) {
                    return true;
                }
            }
        }
        return false;
    }

    public download() {
        const win = this.downloadRS.prepareDownloadWindow();
        this.getPapiergesuchFromServer().then((promiseValue: any) => {
            if (!this.hasPapiergesuch()) {
                this.$log.error('Kein Papiergesuch für Download vorhanden!');
            } else {
                const newest = this.getNewest(this.dokumentePapiergesuch.dokumente);
                this.downloadRS.getAccessTokenDokument(newest.id)
                    .then(response => {
                        const tempDokument = angular.copy(response);
                        this.downloadRS.startDownload(tempDokument.accessToken, newest.filename, false, win);
                    })
                    .catch(ex => {
                        win.close();
                        this.$log.error('An error occurred downloading the document, closing download window.');
                    });
            }
        });
    }

    private getNewest(dokumente: Array<TSDokument>): TSDokument {
        let newest = dokumente[0];
        // tslint:disable-next-line:prefer-for-of
        for (let i = 0; i < dokumente.length; i++) {
            if (dokumente[i].timestampErstellt.isAfter(newest.timestampErstellt)) {
                newest = dokumente[i];
            }
        }
        return newest;

    }

    public upload(files: any[]) {
        this.getPapiergesuchFromServer().then((promiseValue: any) => {
            if (this.hasPapiergesuch()) {
                this.$log.error('Papiergesuch schon vorhanden');
            } else {
                const gesuchID = this.getGesuch().id;
                console.log('Uploading files on gesuch ' + gesuchID);

                const filesTooBig: any[] = [];
                const filesOk: any[] = [];
                this.$log.debug('Uploading files on gesuch ' + gesuchID);
                for (const file of files) {
                    this.$log.debug('File: ' + file.name + ' size: ' + file.size);
                    if (file.size > 10000000) { // Maximale Filegrösse ist 10MB
                        filesTooBig.push(file);
                    } else {
                        filesOk.push(file);
                    }
                }

                if (filesTooBig.length > 0) {
                    // DialogBox anzeigen für Files, welche zu gross sind!
                    let returnString = this.$translate.instant('FILE_ZU_GROSS') + '<br/><br/>';
                    returnString += '<ul>';
                    for (const file of filesTooBig) {
                        returnString += '<li>';
                        returnString += file.name;
                        returnString += '</li>';
                    }
                    returnString += '</ul>';

                    this.dvDialog.showDialog(okHtmlDialogTempl, OkHtmlDialogController, {
                        title: returnString
                    });
                }

                if (filesOk.length > 0) {
                    this.uploadRS.uploadFile(filesOk, this.dokumentePapiergesuch, gesuchID).then(response => {
                        this.dokumentePapiergesuch = angular.copy(response);
                        this.globalCacheService.getCache(TSCacheTyp.EBEGU_DOCUMENT).removeAll();
                    });
                }
            }
        });
    }

    public isGesuchUnsaved(): boolean {
        return this.getGesuch().isNew();
    }

    public getCurrentWizardStep(): TSWizardStep {
        return this.wizardStepManager.getCurrentStep();
    }

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }

    public isInBearbeitungSTV(): boolean {
        return this.gesuchModelManager.getGesuch().status === TSAntragStatus.IN_BEARBEITUNG_STV;
    }

    public freigebenSTV(): void {
        this.dvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: this.getFreigabeTitel(),
            deleteText: this.getFreigabeBeschreibung(),
            parentController: undefined,
            elementID: undefined
        }).then(() => {
            return this.gesuchRS.gesuchBySTVFreigeben(this.getGesuch().id).then((gesuch: TSGesuch) => {
                this.gesuchModelManager.setGesuch(gesuch);
                this.$state.go('pendenzenSteueramt.list-view');
            });
        });
    }

    public showBemerkungenPruefungSTV(): boolean {
        return this.getGesuch().geprueftSTV || this.getGesuch().status === TSAntragStatus.PRUEFUNG_STV || this.getGesuch().status === TSAntragStatus.IN_BEARBEITUNG_STV
            || this.getGesuch().status === TSAntragStatus.GEPRUEFT_STV;
    }

    public getFreigabeName(): string {
        return this.$translate.instant(this.getFreigabeTitel());
    }

    public getFreigabeTitel(): string {
        if (this.getGesuch().areThereOnlySchulamtAngebote()) {
            return 'FREIGABE_SCH';
        }
        return 'FREIGABE_JA';
    }

    public getFreigabeBeschreibung(): string {
        if (this.getGesuch().areThereOnlySchulamtAngebote()) {
            return 'FREIGABE_SCH_BESCHREIBUNG';
        }
        return 'FREIGABE_JA_BESCHREIBUNG';
    }
}
