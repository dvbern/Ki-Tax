/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {TransitionService} from '@uirouter/angular';
import {HookResult} from '@uirouter/core';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {I18nServiceRSRest} from '../../../app/i18n/services/i18nServiceRS.rest';
import {TSBrowserLanguage} from '../../../models/enums/TSBrowserLanguage';
import ITranslateService = angular.translate.ITranslateService;

languageEnabledHookRunBlock.$inject = ['$transitions', 'ApplicationPropertyRS', 'I18nServiceRSRest', '$translate'];

export function languageEnabledHookRunBlock(
    $transitions: TransitionService,
    applicationPropertyService: ApplicationPropertyRS,
    i18nService: I18nServiceRSRest,
    translateService: ITranslateService,
): void {
    $transitions.onBefore({}, async () => changeLanguageIfNotEnabled(applicationPropertyService, i18nService, translateService));
}

async function changeLanguageIfNotEnabled(
    applicationPropertyService: ApplicationPropertyRS,
    i18nService: I18nServiceRSRest,
    translateService: ITranslateService,
): Promise<HookResult> {
    await applicationPropertyService.getFrenchEnabled().then(frenchEnabled => {
        if (!frenchEnabled && i18nService.currentLanguage() === TSBrowserLanguage.FR) {
            i18nService.changeClientLanguage(TSBrowserLanguage.DE, translateService);
        }
    });
    return true;
}
