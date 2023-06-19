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
import {Observable, of, ReplaySubject} from 'rxjs';
import {filter, map, mergeMap, tap} from 'rxjs/operators';
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';
import {TSFerienbetreuungAngabenAngebot} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenAngebot';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSFerienbetreuungAngabenKostenEinnahmen} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenKostenEinnahmen';
import {TSFerienbetreuungAngabenNutzung} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenNutzung';
import {TSFerienbetreuungAngabenStammdaten} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenStammdaten';
import {EbeguRestUtil} from '../../../../utils/EbeguRestUtil';
import {CONSTANTS, HTTP_CODES} from '../../../core/constants/CONSTANTS';
import {LogFactory} from '../../../core/logging/LogFactory';
import {TSFerienbetreuungBerechnung} from '../ferienbetreuung-kosten-einnahmen/TSFerienbetreuungBerechnung';

const LOG = LogFactory.createLog('FerienbetreuungService');

@Injectable({
    providedIn: 'root'
})
export class FerienbetreuungService {

    private readonly API_BASE_URL = `${CONSTANTS.REST_API}ferienbetreuung`;
    private readonly ebeguRestUtil = new EbeguRestUtil();
    // return last item but don't provide initial value like BehaviourSubject does
    private ferienbetreuungAngabenContainerStore =
        new ReplaySubject<TSFerienbetreuungAngabenContainer>(1);

    private ferienbetreuungAngabenContainerVorjahrStore =
        new ReplaySubject<TSFerienbetreuungAngabenContainer>(1);

    public constructor(
        private readonly http: HttpClient,
        private readonly einstellungRS: EinstellungRS
    ) {
    }

    public updateFerienbetreuungContainerStores(id: string): void {
        this.updateFerienBetreuungContainerStore(id);
        this.updateFerienBetreuungVorgaengerContainerStore(id);
    }

    private updateFerienBetreuungContainerStore(id: string): void {
        const url = `${this.API_BASE_URL}/find/${encodeURIComponent(id)}`;
        this.http.get<TSFerienbetreuungAngabenContainer>(url)
            .pipe(
                map(restContainer => this.ebeguRestUtil.parseFerienbetreuungContainer(
                    new TSFerienbetreuungAngabenContainer(),
                    restContainer
                ))
            )
            .subscribe(container => {
                this.ferienbetreuungAngabenContainerStore.next(container);
            }, error => LOG.error(error));
    }

    private updateFerienBetreuungVorgaengerContainerStore(id: string): void {
        const url = `${this.API_BASE_URL}/vorgaenger/${encodeURIComponent(id)}`;
        this.http.get<TSFerienbetreuungAngabenContainer>(url, {observe: 'response'})
            .pipe(
                filter( response => response.status === HTTP_CODES.OK),
                map(response => this.ebeguRestUtil.parseFerienbetreuungContainer(
                    new TSFerienbetreuungAngabenContainer(),
                    response.body
                ))
            )
            .subscribe(container => {
                this.ferienbetreuungAngabenContainerVorjahrStore.next(container);
            }, error => LOG.error(error));
    }

    public getFerienbetreuungContainer(): Observable<TSFerienbetreuungAngabenContainer> {
        return this.ferienbetreuungAngabenContainerStore.asObservable();
    }

    public getFerienbetreuungVorgaengerContainer(): Observable<TSFerienbetreuungAngabenContainer> {
        return this.ferienbetreuungAngabenContainerVorjahrStore.asObservable();
    }

    public emptyStores(): void {
        this.ferienbetreuungAngabenContainerStore = new ReplaySubject<TSFerienbetreuungAngabenContainer>(1);
        this.ferienbetreuungAngabenContainerVorjahrStore = new ReplaySubject<TSFerienbetreuungAngabenContainer>(1);
    }

    public saveKommentar(containerId: string, kommentar: string): Observable<void> {
        return this.http.put<void>(
            `${this.API_BASE_URL}/saveKommentar/${encodeURIComponent(containerId)}`,
            kommentar
        );
    }

