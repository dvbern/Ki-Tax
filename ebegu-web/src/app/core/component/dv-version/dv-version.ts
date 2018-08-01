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

import {IComponentOptions, ILogService, IQService} from 'angular';
import {TSVersionCheckEvent} from '../../events/TSVersionCheckEvent';
import DateUtil from '../../../../utils/DateUtil';
import HttpVersionInterceptor from '../../service/version/HttpVersionInterceptor';
import IRootScopeService = angular.IRootScopeService;
import IWindowService = angular.IWindowService;

const template = require('./dv-version.html');

export class DVVersionComponentConfig implements IComponentOptions {
    transclude = false;
    bindings: any = {};
    template = template;
    controller = DVVersionController;
    controllerAs = 'vm';
}

export class DVVersionController {

    static $inject = ['$rootScope', 'HttpVersionInterceptor', '$q', '$window', '$log'];

    TSRoleUtil: any;

    private backendVersion: string;
    private frontendVersion: string;
    private buildTime: string;
    private showSingleVersion: boolean = true;
    private currentYear: number;

    constructor(private readonly $rootScope: IRootScopeService, private readonly httpVersionInterceptor: HttpVersionInterceptor, private readonly $q: IQService,
        private readonly $window: IWindowService, private readonly $log: ILogService) {

    }

    $onInit() {

        this.backendVersion = this.httpVersionInterceptor.getBackendVersion();
        this.frontendVersion = this.httpVersionInterceptor.frontendVersion();
        this.buildTime = this.httpVersionInterceptor.getBuildTime();
        this.currentYear = DateUtil.currentYear();

        this.$rootScope.$on(TSVersionCheckEvent[TSVersionCheckEvent.VERSION_MISMATCH], () => {
            this.backendVersion = this.httpVersionInterceptor.getBackendVersion();
            this.updateDisplayVersion();
            const msg = 'Der Client (' + this.frontendVersion + ') hat eine andere Version als der Server('
                + this.backendVersion + '). Bitte laden sie die Seite komplett neu (F5)';
            this.$window.alert(msg);

        });

    }

    private updateDisplayVersion() {
        this.showSingleVersion = this.frontendVersion === this.backendVersion || this.backendVersion === null;
    }

}
