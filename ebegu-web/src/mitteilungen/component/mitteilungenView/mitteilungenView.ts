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
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {IMitteilungenStateParams} from '../../mitteilungen.route';

const template = require('./mitteilungenView.html');
require('./mitteilungenView.less');

export class MitteilungenViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = MitteilungenViewController;
    controllerAs = 'vm';
}

export class MitteilungenViewController {

    static $inject: string[] = ['$state', '$stateParams', 'AuthServiceRS', '$timeout'];

    form: IFormController;
    dossierId: string;
    TSRoleUtil = TSRoleUtil;

    /* @ngInject */
    constructor(private readonly $state: StateService, private readonly $stateParams: IMitteilungenStateParams,
                private readonly authServiceRS: AuthServiceRS, private readonly $timeout: ITimeoutService) {
    }

    $onInit() {
        if (this.$stateParams.dossierId) {
            this.dossierId = this.$stateParams.dossierId;
        }
    }

    public cancel(): void {
        if (this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getGesuchstellerOnlyRoles())) {
            this.$state.go('gesuchstellerDashboard');
        } else {
            this.$state.go('posteingang');
        }
    }

    $postLink() {
        this.$timeout(() => {
            EbeguUtil.selectFirst();
        }, 500); // this is the only way because it needs a little until everything is loaded
    }
}
