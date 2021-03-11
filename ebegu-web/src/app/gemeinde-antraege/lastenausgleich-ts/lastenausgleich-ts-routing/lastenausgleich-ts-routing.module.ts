/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {NgModule} from '@angular/core';
import {NgHybridStateDeclaration, UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {Transition} from '@uirouter/core';
import {FreigabeComponent} from '../../gemeinde-antraege/antrag/freigabe/freigabe.component';
import {GemeindeAngabenComponent} from '../../gemeinde-antraege/antrag/gemeinde-angaben/gemeinde-angaben.component';
import {LastenausgleichTsBerechnungComponent} from '../../gemeinde-antraege/antrag/lastenausgleich-ts-berechnung/lastenausgleich-ts-berechnung.component';
import {TagesschulenAngabenComponent} from '../../gemeinde-antraege/antrag/tagesschulen-angaben/tagesschulen-angaben.component';
import {TagesschulenListComponent} from '../../gemeinde-antraege/antrag/tagesschulen-list/tagesschulen-list.component';
import {LastenausgleichTSComponent} from '../lastenausgleich-ts/lastenausgleich-ts.component';
import {TagesschulenUiViewComponent} from '../tagesschulen-ui-view/tagesschulen-ui-view.component';

const states: NgHybridStateDeclaration[] = [
    {
        parent: 'app',
        name: 'LASTENAUSGLEICH_TS',
        url: '/lastenausgleich-ts/:id',
        component: LastenausgleichTSComponent,
        resolve: [
            {
                token: 'lastenausgleichId',
                deps: [Transition],
                resolveFn: (trans: Transition) =>
                    (trans.params().id),
            },
        ],
    },
    {
        name: 'LASTENAUSGLEICH_TS.ANGABEN_GEMEINDE',
        url: '/angaben-gemeinde?triggerValidation',
        component: GemeindeAngabenComponent,
        resolve: [
            {
                token: 'triggerValidationOnInit',
                deps: [Transition],
                resolveFn: (trans: Transition) =>
                    trans.params().triggerValidation,
            },
        ],
    },
    {
        name: 'LASTENAUSGLEICH_TS.ANGABEN_TAGESSCHULEN',
        url: '/angaben-tagesschulen',
        component: TagesschulenUiViewComponent,
    },
    {
        name: 'LASTENAUSGLEICH_TS.ANGABEN_TAGESSCHULEN.LIST',
        url: '/list',
        component: TagesschulenListComponent,
    },
    {
        name: 'LASTENAUSGLEICH_TS.ANGABEN_TAGESSCHULEN.DETAIL',
        url: '/:institutionId',
        component: TagesschulenAngabenComponent,
        resolve: [
            {
                token: 'institutionContainerId',
                deps: [Transition],
                resolveFn: (trans: Transition) =>
                    (trans.params().institutionId),
            },
        ],
    },
    {
        name: 'LASTENAUSGLEICH_TS.FREIGABE',
        url: '/freigabe',
        component: FreigabeComponent,
    },
    {
        name: 'lastenausgleich-ts.lastenausgleich',
        url: '/lastenausgleich',
        component: LastenausgleichTsBerechnungComponent,
    },
];

@NgModule({
    imports: [
        UIRouterUpgradeModule.forChild({states}),
    ],
    exports: [
        UIRouterUpgradeModule,
    ],
})
export class LastenausgleichTsRoutingModule {
}
