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
import * as moment from 'moment';
import {from, Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSRole} from '../../../models/enums/TSRole';
import {TSZahlungslaufTyp} from '../../../models/enums/TSZahlungslaufTyp';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSZahlung} from '../../../models/TSZahlung';
import {TSZahlungsauftrag} from '../../../models/TSZahlungsauftrag';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../constants/CONSTANTS';
import {LogFactory} from '../logging/LogFactory';

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

    public getServiceName(): string {
        return 'ZahlungRS';
    }

    public getAllZahlungsauftraege(): Promise<TSZahlungsauftrag[]> {
        return this.http.get(`${this.serviceURL}/all`).pipe(
            map((response: any) => {
                return this.ebeguRestUtil.parseZahlungsauftragList(response);
            }),
        ).toPromise();
    }

    public getAllZahlungsauftraegeInstitution(): Promise<TSZahlungsauftrag[]> {
        return this.http.get(`${this.serviceURL}/institution`).pipe(
            map((response: any) => {
                return this.ebeguRestUtil.parseZahlungsauftragList(response);
            }),
        ).toPromise();
    }

    public getZahlungsauftrag(zahlungsauftragId: string): Promise<TSZahlungsauftrag> {
        return this.http.get(`${this.serviceURL}/zahlungsauftrag/${encodeURIComponent(zahlungsauftragId)}`)
            .pipe(
                map((response: any) => {
                    return this.ebeguRestUtil.parseZahlungsauftrag(new TSZahlungsauftrag(), response);
                }),
            ).toPromise();
    }

    public getZahlungsauftragInstitution(zahlungsauftragId: string): Promise<TSZahlungsauftrag> {
        return this.http.get(`${this.serviceURL}/zahlungsauftraginstitution/${encodeURIComponent(
            zahlungsauftragId)}`)
            .pipe(
                map((response: any) => {
                    return this.ebeguRestUtil.parseZahlungsauftrag(new TSZahlungsauftrag(), response);
                }),
            ).toPromise();
    }

    public zahlungsauftragAusloesen(zahlungsauftragId: string): Promise<TSZahlungsauftrag> {
        return this.http.put(`${this.serviceURL}/ausloesen/${encodeURIComponent(zahlungsauftragId)}`,
            null)
            .pipe(
                map((response: any) => {
                    LOG.debug('PARSING user REST array object', response);
                    return this.ebeguRestUtil.parseZahlungsauftrag(new TSZahlungsauftrag(), response);
                }),
            ).toPromise();
    }

    public zahlungBestaetigen(zahlungId: string): Promise<TSZahlung> {
        return this.http.put(`${this.serviceURL}/bestaetigen/${encodeURIComponent(zahlungId)}`, null)
            .pipe(
                map((response: any) => {
                    return this.ebeguRestUtil.parseZahlung(new TSZahlung(), response);
                }),
            ).toPromise();
    }

    public createZahlungsauftrag(
        zahlungslaufTyp: TSZahlungslaufTyp,
        gemeinde: TSGemeinde,
        beschrieb: string,
        faelligkeitsdatum: moment.Moment,
        datumGeneriert: moment.Moment,
    ): Promise<TSZahlungsauftrag> {
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
        ).toPromise();
    }

    public updateZahlungsauftrag(
        beschrieb: string,
        faelligkeitsdatum: moment.Moment,
        id: string,
    ): Promise<TSZahlungsauftrag> {
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
        ).toPromise();
    }

    public getZahlungsauftragForRole$(role: TSRole, zahlungsauftragId: string): Observable<TSZahlungsauftrag | null> {
        switch (role) {
            case TSRole.ADMIN_INSTITUTION:
            case TSRole.SACHBEARBEITER_INSTITUTION:
            case TSRole.ADMIN_TRAEGERSCHAFT:
            case TSRole.SACHBEARBEITER_TRAEGERSCHAFT:
                return from(this.getZahlungsauftragInstitution(zahlungsauftragId));
            case TSRole.SUPER_ADMIN:
            case TSRole.ADMIN_BG:
            case TSRole.SACHBEARBEITER_BG:
            case TSRole.ADMIN_GEMEINDE:
            case TSRole.SACHBEARBEITER_GEMEINDE:
            case TSRole.JURIST:
            case TSRole.REVISOR:
            case TSRole.ADMIN_MANDANT:
            case TSRole.SACHBEARBEITER_MANDANT:
                return from(this.getZahlungsauftrag(zahlungsauftragId));
            default:
                return of(null);
        }
    }

    public getZahlungsauftraegeForRole$(role: TSRole): Observable<TSZahlungsauftrag[]> {
        switch (role) {
            case TSRole.ADMIN_INSTITUTION:
            case TSRole.SACHBEARBEITER_INSTITUTION:
            case TSRole.ADMIN_TRAEGERSCHAFT:
            case TSRole.SACHBEARBEITER_TRAEGERSCHAFT:
                return from(this.getAllZahlungsauftraegeInstitution());
            case TSRole.SUPER_ADMIN:
            case TSRole.ADMIN_BG:
            case TSRole.SACHBEARBEITER_BG:
            case TSRole.ADMIN_GEMEINDE:
            case TSRole.SACHBEARBEITER_GEMEINDE:
            case TSRole.JURIST:
            case TSRole.REVISOR:
            case TSRole.ADMIN_MANDANT:
            case TSRole.SACHBEARBEITER_MANDANT:
                return from(this.getAllZahlungsauftraege());
            default:
                return of([]);
        }
    }
}
