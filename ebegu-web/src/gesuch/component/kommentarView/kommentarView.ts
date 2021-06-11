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
import {IComponentOptions, IController, IFormController, ILogService, IPromise, IQService} from 'angular';
import {Subscription} from 'rxjs';
import {MAX_FILE_SIZE} from '../../../app/core/constants/CONSTANTS';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {DownloadRS} from '../../../app/core/service/downloadRS.rest';
import {UploadRS} from '../../../app/core/service/uploadRS.rest';
import {TSDokumenteDTO} from '../../../models/dto/TSDokumenteDTO';
import {TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {TSCacheTyp} from '../../../models/enums/TSCacheTyp';
import {TSDokumentGrundTyp} from '../../../models/enums/TSDokumentGrundTyp';
import {TSEingangsart} from '../../../models/enums/TSEingangsart';
import {TSDokument} from '../../../models/TSDokument';
import {TSDokumentGrund} from '../../../models/TSDokumentGrund';
import {TSGesuch} from '../../../models/TSGesuch';
import {TSWizardStep} from '../../../models/TSWizardStep';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {OkHtmlDialogController} from '../../dialog/OkHtmlDialogController';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {DokumenteRS} from '../../service/dokumenteRS.rest';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {GesuchRS} from '../../service/gesuchRS.rest';
import {GlobalCacheService} from '../../service/globalCacheService';
import {WizardStepManager} from '../../service/wizardStepManager';
import {InternePendenzenRS} from '../internePendenzenView/internePendenzenRS';
import ISidenavService = angular.material.ISidenavService;
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
export class KommentarViewController implements IController {

    public static $inject: string[] = [
        '$log',
        'GesuchModelManager',
        'GesuchRS',
        'DokumenteRS',
        'DownloadRS',
        'UploadRS',
        'WizardStepManager',
        'GlobalCacheService',
        'DvDialog',
        '$translate',
        '$state',
        '$mdSidenav',
        '$q',
        'applicationPropertyRS',
        'InternePendenzenRS'
    ];

    public form: IFormController;
    public dokumentePapiergesuch: TSDokumentGrund;
    public readonly TSRoleUtil = TSRoleUtil;
    public isPersonensucheDisabled: boolean = true;
    public numberInternePendenzen: number;
    private subscription: Subscription;

    public constructor(
        private readonly $log: ILogService,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly gesuchRS: GesuchRS,
        private readonly dokumenteRS: DokumenteRS,
        private readonly downloadRS: DownloadRS,
        private readonly uploadRS: UploadRS,
        private readonly wizardStepManager: WizardStepManager,
        private readonly globalCacheService: GlobalCacheService,
        private readonly dvDialog: DvDialog,
        private readonly $translate: ITranslateService,
        private readonly $state: StateService,
        private readonly $mdSidenav: ISidenavService,
        private readonly $q: IQService,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly internePendenzenRS: InternePendenzenRS
    ) {

        if (!this.isGesuchUnsaved()) {
            this.getPapiergesuchFromServer();
        }
        this.applicationPropertyRS.isPersonensucheDisabled().then((response: any) => {
            this.isPersonensucheDisabled = response;
        });
        this.getNumberInternePendenzen();
    }

    public $onDestroy(): void {
        this.subscription.unsubscribe();
    }

    private getPapiergesuchFromServer(): IPromise<TSDokumenteDTO> {
        if (!this.getGesuch()) {
            return this.$q.resolve(undefined);
        }
        return this.dokumenteRS.getDokumenteByTypeCached(
            this.getGesuch(),
            TSDokumentGrundTyp.PAPIERGESUCH,
            this.globalCacheService.getCache(TSCacheTyp.EBEGU_DOCUMENT))
            .then((promiseValue: TSDokumenteDTO) => {
                // it could also has no Papiergesuch at all
                if (promiseValue.dokumentGruende.length === 1) {
                    this.dokumentePapiergesuch = promiseValue.dokumentGruende[0];
                } else if (promiseValue.dokumentGruende.length > 1) {
                    this.$log.error(
                        `Falsche anzahl Dokumente beim Laden vom Papiergesuch. Es sollte 1 sein, ist aber
                        ${promiseValue.dokumentGruende.length}`);
                }
                return promiseValue;
            });
    }

    private getNumberInternePendenzen(): void {
        if (!this.getGesuch()) {
            return;
        }
        this.subscription = this.internePendenzenRS.getPendenzCountUpdated$(this.getGesuch())
            .subscribe(() => {
                this.internePendenzenRS.countInternePendenzenForGesuch(this.getGesuch())
                    .subscribe(numberInternePendenzen => this.numberInternePendenzen = numberInternePendenzen,
                        error => this.$log.error(error));
            }, error => this.$log.error(error));
    }

    public getGesuch(): TSGesuch {
        return this.gesuchModelManager.getGesuch();
    }

    public toggleEwkSidenav(): void {
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

    public isPapiergesuch(): boolean {
        return this.getGesuch() ? this.getGesuch().eingangsart === TSEingangsart.PAPIER : false;
    }

    public hasPapiergesuch(): boolean {
        return !!(this.dokumentePapiergesuch
            && this.dokumentePapiergesuch.dokumente
            && this.dokumentePapiergesuch.dokumente.length !== 0
            && this.dokumentePapiergesuch.dokumente[0].filename);
    }

    public download(): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.getPapiergesuchFromServer().then(() => {
            if (!this.hasPapiergesuch()) {
                this.$log.error('Kein Papiergesuch für Download vorhanden!');
                return;
            }

            const newest = this.getNewest(this.dokumentePapiergesuch.dokumente);
            this.downloadRS.getAccessTokenDokument(newest.id)
                .then(response => {
                    const tempDokument = angular.copy(response);
                    this.downloadRS.startDownload(tempDokument.accessToken, newest.filename, false, win);
                })
                .catch(ex => EbeguUtil.handleDownloadError(win, ex));
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

    public upload(files: any[]): void {
        this.getPapiergesuchFromServer().then(() => {
            if (this.hasPapiergesuch()) {
                this.$log.error('Papiergesuch schon vorhanden');
                return;
            }
            const gesuchID = this.getGesuch().id;
            console.log('Uploading files on gesuch ' + gesuchID);

            const filesTooBig: any[] = [];
            const filesOk: any[] = [];
            this.$log.debug('Uploading files on gesuch ' + gesuchID);
            for (const file of files) {
                this.$log.debug(`File: ${file.name} size: ${file.size}`);
                if (file.size > MAX_FILE_SIZE) {
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
                    title: returnString,
                });
            }

            if (filesOk.length <= 0) {
                return;
            }

            this.uploadRS.uploadFile(filesOk, this.dokumentePapiergesuch, gesuchID).then(response => {
                this.dokumentePapiergesuch = angular.copy(response);
                this.globalCacheService.getCache(TSCacheTyp.EBEGU_DOCUMENT).removeAll();
            });
        });
    }

    public isGesuchUnsaved(): boolean {
        return this.getGesuch() && this.getGesuch().isNew();
    }

    public getCurrentWizardStep(): TSWizardStep {
        return this.wizardStepManager.getCurrentStep();
    }

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }

    public isInBearbeitungSTV(): boolean {
        return  this.getGesuch() ? (this.getGesuch().status === TSAntragStatus.IN_BEARBEITUNG_STV) : false;
    }

    public freigebenSTV(): void {
        this.dvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'ZURUECK_AN_GEMEINDE_TITLE',
            deleteText: 'ZURUECK_AN_GEMEINDE',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {
            return this.gesuchRS.gesuchBySTVFreigeben(this.getGesuch().id).then((gesuch: TSGesuch) => {
                this.gesuchModelManager.setGesuch(gesuch);
                this.$state.go('pendenzenSteueramt.list-view');
            });
        });
    }

    public showBemerkungenPruefungSTV(): boolean {
        return this.getGesuch() && (
            this.getGesuch().geprueftSTV
            || this.getGesuch().status === TSAntragStatus.PRUEFUNG_STV
            || this.getGesuch().status === TSAntragStatus.IN_BEARBEITUNG_STV
            || this.getGesuch().status === TSAntragStatus.GEPRUEFT_STV);
    }

    public getFreigabeName(): string {
        return this.$translate.instant('ZURUECK_AN_GEMEINDE_TITLE');
    }

    public showGeresAbfrage(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.isPersonensucheDisabled) && !this.isPersonensucheDisabled;
    }
}
