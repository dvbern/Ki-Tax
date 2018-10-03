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
import * as moment from 'moment';
import {of} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {RemoveDialogController} from '../../../gesuch/dialog/RemoveDialogController';
import {TSZahlungsauftragsstatus} from '../../../models/enums/TSZahlungsauftragstatus';
import {TSZahlungsstatus} from '../../../models/enums/TSZahlungsstatus';
import TSDownloadFile from '../../../models/TSDownloadFile';
import TSZahlungsauftrag from '../../../models/TSZahlungsauftrag';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvDialog} from '../../core/directive/dv-dialog/dv-dialog';
import {LogFactory} from '../../core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {ReportRS} from '../../core/service/reportRS.rest';
import ZahlungRS from '../../core/service/zahlungRS.rest';
import IFormController = angular.IFormController;
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTemplate = require('../../../gesuch/dialog/removeDialogTemplate.html');

const LOG = LogFactory.createLog('ZahlungsauftragViewController');

export class ZahlungsauftragViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./zahlungsauftragView.html');
    public controller = ZahlungsauftragViewController;
    public controllerAs = 'vm';
}

export class ZahlungsauftragViewController implements IController {

    public static $inject: string[] = [
        'ZahlungRS',
        '$state',
        'DownloadRS',
        'ApplicationPropertyRS',
        'ReportRS',
        'AuthServiceRS',
        'DvDialog',
        '$translate',
    ];

    public form: IFormController;
    private zahlungsauftragToEdit: TSZahlungsauftrag;
    public zahlungsAuftraege: TSZahlungsauftrag[] = [];

    public beschrieb: string;
    public faelligkeitsdatum: moment.Moment;
    public datumGeneriert: moment.Moment;
    public itemsByPage: number = 12;
    public testMode: boolean = false;
    public minDateForTestlauf: moment.Moment;

    public constructor(
        private readonly zahlungRS: ZahlungRS,
        private readonly $state: StateService,
        private readonly downloadRS: DownloadRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly reportRS: ReportRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly dvDialog: DvDialog,
        private readonly $translate: ITranslateService,
    ) {
    }

    public $onInit(): void {
        // Testlauf darf auch nur in die Zukunft gemacht werden!
        this.minDateForTestlauf = moment(moment.now()).subtract(1, 'days');
        this.updateZahlungsauftrag();
        this.applicationPropertyRS.isZahlungenTestMode().then((response: any) => {
            this.testMode = response;
        });
    }

    private updateZahlungsauftrag(): void {
        this.authServiceRS.principal$
            .pipe(
                switchMap(principal => {
                    if (principal) {
                        return this.zahlungRS.getZahlungsauftraegeForRole$(principal.getCurrentRole());
                    }

                    return of([]);
                }),
            )
            .subscribe(
                zahlungsAuftraege => {
                    this.zahlungsAuftraege = zahlungsAuftraege;
                },
                err => LOG.error(err),
            );
    }

    public gotoZahlung(zahlungsauftrag: TSZahlungsauftrag): void {
        this.$state.go('zahlung.view', {
            zahlungsauftragId: zahlungsauftrag.id,
        });
    }

