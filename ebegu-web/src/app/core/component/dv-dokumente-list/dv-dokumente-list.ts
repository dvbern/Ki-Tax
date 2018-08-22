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

import {IComponentOptions, ILogService, IWindowService} from 'angular';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import {OkHtmlDialogController} from '../../../../gesuch/dialog/OkHtmlDialogController';
import {RemoveDialogController} from '../../../../gesuch/dialog/RemoveDialogController';
import GesuchModelManager from '../../../../gesuch/service/gesuchModelManager';
import WizardStepManager from '../../../../gesuch/service/wizardStepManager';
import {isAnyStatusOfVerfuegtButSchulamt} from '../../../../models/enums/TSAntragStatus';
import {TSDokumentGrundPersonType} from '../../../../models/enums/TSDokumentGrundPersonType';
import {TSRole} from '../../../../models/enums/TSRole';
import TSApplicationProperty from '../../../../models/TSApplicationProperty';
import TSDokument from '../../../../models/TSDokument';
import TSDokumentGrund from '../../../../models/TSDokumentGrund';
import TSDownloadFile from '../../../../models/TSDownloadFile';
import TSKindContainer from '../../../../models/TSKindContainer';
import EbeguUtil from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {DvDialog} from '../../directive/dv-dialog/dv-dialog';
import {ApplicationPropertyRS} from '../../rest-services/applicationPropertyRS.rest';
import {DownloadRS} from '../../service/downloadRS.rest';
import {UploadRS} from '../../service/uploadRS.rest';
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTemplate = require('../../../../gesuch/dialog/removeDialogTemplate.html');
const okHtmlDialogTempl = require('../../../../gesuch/dialog/okHtmlDialogTemplate.html');

export class DVDokumenteListConfig implements IComponentOptions {
    transclude = false;

    bindings = {
        dokumente: '<',
        tableId: '@',
        tableTitle: '@',
        tag: '@',
        titleValue: '<',
        onUploadDone: '&',
        onRemove: '&',
        sonstige: '<'

    };
    template = require('./dv-dokumente-list.html');
    controller = DVDokumenteListController;
    controllerAs = 'vm';
}

export class DVDokumenteListController {

    static $inject: ReadonlyArray<string> = ['UploadRS', 'GesuchModelManager', 'EbeguUtil', 'DownloadRS', 'DvDialog', 'WizardStepManager',
        '$log', 'AuthServiceRS', '$translate', '$window', 'ApplicationPropertyRS'];

    dokumente: TSDokumentGrund[];
    tableId: string;
    tableTitle: string;
    tag: string;
    titleValue: string;
    onUploadDone: (dokumentGrund: any) => void;
    onRemove: (attrs: any) => void;
    sonstige: boolean;
    allowedMimetypes: string = '';

    constructor(private readonly uploadRS: UploadRS,
                private readonly gesuchModelManager: GesuchModelManager,
                private readonly ebeguUtil: EbeguUtil,
                private readonly downloadRS: DownloadRS,
                private readonly dvDialog: DvDialog,
                private readonly wizardStepManager: WizardStepManager,
                private readonly $log: ILogService,
                private readonly authServiceRS: AuthServiceRS,
                private readonly $translate: ITranslateService,
                private readonly $window: IWindowService,
                private readonly applicationPropertyRS: ApplicationPropertyRS) {

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
            const gesuchID = this.gesuchModelManager.getGesuch().id;
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
                this.uploadRS.uploadFile(filesOk, selectDokument, gesuchID).then((response) => {
                    const returnedDG: TSDokumentGrund = angular.copy(response);
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
            for (const dokument of selectDokument.dokumente) {
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
        const readonly: boolean = this.isGesuchReadonly();
        const roleLoggedIn: TSRole = this.authServiceRS.getPrincipalRole();
        let documentUploadedByAmt: boolean = true; // by default true in case there is no uploadUser
        if (dokument.userUploaded) {
            const roleDocumentUpload: TSRole = dokument.userUploaded.getCurrentRole();
            documentUploadedByAmt = (roleDocumentUpload === TSRole.SACHBEARBEITER_BG || roleDocumentUpload === TSRole.ADMIN_BG
                || roleDocumentUpload === TSRole.SCHULAMT || roleDocumentUpload === TSRole.ADMIN_TS || roleDocumentUpload === TSRole.SUPER_ADMIN);
        }
        if (roleLoggedIn === TSRole.GESUCHSTELLER) {
            return !readonly;
        } else if (roleLoggedIn === TSRole.SACHBEARBEITER_BG || roleLoggedIn === TSRole.SCHULAMT) {
            return !readonly && documentUploadedByAmt;
        } else if (roleLoggedIn === TSRole.ADMIN_BG || roleLoggedIn === TSRole.SUPER_ADMIN || roleLoggedIn === TSRole.ADMIN_TS) {
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
        const win: Window = this.downloadRS.prepareDownloadWindow();

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

    getWidth(): string {
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
        const gsAndVerfuegt: boolean = isAnyStatusOfVerfuegtButSchulamt(this.gesuchModelManager.getGesuch().status) && this.authServiceRS.isRole(TSRole.GESUCHSTELLER);
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
                const kindContainer: TSKindContainer = this.gesuchModelManager.getGesuch().extractKindFromKindNumber(dokumentGrund.personNumber);
                if (kindContainer && kindContainer.kindJA) {
                    return kindContainer.kindJA.getFullName();
                }
            }
        }
        return '';
    }
}


