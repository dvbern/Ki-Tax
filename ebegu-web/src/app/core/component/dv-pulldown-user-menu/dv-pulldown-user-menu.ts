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

import {StateService} from '@uirouter/core';
import {IComponentOptions, IController} from 'angular';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import {BUILDTSTAMP, VERSION} from '../../../../environments/version';
import TSBenutzer from '../../../../models/TSBenutzer';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';

export class DvPulldownUserMenuComponentConfig implements IComponentOptions {
    transclude = false;
    bindings = {};
    template = require('./dv-pulldown-user-menu.html');
    controller = DvPulldownUserMenuController;
    controllerAs = 'vm';
}

export class DvPulldownUserMenuController implements IController {

    static $inject: ReadonlyArray<string> = ['$state', 'AuthServiceRS'];

    private readonly unsubscribe$ = new Subject<void>();
    public readonly TSRoleUtil = TSRoleUtil;
    public principal?: TSBenutzer = undefined;

    public readonly VERSION = VERSION;
    public readonly BUILDTSTAMP = BUILDTSTAMP;

    constructor(private readonly $state: StateService,
                private readonly authServiceRS: AuthServiceRS) {
    }

    $onInit(): void {
        this.authServiceRS.principal$
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(principal => {
                this.principal = principal;
            });
    }

    $onDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public logout(): void {
        this.$state.go('authentication.login', {type: 'logout'});
    }
}
