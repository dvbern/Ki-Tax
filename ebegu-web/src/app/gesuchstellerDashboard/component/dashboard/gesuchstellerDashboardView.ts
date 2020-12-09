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
import {IComponentOptions, IController, IPromise} from 'angular';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {GesuchRS} from '../../../../gesuch/service/gesuchRS.rest';
import {SearchRS} from '../../../../gesuch/service/searchRS.rest';
import {IN_BEARBEITUNG_BASE_NAME, isAnyStatusOfVerfuegt, TSAntragStatus} from '../../../../models/enums/TSAntragStatus';
import {TSCreationAction} from '../../../../models/enums/TSCreationAction';
import {TSEingangsart} from '../../../../models/enums/TSEingangsart';
import {TSGesuchBetreuungenStatus} from '../../../../models/enums/TSGesuchBetreuungenStatus';
import {TSAntragDTO} from '../../../../models/TSAntragDTO';
import {TSDossier} from '../../../../models/TSDossier';
import {TSGemeindeKonfiguration} from '../../../../models/TSGemeindeKonfiguration';
import {TSGemeindeStammdaten} from '../../../../models/TSGemeindeStammdaten';
import {TSGesuchsperiode} from '../../../../models/TSGesuchsperiode';
import {DateUtil} from '../../../../utils/DateUtil';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {GesuchsperiodeRS} from '../../../core/service/gesuchsperiodeRS.rest';
import {MitteilungRS} from '../../../core/service/mitteilungRS.rest';
import {IGesuchstellerDashboardStateParams} from '../../gesuchstellerDashboard.route';
import ITranslateService = angular.translate.ITranslateService;

export class GesuchstellerDashboardListViewConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./gesuchstellerDashboardView.html');
    public controller = GesuchstellerDashboardViewController;
    public controllerAs = 'vm';
    public bindings = {
        dossier: '<',
    };
}

export class GesuchstellerDashboardViewController implements IController {

    public static $inject: string[] = [
        '$state',
        '$stateParams',
        'AuthServiceRS',
        'SearchRS',
        'EbeguUtil',
        'GesuchsperiodeRS',
        '$translate',
        'MitteilungRS',
        'GesuchRS',
        'ErrorService',
        'GemeindeRS',
    ];

    private antragList: Array<TSAntragDTO> = [];
    public activeGesuchsperiodenList: Array<TSGesuchsperiode>;
    public dossier: TSDossier;
    public gemeindeStammdaten: TSGemeindeStammdaten;
    public totalResultCount: string = '-';
    public amountNewMitteilungen: number;
    public periodYear: string;
    // In dieser Map wird pro GP die ID des neuesten Gesuchs gespeichert
    public mapOfNewestAntraege: { [key: string]: string } = {};

    public constructor(
        private readonly $state: StateService,
        private readonly $stateParams: IGesuchstellerDashboardStateParams,
        private readonly authServiceRS: AuthServiceRS,
        private readonly searchRS: SearchRS,
        private readonly ebeguUtil: EbeguUtil,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly $translate: ITranslateService,
        private readonly mitteilungRS: MitteilungRS,
        private readonly gesuchRS: GesuchRS,
        private readonly errorService: ErrorService,
        private readonly gemeindeRS: GemeindeRS,
    ) {
    }

    public $onInit(): void {
        if (this.$stateParams.infoMessage) {
            this.errorService.addMesageAsInfo(this.$translate.instant(this.$stateParams.infoMessage));
        }

        this.periodYear = DateUtil
            .calculatePeriodenStartdatumString(this.dossier.gemeinde.betreuungsgutscheineStartdatum);

        this.initViewModel();
        this.loadGemeindeStammdaten();
    }

    private initViewModel(): IPromise<TSAntragDTO[]> {
        return this.searchRS.getAntraegeOfDossier(this.dossier.id).then((response: any) => {
            this.antragList = angular.copy(response);
            this.getAmountNewMitteilungen();
            this.updateActiveGesuchsperiodenList();
            return this.antragList;
        });
    }

    /**
     * Loads the Stammdaten of the gemiende of the current Dossier so we can access them
     * while filling out the Gesuch, wihtout having to load it from server again and again
     */
    private loadGemeindeStammdaten(): void {
        if (!(this.dossier && this.dossier.gemeinde)) {
            return;
        }
        this.gemeindeRS.getGemeindeStammdaten(this.dossier.gemeinde.id)
            .then(stammdaten => {
                this.gemeindeStammdaten = stammdaten;
            });
    }

    private getAmountNewMitteilungen(): void {
        this.mitteilungRS.getAmountNewMitteilungenOfDossierForCurrentRolle(this.dossier.id)
            .then((response: number) => {
                this.amountNewMitteilungen = response;
            });
    }

