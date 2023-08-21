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

import {IHttpResponse, IHttpService, IPromise, IQService} from 'angular';
import {filter} from 'rxjs/operators';
import {EbeguUtil} from '../../utils/EbeguUtil';
import {MandantService} from '../shared/services/mandant.service';
import {MANDANTS} from './constants/MANDANTS';
import {LogFactory} from './logging/LogFactory';

const LOG = LogFactory.createLog('customTranslateLoader');

export function customTranslateLoader(
    $http: IHttpService,
    mandantService: MandantService,
    $q: IQService
): (options: any) => Promise<object> {
    return options => {
        const defered = $q.defer();
        mandantService.mandant$.pipe(filter(mandant => EbeguUtil.isNotNullOrUndefined(mandant))
        ).subscribe(mandant => {
            let translationFiles: IPromise<IHttpResponse<object>[]>;
            if (mandant === MANDANTS.NONE || mandant === MANDANTS.BERN) {
                translationFiles = Promise.all([
                    $http.get<object>(`./assets/translations/translations_${options.key}.json?t=${Date.now()}`)
                ]);
            } else {
                translationFiles = Promise.all(
                    [
                        $http.get<object>(`./assets/translations/translations_${options.key}.json?t=${Date.now()}`),
                        $http.get<object>(
                            `./assets/translations/translations_${mandant.hostname}_${options.key}.json?t=${Date.now()}`
                        )
                    ]
                );
            }

            translationFiles.then(loadadResorces => loadadResorces.map(resource => resource.data))
                .then(loadedResources => loadedResources.reduce(
                    (defaultResource, resource) => ({...defaultResource, ...resource})))
                .then(merged => defered.resolve(merged));
        }, err => LOG.error(err));

        return defered.promise as Promise<object>;
    };
}
