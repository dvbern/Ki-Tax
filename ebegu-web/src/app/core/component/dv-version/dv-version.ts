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

import {IComponentOptions, IController, IRootScopeService} from 'angular';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {BUILDTSTAMP, VERSION} from '../../../../environments/version';
import {DateUtil} from '../../../../utils/DateUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {TSVersionCheckEvent} from '../../events/TSVersionCheckEvent';
import {LogFactory} from '../../logging/LogFactory';
import {ApplicationPropertyRS} from '../../rest-services/applicationPropertyRS.rest';
import {HttpVersionInterceptor} from '../../service/version/HttpVersionInterceptor';
import {VersionService} from '../../service/version/version.service';
import IWindowService = angular.IWindowService;
import ITranslateService = angular.translate.ITranslateService;

const LOG = LogFactory.createLog('DVVersionController');

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
        'AuthServiceRS',
        'VersionService',
    ];

    public backendVersion: string;
    public readonly buildTime: string = BUILDTSTAMP;
    public readonly frontendVersion: string = VERSION;
    public showSingleVersion: boolean = true;
    public showBlog: boolean = false;
    public currentYear: number;
    public currentNode: string;

    // We have two angular versions which both have an interceptor for a version mismatch, but we only want to
    // notify the users once, therefore we track here whether we already displayed a mismatch
    private alreadyHandledVersionMismatchByAnyAngular = false;

    public constructor(
            private readonly $rootScope: IRootScopeService,
            private readonly httpVersionInterceptor: HttpVersionInterceptor,
            private readonly $window: IWindowService,
            private readonly applicationPropertyRS: ApplicationPropertyRS,
            private readonly $translate: ITranslateService,
            private readonly authServiceRS: AuthServiceRS,
            private readonly versionService: VersionService,
    ) {

    }

    public $onInit(): void {
        // AngularJS Version Mismatch
        this.backendVersion = this.httpVersionInterceptor.backendVersion;
        this.$rootScope.$on(TSVersionCheckEvent[TSVersionCheckEvent.VERSION_MISMATCH], () => {
            this.httpVersionInterceptor.eventCaptured = true;
            this.saveVersionAndHandleMismatch(this.httpVersionInterceptor.backendVersion);
        });
        // Anular X Version Mismatch
        this.versionService.$backendVersionChange.subscribe(version => {
            this.backendVersion = version;
        }, error => LOG.error(error));
        this.versionService.$versionMismatch.subscribe(backendVersion => {
            this.saveVersionAndHandleMismatch(backendVersion);
            this.versionService.versionMismatchHandled();
        }, error => LOG.error(error));

        this.currentYear = DateUtil.currentYear();

        // we use this as a healthcheck after we register the listener for VERSION_MISMATCH
        this.applicationPropertyRS.getBackgroundColorFromServer();
        this.applicationPropertyRS.getPublicPropertiesCached()
                .then(value => this.currentNode = value.currentNode);
        // Den Blog für Gesuchsteller nicht anzeigen (Wird nur bei Reload angepasst,
        // sollte aber für unsere Zwecke genügen)
        this.showBlog = this.authServiceRS.isOneOfRoles(TSRoleUtil.getAllRolesButGesuchsteller());
    }

    private saveVersionAndHandleMismatch(backendVersion: string): void {
        this.backendVersion = backendVersion;
        if (this.alreadyHandledVersionMismatchByAnyAngular) {
            return;
        }
        this.updateDisplayVersion();
        const msg = this.$translate.instant(
                'VERSION_ERROR_TEXT',
                {
                    frontendVersion: this.frontendVersion,
                    backendVersion: this.backendVersion,
                });
        this.$window.alert(msg);
        this.alreadyHandledVersionMismatchByAnyAngular = true;
    }

    private updateDisplayVersion(): void {
        this.showSingleVersion = this.frontendVersion === this.backendVersion || this.backendVersion === null;
    }
}
