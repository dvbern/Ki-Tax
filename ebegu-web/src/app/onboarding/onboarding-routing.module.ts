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
import {NgHybridStateDeclaration, UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {HookResult, Transition} from '@uirouter/core';
import {IPromise} from 'angular';
import {map, take} from 'rxjs/operators';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../models/enums/TSRole';
import {getRoleBasedTargetState} from '../../utils/AuthenticationUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import {MandantRS} from '../core/service/mandantRS.rest';
import {UiViewComponent} from '../shared/ui-view/ui-view.component';
import {DummyMandantSelectionComponent} from './dummy-mandant-selection/dummy-mandant-selection.component';
import {OnboardingBeLoginComponent} from './onboarding-be-login/onboarding-be-login.component';
import {OnboardingGsAbschliessenComponent} from './onboarding-gs-abschliessen/onboarding-gs-abschliessen.component';
import {OnboardingInfoGemeindeComponent} from './onboarding-info-gemeinde/onboarding-info-gemeinde.component';
import {OnboardingInfoInstitutionComponent} from './onboarding-info-institution/onboarding-info-institution.component';
import {OnboardingMainComponent} from './onboarding-main/onboarding-main.component';
import {OnboardingNeuBenutzerComponent} from './onboarding-neu-benutzer/onboarding-neu-benutzer.component';
import {OnboardingComponent} from './onboarding/onboarding.component';

resolveTSEnabled.$inject = ['MandantRS'];

export function resolveTSEnabled(mandantRS: MandantRS): IPromise<boolean> {
    const mandantBernId = 'e3736eb8-6eef-40ef-9e52-96ab48d8f220';
    return mandantRS.findMandant(mandantBernId).then((result: { angebotTS: any; }) => {
        return result.angebotTS;
    });
}

export function nextState(): string {
    return 'onboarding.gesuchsteller.registration';
}

export const STATES: NgHybridStateDeclaration[] = [
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
        parent: 'app',
        name: 'mandant',
        url: '/mandant',
        data: {
            roles: TSRoleUtil.getAllRoles(),
        },
        component: DummyMandantSelectionComponent,
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
        url: '/:gemeindeBGId/{gemeindenId}',
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
        url: '/registration/:gemeindeBGId/{gemeindenId}',
        component: OnboardingGsAbschliessenComponent,
    },
    {
        name: 'onboarding.gesuchsteller.registration-incomplete',
        url: '/registration-abschliessen',
        component: OnboardingNeuBenutzerComponent,
        resolve: {
            isTSAngebotEnabled: resolveTSEnabled,
            nextState,
        },
    },
    {
        name: 'onboarding.neubenutzer',
        url: '/neu-benutzer',
        component: OnboardingNeuBenutzerComponent,
        resolve: {
            isTSAngebotEnabled: resolveTSEnabled,
        },
        data: {
            roles: [TSRole.ANONYMOUS],
        },
    },
    {
        name: 'onboarding.infogemeinde',
        url: '/info-gemeinde',
        component: OnboardingInfoGemeindeComponent,
        data: {
            roles: [TSRole.ANONYMOUS],
        },
    },
    {
        name: 'onboarding.infoinstitution',
        url: '/info-institution',
        component: OnboardingInfoInstitutionComponent,
        data: {
            roles: [TSRole.ANONYMOUS],
        },
    },
];

redirectToLandingPage.$inject = ['$transition$'];

export function redirectToLandingPage(transition: Transition): HookResult {
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
            }),
        ).toPromise();
}

disableWhenDossierExists.$inject = ['$transition$'];

export function disableWhenDossierExists(transition: Transition): HookResult {
    const dossierService = transition.injector().get('DossierRS');

    return dossierService.findNewestDossierByCurrentBenutzerAsBesitzer()
        // when there is a dossier, redirect to gesuchsteller.dashboard
        .then(() => transition.router.stateService.target('gesuchsteller.dashboard'))
        // when there is no dossier, continue entering the state
        .catch(() => true);
}

@NgModule({
    imports: [
        UIRouterUpgradeModule.forChild({states: STATES}),
    ],
    exports: [
        UIRouterUpgradeModule,
    ],
    declarations: [],
})
export class OnboardingRoutingModule {
}
