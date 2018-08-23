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
import {IComponentOptions, IFormController, ILogService} from 'angular';
import {IDVFocusableController} from '../../../app/core/component/IDVFocusableController';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import GesuchsperiodeRS from '../../../app/core/service/gesuchsperiodeRS.rest';
import MitteilungRS from '../../../app/core/service/mitteilungRS.rest';
import UserRS from '../../../app/core/service/userRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {
    isAnyStatusOfVerfuegt,
    isAtLeastFreigegebenOrFreigabequittung,
    isStatusVerfuegenVerfuegt
} from '../../../models/enums/TSAntragStatus';
import {TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {TSCreationAction} from '../../../models/enums/TSCreationAction';
import {TSEingangsart} from '../../../models/enums/TSEingangsart';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSMitteilungEvent} from '../../../models/enums/TSMitteilungEvent';
import {TSRole} from '../../../models/enums/TSRole';
import TSAntragDTO from '../../../models/TSAntragDTO';
import TSDossier from '../../../models/TSDossier';
import TSGesuch from '../../../models/TSGesuch';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import EbeguUtil from '../../../utils/EbeguUtil';
import {NavigationUtil} from '../../../utils/NavigationUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';

import {ShowTooltipController} from '../../dialog/ShowTooltipController';
import DossierRS from '../../service/dossierRS.rest';
import GesuchModelManager from '../../service/gesuchModelManager';
import GesuchRS from '../../service/gesuchRS.rest';
import IPromise = angular.IPromise;
import IScope = angular.IScope;

const showKontaktTemplate = require('../../../gesuch/dialog/showKontaktTemplate.html');
const removeDialogTempl = require('../../dialog/removeDialogTemplate.html');

// TODO hefa multiple components in 1 file!?

export class DossierToolbarComponentConfig implements IComponentOptions {
    transclude = false;
    bindings = {
        gesuchid: '@',
        dossierId: '@',
        isDashboardScreen: '@',
        hideActionButtons: '@',
        forceLoadingFromFall: '@'
    };

    template = require('./dossierToolbar.html');
    controller = DossierToolbarController;
    controllerAs = 'vmx';
}

export class DossierToolbarGesuchstellerComponentConfig implements IComponentOptions {
    transclude = false;
    bindings = {
        gesuchid: '@',
        dossierId: '@',
        isDashboardScreen: '@',
        hideActionButtons: '@',
        forceLoadingFromFall: '@'
    };
    template = require('./dossierToolbarGesuchsteller.html');
    controller = DossierToolbarController;
    // Darf, wie es scheint nicht 'vm' heissen, sonst werden im dossierToolBarGesuchsteller.html keine Funktionen
    // gefunden. Bug?!
    controllerAs = 'vmgs';
}

export class DossierToolbarController implements IDVFocusableController {

    static $inject = ['EbeguUtil', 'GesuchRS', '$state',
        '$scope', 'GesuchModelManager', 'AuthServiceRS', '$mdSidenav', '$log', 'GesuchsperiodeRS',
        'DvDialog', 'unsavedWarningSharedService', 'MitteilungRS', 'DossierRS', 'UserRS'];

    antragList: Array<TSAntragDTO>;
    gesuchid: string;
    isDashboardScreen: boolean;
    hideActionButtons: boolean;
    TSRoleUtil = TSRoleUtil;
    forceLoadingFromFall: boolean;
    dossierId: string;
    dossier: TSDossier;

    gesuchsperiodeList: { [key: string]: Array<TSAntragDTO> } = {};
    gesuchNavigationList: { [key: string]: Array<string> } = {};   //mapped z.B. '2006 / 2007' auf ein array mit den
                                                                   // Namen der Antraege
    antragTypList: { [key: string]: TSAntragDTO } = {};
    gemeindeId: string;
    mutierenPossibleForCurrentAntrag: boolean = false;
    erneuernPossibleForCurrentAntrag: boolean = false;
    neuesteGesuchsperiode: TSGesuchsperiode;
    amountNewMitteilungenGS: number = 0;

