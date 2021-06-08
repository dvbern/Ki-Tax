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
import {TSBetreuungMonitoring} from '../../../models/TSBetreuungMonitoring';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../constants/CONSTANTS';
import {LogFactory} from '../logging/LogFactory';

const LOG = LogFactory.createLog('SozialdienstRS');

@Injectable({
    providedIn: 'root',
})
export class BetreuungMonitoringRS {

    public readonly serviceURL: string = `${CONSTANTS.REST_API}betreuungMonitoring`;
    private readonly ebeguRestUtil = new EbeguRestUtil();

    public constructor(
        public readonly $http: HttpClient,
    ) {
    }

    public getServiceName(): string {
        return 'BetreuungMonitoringRS';
    }

    public getBetreuungMonitoringList(): Observable<TSBetreuungMonitoring[]> {
        return this.$http.get<any[]>(this.serviceURL).pipe(map(response => {
            LOG.debug('PARSING Sozialdienst REST array object', response);
            return this.ebeguRestUtil.parseTSBetreuungMonitoringList(response);
        }));
    }
}
