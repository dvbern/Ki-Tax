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

import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {MatSort} from '@angular/material/sort';
import * as moment from 'moment';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSRole} from '../../../models/enums/TSRole';
import {TSZahlungslaufTyp} from '../../../models/enums/TSZahlungslaufTyp';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSPaginationResultDTO} from '../../../models/TSPaginationResultDTO';
import {TSZahlung} from '../../../models/TSZahlung';
import {TSZahlungsauftrag} from '../../../models/TSZahlungsauftrag';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {LogFactory} from '../../core/logging/LogFactory';

const LOG = LogFactory.createLog('ZahlungRS');

@Injectable({
    providedIn: 'root',
})
export class ZahlungRS {

    private readonly serviceURL = `${CONSTANTS.REST_API}zahlungen`;
    private readonly ebeguRestUtil = new EbeguRestUtil();

    public constructor(
        private readonly http: HttpClient,
    ) {
    }

    private static getSearchParams(
        sort: MatSort | undefined,
        page: number,
        pageSize: number,
        filterGemeinde: TSGemeinde | undefined,
        zahlungslaufTyp: TSZahlungslaufTyp): HttpParams {

        let searchParams: HttpParams = new HttpParams();

        if (sort) {
            searchParams = searchParams.append('sortPredicate', sort.active.toString());
            searchParams = searchParams.append('sortReverse', String(sort.direction === 'desc'));
        }
        if (EbeguUtil.isNullOrUndefined(page) || !pageSize) {
            throw Error('page or pageSize not set');
        }
        searchParams = searchParams.append('page', page.toFixed(0));
        searchParams = searchParams.append('pageSize', pageSize.toFixed(0));
        if (filterGemeinde) {
            searchParams = searchParams.append('gemeinde', filterGemeinde.id);
        }
        if (!zahlungslaufTyp) {
            throw Error('zahlungslauftyp not set');
        }
        searchParams = searchParams.append('zahlungslaufTyp', zahlungslaufTyp);

        return searchParams;
    }

    public getServiceName(): string {
        return 'ZahlungRS';
    }

    public getAllZahlungsauftraege(searchParams: HttpParams):
        Observable<TSPaginationResultDTO<TSZahlungsauftrag>> {
        return this.http.get(`${this.serviceURL}/all`, {
            params: searchParams
        }).pipe(
            map(response => this.parseZahlungenResultDTO(response)),
        );
    }

    public getAllZahlungsauftraegeInstitution(searchParams: HttpParams): Observable<TSPaginationResultDTO<TSZahlungsauftrag>> {
        return this.http.get(`${this.serviceURL}/institution`, {
            params: searchParams
        }).pipe(
            map(response => this.parseZahlungenResultDTO(response)),
        );
    }

    private parseZahlungenResultDTO(response: any): TSPaginationResultDTO<TSZahlungsauftrag> {
        const dto = new TSPaginationResultDTO<TSZahlungsauftrag>();
        dto.resultList = this.ebeguRestUtil.parseZahlungsauftragList(response.resultList);
        dto.totalResultSize = response.totalCount;
        return dto;
    }

    public getZahlungsauftrag(zahlungsauftragId: string): Observable<TSZahlungsauftrag> {
        return this.http.get(`${this.serviceURL}/zahlungsauftrag/${encodeURIComponent(zahlungsauftragId)}`)
            .pipe(
                map((response: any) => {
                    return this.ebeguRestUtil.parseZahlungsauftrag(new TSZahlungsauftrag(), response);
                }),
            );
    }

    public getZahlungsauftragInstitution(zahlungsauftragId: string): Observable<TSZahlungsauftrag> {
        return this.http.get(`${this.serviceURL}/zahlungsauftraginstitution/${encodeURIComponent(
            zahlungsauftragId)}`)
            .pipe(
                map((response: any) => {
                    return this.ebeguRestUtil.parseZahlungsauftrag(new TSZahlungsauftrag(), response);
                }),
            );
    }

    public zahlungsauftragAusloesen(zahlungsauftragId: string): Observable<TSZahlungsauftrag> {
        return this.http.put(`${this.serviceURL}/ausloesen/${encodeURIComponent(zahlungsauftragId)}`,
            null)
            .pipe(
                map((response: any) => {
                    LOG.debug('PARSING user REST array object', response);
                    return this.ebeguRestUtil.parseZahlungsauftrag(new TSZahlungsauftrag(), response);
                }),
            );
    }