    public createZahlungsauftrag(): void {
        if (!this.form.$valid) {
            return;
        }

        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            title: this.$translate.instant('ZAHLUNG_ERSTELLEN_CONFIRM'),
            deleteText: this.$translate.instant('ZAHLUNG_ERSTELLEN_INFO'),
            parentController: undefined,
            elementID: undefined,
        }).then(() => {   // User confirmed removal
            this.zahlungRS.createZahlungsauftrag(this.beschrieb, this.faelligkeitsdatum, this.datumGeneriert)
                .then((response: TSZahlungsauftrag) => {
                    this.zahlungsAuftraege.push(response);
                    this.resetEditZahlungsauftrag();
                    this.resetForm();
                });
        });
    }

    public downloadPain(zahlungsauftrag: TSZahlungsauftrag): angular.IPromise<void | never> {
        const win = this.downloadRS.prepareDownloadWindow();
        return this.downloadRS.getPain001AccessTokenGeneratedDokument(zahlungsauftrag.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, true, win);
            })
            .catch(() => {
                win.close();
            });
    }

    public downloadAllDetails(zahlungsauftrag: TSZahlungsauftrag): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.reportRS.getZahlungsauftragReportExcel(zahlungsauftrag.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(() => {
                win.close();
            });
    }

    public ausloesen(zahlungsauftragId: string): void {
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            title: this.$translate.instant('ZAHLUNG_AUSLOESEN_CONFIRM'),
            deleteText: this.$translate.instant('ZAHLUNG_AUSLOESEN_INFO'),
            parentController: undefined,
            elementID: undefined,
        }).then(() => {   // User confirmed removal
            this.zahlungRS.zahlungsauftragAusloesen(zahlungsauftragId).then((response: TSZahlungsauftrag) => {
                const index = EbeguUtil.getIndexOfElementwithID(response, this.zahlungsAuftraege);
                if (index > -1) {
                    this.zahlungsAuftraege[index] = response;
                }
                EbeguUtil.handleSmarttablesUpdateBug(this.zahlungsAuftraege);
            });
        });
    }

    public edit(zahlungsauftrag: TSZahlungsauftrag): void {
        this.zahlungsauftragToEdit = zahlungsauftrag;
    }

    public save(_zahlungsauftrag: TSZahlungsauftrag): void {
        if (!this.isEditValid()) {
            return;
        }

        this.zahlungRS.updateZahlungsauftrag(
            this.zahlungsauftragToEdit.beschrieb,
            this.zahlungsauftragToEdit.datumFaellig,
            this.zahlungsauftragToEdit.id,
        )
            .then((response: TSZahlungsauftrag) => {
                const index = EbeguUtil.getIndexOfElementwithID(response, this.zahlungsAuftraege);
                if (index > -1) {
                    this.zahlungsAuftraege[index] = response;
                }
                // nach dem es gespeichert wird, muessen wir das Form wieder auf clean setzen
                this.form.$setPristine();
                this.resetEditZahlungsauftrag();
            });
    }

    public isEditable(status: TSZahlungsauftragsstatus): boolean {
        return status === TSZahlungsauftragsstatus.ENTWURF;
    }

    public isEditMode(zahlungsauftragId: string): boolean {
        return this.zahlungsauftragToEdit && this.zahlungsauftragToEdit.id === zahlungsauftragId;
    }

    public isEditValid(): boolean {
        if (this.zahlungsauftragToEdit) {
            return this.zahlungsauftragToEdit.beschrieb
                && this.zahlungsauftragToEdit.beschrieb.length > 0
                && this.zahlungsauftragToEdit.datumFaellig !== null
                && this.zahlungsauftragToEdit.datumFaellig !== undefined;
        }
        return false;
    }

    private resetEditZahlungsauftrag(): void {
        this.zahlungsauftragToEdit = null;
    }

    public rowClass(zahlungsauftragId: string): string {
        if (this.isEditMode(zahlungsauftragId) && !this.isEditValid()) {
            return 'errorrow';
        }
        return '';
    }

    /**
     * resets all three variables needed to create a Zahlung.
     */
    private resetForm(): void {
        this.beschrieb = undefined;
        this.faelligkeitsdatum = undefined;
        this.datumGeneriert = undefined;
        this.form.$setPristine();
        this.form.$setUntouched();
    }

    public getCalculatedStatus(zahlungsauftrag: TSZahlungsauftrag): any {
        if (zahlungsauftrag.status !== TSZahlungsauftragsstatus.BESTAETIGT
            && this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())
            && zahlungsauftrag.zahlungen.every(zahlung => zahlung.status === TSZahlungsstatus.BESTAETIGT)) {

            return TSZahlungsstatus.BESTAETIGT;
        }
        return zahlungsauftrag.status;
    }
}
