import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import * as moment from 'moment';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSGemeindeAntragTyp} from '../../../models/enums/TSGemeindeAntragTyp';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSGemeindeAntrag} from '../../../models/gemeindeantrag/TSGemeindeAntrag';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TSDateRange} from '../../../models/types/TSDateRange';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {DVAntragListFilter} from '../../shared/interfaces/DVAntragListFilter';

@Injectable({
    providedIn: 'root',
})
export class GemeindeAntragService {

    private readonly API_BASE_URL = `${CONSTANTS.REST_API}gemeindeantrag`;
    private readonly ebeguRestUtil = new EbeguRestUtil();

    public constructor(private readonly http: HttpClient) {
    }

    public getGemeindeAntraege(filter: DVAntragListFilter, sort: {
        predicate?: string,
        reverse?: boolean
    }): Observable<TSGemeindeAntrag[]> {
        let params = new HttpParams();
        if (filter.gemeinde) {
            params = params.append('gemeinde', filter.gemeinde);
        }
        if (filter.gesuchsperiodeString) {
            params = params.append('periode', filter.gesuchsperiodeString);
        }
        if (filter.antragTyp) {
            params = params.append('typ', filter.antragTyp);
        }
        if (filter.status) {
            params = params.append('status', filter.status);
        }
        return this.http.get<TSGemeindeAntrag[]>(this.API_BASE_URL, {
            params,
        }).pipe(
            map(antraege => this.ebeguRestUtil.parseGemeindeAntragList(antraege)),
            map(antraege => {
                switch (sort.predicate) {
                    case 'status':
                        return sort.reverse ?
                            antraege.sort((a, b) => a.statusString.localeCompare(b.statusString)) :
                            antraege.sort((a, b) => b.statusString.localeCompare(a.statusString));
                    case 'gemeinde':
                        return sort.reverse ?
                            antraege.sort((a, b) => a.gemeinde.name.localeCompare(b.gemeinde.name)) :
                            antraege.sort((a, b) => b.gemeinde.name.localeCompare(a.gemeinde.name));
                    case 'antragTyp':
                        return sort.reverse ?
                            antraege.sort((a, b) => a.gemeindeAntragTyp.localeCompare(b.gemeindeAntragTyp)) :
                            antraege.sort((a, b) => b.gemeindeAntragTyp.localeCompare(a.gemeindeAntragTyp));
                    case 'gesuchsperiodeString':
                        return sort.reverse ?
                            antraege.sort((a, b) =>
                                a.gesuchsperiode.gesuchsperiodeString.localeCompare(b.gesuchsperiode.gesuchsperiodeString)) :
                            antraege.sort((a, b) =>
                                b.gesuchsperiode.gesuchsperiodeString.localeCompare(a.gesuchsperiode.gesuchsperiodeString));
                    default:
                        return antraege;
                }
            }));
    }

    private createDummyData(n: number): TSGemeindeAntrag[] {
        const dummyGemeindeNamen = ['Paris', 'Belp', 'London'];
        const dummyData = [];
        for (let i = 0; i < n; i++) {
            const dummyGemeinde = new TSGemeinde();
            dummyGemeinde.name = dummyGemeindeNamen[Math.floor(Math.random() * dummyGemeindeNamen.length)];
            const dummyGemeindeAntrag = new TSGemeindeAntrag();
            dummyGemeindeAntrag.gemeinde = dummyGemeinde;
            const dummyGesuchsperiode = new TSGesuchsperiode(TSGesuchsperiodeStatus.AKTIV,
                new TSDateRange(moment('31-01-2019'), (moment('01-08-2019'))));
            dummyGemeindeAntrag.gemeindeAntragTyp = TSGemeindeAntragTyp.LASTENAUSGLEICH_TAGESSCHULEN;
            dummyGemeindeAntrag.gesuchsperiode = dummyGesuchsperiode;
            dummyGemeindeAntrag.statusString = 'Neu';

            dummyData.push(dummyGemeindeAntrag);
        }

        return dummyData;
    }

    public getTypes(): string[] {
        return [TSGemeindeAntragTyp.LASTENAUSGLEICH_TAGESSCHULEN, TSGemeindeAntragTyp.FERIENBETREUUNG];
    }

    public createAntrag(toCreate: { periode: string, antragTyp: string }): Observable<TSGemeindeAntrag[]> {
        return this.http.post<TSGemeindeAntrag[]>(`${this.API_BASE_URL}/create/${toCreate.antragTyp}/gesuchsperiode/${toCreate.periode}`,
            toCreate)
            .pipe(map(jaxAntrag => this.ebeguRestUtil.parseGemeindeAntragList(jaxAntrag)));
    }
}
