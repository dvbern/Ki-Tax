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
import {SharedModule} from '../shared/shared.module';
import {DummyMandantSelectionComponent} from './dummy-mandant-selection/dummy-mandant-selection.component';
import {OnboardingGsAbschliessenComponent} from './onboarding-gs-abschliessen/onboarding-gs-abschliessen.component';
import {OnboardingComponent} from './onboarding/onboarding.component';
import {OnboardingBeLoginComponent} from './onboarding-be-login/onboarding-be-login.component';
import {OnboardingMainComponent} from './onboarding-main/onboarding-main.component';
import {OnboardingRoutingModule} from './onboarding-routing.module';
import { OnboardingNeuBenutzerComponent } from './onboarding-neu-benutzer/onboarding-neu-benutzer.component';
import { OnboardingInfoGemeindeComponent } from './onboarding-info-gemeinde/onboarding-info-gemeinde.component';
import { OnboardingInfoInstitutionComponent } from './onboarding-info-institution/onboarding-info-institution.component';

@NgModule({
    imports: [
        SharedModule,
        OnboardingRoutingModule,
    ],
    declarations: [
        OnboardingComponent,
        OnboardingBeLoginComponent,
        OnboardingMainComponent,
        OnboardingGsAbschliessenComponent,
        OnboardingNeuBenutzerComponent,
        OnboardingInfoGemeindeComponent,
        OnboardingInfoInstitutionComponent,
        DummyMandantSelectionComponent
    ],
})
class OnboardingModule {
}

export {OnboardingModule};
