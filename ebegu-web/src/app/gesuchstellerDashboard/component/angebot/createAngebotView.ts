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
import {GesuchModelManager} from '../../../../gesuch/service/gesuchModelManager';
import {TSBetreuungsangebotTyp} from '../../../../models/enums/TSBetreuungsangebotTyp';
import {TSBetreuungsstatus} from '../../../../models/enums/TSBetreuungsstatus';
import {getTSEinschulungTypValues, TSEinschulungTyp} from '../../../../models/enums/TSEinschulungTyp';
import {TSAnmeldungDTO} from '../../../../models/TSAnmeldungDTO';
import {TSBelegungFerieninsel} from '../../../../models/TSBelegungFerieninsel';
import {TSBelegungTagesschule} from '../../../../models/TSBelegungTagesschule';
import {TSBetreuung} from '../../../../models/TSBetreuung';
import {TSInstitutionStammdaten} from '../../../../models/TSInstitutionStammdaten';
import {TSKindContainer} from '../../../../models/TSKindContainer';
import {DateUtil} from '../../../../utils/DateUtil';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {CONSTANTS} from '../../../core/constants/CONSTANTS';
import {BetreuungRS} from '../../../core/service/betreuungRS.rest';
import {IAngebotStateParams} from '../../gesuchstellerDashboard.route';
import IFormController = angular.IFormController;
import ITranslateService = angular.translate.ITranslateService;

export class CreateAngebotListViewConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./createAngebotView.html');
    public controller = CreateAngebotListViewController;
    public controllerAs = 'vm';
}

export class CreateAngebotListViewController implements IController {

    public static $inject: string[] = [
        '$state',
        'GesuchModelManager',
        '$stateParams',
        'BetreuungRS',
        '$translate',
    ];

    public readonly CONSTANTS: any = CONSTANTS;
    public form: IFormController;
    public einschulungTypValues: Array<TSEinschulungTyp>;
    private ts: boolean;
    private fi: boolean;
    private readonly kindContainer: TSKindContainer;
    private readonly institution: TSInstitutionStammdaten;
    private anmeldungDTO: TSAnmeldungDTO = new TSAnmeldungDTO();

    public constructor(
        private readonly $state: StateService,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly $stateParams: IAngebotStateParams,
        private readonly betreuungRS: BetreuungRS,
        private readonly $translate: ITranslateService,
    ) {
    }

    public $onInit(): void {
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

    public getGesuchsperiodeString(): string | undefined {
        if (this.gesuchModelManager.getGesuchsperiode()) {
            return this.gesuchModelManager.getGesuchsperiode().gesuchsperiodeString;
        }
        return undefined;
    }

    public getInstitutionenSDList(): Array<TSInstitutionStammdaten> {
        const result: Array<TSInstitutionStammdaten> = [];
        this.gesuchModelManager.getActiveInstitutionenForGemeindeList().forEach((instStamm: TSInstitutionStammdaten) => {
            if (this.ts) {
                if (instStamm.betreuungsangebotTyp === TSBetreuungsangebotTyp.TAGESSCHULE) {
                    result.push(instStamm);
                }
            } else if (this.fi && instStamm.betreuungsangebotTyp === TSBetreuungsangebotTyp.FERIENINSEL) {
                result.push(instStamm);
            }
        });
        return result;
    }

    public getTextSprichtAmtssprache(): string {
        return this.$translate.instant('SPRICHT_AMTSSPRACHE',
            {
                amtssprache: EbeguUtil
                    .getAmtsspracheAsString(this.gesuchModelManager.gemeindeStammdaten, this.$translate)
            });
    }

    public getKindContainerList(): Array<TSKindContainer> {
        if (this.gesuchModelManager.getGesuch()) {
            return this.gesuchModelManager.getGesuch().kindContainers;
        }
        return [];
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

    // tslint:disable-next-line:cognitive-complexity
    public selectedInstitutionStammdatenChanged(): void {
        if (!this.anmeldungDTO.betreuung) {
            this.anmeldungDTO.betreuung = new TSBetreuung();
        }
        this.anmeldungDTO.betreuung.institutionStammdaten = this.institution;
        // Default Eintrittsdatum ist erster Schultag, wenn noch in Zukunft

        if (this.ts) {
            // Nur fuer die neuen Gesuchsperiode kann die Belegung erfast werden
            if (this.gesuchModelManager.gemeindeKonfiguration.hasTagesschulenAnmeldung()
                && this.isTageschulenAnmeldungAktiv()) {
                this.anmeldungDTO.betreuung.betreuungsstatus = TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST;
                if (!this.anmeldungDTO.betreuung.belegungTagesschule) {
                    this.anmeldungDTO.betreuung.belegungTagesschule = new TSBelegungTagesschule();
                    // Default Eintrittsdatum ist erster Schultag, wenn noch in Zukunft
                    const ersterSchultag =
                        this.gesuchModelManager.gemeindeKonfiguration.konfigTagesschuleErsterSchultag;
                    if (DateUtil.today().isBefore(ersterSchultag)) {
                        this.anmeldungDTO.betreuung.belegungTagesschule.eintrittsdatum = ersterSchultag;
                    }
                }
            }
            this.anmeldungDTO.betreuung.belegungFerieninsel = undefined;
        } else {
            if (!this.anmeldungDTO.betreuung.belegungFerieninsel) {
                this.anmeldungDTO.betreuung.belegungFerieninsel = new TSBelegungFerieninsel();
            }
            this.anmeldungDTO.betreuung.belegungTagesschule = undefined;
        }
    }

    public isTageschulenAnmeldungAktiv(): boolean {
        return this.gesuchModelManager.gemeindeKonfiguration.isTageschulenAnmeldungAktiv();
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
        this.anmeldungDTO.betreuung.gesuchsperiode = this.gesuchModelManager.getGesuchsperiode();
        if (this.ts) {
            this.anmeldungDTO.betreuung.betreuungsstatus = TSBetreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST;

            this.anmeldungDTO.betreuung.belegungTagesschule.belegungTagesschuleModule =
                this.anmeldungDTO.betreuung.belegungTagesschule.belegungTagesschuleModule
                    .filter(modul => modul.modulTagesschule.angemeldet);

            this.betreuungRS.createAngebot(this.anmeldungDTO).then(() => {
                this.backToHome('TAGESSCHULE_ANMELDUNG_GESPEICHERT');
            }).catch(() => {
                this.anmeldungDTO.betreuung.betreuungsstatus = TSBetreuungsstatus.AUSSTEHEND;
            });
        } else if (this.fi) {
            this.anmeldungDTO.betreuung.betreuungsstatus = TSBetreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST;
            this.betreuungRS.createAngebot(this.anmeldungDTO).then(() => {
                this.kindContainer.kindJA.familienErgaenzendeBetreuung = true;
                this.backToHome('FERIENINSEL_ANMELDUNG_GESPEICHERT');
            }).catch(() => {
                this.anmeldungDTO.betreuung.betreuungsstatus = TSBetreuungsstatus.AUSSTEHEND;
            });
        }
    }

    public backToHome(infoMessage?: string): void {
        this.form.$setPristine();
        this.$state.go('gesuchsteller.dashboard', {
            gesuchstellerDashboardStateParams: {infoMessage},
        });
    }

}
