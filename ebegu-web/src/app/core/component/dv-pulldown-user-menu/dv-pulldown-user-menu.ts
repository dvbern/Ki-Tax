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
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {BUILDTSTAMP, VERSION} from '../../../../environments/version';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSBenutzer} from '../../../../models/TSBenutzer';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {LogFactory} from '../../logging/LogFactory';
import {ApplicationPropertyRS} from '../../rest-services/applicationPropertyRS.rest';
import {NotrechtRS} from '../../service/notrechtRS.rest';

export class DvPulldownUserMenuComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./dv-pulldown-user-menu.html');
    public controller = DvPulldownUserMenuController;
    public controllerAs = 'vm';
}

const LOG = LogFactory.createLog('DvPulldownUserMenuController');

export class DvPulldownUserMenuController implements IController {

    public static $inject: ReadonlyArray<string> = ['$state', 'AuthServiceRS', 'NotrechtRS', 'ApplicationPropertyRS'];

    private readonly unsubscribe$ = new Subject<void>();
    public readonly TSRoleUtil = TSRoleUtil;
    public principal?: TSBenutzer = undefined;
    public notrechtVisible: boolean;
    public mandantSwitchVisible: boolean;

    public readonly VERSION = VERSION;
    public readonly BUILDTSTAMP = BUILDTSTAMP;

    public constructor(
        private readonly $state: StateService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly notrechtRS: NotrechtRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS
    ) {
    }

    public $onInit(): void {
        this.authServiceRS.principal$
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                principal => this.principal = principal,
                err => LOG.error(err)
            );
        this.setNotrechtVisible();
        this.setMandantSwitchVisible();
    }

    private setNotrechtVisible(): void {
        if (this.isSuperAdmin()) {
            this.notrechtVisible = true;
            return;
        }
        if (!this.authServiceRS.isOneOfRoles(TSRoleUtil.getAllRolesForNotrecht())) {
            this.notrechtVisible = false;
            return;
        }
        this.notrechtRS.currentUserHasFormular()
            .then(result => {
                this.notrechtVisible = result;
            });
    }

    public $onDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public logout(): void {
        this.$state.go('authentication.login', {type: 'logout'});
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    private setMandantSwitchVisible(): void {
        this.applicationPropertyRS.getPublicPropertiesCached()
            .then(properties => properties.mulitmandantAktiv)
            .then(multimandantAktiv => {
                this.mandantSwitchVisible = multimandantAktiv;
            });
    }
}
