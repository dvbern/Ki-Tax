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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {NgModule} from '@angular/core';
import {NgHybridStateDeclaration, UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {UiViewComponent} from '../../shared/ui-view/ui-view.component';
import {ZahlungsauftragViewXComponent} from '../zahlungsauftrag-view-x/zahlungsauftrag-view-x.component';
import {ZahlungviewXComponent} from '../zahlungview-x/zahlungview-x.component';

export class IBooleanStateParams {
    public isMahlzeitenzahlungen: boolean;
}
const states: NgHybridStateDeclaration[] = [
    {
        parent: 'app',
        abstract: true,
        name: 'zahlung',
        url: '/zahlung',
        component: UiViewComponent,
        data: {
            roles: TSRoleUtil.getAllRolesForZahlungen(),
        },
    },
    {
        name: 'zahlung.view',
        url: '/:zahlungsauftragId',
        component: ZahlungviewXComponent,
        params: {
            isMahlzeitenzahlungen: IBooleanStateParams,
        },
    },
    {
        parent: 'app',
        abstract: true,
        name: 'zahlungsauftrag',
        component: UiViewComponent,
        url: '/zahlungsauftrag',
        data: {
            roles: TSRoleUtil.getAllRolesForZahlungen(),
        },
    },
    {
        name: 'zahlungsauftrag.view',
        component: ZahlungsauftragViewXComponent,
        url: '',
        params: {
            isMahlzeitenzahlungen: false,
        },
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
export class ZahlungRoutingModule {
}
