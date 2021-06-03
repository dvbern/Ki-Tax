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

import {IComponentOptions, IController, ILogService} from 'angular';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {OkHtmlDialogController} from '../../../../gesuch/dialog/OkHtmlDialogController';
import {RemoveDialogController} from '../../../../gesuch/dialog/RemoveDialogController';
import {GesuchModelManager} from '../../../../gesuch/service/gesuchModelManager';
import {WizardStepManager} from '../../../../gesuch/service/wizardStepManager';
import {isAnyStatusOfGeprueftVerfuegenVerfuegtButSchulamt} from '../../../../models/enums/TSAntragStatus';
import {TSDokumentGrundPersonType} from '../../../../models/enums/TSDokumentGrundPersonType';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSDokument} from '../../../../models/TSDokument';
import {TSDokumentGrund} from '../../../../models/TSDokumentGrund';
import {TSDownloadFile} from '../../../../models/TSDownloadFile';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {MAX_FILE_SIZE} from '../../constants/CONSTANTS';
import {DvDialog} from '../../directive/dv-dialog/dv-dialog';
import {ApplicationPropertyRS} from '../../rest-services/applicationPropertyRS.rest';
import {DownloadRS} from '../../service/downloadRS.rest';
import {UploadRS} from '../../service/uploadRS.rest';
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTemplate = require('../../../../gesuch/dialog/removeDialogTemplate.html');
const okHtmlDialogTempl = require('../../../../gesuch/dialog/okHtmlDialogTemplate.html');

export class DVDokumenteListConfig implements IComponentOptions {
    public transclude = false;

    public bindings = {
        dokumente: '<',
        tableId: '@',
        tableTitle: '@',
        tag: '@',
        titleValue: '<',
        onUploadDone: '&',
        onRemove: '&',
        sonstige: '<',

    };
    public template = require('./dv-dokumente-list.html');
    public controller = DVDokumenteListController;
    public controllerAs = 'vm';
}

export class DVDokumenteListController implements IController {

    public static $inject: ReadonlyArray<string> = [
        'UploadRS',
        'GesuchModelManager',
        'DownloadRS',
        'DvDialog',
        'WizardStepManager',
        '$log',
        'AuthServiceRS',
        '$translate',
        'ApplicationPropertyRS',
    ];

    public dokumente: TSDokumentGrund[];
    public tableId: string;
    public tableTitle: string;
    public tag: string;
    public titleValue: string;
    public onUploadDone: (dokumentGrund: any) => void;
    public onRemove: (attrs: any) => void;
    public sonstige: boolean;
    public allowedMimetypes: string = '';

    public constructor(
        private readonly uploadRS: UploadRS,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly downloadRS: DownloadRS,
        private readonly dvDialog: DvDialog,
        private readonly wizardStepManager: WizardStepManager,
        private readonly $log: ILogService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly $translate: ITranslateService,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
    ) {

    }

    public $onInit(): void {
        this.applicationPropertyRS.getAllowedMimetypes().then(response => {
            if (response !== undefined) {
                this.allowedMimetypes = response;
            }
        });

    }

    public uploadAnhaenge(files: any[], selectDokument: TSDokumentGrund): void {
        if (!this.gesuchModelManager.getGesuch()) {
            this.$log.warn('No gesuch found to store file or gesuch is status verfuegt');
            return;
        }

        const gesuchID = this.gesuchModelManager.getGesuch().id;
        const filesTooBig: any[] = [];
        const filesOk: any[] = [];
        this.$log.debug(`Uploading files on gesuch ${gesuchID}`);
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
            let returnString = `${this.$translate.instant('FILE_ZU_GROSS')}<br/><br/>`;
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

        this.uploadRS.uploadFile(filesOk, selectDokument, gesuchID).then(response => {
            const returnedDG = angular.copy(response);
            this.wizardStepManager.findStepsFromGesuch(this.gesuchModelManager.getGesuch().id)
                .then(() => this.handleUpload(returnedDG));
        });
    }

    public hasDokuments(selectDokument: TSDokumentGrund): boolean {
        if (selectDokument.dokumente) {
            for (const dokument of selectDokument.dokumente) {
                if (dokument.filename) {
                    return true;
                }
            }
        }
        return false;
    }

    public handleUpload(returnedDG: TSDokumentGrund): void {
        this.onUploadDone({dokument: returnedDG});
    }

