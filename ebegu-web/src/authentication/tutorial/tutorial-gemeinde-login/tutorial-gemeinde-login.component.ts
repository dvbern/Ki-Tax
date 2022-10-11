/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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
import {StateService, TargetState} from '@uirouter/core';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSMandant} from '../../../models/TSMandant';
import {returnToOriginalState} from '../../../utils/AuthenticationUtil';
import {AuthServiceRS} from '../../service/AuthServiceRS.rest';

@Component({
    selector: 'dv-tutorial-gemeinde-login',
    templateUrl: './tutorial-gemeinde-login.component.html',
    styleUrls: ['../tutorial-login.component.less']
})
export class TutorialGemeindeLoginComponent {

    private static readonly ID_GEMEINDE_TUTORIAL = '11111111-1111-4444-4444-111111111111';

    @Input() public returnTo: TargetState;

    // Only the role Sachbearbeiter. This simplifies the tutorial and gives the user a restricted access
    public sachbearbeiterGemeindeTutorial: TSBenutzer;

    private readonly mandant: TSMandant;
    private gemeindeTutorial: TSGemeinde;

    public constructor(
        private readonly authServiceRS: AuthServiceRS,
        private readonly stateService: StateService,
        private readonly gemeindeRS: GemeindeRS
    ) {

        this.mandant = TutorialGemeindeLoginComponent.getMandant();

        // getAktiveGemeinden() can be called by anonymous.
        this.gemeindeRS.getAktiveGemeinden().then(aktiveGemeinden => {
            this.gemeindeTutorial = aktiveGemeinden
                .find(gemeinde => gemeinde.id === TutorialGemeindeLoginComponent.ID_GEMEINDE_TUTORIAL);

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
        this.createUsersOfGemeinde();
    }

    private createUsersOfGemeinde(): void {
        this.sachbearbeiterGemeindeTutorial = new TSBenutzer('Gerlinde',
            'Tutorial',
            'tust',
            'password9',
            'gerlinde.tutorial@example.com',
            this.mandant,
            TSRole.ADMIN_BG,
            undefined,
            undefined,
            [this.gemeindeTutorial]);
    }

    public logIn(): void {
        this.authServiceRS.loginRequest(this.sachbearbeiterGemeindeTutorial)
            .then(() => returnToOriginalState(this.stateService, this.returnTo));
    }
}
