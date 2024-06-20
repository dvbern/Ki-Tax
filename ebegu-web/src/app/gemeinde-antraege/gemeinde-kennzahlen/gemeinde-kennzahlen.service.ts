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
import {Observable, Subject} from 'rxjs';
import {map, tap} from 'rxjs/operators';
import {TSGemeindeKennzahlen} from '../../../models/gemeindeantrag/gemeindekennzahlen/TSGemeindeKennzahlen';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {LogFactory} from '../../core/logging/LogFactory';

const LOG = LogFactory.createLog('GemeindeKennzahlenService');

@Injectable({
    providedIn: 'root'
})
export class GemeindeKennzahlenService {
    private readonly API_URL = `${CONSTANTS.REST_API}gemeindekennzahlen`;
    private readonly restUtil = new EbeguRestUtil();

    private readonly _gemeindeKennzahlenAntragStore$ =
        new Subject<TSGemeindeKennzahlen>();

    public constructor(private readonly http: HttpClient) {}

    public getGemeindeKennzahlenAntrag(): Observable<TSGemeindeKennzahlen> {
        return this._gemeindeKennzahlenAntragStore$.asObservable();
    }

    public updateGemeindeKennzahlenAntragStore(id: string): void {
        this.http
            .get<TSGemeindeKennzahlen>(`${this.API_URL}/${id}`)
            .pipe(
                map(antrag =>
                    this.restUtil.parseGemeindeKennzahlen(
                        new TSGemeindeKennzahlen(),
                        antrag
                    )
                )
            )
            .subscribe(
                antrag => this._gemeindeKennzahlenAntragStore$.next(antrag),
                error => LOG.error(error)
            );
    }

    public saveGemeindeKennzahlen(
        antrag: TSGemeindeKennzahlen
    ): Observable<TSGemeindeKennzahlen> {
        return this.http
            .post(
                `${this.API_URL}/${encodeURIComponent(antrag.id)}/save`,
                this.restUtil.gemeindeKennzahlenToRestObject({}, antrag)
            )
            .pipe(
                map(antragFromServer =>
                    this.restUtil.parseGemeindeKennzahlen(
                        new TSGemeindeKennzahlen(),
                        antragFromServer
                    )
                ),
                tap(parsedAntrag =>
                    this._gemeindeKennzahlenAntragStore$.next(parsedAntrag)
                )
            );
    }

    public gemeindeKennzahlenAbschliessen(
        antrag: TSGemeindeKennzahlen
    ): Observable<TSGemeindeKennzahlen> {
        return this.http
            .post(
                `${this.API_URL}/${encodeURIComponent(antrag.id)}/abschliessen`,
                this.restUtil.gemeindeKennzahlenToRestObject({}, antrag)
            )
            .pipe(
                map(antragFromServer =>
                    this.restUtil.parseGemeindeKennzahlen(
                        new TSGemeindeKennzahlen(),
                        antragFromServer
                    )
                ),
                tap(parsedAntrag =>
                    this._gemeindeKennzahlenAntragStore$.next(parsedAntrag)
                )
            );
    }

    public gemeindeKennzahlenZurueckAnGemeinde(
        antrag: TSGemeindeKennzahlen
    ): Observable<TSGemeindeKennzahlen> {
        return this.http
            .put(
                `${this.API_URL}/${encodeURIComponent(antrag.id)}/zurueck-an-gemeinde`,
                this.restUtil.gemeindeKennzahlenToRestObject({}, antrag)
            )
            .pipe(
                map(antragFromServer =>
                    this.restUtil.parseGemeindeKennzahlen(
                        new TSGemeindeKennzahlen(),
                        antragFromServer
                    )
                ),
                tap(parsedAntrag =>
                    this._gemeindeKennzahlenAntragStore$.next(parsedAntrag)
                )
            );
    }
}
