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
import TSBenutzer from '../../../../models/TSBenutzer';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {LogFactory} from '../../logging/LogFactory';

export class DVLoginButtonConfig implements IComponentOptions {
    public transclude = true;
    public template = require('./dv-login-button.html');
    public controller = DVLoginButtonController;
    public controllerAs = 'vm';
}

const LOG = LogFactory.createLog('DvLoginButtonController');

export class DVLoginButtonController implements IController {

    public static $inject: ReadonlyArray<string> = ['$state', 'AuthServiceRS'];

    private readonly unsubscribe$ = new Subject<void>();
    public readonly TSRoleUtil = TSRoleUtil;
    public principal?: TSBenutzer = undefined;

    public constructor(
        private readonly $state: StateService,
        private readonly authServiceRS: AuthServiceRS,
    ) {
    }

    public $onInit(): void {
        this.authServiceRS.principal$
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(principal => {
                    this.principal = principal;
                },
                err => LOG.error(err));
    }
}
