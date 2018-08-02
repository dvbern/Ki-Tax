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

import {IComponentOptions} from 'angular';
import {StateService} from '@uirouter/core';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import ErrorService from '../../../core/errors/service/ErrorService';
import GesuchsperiodeRS from '../../../core/service/gesuchsperiodeRS.rest';
import MitteilungRS from '../../../core/service/mitteilungRS.rest';
import DossierRS from '../../../gesuch/service/dossierRS.rest';
import GesuchRS from '../../../gesuch/service/gesuchRS.rest';
import SearchRS from '../../../gesuch/service/searchRS.rest';
import {IN_BEARBEITUNG_BASE_NAME, isAnyStatusOfVerfuegt, TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {TSEingangsart} from '../../../models/enums/TSEingangsart';
import {TSGesuchBetreuungenStatus} from '../../../models/enums/TSGesuchBetreuungenStatus';
import TSAntragDTO from '../../../models/TSAntragDTO';
import TSDossier from '../../../models/TSDossier';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import ILogService = angular.ILogService;
import IPromise = angular.IPromise;
import ITranslateService = angular.translate.ITranslateService;

let template = require('./gesuchstellerDashboardView.html');
require('./gesuchstellerDashboardView.less');

export class GesuchstellerDashboardListViewConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = GesuchstellerDashboardViewController;
    controllerAs = 'vm';
}

export class GesuchstellerDashboardViewController {

    private antragList: Array<TSAntragDTO> = [];
    private _activeGesuchsperiodenList: Array<TSGesuchsperiode>;
    dossier: TSDossier;
    totalResultCount: string = '-';
    amountNewMitteilungen: number;
    mapOfNewestAntraege: { [key: string]: string } = {}; // In dieser Map wird pro GP die ID des neuesten Gesuchs gespeichert

    static $inject: string[] = ['$state', '$log', 'AuthServiceRS', 'SearchRS', 'EbeguUtil', 'GesuchsperiodeRS',
        '$translate', 'MitteilungRS', 'GesuchRS', 'ErrorService', 'DossierRS'];

    constructor(private $state: StateService, private $log: ILogService,
                private authServiceRS: AuthServiceRS, private searchRS: SearchRS, private ebeguUtil: EbeguUtil,
                private gesuchsperiodeRS: GesuchsperiodeRS, private $translate: ITranslateService,
                private mitteilungRS: MitteilungRS, private gesuchRS: GesuchRS, private errorService: ErrorService, private dossierRS: DossierRS) {
    }

    $onInit() {
        if (this.$state.params.gesuchstellerDashboardStateParams && this.$state.params.gesuchstellerDashboardStateParams.infoMessage) {
            this.errorService.addMesageAsInfo(this.$translate.instant(this.$state.params.gesuchstellerDashboardStateParams.infoMessage));
        }
        this.initViewModel();
    }

    private initViewModel() {
        this.updateAntragList().then(() => {
            this.getAmountNewMitteilungen();
            this.updateActiveGesuchsperiodenList();
        });
    }

    private updateAntragList(): IPromise<any> {
        //TODO (KIBON-6) Gemeinde!
        return this.dossierRS.getOrCreateDossierAndFallForCurrentUserAsBesitzer('unknown').then((createdDossier: TSDossier) => {
            this.dossier = createdDossier;
            return this.searchRS.getAntraegeGesuchstellerList().then((response: any) => {
                this.antragList = angular.copy(response);
                return this.antragList;
            });
        });
    }

    private readDossierOfCurrentBenutzer(fallId: string) {
        //TODO (KIBON-6) Gemeinde!
        this.dossierRS.getDossierForFallAndGemeinde('unnknown', fallId).then((resultDossier: TSDossier) => {
            if (resultDossier) {
                this.dossier = resultDossier;
            } else {
                this.dossier = undefined;
            }
        });
    }

    private getAmountNewMitteilungen(): void {
        if (this.dossier) {
            this.mitteilungRS.getAmountNewMitteilungenOfDossierForCurrentRolle(this.dossier.id).then((response: number) => {
                this.amountNewMitteilungen = response;
            });
        }
    }

    private updateActiveGesuchsperiodenList(): void {
        this.gesuchsperiodeRS.getAllActiveGesuchsperioden().then((response: TSGesuchsperiode[]) => {
            this._activeGesuchsperiodenList = angular.copy(response);
            // Jetzt sind sowohl die Gesuchsperioden wie die Gesuche des Falles geladen. Wir merken uns das jeweils neueste Gesuch pro Periode
            for (let gp of this._activeGesuchsperiodenList) {
                this.gesuchRS.getIdOfNewestGesuch(gp.id, this.dossier.id).then(response => {
                    this.mapOfNewestAntraege[gp.id] = response;
                });
            }
        });
    }

