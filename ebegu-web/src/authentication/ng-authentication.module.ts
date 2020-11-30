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

import {NgModule} from '@angular/core';
import {SharedModule} from '../app/shared/shared.module';
import {LocalLoginComponent} from './local-login/local-login.component';
import {NgAuthenticationRoutingModule} from './ng-authentication-routing.module';
import {TutorialGemeindeLoginComponent} from './tutorial/tutorial-gemeinde-login/tutorial-gemeinde-login.component';
import {TutorialInstitutionLoginComponent} from './tutorial/tutorial-institution-login/tutorial-institution-login.component';

@NgModule({
    imports: [
        SharedModule,
        NgAuthenticationRoutingModule,
    ],
    declarations: [
        LocalLoginComponent,
        TutorialInstitutionLoginComponent,
        TutorialGemeindeLoginComponent,
    ],
    providers: [
        // use providedIn in the class instead
    ],
})

export class NgAuthenticationModule {
}
