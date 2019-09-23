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
import {HookResult, Ng2StateDeclaration, Transition} from '@uirouter/angular';
import {UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {map, take} from 'rxjs/operators';
import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../models/enums/TSRole';
import {getRoleBasedTargetState} from '../../utils/AuthenticationUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import {UiViewComponent} from '../shared/ui-view/ui-view.component';
import {OnboardingBeLoginComponent} from './onboarding-be-login/onboarding-be-login.component';
import {OnboardingGsAbschliessenComponent} from './onboarding-gs-abschliessen/onboarding-gs-abschliessen.component';
import {OnboardingInfoGemComponent} from './onboarding-info-gem/onboarding-info-gem.component';
import {OnboardingInfoKitagComponent} from './onboarding-info-kitag/onboarding-info-kitag.component';
import {OnboardingMainComponent} from './onboarding-main/onboarding-main.component';
import {OnboardingNeuBenutzerComponent} from './onboarding-neu-benutzer/onboarding-neu-benutzer.component';
import {OnboardingComponent} from './onboarding/onboarding.component';
import {MandantRS} from '../core/service/mandantRS.rest';

const mandantBernId: string = 'e3736eb8-6eef-40ef-9e52-96ab48d8f220';

const tsEnabledResolver = [
    'MandantRS', (mandantRS: MandantRS) => {
       return mandantRS.findMandant(mandantBernId).then((result: { angebotTS: any; }) => {
           return result.angebotTS;
        });
    },
];

const states: Ng2StateDeclaration[] = [
    {
        parent: 'app',
        name: 'onboarding',
        abstract: true,
        component: OnboardingMainComponent,
    },
    {
        name: 'onboarding.start',
        url: '/',
        data: {
            roles: TSRoleUtil.getAllRoles(),
        },
        onEnter: redirectToLandingPage,
    },
    {
        name: 'onboarding.anmeldung',
        url: '/anmeldung',
        component: OnboardingComponent,
        data: {
            roles: [TSRole.ANONYMOUS],
        },
    },
    {
        name: 'onboarding.be-login',
        url: '/{gemeindenId}',
        component: OnboardingBeLoginComponent,
        data: {
            roles: [TSRole.ANONYMOUS],
        },
    },
    {
        name: 'onboarding.gesuchsteller',
        abstract: true,
        component: UiViewComponent,
        data: {
            roles: [TSRole.GESUCHSTELLER],
        },
        onEnter: disableWhenDossierExists,
    },
    {
        name: 'onboarding.gesuchsteller.registration',
        url: '/registration/{gemeindenId}',
        component: OnboardingGsAbschliessenComponent,
    },
    {
        name: 'onboarding.gesuchsteller.registration-incomplete',
        url: '/registration-abschliessen',
        component: OnboardingNeuBenutzerComponent,
        resolve: {
            isTSAngebotEnabled: tsEnabledResolver,
            nextState: () => 'onboarding.gesuchsteller.registration',
        },
    },
    {
        name: 'onboarding.neubenutzer',
        url: '/neu-benutzer',
        component: OnboardingNeuBenutzerComponent,
        resolve: {
            isTSAngebotEnabled: tsEnabledResolver,
        },
        data: {
            roles: [TSRole.ANONYMOUS],
        },
    },
    {
        name: 'onboarding.infogemeinde',
        url: '/info-gemeinde',
        component: OnboardingInfoGemComponent,
        data: {
            roles: [TSRole.ANONYMOUS],
        },
    },
    {
        name: 'onboarding.infokitag',
        url: '/info-kitag',
        component: OnboardingInfoKitagComponent,
        data: {
            roles: [TSRole.ANONYMOUS],
        },
    },
];

redirectToLandingPage.$inject = ['$transition$'];

function redirectToLandingPage(transition: Transition): HookResult {
    const authService: AuthServiceRS = transition.injector().get('AuthServiceRS');

    return authService.principal$
        .pipe(
            take(1),
            map(principal => {
                if (!principal) {
                    return getRoleBasedTargetState(TSRole.ANONYMOUS, transition.router.stateService);
                }

                // no principal and not allowed to access the target state: redirect to default ANONYMOUS state
                return getRoleBasedTargetState(principal.currentBerechtigung.role, transition.router.stateService);
            })
        ).toPromise();
}

disableWhenDossierExists.$inject = ['$transition$'];

function disableWhenDossierExists(transition: Transition): HookResult {
    const dossierService = transition.injector().get('DossierRS');

    return dossierService.findNewestDossierByCurrentBenutzerAsBesitzer()
    // when there is a dossier, redirect to gesuchsteller.dashboard
        .then(() => transition.router.stateService.target('gesuchsteller.dashboard'))
        // when there is no dossier, continue entering the state
        .catch(() => true);
}

@NgModule({
    imports: [
        UIRouterUpgradeModule.forChild({states}),
    ],
    exports: [
        UIRouterUpgradeModule,
    ],
    declarations: [],
})
export class OnboardingRoutingModule {
}
