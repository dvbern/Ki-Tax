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
import {TestFaelleRS} from '../admin/service/testFaelleRS.rest';
import {TSRole} from '../models/enums/TSRole';
import TSInstitution from '../models/TSInstitution';
import {TSMandant} from '../models/TSMandant';
import {TSTraegerschaft} from '../models/TSTraegerschaft';
import TSUser from '../models/TSUser';
import AuthenticationUtil from '../utils/AuthenticationUtil';
import AuthServiceRS from './service/AuthServiceRS.rest';
import ITimeoutService = angular.ITimeoutService;

export const SchulungComponentConfig: IComponentOptions = {
    transclude: false,
    template: require('./schulung.html'),
    controllerAs: 'vm',
};

export class SchulungViewController implements IController {

    static $inject: string[] = ['$state', 'AuthServiceRS', '$timeout', 'TestFaelleRS'];

    public usersList: Array<TSUser> = Array<TSUser>();
    private gesuchstellerList: string[];
    private readonly institutionsuserList: Array<TSUser> = Array<TSUser>();
    private readonly amtUserList: Array<TSUser> = Array<TSUser>();
    private readonly mandant: TSMandant;
    private readonly institutionForelle: TSInstitution;
    private readonly traegerschaftFisch: TSTraegerschaft;

    constructor(private readonly $state: StateService, private readonly authServiceRS: AuthServiceRS,
                private readonly $timeout: ITimeoutService,
                private readonly testFaelleRS: TestFaelleRS) {

        this.mandant = this.getMandant();
        this.traegerschaftFisch = this.getTraegerschaftFisch();
        this.institutionForelle = this.getInstitutionForelle();
        this.testFaelleRS.getSchulungBenutzer().then((response: any) => {
            this.gesuchstellerList = response;
            for (let i = 0; i < this.gesuchstellerList.length; i++) {
                const name = this.gesuchstellerList[i];
                const username = 'sch' + (((i + 1) < 10) ? '0' + (i + 1).toString() : (i + 1).toString());
                this.usersList.push(new TSUser('Sandra', name, username, 'password1', 'sandra.' + name.toLocaleLowerCase() + '@example.com', this.mandant, TSRole.GESUCHSTELLER));
            }

            this.setInstitutionUsers();
            this.setAmtUsers();
        });
    }

    private setInstitutionUsers() {
        this.institutionsuserList.push(new TSUser('Fritz', 'Fisch', 'sch20', 'password1', 'fritz.fisch@example.com',
            this.mandant, TSRole.SACHBEARBEITER_TRAEGERSCHAFT, this.traegerschaftFisch, undefined));
        this.institutionsuserList.push(new TSUser('Franz', 'Forelle', 'sch21', 'password1', 'franz.forelle@example.com',
            this.mandant, TSRole.SACHBEARBEITER_INSTITUTION, undefined, this.institutionForelle));
    }

    private setAmtUsers() {
        this.amtUserList.push(new TSUser('Julien', 'Schuler', 'scju', 'password9', 'julien.schuler@example.com',
            this.mandant, TSRole.SCHULAMT));
        this.amtUserList.push(new TSUser('Jennifer', 'Müller', 'jemu', 'password2', 'jennifer.mueller@example.com',
            this.mandant, TSRole.SACHBEARBEITER_JA));
    }

    /**
     * Der Mandant wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     * @returns {TSMandant}
     */
    private getMandant(): TSMandant {
        const mandant = new TSMandant();
        mandant.name = 'TestMandant';
        mandant.id = 'e3736eb8-6eef-40ef-9e52-96ab48d8f220';
        return mandant;
    }

    /**
     * Die Traegerschaft wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private getTraegerschaftFisch(): TSTraegerschaft {
        const traegerschaft = new TSTraegerschaft();
        traegerschaft.name = 'Fisch';
        traegerschaft.mail = 'fisch@example.com';
        traegerschaft.id = '11111111-1111-1111-1111-111111111111';
        return traegerschaft;
    }

    /**
     * Die Institution wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private getInstitutionForelle(): TSInstitution {
        const institution = new TSInstitution();
        institution.name = 'Forelle';
        institution.id = '22222222-1111-1111-1111-111111111111';
        institution.mail = 'forelle@example.com';
        institution.traegerschaft = this.traegerschaftFisch;
        institution.mandant = this.mandant;
        return institution;
    }

    public logIn(user: TSUser): void {
        this.authServiceRS.loginRequest(user).then(() => {
            AuthenticationUtil.navigateToStartPageForRole(user, this.$state);
        });
    }
}

SchulungComponentConfig.controller = SchulungViewController;