    public zahlungBestaetigen(zahlungId: string): Observable<TSZahlung> {
        return this.http.put(`${this.serviceURL}/bestaetigen/${encodeURIComponent(zahlungId)}`, null)
            .pipe(
                map((response: any) => {
                    return this.ebeguRestUtil.parseZahlung(new TSZahlung(), response);
                }),
            );
    }

    public createZahlungsauftrag(
        zahlungslaufTyp: TSZahlungslaufTyp,
        gemeinde: TSGemeinde,
        beschrieb: string,
        faelligkeitsdatum: moment.Moment,
        datumGeneriert: moment.Moment,
    ): Observable<TSZahlungsauftrag> {
        return this.http.get(`${this.serviceURL}/create`,
            {
                params: {
                    zahlungslaufTyp: zahlungslaufTyp.toString(),
                    gemeindeId: gemeinde.id,
                    faelligkeitsdatum: DateUtil.momentToLocalDate(faelligkeitsdatum),
                    beschrieb,
                    datumGeneriert: DateUtil.momentToLocalDate(datumGeneriert),
                },
            },
        ).pipe(
            map((httpresponse: any) => {
                return this.ebeguRestUtil.parseZahlungsauftrag(new TSZahlungsauftrag(), httpresponse);
            }),
        );
    }

    public updateZahlungsauftrag(
        beschrieb: string,
        faelligkeitsdatum: moment.Moment,
        id: string,
    ): Observable<TSZahlungsauftrag> {
        return this.http.get(`${this.serviceURL}/update`,
            {
                params: {
                    beschrieb,
                    faelligkeitsdatum: DateUtil.momentToLocalDate(faelligkeitsdatum),
                    id,
                },
            },
        ).pipe(
            map((httpresponse: any) => {
                return this.ebeguRestUtil.parseZahlungsauftrag(new TSZahlungsauftrag(), httpresponse);
            }),
        );
    }

    public getZahlungsauftragForRole$(role: TSRole, zahlungsauftragId: string): Observable<TSZahlungsauftrag | null> {
        switch (role) {
            case TSRole.ADMIN_INSTITUTION:
            case TSRole.SACHBEARBEITER_INSTITUTION:
            case TSRole.ADMIN_TRAEGERSCHAFT:
            case TSRole.SACHBEARBEITER_TRAEGERSCHAFT:
                return this.getZahlungsauftragInstitution(zahlungsauftragId);
            case TSRole.SUPER_ADMIN:
            case TSRole.ADMIN_BG:
            case TSRole.SACHBEARBEITER_BG:
            case TSRole.ADMIN_GEMEINDE:
            case TSRole.SACHBEARBEITER_GEMEINDE:
            case TSRole.JURIST:
            case TSRole.REVISOR:
            case TSRole.ADMIN_MANDANT:
            case TSRole.SACHBEARBEITER_MANDANT:
                return this.getZahlungsauftrag(zahlungsauftragId);
            default:
                return of(null);
        }
    }

    public getZahlungsauftraegeForRole$(
        role: TSRole,
        sort: MatSort,
        page: number,
        pageSize: number,
        filterGemeinde: TSGemeinde,
        zahlungslaufTyp: TSZahlungslaufTyp
    ): Observable<TSPaginationResultDTO<TSZahlungsauftrag>> {
        const searchParams = ZahlungRS.getSearchParams(sort, page, pageSize, filterGemeinde, zahlungslaufTyp);
        switch (role) {
            case TSRole.ADMIN_INSTITUTION:
            case TSRole.SACHBEARBEITER_INSTITUTION:
            case TSRole.ADMIN_TRAEGERSCHAFT:
            case TSRole.SACHBEARBEITER_TRAEGERSCHAFT:
                return this.getAllZahlungsauftraegeInstitution(searchParams);
            case TSRole.SUPER_ADMIN:
            case TSRole.ADMIN_BG:
            case TSRole.SACHBEARBEITER_BG:
            case TSRole.ADMIN_GEMEINDE:
            case TSRole.SACHBEARBEITER_GEMEINDE:
            case TSRole.JURIST:
            case TSRole.REVISOR:
            case TSRole.ADMIN_MANDANT:
            case TSRole.SACHBEARBEITER_MANDANT:
                return this.getAllZahlungsauftraege(searchParams);
            default:
                return of(new TSPaginationResultDTO([], 0));
        }
    }
}
