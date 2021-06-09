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

import {IHttpService, ILogService, IPromise} from 'angular';
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

export class ZahlungRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log'];
    public serviceURL: string;

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        private readonly $log: ILogService,
    ) {
        this.serviceURL = `${REST_API}zahlungen`;
    }

    public getServiceName(): string {
        return 'ZahlungRS';
    }

    public getAllZahlungsauftraege(): IPromise<TSZahlungsauftrag[]> {
        return this.http.get(`${this.serviceURL}/all`).then((response: any) => {
            return this.ebeguRestUtil.parseZahlungsauftragList(response.data);
        });
    }

    public getAllZahlungsauftraegeInstitution(): IPromise<TSZahlungsauftrag[]> {
        return this.http.get(`${this.serviceURL}/institution`).then((response: any) => {
            return this.ebeguRestUtil.parseZahlungsauftragList(response.data);
        });
    }

    public getZahlungsauftrag(zahlungsauftragId: string): IPromise<TSZahlungsauftrag> {
        return this.http.get(`${this.serviceURL}/zahlungsauftrag/${encodeURIComponent(zahlungsauftragId)}`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseZahlungsauftrag(new TSZahlungsauftrag(), response.data);
            });
    }

    public getZahlungsauftragInstitution(zahlungsauftragId: string): IPromise<TSZahlungsauftrag> {
        return this.http.get(`${this.serviceURL}/zahlungsauftraginstitution/${encodeURIComponent(
            zahlungsauftragId)}`).then((response: any) => {
            return this.ebeguRestUtil.parseZahlungsauftrag(new TSZahlungsauftrag(), response.data);
        });
    }

    public zahlungsauftragAusloesen(zahlungsauftragId: string): IPromise<TSZahlungsauftrag> {
        return this.http.put(`${this.serviceURL}/ausloesen/${encodeURIComponent(zahlungsauftragId)}`,
            null).then((response: any) => {
            this.$log.debug('PARSING user REST array object', response.data);
            return this.ebeguRestUtil.parseZahlungsauftrag(new TSZahlungsauftrag(), response.data);
        });
    }

    public zahlungBestaetigen(zahlungId: string): IPromise<TSZahlung> {
        return this.http.put(`${this.serviceURL}/bestaetigen/${encodeURIComponent(zahlungId)}`,
            null).then((response: any) => {
            return this.ebeguRestUtil.parseZahlung(new TSZahlung(), response.data);
        });
    }

    public createZahlungsauftrag(
        zahlungslaufTyp: TSZahlungslaufTyp,
        gemeinde: TSGemeinde,
        beschrieb: string,
        faelligkeitsdatum: moment.Moment,
        datumGeneriert: moment.Moment,
    ): IPromise<TSZahlungsauftrag> {
        return this.http.get(`${this.serviceURL}/create`,
            {
                params: {
                    zahlungslaufTyp: zahlungslaufTyp.toString(),
                    gemeindeId: gemeinde.id,
                    faelligkeitsdatum: DateUtil.momentToLocalDate(faelligkeitsdatum),
                    beschrieb,
                    datumGeneriert: DateUtil.momentToLocalDate(datumGeneriert),
                },
            }).then((httpresponse: any) => {
            return this.ebeguRestUtil.parseZahlungsauftrag(new TSZahlungsauftrag(), httpresponse.data);
        });
    }

    public updateZahlungsauftrag(
        beschrieb: string,
        faelligkeitsdatum: moment.Moment,
        id: string,
    ): IPromise<TSZahlungsauftrag> {
        return this.http.get(`${this.serviceURL}/update`,
            {
                params: {
                    beschrieb,
                    faelligkeitsdatum: DateUtil.momentToLocalDate(faelligkeitsdatum),
                    id,
                },
            }).then((httpresponse: any) => {
            return this.ebeguRestUtil.parseZahlungsauftrag(new TSZahlungsauftrag(), httpresponse.data);
        });
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
                return from(this.getAllZahlungsauftraegeInstitution())
                    .pipe(map(a => angular.copy(a)));
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
