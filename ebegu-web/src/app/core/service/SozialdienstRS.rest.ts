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
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSSozialdienstStammdaten} from '../../../models/sozialdienst/TSSozaildienstStammdaten';
import {TSSozialdienst} from '../../../models/sozialdienst/TSSozialdienst';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../constants/CONSTANTS';
import {LogFactory} from '../logging/LogFactory';

const LOG = LogFactory.createLog('SozialdienstRS');

@Injectable({
    providedIn: 'root',
})
export class SozialdienstRS {

    public readonly serviceURL: string = `${CONSTANTS.REST_API}sozialdienst`;
    private readonly ebeguRestUtil = new EbeguRestUtil();

    public constructor(
        public readonly $http: HttpClient,
    ) {
    }

    public getServiceName(): string {
        return 'SozialdienstRS';
    }

    public createSozialdienst(sozialdienst: TSSozialdienst, email: string): Observable<TSSozialdienst> {

        const restSozialdienst = this.ebeguRestUtil.sozialdienstToRestObject({}, sozialdienst);

        return this.$http.post(this.serviceURL, restSozialdienst,
            {
                params: {
                    adminMail: email,
                },
            })
            .pipe(
                map(response => {
                    LOG.debug('PARSING sozialdienst REST object ', response);
                    return this.ebeguRestUtil.parseSozialdienst(new TSSozialdienst(), response);
                }),
            );
    }

    public getSozialdienstList(): Observable<TSSozialdienst[]> {
        return this.$http.get<any[]>(this.serviceURL).pipe(map(response => {
            LOG.debug('PARSING Sozialdienst REST array object', response);
            return this.ebeguRestUtil.parseSozialdienstList(response);
        }));
    }

    public getSozialdienstStammdaten(sozialdienstId: string): Observable<TSSozialdienstStammdaten> {
        return this.$http.get<any[]>(`${this.serviceURL}/stammdaten/${encodeURIComponent(sozialdienstId)}`)
            .pipe(map(response => {
                LOG.debug('PARSING Sozialdienst Stammdaten REST object', response);
                return this.ebeguRestUtil.parseSozialdienstStammdaten(new TSSozialdienstStammdaten(), response);
            }));
    }
}
