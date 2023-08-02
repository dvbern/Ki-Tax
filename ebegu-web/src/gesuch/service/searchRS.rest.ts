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
import {CONSTANTS} from '../../app/core/constants/CONSTANTS';
import {TSAntragDTO} from '../../models/TSAntragDTO';
import {TSAntragSearchresultDTO} from '../../models/TSAntragSearchresultDTO';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';

@Injectable({
    providedIn: 'root'
})
export class SearchRS {

    private readonly serviceURL = `${CONSTANTS.REST_API}search`;
    private readonly ebeguRestUtil = new EbeguRestUtil();

    public constructor(
        public http: HttpClient
    ) {}

    public searchAntraege(antragSearch: any): Observable<TSAntragSearchresultDTO> {
        return this.http.post(`${this.serviceURL}/search/`, antragSearch)
            .pipe(map(response => this.toAntragSearchresult(response)));
    }

    public searchAntraegeInAlleFaelleView(antragSearch: any): Observable<TSAntragSearchresultDTO> {
        return this.http.post(`${this.serviceURL}/alleFaelleView/search`, antragSearch)
            .pipe(map(response => this.toAntragSearchresult(response)));
    }

    public countAntraege(antragSearch: any): Observable<number> {
        return this.http.post<number>(`${this.serviceURL}/search/count`, antragSearch);
    }

    public getPendenzenList(antragSearch: any): Observable<TSAntragSearchresultDTO> {
        return this.http.post(`${this.serviceURL}/jugendamt/`, antragSearch)
            .pipe(map(response => this.toAntragSearchresult(response)));
    }

    public countPendenzenList(antragSearch: any): Observable<number> {
        return this.http.post<number>(`${this.serviceURL}/jugendamt/count`, antragSearch);
    }

    public recreateAlleFaelleView(): Observable<any> {
        return this.http.get(`${this.serviceURL}/rebuild`, {
            responseType: 'text'
        });
    }

    private toAntragSearchresult(response: any): TSAntragSearchresultDTO {
        const tsAntragDTOS = this.ebeguRestUtil.parseAntragDTOs(response.antragDTOs);

        return new TSAntragSearchresultDTO(tsAntragDTOS);
    }

    public getAntraegeOfDossier(dossierId: string): Observable<Array<TSAntragDTO>> {
        return this.http.get(`${this.serviceURL}/gesuchsteller/${encodeURIComponent(dossierId)}`)
            .pipe(map((response: any) => this.ebeguRestUtil.parseAntragDTOs(response)));
    }
}
