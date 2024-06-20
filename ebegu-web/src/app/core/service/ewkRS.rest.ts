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

import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSEWKPerson} from '../../../models/TSEWKPerson';
import {TSEWKResultat} from '../../../models/TSEWKResultat';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../constants/CONSTANTS';
import {LogFactory} from '../logging/LogFactory';

@Injectable({
    providedIn: 'root'
})
export class EwkRS {
    private readonly log = LogFactory.createLog('EwkRS');

    public serviceURL: string;

    public constructor(
        private readonly http: HttpClient,
        private readonly ebeguRestUtil: EbeguRestUtil
    ) {
        this.serviceURL = `${CONSTANTS.REST_API}gesuche`;
    }

    public sucheInEwk(gesuchId: string): Observable<TSEWKPerson[]> {
        return this.http
            .get(`${this.serviceURL}/ewk/searchgesuch/${gesuchId}`)
            .pipe(map(response => this.handlePersonSucheResult(response)));
    }

    private handlePersonSucheResult(response: any): TSEWKPerson[] {
        this.log.debug('PARSING ewkResultat REST object ', response);
        return this.ebeguRestUtil.parseEWKResultat(
            new TSEWKResultat(),
            response
        ).personen;
    }
}