    public saveVerantwortlicher(containerId: string, username: string): void {
        this.http.put<void>(
            `${this.API_BASE_URL}/saveVerantworlicher/${encodeURIComponent(containerId)}`,
            username
        ).subscribe(() => {}, error => {
            LOG.error(error);
        });
    }

    public saveStammdaten(containerId: string, stammdaten: TSFerienbetreuungAngabenStammdaten):
        Observable<TSFerienbetreuungAngabenStammdaten> {
        return this.http.put<any>(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/stammdaten/save`,
            this.ebeguRestUtil.ferienbetreuungStammdatenToRestObject({}, stammdaten)
        ).pipe(map(restStammdaten => this.parseRestStammdaten(restStammdaten)));
    }

    private parseRestStammdaten(restStammdaten: any): TSFerienbetreuungAngabenStammdaten {
        return this.ebeguRestUtil.parseFerienbetreuungStammdaten(
            new TSFerienbetreuungAngabenStammdaten(),
            restStammdaten
        );
    }

    public saveAngebot(containerId: string, angebot: TSFerienbetreuungAngabenAngebot):
        Observable<TSFerienbetreuungAngabenAngebot> {
        return this.http.put<any>(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/angebot/save`,
            this.ebeguRestUtil.ferienbetreuungAngebotToRestObject({}, angebot)
        ).pipe(map(restAngebot => this.parseRestAngebot(restAngebot)));
    }

    public saveNutzung(containerId: string, nutzung: TSFerienbetreuungAngabenNutzung):
        Observable<TSFerienbetreuungAngabenNutzung> {
        return this.http.put<any>(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/nutzung/save`,
            this.ebeguRestUtil.ferienbetreuungNutzungToRestObject({}, nutzung)
        ).pipe(map(restNutzung => this.parseRestNutzung(restNutzung)));
    }

    private parseRestNutzung(restNutzung: any): TSFerienbetreuungAngabenNutzung {
        return this.ebeguRestUtil.parseFerienbetreuungNutzung(
            new TSFerienbetreuungAngabenNutzung(),
            restNutzung
        );
    }

    public saveKostenEinnahmen(containerId: string, kostenEinnahmen: TSFerienbetreuungAngabenKostenEinnahmen):
        Observable<TSFerienbetreuungAngabenKostenEinnahmen> {
        return this.http.put<any>(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/kostenEinnahmen/save`,
            this.ebeguRestUtil.ferienbetreuungKostenEinnahmenToRestObject({}, kostenEinnahmen)
        ).pipe(map(restKostenEinnahmen => this.parseRestKostenEinnahmen(restKostenEinnahmen)));
    }

    public saveBerechnung(containerId: string, berechnung: TSFerienbetreuungBerechnung):
        Observable<TSFerienbetreuungBerechnung> {

        return this.http.put<any>(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/berechnung/save`,
            this.ebeguRestUtil.parseFerienbetreuungBerechnungenToRestObject({}, berechnung)
        ).pipe(map(berechnungen => this.parseRestBerechnung(berechnungen)));
    }

    private parseRestKostenEinnahmen(restKostenEinnahmen: any): TSFerienbetreuungAngabenKostenEinnahmen {
        return this.ebeguRestUtil.parseFerienbetreuungKostenEinnahmen(
            new TSFerienbetreuungAngabenKostenEinnahmen(),
            restKostenEinnahmen
        );
    }

    public angebotAbschliessen(
        containerId: string,
        angebot: TSFerienbetreuungAngabenAngebot
    ): Observable<TSFerienbetreuungAngabenAngebot> {
        return this.http.put(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/angebot/abschliessen`,
            this.ebeguRestUtil.ferienbetreuungAngebotToRestObject({}, angebot)
        ).pipe(
            map(restAngebot => this.parseRestAngebot(restAngebot)),
            tap(() => this.updateFerienbetreuungContainerStores(containerId))
        );
    }

