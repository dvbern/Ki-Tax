/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {Component, Input} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {StateService, TargetState} from '@uirouter/core';
import {ApplicationPropertyRS} from '../../app/core/rest-services/applicationPropertyRS.rest';
import GemeindeRS from '../../gesuch/service/gemeindeRS.rest';
import {TSRole} from '../../models/enums/TSRole';
import TSBenutzer from '../../models/TSBenutzer';
import TSGemeinde from '../../models/TSGemeinde';
import TSInstitution from '../../models/TSInstitution';
import {TSMandant} from '../../models/TSMandant';
import {returnToOriginalState} from '../../utils/AuthenticationUtil';
import AuthServiceRS from '../service/AuthServiceRS.rest';

// tslint:disable:no-duplicate-string no-identical-functions
@Component({
    selector: 'dv-tutorial-login',
    templateUrl: './tutorial-login.component.html',
    styleUrls: ['./tutorial-login.component.less'],
})
export class TutorialLoginComponent {

    private static readonly ID_GEMEINDE_TUTORIAL = 'ea02b313-e7c3-4b26-9ef7-e413f4046d77';

    @Input() public returnTo: TargetState;

    // Institution Users
    public administratorInstitutionKitaTutorial: TSBenutzer;
    public sachbearbeiterInstitutionKitaTutorial: TSBenutzer;

    // Gemeindeabhängige Users
    public administratorGemeindeTutorial: TSBenutzer;
    public sachbearbeiterGemeindeTutorial: TSBenutzer;

    public steueramtTutorial: TSBenutzer;
    public revisorTutorial: TSBenutzer;
    public juristTutorial: TSBenutzer;

    public devMode: boolean;

    private readonly mandant: TSMandant;
    private gemeindeTutorial: TSGemeinde;
    private readonly institutionTutorial: TSInstitution;

    public constructor(
        private readonly authServiceRS: AuthServiceRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly stateService: StateService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly translate: TranslateService,
    ) {

        this.mandant = TutorialLoginComponent.getMandant();
        this.institutionTutorial = this.getInsitution();
        this.applicationPropertyRS.isDevMode().then(response => {
            this.devMode = response;
        });

        // getAktiveGemeinden() can be called by anonymous.
        this.gemeindeRS.getAktiveGemeinden().then(aktiveGemeinden => {
            this.gemeindeTutorial = aktiveGemeinden
                .find(gemeinde => gemeinde.id === TutorialLoginComponent.ID_GEMEINDE_TUTORIAL);

            this.initUsers();
        });
    }

    /**
     * Der Mandant wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getMandant(): TSMandant {
        const mandant = new TSMandant();
        mandant.name = 'TestMandant';
        mandant.id = 'e3736eb8-6eef-40ef-9e52-96ab48d8f220';
        return mandant;
    }

    private initUsers(): void {
        this.createInstitutionUsers();
        this.createUsersOfGemeinde();
    }

    private createInstitutionUsers(): void {
        this.administratorInstitutionKitaTutorial = new TSBenutzer('Silvia',
            'Tutorial',
            'tusi',
            'password9',
            'silvia.tutorial@example.com',
            this.mandant,
            TSRole.ADMIN_INSTITUTION,
            undefined,
            this.institutionTutorial);
        this.sachbearbeiterInstitutionKitaTutorial = new TSBenutzer('Sophie',
            'Tutorial',
            'tuso',
            'password9',
            'sophie.tutorial@example.com',
            this.mandant,
            TSRole.SACHBEARBEITER_INSTITUTION,
            undefined,
            this.institutionTutorial);
    }

    private createUsersOfGemeinde(): void {
        this.administratorGemeindeTutorial = new TSBenutzer('Gerlinde',
            'Tutorial',
            'tuge',
            'password9',
            'gerlinde.tutorial@example.com',
            this.mandant,
            TSRole.ADMIN_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeTutorial]);
        this.sachbearbeiterGemeindeTutorial = new TSBenutzer('Stefan',
            'Tutorial',
            'tust',
            'password9',
            'stefan.tutorial@example.com',
            this.mandant,
            TSRole.SACHBEARBEITER_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeTutorial]);
        this.steueramtTutorial = new TSBenutzer('Rodolfo',
            'Tutorial',
            'turo',
            'password9',
            'rodolfo.tutorial@example.com',
            this.mandant,
            TSRole.STEUERAMT,
            undefined,
            undefined,
            [this.gemeindeTutorial]);
        this.revisorTutorial = new TSBenutzer('Reto',
            'Tutorial',
            'ture',
            'password9',
            'reto.tutorial@example.com',
            this.mandant,
            TSRole.REVISOR,
            undefined,
            undefined,
            [this.gemeindeTutorial]);
        this.juristTutorial = new TSBenutzer('Julia',
            'Tutorial',
            'tuju',
            'password9',
            'julia.tutorial@example.com',
            this.mandant,
            TSRole.JURIST,
            undefined,
            undefined,
            [this.gemeindeTutorial]);
    }

    /**
     * Die Institution wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private getInsitution(): TSInstitution {
        const institution = new TSInstitution();
        institution.name = 'Kita Tutorial';
        institution.id = '1b6f476f-e0f5-4380-9ef6-836d68885377';
        institution.traegerschaft = undefined;
        institution.mandant = this.mandant;
        return institution;
    }

    public logIn(credentials: TSBenutzer): void {
        this.authServiceRS.loginRequest(credentials)
            .then(() => returnToOriginalState(this.stateService, this.returnTo));
    }

    public getTextForLoginButton(user: TSBenutzer): string {
        if (!user) {
            return '';
        }
        return `${this.translate.instant('TUTORIAL_LOGIN_AS')} ${user.vorname} ${user.nachname}`;
    }
}
