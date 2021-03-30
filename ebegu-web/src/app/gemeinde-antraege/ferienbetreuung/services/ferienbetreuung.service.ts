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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, ReplaySubject} from 'rxjs';
import {map, tap} from 'rxjs/operators';
import {TSFerienbetreuungAngabenAngebot} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenAngebot';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSFerienbetreuungAngabenKostenEinnahmen} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenKostenEinnahmen';
import {TSFerienbetreuungAngabenNutzung} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenNutzung';
import {TSFerienbetreuungAngabenStammdaten} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenStammdaten';
import {EbeguRestUtil} from '../../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../../../core/constants/CONSTANTS';
import {LogFactory} from '../../../core/logging/LogFactory';

const LOG = LogFactory.createLog('FerienbetreuungService');

@Injectable({
    providedIn: 'root',
})
export class FerienbetreuungService {

    private readonly API_BASE_URL = `${CONSTANTS.REST_API}ferienbetreuung`;
    private readonly ebeguRestUtil = new EbeguRestUtil();
    // return last item but don't provide initial value like BehaviourSubject does
    private ferienbetreuungAngabenContainerStore =
        new ReplaySubject<TSFerienbetreuungAngabenContainer>(1);

    public constructor(private readonly http: HttpClient) {
    }

    public updateFerienbetreuungContainerStore(id: string): void {
        const url = `${this.API_BASE_URL}/find/${encodeURIComponent(id)}`;
        this.http.get<TSFerienbetreuungAngabenContainer>(url)
            .subscribe(container => {
                this.next(container);
            }, error => LOG.error(error));
    }

    public getFerienbetreuungContainer(): Observable<TSFerienbetreuungAngabenContainer> {
        return this.ferienbetreuungAngabenContainerStore.asObservable();
    }

    public emptyStore(): void {
        this.ferienbetreuungAngabenContainerStore = new ReplaySubject<TSFerienbetreuungAngabenContainer>(1);
    }

    private next(restContainer: TSFerienbetreuungAngabenContainer): void {
        const container = this.ebeguRestUtil.parseFerienbetreuungContainer(
            new TSFerienbetreuungAngabenContainer(),
            restContainer,
        );
        this.ferienbetreuungAngabenContainerStore.next(container);
    }

    public saveKommentar(containerId: string, kommentar: string): Observable<void> {
        return this.http.put<void>(
            `${this.API_BASE_URL}/saveKommentar/${encodeURIComponent(containerId)}`,
            kommentar,
        );
    }

    public saveStammdaten(containerId: string, stammdaten: TSFerienbetreuungAngabenStammdaten):
        Observable<TSFerienbetreuungAngabenStammdaten> {
        return this.http.put<any>(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/stammdaten/save`,
            this.ebeguRestUtil.ferienbetreuungStammdatenToRestObject({}, stammdaten),
        ).pipe(map(restStammdaten => {
            return this.ebeguRestUtil.parseFerienbetreuungStammdaten(
                new TSFerienbetreuungAngabenStammdaten(),
                restStammdaten,
            );
        }));
    }

    public saveAngebot(containerId: string, angebot: TSFerienbetreuungAngabenAngebot):
        Observable<TSFerienbetreuungAngabenAngebot> {
        return this.http.put<any>(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/angebot/save`,
            this.ebeguRestUtil.ferienbetreuungAngebotToRestObject({}, angebot),
        ).pipe(map(restAngebot => this.parseRestAngebot(restAngebot)));
    }

    public saveNutzung(containerId: string, nutzung: TSFerienbetreuungAngabenNutzung):
        Observable<TSFerienbetreuungAngabenNutzung> {
        return this.http.put<any>(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/nutzung/save`,
            this.ebeguRestUtil.ferienbetreuungNutzungToRestObject({}, nutzung),
        ).pipe(map(restNutzung => this.parseRestNutzung(restNutzung)));
    }

    private parseRestNutzung(restNutzung: any): TSFerienbetreuungAngabenNutzung {
        return this.ebeguRestUtil.parseFerienbetreuungNutzung(
            new TSFerienbetreuungAngabenNutzung(),
            restNutzung,
        );
    }

    public saveKostenEinnahmen(containerId: string, kostenEinnahmen: TSFerienbetreuungAngabenKostenEinnahmen):
        Observable<TSFerienbetreuungAngabenKostenEinnahmen> {
        return this.http.put<any>(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/kostenEinnahmen/save`,
            this.ebeguRestUtil.ferienbetreuungKostenEinnahmenToRestObject({}, kostenEinnahmen),
        ).pipe(map(restKostenEinnahmen => {
            return this.ebeguRestUtil.parseFerienbetreuungKostenEinnahmen(
                new TSFerienbetreuungAngabenKostenEinnahmen(),
                restKostenEinnahmen,
            );
        }));
    }

    public angebotAbschliessen(
        containerId: string,
        angebot: TSFerienbetreuungAngabenAngebot,
    ): Observable<TSFerienbetreuungAngabenAngebot> {
        return this.http.put(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/angebot/abschliessen`,
            this.ebeguRestUtil.ferienbetreuungAngebotToRestObject({}, angebot),
        ).pipe(
            map(restAngebot => this.parseRestAngebot(restAngebot)),
            tap(() => this.updateFerienbetreuungContainerStore(containerId)),
        );
    }

    public falscheAngabenAngebot(
        containerId: string,
        angebot: TSFerienbetreuungAngabenAngebot,
    ): Observable<TSFerienbetreuungAngabenAngebot> {
        return this.http.put(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/angebot/falsche-angaben`,
            this.ebeguRestUtil.ferienbetreuungAngebotToRestObject({}, angebot),
        ).pipe(
            map(restAngebot => this.parseRestAngebot(restAngebot)),
            tap(() => this.updateFerienbetreuungContainerStore(containerId)),
        );
    }

    private parseRestAngebot(restAngebot: any): TSFerienbetreuungAngabenAngebot {
        return this.ebeguRestUtil.parseFerienbetreuungAngebot(
            new TSFerienbetreuungAngabenAngebot(),
            restAngebot,
        );
    }

    public nutzungAbschliessen(
        containerId: string,
        nutzung: TSFerienbetreuungAngabenNutzung,
    ): Observable<TSFerienbetreuungAngabenNutzung> {
        return this.http.put(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/nutzung/abschliessen`,
            this.ebeguRestUtil.ferienbetreuungNutzungToRestObject({}, nutzung),
        ).pipe(
            map(restNutzung => this.parseRestNutzung(restNutzung)),
            tap(() => this.updateFerienbetreuungContainerStore(containerId)),
        );
    }
}