    constructor(private readonly ebeguUtil: EbeguUtil,
                private readonly gesuchRS: GesuchRS,
                private readonly $state: StateService, private readonly $scope: IScope,
                private readonly gesuchModelManager: GesuchModelManager,
                private readonly authServiceRS: AuthServiceRS,
                private readonly $mdSidenav: ng.material.ISidenavService,
                private readonly $log: ILogService,
                private readonly gesuchsperiodeRS: GesuchsperiodeRS,
                private readonly dvDialog: DvDialog,
                private readonly unsavedWarningSharedService: any,
                private readonly mitteilungRS: MitteilungRS,
                private readonly dossierRS: DossierRS) {

    }

    $onInit() {
        this.updateAntragDTOList();
        //add watchers
        this.addWatchers(this.$scope);
        this.gesuchsperiodeRS.getAllActiveGesuchsperioden().then((response: TSGesuchsperiode[]) => {
            // Die neueste ist zuoberst
            this.neuesteGesuchsperiode = response[0];
        });
    }

    private updateAmountNewMitteilungenGS(): void {
        this.mitteilungRS.getAmountNewMitteilungenOfDossierForCurrentRolle(this.dossierId).then((response: number) => {
            this.amountNewMitteilungenGS = response;
        });
    }

    public getAmountNewMitteilungenGS(): string {
        return '(' + this.amountNewMitteilungenGS + ')';
    }

    public toggleSidenav(componentId: string): void {
        this.$mdSidenav(componentId).toggle();
    }

    public closeSidenav(componentId: string): void {
        this.$mdSidenav(componentId).close();
    }

    public logout(): void {
        this.$state.go('authentication.login', {type: 'logout'});
    }

    private addWatchers($scope: angular.IScope) {
        // needed because of test is not able to inject $scope!
        if ($scope) {
            //watcher fuer gesuch id change
            $scope.$watch(() => {
                return this.gesuchid;
            }, (newValue, oldValue) => {
                if (newValue !== oldValue) {
                    if (this.gesuchid) {
                        this.updateAntragDTOList();
                    } else {
                        this.gemeindeId = null;
                        this.antragTypList = {};
                        this.gesuchNavigationList = {};
                        this.gesuchsperiodeList = {};
                        this.antragList = [];
                        this.antragMutierenPossible(); //neu berechnen ob mutieren moeglich ist
                        this.antragErneuernPossible();
                    }
                }
            });
            //watcher fuer status change
            if (this.gesuchModelManager && this.getGesuch()) {
                $scope.$watch(() => {
                    if (this.getGesuch()) {
                        return this.getGesuch().status;
                    }
                    return undefined;
                }, (newValue, oldValue) => {
                    if ((newValue !== oldValue) && (isAnyStatusOfVerfuegt(newValue))) {
                        this.updateAntragDTOList();
                    }
                });
            }
            //watcher fuer dossier id change
            $scope.$watch(() => {
                return this.dossierId;
            }, (newValue, oldValue) => {
                if (newValue !== oldValue) {
                    if (this.dossierId) {
                        this.updateAntragDTOList();
                        this.updateAmountNewMitteilungenGS();
                    } else {
                        // Fall-ID hat auf undefined gewechselt -> Fall zuruecksetzen
                        this.dossierId = undefined;
                        this.antragTypList = {};
                        this.gesuchNavigationList = {};
                        this.gesuchsperiodeList = {};
                        this.antragList = [];
                        this.antragMutierenPossible(); //neu berechnen ob mutieren moeglich ist
                        this.antragErneuernPossible();
                    }
                }
            });
            // Wenn eine Mutationsmitteilung uebernommen wird und deshalb eine neue Mutation erstellt wird, muss
            // die toolbar aktualisisert werden, damit diese Mutation auf der Liste erscheint
            $scope.$on(TSMitteilungEvent[TSMitteilungEvent.MUTATIONSMITTEILUNG_NEUE_MUTATION], () => {
                this.updateAntragDTOList();
            });
        }
    }

    public showGesuchPeriodeNavigationMenu(): boolean {
        return !this.isDashboardScreen && !angular.equals(this.gesuchsperiodeList, {})
            && !this.authServiceRS.isRole(TSRole.STEUERAMT);
    }

    /**
     * Die Liste wird nicht angezeigt wenn sie leer ist oder wenn der Benutzer sich auf dem Dashboard befindet
     */
    public showAntragTypListNavigationMenu(): boolean {
        return !this.isDashboardScreen && !angular.equals(this.antragTypList, {})
            && !this.authServiceRS.isRole(TSRole.STEUERAMT);
    }

