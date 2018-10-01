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

import {upgradeModule} from '@uirouter/angular-hybrid';
import {Ng1StateDeclaration, StateProvider} from '@uirouter/angularjs';
import * as angular from 'angular';
import EbeguWebAdmin from '../admin/admin.module';
import {EbeguWebAlleVerfuegungen} from './alleVerfuegungen/alleVerfuegungen.module';
import {EbeguWebFaelle} from './faelle/faelle.module';
import {EbeguWebGesuch} from '../gesuch/gesuch.module';
import {EbeguWebGesuchstellerDashboard} from './gesuchstellerDashboard/gesuchstellerDashboard.module';
import {EbeguWebMitteilungen} from '../mitteilungen/mitteilungen.module';
import {EbeguWebPendenzen} from '../pendenzen/pendenzen.module';
import {EbeguWebPendenzenBetreuungen} from '../pendenzenBetreuungen/pendenzenBetreuungen.module';
import {EbeguWebPendenzenSteueramt} from '../pendenzenSteueramt/pendenzenSteueramt.module';
import {EbeguWebPosteingang} from '../posteingang/posteingang.module';
import {EbeguWebQuicksearch} from '../quicksearch/quicksearch.module';
import {EbeguWebSearch} from '../searchResult/search.module';
import {EbeguWebStatistik} from '../statistik/statistik.module';
import {EbeguWebVerlauf} from '../verlauf/verlauf.module';
import {EbeguWebZahlung} from '../zahlung/zahlung.module';
import {EbeguWebZahlungsauftrag} from '../zahlungsauftrag/zahlungsauftrag.module';
import {AppAngularjsComponent} from './app.angularjs.component';
import {EbeguWebCore} from './core/core.angularjs.module';
import {EbeguWebDossier} from './dossier/dossier.angularjs.module';

export const appModuleAngularJS = angular.module('ebeguWeb', [
        upgradeModule.name,
        EbeguWebCore.name,
        EbeguWebAdmin.name,
        EbeguWebGesuch.name,
        EbeguWebPendenzen.name,
        EbeguWebDossier.name,
        EbeguWebPendenzenBetreuungen.name,
        EbeguWebPendenzenSteueramt.name,
        EbeguWebFaelle.name,
        EbeguWebGesuchstellerDashboard.name,
        EbeguWebMitteilungen.name,
        EbeguWebPosteingang.name,
        EbeguWebSearch.name,
        EbeguWebStatistik.name,
        EbeguWebZahlung.name,
        EbeguWebZahlungsauftrag.name,
        EbeguWebAlleVerfuegungen.name,
        EbeguWebVerlauf.name,
        EbeguWebQuicksearch.name,
    ])
        .component('appRoot', AppAngularjsComponent)
        .config(conf)
;

conf.$inject = ['$stateProvider'];

function conf($stateProvider: StateProvider): void {
    const definition: Ng1StateDeclaration = {
        url: '?{debug:bool}',
        name: 'app',
        component: 'appRoot',
        redirectTo: 'onboarding.start',
        abstract: true,
    };
    $stateProvider.state(
        definition
    );
}
