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
import {DvDialog} from '../../app/core/directive/dv-dialog/dv-dialog';
import {ApplicationPropertyRS} from '../../app/core/rest-services/applicationPropertyRS.rest';
import {DownloadRS} from '../../app/core/service/downloadRS.rest';
import {ReportRS} from '../../app/core/service/reportRS.rest';
import ZahlungRS from '../../app/core/service/zahlungRS.rest';
import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import {RemoveDialogController} from '../../gesuch/dialog/RemoveDialogController';
import {TSRole} from '../../models/enums/TSRole';
import {TSZahlungsauftragsstatus} from '../../models/enums/TSZahlungsauftragstatus';
import {TSZahlungsstatus} from '../../models/enums/TSZahlungsstatus';
import TSDownloadFile from '../../models/TSDownloadFile';
import TSZahlungsauftrag from '../../models/TSZahlungsauftrag';
import EbeguUtil from '../../utils/EbeguUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import IFormController = angular.IFormController;
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTemplate = require('../../gesuch/dialog/removeDialogTemplate.html');

export class ZahlungsauftragViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = require('./zahlungsauftragView.html');
    controller = ZahlungsauftragViewController;
    controllerAs = 'vm';
}

export class ZahlungsauftragViewController implements IController {

    static $inject: string[] = ['ZahlungRS', 'CONSTANTS', '$state', 'DownloadRS', 'ApplicationPropertyRS', 'ReportRS',
        'AuthServiceRS', 'EbeguUtil', 'DvDialog', '$translate'];

    form: IFormController;
    private zahlungsauftragen: Array<TSZahlungsauftrag>;
    private zahlungsauftragToEdit: TSZahlungsauftrag;

    beschrieb: string;
    faelligkeitsdatum: moment.Moment;
    datumGeneriert: moment.Moment;
    itemsByPage: number = 12;
    testMode: boolean = false;
    minDateForTestlauf: moment.Moment;

    constructor(private readonly zahlungRS: ZahlungRS,
                private readonly CONSTANTS: any,
                private readonly $state: StateService,
                private readonly downloadRS: DownloadRS,
                private readonly applicationPropertyRS: ApplicationPropertyRS,
                private readonly reportRS: ReportRS,
                private readonly authServiceRS: AuthServiceRS,
                private readonly ebeguUtil: EbeguUtil,
                private readonly dvDialog: DvDialog,
                private readonly $translate: ITranslateService) {
    }

    public getZahlungsauftragen() {
        return this.zahlungsauftragen;
    }

    public $onInit() {
        this.minDateForTestlauf = moment(moment.now()).subtract(1, 'days'); // Testlauf darf auch nur in die Zukunft gemacht werden!
        this.updateZahlungsauftrag();
        this.applicationPropertyRS.isZahlungenTestMode().then((response: any) => {
            this.testMode = response;
        });
    }

    private updateZahlungsauftrag() {
        if (this.authServiceRS.getPrincipal()) {
            switch (this.authServiceRS.getPrincipal().getCurrentRole()) {
                case TSRole.ADMIN_INSTITUTION:
                case TSRole.SACHBEARBEITER_INSTITUTION:
                case TSRole.ADMIN_TRAEGERSCHAFT:
                case TSRole.SACHBEARBEITER_TRAEGERSCHAFT: {
                    this.zahlungRS.getAllZahlungsauftraegeInstitution().then((response: any) => {
                        this.zahlungsauftragen = angular.copy(response);

                    });
                    break;
                }
                case TSRole.SUPER_ADMIN:
                case TSRole.ADMIN_BG:
                case TSRole.SACHBEARBEITER_BG:
                case TSRole.ADMIN_GEMEINDE:
                case TSRole.SACHBEARBEITER_GEMEINDE:
                case TSRole.JURIST:
                case TSRole.REVISOR: {
                    this.zahlungRS.getAllZahlungsauftraege().then((response: any) => {
                        this.zahlungsauftragen = angular.copy(response);

                    });
                    break;
                }
                default:
                    break;
            }
        }
    }

    public gotoZahlung(zahlungsauftrag: TSZahlungsauftrag) {
        this.$state.go('zahlung', {
            zahlungsauftragId: zahlungsauftrag.id
        });
    }

