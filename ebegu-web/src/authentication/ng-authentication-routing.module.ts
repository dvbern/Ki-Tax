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
import {Ng2StateDeclaration} from '@uirouter/angular';
import {ApplicationPropertyRS} from '../admin/service/applicationPropertyRS.rest';
import {StateService} from '@uirouter/core';
import {Log} from '../utils/LogFactory';
import {IPromise} from 'angular';
import {DummyAuthenticationListViewComponent} from './dummyAuthenticaton';



export const localLoginState: Ng2StateDeclaration = {
    name: 'locallogin',
    url: '/locallogin',
    component: DummyAuthenticationListViewComponent,
    resolve: [
        {
            token: 'locallogin',
            deps: [ApplicationPropertyRS, StateService],
            resolveFn: readDummyLoginEnabled,
        }
    ],
};


@NgModule({
    imports: [
        UIRouterUpgradeModule.forChild({ states: [ localLoginState ] })
    ],
    exports: [],
})
export class NgAuthenticationRoutingModule {
}




/* @ngInject */
export function readDummyLoginEnabled(applicationPropertyRS: ApplicationPropertyRS, $state: StateService): IPromise<boolean> {

    // todo ein Guard machen!!!! schauen ob es in ui-router gibt
    const log: Log = Log.createLog(readDummyLoginEnabled);

    return applicationPropertyRS.isDummyMode()
        .then((response: boolean) => {
            if (response === false) {
                log.debug('page is disabled');
                $state.go('start');
            }
            return true;
        })
        .catch(() => {
            log.error('there was an error while opening locallogin');
            $state.go('login');
            return false;
        });

}
