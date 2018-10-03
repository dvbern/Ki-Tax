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

mitteilungenRun.$inject = ['RouterHelper'];

export function mitteilungenRun(routerHelper: RouterHelper): void {
    routerHelper.configureStates(ng1States, []);
}

const ng1States: Ng1StateDeclaration[] = [
    {
        parent: 'app',
        abstract: true,
        name: 'mitteilungen',
        data: {
            roles: TSRoleUtil.getAllRolesButSteueramt(),
        },
    },
    {
        name: 'mitteilungen.view',
        template: '<mitteilungen-view flex="auto" class="overflow-hidden" layout="column">',
        url: '/mitteilungen/:fallId/:dossierId/:betreuungId',
        params: {
            betreuungId: '',
        },
    },
];

// TODO hefa nicht alle Params wurden bei der StateDefinition deklariert!
export class IMitteilungenStateParams {
    public fallId: string;
    public dossierId: string;
    public betreuungId: string;
    public mitteilungId: string;
}
