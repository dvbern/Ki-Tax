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

import {RouterHelper} from '../dvbModules/router/route-helper-provider';
import {Ng1StateDeclaration} from '@uirouter/angularjs';

verlaufRun.$inject = ['RouterHelper'];

/* @ngInject */
export function verlaufRun(routerHelper: RouterHelper) {
    routerHelper.configureStates(getStates(), '/start');
}

function getStates(): Ng1StateDeclaration[] {
    return [
        new EbeguVerlaufState()
    ];
}

//STATES

export class EbeguVerlaufState implements Ng1StateDeclaration {
    name = 'verlauf';
    template = '<verlauf-view flex="auto" class="overflow-hidden" layout="column">';
    url = '/verlauf/:gesuchId';
}

// PARAMS

export class IVerlaufStateParams {
    gesuchId: string;
}
