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

import {IComponentOptions, ILogService} from 'angular';
import TSDokumentGrund from '../../../models/TSDokumentGrund';
import {UploadRS} from '../../service/uploadRS.rest';
import GesuchModelManager from '../../../gesuch/service/gesuchModelManager';
import EbeguUtil from '../../../utils/EbeguUtil';
import TSDokument from '../../../models/TSDokument';
import {DownloadRS} from '../../service/downloadRS.rest';
import TSDownloadFile from '../../../models/TSDownloadFile';
import {DvDialog} from '../../directive/dv-dialog/dv-dialog';
import {RemoveDialogController} from '../../../gesuch/dialog/RemoveDialogController';
import WizardStepManager from '../../../gesuch/service/wizardStepManager';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {OkHtmlDialogController} from '../../../gesuch/dialog/OkHtmlDialogController';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {TSDokumentGrundPersonType} from '../../../models/enums/TSDokumentGrundPersonType';
import TSKindContainer from '../../../models/TSKindContainer';
import {TSRole} from '../../../models/enums/TSRole';
import {isAnyStatusOfVerfuegtButSchulamt} from '../../../models/enums/TSAntragStatus';
import {ApplicationPropertyRS} from '../../../admin/service/applicationPropertyRS.rest';
import TSApplicationProperty from '../../../models/TSApplicationProperty';
import ITranslateService = angular.translate.ITranslateService;

let template = require('./dv-dokumente-list.html');
let removeDialogTemplate = require('../../../gesuch/dialog/removeDialogTemplate.html');
require('./dv-dokumente-list.less');
let okHtmlDialogTempl = require('../../../gesuch/dialog/okHtmlDialogTemplate.html');

export class DVDokumenteListConfig implements IComponentOptions {
    transclude = false;

    bindings: any = {
        dokumente: '<',
        tableId: '@',
        tableTitle: '@',
        tag: '@',
        titleValue: '<',
        onUploadDone: '&',
        onRemove: '&',
        sonstige: '<'

    };
    template = template;
    controller = DVDokumenteListController;
    controllerAs = 'vm';
}

export class DVDokumenteListController {

    dokumente: TSDokumentGrund[];
    tableId: string;
    tableTitle: string;
    tag: string;
    titleValue: string;
    onUploadDone: (dokumentGrund: any) => void;
    onRemove: (attrs: any) => void;
    sonstige: boolean;
    allowedMimetypes: string = '';

    static $inject: any[] = ['UploadRS', 'GesuchModelManager', 'EbeguUtil', 'DownloadRS', 'DvDialog', 'WizardStepManager',
        '$log', 'AuthServiceRS', '$translate', '$window', 'ApplicationPropertyRS'];

    /* @ngInject */
    constructor(private uploadRS: UploadRS, private gesuchModelManager: GesuchModelManager, private ebeguUtil: EbeguUtil,
        private downloadRS: DownloadRS, private dvDialog: DvDialog, private wizardStepManager: WizardStepManager,
        private $log: ILogService, private authServiceRS: AuthServiceRS, private $translate: ITranslateService,
        private $window: ng.IWindowService, private applicationPropertyRS: ApplicationPropertyRS) {

    }

    $onInit() {
        this.applicationPropertyRS.getAllowedMimetypes().then((response: TSApplicationProperty) => {
            if (response !== undefined) {
                this.allowedMimetypes = response.value;
            }
        });

    }

