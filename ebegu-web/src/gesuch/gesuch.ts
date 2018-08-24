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

import AuthServiceRS from '../authentication/service/AuthServiceRS.rest';
import ErrorService from '../app/core/errors/service/ErrorService';
import AntragStatusHistoryRS from '../app/core/service/antragStatusHistoryRS.rest';
import EwkRS from '../app/core/service/ewkRS.rest';
import {IN_BEARBEITUNG_BASE_NAME, TSAntragStatus} from '../models/enums/TSAntragStatus';
import {TSAntragTyp} from '../models/enums/TSAntragTyp';
import {TSGesuchBetreuungenStatus} from '../models/enums/TSGesuchBetreuungenStatus';
import {TSGesuchEvent} from '../models/enums/TSGesuchEvent';
import {TSRole} from '../models/enums/TSRole';
import {TSWizardStepName} from '../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../models/enums/TSWizardStepStatus';
import TSDossier from '../models/TSDossier';
import TSEWKPerson from '../models/TSEWKPerson';
import GesuchstellerRS from '../app/core/service/gesuchstellerRS.rest';
import {ILogService, IRootScopeService} from 'angular';
import TSEWKResultat from '../models/TSEWKResultat';
import TSFall from '../models/TSFall';
import TSGesuch from '../models/TSGesuch';
import TSGesuchsteller from '../models/TSGesuchsteller';
import TSGesuchstellerContainer from '../models/TSGesuchstellerContainer';
import DateUtil from '../utils/DateUtil';
import EbeguUtil from '../utils/EbeguUtil';
import {TSRoleUtil} from '../utils/TSRoleUtil';
import BerechnungsManager from './service/berechnungsManager';
import GesuchModelManager from './service/gesuchModelManager';
import WizardStepManager from './service/wizardStepManager';
import ITranslateService = angular.translate.ITranslateService;

export class GesuchRouteController {

    static $inject: string[] = ['GesuchModelManager', 'BerechnungsManager', 'WizardStepManager', 'EbeguUtil', 'ErrorService',
        'AntragStatusHistoryRS', '$translate', 'AuthServiceRS', '$mdSidenav', 'CONSTANTS', 'GesuchstellerRS', 'EwkRS', '$log', '$rootScope'];

    TSRole = TSRole;
    TSRoleUtil = TSRoleUtil;
    openEwkSidenav: boolean;

    constructor(private readonly gesuchModelManager: GesuchModelManager, berechnungsManager: BerechnungsManager,
                private readonly wizardStepManager: WizardStepManager, private readonly ebeguUtil: EbeguUtil,
                private readonly errorService: ErrorService,
                private readonly antragStatusHistoryRS: AntragStatusHistoryRS, private readonly $translate: ITranslateService,
                private readonly authServiceRS: AuthServiceRS, private readonly $mdSidenav: ng.material.ISidenavService, private readonly CONSTANTS: any,
                private readonly gesuchstellerRS: GesuchstellerRS, private readonly ewkRS: EwkRS,
                private readonly $log: ILogService, private readonly $rootScope: IRootScopeService) {
        //super(gesuchModelManager, berechnungsManager, wizardStepManager);
        this.antragStatusHistoryRS.loadLastStatusChange(this.gesuchModelManager.getGesuch());
    }

    showFinanzielleSituationStart(): boolean {
        return this.gesuchModelManager.showFinanzielleSituationStart();
    }

    public getDateFromGesuch(): string {
        if (this.gesuchModelManager && this.gesuchModelManager.getGesuch()) {
            return DateUtil.momentToLocalDateFormat(this.gesuchModelManager.getGesuch().eingangsdatum, 'DD.MM.YYYY');
        }
        return undefined;
    }

    public toggleSidenav(componentId: string) {
        this.$mdSidenav(componentId).toggle();
    }

    public closeSidenav(componentId: string) {
        this.$mdSidenav(componentId).close();
    }

