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
import uiRouter, {StateProvider} from '@uirouter/angularjs';
import * as angular from 'angular';
import './app.module.less';
import {AppAngularjsComponent} from './app/app.angularjs.component';
import {EbeguWebCore} from './core/core.angularjs.module';
import './style/mediaqueries.less';

export const appModuleAngularJS = angular.module('ebeguWeb', [
        uiRouter,
        upgradeModule.name,
        // EbeguWebCore.name,
        //EbeguWebAdmin.name, EbeguWebGesuch.name, EbeguWebPendenzen.name,
        // EbeguWebPendenzenBetreuungen.name, EbeguWebPendenzenSteueramt.name, EbeguWebFaelle.name, EbeguWebGesuchstellerDashboard.name,
        // EbeguWebMitteilungen.name, EbeguWebPosteingang.name, EbeguWebSearch.name, EbeguWebStatistik.name, EbeguWebZahlung.name,
        // EbeguWebZahlungsauftrag.name, EbeguWebAlleVerfuegungen.name, EbeguWebVerlauf.name, EbeguWebQuicksearch.name
    ])
        .component('appRoot', AppAngularjsComponent)
        .config(conf)
;

conf.$inject = ['$stateProvider'];

function conf($stateProvider: StateProvider): void {
    $stateProvider.state(
        {
            url: '/foo',
            name: 'app',
            component: 'appRoot',
            // redirectTo: 'welcome',
            // template: '<h1>hallo</h1>'
        },
        // {
        //     parent: 'app',
        //     name: 'welcome',
        //     url: '/welcome',
        //     component: 'welcome'
        // }
    )
    ;
}
