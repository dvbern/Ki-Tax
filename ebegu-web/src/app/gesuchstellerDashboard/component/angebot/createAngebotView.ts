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
import {IComponentOptions} from 'angular';
import * as moment from 'moment';
import BetreuungRS from '../../../core/service/betreuungRS.rest';
import GesuchModelManager from '../../../../gesuch/service/gesuchModelManager';
import {TSBetreuungsangebotTyp} from '../../../../models/enums/TSBetreuungsangebotTyp';
import {TSBetreuungsstatus} from '../../../../models/enums/TSBetreuungsstatus';
import {getTSEinschulungTypValues, TSEinschulungTyp} from '../../../../models/enums/TSEinschulungTyp';
import TSAnmeldungDTO from '../../../../models/TSAnmeldungDTO';
import TSBelegungFerieninsel from '../../../../models/TSBelegungFerieninsel';
import TSBelegungTagesschule from '../../../../models/TSBelegungTagesschule';
import TSBetreuung from '../../../../models/TSBetreuung';
import TSInstitutionStammdaten from '../../../../models/TSInstitutionStammdaten';
import TSKindContainer from '../../../../models/TSKindContainer';
import DateUtil from '../../../../utils/DateUtil';
import {IAngebotStateParams} from '../../gesuchstellerDashboard.route';
import IFormController = angular.IFormController;
import ILogService = angular.ILogService;

export class CreateAngebotListViewConfig implements IComponentOptions {
    transclude = false;
    template = require('./createAngebotView.html');
    controller = CreateAngebotListViewController;
    controllerAs = 'vm';
}

export class CreateAngebotListViewController {

    static $inject: string[] = ['$state', '$log', 'GesuchModelManager', '$stateParams', 'BetreuungRS'];

    form: IFormController;
    einschulungTypValues: Array<TSEinschulungTyp>;
    private ts: boolean;
    private fi: boolean;
    private readonly kindContainer: TSKindContainer;
    private readonly institution: TSInstitutionStammdaten;
    private anmeldungDTO: TSAnmeldungDTO = new TSAnmeldungDTO;

    constructor(private readonly $state: StateService, private readonly $log: ILogService,
                private readonly gesuchModelManager: GesuchModelManager, private readonly $stateParams: IAngebotStateParams,
                private readonly betreuungRS: BetreuungRS) {
    }

    $onInit() {
        this.anmeldungDTO = new TSAnmeldungDTO();
        this.einschulungTypValues = getTSEinschulungTypValues();
        if (this.$stateParams.type === 'TS') {
            this.ts = true;
        } else if (this.$stateParams.type === 'FI') {
            this.fi = true;
        } else {
            console.error('type must be set!');
            this.backToHome();
        }
    }

    public getGesuchsperiodeString(): string {
        return this.gesuchModelManager.getGesuchsperiode().gesuchsperiodeString;
    }

    public getInstitutionenSDList(): Array<TSInstitutionStammdaten> {
        const result: Array<TSInstitutionStammdaten> = [];
        /*if (this.betreuungsangebot) {*/
        this.gesuchModelManager.getActiveInstitutionenList().forEach((instStamm: TSInstitutionStammdaten) => {
            if (this.ts) {
                if (instStamm.betreuungsangebotTyp === TSBetreuungsangebotTyp.TAGESSCHULE && this.gesuchModelManager.isDefaultTagesschuleAllowed(instStamm)) {
                    result.push(instStamm);
                }
            } else if (this.fi) {
                if (instStamm.betreuungsangebotTyp === TSBetreuungsangebotTyp.FERIENINSEL) {
                    result.push(instStamm);
                }
            }
        });
        return result;
    }

    public getKindContainerList(): Array<TSKindContainer> {

        return this.gesuchModelManager.getGesuch().kindContainers;

    }

    public showInstitutionSelect(): boolean {
        return !!this.kindContainer;
    }

    public displayModuleTagesschule(): boolean {
        return this.ts && !!this.institution;
    }

    public displayModuleFerieninsel(): boolean {
        return this.fi && !!this.institution;
    }