    public getIcon(stepName: TSWizardStepName): string {
        const step = this.wizardStepManager.getStepByName(stepName);
        if (step) {

            if (!this.getGesuch()) {
                return '';
            }

            const status = step.wizardStepStatus;
            if (status === TSWizardStepStatus.MUTIERT) {
                return 'fa-circle green';
            } else if (status === TSWizardStepStatus.OK) {
                if (this.getGesuch().isMutation()) {
                    if (step.wizardStepName === TSWizardStepName.VERFUEGEN) { // Verfuegung auch bei Mutation mit Hacken (falls verfuegt)
                        return 'fa-check green';
                    }
                    return '';
                } else {
                    return 'fa-check green';
                }
            } else if (status === TSWizardStepStatus.NOK) {
                return 'fa-close red';
            } else if (status === TSWizardStepStatus.IN_BEARBEITUNG) {
                if (step.wizardStepName === TSWizardStepName.DOKUMENTE || step.wizardStepName === TSWizardStepName.FREIGABE) { // Dokumenten haben kein Icon wenn nicht alle hochgeladen wurden
                    return '';
                }
                return 'fa-pencil black';
            } else if (status === TSWizardStepStatus.PLATZBESTAETIGUNG || status === TSWizardStepStatus.WARTEN) {
                if (this.getGesuch().isMutation() && this.isWizardStepDisabled(step.wizardStepName)) { // in einer Mutation bekommt icon nur wenn es aktiviert ist
                    return '';
                } else {
                    return 'fa-hourglass orange';
                }
            } else if (status === TSWizardStepStatus.UNBESUCHT) {
                return '';
            }
        }
        return '';
    }

    /**
     * Steps are disabled when the field verfuegbar is false or if they are not allowed for the current role
     * @param stepName
     * @returns {boolean} Sollte etwas schief gehen, true wird zurueckgegeben
     */
    public isWizardStepDisabled(stepName: TSWizardStepName): boolean {
        const step = this.wizardStepManager.getStepByName(stepName);
        if (step) {
            return !this.wizardStepManager.isStepClickableForCurrentRole(step, this.gesuchModelManager.getGesuch());
        }
        return true;
    }

    public isStepVisible(stepName: TSWizardStepName): boolean {
        if (stepName) {
            return this.wizardStepManager.isStepVisible(stepName);
        }
        return true;
    }

    public isElementActive(stepName: TSWizardStepName): boolean {
        return this.wizardStepManager.getCurrentStepName() === stepName;
    }

    /**
     * Uebersetzt den Status des Gesuchs und gibt ihn zurueck. Sollte das Gesuch noch keinen Status haben IN_BEARBEITUNG_JA
     * wird zurueckgegeben
     * @returns {string}
     */
    public getGesuchStatusTranslation(): string {
        let toTranslate: TSAntragStatus = TSAntragStatus.IN_BEARBEITUNG_JA;
        if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().status) {
            toTranslate = this.gesuchModelManager.calculateNewStatus(this.gesuchModelManager.getGesuch().status);
        }
        const isUserGesuchsteller: boolean = this.authServiceRS.isOneOfRoles(TSRoleUtil.getGesuchstellerOnlyRoles());
        const isUserAmt: boolean = this.authServiceRS.isOneOfRoles(TSRoleUtil.getJugendamtAndSchulamtRole());
        const isUserSTV: boolean = this.authServiceRS.isOneOfRoles(TSRoleUtil.getSteueramtOnlyRoles());

        if (toTranslate === TSAntragStatus.IN_BEARBEITUNG_GS && isUserGesuchsteller) {
            if (TSGesuchBetreuungenStatus.ABGEWIESEN === this.gesuchModelManager.getGesuch().gesuchBetreuungenStatus) {
                return this.ebeguUtil.translateString(TSAntragStatus[TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN]);
            } else if (TSGesuchBetreuungenStatus.WARTEN === this.gesuchModelManager.getGesuch().gesuchBetreuungenStatus) {
                return this.ebeguUtil.translateString(TSAntragStatus[TSAntragStatus.PLATZBESTAETIGUNG_WARTEN]);
            }
        }
        if (toTranslate === TSAntragStatus.IN_BEARBEITUNG_JA && isUserAmt) {
            return this.ebeguUtil.translateString(IN_BEARBEITUNG_BASE_NAME);
        }
        switch (toTranslate) {
            case TSAntragStatus.GEPRUEFT_STV:
            case TSAntragStatus.IN_BEARBEITUNG_STV:
            case TSAntragStatus.PRUEFUNG_STV:
                if (!isUserAmt && !isUserSTV) {
                    return this.ebeguUtil.translateString('VERFUEGT');
                }
                break;
            default:
                break;

        }