    public getActiveGesuchsperiodenList(): Array<TSGesuchsperiode> {
        return this._activeGesuchsperiodenList;
    }

    public goToMitteilungenOeffen() {
        this.$state.go('mitteilungen', {
            dossierId: this.dossier.id,
            fallId: this.dossier.fall.id,
        });
    }

    public getFallId(): string {
        if (this.dossier && this.dossier.fall) {
            return this.dossier.fall.id;
        }
        return '';
    }

    public getAntragList(): Array<TSAntragDTO> {
        return this.antragList;
    }

    public displayAnsehenButton(periode: TSGesuchsperiode): boolean {
        let antrag: TSAntragDTO = this.getAntragForGesuchsperiode(periode);
        if (antrag) {
            if (TSAntragStatus.IN_BEARBEITUNG_GS === antrag.status) {
                return false;
            }
            return true;
        }
        return false;
    }

    public getNumberMitteilungen(): number {
        return this.amountNewMitteilungen;
    }

    public openAntrag(periode: TSGesuchsperiode, ansehen: boolean): void {
        let antrag: TSAntragDTO = this.getAntragForGesuchsperiode(periode);
        if (antrag) {
            if (TSAntragStatus.IN_BEARBEITUNG_GS === antrag.status || ansehen) {
                // Noch nicht freigegeben
                this.$state.go('gesuch.fallcreation', {createNew: false, gesuchId: antrag.antragId, dossierId: antrag.dossierId});
            } else if (!isAnyStatusOfVerfuegt(antrag.status) || antrag.beschwerdeHaengig) {
                // Alles ausser verfuegt und InBearbeitung
                this.$state.go('gesuch.dokumente', {gesuchId: antrag.antragId});
            } else {
                // Im Else-Fall ist das Gesuch nicht mehr ueber den Button verfuegbar
                // Es kann nur noch eine Mutation gemacht werden
                this.$state.go('gesuch.mutation', {
                    createMutation: true,
                    eingangsart: TSEingangsart.ONLINE,
                    gesuchsperiodeId: periode.id,
                    gesuchId: antrag.antragId,
                    dossierId: this.dossier.id
                });
            }
        } else {
            // Noch kein Antrag für die Gesuchsperiode vorhanden
            if (this.antragList && this.antragList.length > 0) {
                // Aber schon mindestens einer für eine frühere Periode
                this.$state.go('gesuch.erneuerung', {
                    createErneuerung: true,
                    gesuchsperiodeId: periode.id,
                    eingangsart: TSEingangsart.ONLINE,
                    gesuchId: this.antragList[0].antragId,
                    dossierId: this.dossier.id
                });
            } else {
                // Dies ist das erste Gesuch
                this.$state.go('gesuch.fallcreation', {
                    createNew: true,
                    eingangsart: TSEingangsart.ONLINE,
                    gesuchId: null,
                    gesuchsperiodeId: periode.id,
                    dossierId: this.dossier.id
                });
            }
        }
    }

    public createTagesschule(periode: TSGesuchsperiode): void {
        let antrag: TSAntragDTO = this.getAntragForGesuchsperiode(periode);

        if (antrag) {
            this.$state.go('createAngebot', {type: 'TS', gesuchId: antrag.antragId});
        } else {
            console.error('Fehler: kein Gesuch gefunden für Gesuchsperiode in createTagesschule');
        }
    }

    public createFerieninsel(periode: TSGesuchsperiode): void {
        let antrag: TSAntragDTO = this.getAntragForGesuchsperiode(periode);

        if (antrag) {
            this.$state.go('createAngebot', {type: 'FI', gesuchId: antrag.antragId});
        } else {
            console.error('Fehler: kein Gesuch gefunden für Gesuchsperiode in createFerieninsel');
        }
    }

    public showAnmeldungCreate(periode: TSGesuchsperiode): boolean {
        let antrag: TSAntragDTO = this.getAntragForGesuchsperiode(periode);
        return periode.hasTagesschulenAnmeldung() && !!antrag &&
            antrag.status !== TSAntragStatus.IN_BEARBEITUNG_GS &&
            antrag.status !== TSAntragStatus.FREIGABEQUITTUNG
            && this.isNeuestAntragOfGesuchsperiode(periode, antrag);
    }