    public selectedInstitutionStammdatenChanged(): void {
        if (!this.anmeldungDTO.betreuung) {
            this.anmeldungDTO.betreuung = new TSBetreuung();
        }
        this.anmeldungDTO.betreuung.institutionStammdaten = this.institution;
        // Default Eintrittsdatum ist erster Schultag, wenn noch in Zukunft

        if (this.ts) {
            // Nur fuer die neuen Gesuchsperiode kann die Belegung erfast werden
            if (this.gesuchModelManager.getGesuchsperiode().hasTagesschulenAnmeldung()
                && this.isTageschulenAnmeldungAktiv()) {
                this.anmeldungDTO.betreuung.betreuungsstatus = TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST;
                if (!this.anmeldungDTO.betreuung.belegungTagesschule) {
                    this.anmeldungDTO.betreuung.belegungTagesschule = new TSBelegungTagesschule();
                    // Default Eintrittsdatum ist erster Schultag, wenn noch in Zukunft
                    const ersterSchultag: moment.Moment = this.gesuchModelManager.getGesuchsperiode().datumErsterSchultag;
                    if (DateUtil.today().isBefore(ersterSchultag)) {
                        this.anmeldungDTO.betreuung.belegungTagesschule.eintrittsdatum = ersterSchultag;
                    }
                }
            } else {
                // "Alte" Tagesschule: Noch keine Modulanmeldung moeglich. Wir setzen Default-Institution
                this.anmeldungDTO.betreuung.betreuungsstatus = TSBetreuungsstatus.SCHULAMT;

            }
            this.anmeldungDTO.betreuung.belegungFerieninsel = undefined;
        } else {
            if (!this.anmeldungDTO.betreuung.belegungFerieninsel) {
                this.anmeldungDTO.betreuung.belegungFerieninsel = new TSBelegungFerieninsel();
            }
            this.anmeldungDTO.betreuung.belegungTagesschule = undefined;
        }

    }

    public isTageschulenAnmeldungAktiv() {
        return this.gesuchModelManager.getGesuchsperiode().isTageschulenAnmeldungAktiv();
    }

    public selectedKindChanged(): void {
        if (this.kindContainer) {
            this.anmeldungDTO.additionalKindQuestions = !this.kindContainer.kindJA.familienErgaenzendeBetreuung;
            this.anmeldungDTO.kindContainerId = this.kindContainer.id;
        }
    }

    public getDatumEinschulung(): moment.Moment {
        return this.gesuchModelManager.getGesuchsperiodeBegin();
    }

    public anmeldenSchulamt(): void {
        if (this.ts) {
            this.anmeldungDTO.betreuung.betreuungsstatus = TSBetreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST;

            this.anmeldungDTO.betreuung.belegungTagesschule.moduleTagesschule = this.anmeldungDTO.betreuung.belegungTagesschule.moduleTagesschule
                .filter(modul => modul.angemeldet === true);

            this.betreuungRS.createAngebot(this.anmeldungDTO).then((response: any) => {
                this.backToHome('TAGESSCHULE_ANMELDUNG_GESPEICHERT');
            }).catch(() => {
                this.anmeldungDTO.betreuung.betreuungsstatus = TSBetreuungsstatus.AUSSTEHEND;
            });
        } else if (this.fi) {
            this.anmeldungDTO.betreuung.betreuungsstatus = TSBetreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST;
            this.betreuungRS.createAngebot(this.anmeldungDTO).then((response: any) => {
                this.kindContainer.kindJA.familienErgaenzendeBetreuung = true;
                this.backToHome('FERIENINSEL_ANMELDUNG_GESPEICHERT');
            }).catch(() => {
                this.anmeldungDTO.betreuung.betreuungsstatus = TSBetreuungsstatus.AUSSTEHEND;
            });

        }
    }

    public backToHome(infoMessage: string | undefined = undefined) {
        this.form.$setPristine();
        this.$state.go('gesuchsteller.dashboard', {
            gesuchstellerDashboardStateParams: {infoMessage: infoMessage}
        });
    }

}
