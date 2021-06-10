import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {CONSTANTS} from '../../../app/core/constants/CONSTANTS';
import {TSGesuch} from '../../../models/TSGesuch';
import {TSInternePendenz} from '../../../models/TSInternePendenz';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

@Injectable({
    providedIn: 'root'
})
export class InternePendenzenRS {

    public readonly serviceURL: string = `${CONSTANTS.REST_API}gesuch/internependenz`;
    private readonly ebeguRestUtil = new EbeguRestUtil();

    public constructor(
        private http: HttpClient
    ) {
    }

    public createInternePendenz(internePendenz: TSInternePendenz): Observable<TSInternePendenz> {
        return this.http.post(
            this.serviceURL,
            this.ebeguRestUtil.internePendenzToRestObject({}, internePendenz)
        ).pipe(map(pendenzFromServer => {
            return this.ebeguRestUtil.parseInternePendenz(new TSInternePendenz(), pendenzFromServer)
        }));
    }

    public updateInternePendenz(internePendenz: TSInternePendenz): Observable<TSInternePendenz> {
        return this.http.put(
            this.serviceURL,
            this.ebeguRestUtil.internePendenzToRestObject({}, internePendenz)
        ).pipe(map(pendenzFromServer => {
            return this.ebeguRestUtil.parseInternePendenz(new TSInternePendenz(), pendenzFromServer)
        }));
    }

    public deleteInternePendenz(internePendenz: TSInternePendenz): Observable<void> {
        return this.http.delete<void>(
            `${this.serviceURL}/${internePendenz.id}`,
        );
    }

    public findInternePendenzenForGesuch(gesuch: TSGesuch): Observable<TSInternePendenz[]> {
        return this.http.get<any[]>(
            `${this.serviceURL}/all/${gesuch.id}`
        ).pipe(map(pendenzenFromServer => {
            return pendenzenFromServer.map(pendenzFromServer => {
                return this.ebeguRestUtil.parseInternePendenz(new TSInternePendenz(), pendenzFromServer);
            });
        }));
    }

    public countInternePendenzenForGesuch(gesuch: TSGesuch): Observable<number> {
        return this.http.get<number>(
            `${this.serviceURL}/count/${gesuch.id}`
        );
    }


}
