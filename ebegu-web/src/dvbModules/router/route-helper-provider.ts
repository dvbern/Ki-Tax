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

import {Ng2StateDeclaration, Transition} from '@uirouter/angular';
import {Ng1StateDeclaration, StateProvider, UIRouter} from '@uirouter/angularjs';
import {ILocationProvider, IServiceProvider} from 'angular';

export class RouterHelper {
    public static $inject = ['$stateProvider', '$uiRouterProvider'];

    public constructor(public stateProvider: StateProvider, public uiRouterProvider: UIRouter) {
    }

    public configureStates(legacy: Ng1StateDeclaration[] = [], states: Ng2StateDeclaration[] = []): void {
        legacy.forEach(state => {
            this.stateProvider.state(state);
        });
        states.forEach(state => {
            this.stateProvider.state(state);
        });

        this.uiRouterProvider.urlService.rules.otherwise('/');
    }
}

export default class RouterHelperProvider implements IServiceProvider {
    public static $inject = ['$locationProvider', '$stateProvider', '$uiRouterProvider'];

    private readonly routerHelper: RouterHelper;

    public constructor($locationProvider: ILocationProvider,
                       $stateProvider: StateProvider,
                       $uiRouterProvider: UIRouter) {
        $locationProvider.html5Mode(false);
        this.routerHelper = new RouterHelper($stateProvider, $uiRouterProvider);
    }

    public $get(): RouterHelper {
        return this.routerHelper;
    }
}

export function hasFromState(transition: Transition): boolean {
    return transition.from() && transition.from().name !== '';
}
