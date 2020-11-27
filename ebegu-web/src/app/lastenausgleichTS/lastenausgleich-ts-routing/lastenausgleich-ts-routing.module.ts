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
import {FreigabeComponent} from '../antrag/freigabe/freigabe.component';
import {GemeindeAngabenComponent} from '../antrag/gemeinde-angaben/gemeinde-angaben.component';
import {LastenausgleichTsBerechnungComponent} from '../antrag/lastenausgleich-ts-berechnung/lastenausgleich-ts-berechnung.component';
import {TagesschulenAngabenComponent} from '../antrag/tagesschulen-angaben/tagesschulen-angaben.component';
import {LastenausgleichTSComponent} from '../lastenausgleich-ts/lastenausgleich-ts.component';

const states: NgHybridStateDeclaration[] = [
    {
        parent: 'app',
        name: 'lastenausgleich-ts',
        url: '/lastenausgleich-ts',
        component: LastenausgleichTSComponent,
    },
    {
        name: 'LASTENAUSGLEICH_TS.ANGABEN_GEMEINDE',
        url: '/:lastenausgleichTsId/angaben-gemeinde',
        component: GemeindeAngabenComponent
    },
    {
        name: 'LASTENAUSGLEICH_TS.ANGABEN_TAGESSCHULEN',
        url: '/:lastenausgleichTsId/angaben-tagesschulen',
        component: TagesschulenAngabenComponent
    },
    {
        name: 'LASTENAUSGLEICH_TS.FREIGABE',
        url: '/:lastenausgleichTsId/freigabe',
        component: FreigabeComponent
    },
    {
        name: 'lastenausgleich-ts.lastenausgleich',
        url: '/:lastenausgleichTsId/lastenausgleich',
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
