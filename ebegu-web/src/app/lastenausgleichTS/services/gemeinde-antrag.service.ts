import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, of} from 'rxjs';
import {TSGemeindeAntragTyp} from '../../../models/enums/TSGemeindeAntragTyp';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSGemeindeAntrag} from '../../../models/gemeindeantrag/TSGemeindeAntrag';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TSDateRange} from '../../../models/types/TSDateRange';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import * as moment from 'moment';

@Injectable({
    providedIn: 'root',
})
export class GemeindeAntragService {

    private readonly API_BASE_URL = `${CONSTANTS.REST_API}gemeindeantrag`;

    public constructor(private readonly http: HttpClient) {
    }

    public getAllGemeindeAntraege(): Observable<TSGemeindeAntrag[]> {
        // return this.http.get<TSGemeindeAntrag[]>(this.API_BASE_URL);
        return of(this.createDummyData(10));
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
}
