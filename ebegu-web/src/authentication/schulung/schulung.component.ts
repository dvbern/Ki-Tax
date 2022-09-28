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
import {IComponentOptions, IController} from 'angular';
import {TestFaelleRS} from '../../admin/service/testFaelleRS.rest';
import {LogFactory} from '../../app/core/logging/LogFactory';
import {TSRole} from '../../models/enums/TSRole';
import {TSBenutzer} from '../../models/TSBenutzer';
import {TSInstitution} from '../../models/TSInstitution';
import {TSMandant} from '../../models/TSMandant';
import {TSTraegerschaft} from '../../models/TSTraegerschaft';
import {navigateToStartPageForRole} from '../../utils/AuthenticationUtil';
import {AuthServiceRS} from '../service/AuthServiceRS.rest';

export const SCHULUNG_COMPONENT_CONFIG: IComponentOptions = {
    transclude: false,
    template: require('./schulung.component.html'),
    controllerAs: 'vm',
};

const LOG = LogFactory.createLog('SchulungViewController');

export class SchulungViewController implements IController {

    public static $inject: string[] = ['$state', 'AuthServiceRS', 'TestFaelleRS'];

    public usersList: Array<TSBenutzer> = Array<TSBenutzer>();
    private gesuchstellerList: string[];
    private readonly institutionsuserList: Array<TSBenutzer> = Array<TSBenutzer>();
    private readonly amtUserList: Array<TSBenutzer> = Array<TSBenutzer>();
    private readonly mandant: TSMandant;
    private readonly institutionForelle: TSInstitution;
    private readonly traegerschaftFisch: TSTraegerschaft;

    public constructor(
        private readonly $state: StateService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly testFaelleRS: TestFaelleRS,
    ) {

        this.mandant = this.getMandant();
        this.traegerschaftFisch = this.getTraegerschaftFisch();
        this.institutionForelle = this.getInstitutionForelle();
        this.testFaelleRS.getSchulungBenutzer().subscribe((response: any) => {
            this.gesuchstellerList = response;
            for (let i = 0; i < this.gesuchstellerList.length; i++) {
                const name = this.gesuchstellerList[i];
                const username = 'sch' + (((i + 1) < 10) ? '0' + (i + 1).toString() : (i + 1).toString());
                const benutzer = new TSBenutzer('Sandra',
                    name,
                    username,
                    'password1',
                    `sandra.${name.toLocaleLowerCase()}@example.com`,
                    this.mandant,
                    TSRole.GESUCHSTELLER);
                this.usersList.push(benutzer);
            }

            this.setInstitutionUsers();
            this.setAmtUsers();
        }, err => LOG.error(err));
    }

    private setInstitutionUsers(): void {
        this.institutionsuserList.push(new TSBenutzer('Fritz', 'Fisch', 'sch20', 'password1', 'fritz.fisch@example.com',
            this.mandant, TSRole.SACHBEARBEITER_TRAEGERSCHAFT, this.traegerschaftFisch, undefined));
        this.institutionsuserList.push(new TSBenutzer('Franz',
            'Forelle',
            'sch21',
            'password1',
            'franz.forelle@example.com',
            this.mandant,
            TSRole.SACHBEARBEITER_INSTITUTION,
            undefined,
            this.institutionForelle));
    }

    private setAmtUsers(): void {
        this.amtUserList.push(new TSBenutzer('Julien', 'Schuler', 'scju', 'password9', 'julien.schuler@example.com',
            this.mandant, TSRole.SACHBEARBEITER_TS));
        this.amtUserList.push(new TSBenutzer('Jennifer', 'Müller', 'jemu', 'password2', 'jennifer.mueller@example.com',
            this.mandant, TSRole.SACHBEARBEITER_BG));
    }

    /**
     * Der Mandant wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
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
        institution.traegerschaft = this.traegerschaftFisch;
        institution.mandant = this.mandant;
        return institution;
    }

    public logIn(credentials: TSBenutzer): void {
        this.authServiceRS.loginRequest(credentials)
            .then(user => navigateToStartPageForRole(user.getCurrentRole(), this.$state));
    }
}

SCHULUNG_COMPONENT_CONFIG.controller = SchulungViewController;
