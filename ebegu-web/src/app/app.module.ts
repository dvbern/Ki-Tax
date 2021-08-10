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
import {BrowserModule} from '@angular/platform-browser';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {UpgradeModule} from '@angular/upgrade/static';
import {NgAdminModule} from '../admin/ng-admin.module';
import {NgAuthenticationModule} from '../authentication/ng-authentication.module';
import {NgGesuchModule} from '../gesuch/ng-gesuch.module';
import {AppRoutingModule} from './app-routing.module';
import {APP_JS_MODULE} from './app.angularjs.module';
import {BenutzerModule} from './benutzer/benutzer.module';
import {CoreModule} from './core/core.module';
import {EinladungModule} from './einladung/einladung.module';
import {GemeindeAntraegeModule} from './gemeinde-antraege/gemeinde-antraege.module';
import {GemeindeModule} from './gemeinde/gemeinde.module';
import {InstitutionModule} from './institution/institution.module';
import {LastenausgleichModule} from './lastenausgleich/lastenausgleich.module';
import {NotrechtModule} from './notrecht/notrecht.module';
import {OnboardingModule} from './onboarding/onboarding.module';
import {PendenzenXModule} from './pendenzen/pendenzen-x.module';
import {NgPosteingangModule} from './posteingang/ng-posteingang.module';
import {SharedModule} from './shared/shared.module';
import {SozialdienstModule} from './sozialdienst/sozialdienst.module';
import {TraegerschaftModule} from './traegerschaft/traegerschaft.module';
import {VerlaufModule} from './verlauf/verlauf.module';
import {WelcomeModule} from './welcome/welcome.module';
import {WizardstepXModule} from './wizardstepX/wizardstep-x.module';
import {ZahlungXModule} from './zahlung/zahlung-x.module';

@NgModule({
    imports: [
        BrowserModule,
        NoopAnimationsModule, // we don't want material animations in the project yet
        UpgradeModule,

        // Core & Shared
        CoreModule.forRoot(),
        SharedModule,

        AppRoutingModule,
        BenutzerModule,
        EinladungModule,
        GemeindeModule,
        InstitutionModule,
        NgAdminModule,
        NgAuthenticationModule,
        NgGesuchModule,
        NgPosteingangModule,
        OnboardingModule,
        TraegerschaftModule,
        WelcomeModule,
        NotrechtModule,
        GemeindeAntraegeModule,
        LastenausgleichModule,
        WizardstepXModule,
        SozialdienstModule,
        PendenzenXModule,
        ZahlungXModule,
        PendenzenXModule,
        VerlaufModule
    ],
})

export class AppModule {

    public constructor(private readonly upgrade: UpgradeModule) {
    }

    // noinspection JSUnusedGlobalSymbols
    public ngDoBootstrap(): void {
        // noinspection XHTMLIncompatabilitiesJS
        this.upgrade.bootstrap(document.body, [APP_JS_MODULE.name], {strictDi: true});
    }
}
