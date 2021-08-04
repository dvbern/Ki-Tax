/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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
import * as moment from 'moment';
import {MAX_FILE_SIZE} from '../../../app/core/constants/CONSTANTS';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {DownloadRS} from '../../../app/core/service/downloadRS.rest';
import {GesuchsperiodeRS} from '../../../app/core/service/gesuchsperiodeRS.rest';
import {UploadRS} from '../../../app/core/service/uploadRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {OkHtmlDialogController} from '../../../gesuch/dialog/OkHtmlDialogController';
import {RemoveDialogController} from '../../../gesuch/dialog/RemoveDialogController';
import {GlobalCacheService} from '../../../gesuch/service/globalCacheService';
import {TSCacheTyp} from '../../../models/enums/TSCacheTyp';
import {TSDokumentTyp} from '../../../models/enums/TSDokumentTyp';
import {getTSGesuchsperiodeStatusValues, TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSSprache} from '../../../models/enums/TSSprache';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TSDateRange} from '../../../models/types/TSDateRange';
import {AbstractAdminViewController} from '../../abstractAdminView';
import {IGesuchsperiodeStateParams} from '../../admin.route';
import {EinstellungRS} from '../../service/einstellungRS.rest';
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTemplate = require('../../../gesuch/dialog/removeDialogTemplate.html');
const okHtmlDialogTempl = require('../../../gesuch/dialog/okHtmlDialogTemplate.html');

export class GesuchsperiodeViewComponentConfig implements IComponentOptions {
    public transclude: boolean = false;
    public template: string = require('./gesuchsperiodeView.html');
    public controller: any = GesuchsperiodeViewController;
    public controllerAs: string = 'vm';
}

export class GesuchsperiodeViewController extends AbstractAdminViewController {

    private static readonly DOKUMENT_TYP_NOT_DEFINED = 'DokumentTyp not defined';

    public static $inject = [
        'EinstellungRS',
        'DvDialog',
        'GlobalCacheService',
        'GesuchsperiodeRS',
        '$log',
        '$stateParams',
        '$state',
        '$translate',
        'UploadRS',
        'DownloadRS',
        'AuthServiceRS',
    ];

    public form: IFormController;
    public gesuchsperiode: TSGesuchsperiode;
    public einstellungenGesuchsperiode: TSEinstellung[];

    public initialStatus: TSGesuchsperiodeStatus;
    public datumFreischaltungTagesschule: moment.Moment;

    public isErlaeuterungDE: boolean = false;
    public isErlaeuterungFR: boolean = false;

    public isVorlageMerkblattDE: boolean = false;
    public isVorlageMerkblattFR: boolean = false;

    public isVorlageVerfuegungLatsDE: boolean = false;
    public isVorlageVerfuegungLatsFR: boolean = false;

    public constructor(
        private readonly einstellungenRS: EinstellungRS,
        private readonly dvDialog: DvDialog,
        private readonly globalCacheService: GlobalCacheService,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly $log: ILogService,
        private readonly $stateParams: IGesuchsperiodeStateParams,
        private readonly $state: StateService,
        private readonly $translate: ITranslateService,
        private readonly uploadRS: UploadRS,
        private readonly downloadRS: DownloadRS,
        authServiceRS: AuthServiceRS,
    ) {
        super(authServiceRS);
    }

    public $onInit(): void {
        if (!this.$stateParams.gesuchsperiodeId) {
            this.createGesuchsperiode();

            return;
        }

        this.gesuchsperiodeRS.findGesuchsperiode(this.$stateParams.gesuchsperiodeId).then((found: TSGesuchsperiode) => {
            this.setSelectedGesuchsperiode(found);
            this.initialStatus = this.gesuchsperiode.status;

            this.updateExistDokumenten(this.gesuchsperiode);
        });
    }

    public getTSGesuchsperiodeStatusValues(): Array<TSGesuchsperiodeStatus> {
        return getTSGesuchsperiodeStatusValues();
    }

