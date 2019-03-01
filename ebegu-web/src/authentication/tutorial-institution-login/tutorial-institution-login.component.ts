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
import TSInstitution from '../../models/TSInstitution';
import {TSMandant} from '../../models/TSMandant';
import {returnToOriginalState} from '../../utils/AuthenticationUtil';
import AuthServiceRS from '../service/AuthServiceRS.rest';

// tslint:disable:no-duplicate-string no-identical-functions
@Component({
    selector: 'dv-tutorial-login',
    templateUrl: './tutorial-institution-login.component.html',
    styleUrls: ['./tutorial-institution-login.component.less'],
})
export class TutorialInstitutionLoginComponent {

    private static readonly ID_KITA_TUTORIAL = '22222222-1111-1111-1111-444444444444';

    @Input() public returnTo: TargetState;

    // Institution Users
    public administratorInstitutionKitaTutorial: TSBenutzer;
    public sachbearbeiterInstitutionKitaTutorial: TSBenutzer;

    public devMode: boolean;

    private readonly mandant: TSMandant;
    private readonly institutionTutorial: TSInstitution;

    public constructor(
        private readonly authServiceRS: AuthServiceRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly stateService: StateService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly translate: TranslateService,
    ) {

        this.mandant = TutorialInstitutionLoginComponent.getMandant();
        this.institutionTutorial = this.getInsitution();
        this.applicationPropertyRS.isDevMode().then(response => {
            this.devMode = response;
        });

        this.initUsers();
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

    /**
     * Die Institution wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private getInsitution(): TSInstitution {
        const institution = new TSInstitution();
        institution.name = 'Kita kiBon';
        institution.id = TutorialInstitutionLoginComponent.ID_KITA_TUTORIAL;
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