    public getButtonText(periode: TSGesuchsperiode): string {
        let antrag: TSAntragDTO = this.getAntragForGesuchsperiode(periode);
        if (antrag) {
            if (TSAntragStatus.IN_BEARBEITUNG_GS === antrag.status) {
                // Noch nicht freigegeben -> Text BEARBEITEN
                return this.$translate.instant('GS_BEARBEITEN');
            } else if (!isAnyStatusOfVerfuegt(antrag.status) || antrag.beschwerdeHaengig) {
                // Alles ausser verfuegt und InBearbeitung -> Text DOKUMENTE HOCHLADEN
                return this.$translate.instant('GS_DOKUMENTE_HOCHLADEN');
            } else if (this.isNeuestAntragOfGesuchsperiode(periode, antrag)) {
                // Im Else-Fall ist das Gesuch nicht mehr ueber den Button verfuegbar
                // Es kann nur noch eine Mutation gemacht werden -> Text MUTIEREN
                return this.$translate.instant('GS_MUTIEREN');
            }
        } else {
            // Noch kein Antrag vorhanden -> Text GESUCH BEANTRAGEN
            return this.$translate.instant('GS_BEANTRAGEN');
        }
        return undefined;
    }

    public editAntrag(antrag: TSAntragDTO): void {
        if (antrag) {
            if (isAnyStatusOfVerfuegt(antrag.status)) {
                this.$state.go('gesuch.verfuegen', {gesuchId: antrag.antragId});
            } else {
                this.$state.go('gesuch.fallcreation', {createNew: false, gesuchId: antrag.antragId, dossierId: antrag.dossierId});
            }
        }
    }

    private getAntragForGesuchsperiode(periode: TSGesuchsperiode): TSAntragDTO {
        // Die Antraege sind nach Laufnummer sortiert, d.h. der erste einer Periode ist immer der aktuellste
        if (this.antragList) {
            for (let antrag of this.antragList) {
                if (antrag.gesuchsperiodeGueltigAb.year() === periode.gueltigkeit.gueltigAb.year()) {
                    return antrag;
                }
            }
        }
        return undefined;
    }

    /**
     * Status muss speziell uebersetzt werden damit Gesuchsteller nur "In Bearbeitung" sieht und nicht in "Bearbeitung Gesuchsteller"
     */
    public translateStatus(antrag: TSAntragDTO) {
        let status: TSAntragStatus = antrag.status;
        let isUserGesuchsteller: boolean = this.authServiceRS.isOneOfRoles(TSRoleUtil.getGesuchstellerOnlyRoles());
        if (status === TSAntragStatus.IN_BEARBEITUNG_GS && isUserGesuchsteller) {
            if (TSGesuchBetreuungenStatus.ABGEWIESEN === antrag.gesuchBetreuungenStatus) {
                return this.ebeguUtil.translateString(TSAntragStatus[TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN]);
            } else if (TSGesuchBetreuungenStatus.WARTEN === antrag.gesuchBetreuungenStatus) {
                return this.ebeguUtil.translateString(TSAntragStatus[TSAntragStatus.PLATZBESTAETIGUNG_WARTEN]);
            }
            return this.ebeguUtil.translateString(IN_BEARBEITUNG_BASE_NAME);
        }
        if ((status === TSAntragStatus.NUR_SCHULAMT)
            && isUserGesuchsteller) {
            return this.ebeguUtil.translateString('ABGESCHLOSSEN');
        }
        return this.ebeguUtil.translateString(TSAntragStatus[status]);
    }

    /**
     * JA und Mischgesuche -> verantwortlicherBG
     * SCHGesuche -> verantwortlicherTS (oder "Schulamt" wenn kein Verantwortlicher vorhanden
     */
    public getHauptVerantwortlicherFullName(antrag: TSAntragDTO): string {
        if (antrag) {
            if (antrag.verantwortlicherBG) {
                return antrag.verantwortlicherBG;
            }
            if (antrag.verantwortlicherTS) {
                return antrag.verantwortlicherTS;
            }
            if (antrag.status === TSAntragStatus.NUR_SCHULAMT) { //legacy for old Faelle where verantwortlicherTS didn't exist
                return this.ebeguUtil.translateString('NUR_SCHULAMT');
            }
        }
        return '';
    }

    public gesperrtWegenMutation(periode: TSGesuchsperiode) {
        let antrag: TSAntragDTO = this.getAntragForGesuchsperiode(periode);
        return !!antrag && !this.isNeuestAntragOfGesuchsperiode(periode, antrag);
    }

    public hasOnlyFerieninsel(periode: TSGesuchsperiode) {
        let antrag: TSAntragDTO = this.getAntragForGesuchsperiode(periode);
        return !!antrag && antrag.hasOnlyFerieninsel();
    }

    private isNeuestAntragOfGesuchsperiode(periode: TSGesuchsperiode, antrag: TSAntragDTO): boolean {
        return antrag.antragId === this.mapOfNewestAntraege[periode.id];
    }
}
