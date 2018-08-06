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
import {IComponentOptions, IOnDestroy, IOnInit} from 'angular';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {AuthLifeCycleService} from '../../../../authentication/service/authLifeCycle.service';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import {BUILDTSTAMP, VERSION} from '../../../../environments/version';
import {TSAuthEvent} from '../../../../models/enums/TSAuthEvent';
import TSUser from '../../../../models/TSUser';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';

export class DvPulldownUserMenuComponentConfig implements IComponentOptions {
    transclude = false;
    bindings = {};
    template = require('./dv-pulldown-user-menu.html');
    controller = DvPulldownUserMenuController;
    controllerAs = 'vm';
}

export class DvPulldownUserMenuController implements IOnInit, IOnDestroy {

    static $inject: ReadonlyArray<string> = ['$state', 'AuthServiceRS', 'AuthLifeCycleService'];

    private readonly unsubscribe$ = new Subject<void>();
    TSRoleUtil = TSRoleUtil;
    principal: TSUser;

    public readonly VERSION = VERSION;
    public readonly BUILDTSTAMP = BUILDTSTAMP;

    constructor(private readonly $state: StateService,
                private readonly authServiceRS: AuthServiceRS,
                private readonly authLifeCycleService: AuthLifeCycleService) {

        this.TSRoleUtil = TSRoleUtil;
    }

    $onInit(): void {
        this.principal = this.authServiceRS.getPrincipal();
        this.authLifeCycleService.get$(TSAuthEvent.LOGIN_SUCCESS)
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(() => {
                this.principal = this.authServiceRS.getPrincipal();
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