    public falscheAngabenAngebot(
        containerId: string,
        angebot: TSFerienbetreuungAngabenAngebot
    ): Observable<TSFerienbetreuungAngabenAngebot> {
        return this.http.put(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/angebot/falsche-angaben`,
            this.ebeguRestUtil.ferienbetreuungAngebotToRestObject({}, angebot)
        ).pipe(
            map(restAngebot => this.parseRestAngebot(restAngebot)),
            tap(() => this.updateFerienbetreuungContainerStores(containerId))
        );
    }

    private parseRestAngebot(restAngebot: any): TSFerienbetreuungAngabenAngebot {
        return this.ebeguRestUtil.parseFerienbetreuungAngebot(
            new TSFerienbetreuungAngabenAngebot(),
            restAngebot
        );
    }

    public nutzungAbschliessen(
        containerId: string,
        nutzung: TSFerienbetreuungAngabenNutzung
    ): Observable<TSFerienbetreuungAngabenNutzung> {
        return this.http.put(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/nutzung/abschliessen`,
            this.ebeguRestUtil.ferienbetreuungNutzungToRestObject({}, nutzung)
        ).pipe(
            map(restNutzung => this.parseRestNutzung(restNutzung)),
            tap(() => this.updateFerienbetreuungContainerStores(containerId))
        );
    }

    public falscheAngabenNutzung(
        containerId: string,
        nutzung: TSFerienbetreuungAngabenNutzung
    ): Observable<TSFerienbetreuungAngabenNutzung> {
        return this.http.put(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/nutzung/falsche-angaben`,
            this.ebeguRestUtil.ferienbetreuungNutzungToRestObject({}, nutzung)
        ).pipe(
            map(restNutzung => this.parseRestNutzung(restNutzung)),
            tap(() => this.updateFerienbetreuungContainerStores(containerId))
        );
    }

    public kostenEinnahmenAbschliessen(
        containerId: string,
        kostenEinnahmen: TSFerienbetreuungAngabenKostenEinnahmen
    ): Observable<TSFerienbetreuungAngabenKostenEinnahmen> {
        return this.http.put(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/kostenEinnahmen/abschliessen`,
            this.ebeguRestUtil.ferienbetreuungKostenEinnahmenToRestObject({}, kostenEinnahmen)
        ).pipe(
            map(restNutzung => this.parseRestKostenEinnahmen(restNutzung)),
            tap(() => this.updateFerienbetreuungContainerStores(containerId))
        );
    }

    public falscheAngabenKostenEinnahmen(
        containerId: string,
        kostenEinnahmen: TSFerienbetreuungAngabenKostenEinnahmen
    ): Observable<TSFerienbetreuungAngabenKostenEinnahmen> {
        return this.http.put(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/kostenEinnahmen/falsche-angaben`,
            this.ebeguRestUtil.ferienbetreuungKostenEinnahmenToRestObject({}, kostenEinnahmen)
        ).pipe(
            map(restNutzung => this.parseRestKostenEinnahmen(restNutzung)),
            tap(() => this.updateFerienbetreuungContainerStores(containerId))
        );
    }

    public stammdatenAbschliessen(
        containerId: string,
        stammdaten: TSFerienbetreuungAngabenStammdaten
    ): Observable<TSFerienbetreuungAngabenStammdaten> {
        return this.http.put(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/stammdaten/abschliessen`,
            this.ebeguRestUtil.ferienbetreuungStammdatenToRestObject({}, stammdaten)
        ).pipe(
            map(restStammdaten => this.parseRestStammdaten(restStammdaten)),
            tap(() => this.updateFerienbetreuungContainerStores(containerId))
        );
    }

    public falscheAngabenStammdaten(
        containerId: string,
        stammdaten: TSFerienbetreuungAngabenStammdaten
    ): Observable<TSFerienbetreuungAngabenStammdaten> {
        return this.http.put(
            `${this.API_BASE_URL}/${encodeURIComponent(containerId)}/stammdaten/falsche-angaben`,
            this.ebeguRestUtil.ferienbetreuungStammdatenToRestObject({}, stammdaten)
        ).pipe(
            map(restStammdaten => this.parseRestStammdaten(restStammdaten)),
            tap(() => this.updateFerienbetreuungContainerStores(containerId))
        );
    }

