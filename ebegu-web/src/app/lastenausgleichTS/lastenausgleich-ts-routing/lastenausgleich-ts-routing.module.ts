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
import {FreigabeComponent} from '../antrag/freigabe/freigabe.component';
import {GemeindeAngabenComponent} from '../antrag/gemeinde-angaben/gemeinde-angaben.component';
import {LastenausgleichTsBerechnungComponent} from '../antrag/lastenausgleich-ts-berechnung/lastenausgleich-ts-berechnung.component';
import {TagesschulenAngabenComponent} from '../antrag/tagesschulen-angaben/tagesschulen-angaben.component';
import {TagesschulenListComponent} from '../antrag/tagesschulen-list/tagesschulen-list.component';
import {GemeindeAntraegeComponent} from '../gemeinde-antraege/gemeinde-antraege.component';
import {LastenausgleichTSComponent} from '../lastenausgleich-ts/lastenausgleich-ts.component';
import {GemeindeAntragService} from '../services/gemeinde-antrag.service';

const states: NgHybridStateDeclaration[] = [
    {
        parent: 'app',
        name: 'GEMEINDE_ANTRAEGE',
        url: '/gemeinde-antraege',
        component: GemeindeAntraegeComponent,
    },
    {
        parent: 'app',
        name: 'LASTENAUSGLEICH_TS',
        url: '/lastenausgleich-ts/:id',
        component: LastenausgleichTSComponent,
        resolve: [
            {
                token: 'lastenausgleichId',
                deps: [Transition, GemeindeAntragService],
                resolveFn: (trans: Transition) =>
                    (trans.params().id),
            },
        ],
    },
    {
        name: 'LASTENAUSGLEICH_TS.ANGABEN_GEMEINDE',
        url: '/angaben-gemeinde',
        component: GemeindeAngabenComponent
    },
    {
        name: 'LASTENAUSGLEICH_TS.ANGABEN_TAGESSCHULEN',
        url: '/angaben-tagesschulen',
        component: TagesschulenListComponent
    },
    {
        name: 'LASTENAUSGLEICH_TS.FREIGABE',
        url: '/freigabe',
        component: FreigabeComponent
    },
    {
        name: 'lastenausgleich-ts.lastenausgleich',
        url: '/lastenausgleich',
        component: LastenausgleichTsBerechnungComponent
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
