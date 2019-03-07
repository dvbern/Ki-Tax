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
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import GesuchsperiodeRS from '../../../app/core/service/gesuchsperiodeRS.rest';
import {UploadRS} from '../../../app/core/service/uploadRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {RemoveDialogController} from '../../../gesuch/dialog/RemoveDialogController';
import GlobalCacheService from '../../../gesuch/service/globalCacheService';
import {TSCacheTyp} from '../../../models/enums/TSCacheTyp';
import {getTSGesuchsperiodeStatusValues, TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSSprache} from '../../../models/enums/TSSprache';
import TSEinstellung from '../../../models/TSEinstellung';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import {TSDateRange} from '../../../models/types/TSDateRange';
import EbeguUtil from '../../../utils/EbeguUtil';
import AbstractAdminViewController from '../../abstractAdminView';
import {IGesuchsperiodeStateParams} from '../../admin.route';
import {EinstellungRS} from '../../service/einstellungRS.rest';

const removeDialogTemplate = require('../../../gesuch/dialog/removeDialogTemplate.html');

export class GesuchsperiodeViewComponentConfig implements IComponentOptions {
    public transclude: boolean = false;
    public template: string = require('./gesuchsperiodeView.html');
    public controller: any = GesuchsperiodeViewController;
    public controllerAs: string = 'vm';
}

export class GesuchsperiodeViewController extends AbstractAdminViewController {

    public static $inject = [
        'EinstellungRS',
        'DvDialog',
        'GlobalCacheService',
        'GesuchsperiodeRS',
        '$log',
        '$stateParams',
        '$state',
        '$translate',
        'AuthServiceRS',
    ];

    public form: IFormController;
    public gesuchsperiode: TSGesuchsperiode;
    public einstellungenGesuchsperiode: TSEinstellung[];

    public initialStatus: TSGesuchsperiodeStatus;
    public datumFreischaltungTagesschule: moment.Moment;
    public datumFreischaltungMax: moment.Moment;

    public uploadRS: UploadRS;