    uploadAnhaenge(files: any[], selectDokument: TSDokumentGrund) {
        if (this.gesuchModelManager.getGesuch()) {
            let gesuchID = this.gesuchModelManager.getGesuch().id;
            let filesTooBig: any[] = [];
            let filesOk: any[] = [];
            this.$log.debug('Uploading files on gesuch ' + gesuchID);
            for (let file of files) {
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
                for (let file of filesTooBig) {
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
                this.uploadRS.uploadFile(filesOk, selectDokument, gesuchID).then((response) => {
                    let returnedDG: TSDokumentGrund = angular.copy(response);
                    this.wizardStepManager.findStepsFromGesuch(this.gesuchModelManager.getGesuch().id).then(() => {
                        this.handleUpload(returnedDG);
                    });
                });
            }
        } else {
            this.$log.warn('No gesuch found to store file or gesuch is status verfuegt');
        }
    }

    hasDokuments(selectDokument: TSDokumentGrund): boolean {
        if (selectDokument.dokumente) {
            for (let dokument of selectDokument.dokumente) {
                if (dokument.filename) {
                    return true;
                }
            }
        }
        return false;
    }

    handleUpload(returnedDG: TSDokumentGrund) {
        this.onUploadDone({dokument: returnedDG});
    }

    isRemoveAllowed(dokumentGrund: TSDokumentGrund, dokument: TSDokument): boolean {
        // Loeschen von Dokumenten ist nur in folgenden Faellen erlaubt:
        // - GS bis Freigabe (d.h nicht readonlyForRole). In diesem Status kann es nur "seine" Dokumente geben
        // - JA bis Verfuegen, aber nur die von JA hinzugefuegten: d.h. wenn noch nicht verfuegt: die eigenen, wenn readonly: nichts
        // - Admin: Auch nach verfuegen, aber nur die vom JA hinzugefuegten: wenn noch nicht verfuegt oder readonly: die eigenen
        // - Alle anderen Rollen: nichts
        let readonly: boolean = this.isGesuchReadonly();
        let roleLoggedIn: TSRole = this.authServiceRS.getPrincipalRole();
        let documentUploadedByAmt: boolean = true; // by default true in case there is no uploadUser
        if (dokument.userUploaded) {
            let roleDocumentUpload: TSRole = dokument.userUploaded.getCurrentRole();
            documentUploadedByAmt = (roleDocumentUpload === TSRole.SACHBEARBEITER_JA || roleDocumentUpload === TSRole.ADMIN
                || roleDocumentUpload === TSRole.SCHULAMT || roleDocumentUpload === TSRole.ADMINISTRATOR_SCHULAMT || roleDocumentUpload === TSRole.SUPER_ADMIN);
        }
        if (roleLoggedIn === TSRole.GESUCHSTELLER) {
            return !readonly;
        } else if (roleLoggedIn === TSRole.SACHBEARBEITER_JA || roleLoggedIn === TSRole.SCHULAMT) {
            return !readonly && documentUploadedByAmt;
        } else if (roleLoggedIn === TSRole.ADMIN || roleLoggedIn === TSRole.SUPER_ADMIN || roleLoggedIn === TSRole.ADMINISTRATOR_SCHULAMT) {
            return documentUploadedByAmt;
        }
        return false;
    }

    remove(dokumentGrund: TSDokumentGrund, dokument: TSDokument) {
        this.$log.debug('component -> remove dokument ' + dokument.filename);
        this.dvDialog.showRemoveDialog(removeDialogTemplate, undefined, RemoveDialogController, {
            deleteText: '',
            title: 'FILE_LOESCHEN',
            parentController: undefined,
            elementID: undefined
        })
        .then(() => {   //User confirmed removal
            this.onRemove({dokumentGrund: dokumentGrund, dokument: dokument});

        });
    }

    download(dokument: TSDokument, attachment: boolean) {
        this.$log.debug('download dokument ' + dokument.filename);
        let win: Window = this.downloadRS.prepareDownloadWindow();

        this.downloadRS.getAccessTokenDokument(dokument.id)
        .then((downloadFile: TSDownloadFile) => {
            this.$log.debug('accessToken: ' + downloadFile.accessToken);
            this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, attachment, win);
        })
        .catch((ex) => {
            win.close();
            this.$log.error('An error occurred downloading the document, closing download window.');
        });
    }

    getWidth(): String {
        if (this.sonstige) {
            return '95%';
        } else {
            if (this.tag) {
                return '45%';
            } else {
                return '60%';
            }
        }
    }

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }

    public isDokumenteUploadDisabled(): boolean {
        // Dokument-Upload ist eigentlich in jedem Status möglich, aber nicht für alle Rollen. Also nicht
        // gleichbedeutend mit readonly auf dem Gesuch
        // Jedoch darf der Gesuchsteller nach der Verfuegung nichts mehr hochladen
        let gsAndVerfuegt: boolean = isAnyStatusOfVerfuegtButSchulamt(this.gesuchModelManager.getGesuch().status) && this.authServiceRS.isRole(TSRole.GESUCHSTELLER);
        return gsAndVerfuegt || this.authServiceRS.isOneOfRoles(TSRoleUtil.getReadOnlyRoles());
    }

    /**
     * According to the personType the right FullName will be calculated.
     * - For FREETEXT the value in field fullName prevails.
     * - For GESUCHSTELLER the fullname will be taken out of the GESUCHSTELLER. The value of personNumber indicates from which Gesuchsteller.
     * - For KIND the fullname will be taken out of the KIND. The value of personNumber indicates from which Kind using its field kindNumber.
     */
    public extractFullName(dokumentGrund: TSDokumentGrund): string {
        if (dokumentGrund.personType === TSDokumentGrundPersonType.FREETEXT) {
            return dokumentGrund.fullName;
        } else if (dokumentGrund.personType === TSDokumentGrundPersonType.GESUCHSTELLER) {
            if (this.gesuchModelManager.getGesuch()) {
                if (dokumentGrund.personNumber === 2 && this.gesuchModelManager.getGesuch().gesuchsteller2) {
                    return this.gesuchModelManager.getGesuch().gesuchsteller2.extractFullName();

                } else if (dokumentGrund.personNumber === 1 && this.gesuchModelManager.getGesuch().gesuchsteller1) {
                    return this.gesuchModelManager.getGesuch().gesuchsteller1.extractFullName();
                }
            }
        } else if (dokumentGrund.personType === TSDokumentGrundPersonType.KIND) {
            if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().kindContainers) {
                let kindContainer: TSKindContainer = this.gesuchModelManager.getGesuch().extractKindFromKindNumber(dokumentGrund.personNumber);
                if (kindContainer && kindContainer.kindJA) {
                    return kindContainer.kindJA.getFullName();
                }
            }
        }
        return '';
    }
}