    public showKontaktMenu(): boolean {
        if (this.getGesuch() && this.getGesuch().gesuchsteller1) {
            return true;
        }
        return false;
    }

    public updateAntragDTOList(): void {
        if (this.dossierId) {
            this.dossierRS.findDossier(this.dossierId).then((response: TSDossier) => {
                if (response) {
                    this.dossier = response;
                    this.gemeindeId = this.dossier.gemeinde.id;
                    if (!this.forceLoadingFromFall && this.getGesuch() && this.getGesuch().id) {
                        this.gesuchRS.getAllAntragDTOForDossier(this.getGesuch().dossier.id).then((response) => {
                            this.antragList = angular.copy(response);
                            this.updateGesuchperiodeList();
                            this.updateGesuchNavigationList();
                            this.updateAntragTypList();
                            this.antragMutierenPossible();
                            this.antragErneuernPossible();
                        });
                    } else if (this.dossier) {
                        this.gesuchRS.getAllAntragDTOForDossier(this.dossier.id).then((response) => {
                            this.antragList = angular.copy(response);
                            if (response && response.length > 0) {
                                const newest = this.getNewest(this.antragList);
                                this.gesuchRS.findGesuch(newest.antragId).then((response) => {
                                    if (!response) {
                                        this.$log.warn('Could not find gesuch for id ' + newest.antragId);
                                    }
                                    this.gesuchModelManager.setGesuch(angular.copy(response));
                                    this.updateGesuchperiodeList();
                                    this.updateGesuchNavigationList();
                                    this.updateAntragTypList();
                                    this.antragMutierenPossible();
                                    this.antragErneuernPossible();
                                });
                            } else {
                                // Wenn das Gesuch noch neu ist, sind wir noch ungespeichert auf der FallCreation-Seite
                                // In diesem Fall durfen wir das Gesuch nicht zuruecksetzen
                                if (!this.gesuchModelManager.getGesuch() || !this.gesuchModelManager.getGesuch().isNew()) {
                                    // in this case there is no Gesuch for this fall, so we remove all content
                                    const gesuch = new TSGesuch();
                                    gesuch.dossier = angular.copy(this.dossier);
                                    this.gesuchModelManager.setGesuch(gesuch);
                                    this.resetNavigationParameters();
                                }
                            }
                        });
                        this.updateAmountNewMitteilungenGS();
                    } else {
                        this.resetNavigationParameters();
                    }
                }
            });
        }
        this.forceLoadingFromFall = false; // reset it because it's not needed any more
    }

    private resetNavigationParameters() {
        this.gesuchsperiodeList = {};
        this.gesuchNavigationList = {};
        this.antragTypList = {};
        this.antragMutierenPossible();
        this.antragErneuernPossible();
    }

    private updateGesuchperiodeList() {
        this.gesuchsperiodeList = {};
        // tslint:disable-next-line:prefer-for-of
        for (let i = 0; i < this.antragList.length; i++) {
            const gs = this.antragList[i].gesuchsperiodeString;

            if (!this.gesuchsperiodeList[gs]) {
                this.gesuchsperiodeList[gs] = [];
            }
            this.gesuchsperiodeList[gs].push(this.antragList[i]);
        }
    }

    private updateGesuchNavigationList() {
        this.gesuchNavigationList = {};  // clear
        // tslint:disable-next-line:prefer-for-of
        for (let i = 0; i < this.antragList.length; i++) {
            const gs = this.antragList[i].gesuchsperiodeString;
            const antrag: TSAntragDTO = this.antragList[i];

            if (!this.gesuchNavigationList[gs]) {
                this.gesuchNavigationList[gs] = [];
            }
            this.gesuchNavigationList[gs].push(this.ebeguUtil
                .getAntragTextDateAsString(antrag.antragTyp, antrag.eingangsdatum, antrag.laufnummer));
        }
    }