    private updateActiveGesuchsperiodenList(): void {
        this.gesuchsperiodeRS.getAktivePeriodenForGemeinde(this.dossier.gemeinde.id)
            .then((response: TSGesuchsperiode[]) => {
                this.activeGesuchsperiodenList = response;
                // Jetzt sind sowohl die Gesuchsperioden wie die Gesuche des Falles geladen.
                // Wir merken uns das jeweils neueste Gesuch pro Periode
                response.forEach(gp => {
                    this.gesuchRS.getIdOfNewestGesuchForGesuchsperiode(gp.id, this.dossier.id)
                        .then(id => {
                            this.mapOfNewestAntraege[gp.id] = id;
                        });
                });
            });
    }

    public goToMitteilungenOeffen(): void {
        this.$state.go('mitteilungen.view', {
            dossierId: this.dossier.id,
            fallId: this.dossier.fall.id,
        });
    }

    public getAntragList(): Array<TSAntragDTO> {
        return this.antragList;
    }

    public displayAnsehenButton(periode: TSGesuchsperiode): boolean {
        const antrag = this.getAntragForGesuchsperiode(periode);
        if (!antrag) {
            return false;
        }

        return TSAntragStatus.IN_BEARBEITUNG_GS !== antrag.status;
    }

    public getNumberMitteilungen(): number {
        return this.amountNewMitteilungen;
    }

    public openAntrag(periode: TSGesuchsperiode, ansehen: boolean): void {
        const antrag = this.getAntragForGesuchsperiode(periode);
        const fallcreation = 'gesuch.fallcreation';

        if (antrag) {
            if (TSAntragStatus.IN_BEARBEITUNG_GS === antrag.status || ansehen) {
                // Noch nicht freigegeben
                this.$state.go(fallcreation, {gesuchId: antrag.antragId, dossierId: antrag.dossierId});
            } else if (!isAnyStatusOfVerfuegt(antrag.status) || antrag.beschwerdeHaengig) {
                // Alles ausser verfuegt und InBearbeitung
                this.$state.go('gesuch.dokumente', {gesuchId: antrag.antragId});
            } else {
                // Im Else-Fall ist das Gesuch nicht mehr ueber den Button verfuegbar
                // Es kann nur noch eine Mutation gemacht werden
                this.$state.go('gesuch.mutation', {
                    creationAction: TSCreationAction.CREATE_NEW_MUTATION,
                    eingangsart: TSEingangsart.ONLINE,
                    gesuchsperiodeId: periode.id,
                    gesuchId: antrag.antragId,
                    dossierId: this.dossier.id,
                });
            }
        } else if (this.antragList && this.antragList.length > 0) {
            // Noch kein Antrag für die Gesuchsperiode vorhanden
            // Aber schon mindestens einer für eine frühere Periode
            this.$state.go('gesuch.erneuerung', {
                creationAction: TSCreationAction.CREATE_NEW_FOLGEGESUCH,
                gesuchsperiodeId: periode.id,
                eingangsart: TSEingangsart.ONLINE,
                gesuchId: this.antragList[0].antragId,
                dossierId: this.dossier.id,
            });
        } else {
            // Dies ist das erste Gesuch
            this.$state.go(fallcreation, {
                creationAction: TSCreationAction.CREATE_NEW_GESUCH,
                eingangsart: TSEingangsart.ONLINE,
                gesuchsperiodeId: periode.id,
                gemeindeId: this.dossier.gemeinde.id,
                dossierId: this.dossier.id,
            });
        }
    }

    public createTagesschule(periode: TSGesuchsperiode): void {
        this.createAntrag(periode, 'TS');
    }

    public createFerieninsel(periode: TSGesuchsperiode): void {
        this.createAntrag(periode, 'FI');
    }

    private createAntrag(periode: TSGesuchsperiode, type: 'FI' | 'TS'): void {
        const antrag = this.getAntragForGesuchsperiode(periode);

        if (antrag) {
            this.$state.go('gesuchsteller.createAngebot', {type, gesuchId: antrag.antragId});
        } else {
            console.error('Fehler: kein Gesuch gefunden für Gesuchsperiode und Typ ' + type);
        }
    }

    private loadGemeindeKonfiguration(gp: TSGesuchsperiode): TSGemeindeKonfiguration {
        if (this.gemeindeStammdaten) {
            for (const konfigurationsListeElement of this.gemeindeStammdaten.konfigurationsListe) {
                // tslint:disable-next-line:early-exit
                if (konfigurationsListeElement.gesuchsperiode.id === gp.id) {
                    konfigurationsListeElement.initProperties();
                    return konfigurationsListeElement;
                }
            }
        }
        return undefined;
    }

    public showAnmeldungTagesschuleCreate(periode: TSGesuchsperiode): boolean {
        if (this.gemeindeStammdaten) {
            return this.gemeindeStammdaten.gemeinde.angebotTS && this.showAnmeldungCreateTS(periode)
                && periode.gueltigkeit.gueltigBis.isAfter(this.gemeindeStammdaten.gemeinde.tagesschulanmeldungenStartdatum);
        }
        return undefined;
    }

