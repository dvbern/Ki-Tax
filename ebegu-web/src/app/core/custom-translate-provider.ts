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

import {IHttpService, IQService} from 'angular';
import {filter} from 'rxjs/operators';
import {EbeguUtil} from '../../utils/EbeguUtil';
import {MandantService} from '../shared/services/mandant.service';

export function customTranslateLoader(
    $http: IHttpService,
    mandantService: MandantService,
    $q: IQService,
): (options: any) => Promise<object> {
    return (options: any) => {
        const defered = $q.defer();
        mandantService.mandant$.pipe(filter(mandant => EbeguUtil.isNotNullOrUndefined(mandant)),
        ).subscribe(mandant => {
            Promise.all(
                [
                    $http.get(`./assets/translations/translations_${options.key}.json?t=${Date.now()}`),
                    $http.get(`./assets/translations/translations_${mandant}_${options.key}.json?t=${Date.now()}`),
                ],
            )
                .then(loadadResorces => loadadResorces.map(resource => resource.data as object))
                .then(loadedResources => {
                    return loadedResources.reduce((defaultResource, resource) => {
                        return {...defaultResource, ...resource};
                    });
                }).then(defered.resolve);
        });

        return defered.promise as Promise<object>;
    };
}