    private updateAntragTypList() {
        this.antragTypList = {};  //clear
        // tslint:disable-next-line:prefer-for-of
        for (let i = 0; i < this.antragList.length; i++) {
            const antrag: TSAntragDTO = this.antragList[i];
            if (this.getGesuch().gesuchsperiode.gueltigkeit.gueltigAb.isSame(antrag.gesuchsperiodeGueltigAb)) {
                const txt = this.ebeguUtil.getAntragTextDateAsString(antrag.antragTyp,
                    antrag.eingangsdatum,
                    antrag.laufnummer);

                this.antragTypList[txt] = antrag;
            }

        }
    }

    getKeys(map: { [key: string]: Array<TSAntragDTO> }): Array<String> {
        const keys: Array<String> = [];
        for (const key in map) {
            if (map.hasOwnProperty(key)) {
                keys.push(key);
            }
        }
        return keys;
    }

    public getGesuch(): TSGesuch {
        return this.gesuchModelManager.getGesuch();
    }

    public getCurrentGesuchsperiode(): string {
        if (this.getGesuch() && this.getGesuch().gesuchsperiode) {
            return this.getGesuchsperiodeAsString(this.getGesuch().gesuchsperiode);
        } else {
            return '';
        }
    }

    public getAntragTyp(): string {
        if (this.getGesuch()) {
            return this.ebeguUtil.getAntragTextDateAsString(this.getGesuch().typ,
                this.getGesuch().eingangsdatum,
                this.getGesuch().laufnummer);
        } else {
            return '';
        }
    }

    public getGesuchsperiodeAsString(tsGesuchsperiode: TSGesuchsperiode) {
        return tsGesuchsperiode.gesuchsperiodeString;
    }

    public setGesuchsperiode(gesuchsperiodeKey: string) {
        const selectedGesuche = this.gesuchsperiodeList[gesuchsperiodeKey];
        const selectedGesuch: TSAntragDTO = this.getNewest(selectedGesuche);

        this.goToOpenGesuch(selectedGesuch.antragId);
    }

    private getNewest(arrayTSAntragDTO: Array<TSAntragDTO>): TSAntragDTO {
        let newest: TSAntragDTO = arrayTSAntragDTO[0];
        // tslint:disable-next-line:prefer-for-of
        for (let i = 0; i < arrayTSAntragDTO.length; i++) {
            // Wenn eines noch gar kein Eingangsdatum hat ist es sicher das neueste
            if (!arrayTSAntragDTO[i].eingangsdatum) {
                return arrayTSAntragDTO[i];
            }
            if (arrayTSAntragDTO[i].eingangsdatum.isAfter(newest.eingangsdatum)) {
                newest = arrayTSAntragDTO[i];
            }
        }
        return newest;
    }

    /**
     * Institutionen werden zum Screen Betreuungen geleitet, waehrend alle anderen Benutzer zu fallCreation gehen
     */
    private goToOpenGesuch(gesuchId: string): void {
        if (gesuchId) {
            NavigationUtil.navigateToStartsiteOfGesuchForRole(
                this.authServiceRS.getPrincipalRole(),
                this.$state,
                gesuchId
            );
        }
    }

    public setAntragTypDatum(antragTypDatumKey: string) {
        const selectedAntragTypGesuch = this.antragTypList[antragTypDatumKey];
        this.goToOpenGesuch(selectedAntragTypGesuch.antragId);
    }

    public setAntragTypDatumMobile(gesuchperiodeKey: string, antragTypDatumKey: string) {
        const tmpAntragList: { [key: string]: TSAntragDTO } = {};
        // tslint:disable-next-line:prefer-for-of
        for (let i = 0; i < this.antragList.length; i++) {
            const antrag: TSAntragDTO = this.antragList[i];
            if (this.gesuchsperiodeList[gesuchperiodeKey][0].gesuchsperiodeGueltigAb.isSame(antrag.gesuchsperiodeGueltigAb)) {
                const txt = this.ebeguUtil.getAntragTextDateAsString(antrag.antragTyp,
                    antrag.eingangsdatum,
                    antrag.laufnummer);
                tmpAntragList[txt] = antrag;
            }
        }
        const selectedAntragTypGesuch = tmpAntragList[antragTypDatumKey];
        this.goToOpenGesuch(selectedAntragTypGesuch.antragId);
    }