    public constructor(
        private readonly einstellungenRS: EinstellungRS,
        private readonly dvDialog: DvDialog,
        private readonly globalCacheService: GlobalCacheService,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly $log: ILogService,
        private readonly $stateParams: IGesuchsperiodeStateParams,
        private readonly $state: StateService,
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
        });
    }

    public getTSGesuchsperiodeStatusValues(): Array<TSGesuchsperiodeStatus> {
        return getTSGesuchsperiodeStatusValues();
    }

    private setSelectedGesuchsperiode(gesuchsperiode: any): void {
        this.gesuchsperiode = gesuchsperiode;
        this.readEinstellungenByGesuchsperiode();
        this.datumFreischaltungTagesschule = undefined;
        this.datumFreischaltungMax = this.getDatumFreischaltungMax();
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

        if (this.gesuchsperiode.isNew()
            || this.initialStatus !== this.gesuchsperiode.status
            || this.gesuchsperiode.status === TSGesuchsperiodeStatus.AKTIV) {
            const dialogText = this.getGesuchsperiodeSaveDialogText(this.initialStatus !== this.gesuchsperiode.status);
            this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
                title: 'GESUCHSPERIODE_DIALOG_TITLE',
                deleteText: dialogText,
                parentController: undefined,
                elementID: undefined,
            }).then(() => {
                this.saveGesuchsperiodeFreischaltungTagesschule();
            });
            return;
        }

        this.saveGesuchsperiodeFreischaltungTagesschule();
    }

    public saveGesuchsperiodeFreischaltungTagesschule(): void {
        // Zweite Rückfrage falls neu ein Datum für die Freischaltung der Tagesschulen gesetzt wurde
        if (this.gesuchsperiode.isTagesschulenAnmeldungKonfiguriert() || !this.isDatumFreischaltungTagesschuleValid()) {
            this.doSave();

            return;
        }

        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            title: 'FREISCHALTUNG_TAGESSCHULE_DIALOG_TITLE',
            deleteText: 'FREISCHALTUNG_TAGESSCHULE_DIALOG_TEXT',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {
            this.gesuchsperiode.datumFreischaltungTagesschule = this.datumFreischaltungTagesschule;
            this.doSave();
        });
    }

    private isDatumFreischaltungTagesschuleValid(): boolean {
        return this.datumFreischaltungTagesschule
            && this.datumFreischaltungTagesschule.isBefore(this.gesuchsperiode.gueltigkeit.gueltigAb);
    }

    private doSave(): void {
        this.gesuchsperiodeRS.updateGesuchsperiode(this.gesuchsperiode).then((response: TSGesuchsperiode) => {
            this.gesuchsperiode = response;
            this.datumFreischaltungTagesschule = undefined;
            this.globalCacheService.getCache(TSCacheTyp.EBEGU_EINSTELLUNGEN).removeAll();
            // Die E-BEGU-Parameter für die neue Periode lesen bzw. erstellen, wenn noch nicht vorhanden
            this.readEinstellungenByGesuchsperiode();
            this.gesuchsperiodeRS.updateActiveGesuchsperiodenList(); // reset gesuchperioden in manager
            this.gesuchsperiodeRS.updateNichtAbgeschlosseneGesuchsperiodenList();
            this.initialStatus = this.gesuchsperiode.status;
        });
    }

    public createGesuchsperiode(): void {
        this.gesuchsperiodeRS.getNewestGesuchsperiode().then(newestGeuschsperiode => {
            this.gesuchsperiode = new TSGesuchsperiode(TSGesuchsperiodeStatus.ENTWURF, new TSDateRange());
            this.initialStatus = undefined; // initialStatus ist undefined for new created Gesuchsperioden
            this.datumFreischaltungTagesschule = undefined;
            this.gesuchsperiode.gueltigkeit.gueltigAb =
                newestGeuschsperiode.gueltigkeit.gueltigAb.clone().add(1, 'years');
            this.gesuchsperiode.gueltigkeit.gueltigBis =
                newestGeuschsperiode.gueltigkeit.gueltigBis.clone().add(1, 'years');
            this.gesuchsperiode.datumFreischaltungTagesschule = this.gesuchsperiode.gueltigkeit.gueltigAb;
            this.datumFreischaltungMax = this.getDatumFreischaltungMax();
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

    public ersterSchultagRequired(): boolean {
        return (EbeguUtil.isNotNullOrUndefined(this.gesuchsperiode.datumFreischaltungTagesschule)
            && this.gesuchsperiode.datumFreischaltungTagesschule.isBefore(this.gesuchsperiode.gueltigkeit.gueltigAb))
            || (EbeguUtil.isNotNullOrUndefined(this.datumFreischaltungTagesschule)
                && this.datumFreischaltungTagesschule.isBefore(this.gesuchsperiode.gueltigkeit.gueltigAb));
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

    public uploadErlaeuterung(file: any[], sprache: TSSprache): void {

        // TODO KIBON 352: validierung
        // if (file.size > MAX_FILE_SIZE) {
        //     // DialogBox anzeigen für Files, welche zu gross sind!
        //     let returnString = `${this.$translate.instant('FILE_ZU_GROSS')}<br/><br/>`;
        //     returnString += '<ul>';
        //     for (const file of filesTooBig) {
        //         returnString += '<li>';
        //         returnString += file.name;
        //         returnString += '</li>';
        //     }
        //     returnString += '</ul>';
        //
        //     this.dvDialog.showDialog(okHtmlDialogTempl, OkHtmlDialogController, {
        //         title: returnString,
        //     });
        // }

        if (file.length <= 0) {
            return;
        }
        this.uploadRS.uploadErlaeuterungVerfuegung(file, sprache, this.gesuchsperiode.id).then(response => {
            // const returnedDG = angular.copy(response);
            // TODO KIBON-352: überprüfen
            // this.wizardStepManager.findStepsFromGesuch(this.gesuchModelManager.getGesuch().id)
            //     .then(() => this.handleUpload(returnedDG));
        });
    }
}