        if ((toTranslate === TSAntragStatus.NUR_SCHULAMT)
            && isUserGesuchsteller) {
            return this.ebeguUtil.translateString('ABGESCHLOSSEN');
        }

        return this.ebeguUtil.translateString(TSAntragStatus[toTranslate]);
    }

    public getUserFullname(): string {
        return this.antragStatusHistoryRS.getUserFullname();
    }

    public getGesuchId(): string {
        if (this.getGesuch()) {
            return this.getGesuch().id;
        }
        return undefined;
    }

    public getGesuch(): TSGesuch {
        if (this.gesuchModelManager.getGesuch()) {
            return this.gesuchModelManager.getGesuch();
        }
        return undefined;
    }

    public getFall(): TSFall {
        return this.gesuchModelManager.getFall() ? this.gesuchModelManager.getFall() : undefined;
    }

    public getFallId(): string {
        return this.getFall() ? this.getFall().id : undefined;
    }

    public getDossier(): TSDossier {
        return this.getGesuch() ? this.getGesuch().dossier : undefined;
    }

    public getDossierId(): string {
        return (this.getGesuch() && this.getGesuch().dossier) ? this.getGesuch().dossier.id : '';
    }

    public getGesuchErstellenStepTitle(): string {
        if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.isGesuch()) {
            if (this.getDateFromGesuch()) {
                const key = (this.gesuchModelManager.getGesuch().typ === TSAntragTyp.ERNEUERUNGSGESUCH) ? 'MENU_ERNEUERUNGSGESUCH_VOM' : 'MENU_ERSTGESUCH_VOM';
                return this.$translate.instant(key, {
                    date: this.getDateFromGesuch()
                });
            } else {
                const key = (this.gesuchModelManager.getGesuch().typ === TSAntragTyp.ERNEUERUNGSGESUCH) ? 'MENU_ERNEUERUNGSGESUCH' : 'MENU_ERSTGESUCH';
                return this.$translate.instant(key);
            }
        } else {
            if (this.getDateFromGesuch()) {
                return this.$translate.instant('MENU_MUTATION_VOM', {
                    date: this.getDateFromGesuch()
                });
            } else {
                return this.$translate.instant('MENU_MUTATION');
            }
        }
    }

    public getGesuchName(): string {
        return this.gesuchModelManager.getGesuchName();
    }

    public getActiveElement(): TSWizardStepName {
        return this.wizardStepManager.getCurrentStepName();
    }

    public getGesuchstellerTitle(gsnumber: number): string {
        const gs: TSGesuchsteller = this.ewkRS.getGesuchsteller(gsnumber).gesuchstellerJA;
        if (gs) {
            const title: string = gs.getFullName();
            if (gs.ewkPersonId) {
                return title + ' (' + gs.ewkPersonId + ')';
            }
            return title;
        }
        return undefined;
    }

    public showAbfrageForGesuchsteller(n: any): boolean {
        return this.ewkRS.ewkSearchAvailable(n);
    }

    public getGesuchsteller(n: number): TSGesuchstellerContainer {
        switch (n) {
            case 1:
                if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().gesuchsteller1) {
                    return this.gesuchModelManager.getGesuch().gesuchsteller1;
                }
                return undefined;
            case 2:
                if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().gesuchsteller2) {
                    return this.gesuchModelManager.getGesuch().gesuchsteller2;
                }
                return undefined;
            default:
                return undefined;
        }
    }

    public getEWKResultat(n: number): TSEWKResultat {
        switch (n) {
            case 1:
                return this.gesuchModelManager.ewkResultatGS1;
            case 2:
                return this.gesuchModelManager.ewkResultatGS2;
            default:
                return undefined;
        }
    }

    public getEWKPerson(n: number): TSEWKPerson {
        switch (n) {
            case 1:
                return this.gesuchModelManager.ewkPersonGS1;
            case 2:
                return this.gesuchModelManager.ewkPersonGS2;
            default:
                return undefined;
        }
    }

    public checkEWKPerson(person: TSEWKPerson, n: number): boolean {
        switch (n) {
            case 1:
                return (person.personID === this.ewkRS.gesuchsteller1.gesuchstellerJA.ewkPersonId);
            case 2:
                return (person.personID === this.ewkRS.gesuchsteller2.gesuchstellerJA.ewkPersonId);
            default:
                return false;
        }
    }

    public searchGesuchsteller(n: number): void {
        this.errorService.clearAll();
        this.ewkRS.suchePerson(n).then(response => {
            switch (n) {
                case 1:
                    this.gesuchModelManager.ewkResultatGS1 = response;
                    if (this.gesuchModelManager.ewkResultatGS1.anzahlResultate === 1) {
                        this.selectPerson(this.gesuchModelManager.ewkResultatGS1.personen[0], n);
                    } else {
                        this.setDateEWKAbfrage(n);
                    }
                    break;
                case 2:
                    this.gesuchModelManager.ewkResultatGS2 = response;
                    if (this.gesuchModelManager.ewkResultatGS2.anzahlResultate === 1) {
                        this.selectPerson(this.gesuchModelManager.ewkResultatGS2.personen[0], n);
                    } else {
                        this.setDateEWKAbfrage(n);
                    }
                    break;
                default:
                    break;
            }
        }).catch((exception) => {
            const bussinesExceptionMitFehlercode = (this.errorService.getErrors().filter(
                    function filterForBusinessException(e) {
                        return (e.errorCodeEnum === 'ERROR_PERSONENSUCHE_BUSINESS' && e.argumentList[0]);
                    }).length) > 0;

            if (bussinesExceptionMitFehlercode) {
                // es war eine Businessexception und der Sercvice hat mit einem ErrorCode geantwortet
                // Abfrage hat stattgefunden
                this.setDateEWKAbfrage(n);
            }
            this.$log.error('there was an error searching the person in EWK ', exception);
        });
    }

    public selectPerson(person: TSEWKPerson, n: number): void {
        this.ewkRS.selectPerson(n, person.personID);
        switch (n) {
            case 1:
                this.gesuchModelManager.ewkPersonGS1 = person;
                this.$rootScope.$broadcast(TSGesuchEvent[TSGesuchEvent.EWK_PERSON_SELECTED], 1, person.personID);
                break;
            case 2:
                this.gesuchModelManager.ewkPersonGS2 = person;
                this.$rootScope.$broadcast(TSGesuchEvent[TSGesuchEvent.EWK_PERSON_SELECTED], 2, person.personID);
                break;
            default:
                break;
        }
    }

    public isGesuchGesperrt(): boolean {
        if (this.gesuchModelManager.getGesuch()) {
            return this.gesuchModelManager.getGesuch().gesperrtWegenBeschwerde === true;
        }
        return false;
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public isDocumentUploaded(): boolean {
        return this.getGesuch() && this.getGesuch().dokumenteHochgeladen;
    }

    private setDateEWKAbfrage(n: number) {
        this.ewkRS.selectPerson(n, null);
        switch (n) {
            case 1:
                this.$rootScope.$broadcast(TSGesuchEvent[TSGesuchEvent.EWK_PERSON_SELECTED], 1, null);
                break;
            case 2:
                this.$rootScope.$broadcast(TSGesuchEvent[TSGesuchEvent.EWK_PERSON_SELECTED], 2, null);
                break;
            default:
                break;
        }

    }
}
