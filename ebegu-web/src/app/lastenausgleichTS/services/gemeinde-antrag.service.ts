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

    public getAllGemeindeAntraege(filter: DVAntragListFilter): Observable<TSGemeindeAntrag[]> {
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
            params
        }).pipe(
            map(antraege => this.ebeguRestUtil.parseGemeindeAntragList(antraege))
        );
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
