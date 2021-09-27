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
import {GESUCH_JS_MODULE} from '../gesuch/gesuch.module';
import {ALLE_VERFUEGUNGEN_JS_MODULE} from './alleVerfuegungen/alleVerfuegungen.module';
import {APP_ANGULARJS_COMPONENT} from './app.angularjs.component';
import {CORE_JS_MODULE} from './core/core.angularjs.module';
import {DOSSIER_JS_MODULE} from './dossier/dossier.angularjs.module';
import {FAELLE_JS_MODULE} from './faelle/faelle.module';
import {GESUCHSTELLER_DASHBOARD_JS_MODULE} from './gesuchstellerDashboard/gesuchstellerDashboard.module';
import {MITTEILUNGEN_JS_MODULE} from './mitteilungen/mitteilungen.module';
import {PENDENZEN_BETREUUNGEN_JS_MODULE} from './pendenzen/betreuungen/pendenzenBetreuungen.module';
import {PENDENZEN_JS_MODULE} from './pendenzen/default/pendenzen.module';
import {PENDENZEN_STEUERAMT_JS_MODULE} from './pendenzen/steueramt/pendenzenSteueramt.module';
import {QUICKSEARCH_JS_MODULE} from './quicksearch/quicksearch.module';
import {SEARCH_JS_MODULE} from './searchResult/search.module';
import {STATISTIK_JS_MODULE} from './statistik/statistik.module';

export const APP_JS_MODULE = angular.module('ebeguWeb', [
        'ui.router',
        upgradeModule.name,
        CORE_JS_MODULE.name,
        ADMIN_JS_MODULE.name,
        GESUCH_JS_MODULE.name,
        PENDENZEN_JS_MODULE.name,
        DOSSIER_JS_MODULE.name,
        PENDENZEN_BETREUUNGEN_JS_MODULE.name,
        PENDENZEN_STEUERAMT_JS_MODULE.name,
        FAELLE_JS_MODULE.name,
        GESUCHSTELLER_DASHBOARD_JS_MODULE.name,
        MITTEILUNGEN_JS_MODULE.name,
        SEARCH_JS_MODULE.name,
        STATISTIK_JS_MODULE.name,
        ALLE_VERFUEGUNGEN_JS_MODULE.name,
        QUICKSEARCH_JS_MODULE.name,
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
        definition,
    );
}
