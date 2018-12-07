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
import {take, takeUntil} from 'rxjs/operators';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import {BUILDTSTAMP, VERSION} from '../../../../environments/version';
import {TSLanguage} from '../../../../models/enums/TSLanguage';
import {TSSprache} from '../../../../models/enums/TSSprache';
import TSBenutzer from '../../../../models/TSBenutzer';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {I18nServiceRSRest} from '../../../i18n/services/i18nServiceRS.rest';
import {LogFactory} from '../../logging/LogFactory';
import ITranslateService = angular.translate.ITranslateService;

export class DvPulldownUserMenuComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./dv-pulldown-user-menu.html');
    public controller = DvPulldownUserMenuController;
    public controllerAs = 'vm';
}

const LOG = LogFactory.createLog('DvPulldownUserMenuController');

export class DvPulldownUserMenuController implements IController {

    public static $inject: ReadonlyArray<string> = [
        '$state',
        'AuthServiceRS',
        '$translate',
        'I18nServiceRSRest',
    ];

    private readonly unsubscribe$ = new Subject<void>();
    public readonly TSRoleUtil = TSRoleUtil;
    public principal?: TSBenutzer = undefined;

    public readonly VERSION = VERSION;
    public readonly BUILDTSTAMP = BUILDTSTAMP;

    public constructor(
        private readonly $state: StateService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly $translate: ITranslateService,
        private readonly i18nServiceRS: I18nServiceRSRest,
    ) {
    }

    public $onInit(): void {
        this.authServiceRS.principal$
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                principal => this.principal = principal,
                err => LOG.error(err)
            );
    }

    public $onDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public logout(): void {
        this.$state.go('authentication.login', {type: 'logout'});
    }

    public changeLanguage(language: TSSprache): void {
        let selectedLanguage = TSLanguage.DE;
        switch (language) {
            case TSSprache.DEUTSCH:
                selectedLanguage = TSLanguage.DE;
                break;
            case TSSprache.FRANZOESISCH:
                selectedLanguage = TSLanguage.FR;
                break;
            default:
                selectedLanguage = TSLanguage.DE;
                break;
        }

        this.i18nServiceRS.changeServerLanguage(selectedLanguage)
            .pipe(take(1))
            .subscribe(
                () => {
                    this.i18nServiceRS.changeClientLanguage(selectedLanguage); // angular and localStorage
                    this.$translate.use(selectedLanguage); // angularjs
                    LOG.info('language changed', selectedLanguage);
                },
            );
    }
}