    public showAnmeldungFerieninselCreate(periode: TSGesuchsperiode): boolean {
        if (this.gemeindeStammdaten) {
            return this.gemeindeStammdaten.gemeinde.angebotFI && this.showAnmeldungCreateFI(periode) &&
                periode.gueltigkeit.gueltigBis.isAfter(this.gemeindeStammdaten.gemeinde.ferieninselanmeldungenStartdatum);
        }
        return undefined;
    }

    private showAnmeldungCreateTS(periode: TSGesuchsperiode): boolean {
        const antrag = this.getAntragForGesuchsperiode(periode);
        const tsEnabledForMandant = this.authServiceRS.hasMandantAngebotTS();
        const tsEnabledForGemeinde = this.loadGemeindeKonfiguration(periode).hasTagesschulenAnmeldung();
        return tsEnabledForMandant
            && tsEnabledForGemeinde
            && !!antrag
            && antrag.status !== TSAntragStatus.IN_BEARBEITUNG_GS
            && antrag.status !== TSAntragStatus.FREIGABEQUITTUNG
            && this.isNeuestAntragOfGesuchsperiode(periode, antrag);
    }

    private showAnmeldungCreateFI(periode: TSGesuchsperiode): boolean {
        const antrag = this.getAntragForGesuchsperiode(periode);
        const fiEnabledForMandant = this.authServiceRS.hasMandantAngebotFI();
        const fiEnabledForGemeinde = this.loadGemeindeKonfiguration(periode).hasFerieninseAnmeldung();
        return fiEnabledForMandant
            && fiEnabledForGemeinde
            && !!antrag
            && antrag.status !== TSAntragStatus.IN_BEARBEITUNG_GS
            && antrag.status !== TSAntragStatus.FREIGABEQUITTUNG
            && this.isNeuestAntragOfGesuchsperiode(periode, antrag);
    }

    public getButtonText(periode: TSGesuchsperiode): string {
        const antrag = this.getAntragForGesuchsperiode(periode);
        if (antrag) {
            if (TSAntragStatus.IN_BEARBEITUNG_GS === antrag.status) {
                // Noch nicht freigegeben -> Text BEARBEITEN
                return this.$translate.instant('GS_BEARBEITEN');
            }
            if (!isAnyStatusOfVerfuegt(antrag.status) || antrag.beschwerdeHaengig) {
                // Alles ausser verfuegt und InBearbeitung -> Text DOKUMENTE HOCHLADEN
                return this.$translate.instant('GS_DOKUMENTE_HOCHLADEN');
            }
            if (this.isNeuestAntragOfGesuchsperiode(periode, antrag)) {
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
        if (!antrag) {
            return;
        }

        if (isAnyStatusOfVerfuegt(antrag.status)) {
            this.$state.go('gesuch.verfuegen', {gesuchId: antrag.antragId});
        } else {
            this.$state.go('gesuch.fallcreation', {gesuchId: antrag.antragId, dossierId: antrag.dossierId});
        }
    }

    private getAntragForGesuchsperiode(periode: TSGesuchsperiode): TSAntragDTO {
        // Die Antraege sind nach Laufnummer sortiert, d.h. der erste einer Periode ist immer der aktuellste
        if (this.antragList) {
            for (const antrag of this.antragList) {
                if (antrag.gesuchsperiodeGueltigAb.year() === periode.gueltigkeit.gueltigAb.year()) {
                    return antrag;
                }
            }
        }
        return undefined;
    }

    /**
     * Status muss speziell uebersetzt werden damit Gesuchsteller nur "In Bearbeitung" sieht und nicht in
     * "Bearbeitung Gesuchsteller"
     */
    public translateStatus(antrag: TSAntragDTO): string {
        const status = antrag.status;
        const isUserGesuchsteller = this.authServiceRS.isOneOfRoles(TSRoleUtil.getGesuchstellerOnlyRoles());
        if (status === TSAntragStatus.IN_BEARBEITUNG_GS && isUserGesuchsteller) {
            if (TSGesuchBetreuungenStatus.ABGEWIESEN === antrag.gesuchBetreuungenStatus) {
                return this.ebeguUtil.translateString(TSAntragStatus[TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN]);
            }
            if (TSGesuchBetreuungenStatus.WARTEN === antrag.gesuchBetreuungenStatus) {
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
            if (antrag.status === TSAntragStatus.NUR_SCHULAMT) {
                // legacy for old Faelle where verantwortlicherTS didn't exist
                return this.ebeguUtil.translateString('NUR_SCHULAMT');
            }
        }
        return '';
    }

    public gesperrtWegenMutation(periode: TSGesuchsperiode): boolean {
        const antrag = this.getAntragForGesuchsperiode(periode);
        return !!antrag && !this.isNeuestAntragOfGesuchsperiode(periode, antrag);
    }

    public hasOnlyFerieninsel(periode: TSGesuchsperiode): boolean {
        const antrag = this.getAntragForGesuchsperiode(periode);
        return !!antrag && antrag.hasOnlyFerieninsel();
    }

    private isNeuestAntragOfGesuchsperiode(periode: TSGesuchsperiode, antrag: TSAntragDTO): boolean {
        return antrag.antragId === this.mapOfNewestAntraege[periode.id];
    }
}
