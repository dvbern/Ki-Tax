/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {GemeindeKennzahlenFormularComponent} from '../gemeinde-kennzahlen-formular/gemeinde-kennzahlen-formular.component';
import {GemeindeKennzahlenUiComponent} from '../gemeinde-kennzahlen-ui/gemeinde-kennzahlen-ui.component';

const states: NgHybridStateDeclaration[] = [
    {
        parent: 'app',
        name: 'GEMEINDE_KENNZAHLEN',
        url: '/gemeinde-kennzahlen/:id',
        component: GemeindeKennzahlenUiComponent,
        resolve: [
            {
                token: 'gemeindeKennzahlenId',
                deps: [Transition],
                resolveFn: (trans: Transition) =>
                    (trans.params().id),
            },
        ],
        data: {
            roles: TSRoleUtil.getGemeindeKennzahlenRoles()
        }
    },
    {
        name: 'GEMEINDE_KENNZAHLEN.FORMULAR',
        url: '/formular',
        component: GemeindeKennzahlenFormularComponent
    }
];

@NgModule({
    imports: [
        UIRouterUpgradeModule.forChild({states}),
    ],
    exports: [
        UIRouterUpgradeModule,
    ],
})
export class GemeindeKennzahlenRoutingModule {
}
