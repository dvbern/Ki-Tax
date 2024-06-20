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

import IComponentOptions = angular.IComponentOptions;
import IFormController = angular.IFormController;
import ITimeoutService = angular.ITimeoutService;
import {StateService} from '@uirouter/core';
import {IController} from 'angular';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {IMitteilungenStateParams} from '../../mitteilungen.route';

export class MitteilungenViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./mitteilungenView.html');
    public controller = MitteilungenViewController;
    public controllerAs = 'vm';
}

export class MitteilungenViewController implements IController {
    public static $inject: string[] = [
        '$state',
        '$stateParams',
        'AuthServiceRS',
        '$timeout'
    ];

    public form: IFormController;
    public dossierId: string;
    public fallId: string;
    public readonly TSRoleUtil = TSRoleUtil;

    public constructor(
        private readonly $state: StateService,
        private readonly $stateParams: IMitteilungenStateParams,
        private readonly authServiceRS: AuthServiceRS,
        private readonly $timeout: ITimeoutService
    ) {}

    public $onInit(): void {
        if (this.$stateParams.dossierId) {
            this.dossierId = this.$stateParams.dossierId;
        }
        if (this.$stateParams.fallId) {
            this.fallId = this.$stateParams.fallId;
        }
    }

    public cancel(): void {
        if (
            this.authServiceRS.isOneOfRoles(
                this.TSRoleUtil.getGesuchstellerOnlyRoles()
            )
        ) {
            this.$state.go('gesuchsteller.dashboard');
        } else {
            this.$state.go('posteingang.view');
        }
    }

    public $postLink(): void {
        const delay = 500;
        this.$timeout(() => {
            EbeguUtil.selectFirst();
        }, delay); // this is the only way because it needs a little until everything is loaded
    }
}
