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

import {IAttributes, IAugmentedJQuery, IDirective, IDirectiveFactory, IDirectiveLinkFn, IScope} from 'angular';
import {Subscription} from 'rxjs';
import {AuthLifeCycleService} from '../../../../authentication/service/authLifeCycle.service';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../../gesuch/service/gemeindeRS.rest';
import {TSAuthEvent} from '../../../../models/enums/TSAuthEvent';
import {TSRole} from '../../../../models/enums/TSRole';
import TSBerechtigung from '../../../../models/TSBerechtigung';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {DVAntragListController} from '../../component/dv-antrag-list/dv-antrag-list';
import {DVsTPersistService} from '../../service/dVsTPersistService';
import {InstitutionRS} from '../../service/institutionRS.rest';
import UserRS from '../../service/userRS.rest';

/**
 * This directive allows a filter and sorting configuration to be saved after leaving the table.
 * The information will be stored in an angular-service.
 */
export default class DVSTPersistAntraege implements IDirective {

    static $inject: string[] = ['UserRS', 'InstitutionRS', 'AuthServiceRS', 'DVsTPersistService', 'GemeindeRS',
        'AuthLifeCycleService'];

    restrict = 'A';
    require = ['^stTable', '^dvAntragList'];
    link: IDirectiveLinkFn;
    obss: Subscription;

    constructor(private readonly userRS: UserRS,
                private readonly institutionRS: InstitutionRS,
                private readonly authServiceRS: AuthServiceRS,
                private readonly dVsTPersistService: DVsTPersistService,
                private readonly gemeindeRS: GemeindeRS,
                private readonly authLifeCycleService: AuthLifeCycleService) {

        this.link = (scope: IScope, element: IAugmentedJQuery, attrs: IAttributes, ctrlArray: any) => {
            this.obss = this.authLifeCycleService.get$(TSAuthEvent.LOGIN_SUCCESS)
                .subscribe(() => this.loadData(attrs, ctrlArray, scope, dVsTPersistService));

            scope.$on('$destroy', () => {
                this.destroy();
            });
        };
    }

    static factory(): IDirectiveFactory {
        const directive = (userRS: any,
                           institutionRS: any,
                           authServiceRS: any,
                           dVsTPersistService: any,
                           gemeindeRS: any,
                           authLifeCycleService: any) =>
            new DVSTPersistAntraege(userRS, institutionRS, authServiceRS, dVsTPersistService, gemeindeRS, authLifeCycleService);

        directive.$inject = ['UserRS', 'InstitutionRS', 'AuthServiceRS', 'DVsTPersistService', 'GemeindeRS', 'AuthLifeCycleService'];
        return directive;
    }

    /**
     * Die Directive wird nicht destroyed, daher muss man beim destroyen vom Scope die observables unsubscriben. Sollte dies nicht
     * gemacht werden, bleibt die Directive aktiv und der Code wird immer wieder ausfgefuehrt
     *
     * INFO: wir speichern die Observables in eine ISubscription die beim destroyen von scope unsubscribed werden muss. Die Alternative
     * mit takeUntil ist in diesem Fall (fuer eine Directive) nicht so gut weil es nicht completen kann. Da die Directive nur einmal erstellt
     * wird, wird der Constructor nur einmal ausgefuehrt und die unsubscription$ object deshalb nur einmal erstellt.
     */
    private destroy() {
        this.obss.unsubscribe();
    }