    public showButtonMutieren(): boolean {
        if (this.hideActionButtons) {
            return false;
        }
        if (this.getGesuch()) {
            if (this.getGesuch().isNew()) {
                return false;
            }
            // Wenn die Gesuchsperiode geschlossen ist, kann sowieso keine Mutation mehr gemacht werden
            if (this.getGesuch().gesuchsperiode && this.getGesuch().gesuchsperiode.status === TSGesuchsperiodeStatus.GESCHLOSSEN) {
                return false;
            }
        }
        return this.mutierenPossibleForCurrentAntrag;
    }

    private antragMutierenPossible(): void {
        if (this.antragList && this.antragList.length !== 0) {
            let mutierenGesperrt = false;
            // tslint:disable-next-line:prefer-for-of
            for (let i = 0; i < this.antragList.length; i++) {
                const antragItem: TSAntragDTO = this.antragList[i];
                // Wir muessen nur die Antraege der aktuell ausgewaehlten Gesuchsperiode beachten
                if (antragItem.gesuchsperiodeString === this.getCurrentGesuchsperiode()) {
                    // Falls wir ein Gesuch finden das nicht verfuegt ist oder eine Beschwerde hängig ist, darf nicht
                    // mutiert werden
                    if (antragItem.verfuegt === false || antragItem.beschwerdeHaengig === true) {
                        mutierenGesperrt = true;
                        break;
                    }
                }
            }
            this.mutierenPossibleForCurrentAntrag = !mutierenGesperrt;
        } else {
            this.mutierenPossibleForCurrentAntrag = false;
        }
    }