    public isRemoveAllowed(_dokumentGrund: TSDokumentGrund, dokument: TSDokument): boolean {
        // Loeschen von Dokumenten ist nur in folgenden Faellen erlaubt:
        // - GS bis Freigabe (d.h nicht readonlyForRole). In diesem Status kann es nur "seine" Dokumente geben
        // - JA bis Verfuegen, aber nur die von JA hinzugefuegten: d.h. wenn noch nicht verfuegt: die eigenen, wenn
        // readonly: nichts - Admin: Auch nach verfuegen, aber nur die vom JA hinzugefuegten: wenn noch nicht verfuegt
        // oder readonly: die eigenen - Alle anderen Rollen: nichts
        const readonly = this.isGesuchReadonly();
        const roleLoggedIn = this.authServiceRS.getPrincipalRole();
        let documentUploadedByAmt = true; // by default true in case there is no uploadUser
        if (dokument.userUploaded) {
            const roleDocumentUpload = dokument.userUploaded.getCurrentRole();
            const amtroles = [
                TSRole.SACHBEARBEITER_BG,
                TSRole.ADMIN_BG,
                TSRole.SACHBEARBEITER_GEMEINDE,
                TSRole.ADMIN_GEMEINDE,
                TSRole.SACHBEARBEITER_TS,
                TSRole.ADMIN_TS,
                TSRole.SUPER_ADMIN,
            ];
            documentUploadedByAmt = amtroles.includes(roleDocumentUpload);
        }

        switch (roleLoggedIn) {
            case TSRole.GESUCHSTELLER:
                return !readonly;
            case TSRole.SACHBEARBEITER_BG:
            case TSRole.SACHBEARBEITER_GEMEINDE:
            case TSRole.SACHBEARBEITER_TS:
                return !readonly && documentUploadedByAmt;
            case TSRole.ADMIN_BG:
            case TSRole.ADMIN_GEMEINDE:
            case TSRole.SUPER_ADMIN:
            case TSRole.ADMIN_TS:
                return documentUploadedByAmt;
            default:
                return false;
        }
    }

    public remove(dokumentGrund: TSDokumentGrund, dokument: TSDokument): void {
        this.$log.debug(`component -> remove dokument ${dokument.filename}`);
        this.dvDialog.showRemoveDialog(removeDialogTemplate, undefined, RemoveDialogController, {
            deleteText: '',
            title: 'FILE_LOESCHEN',
            parentController: undefined,
            elementID: undefined,
        })
            .then(() => {   // User confirmed removal
                this.onRemove({dokumentGrund, dokument});

            });
    }

    public download(dokument: TSDokument, attachment: boolean): void {
        this.$log.debug(`download dokument ${dokument.filename}`);
        const win = this.downloadRS.prepareDownloadWindow();

        this.downloadRS.getAccessTokenDokument(dokument.id)
            .then((downloadFile: TSDownloadFile) => {
                this.$log.debug(`accessToken: ${downloadFile.accessToken}`);
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, attachment, win);
            })
            .catch(() => {
                win.close();
                this.$log.error('An error occurred downloading the document, closing download window.');
            });
    }

    public getWidth(): string {
        if (this.sonstige) {
            return '95%';
        }

        return this.tag ? '45%' : '60%';
    }

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }

    public isDokumenteUploadDisabled(): boolean {
        // Dokument-Upload ist eigentlich in jedem Status möglich, aber nicht für alle Rollen. Also nicht
        // gleichbedeutend mit readonly auf dem Gesuch
        // Jedoch darf der Gesuchsteller nach der Verfuegung und in Bearbeitung Gemeinde/JA nichts mehr hochladen
        const gsAndVerfuegt = this.gesuchModelManager.getGesuch()
            && isAnyStatusOfGeprueftVerfuegenVerfuegtButSchulamt(this.gesuchModelManager.getGesuch().status)
            && this.authServiceRS.isRole(TSRole.GESUCHSTELLER);
        return gsAndVerfuegt || this.authServiceRS.isOneOfRoles(TSRoleUtil.getReadOnlyRoles());
    }

    /**
     * According to the personType the right FullName will be calculated.
     * - For GESUCHSTELLER the fullname will be taken out of the GESUCHSTELLER. The value of personNumber indicates
     * from which Gesuchsteller.
     * - For KIND the fullname will be taken out of the KIND. The value of personNumber indicates from which Kind using
     * its field kindNumber.
     */
    public extractFullName(dokumentGrund: TSDokumentGrund): string {
        const gesuch = this.gesuchModelManager.getGesuch();
        if (!gesuch) {
            return '';
        }

        if (dokumentGrund.personType === TSDokumentGrundPersonType.GESUCHSTELLER) {
            if (dokumentGrund.personNumber === 2 && gesuch.gesuchsteller2) {
                return gesuch.gesuchsteller2.extractFullName();
            }
            if (dokumentGrund.personNumber === 1 && gesuch.gesuchsteller1) {
                return gesuch.gesuchsteller1.extractFullName();
            }
        } else if (dokumentGrund.personType === TSDokumentGrundPersonType.KIND && gesuch.kindContainers) {
            const kindContainer = gesuch.extractKindFromKindNumber(dokumentGrund.personNumber);
            if (kindContainer && kindContainer.kindJA) {
                return kindContainer.kindJA.getFullName();
            }
        }
        return '';
    }

    public getDokumentText(dokumentGrund: TSDokumentGrund): string {
        const key = `${dokumentGrund.dokumentGrundTyp}_${dokumentGrund.dokumentTyp}`;
        return this.$translate.instant(key);
    }
}
