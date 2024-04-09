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
import {CONSTANTS} from '../constants/CONSTANTS';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSUebersichtVersendeteMails} from '../../../models/TSUebersichtVersendeteMails';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

@Injectable({
    providedIn: 'root'
})
export class UebersichtVersendeteMailsRS {
    public readonly serviceURL = `${CONSTANTS.REST_API}uebersichtVersendeteMails`;
    private readonly ebeguRestUtil = new EbeguRestUtil();
    public constructor(
        public http: HttpClient
    ) {
    }
    public getAllMails(): Observable<TSUebersichtVersendeteMails[]> {
        return this.getInfo(`${this.serviceURL}/allMails`);
    }
    private getInfo(url: string): Observable<Array<TSUebersichtVersendeteMails>> {
        return this.http.get(url)
            .pipe(map((response: any) => this.ebeguRestUtil.parseTSUebersichtVersendeteMailsList(response)));
    }
}
