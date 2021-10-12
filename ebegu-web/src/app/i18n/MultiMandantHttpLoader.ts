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

import {HttpClient} from '@angular/common/http';
import {TranslateLoader} from '@ngx-translate/core';
import {forkJoin, iif, Observable, of} from 'rxjs';
import {catchError, map, mergeMap} from 'rxjs/operators';
import {KiBonMandant} from '../core/constants/MANDANTS';
import {MandantService} from '../shared/services/mandant.service';

interface Resource {
    prefix: string;
    suffix: string;
}

export class MultiMandantHttpLoader implements TranslateLoader {

    private readonly DEFAULT_RESOURCE: Resource = {
        prefix: './assets/translations/translations_',
        suffix: `.json?t=${Date.now()}`,
    };
    private readonly MANDANT_RESOURCE: Resource = {
        prefix: './assets/translations/translations_',
        suffix: `.json?t=${Date.now()}`,
    };

    public constructor(
        private readonly http: HttpClient,
        private readonly mandantService: MandantService,
    ) {
    }

    public getTranslation(lang: string): Observable<any> {
        return this.mandantService.mandant$.pipe(
            mergeMap(mandant => iif(() => mandant !== KiBonMandant.NONE,
                this.createMultimandantRequests(lang, mandant),
                this.createBaseTranslationRequest(lang)),
            ),
        );
    }

    private createMultimandantRequests(lang: string, mandant: KiBonMandant): Observable<any> {
        return forkJoin([
                this.createBaseTranslationRequest(lang),
                this.http.get(`${this.MANDANT_RESOURCE.prefix}${mandant}_${lang}${this.MANDANT_RESOURCE.suffix}`)
                    .pipe(catchError(err => {
                        console.error(err);
                        return of({});
                    })),
            ],
        ).pipe(
            map(loadedResources => {
                return loadedResources.reduce((defaultResource, resource) => {
                    return {...defaultResource, ...resource};
                });
            }),
        );
    }

    private createBaseTranslationRequest(lang: string): Observable<any> {
        return this.http.get(`${this.DEFAULT_RESOURCE.prefix}${lang}${this.DEFAULT_RESOURCE.suffix}`);
    }
}
