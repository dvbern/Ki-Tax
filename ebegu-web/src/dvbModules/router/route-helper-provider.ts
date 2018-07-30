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

import {StateProvider, Ng1StateDeclaration, UIRouter} from '@uirouter/angularjs';
import {ILocationProvider, IServiceProvider} from 'angular';
import {Ng2StateDeclaration} from '@uirouter/angular';

export class RouterHelper {
    static $inject = ['$stateProvider', '$uiRouterProvider'];

    hasOtherwise: boolean = false;

    constructor(public stateProvider: StateProvider, public uiRouterProvider: UIRouter) {
    }

    public configureStates(states: Array<Ng1StateDeclaration | Ng2StateDeclaration>, otherwisePath?: string): void {
        states.forEach((state) => {
            // TODO update ui-router to 6.0.0 to get the correct typing
            // https://github.com/ui-router/angular-hybrid/blob/master/CHANGELOG.md#600-2018-05-20
            this.stateProvider.state(state as Ng1StateDeclaration);
        });
        if (otherwisePath && !this.hasOtherwise) {
            this.hasOtherwise = true;
            this.uiRouterProvider.urlService.rules.otherwise(otherwisePath);
        }
    }

    // public getStates(): Ng1StateDeclaration[] {
    //     return this.stateProvider.$get();
    // }
}

export default class RouterHelperProvider implements IServiceProvider {
    static $inject = ['$locationProvider', '$stateProvider', '$uiRouterProvider'];

    private readonly routerHelper: RouterHelper;

    /* @ngInject */
    constructor($locationProvider: ILocationProvider, $stateProvider: StateProvider, $uiRouterProvider: UIRouter) {
        $locationProvider.html5Mode(false);
        this.routerHelper = new RouterHelper($stateProvider, $uiRouterProvider);
    }

    $get(): RouterHelper {
        return this.routerHelper;
    }
}
