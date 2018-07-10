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
import {UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {TraegerschaftRS} from '../core/service/traegerschaftRS.rest';
import {TestdatenViewComponent} from './component/testdatenView/testdatenView';
import {TraegerschaftViewComponent} from './component/traegerschaftView/traegerschaftView';
import {Ng2StateDeclaration} from '@uirouter/angular';



export const traegerschaftState: Ng2StateDeclaration = {
    name: 'traegerschaft',
    url: '/traegerschaft',
    component: TraegerschaftViewComponent,
    resolve: [
        {
            token: 'traegerschaften',
            deps: [TraegerschaftRS],
            resolveFn: getTraegerschaften,
        }
    ]
};

export const testdatenState: Ng2StateDeclaration = {
    name: 'testdaten',
    url: '/testdaten',
    component: TestdatenViewComponent,
};


@NgModule({
    imports: [
        UIRouterUpgradeModule.forChild({ states: [ traegerschaftState, testdatenState ] })
    ],
    exports: [],
})
export class NgAdminRoutingModule {
}



function getTraegerschaften(traegerschaftRS: TraegerschaftRS) {
    return traegerschaftRS.getAllActiveTraegerschaften();
}
