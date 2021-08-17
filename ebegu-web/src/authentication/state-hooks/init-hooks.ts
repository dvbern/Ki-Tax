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

import {NgModuleRef} from '@angular/core';
import {TransitionService} from '@uirouter/angular';
import {AppModule} from '../../app/app.module';
import {MandantService} from '../../app/shared/services/mandant.service';
import {AuthServiceRS} from '../service/AuthServiceRS.rest';
import {authenticationHookRunBlockX} from './onBefore/authenticationX.hook';
import {authorisationHookRunBlockX} from './onBefore/authorisationX.hook';
import {debugHookRunBlock} from './onBefore/debug.hook';
import {mandantCheckX} from './onBefore/mandantX.hook';

export function initHooks(platformRef: NgModuleRef<AppModule>): void {

    authenticationHookRunBlockX(
        platformRef.injector.get<TransitionService>(TransitionService),
        platformRef.injector.get<AuthServiceRS>(AuthServiceRS)
    );
    authorisationHookRunBlockX(
        platformRef.injector.get<TransitionService>(TransitionService),
        platformRef.injector.get<AuthServiceRS>(AuthServiceRS)
    );
    debugHookRunBlock(
        platformRef.injector.get<TransitionService>(TransitionService)
    );
    mandantCheckX(
        platformRef.injector.get<TransitionService>(TransitionService),
        platformRef.injector.get<MandantService>(MandantService),
    );
}