    private loadData(attrs: angular.IAttributes, ctrlArray: any, scope: angular.IScope, dVsTPersistService: DVsTPersistService) {
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getAllRolesButGesuchsteller())) { // just to be sure that the user has the required role
            const nameSpace: string = attrs.dvStPersistAntraege;
            const stTableCtrl: any = ctrlArray[0];
            const antragListController: DVAntragListController = ctrlArray[1];

            //save the table state every time it changes
            scope.$watch(() => stTableCtrl.tableState(), (newValue, oldValue) => {
                if (newValue !== oldValue) {
                    // sessionStorage.setItem(nameSpace, JSON.stringify(newValue));
                    dVsTPersistService.saveData(nameSpace, newValue);
                }
            }, true);

            // if (sessionStorage.getItem(nameSpace)) {
            // let savedState = JSON.parse(sessionStorage.getItem(nameSpace));

            //fetch the table state when the directive is loaded
            let savedState = dVsTPersistService.loadData(nameSpace);
            savedState = this.setCurrentUserAsVerantwortlicher(antragListController, savedState);
            if (savedState) {
                if (savedState.search && savedState.search.predicateObject) { //update all objects of the model for the filters
                    antragListController.selectedAntragTyp = savedState.search.predicateObject.antragTyp;
                    antragListController.selectedGesuchsperiode =
                        savedState.search.predicateObject.gesuchsperiodeString;
                    antragListController.selectedAntragStatus = savedState.search.predicateObject.status;
                    antragListController.selectedBetreuungsangebotTyp = savedState.search.predicateObject.angebote;
                    this.setInstitutionFromName(antragListController, savedState.search.predicateObject.institutionen);
                    antragListController.selectedFallNummer = savedState.search.predicateObject.fallNummer;
                    antragListController.selectedFamilienName = savedState.search.predicateObject.familienName;
                    antragListController.selectedKinder = savedState.search.predicateObject.kinder;
                    antragListController.selectedAenderungsdatum = savedState.search.predicateObject.aenderungsdatum;
                    antragListController.selectedEingangsdatum = savedState.search.predicateObject.eingangsdatum;
                    antragListController.selectedDokumenteHochgeladen =
                        savedState.search.predicateObject.dokumenteHochgeladen;
                    antragListController.selectedEingangsdatumSTV = savedState.search.predicateObject.eingangsdatumSTV;
                    this.setGemeindeFromName(antragListController, savedState.search.predicateObject.gemeinde);
                    this.setVerantwortlicherBGFromName(antragListController,
                        savedState.search.predicateObject.verantwortlicherBG);
                    this.setVerantwortlicherTSFromName(antragListController,
                        savedState.search.predicateObject.verantwortlicherTS);
                }
                const tableState = stTableCtrl.tableState();

                angular.extend(tableState, savedState);
                stTableCtrl.pipe();

            }
        }
    }

    /**
     * Extracts the user out of her name. This method is needed because the filter saves the user using its name
     * while the dropdownlist is constructed using the object TSUser. So in order to be able to select the right user
     * with need the complete object and not only its Fullname.
     */
    private setVerantwortlicherBGFromName(antragListController: DVAntragListController,
                                          verantwortlicherBGFullname: string): void {
        if (verantwortlicherBGFullname && antragListController) {
            this.userRS.getBenutzerJAorAdmin().then(userList => {
                antragListController.selectedVerantwortlicherBG = userList.find(
                    user => user.getFullName() === verantwortlicherBGFullname);
            });
        }
    }

    /**
     * Extracts the user out of her name. This method is needed because the filter saves the user using its name
     * while the dropdownlist is constructed using the object TSUser. So in order to be able to select the right user
     * with need the complete object and not only its Fullname.
     */
    private setVerantwortlicherTSFromName(antragListController: DVAntragListController,
                                          verantwortlicherTSFullname: string): void {
        if (verantwortlicherTSFullname && antragListController) {
            this.userRS.getBenutzerSCHorAdminSCH().then(userList => {
                antragListController.selectedVerantwortlicherTS = userList.find(
                    user => user.getFullName() === verantwortlicherTSFullname);
            });
        }
    }

    /**
     * Extracts the Institution from the institutionList of the controller using the name that had been saved in the
     * filter. This is needed because the filter saves the name and not the object.
     */
    private setInstitutionFromName(antragListController: DVAntragListController, institution: string): void {
        if (institution && antragListController) {
            this.institutionRS.getInstitutionenForCurrentBenutzer().then(institutionList => {
                if (Array.isArray(institutionList)) {
                    const found = institutionList.find(i => i.name === institution);
                    if (found) {
                        antragListController.selectedInstitution = found;
                    }
                }
            });
        }
    }

    private setGemeindeFromName(antragListController: DVAntragListController, gemeinde: string): void {
        if (gemeinde && antragListController) {
            this.gemeindeRS.getAllGemeinden().then(gemeindeList => {
                antragListController.selectedGemeinde = gemeindeList.find(g => g.name === gemeinde);
            });
        }
    }

    /**
     * Setzt den aktuellen Benutzer als selectedVerantwotlicher wenn:
     * - es eine pendenzenListe ist: ctrl.pendenz===true
     * - es noch nicht gesetzt wurde, d.h. nichts war ausgewaehlt
     */
    private setCurrentUserAsVerantwortlicher(antragListController: DVAntragListController, savedState: any): any {
        let savedStateToReturn: any = angular.copy(savedState);
        if (antragListController.pendenz) {
            if (!savedStateToReturn) {
                savedStateToReturn = {search: {predicateObject: this.extractVerantwortlicherFullName()}};
            }
            if (!savedStateToReturn.search.predicateObject) {
                savedStateToReturn.search.predicateObject = this.extractVerantwortlicherFullName();
            }
            if (!savedStateToReturn.search.predicateObject.verantwortlicher) {
                const berechtigung: TSBerechtigung = this.authServiceRS.getPrincipal().currentBerechtigung;
                if (berechtigung.role === TSRole.ADMIN_TS || berechtigung.role === TSRole.SACHBEARBEITER_TS) {
                    savedStateToReturn.search.predicateObject.verantwortlicherTS =
                        this.authServiceRS.getPrincipal().getFullName();
                } else { //JA
                    savedStateToReturn.search.predicateObject.verantwortlicherBG =
                        this.authServiceRS.getPrincipal().getFullName();
                }
            }
        }
        return savedStateToReturn;
    }

    private extractVerantwortlicherFullName() {
        if (this.authServiceRS.getPrincipal()) {
            const berechtigung: TSBerechtigung = this.authServiceRS.getPrincipal().currentBerechtigung;
            if (berechtigung.role === TSRole.ADMIN_TS || berechtigung.role === TSRole.SACHBEARBEITER_TS) {
                return {verantwortlicherTS: this.authServiceRS.getPrincipal().getFullName()};
            } else { //JA
                return {verantwortlicherBG: this.authServiceRS.getPrincipal().getFullName()};
            }
        }
        return '';
    }
}