    public createZahlungsauftrag() {
        if (this.form.$valid) {
            this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
                title: this.$translate.instant('ZAHLUNG_ERSTELLEN_CONFIRM'),
                deleteText: this.$translate.instant('ZAHLUNG_ERSTELLEN_INFO'),
                parentController: undefined,
                elementID: undefined
            }).then(() => {   //User confirmed removal
                this.zahlungRS.createZahlungsauftrag(this.beschrieb, this.faelligkeitsdatum, this.datumGeneriert).then((response: TSZahlungsauftrag) => {
                    this.zahlungsauftragen.push(response);
                    this.resetEditZahlungsauftrag();
                    this.resetForm();
                });
            });
        }
    }

    public downloadPain(zahlungsauftrag: TSZahlungsauftrag) {
        const win: Window = this.downloadRS.prepareDownloadWindow();
        return this.downloadRS.getPain001AccessTokenGeneratedDokument(zahlungsauftrag.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, true, win);
            })
            .catch((ex) => {
                win.close();
            });
    }

    public downloadAllDetails(zahlungsauftrag: TSZahlungsauftrag) {
        const win: Window = this.downloadRS.prepareDownloadWindow();
        this.reportRS.getZahlungsauftragReportExcel(zahlungsauftrag.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch((ex) => {
                win.close();
            });
    }

    public ausloesen(zahlungsauftragId: string) {
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            title: this.$translate.instant('ZAHLUNG_AUSLOESEN_CONFIRM'),
            deleteText: this.$translate.instant('ZAHLUNG_AUSLOESEN_INFO'),
            parentController: undefined,
            elementID: undefined
        }).then(() => {   //User confirmed removal
            this.zahlungRS.zahlungsauftragAusloesen(zahlungsauftragId).then((response: TSZahlungsauftrag) => {
                const index = EbeguUtil.getIndexOfElementwithID(response, this.zahlungsauftragen);
                if (index > -1) {
                    this.zahlungsauftragen[index] = response;
                }
                EbeguUtil.handleSmarttablesUpdateBug(this.zahlungsauftragen);
            });
        });
    }

    public edit(zahlungsauftrag: TSZahlungsauftrag) {
        this.zahlungsauftragToEdit = zahlungsauftrag;
    }

    public save(zahlungsauftrag: TSZahlungsauftrag) {
        if (this.isEditValid()) {
            this.zahlungRS.updateZahlungsauftrag(
                this.zahlungsauftragToEdit.beschrieb, this.zahlungsauftragToEdit.datumFaellig, this.zahlungsauftragToEdit.id).then((response: TSZahlungsauftrag) => {
                const index = EbeguUtil.getIndexOfElementwithID(response, this.zahlungsauftragen);
                if (index > -1) {
                    this.zahlungsauftragen[index] = response;
                }
                this.form.$setPristine(); // nach dem es gespeichert wird, muessen wir das Form wieder auf clean setzen
                this.resetEditZahlungsauftrag();
            });

        }
    }

    public isEditable(status: TSZahlungsauftragsstatus): boolean {
        return status === TSZahlungsauftragsstatus.ENTWURF;
    }

    public isEditMode(zahlungsauftragId: string): boolean {
        return this.zahlungsauftragToEdit && this.zahlungsauftragToEdit.id === zahlungsauftragId;
    }

    public isEditValid(): boolean {
        if (this.zahlungsauftragToEdit) {
            return this.zahlungsauftragToEdit.beschrieb && this.zahlungsauftragToEdit.beschrieb.length > 0 &&
                this.zahlungsauftragToEdit.datumFaellig !== null && this.zahlungsauftragToEdit.datumFaellig !== undefined;
        }
        return false;
    }

    private resetEditZahlungsauftrag() {
        this.zahlungsauftragToEdit = null;
    }

    public rowClass(zahlungsauftragId: string) {
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

    public getCalculatedStatus(zahlungsauftrag: TSZahlungsauftrag) {
        if (zahlungsauftrag.status !== TSZahlungsauftragsstatus.BESTAETIGT && this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            if (zahlungsauftrag.zahlungen.every(zahlung => zahlung.status === TSZahlungsstatus.BESTAETIGT)) {
                return TSZahlungsstatus.BESTAETIGT;
            }
        }
        return zahlungsauftrag.status;
    }
}
