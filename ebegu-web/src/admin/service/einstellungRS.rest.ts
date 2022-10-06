/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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
import {forkJoin, Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {CONSTANTS} from '../../app/core/constants/CONSTANTS';
import {TSEinstellungKey} from '../../models/enums/TSEinstellungKey';
import {TSFerienbetreuungAngabenContainer} from '../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSEinstellung} from '../../models/TSEinstellung';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';

@Injectable({
    providedIn: 'root'
})
export class EinstellungRS {

    public serviceURL: string;
    public readonly ebeguRestUtil: EbeguRestUtil = new EbeguRestUtil();

    private readonly _einstellungenCacheMap = new Map<string, TSEinstellung[]>();

    public constructor(
        public readonly http: HttpClient
    ) {
        this.serviceURL = `${CONSTANTS.REST_API}einstellung`;
    }

    public saveEinstellung(tsEinstellung: TSEinstellung): Observable<TSEinstellung> {
        let restEinstellung = {};
        restEinstellung = this.ebeguRestUtil.einstellungToRestObject(restEinstellung, tsEinstellung);
        return this.http.put(this.serviceURL, restEinstellung)
            .pipe(map((response: any) => this.ebeguRestUtil.parseEinstellung(new TSEinstellung(), response)));
    }

    public findEinstellung(
        key: TSEinstellungKey,
        gemeindeId: string,
        gesuchsperiodeId: string
    ): Observable<TSEinstellung> {
        return this.http.get<TSEinstellung>(`${this.serviceURL}/key/${key}/gemeinde/${gemeindeId}/gp/${gesuchsperiodeId}`);
    }

    public findEinstellungByKey(key: TSEinstellungKey): Observable<TSEinstellung[]> {
        return this.http.get(`${this.serviceURL}/key/${key}`)
            .pipe(map((param: any) => this.ebeguRestUtil.parseEinstellungList(param)));
    }

    public getAllEinstellungenActiveForMandantBySystem(gesuchsperiodeId: string): Observable<TSEinstellung[]> {
        return this.http.get(`${this.serviceURL}/mandant/gesuchsperiode/${gesuchsperiodeId}`)
            .pipe(map((response: any) => this.ebeguRestUtil.parseEinstellungList(response)));
    }

    public getAllEinstellungenBySystemCached(gesuchsperiodeId: string): Observable<TSEinstellung[]> {
        if (this._einstellungenCacheMap.has(gesuchsperiodeId)) {
            return of(this._einstellungenCacheMap.get(gesuchsperiodeId));
        }

        return this.getAllEinstellungenBySystem(gesuchsperiodeId)
            .pipe(map(result => {
                this._einstellungenCacheMap.set(gesuchsperiodeId, result);
                return this._einstellungenCacheMap.get(gesuchsperiodeId);
            }));
    }

    public getPauschalbetraegeFerienbetreuung(container: TSFerienbetreuungAngabenContainer):
        Observable<[number, number]> {
        const findPauschale$ = this.findEinstellung(
            TSEinstellungKey.FERIENBETREUUNG_CHF_PAUSCHALBETRAG,
            container.gemeinde.id,
            container.gesuchsperiode.id
        );
        const findPauschaleSonderschueler$ = this.findEinstellung(
            TSEinstellungKey.FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER,
            container.gemeinde.id,
            container.gesuchsperiode.id
        );
        return forkJoin([
            findPauschale$,
            findPauschaleSonderschueler$
        ]).pipe(map(([e1, e2]) => [
                parseFloat(e1.value),
                parseFloat(e2.value)
            ]));
    }
}
