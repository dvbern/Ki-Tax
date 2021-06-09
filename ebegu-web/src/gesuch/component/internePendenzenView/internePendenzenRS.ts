import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {CONSTANTS} from '../../../app/core/constants/CONSTANTS';
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


}
