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
import GemeindeRS from '../../../../gesuch/service/gemeindeRS.rest';
import {DVQuicksearchListController} from '../../../quicksearch/component/dv-quicksearch-list/dv-quicksearch-list';
import {DVsTPersistService} from '../../service/dVsTPersistService';
import {InstitutionRS} from '../../service/institutionRS.rest';
import BenutzerRS from '../../service/benutzerRS.rest';

/**
 * This directive allows a filter and sorting configuration to be saved after leaving the table.
 * The information will be stored in an angular-service, whi
 */
export default class DVSTPersistQuicksearch implements IDirective {
    static $inject: string[] = ['BenutzerRS', 'InstitutionRS', 'DVsTPersistService', 'GemeindeRS'];

    restrict = 'A';
    require = ['^stTable', '^dvQuicksearchList'];
    link: IDirectiveLinkFn;

    constructor(private readonly benutzerRS: BenutzerRS,
                private readonly institutionRS: InstitutionRS,
                private readonly dVsTPersistService: DVsTPersistService,
                private readonly gemeindeRS: GemeindeRS) {
        this.link = (scope: IScope, element: IAugmentedJQuery, attrs: IAttributes, ctrlArray: any) => {
            const nameSpace: string = attrs.dvStPersistQuicksearch;
            const stTableCtrl: any = ctrlArray[0];
            const quicksearchListController: DVQuicksearchListController = ctrlArray[1];

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
            const savedState = dVsTPersistService.loadData(nameSpace);
            if (savedState) {
                if (savedState.search && savedState.search.predicateObject) { //update all objects of the model for the filters
                    quicksearchListController.selectedAntragTyp = savedState.search.predicateObject.antragTyp;
                    quicksearchListController.selectedGesuchsperiode =
                        savedState.search.predicateObject.gesuchsperiodeString;
                    quicksearchListController.selectedAntragStatus = savedState.search.predicateObject.status;
                    quicksearchListController.selectedBetreuungsangebotTyp = savedState.search.predicateObject.angebote;
                    this.setInstitutionFromName(quicksearchListController,
                        savedState.search.predicateObject.institutionen);
                    quicksearchListController.selectedFallNummer = savedState.search.predicateObject.fallNummer;
                    quicksearchListController.selectedFamilienName = savedState.search.predicateObject.familienName;
                    quicksearchListController.selectedKinder = savedState.search.predicateObject.kinder;
                    quicksearchListController.selectedEingangsdatum = savedState.search.predicateObject.eingangsdatum;
                    quicksearchListController.selectedDokumenteHochgeladen =
                        savedState.search.predicateObject.dokumenteHochgeladen;
                    this.setGemeindeFromName(quicksearchListController, savedState.search.predicateObject.gemeinde);
                    this.setVerantwortlicherBGFromName(quicksearchListController,
                        savedState.search.predicateObject.verantwortlicherBG);
                    this.setVerantwortlicherTSFromName(quicksearchListController,
                        savedState.search.predicateObject.verantwortlicherTS);
                }
                const tableState = stTableCtrl.tableState();

                angular.extend(tableState, savedState);
                stTableCtrl.pipe();

            }
        };
    }

    static factory(): IDirectiveFactory {
        const directive = (benutzerRS: any,
                           institutionRS: any,
                           dVsTPersistService: any,
                           gemeindeRS: any) => new DVSTPersistQuicksearch(benutzerRS,
            institutionRS,
            dVsTPersistService,
            gemeindeRS);
        directive.$inject = ['BenutzerRS', 'InstitutionRS', 'DVsTPersistService', 'GemeindeRS'];
        return directive;
    }

    /**
     * Extracts the user out of her name. This method is needed because the filter saves the user using its name
     * while the dropdownlist is constructed using the object TSUser. So in order to be able to select the right user
     * with need the complete object and not only its Fullname.
     */
    private setVerantwortlicherBGFromName(quicksearchListController: DVQuicksearchListController,
                                          verantwortlicherBGFullname: string): void {
        if (verantwortlicherBGFullname && quicksearchListController) {
            this.benutzerRS.getBenutzerJAorAdmin().then(userList => {
                const verantwortlicher = userList.find(user => user.getFullName() === verantwortlicherBGFullname);
                quicksearchListController.selectedVerantwortlicherBG = verantwortlicher;
                quicksearchListController.userChanged(verantwortlicher);
            });
        }
    }

    /**
     * Extracts the user out of her name. This method is needed because the filter saves the user using its name
     * while the dropdownlist is constructed using the object TSUser. So in order to be able to select the right user
     * with need the complete object and not only its Fullname.
     */
    private setVerantwortlicherTSFromName(quicksearchListController: DVQuicksearchListController,
                                          verantwortlicherTSFullname: string): void {
        if (verantwortlicherTSFullname && quicksearchListController) {
            this.benutzerRS.getBenutzerSCHorAdminSCH().then(userList => {
                const verantwortlicher = userList.find(user => user.getFullName() === verantwortlicherTSFullname);
                quicksearchListController.selectedVerantwortlicherTS = verantwortlicher;
                quicksearchListController.userChanged(verantwortlicher);
            });
        }
    }

    /**
     * Extracts the Institution from the institutionList of the controller using the name that had been saved in the
     * filter. This is needed because the filter saves the name and not the object.
     */
    private setInstitutionFromName(quicksearchListController: DVQuicksearchListController, institution: string): void {
        if (institution && quicksearchListController) {
            this.institutionRS.getInstitutionenForCurrentBenutzer().then(institutionList => {
                quicksearchListController.selectedInstitution = institutionList.find(i => i.name === institution);
            });
        }
    }

    private setGemeindeFromName(quicksearchListController: DVQuicksearchListController, gemeinde: string): void {
        if (gemeinde && quicksearchListController) {
            this.gemeindeRS.getAllGemeinden().then(gemeindeList => {
                quicksearchListController.selectedGemeinde = gemeindeList.find(g => g.name === gemeinde);
            });
        }
    }
}

