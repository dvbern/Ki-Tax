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
import ADMIN_JS_MODULE from '../admin/admin.module';
import {EbeguWebGesuch} from '../gesuch/gesuch.module';
import {ZAHLUNG_JS_MODULE} from './zahlung/zahlung.module';
import {EbeguWebZahlungsauftrag} from './zahlungsauftrag/zahlungsauftrag.module';
import {ALLE_VERFUEGUNGEN_JS_MODULE} from './alleVerfuegungen/alleVerfuegungen.module';
import {APP_ANGULARJS_COMPONENT} from './app.angularjs.component';
import {CORE_JS_MODULE} from './core/core.angularjs.module';
import {DOSSIER_JS_MODULE} from './dossier/dossier.angularjs.module';
import {EbeguWebFaelle} from './faelle/faelle.module';
import {EbeguWebGesuchstellerDashboard} from './gesuchstellerDashboard/gesuchstellerDashboard.module';
import {EbeguWebMitteilungen} from './mitteilungen/mitteilungen.module';
import {EbeguWebPendenzenBetreuungen} from './pendenzen/betreuungen/pendenzenBetreuungen.module';
import {EbeguWebPendenzen} from './pendenzen/default/pendenzen.module';
import {EbeguWebPendenzenSteueramt} from './pendenzen/steueramt/pendenzenSteueramt.module';
import {EbeguWebPosteingang} from './posteingang/posteingang.module';
import {EbeguWebQuicksearch} from './quicksearch/quicksearch.module';
import {EbeguWebSearch} from './searchResult/search.module';
import {EbeguWebStatistik} from './statistik/statistik.module';
import {VERLAUF_JS_MODULE} from './verlauf/verlauf.module';

export const APP_JS_MODULE = angular.module('ebeguWeb', [
        upgradeModule.name,
        CORE_JS_MODULE.name,
        ADMIN_JS_MODULE.name,
        EbeguWebGesuch.name,
        EbeguWebPendenzen.name,
        DOSSIER_JS_MODULE.name,
        EbeguWebPendenzenBetreuungen.name,
        EbeguWebPendenzenSteueramt.name,
        EbeguWebFaelle.name,
        EbeguWebGesuchstellerDashboard.name,
        EbeguWebMitteilungen.name,
        EbeguWebPosteingang.name,
        EbeguWebSearch.name,
        EbeguWebStatistik.name,
        ZAHLUNG_JS_MODULE.name,
        EbeguWebZahlungsauftrag.name,
        ALLE_VERFUEGUNGEN_JS_MODULE.name,
        VERLAUF_JS_MODULE.name,
        EbeguWebQuicksearch.name,
    ])
        .component('appRoot', APP_ANGULARJS_COMPONENT)
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
