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

import {Ng1StateDeclaration} from '@uirouter/angularjs';
import {RouterHelper} from '../../dvbModules/router/route-helper-provider';
import {TSRoleUtil} from '../../utils/TSRoleUtil';

verlaufRun.$inject = ['RouterHelper'];

export function verlaufRun(routerHelper: RouterHelper): void {
    routerHelper.configureStates(ng1States);
}

const ng1States: Ng1StateDeclaration[] = [
    {
        parent: 'app',
        abstract: true,
        name: 'verlauf-old',
        data: {
            roles: TSRoleUtil.getJugendamtAndSchulamtRole(),
        },
    },
    {
        name: 'verlauf.view',
        template: '<verlauf-view flex="auto" class="overflow-hidden" layout="column">',
        url: '/verlauf/:gesuchId',
    },
];

// TODO hefa implizite state param definition
export class IVerlaufStateParams {
    public gesuchId: string;
}