    public antragMutieren(): void {
        this.mutierenPossibleForCurrentAntrag = false;
        let eingangsart: TSEingangsart;
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getGesuchstellerOnlyRoles())) {
            eingangsart = TSEingangsart.ONLINE;
        } else {
            eingangsart = TSEingangsart.PAPIER;
        }
        this.$state.go('gesuch.mutation', {
            creationAction: TSCreationAction.CREATE_NEW_MUTATION,
            eingangsart: eingangsart,
            gesuchsperiodeId: this.getGesuch().gesuchsperiode.id,
            gesuchId: this.getGesuchIdFuerMutationOrErneuerung(),
            dossierId: this.getGesuch().dossier.id,
        });
    }

    private antragErneuernPossible(): void {
        if (this.antragList && this.antragList.length !== 0) {
            let erneuernGesperrt = false;
            // tslint:disable-next-line:prefer-for-of
            for (let i = 0; i < this.antragList.length; i++) {
                const antragItem: TSAntragDTO = this.antragList[i];
                // Wir muessen nur die Antraege der aktuell ausgewaehlten Gesuchsperiode beachten
                if (antragItem.gesuchsperiodeString === this.getGesuchsperiodeAsString(this.neuesteGesuchsperiode)) {
                    // Es gibt schon (mindestens 1) Gesuch für die neueste Periode
                    erneuernGesperrt = true;
                    break;
                }
                // Wenn das Erstgesuch der Periode ein Online Gesuch war, darf dieser *nur* durch den GS selber
                // erneuert werden. JA/SCH muss einen neuen Fall eröffnen, da Papier und Online Gesuche nie vermischt
                // werden duerfen!
                if (antragItem.eingangsart === TSEingangsart.ONLINE && antragItem.antragTyp !== TSAntragTyp.MUTATION) {
                    if (!this.authServiceRS.isOneOfRoles(TSRoleUtil.getGesuchstellerOnlyRoles())) {
                        erneuernGesperrt = true;
                        break;
                    }
                }
            }
            this.erneuernPossibleForCurrentAntrag = !erneuernGesperrt;
        } else {
            this.erneuernPossibleForCurrentAntrag = false;
        }
    }

    public antragErneuern(): void {
        this.erneuernPossibleForCurrentAntrag = false;
        let eingangsart: TSEingangsart;
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getGesuchstellerOnlyRoles())) {
            eingangsart = TSEingangsart.ONLINE;
        } else {
            eingangsart = TSEingangsart.PAPIER;
        }
        this.$state.go('gesuch.erneuerung', {
            creationAction: TSCreationAction.CREATE_NEW_FOLGEGESUCH,
            eingangsart: eingangsart,
            gesuchsperiodeId: this.neuesteGesuchsperiode.id,
            dossierId: this.dossier.id,
            gesuchId: this.getGesuchIdFuerMutationOrErneuerung(),
        });
    }

    private getGesuchIdFuerMutationOrErneuerung(): string {
        // GesuchId ermitteln fuer Mutation ermitteln: Falls wir auf der Verlauf-View sind, nehmen wir einfach
        // irgendeines der Liste (es wird auf dem Server sichergestellt, dass die Mutation ab dem neuesten Gesuch
        // der Periode gemacht wird), wichtig ist nur, dass es sich um die richtige Gesuchsperiode handelt.
        if (this.gesuchid) {
            return this.gesuchid;
        } else {
            if (this.getGesuch()) {
                return this.getGesuch().id;
            }
        }
        return undefined;
    }

    public openMitteilungen(): void {
        this.$state.go('mitteilungen.view', {
            dossierId: this.dossier.id,
            fallId: this.dossier.fall.id,
        });
    }

    public showVerlauf(): boolean {
        return this.getGesuch() !== null && this.getGesuch() !== undefined && !this.getGesuch().isNew();
    }

    public openVerlauf(): void {
        this.$state.go('verlauf.view', {
            gesuchId: this.getGesuch().id
        });
    }

    public showGesuchLoeschen(): boolean {
        if (!this.getGesuch() || this.getGesuch().isNew()) {
            return false;
        }
        if (this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getGesuchstellerOnlyRoles())) {
            // GS darf nur vor der Freigabe loeschen
            if (this.hideActionButtons || this.isDashboardScreen || isAtLeastFreigegebenOrFreigabequittung(this.getGesuch().status)) {
                return false;
            }
        } else {
            // JA: Darf nicht verfuegen oder verfuegt sein und muss Papier sein
            if (isStatusVerfuegenVerfuegt(this.getGesuch().status) || this.getGesuch().eingangsart === TSEingangsart.ONLINE) {
                return false;
            }
        }
        return true;
    }

    public gesuchLoeschen(): IPromise<void> {
        return this.dvDialog.showRemoveDialog(removeDialogTempl, undefined, RemoveDialogController, {
            title: 'CONFIRM_GESUCH_LOESCHEN',
            deleteText: 'BESCHREIBUNG_GESUCH_LOESCHEN',
            parentController: this,
            elementID: 'gesuchLoeschenButton'
        }).then(() => {
            this.setAllFormsPristine();
            if (this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getGesuchstellerOnlyRoles())) {
                this.gesuchRS.removeGesuchstellerAntrag(this.getGesuch().id).then(result => {
                    this.gesuchModelManager.setGesuch(new TSGesuch());
                    this.resetNavigationParameters();
                    this.$state.go('gesuchsteller.dashboard');
                });
            } else {
                this.gesuchRS.removePapiergesuch(this.getGesuch().id).then(() => {
                    if (this.antragList.length > 1) {
                        const navObj: any = {
                            gesuchId: this.antragList[0].antragId,
                            dossierId: this.antragList[0].dossierId
                        };
                        this.$state.go('gesuch.fallcreation', navObj);
                    } else {
                        this.$state.go('pendenzen.list-view');
                    }
                });
            }
        });
    }

    private setAllFormsPristine(): void {
        const forms: [IFormController] = this.unsavedWarningSharedService.allForms();
        // tslint:disable-next-line:prefer-for-of
        for (let index = 0; index < forms.length; index++) {
            const form: IFormController = forms[index];
            form.$setPristine();
            form.$setUntouched();
        }
    }

    public openAlleVerfuegungen(): void {
        this.$state.go('alleVerfuegungen.view', {
            dossierId: this.dossier.id
        });
    }

    public showKontakt(): void {
        let text: string;
        if (this.dossier.isHauptverantwortlicherTS()) {
            text = this.ebeguUtil.getKontaktSchulamt();
        } else {
            text = this.ebeguUtil.getKontaktJugendamt();
        }
        this.dvDialog.showDialog(showKontaktTemplate, ShowTooltipController, {
            title: '',
            text: text,
            parentController: this
        });
    }

    /**
     * Sets the focus back to the Kontakt icon.
     */
    public setFocusBack(elementID: string): void {
        angular.element('#kontaktButton').first().focus();
    }
}