    public ferienbetreuungAngabenGeprueft(
        container: TSFerienbetreuungAngabenContainer
    ): Observable<any> {

        return this.einstellungRS.getPauschalbetraegeFerienbetreuung(container)
            .pipe(
                mergeMap(([pauschale, pauschaleSonderschueler]) => {
                    container.calculateBerechnungen(pauschale, pauschaleSonderschueler);
                    return of(container);
                }),
                mergeMap(containerUpdated => this.setContainerToGeprueft(containerUpdated)),
                tap(() => this.updateFerienbetreuungContainerStores(container.id))
            );

    }

    private setContainerToGeprueft(container: TSFerienbetreuungAngabenContainer): Observable<any> {
        return this.http.put(
            `${this.API_BASE_URL}/geprueft/${encodeURIComponent(container.id)}`,
            this.ebeguRestUtil.ferienbetreuungContainerToRestObject({}, container)
        );
    }

    public ferienbetreuungAngabenFreigeben(
        container: TSFerienbetreuungAngabenContainer
    ): Observable<TSFerienbetreuungAngabenContainer> {
        return this.http.put(
            `${this.API_BASE_URL}/freigeben/${encodeURIComponent(container.id)}`,
            {}
        ).pipe(
            map(
                restAngaben => this.ebeguRestUtil.parseFerienbetreuungContainer(new TSFerienbetreuungAngabenContainer(),
                    restAngaben)
            ),
            tap(() => this.updateFerienbetreuungContainerStores(container.id))
        );
    }

    public zurueckAnGemeinde(
        container: TSFerienbetreuungAngabenContainer
    ): Observable<TSFerienbetreuungAngabenContainer> {
        return this.http.put(
            `${this.API_BASE_URL}/zurueck-an-gemeinde/${encodeURIComponent(container.id)}`,
            {}
        ).pipe(
            map(restAngaben => this.ebeguRestUtil.parseFerienbetreuungContainer(new TSFerienbetreuungAngabenContainer(),
                restAngaben)),
            tap(() => this.updateFerienbetreuungContainerStores(container.id))
        );

    }

    public abschliessen(container: TSFerienbetreuungAngabenContainer):
        Observable<TSFerienbetreuungAngabenContainer> {
        return this.http.put(
            `${this.API_BASE_URL}/abschliessen/${encodeURIComponent(container.id)}`,
            {}
        ).pipe(
            map(restAngaben => this.ebeguRestUtil.parseFerienbetreuungContainer(new TSFerienbetreuungAngabenContainer(),
                restAngaben)),
            tap(() => this.updateFerienbetreuungContainerStores(container.id))
        );
    }

    public zurueckAnKanton(container: TSFerienbetreuungAngabenContainer):
        Observable<TSFerienbetreuungAngabenContainer> {
        return this.http.put(
            `${this.API_BASE_URL}/zurueck-an-kanton/${encodeURIComponent(container.id)}`,
            {}
        ).pipe(
            map(restAngaben => this.ebeguRestUtil.parseFerienbetreuungContainer(new TSFerienbetreuungAngabenContainer(),
                restAngaben)),
            tap(() => this.updateFerienbetreuungContainerStores(container.id))
        );
    }

    public generateFerienbetreuungReport(container: TSFerienbetreuungAngabenContainer): Observable<BlobPart> {
        return this.http.get(`${this.API_BASE_URL}/${container.id}/report`, {responseType: 'blob'});
    }

    private parseRestBerechnung(berechnungen: any): TSFerienbetreuungBerechnung {
        return this.ebeguRestUtil.parseFerienbetreuungBerechnung(
            new TSFerienbetreuungBerechnung(),
            berechnungen
        );
    }
}