    private setSelectedGesuchsperiode(gesuchsperiode: any): void {
        this.gesuchsperiode = gesuchsperiode;
        this.readEinstellungenByGesuchsperiode();
    }

    private readEinstellungenByGesuchsperiode(): void {
        this.einstellungenRS.getAllEinstellungenBySystem(this.gesuchsperiode.id).then((response: TSEinstellung[]) => {
            this.einstellungenGesuchsperiode = response;
        });
    }

    public cancelGesuchsperiode(): void {
        this.$state.go('admin.parameter');
    }

    public saveGesuchsperiode(): void {
        if (!this.form.$valid || !this.statusHaveChanged()) {
            return;
        }
        if (!(this.gesuchsperiode.isNew()
            || this.initialStatus !== this.gesuchsperiode.status
            || this.gesuchsperiode.status === TSGesuchsperiodeStatus.AKTIV)) {
            return;
        }
        const dialogText = this.getGesuchsperiodeSaveDialogText(this.initialStatus !== this.gesuchsperiode.status);
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            title: 'GESUCHSPERIODE_DIALOG_TITLE',
            deleteText: dialogText,
            parentController: undefined,
            elementID: undefined,
        }).then(() => {
            this.doSave();
        });
        return;
    }

    private doSave(): void {
        this.gesuchsperiodeRS.saveGesuchsperiode(this.gesuchsperiode).then((response: TSGesuchsperiode) => {
            this.gesuchsperiode = response;
            this.globalCacheService.getCache(TSCacheTyp.EBEGU_EINSTELLUNGEN).removeAll();
            // Die E-BEGU-Parameter für die neue Periode lesen bzw. erstellen, wenn noch nicht vorhanden
            this.readEinstellungenByGesuchsperiode();
            this.gesuchsperiodeRS.updateActiveGesuchsperiodenList(); // reset gesuchperioden in manager
            this.gesuchsperiodeRS.updateNichtAbgeschlosseneGesuchsperiodenList();
            this.initialStatus = this.gesuchsperiode.status;
        });
    }

    public createGesuchsperiode(): void {
        this.gesuchsperiodeRS.getNewestGesuchsperiode().then(newestGesuchsperiode => {
            this.gesuchsperiode = new TSGesuchsperiode(TSGesuchsperiodeStatus.ENTWURF, new TSDateRange());
            this.initialStatus = undefined; // initialStatus ist undefined for new created Gesuchsperioden
            this.gesuchsperiode.gueltigkeit.gueltigAb =
                newestGesuchsperiode.gueltigkeit.gueltigAb.clone().add(1, 'years');
            this.gesuchsperiode.gueltigkeit.gueltigBis =
                newestGesuchsperiode.gueltigkeit.gueltigBis.clone().add(1, 'years');

            // we need to check for erlaeuterung already here, because the server wont send any information back and we
            // do not want to reloade the page
            this.updateExistDokumenten(newestGesuchsperiode);

        });
        this.gesuchsperiode = undefined;
    }

    public saveParameterByGesuchsperiode(): void {
        this.einstellungenGesuchsperiode.forEach(param => this.einstellungenRS.saveEinstellung(param));
        this.globalCacheService.getCache(TSCacheTyp.EBEGU_EINSTELLUNGEN).removeAll();
        this.gesuchsperiodeRS.updateActiveGesuchsperiodenList();
        this.gesuchsperiodeRS.updateNichtAbgeschlosseneGesuchsperiodenList();
    }

    private getGesuchsperiodeSaveDialogText(hasStatusChanged: boolean): string {
        if (!hasStatusChanged) {
            return ''; // if the status didn't change no message is required
        }
        if (this.gesuchsperiode.status === TSGesuchsperiodeStatus.ENTWURF) {
            return 'GESUCHSPERIODE_DIALOG_TEXT_ENTWURF';
        }
        if (this.gesuchsperiode.status === TSGesuchsperiodeStatus.AKTIV) {
            return 'GESUCHSPERIODE_DIALOG_TEXT_AKTIV';
        }
        if (this.gesuchsperiode.status === TSGesuchsperiodeStatus.INAKTIV) {
            return 'GESUCHSPERIODE_DIALOG_TEXT_INAKTIV';
        }
        if (this.gesuchsperiode.status === TSGesuchsperiodeStatus.GESCHLOSSEN) {
            return 'GESUCHSPERIODE_DIALOG_TEXT_GESCHLOSSEN';
        }
        this.$log.warn('Achtung, Status unbekannt: ', this.gesuchsperiode.status);

        return null;
    }

    public periodenParamsEditable(): boolean {
        return this.periodenParamsEditableForPeriode(this.gesuchsperiode);
    }

    /**
     * Gibt true zurueck wenn der Status sich geaendert hat oder wenn der Status AKTIV ist, da in Status AKTIV, die
     * Parameter (Tagesschule) noch geaendert werden koennen.
     */
    private statusHaveChanged(): boolean {
        return this.initialStatus !== this.gesuchsperiode.status || this.gesuchsperiode.status === TSGesuchsperiodeStatus.AKTIV;
    }

    public getDatumFreischaltungMax(): moment.Moment {
        const gueltigAb = angular.copy(this.gesuchsperiode.gueltigkeit.gueltigAb);

        return gueltigAb.subtract(1, 'days');
    }

    public uploadGesuchsperiodeDokument(file: any[], sprache: TSSprache, dokumentTyp: TSDokumentTyp): void {
        if (file.length <= 0) {
            return;
        }
        const selectedFile = file[0];
        if (selectedFile.size > MAX_FILE_SIZE) {
            this.dvDialog.showDialog(okHtmlDialogTempl, OkHtmlDialogController, {
                title: this.$translate.instant('FILE_ZU_GROSS'),
            });
            return;
        }

        this.uploadRS.uploadGesuchsperiodeDokument(selectedFile, sprache, this.gesuchsperiode.id, dokumentTyp)
            .then(() => {
                switch (dokumentTyp) {
                    case TSDokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG: {
                        this.setErlauterungBoolean(true, sprache);
                        break;
                    } case TSDokumentTyp.VORLAGE_MERKBLATT_TS: {
                        this.setVorlageMerkblattTSBoolean(true, sprache);
                        break;
                    } case TSDokumentTyp.VORLAGE_VERFUEGUNG_LATS: {
                        this.setVorlageVerfuegungLatsBoolean(true, sprache);
                        break;
                    } default: {
                        throw new Error(GesuchsperiodeViewController.DOKUMENT_TYP_NOT_DEFINED);
                    }
                }
            });
    }

    public removeGesuchsperiodeDokument(sprache: TSSprache, dokumentTyp: TSDokumentTyp): void {
        this.gesuchsperiodeRS.removeGesuchsperiodeDokument(this.gesuchsperiode.id, sprache, dokumentTyp)
            .then(() => {
                switch (dokumentTyp) {
                    case TSDokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG: {
                        this.setErlauterungBoolean(false, sprache);
                        break;
                    } case TSDokumentTyp.VORLAGE_MERKBLATT_TS: {
                        this.setVorlageMerkblattTSBoolean(false, sprache);
                        break;
                    } case TSDokumentTyp.VORLAGE_VERFUEGUNG_LATS: {
                        this.setVorlageVerfuegungLatsBoolean(false, sprache);
                        break;
                    } default: {
                        throw new Error(GesuchsperiodeViewController.DOKUMENT_TYP_NOT_DEFINED);
                    }
                }
            });
    }

    public downloadGesuchsperiodeDokument(sprache: TSSprache, dokumentTyp: TSDokumentTyp): void {
        this.gesuchsperiodeRS.downloadGesuchsperiodeDokument(this.gesuchsperiode.id, sprache, dokumentTyp).then(
            response => {
                let file;
                let filename;
                switch (dokumentTyp) {
                    case TSDokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG: {
                        file = new Blob([response], {type: 'application/pdf'});
                        filename = this.$translate.instant('ERLAUTERUNG_ZUR_VERFUEGUNG_DATEI_NAME');
                        break;
                    } case TSDokumentTyp.VORLAGE_MERKBLATT_TS: {
                        file = new Blob([response],
                            {type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'});
                        filename = this.$translate.instant('VORLAGE_MERKBLATT_ANMELDUNG_TAGESSCHULE_DATEI_NAME');
                        break;
                    } case TSDokumentTyp.VORLAGE_VERFUEGUNG_LATS: {
                        file = new Blob([response],
                            {type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'});
                        filename = this.$translate.instant('VORLAGE_VERFUEGUNG_LATS_DATEI_NAME');
                        break;
                    } default: {
                        throw new Error(GesuchsperiodeViewController.DOKUMENT_TYP_NOT_DEFINED);
                    }
                }
                this.downloadRS.openDownload(file, filename);
            });
    }

    private setErlauterungBoolean(value: boolean, sprache: TSSprache): void {
        switch (sprache) {
            case TSSprache.FRANZOESISCH:
                this.isErlaeuterungFR = value;
                break;
            case TSSprache.DEUTSCH:
                this.isErlaeuterungDE = value;
                break;
            default:
                return;
        }
    }

    private updateExistDokumenten(gesuchsperiode: TSGesuchsperiode): void {
        this.gesuchsperiodeRS.existDokument(
            gesuchsperiode.id, TSSprache.DEUTSCH, TSDokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG).then(
            result => {
                this.isErlaeuterungDE = !!result;
            });
        this.gesuchsperiodeRS.existDokument(
            gesuchsperiode.id, TSSprache.FRANZOESISCH, TSDokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG).then(
            result => {
                this.isErlaeuterungFR = !!result;
            });
        this.gesuchsperiodeRS.existDokument(
            gesuchsperiode.id, TSSprache.DEUTSCH, TSDokumentTyp.VORLAGE_MERKBLATT_TS).then(
            result => {
                this.isVorlageMerkblattDE = !!result;
            });
        this.gesuchsperiodeRS.existDokument(
            gesuchsperiode.id, TSSprache.FRANZOESISCH, TSDokumentTyp.VORLAGE_MERKBLATT_TS).then(
            result => {
                this.isVorlageMerkblattFR = !!result;
            });
        this.gesuchsperiodeRS.existDokument(
            gesuchsperiode.id, TSSprache.DEUTSCH, TSDokumentTyp.VORLAGE_VERFUEGUNG_LATS).then(
            result => {
                this.isVorlageVerfuegungLatsDE = !!result;
            });
        this.gesuchsperiodeRS.existDokument(
            gesuchsperiode.id, TSSprache.FRANZOESISCH, TSDokumentTyp.VORLAGE_VERFUEGUNG_LATS).then(
            result => {
                this.isVorlageVerfuegungLatsFR = !!result;
            });
    }

    private setVorlageMerkblattTSBoolean(value: boolean, sprache: TSSprache): void {
        switch (sprache) {
            case TSSprache.FRANZOESISCH:
                this.isVorlageMerkblattFR = value;
                break;
            case TSSprache.DEUTSCH:
                this.isVorlageMerkblattDE = value;
                break;
            default:
                return;
        }
    }

    private setVorlageVerfuegungLatsBoolean(value: boolean, sprache: TSSprache): void {
        switch (sprache) {
            case TSSprache.FRANZOESISCH:
                this.isVorlageVerfuegungLatsFR = value;
                break;
            case TSSprache.DEUTSCH:
                this.isVorlageVerfuegungLatsDE = value;
                break;
            default:
                return;
        }
    }
}
