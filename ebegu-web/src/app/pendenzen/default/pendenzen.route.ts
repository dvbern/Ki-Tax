/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {Ng1StateDeclaration} from '@uirouter/angularjs';
import {RouterHelper} from '../../../dvbModules/router/route-helper-provider';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';

pendenzRun.$inject = ['RouterHelper'];

export function pendenzRun(routerHelper: RouterHelper): void {
    routerHelper.configureStates(ng1States);
}

const ng1States: Ng1StateDeclaration[] = [
    {
        parent: 'app',
        abstract: true,
        name: 'pendenzen',
        data: {
            roles: TSRoleUtil.getAdministratorOrAmtOrSozialdienstRolle(),
        },
    },
    {
        name: 'pendenzen.list-view',
        template: '<pendenzen-list-view flex="auto" class="overflow-scroll">',
        url: '/pendenzen/:tourType?',
        params:  {
            tourType: {
                value: null,
                squash: true
            }
        }
    },
];
