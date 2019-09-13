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

import {IComponentOptions, IController} from 'angular';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import {BUILDTSTAMP, VERSION} from '../../../../environments/version';
import DateUtil from '../../../../utils/DateUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {TSVersionCheckEvent} from '../../events/TSVersionCheckEvent';
import {ApplicationPropertyRS} from '../../rest-services/applicationPropertyRS.rest';
import HttpVersionInterceptor from '../../service/version/HttpVersionInterceptor';
import IRootScopeService = angular.IRootScopeService;
import IWindowService = angular.IWindowService;
import ITranslateService = angular.translate.ITranslateService;

export class DVVersionComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./dv-version.html');
    public controller = DVVersionController;
    public controllerAs = 'vm';
}

export class DVVersionController implements IController {

    public static $inject = [
        '$rootScope',
        'HttpVersionInterceptor',
        '$window',
        'ApplicationPropertyRS',
        '$translate',
        'AuthServiceRS'
    ];

    public backendVersion: string;
    public readonly buildTime: string = BUILDTSTAMP;
    public readonly frontendVersion: string = VERSION;
    public showSingleVersion: boolean = true;
    public showBlog: boolean = false;
    public currentYear: number;
    public currentNode: string;

    public constructor(
        private readonly $rootScope: IRootScopeService,
        private readonly httpVersionInterceptor: HttpVersionInterceptor,
        private readonly $window: IWindowService,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly $translate: ITranslateService,
        private readonly authServiceRS: AuthServiceRS,
    ) {

    }

    public $onInit(): void {

        this.backendVersion = this.httpVersionInterceptor.backendVersion;
        this.currentYear = DateUtil.currentYear();
        this.$rootScope.$on(TSVersionCheckEvent[TSVersionCheckEvent.VERSION_MISMATCH], () => {
            this.httpVersionInterceptor.eventCaptured = true;
            this.backendVersion = this.httpVersionInterceptor.backendVersion;
            this.updateDisplayVersion();
            const msg = this.$translate.instant(
                'VERSION_ERROR_TEXT',
                {
                    frontendVersion: this.frontendVersion,
                    backendVersion: this.backendVersion,
                });
            this.$window.alert(msg);
        });

        // we use this as a healthcheck after we register the listener for VERSION_MISMATCH
        this.applicationPropertyRS.getBackgroundColorFromServer();
        this.applicationPropertyRS.getPublicPropertiesCached()
            .then(value => this.currentNode = value.currentNode);
        // Den Blog für Gesuchsteller nicht anzeigen (Wird nur bei Reload angepasst,
        // sollte aber für unsere Zwecke genügen)
        this.showBlog = this.authServiceRS.isOneOfRoles(TSRoleUtil.getAllRolesButGesuchsteller());
    }

    private updateDisplayVersion(): void {
        this.showSingleVersion = this.frontendVersion === this.backendVersion || this.backendVersion === null;
    }
}
